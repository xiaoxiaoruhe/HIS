package com.zeroone.star.project.vo.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 描述：登录显示数据对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Schema(description = "登录显示对象")
@Data
public class LoginVO {
    @Schema(description = "用户唯一编号", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "是否启用 1 启用 0 禁用 ", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Byte isEnabled;

    @Schema(description = "用户角色列表", example = "['ADMIN','MANAGER']", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> roles;
}
