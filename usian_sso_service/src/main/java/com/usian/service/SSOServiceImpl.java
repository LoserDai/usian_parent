package com.usian.service;

import com.usian.config.RedisClient;
import com.usian.mapper.TbUserMapper;
import com.usian.pojo.TbUser;
import com.usian.pojo.TbUserExample;
import com.usian.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Loser
 * @date 2021年12月01日 14:09
 */
@Service
@Transactional
public class SSOServiceImpl implements SSOService {
    @Autowired
    private TbUserMapper tbUserMapper;
    @Autowired
    private RedisClient redisClient;
    @Value("${USER_INFO}")
    private String USER_INFO;
    @Value("${SESSION_EXPIRE}")
    private Long SESSION_EXPIRE;

    /**
     * 注册信息校验
     * @param checkValue
     * @param checkFlag
     * @return
     */
    @Override
    public Boolean checkUserInfo(String checkValue, int checkFlag) {
        //判断该用户的手机号以及用户名是否存在于数据库中
        TbUserExample tbUserExample = new TbUserExample();
        TbUserExample.Criteria criteria = tbUserExample.createCriteria();
            if (checkFlag == 1){
                criteria.andUsernameEqualTo(checkValue);
            }else if (checkFlag == 2){
                criteria.andPhoneEqualTo(checkValue);
            }
        List<TbUser> tbUsers = tbUserMapper.selectByExample(tbUserExample);
            if (tbUsers == null || tbUsers.size() == 0){
                return true;
            }
        return false;
    }

    /**
     * 用户信息注册
     * @param tbUser
     * @return
     */
    @Override
    public Integer userRegister(TbUser tbUser) {
        //密码加密
        String pwd = MD5Utils.digest(tbUser.getPassword());
        tbUser.setPassword(pwd);
        //补齐数据
        tbUser.setCreated(new Date());
        tbUser.setUpdated(new Date());
        return tbUserMapper.insertSelective(tbUser);
    }

    /**
     * 用户登录并将信息缓存到redis
     * @param username
     * @param password
     * @return
     */
    @Override
    public Map userLogin(String username, String password) {
        Map<String, Object> map = new HashMap<>();
        //1、根据username、password查询user
        TbUserExample tbUserExample = new TbUserExample();
        TbUserExample.Criteria criteria = tbUserExample.createCriteria();
        criteria.andUsernameEqualTo(username);
        criteria.andPasswordEqualTo(MD5Utils.digest(password));
        List<TbUser> tbUserList = tbUserMapper.selectByExample(tbUserExample);
        if(tbUserList == null || tbUserList.size() == 0){
            return map;
        }
        //2、把user存到redis
        TbUser tbUser = tbUserList.get(0);
        tbUser.setPassword(null);
        String token = UUID.randomUUID().toString();
        redisClient.set(USER_INFO+":"+token,tbUser);
        redisClient.expire(USER_INFO+":"+token,SESSION_EXPIRE);

        //3、返回Map
        map.put("token",token);
        map.put("userid",tbUser.getId());
        map.put("username",tbUser.getUsername());
        return map;
    }

    /**
     * 显示出redis中的用户名在首页(已登录)
     * @param token
     * @return
     */
    @Override
    public TbUser getUserByToken(String token) {
        return (TbUser) redisClient.get(USER_INFO + ":" + token);
    }

    /**
     * 退出登录(删除redis中的信息)
     * @param token
     * @return
     */
    @Override
    public Boolean logOut(String token) {
        return redisClient.del(USER_INFO + ":" + token);
    }
}
