package com.zeroone.star.oauth2.service.impl.oauth2;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zeroone.cloud.oauth2.entity.SecurityUser;
import com.zeroone.cloud.starter.oauth2.service.LoadUserDetailService;
import com.zeroone.star.oauth2.config.Oauth2Properties;
import com.zeroone.star.oauth2.entity.AdmPractitioner;
import com.zeroone.star.oauth2.entity.Role;
import com.zeroone.star.oauth2.entity.User;
import com.zeroone.star.oauth2.entity.WhaleUserOrganizationUnits;
import com.zeroone.star.oauth2.mapper.AdmPractitionerMapper;
import com.zeroone.star.oauth2.mapper.WhaleUserOrganizationUnitsMapper;
import com.zeroone.star.oauth2.service.IRoleService;
import com.zeroone.star.oauth2.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 描述：加载用户信息服务实现
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Service
public class LoadUserDetailServiceImpl implements LoadUserDetailService {

    @Resource
    Oauth2Properties properties;

    @Resource
    AdmPractitionerMapper practitionerMapper;

    @Resource
    WhaleUserOrganizationUnitsMapper userOrganizationUnitsMapper;

    @Override
    public SecurityUser loadUserDetail(String username, String clientId) throws UsernameNotFoundException {
        if (properties.getMgrId().equals(clientId)) {
            return loadUserDetailForMgr(username);
        } else if (properties.getUserId().equals(clientId)) {
            return loadUserDetailForUser(username);
        }
        throw new UsernameNotFoundException("登录客户端ID错误");
    }

    @Resource
    IUserService userService;
    @Resource
    IRoleService roleService;

    private SecurityUser loadUserDetailForMgr(String username) throws UsernameNotFoundException {
        // TODO：通过用户名查询用户,需要根据你的数据库设计来修改代码
        // 1 通过用户名查找用户对象
        User user = new User();
        /// 创建User对象，设置用户名
        user.setUsername(username);
        /// 查询该用户是否存在,只取第一个用户
        user = userService.getOne(new QueryWrapper<>(user));
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        /// 设置密码加密方式
        /// user.setPassword("{bcrypt}" + user.getPassword());
        // TODO：通过用户编号查询角色,需要根据你的数据库设计来修改代码
        // 2 通过用户ID获取角色列表
        List<Role> roles = roleService.listRoleByUserId(Integer.parseInt(user.getId()));
        /// 将对象换成code
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .toList();
        /// 角色为空的时候给一个默认角色,避免后续权限判断全部失败
        if (roleCodes.isEmpty()){
            roleCodes = Collections.singletonList("ROLE_USER");
        }
        ///  加载医务人员信息，只需要一份医务人员的档案
        AdmPractitioner admPractitioner = practitionerMapper.selectList(
                new QueryWrapper<AdmPractitioner>()
                        .eq("user_id", user.getId())
                        .eq("is_deleted", 0)
        )
                .stream()
                .findFirst()
                .orElse(null);

        /// 如果不等于空则把信息装载到User中
        if(admPractitioner != null){
            user.setPractitionerId(admPractitioner.getId());
            user.setPractitionerName(admPractitioner.getName());
            user.setTitleCode(admPractitioner.getDrProfttlCode());
        }

        /// 加载组织单元
        List<WhaleUserOrganizationUnits> units = userOrganizationUnitsMapper.selectList(
                new QueryWrapper<WhaleUserOrganizationUnits>()
                        .eq("user_id", user.getId())
                        .eq("is_deleted", 0));

        LinkedHashMap<String, String> unitIds = units.stream()
                .map(WhaleUserOrganizationUnits::getOrganizationUnitId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toMap(
                        id -> id,
                        id -> id,
                        (id1, id2) -> id1,
                        LinkedHashMap::new
                ));

        /// 将该用户所属组织装载到User对象中
        user.setOrganizationUnits(unitIds);
        return SecurityUser.create(
                user,
                user.getUsername(),
                user.getPassword(),
                roleCodes
        );
    }

    private SecurityUser loadUserDetailForUser(String username) throws UsernameNotFoundException {
        // TODO：用户端查找用户尚未实现
        System.out.println(username);
        throw new UsernameNotFoundException("用户端查找用户尚未实现");
    }
}
