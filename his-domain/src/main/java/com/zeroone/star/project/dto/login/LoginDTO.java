package com.zeroone.star.project.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 描述：用户登录传输数据
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Schema(description = "登录上传数据对象")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /**
     * 验证码
     */
    @Schema(description = "验证码", example = "999818")
    private String code;
}
