package com.zeroone.star.project.components.sms.aliyun;

import cn.hutool.json.JSONUtil;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 * 描述：短信认证服务组件
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Component
public class PnsComponent {
    /**
     * 注入key
     */
    @Resource
    private KeyConfig key;

    /**
     * 创建短信认证服务客户端
     * @return 短信认证服务客户端
     * @throws Exception 创建客户端异常
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(key.getAk())
                .setAccessKeySecret(key.getSk());
        // Endpoint请参考 https://api.aliyun.com/product/Dypnsapi
        config.endpoint = "dypnsapi.aliyuncs.com";
        return new Client(config);
    }

    /**
     * 发送短信验证码
     * @param phoneNumber    手机号码
     * @param signName       签名名称
     * @param templateCode   短信模板CODE
     * @param templateParams 模板参数值，key 模板参数名称 value 模板参数值
     * @return 发送结果
     */
    public PnsResult sendVerifyCode(String phoneNumber, String signName, String templateCode, Map<String, String> templateParams) {
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phoneNumber)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam(JSONUtil.toJsonStr(templateParams));
        try {
            Client client = createClient();
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request);
            SendSmsVerifyCodeResponseBody body = response.getBody();
            return PnsResult.builder()
                    .code(body.getCode())
                    .message(body.getMessage())
                    .accessDeniedDetail(body.getAccessDeniedDetail())
                    .bizId(body.model == null ? null : body.model.getBizId())
                    .outId(body.model == null ? null : body.model.getOutId())
                    .requestId(body.model == null ? body.getRequestId() : body.model.getRequestId())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            TeaException error = new TeaException(e.getMessage(), e);
            return PnsResult.builder()
                    .code(error.getCode())
                    .message(error.getMessage())
                    .build();
        }
    }

    /**
     * 校验验证码
     * @param phoneNumber 手机号码
     * @param verifyCode  验证码
     * @return 校验结果
     */
    public PnsResult checkVerifyCode(String phoneNumber, String verifyCode) {
        CheckSmsVerifyCodeRequest request = new CheckSmsVerifyCodeRequest()
                .setPhoneNumber(phoneNumber)
                .setVerifyCode(verifyCode);
        try {
            Client client = createClient();
            CheckSmsVerifyCodeResponse response = client.checkSmsVerifyCode(request);
            CheckSmsVerifyCodeResponseBody body = response.getBody();
            return PnsResult.builder()
                    .code(body.getCode())
                    .message(body.getMessage())
                    .accessDeniedDetail(body.getAccessDeniedDetail())
                    .outId(body.model == null ? null : body.model.getOutId())
                    .verifyResult(body.model == null ? null : body.model.getVerifyResult())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            TeaException error = new TeaException(e.getMessage(), e);
            return PnsResult.builder()
                    .code(error.getCode())
                    .message(error.getMessage())
                    .build();
        }
    }
}
