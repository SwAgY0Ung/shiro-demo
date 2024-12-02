package com.yhy.shiro.shiro_springboot.realm;

import com.yhy.shiro.shiro_springboot.entity.*;
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
import org.apache.shiro.util.ByteSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 该类专用来做认证，返回用户信息和权限
 */
public class ShiroRealm extends AuthorizingRealm {

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
                    simpleAuthorizationInfo.addRoles(funcNames);
                }
            }
        }
        return simpleAuthorizationInfo;
    }

    /**
     * 认证
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 1.获取当前用户信息（账号）
        Object principal = authenticationToken.getPrincipal();

        // 2.根据账号查询数据库
        User user = userService.lambdaQuery()
                .eq(User::getAccount, principal)
                .one();

        // 3.返回用户信息（md5加密密码）
        if (user != null) {

            //四个参数：账号、密码、盐、realm名称
            return new SimpleAuthenticationInfo(
                    user.getAccount(),
                    user.getPwd(),
                    ByteSource.Util.bytes(user.getSalt()),
                    "shiroRealm"
            );
        }
        return null;
    }
}
