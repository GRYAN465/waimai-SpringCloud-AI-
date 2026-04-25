package com.cloudtakeout.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenUtil {
    private static final String SECRET = "cloud_takeout_demo_secret_please_change";
    private static final long EXPIRE_MILLIS = 2 * 60 * 60 * 1000L;

    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRE_MILLIS))
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes(StandardCharsets.UTF_8))
                .compact();
    }
}
