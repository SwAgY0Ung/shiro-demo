package com.yhy.shiro.shiro_springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yhy.shiro.shiro_springboot.entity.Role;
import com.yhy.shiro.shiro_springboot.service.RoleService;
import com.yhy.shiro.shiro_springboot.dao.RoleMapper;
import org.springframework.stereotype.Service;

/**
* @author yhy
* @description 针对表【role】的数据库操作Service实现
* @createDate 2024-11-13 22:53:04
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService {

}




