package com.zeroone.cloud.starter.gateway.service;

import java.util.List;

/**
 * <p>
 * 描述：加载资源数据接口，用于适配目标系统对应的资源查询操作
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public interface LoadResourcesData {
    /**
     * 获取指定url地址，可访问的角色列表
     * @param url 指定url地址
     * @return 可访问的角色列表
     */
    List<String> loadRolesByUrlPath(String url);
}
