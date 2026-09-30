package com.zeroone.star.project.vo.login;

import com.zeroone.star.project.utils.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 描述：树状菜单显示数据
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Data
public class MenuTreeVO extends TreeNode {
    @Schema(description = "序号", example = "1")
    private Integer id;
    @Schema(description = "菜单名称", example = "主页")
    private String text;
    @Schema(description = "图标", example = "dashboard")
    private String icon;
    @Schema(description = "路由地址", example = "/dashboard")
    private String href;
    @Schema(description = "父级菜单编号", example = "0")
    private Integer pid;

    @Schema(description = "节点包含的子节点")
    public List<MenuTreeVO> getChildren() {
        return childrenElementTrans();
    }
}
