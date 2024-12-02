package com.yhy.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;

public class Main {
    public static void main(String[] args) {
        // 1.获取工厂，获取实例
        IniSecurityManagerFactory factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager instance = factory.getInstance();
        // 将读取了ini文件的实例设置到SecurityUtils(安全管理器的工具类)中
        SecurityUtils.setSecurityManager(instance);

        // 2.获取subject对象，进行login登陆
        Subject subject = SecurityUtils.getSubject();
        subject.login(new UsernamePasswordToken("zhangsan", "z3"));

        System.out.println("登陆成功");
    }
}