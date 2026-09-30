package com.zeroone.star.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zeroone.star.login.entity.Menu;

import java.util.List;

/**
 * <p>
 * 菜单 Mapper 接口
 * </p>
 * @author 阿伟
 */
public interface MenuMapper extends BaseMapper<Menu> {
    /**
     * 通过角色名获取对应的菜单资源
     * @param roleName 角色名
     * @return 返回菜单列表
     */
    List<Menu> selectByRoleName(String roleName);
}
