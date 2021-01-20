package com.atguigu.gulimall.seckill.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: LDeng
 * @Date: 2021-01-09 15:05
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser=new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //order/order/status/{orderSn}这个请求需要直接放行， 关乎ware模块调用order 模块
        String uri = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean match = matcher.match("/kill/**", uri);
        if(match){
            MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if(attribute!=null){
                //登录了,将用户信息放入ThreadLocal并放行
                loginUser.set(attribute);
                return true;
            }else{
                //没登录， 去登录
                request.getSession().setAttribute("msg","请先进行登录");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        }

        return true;
    }
}
