package com.zeroone.cloud.starter.oauth2.autoconfiguration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.zeroone.cloud.starter.oauth2.properties.Oauth2Properties;
import com.zeroone.cloud.starter.oauth2.service.impl.TokenEnhancerServiceImpl;
import com.zeroone.cloud.starter.oauth2.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * <p>
 * 描述：认证授权服务自动装配配置
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "zo.cloud.starter.oauth2", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(Oauth2Properties.class)
@Import({WebSecurityConfig.class, TokenEnhancerServiceImpl.class, UserDetailsServiceImpl.class})
public class Oauth2ServerAutoConfiguration {
    private final Oauth2Properties properties;

    @Autowired
    public Oauth2ServerAutoConfiguration(Oauth2Properties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2AuthorizationService.class)
    public OAuth2AuthorizationService authorizationService() {
        // 默认使用内存存储，可以在外部注入来覆盖默认的
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(
            JWKSource<SecurityContext> jwkSource,
            OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer) {
        // 1. 定义JWT生成器（用于生成Access Token 和 ID Token）
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtTokenCustomizer);
        // 2. 定义不透明的Access Token 生成器（如果不需要JWT格式的Access Token才会用到）
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        // 3. 定义Refresh Token生成器
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        // 4. 将它们组合在一起
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        // 1. 加载JKS密钥库
        ClassPathResource resource = new ClassPathResource(properties.getJks());
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(resource.getInputStream(), properties.getJksPassword().toCharArray());

        // 2. 从密钥库中读取密钥和证书
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(properties.getKeyAlias(), properties.getKeyPassword().toCharArray());
        Certificate certificate = keyStore.getCertificate(properties.getKeyAlias());
        RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();

        // 3. 构建RSAKey
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        // 4. 包装成JWKSet和JWKSource
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
