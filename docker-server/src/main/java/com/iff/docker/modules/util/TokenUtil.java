/*******************************************************************************
 * Copyright (c) 2020-01-10 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

/**
 * TokenUtil
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-01-10
 */
public class TokenUtil {
    public static String getUserId(String token) {
        String userId = JWT.decode(token).getAudience().get(0);
        return userId;
    }

    public static String toToken(String userName, String password) {
        Date start = new Date();
        //5天有效时间
        Date end = new Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000);
        String token = JWT.create()
                .withAudience(userName)
                .withIssuedAt(start)
                .withExpiresAt(end)
                .sign(Algorithm.HMAC256(password));
        return token;
    }

    public static String renewToken(String token, String userName, String password) {
        long time = JWT.decode(token).getExpiresAt().getTime();
        if (System.currentTimeMillis() - time < 120 * 60 * 1000) {//如果过期时间小于120分钟，则重新生成一个 Token
            return toToken(userName, password);
        }
        return null;
    }

    public static boolean verifyToken(String token, String password) {
        try {
            // 验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(password)).build();
            jwtVerifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
