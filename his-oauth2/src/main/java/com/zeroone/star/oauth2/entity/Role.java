package com.zeroone.star.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 角色表 (whale_roles)
 * </p>
 * @author 阿伟
 */
@Getter
@Setter
@ToString
@TableName("whale_roles")
public class Role implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private String id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码（权限控制关键字）
     */
    private String code;

    /**
     * 角色描述
     */
    private String description;


}
