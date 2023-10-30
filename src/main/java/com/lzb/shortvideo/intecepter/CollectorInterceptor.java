package com.lzb.shortvideo.intecepter;

import cn.hutool.extra.servlet.ServletUtil;
import com.lzb.shortvideo.constant.UserConstant;
import com.lzb.shortvideo.model.dto.log.RequestInfo;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.utils.RequestHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 信息收集的拦截器
 */
@Order(1)
@Slf4j
@Component
public class CollectorInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequestInfo info = new RequestInfo();
        // 未登录不获取
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null || currentUser.getId() == null) {
            return true;
        }
        info.setUid(currentUser.getId());
        info.setIp(ServletUtil.getClientIP(request));
        RequestHolder.set(info);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestHolder.remove();
    }

}
