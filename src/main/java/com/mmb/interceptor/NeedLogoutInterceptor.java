package com.mmb.interceptor;

import com.mmb.dto.Req;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class NeedLogoutInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Req req = (Req) request.getAttribute("req");

        if (req != null && req.getLoginedMember() != null) {
            req.jsPrintReplace("You cannot access this page while logged in.", "/");
            return false;
        }

        return true;
    }
}
