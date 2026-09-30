package com.zeroone.star.project.components.sms.aliyun;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 描述：发送消息响应结果
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Data
@Builder
public class PnsResult {
    /**
     * 响应成功
     */
    public static final String OK_CODE = "OK";
    /**
     * 验证成功
     */
    public static final String PASS_CODE = "PASS";
    /**
     * 请求状态码。
     * 返回OK代表请求成功。
     * 其他错误码，请参见<a href="https://help.aliyun.com/zh/pnvs/developer-reference/api-return-code">API错误码</a>。
     */
    private String code;
    /**
     * 状态码的描述。
     */
    private String message;
    /**
     * 访问被拒绝详细信息。
     */
    private String accessDeniedDetail;
    /**
     * 业务 ID。
     */
    private String bizId;
    /**
     * 外部流水号。
     */
    private String outId;
    /**
     * 请求ID。
     */
    private String requestId;
    /**
     * 短信验证码核验结果。取值：
     * PASS：短信验证码核验成功。
     * UNKNOWN：短信验证码核验失败
     */
    private String verifyResult;
}
