package com.post_it.blog.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        if (session == null) return true;

        Long expireAt = (Long) session.getAttribute("emailAuthExpireAt");
        if (expireAt != null && expireAt < System.currentTimeMillis()) {
            session.removeAttribute("emailAuthStatus");
            session.removeAttribute("emailAuthExpireAt");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("SESSION_EXPIRE");
            return false;
        }
        return true;
    }
}
