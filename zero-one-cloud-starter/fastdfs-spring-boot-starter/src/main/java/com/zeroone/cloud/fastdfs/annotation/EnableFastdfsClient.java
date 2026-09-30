package com.zeroone.cloud.fastdfs.annotation;

import com.zeroone.cloud.fastdfs.autoconfiguration.FastdfsAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * <p>
 * 描述：启用fastdfs-starter支持
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(FastdfsAutoConfiguration.class)
@Documented
public @interface EnableFastdfsClient {
}
