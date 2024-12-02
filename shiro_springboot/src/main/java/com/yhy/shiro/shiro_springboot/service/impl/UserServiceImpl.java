package com.yhy.shiro.shiro_springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yhy.shiro.shiro_springboot.entity.User;
import com.yhy.shiro.shiro_springboot.service.UserService;
import com.yhy.shiro.shiro_springboot.dao.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author yhy
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-11-13 22:53:04
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




