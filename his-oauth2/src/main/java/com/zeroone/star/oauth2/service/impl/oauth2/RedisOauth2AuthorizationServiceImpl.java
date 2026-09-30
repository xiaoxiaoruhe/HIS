package com.zeroone.star.oauth2.service.impl.oauth2;

import com.zeroone.star.project.constant.RedisConstant;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * <p>
 * 描述：用于将授权信息存储到Redis中
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public class RedisOauth2AuthorizationServiceImpl implements OAuth2AuthorizationService {
    /**
     * RedisTemplate对象，用于操作Redis数据库
     */
    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 创建一个Redis序列化器,使用JDK序列化器来序列化值
     */
    private final RedisSerializer<Object> valueSerializer = new JdkSerializationRedisSerializer();
    /**
     * 创建一个Redis序列化器,使用String序列化器来序列化键
     */
    private final RedisSerializer<String> stringSerializer = new StringRedisSerializer();
    /**
     * 缓存的TTL（10小时）
     */
    private static final Duration TTL = Duration.ofHours(10);

    /**
     * 构造函数，传入RedisTemplate对象
     * @param redisTemplate RedisTemplate对象
     */
    public RedisOauth2AuthorizationServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 动态计算Token的剩余有效期
     */
    private Duration calculateTtl(OAuth2Authorization.Token<? extends OAuth2Token> token) {
        return Optional.ofNullable(token.getToken().getExpiresAt())
                .map(expiresAt -> Duration.between(Instant.now(), expiresAt))
                // 如果过期了（负数或零），给一个极小值（1秒），让它在Redis里立即失效
                .map(duration -> duration.isNegative() ? Duration.ofSeconds(1) : duration)
                // 如果没有过期时间，使用全局默认保底时间 (10h)
                .orElse(TTL);
    }

    /**
     * 删除旧的凭证数据，包括id token以及access token，针对于刷新令牌逻辑数据清理
     * @param auth 授权信息
     */
    private void removeOldTokens(OAuth2Authorization auth) {
        // 1. 获取Redis中已有的旧数据
        OAuth2Authorization oldAuth = this.findById(auth.getId());
        if (oldAuth != null) {
            List<String> keysToDelete = new ArrayList<>();
            // 2. 检查并清理旧的 Access Token (如果已变更)
            String oldAt = Optional.ofNullable(oldAuth.getAccessToken())
                    .map(t -> t.getToken().getTokenValue())
                    .orElse(null);
            String newAt = Optional.ofNullable(auth.getAccessToken())
                    .map(t -> t.getToken().getTokenValue())
                    .orElse(null);
            if (oldAt != null && !oldAt.equals(newAt)) {
                keysToDelete.add(buildKey("access_token", oldAt));
                keysToDelete.add(RedisConstant.ACCESS_TOKEN_PREFIX + oldAt);
            }

            // 3. 检查并清理旧的 OIDC ID Token (如果已变更)
            String oldIdToken = Optional.ofNullable(oldAuth.getToken(OidcIdToken.class))
                    .map(t -> t.getToken().getTokenValue())
                    .orElse(null);
            String newIdToken = Optional.ofNullable(auth.getToken(OidcIdToken.class))
                    .map(t -> t.getToken().getTokenValue())
                    .orElse(null);
            if (oldIdToken != null && !oldIdToken.equals(newIdToken)) {
                keysToDelete.add(buildKey("id_token", oldIdToken));
            }

            // 4. 执行删除旧凭证操作
            if (!keysToDelete.isEmpty()) {
                this.redisTemplate.delete(keysToDelete);
            }
        }
    }

    /**
     * 保存Token索引
     * @param auth 授权信息
     */
    private void saveTokenIndexes(OAuth2Authorization auth) {
        final String authId = auth.getId();
        // 映射所有类型的Token索引
        record TokenInfo(String type, String value, Duration ttl) {
        }
        List<TokenInfo> tokenInfos = new ArrayList<>();

        // 映射授权码 (Code)
        Optional.ofNullable(auth.getToken(OAuth2AuthorizationCode.class))
                .ifPresent(t -> tokenInfos.add(new TokenInfo("code", t.getToken().getTokenValue(), calculateTtl(t))));
        // 映射 AccessToken
        Optional.ofNullable(auth.getAccessToken())
                .ifPresent(t -> {
                    String val = t.getToken().getTokenValue();
                    tokenInfos.add(new TokenInfo("access_token", val, calculateTtl(t)));
                    saveReadableForShared(val, auth, calculateTtl(t));
                });
        // 映射 RefreshToken
        Optional.ofNullable(auth.getRefreshToken())
                .ifPresent(t -> tokenInfos.add(new TokenInfo("refresh_token", t.getToken().getTokenValue(), calculateTtl(t))));
        // 映射 ID Token (用于OIDC退出登录)
        Optional.ofNullable(auth.getToken(OidcIdToken.class))
                .ifPresent(t -> tokenInfos.add(new TokenInfo("id_token", t.getToken().getTokenValue(), calculateTtl(t))));

        // 批量写入 Redis 索引
        tokenInfos.forEach(info -> redisTemplate.opsForValue().set(buildKey(info.type(), info.value()), authId, info.ttl()));
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");

        // 1. 判断是否已被撤销
        if (isInvalidated(authorization)) {
            this.remove(authorization);
            return;
        }
        // 删除旧的凭证数据
        removeOldTokens(authorization);

        // 2. 正常保存逻辑
        // 使用JDK序列化（byte[]）保存主记录，确保SAS内部类100%兼容
        String idKey = buildKey("id", authorization.getId());
        byte[] keyBytes = stringSerializer.serialize(idKey);
        byte[] valueBytes = valueSerializer.serialize(authorization);
        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.stringCommands().setEx(keyBytes, TTL.toSeconds(), valueBytes));
        if (!result) {
            return;
        }

        // 3. 建立索引映射
        saveTokenIndexes(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");

        // 构建Redis的key
        String idKey = buildKey("id", id);
        byte[] keyBytes = stringSerializer.serialize(idKey);

        // 强制以原始字节读取，避开外部String/Jackson序列化器
        byte[] valueBytes = redisTemplate.execute((RedisCallback<byte[]>) connection -> connection.stringCommands().get(keyBytes));

        // 反序列化成OAuth2Authorization对象
        return (OAuth2Authorization) valueSerializer.deserialize(valueBytes);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        String id = null;
        if (tokenType != null) {
            // 如果有明确类型，按类型拼接key查找
            id = (String) redisTemplate.opsForValue().get(buildKey(tokenType.getValue(), token));
        } else {
            // 依次尝试access_token,refresh_token,code
            String[] types = {"access_token", "refresh_token", "code", "id_token"};
            for (String type : types) {
                id = (String) redisTemplate.opsForValue().get(buildKey(type, token));
                if (id != null) {
                    break;
                }
            }
        }
        return id != null ? findById(id) : null;
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        // 使用Stream收集所有需要删除的Key
        List<String> keys = Stream.of(
                        // 1. ID Key
                        Optional.of(buildKey("id", authorization.getId())),
                        // 2. 授权码索引
                        Optional.ofNullable(authorization.getToken(OAuth2AuthorizationCode.class))
                                .map(t -> buildKey("code", t.getToken().getTokenValue())),
                        // 3. AccessToken索引
                        Optional.ofNullable(authorization.getAccessToken())
                                .map(t -> t.getToken().getTokenValue())
                                .map(token -> List.of(buildKey("access_token", token), RedisConstant.ACCESS_TOKEN_PREFIX + token)),
                        // 4. RefreshToken索引
                        Optional.ofNullable(authorization.getRefreshToken())
                                .map(t -> t.getToken().getTokenValue())
                                .map(token -> List.of(buildKey("refresh_token", token), RedisConstant.REFRESH_TOKEN_PREFIX + token)),
                        // 5. OIDC ID Token索引
                        Optional.ofNullable(authorization.getToken(OidcIdToken.class))
                                .map(t -> buildKey("id_token", t.getToken().getTokenValue()))
                )
                // 过滤掉空的Optional并展开
                .flatMap(Optional::stream)
                // 处理List嵌套
                .flatMap(obj -> obj instanceof List<?> list ? list.stream() : Stream.of(obj))
                .map(Object::toString)
                .toList();
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 构建索引Key
     * @param type  索引类型
     * @param value 索引值
     * @return 索引Key
     */
    private String buildKey(String type, String value) {
        return "oauth2:auth:" + type + ":" + value;
    }

    /**
     * 判断凭证是否被撤销
     * @param authorization 授权对象
     * @return 如果撤销则返回true
     */
    private boolean isInvalidated(OAuth2Authorization authorization) {
        // 如果AccessToken或RefreshToken任何一个被标记为无效，则认为认证被撤销
        return Stream.of(
                        Optional.ofNullable(authorization.getAccessToken()).map(OAuth2Authorization.Token::isInvalidated),
                        Optional.ofNullable(authorization.getRefreshToken()).map(OAuth2Authorization.Token::isInvalidated)
                )
                .flatMap(Optional::stream)
                .anyMatch(Boolean::booleanValue);
    }

    /**
     * 存储一份可读的数据，方便其他服务读取，如：网关判断凭证是否注销
     * @param token 凭证
     * @param auth  授权对象
     * @param ttl   凭证的剩余有效期
     */
    private void saveReadableForShared(String token, OAuth2Authorization auth, Duration ttl) {
        // FIXME: 此处逻辑根据需求自行调整
        Map<String, Object> data = new HashMap<>(1);
        // 添加用户名
        data.put("principal", auth.getPrincipalName());
        // 这里我们就主要存储访问凭证
        redisTemplate.opsForValue().set(RedisConstant.ACCESS_TOKEN_PREFIX + token, data, ttl);
    }
}
