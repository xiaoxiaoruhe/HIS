package com.zeroone.star.project.config.swagger;

import cn.hutool.core.util.RandomUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 描述：Swagger配置
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {
    /**
     * 注入全局扩展参数
     * @return GlobalOpenApiCustomizer对象
     */
    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            // 根据@Tag上的排序，写入x-order
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>(1);
                    map.put("x-order", RandomUtil.randomInt(0, 100));
                    tag.setExtensions(map);
                });
            }
            if (openApi.getPaths() != null) {
                openApi.addExtension("x-test123", "333");
                openApi.getPaths().addExtension("x-abb", RandomUtil.randomInt(1, 100));
            }
            // 全局添加鉴权参数
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((s, pathItem) -> {
                    // 为所有接口添加鉴权
                    pathItem.readOperations().forEach(operation -> {
                        operation.addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
                    });
                });
            }
        };
    }

    /**
     * 构建OpenAPI对象
     * @return OpenAPI对象
     */
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(createApiInfo())
                // 添加全局安全认证
                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION))
                .components(new Components().addSecuritySchemes(HttpHeaders.AUTHORIZATION,
                        new SecurityScheme().name(HttpHeaders.AUTHORIZATION).type(SecurityScheme.Type.HTTP).scheme("bearer")));
    }

    /**
     * 构建文档说明对象
     * @return ApiInfo对象
     */
    private Info createApiInfo() {
        return new Info()
                .title("前后端API接口文档")
                .description("用于前后端交互，提供一套API说明")
                .version("1.0.0")
                .termsOfService("https://space.bilibili.com/1653229811/?spm_id_from=333.999.0.0")
                .contact(new Contact()
                        .name("零壹星球")
                        .url("https://space.bilibili.com/1653229811/?spm_id_from=333.999.0.0")
                        .email("01xq@mail.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }
}
