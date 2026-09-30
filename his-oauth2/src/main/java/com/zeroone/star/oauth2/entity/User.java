package com.zeroone.star.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * 用户表（whale_users）
 * </p>
 * @author 阿伟
 */
@Getter
@Setter
@ToString
@TableName("whale_users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户编号
     */
    @TableId(value = "id")
    private String id;

    /**
     * 账户名（whale_users.user_name）
     */
    @TableField("user_name")
    private String username;

    /**
     * 密码哈希（whale_users.password_hash）
     */
    @TableField("password_hash")
    private String password;

    /**
     * 真实姓名（whale_users.name）
     */
    @TableField("name")
    private String name;

    /**
     * 关联的医务人员编号（adm_practitioner.id），非持久化，由 LoadUserDetail 装配
     */
    @TableField(exist = false)
    private String practitionerId;

    /**
     * 医务人员姓名（adm_practitioner.name），非持久化
     */
    @TableField(exist = false)
    private String practitionerName;

    /**
     * 职称编码（adm_practitioner.dr_profttl_code），非持久化
     */
    @TableField(exist = false)
    private Integer titleCode;

    /**
     * 职称名称，非持久化；当前无编码字典表，暂置空
     */
    @TableField(exist = false)
    private String titleName;

    /**
     * 组织单元集合（whale_user_organization_units.organization_unit_id），key=value=组织单元编号，非持久化
     */
    @TableField(exist = false)
    private Map<String, String> organizationUnits;


}
