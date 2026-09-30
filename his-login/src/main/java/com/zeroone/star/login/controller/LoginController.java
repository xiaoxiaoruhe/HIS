package com.zeroone.star.login.controller;

import cn.hutool.core.bean.BeanUtil;
import com.zeroone.star.login.service.IMenuService;
import com.zeroone.star.project.components.user.UserDTO;
import com.zeroone.star.project.components.user.UserHolder;
import com.zeroone.star.project.login.LoginApis;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.enums.ResultStatus;
import com.zeroone.star.project.vo.login.LoginVO;
import com.zeroone.star.project.vo.login.MenuTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 描述：登录接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@RestController
@Tag(name = "login", description = "登录接口")
public class LoginController implements LoginApis {
    @Resource
    UserHolder userHolder;
    @Resource
    IMenuService menuService;

    @Operation(summary = "获取当前用户")
    @GetMapping("current-user")
    @Override
    public JsonVO<LoginVO> getCurrUser() {
        UserDTO currentUser;
        try {
            currentUser = userHolder.getCurrentUser();
        } catch (Exception e) {
            return JsonVO.create(null, ResultStatus.FAIL.getCode(), e.getMessage());
        }
        if (currentUser == null) {
            return JsonVO.fail(null);
        } else {
            // TODO:这里需要根据业务需求，重新实现
            LoginVO vo = new LoginVO();
            BeanUtil.copyProperties(currentUser, vo);
            return JsonVO.success(vo);
        }
    }

    @Operation(summary = "获取菜单")
    @GetMapping("get-menus")
    @Override
    public JsonVO<List<MenuTreeVO>> getMenus() {
        // TODO:未实现根据实际数据库设计业务逻辑，下面逻辑属于示例逻辑
        // 1 获取当前用户
        UserDTO currentUser;
        try {
            currentUser = userHolder.getCurrentUser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 2 获取当前用户拥有的菜单
        List<MenuTreeVO> menus = menuService.listMenuByRoleName(currentUser.getRoles());
        return JsonVO.success(menus);
    }
}
