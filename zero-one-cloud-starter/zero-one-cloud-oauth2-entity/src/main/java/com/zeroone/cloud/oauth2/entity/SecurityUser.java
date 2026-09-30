package com.zeroone.cloud.oauth2.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 描述：权限认证用户实体
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class SecurityUser extends org.springframework.security.core.userdetails.User {
    /**
     * 关联一个扩展数据对象，一般可以设定为数据库用户表对应的DO对象
     */
    private final Object extendsObject;

    /**
     * 构造初始化
     * @param extendsObject 扩展数据对象
     * @param username      用户名
     * @param password      密码
     * @param authorities   权限列表
     */
    private SecurityUser(Object extendsObject,
                         String username,
                         String password,
                         Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.extendsObject = extendsObject;
    }

    /**
     * 获取扩展数据对象
     * @return 返回扩展数据对象
     */
    public Object getExtendsObject() {
        return extendsObject;
    }

    /**
     * 创建SecurityUser对象
     * @param extendsObject 扩展数据对象
     * @param username      用户名
     * @param password      密码
     * @param roles         角色名称列表
     * @return 返回SecurityUser对象
     */
    public static SecurityUser create(Object extendsObject,
                                      String username,
                                      String password,
                                      List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new SecurityUser(extendsObject, username, password, authorities);
    }
}
