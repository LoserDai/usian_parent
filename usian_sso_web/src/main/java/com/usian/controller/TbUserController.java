package com.usian.controller;

import com.usian.feign.SSOServiceFeign;
import com.usian.pojo.TbUser;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Loser
 * @date 2021年12月01日 11:48
 */
@RestController
@RequestMapping("/frontend/sso")
public class TbUserController {

    @Autowired
    private SSOServiceFeign ssoServiceFeign;

    @RequestMapping("/checkUserInfo/{checkValue}/{checkFlag}")
    public Result checkUserInfo(@PathVariable String checkValue , @PathVariable Integer checkFlag){
        Boolean checkUserInfo = ssoServiceFeign.checkUserInfo(checkValue, checkFlag);
        if (checkUserInfo){
            return Result.ok();
        }
        return Result.error("校验失败");
    }

    @RequestMapping("/userRegister")
    public Result userRegister(TbUser tbUser){
        int count = ssoServiceFeign.userRegister(tbUser);
        if (count == 1){
            return Result.ok();
        }
        return Result.error("注册失败");
    }

    @RequestMapping("/userLogin")
    public Result userLogin(String username , String password){
        Map map = ssoServiceFeign.userLogin(username, password);
        if (map.size() > 0){
            return Result.ok(map);
        }
        return Result.error("登录失败");
    }

    @RequestMapping("/getUserByToken/{token}")
    public Result getUserByToken(@PathVariable String token){
        TbUser tbUser = ssoServiceFeign.getUserByToken(token);
        if (tbUser != null){
            return Result.ok();
        }
        return Result.error("查询失败");
    }

    @RequestMapping("/logOut")
    public Result logOut(String token){
        Boolean logOut = ssoServiceFeign.logOut(token);
        if (logOut){
            return Result.ok();
        }
        return Result.error("退出登录失败");
    }
}
