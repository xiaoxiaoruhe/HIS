package com.zeroone.cloud.fastdfs.autoconfiguration;

import com.zeroone.cloud.fastdfs.properties.FastdfsProperties;
import com.zeroone.cloud.fastdfs.service.FastdfsClientService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 描述：dfs自动装配
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@Configuration
@EnableConfigurationProperties(FastdfsProperties.class)
public class FastdfsAutoConfiguration {
    private final FastdfsProperties fastdfsProperties;

    public FastdfsAutoConfiguration(FastdfsProperties fastdfsProperties) {
        this.fastdfsProperties = fastdfsProperties;
    }

    @Bean
    @ConditionalOnMissingBean(FastdfsClientService.class)
    public FastdfsClientService fastdfsClientService() throws Exception {
        return new FastdfsClientService(fastdfsProperties);
    }
}