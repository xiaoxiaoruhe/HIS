package com.zeroone.cloud.starter.gateway.authorization;

import com.zeroone.cloud.starter.gateway.properties.GatewayProperties;
import com.zeroone.cloud.starter.gateway.service.LoadResourcesData;
import jakarta.annotation.Resource;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 描述：鉴权管理器，用于判断是否有资源的访问权限
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
    @Resource
    LoadResourcesData loadResourcesData;

    @Resource
    GatewayProperties properties;

    /**
     * 通过路径查询角色列表
     * @param path 路径名
     * @return 没有查询到返回空列表
     */
    private List<String> queryRoleListByPath(String path) {
        List<String> authorities = loadResourcesData.loadRolesByUrlPath(path);
        if (authorities == null) {
            return new ArrayList<>();
        }
        authorities = authorities.stream().map(roleName -> roleName = properties.getAuthorityPrefix() + roleName).collect(Collectors.toList());
        return authorities;
    }

    /**
     * 通过模式匹配的方式查询角色列表
     * 函数会从请求路径的结尾路径匹配，如果匹配不到，则去掉最后一个路径，再匹配一次，直到匹配到或者路径为空
     * @param path    请求地址
     * @param pattern 模式匹配符一般为**
     * @return 没有查询到返回空列表
     */
    private List<String> queryRoleListByPath(String path, String pattern) {
        int lastIndex = path.lastIndexOf("/");
        if (lastIndex != -1) {
            path = path.substring(0, lastIndex);
            String patternPath = path + "/" + pattern;
            List<String> authorities = queryRoleListByPath(patternPath);
            if (!authorities.isEmpty()) {
                return authorities;
            } else {
                return queryRoleListByPath(path, pattern);
            }
        }
        return new ArrayList<>();
    }

    @Deprecated
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        return this.authorize(authentication, context).cast(AuthorizationDecision.class);
    }

    @Override
    public Mono<AuthorizationResult> authorize(Mono<Authentication> authentication, AuthorizationContext context) {
        // 1 如果关闭鉴权功能，直接放行
        if (!properties.isOpenAuthorization()) {
            return Mono.just(new AuthorizationDecision(true));
        }

        // 2 获取当前路径可访问角色列表
        String path = context.getExchange().getRequest().getURI().getPath();
        // 2.1 首先精确匹配
        List<String> authorities = queryRoleListByPath(path);
        // 2.2 没有查询到结果，尝试模式匹配
        if (authorities.isEmpty()) {
            authorities = queryRoleListByPath(path, "**");
        }

        // 3 认证通过且角色匹配的用户可访问当前路径
        return authentication
                // 判断是否认证
                .filter(Authentication::isAuthenticated)
                // 获取权限对象列表(即：角色列表)
                .flatMapIterable(Authentication::getAuthorities)
                // 遍历获取权限对象(即：获取单个角色名)
                .map(GrantedAuthority::getAuthority)
                // 查看当前路径中是否包对应权限（即：是否包含角色名）
                .any(authorities::contains)
                // 根据判断结果构建一个鉴权对象
                .map(AuthorizationDecision::new)
                // 没有权限列表的情况，构建一个无权访问对象
                .defaultIfEmpty(new AuthorizationDecision(false))
                // 类型转换
                .cast(AuthorizationResult.class);
    }
}
