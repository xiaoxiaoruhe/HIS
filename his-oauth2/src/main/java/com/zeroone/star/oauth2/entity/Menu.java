package com.zeroone.star.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * 菜单 (whale_menus)
 * </p>
 * @author 阿伟
 */
@Getter
@Setter
@ToString
@TableName("whale_menus")
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private String id;

    /**
     * 菜单编码
     */
    private String code;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 图标
     */
    private String icon;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 关联权限名称
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * 父菜单 ID
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Integer isEnabled;

    /**
     * 是否隐藏
     */
    @TableField("is_hidden")
    private Integer isHidden;

    /**
     * 是否外链
     */
    @TableField("is_external")
    private Integer isExternal;

    /**
     * 外链 URL
     */
    @TableField("external_url")
    private String externalUrl;

    /**
     * 菜单类型 (menu/directory)
     */
    @TableField("menu_type")
    private String menuType;

    /**
     * 备注
     */
    private String remark;

}
