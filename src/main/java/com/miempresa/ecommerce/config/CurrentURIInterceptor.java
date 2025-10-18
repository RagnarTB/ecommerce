package com.miempresa.ecommerce.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CurrentURIInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) throws Exception {

        if (modelAndView != null && modelAndView.getModelMap() != null) {
            modelAndView.getModelMap().addAttribute("currentURI", request.getRequestURI());
        }
    }
}
