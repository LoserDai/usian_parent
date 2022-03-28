package com.usian.interceptor;

import com.usian.feign.SSOServiceFeign;
import com.usian.pojo.TbUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Loser
 * @date 2021年12月03日 14:27
 */
@Component
public class UserLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private SSOServiceFeign ssoServiceFeign;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //通过token获取到用户
        String token = request.getParameter("token");
        if (StringUtils.isBlank(token)){
            return false;
        }
        TbUser user = ssoServiceFeign.getUserByToken(token);
        if (user == null){
            return false;
        }
        return true;
    }
}
