package com.zeroone.star.gateway.service.gateway;

import com.zeroone.cloud.starter.gateway.service.TokenExtendsValidate;
import com.zeroone.star.project.constant.RedisConstant;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 描述：这里实现注销凭证校验处理
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class TokenExtendsValidateImpl implements TokenExtendsValidate {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean isLogout(String token) {
        // FIXME：判断凭证是否注销，这里实现逻辑为判断凭证是否存在，存在则返回true，不存在则返回false，可以根据你的业务进行修改
        return !redisTemplate.hasKey(RedisConstant.ACCESS_TOKEN_PREFIX + token);
    }
}
