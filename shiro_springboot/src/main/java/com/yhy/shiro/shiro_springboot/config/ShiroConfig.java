package com.yhy.shiro.shiro_springboot.config;

import com.yhy.shiro.shiro_springboot.jwt.JwtFilter;

import com.yhy.shiro.shiro_springboot.realm.JwtRealm;
import com.yhy.shiro.shiro_springboot.realm.ShiroRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.*;


@Configuration
public class ShiroConfig {

    /**
     * JwtRealm 配置，需实现 Realm 接口
     */
    @Bean
    JwtRealm jwtRealm() {
        JwtRealm jwtRealm = new JwtRealm();
        return jwtRealm;
    }

    /**
     * ShiroRealm 配置，需实现 Realm 接口
     */
    @Bean
    ShiroRealm shiroRealm() { //这里其实是模拟的数据库的类，但是也继承了AuthorizingRealm
        ShiroRealm shiroRealm = new ShiroRealm();

        // 2.创建加密对象
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        // 2.1.设置加密方式（md5）
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        // 2.2.设置加密次数（3）
        hashedCredentialsMatcher.setHashIterations(3);

        shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher);
        return shiroRealm;
    }


    /**
     * 向容器中注入默认的securityManager
     * @return
     */
    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager() {
        // 1.创建defaultSecurityManager对象
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        // 设置认证策略:只要有一个（或多个）的Realm验证成功，那么认证将视为成功
//        ModularRealmAuthenticator modularRealmAuthenticator = new ModularRealmAuthenticator();
//        modularRealmAuthenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
//        securityManager().setAuthenticator(modularRealmAuthenticator);

        // 4.设置多个realm，一个用来login登陆认证，一个用来授权（获取到请求头中的Authorization，然后解析并实现登陆授权）
        List<Realm> realms = new ArrayList<>(16);
        realms.add(jwtRealm());
        realms.add(shiroRealm());
        securityManager.setRealms(realms); // 配置多个realm

        // 关闭shiro自带的session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);

        // 5.返回
        return securityManager;
    }

    @Bean
    public JwtFilter jwtFilter() {
        //过滤器如果加了@Compoent就没必要用这个方法了
        return new JwtFilter();
    }

    @Bean
    public FilterRegistrationBean<Filter> registration(JwtFilter filter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<Filter>(filter);
        registration.setEnabled(false);
        return registration;
    }


    /**
     * 配置访问资源需要的权限
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager defaultWebSecurityManager) {

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //给工厂bean设置web安全管理器
        shiroFilterFactoryBean.setSecurityManager(defaultWebSecurityManager);

        shiroFilterFactoryBean.setLoginUrl("/index/login");
        shiroFilterFactoryBean.setSuccessUrl("/authorized");
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorized");

        // 添加 jwt 专用过滤器，拦截除 /login 和 /logout 外的请求
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("jwtFilter", jwtFilter());
        shiroFilterFactoryBean.setFilters(filterMap);

        //配置系统受限资源以及公共资源
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        filterChainDefinitionMap.put("/index/userLogin", "anon"); // 可匿名访问
        filterChainDefinitionMap.put("/logout", "logout"); // 退出登录
        filterChainDefinitionMap.put("/**", "jwtFilter,authc"); // 需登录才能访问
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    @Bean
    public Authorizer authorizer(){
        //这里是个坑，如果没有这个bean，启动会报错，所以得加上
        return new ModularRealmAuthorizer();
    }

}
