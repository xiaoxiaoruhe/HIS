package com.zeroone.star.oauth2.service.impl.oauth2;

import cn.hutool.json.JSONUtil;
import com.zeroone.star.oauth2.entity.Menu;
import com.zeroone.star.oauth2.entity.Role;
import com.zeroone.star.oauth2.service.IMenuService;
import com.zeroone.star.oauth2.service.IRoleService;
import com.zeroone.star.project.constant.RedisConstant;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 描述：资源与角色缓存初始化服务
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class ResourcesServiceImpl {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private IMenuService menuService;
    @Resource
    private IRoleService roleService;

    /**
     * 加载资源与角色缓存
     * @param isClearCacheBeforeLoad 加载前是否清除缓存
     */
    public void loadData(boolean isClearCacheBeforeLoad) {
        // 清除缓存
        if (isClearCacheBeforeLoad) {
            redisTemplate.delete(RedisConstant.RESOURCE_ROLES_MAP);
        }

        // TODO：缓存权限资源逻辑,需要根据自己数据库设计来初始化--start
        // 定义缓存map，hashMap也可以，treeMap有排序，看起来整齐
        Map<String,String> resourceRolesMap = new TreeMap<>();
        // 1 获取所有菜单
        List<Menu> tMenus = menuService.listAllLinkUrl();
        tMenus.forEach(menu -> {
            // 2 获取菜单对应的角色
            List<Role> rolesMenu = roleService.listRoleByMenuPath(menu.getPath());
            // 3 把角色转成code
            List<String> roles = rolesMenu.stream().map(Role::getCode).toList();
            // 4 放入Map中
            resourceRolesMap.put(menu.getPath(), JSONUtil.toJsonStr(roles));
        });
        // TODO：缓存权限资源逻辑,需要根据自己数据库设计来初始化--end

        // 将资源缓存到redis
        redisTemplate.opsForHash().putAll(RedisConstant.RESOURCE_ROLES_MAP, resourceRolesMap);
    }

    @PostConstruct
    public void init() {
        loadData(false);
    }
}
