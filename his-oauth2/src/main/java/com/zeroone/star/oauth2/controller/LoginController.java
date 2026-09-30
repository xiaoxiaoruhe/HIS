package com.zeroone.star.oauth2.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <p>
 * 描述：自定义登录页控制器
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Controller
public class LoginController {
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, Model model) {
        // 从Session中获取被拦截前的原始请求
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            // 提取 client_id 参数
            String[] clientIds = savedRequest.getParameterValues("client_id");
            if (clientIds != null && clientIds.length > 0) {
                String clientId = clientIds[0];
                model.addAttribute("client_id", clientId);
                // 存入Session供UserDetailsService后面使用
                request.getSession().setAttribute("client_id", clientId);
            }
        }
        return "login";
    }
}
