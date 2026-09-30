package com.zeroone.star.project.login;

import com.zeroone.star.project.dto.login.LoginDTO;
import com.zeroone.star.project.dto.login.Oauth2TokenDTO;
import com.zeroone.star.project.dto.login.RefreshTokenDTO;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.vo.login.LoginVO;
import com.zeroone.star.project.vo.login.MenuTreeVO;

import java.util.List;

/**
 * <p>
 * 描述：用户登录接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
public interface LoginApis {
    /**
     * 授权登录接口
     * 提示：如果使用的授权码模式并且也不需要支持其他授权模式，则不需要实现这个接口
     * @param loginDTO 登录数据
     * @return 授权登录结果
     */
    default JsonVO<Oauth2TokenDTO> authLogin(LoginDTO loginDTO) {
        return null;
    }

    /**
     * 刷新Token认证
     * 提示：如果使用的授权码模式并且也不需要支持其他授权模式，则不需要实现这个接口
     * @param refreshTokenDTO 刷新凭证数据对象
     * @return 刷新Token结果
     */
    default JsonVO<Oauth2TokenDTO> refreshToken(RefreshTokenDTO refreshTokenDTO) {
        return null;
    }

    /**
     * 退出登录
     * 提示：如果使用的授权码模式并且也不需要支持其他授权模式，则不需要实现这个接口
     * @return 退出结果
     */
    default JsonVO<String> logout(){
        return null;
    }

    /**
     * 获取当前用户信息，登录成功后才能调用，需要通过凭证获取信息的
     * @return 返回当前用户信息
     */
    JsonVO<LoginVO> getCurrUser();

    /**
     * 获取菜单数据，登录成功后才能调用，需要通过凭证获取信息的
     * @return 菜单数据
     */
    JsonVO<List<MenuTreeVO>> getMenus();
}
