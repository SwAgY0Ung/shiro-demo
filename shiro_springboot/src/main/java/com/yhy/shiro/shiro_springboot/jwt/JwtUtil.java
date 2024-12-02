package com.yhy.shiro.shiro_springboot.jwt;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final int expireMinutes = 24 * 30; // 小时
    private static final String secret = "c7aebdd9ea8f6d81bbf668fcec1365d";

    /**
     * 签名并颁发jwt
     *
     * @param subject
     * @return
     */
    public static String sign(String subject) {
        try {
            Date date = new Date(System.currentTimeMillis() + (expireMinutes * 60 * 60 * 1000L));
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withSubject(subject)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验签
     *
     * @param token
     * @param subject
     * @return
     */
    public static boolean verify(String token, String subject) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withSubject(subject)
                    .build();
            verifier.verify(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * 从token解析出subject
     *
     * @param token
     * @return
     */
    public static String getSubject(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 获取Token过期时间 分钟
     *
     * @param token
     * @return
     */
    public static Long getExpirationTime(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            Date expiresAt = jwt.getExpiresAt();
            return DateUtil.between(new Date(), expiresAt, DateUnit.HOUR);
        } catch (JWTDecodeException e) {
            return null;
        }
    }
}
