package com.zeroone.star.oauth2.service.impl.oauth2;

import com.zeroone.cloud.starter.oauth2.service.TokenGranterStrategy;
import com.zeroone.cloud.starter.oauth2.service.grant.AbstractCustomAuthenticationConverter;
import com.zeroone.cloud.starter.oauth2.service.grant.AbstractCustomAuthenticationProvider;
import com.zeroone.cloud.starter.oauth2.service.grant.CustomGrantAuthenticationToken;
import com.zeroone.cloud.oauth2.entity.SecurityUser;
import com.zeroone.star.oauth2.config.Oauth2Properties;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/* OAuth2 密码模式授权策略，认证数据源为 whale_users。 */

/**
 * 这个接口是干嘛的：扩展 OAuth2 自定义登录方式。
 * 它把自定义授权模式拆成三块：grantType 定类型、Converter 解析请求、Provider 做认证。
 * 密码模式里做了什么：取 username/password → 查库 → 比对 BCrypt 密码 → 返回 SecurityUser。
 * 为什么要封装：降低扩展难度，后面加短信验证码登录只需要按模板填三块，不用动底层。
 */
@Component
public class PasswordTokenGranterStrategy implements TokenGranterStrategy {

    private static final String GRANT_TYPE = "password";

    @Resource
    private LoadUserDetailServiceImpl loadUserDetailService;

    @Resource
    private Oauth2Properties oauth2Properties;

    @Override
    public String getGrantType() {
        return GRANT_TYPE;
    }

    @Override
    public AuthenticationConverter getConverter() {
        return new AbstractCustomAuthenticationConverter<CustomGrantAuthenticationToken>(GRANT_TYPE) {
            @Override
            protected CustomGrantAuthenticationToken createAuthentication(Authentication clientPrincipal,
                                                                          Map<String, Object> parameters) {
                return new CustomGrantAuthenticationToken(GRANT_TYPE, clientPrincipal, parameters);
            }
        };
    }

    @Override
    public AuthenticationProvider getProvider(OAuth2AuthorizationService authorizationService,
                                              OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        // 返回一个自定义认证提供者实例，继承自 AbstractCustomAuthenticationProvider，
        // 由框架传入 OAuth2AuthorizationService（管理授权记录）和 tokenGenerator（签发 Token）
        return new AbstractCustomAuthenticationProvider(authorizationService, tokenGenerator) {
            @Override
            protected Authentication authenticateUser(CustomGrantAuthenticationToken authentication,
                                                      RegisteredClient registeredClient) {
                // 从认证对象的附加参数中安全地取出用户名
                String username = value(authentication, "username");
                // 从认证对象的附加参数中安全地取出密码
                String password = value(authentication, "password");

                // 校验用户名和密码是否为空（StringUtils.hasText 会同时判 null、空串、纯空格）
                // 任意一个为空则直接抛出 invalid_grant，避免后续空指针或无效查询
                if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                    throw new OAuth2AuthenticationException("invalid_grant");
                }

                // 根据用户名查询用户详情，必须返回 SecurityUser 而非原始数据库 User 对象。
                // 因为 JWT 自定义声明增强器（TokenEnhancerDataService）以 SecurityUser 为输入，
                // 如果返回原始 User，id、roles、人员与组织单元等业务字段将无法写入 access token，
                // 后续 /user/info 接口解析 Token 时无法还原当前用户信息。
                SecurityUser user = loadUserDetailService.loadUserDetail(username, oauth2Properties.getMgrId());

                // 校验用户是否存在，以及密码是否正确：
                // 1. user == null：账号不存在
                // 2. BCryptPasswordEncoder().matches()：用 BCrypt 算法比对明文密码与数据库哈希值
                //    bcryptHash() 用于去掉数据库中可能存在的 "{bcrypt}" 前缀
                // 统一抛 invalid_grant，不区分是账号错还是密码错，防止账号枚举攻击
                if (user == null || !new BCryptPasswordEncoder().matches(password, bcryptHash(user.getPassword()))) {
                    throw new OAuth2AuthenticationException("invalid_grant");
                }

                // 认证通过，返回包含 SecurityUser 的 Authentication 对象。
                // 第二个参数 null 表示凭证已擦除（密码不再保留），第三个参数是用户的权限列表。
                // 框架后续会把这个 Authentication 交给 JWT 增强器，将用户业务字段写入 Token。
                return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            }
        };
    }

    /**
     * 从 CustomGrantAuthenticationToken 的附加参数中安全地获取指定 key 的值。
     * 直接调用 get(key).toString() 在 key 不存在时会抛空指针，
     * 这里先判 null 再转字符串，避免 NPE。
     *
     * @param authentication 认证对象，内部持有前端传来的表单参数
     * @param key            参数名，如 "username"、"password"
     * @return 参数值，不存在则返回 null
     */
    private String value(CustomGrantAuthenticationToken authentication, String key) {
        Object value = authentication.getAdditionalParameters().get(key);
        return value == null ? null : value.toString();
    }

    /**
     * 处理数据库中密码字段的存储格式。
     * Spring Security 存储 BCrypt 哈希时可能带 "{bcrypt}" 前缀（如 "{bcrypt}$2a$10$..."），
     * 而 BCryptPasswordEncoder.matches() 期望接收纯哈希字符串，
     * 因此需要去掉前缀后再进行比对。
     *
     * @param passwordHash 数据库中的密码哈希值
     * @return 去掉 "{bcrypt}" 前缀后的纯哈希字符串
     */
    private String bcryptHash(String passwordHash) {
        return passwordHash != null && passwordHash.startsWith("{bcrypt}")
                ? passwordHash.substring("{bcrypt}".length()) : passwordHash;
    }
}
