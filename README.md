shiro的最佳实践就是：
1. shiro+auth0(jwt)
2. 创建两个自定义realm：
   1. 一个shiroRealm，重写doGetAuthenticationInfo方法。专门用来提供给系统的入口index/userLogin接口
   2. 一个jwtRealm，配合拦截器。专门用来解析请求头中的Authorization，获取到了token之后调用login方法，会调用jwtRealm中的doGetAuthenticationInfo方法，重写该方法只需要进行解析jwt就可以了
