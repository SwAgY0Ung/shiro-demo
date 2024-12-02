package com.yhy.shiro.shiro_springboot.controller;

import com.yhy.shiro.shiro_springboot.entity.User;
import com.yhy.shiro.shiro_springboot.jwt.JwtUtil;
import com.yhy.shiro.shiro_springboot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/index")
@Slf4j
public class LoginController {

    @Resource
    private UserService userService;

    /**
     * 登陆
     * @param account
     * @param password
     * @return 返回account（假设是token）
     */
    @GetMapping("/userLogin")
    public String userLogin(String account, String password) {
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(account, password);
        Subject subject = SecurityUtils.getSubject();
        subject.login(usernamePasswordToken);

        User user = userService.lambdaQuery().eq(User::getAccount, account).one();
        // 登陆成功，签发jwt，返回给前端
        // 调用工具类，签名并生成jwt
        String jwt = JwtUtil.sign(user.getId().toString());
        log.info("登陆成功");
        return jwt;
    }

    /**
     * 判断用户是否拥有admin角色
     * @return 是否拥有
     */
    @RequiresRoles("admin")
    @GetMapping("/hasAdminRoles")
    public String hasRoles() {
        return "拥有admin角色";
    }

    /**
     * 判断用户是否拥有/index权限
     * @return 是否拥有
     */
    @RequiresPermissions("/index")
    @GetMapping("/hasIndexPermission")
    public String hasPermission() {
        return "拥有/index权限";
    }
}
