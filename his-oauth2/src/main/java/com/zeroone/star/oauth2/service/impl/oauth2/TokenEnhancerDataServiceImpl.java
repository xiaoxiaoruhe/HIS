package com.zeroone.star.oauth2.service.impl.oauth2;

import com.zeroone.cloud.oauth2.entity.SecurityUser;
import com.zeroone.cloud.starter.oauth2.service.TokenEnhancerDataService;
import com.zeroone.star.oauth2.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 描述：Token增强数据处理服务实现
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class TokenEnhancerDataServiceImpl implements TokenEnhancerDataService {
    @Override
    public Map<String, Object> enhance(SecurityUser securityUser) {
        Map<String, Object> info = new HashMap<>();
        // 如果是管理端数据库用户DO对象
        if (securityUser.getExtendsObject() instanceof User user) {
            // 用户ID
            info.put("id", user.getId());
            // 用户名（UserHolder 优先读 user_name，回退 sub）
            info.put("user_name", securityUser.getUsername());
            // 是否启用（SecurityUser 继承 Spring Security User，默认 true）
            info.put("is_active", securityUser.isEnabled() ? 1 : 0);
            // 角色列表（从 authorities 提取角色关键词）
            List<String> roles = securityUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            info.put("roles", roles);

            // ===== 扩展业务字段（与 UserHolder/UserDTO 契约对齐）=====
            // 真实姓名
            if (user.getName() != null) {
                info.put("name", user.getName());
            }
            // 医务人员编号
            if (user.getPractitionerId() != null) {
                info.put("practitionerId", user.getPractitionerId());
            }
            // 医务人员姓名
            if (user.getPractitionerName() != null) {
                info.put("practitionerName", user.getPractitionerName());
            }
            // 职称编码
            if (user.getTitleCode() != null) {
                info.put("titleCode", user.getTitleCode());
            }
            // 职称名称（暂无字典表，可能为空串）
            info.put("titleName", user.getTitleName() == null ? "" : user.getTitleName());
            // 组织单元集合（Map<组织单元编号, 组织单元编号>，名称暂无字典表）
            if (user.getOrganizationUnits() != null && !user.getOrganizationUnits().isEmpty()) {
                info.put("organizationUnits", user.getOrganizationUnits());
            }
        }
        return info;
    }
}
