package com.yhy.shiro.shiro_springboot.realm;

import cn.hutool.core.util.StrUtil;
import com.yhy.shiro.shiro_springboot.entity.*;
import com.yhy.shiro.shiro_springboot.jwt.JwtToken;
import com.yhy.shiro.shiro_springboot.jwt.JwtUtil;
import com.yhy.shiro.shiro_springboot.service.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


public class JwtRealm extends AuthorizingRealm {

    @Resource
    private UserService userService;
    @Resource
    private UserRoleRefService userRoleRefService;
    @Resource
    private RoleService roleService;
    @Resource
    private RoleFuncRefService roleFuncRefService;
    @Resource
    private FuncService funcService;

    /**
     * 限定这个 Realm 只处理我们自定义的 JwtToken
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

        // 1.获取subject对象
        Subject subject = SecurityUtils.getSubject();

        // 2.获取当前用户账号（因为当前login方法实现是用账号作为principal）
        String account = subject.getPrincipal().toString();


        // 3.通过账号获取角色和权限
        if (account != null) {
            // 通过账号获取用户
            User user = userService.lambdaQuery().eq(User::getAccount, account).one();
            // 获取用户的角色，
            List<UserRoleRef> userRoleRefs = userRoleRefService.lambdaQuery()
                    .eq(UserRoleRef::getUserId, user.getId())
                    .list();
            if (userRoleRefs != null && !userRoleRefs.isEmpty()) {
                List<Integer> roleIds = userRoleRefs.stream().map(UserRoleRef::getRoleId).collect(Collectors.toList());
                List<Role> roles = roleService.lambdaQuery()
                        .in(Role::getId, roleIds)
                        .list();
                List<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toList());
                simpleAuthorizationInfo.addRoles(roleNames);

                // 获取角色对应的权限
                List<RoleFuncRef> roleFuncRefs = roleFuncRefService.lambdaQuery().in(RoleFuncRef::getRoleId, roleIds).list();
                if (roleFuncRefs != null && !roleFuncRefs.isEmpty()) {
                    List<Integer> funcIds = roleFuncRefs.stream().map(RoleFuncRef::getFuncId).collect(Collectors.toList());
                    List<Func> funcs = funcService.lambdaQuery().in(Func::getId, funcIds).list();
                    List<String> funcNames = funcs.stream().map(Func::getName).collect(Collectors.toList());
                    simpleAuthorizationInfo.addStringPermissions(funcNames);
                }
            }
        }
        return simpleAuthorizationInfo;
    }

    /**
     * 认证
     * 这里的认证和登陆认证不同，这里的认证用来解析jwtToken中的账号，然后和数据库中的账号进行对比
     *
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 1.获取当前用户信息（账号）
        JwtToken jwtToken = (JwtToken) authenticationToken;
        if (jwtToken.getPrincipal() == null) {
            throw new RuntimeException("JWT token参数异常！");
        }
        // 从 JwtToken 中获取token(jwt)
        String token = jwtToken.getPrincipal().toString();
        // 解析jwt，
        // 就是获取jwt的subject，因为生成jwt的时候payload代表的参数名就是subject
        // payload可以是很多非敏感信息的组合，但是这里为了简单明了，使用的是userId
        String userId = JwtUtil.getSubject(token);

        // 空指校验
        if (StrUtil.isBlank(userId)) {
            throw new AuthenticationException();
        }

        // 验签（众所周知，jwt由三部分组成：header、payload、signature），所以在此处需要验签
        if (!JwtUtil.verify(token, userId)) {
            throw new AuthenticationException();
        }

        // 检验账号是否存在
        User user = userService.lambdaQuery().eq(User::getId, userId).one();

        if (user == null) {
            throw new RuntimeException("该账号不存在！");
        }

        return new SimpleAuthenticationInfo(user.getAccount(), token, "jwtRealm");
    }
}
