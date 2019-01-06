package com.lucassimao.fluxodecaixa.service;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class TokenAuthenticationService {
    private static final long EXPIRATION_TIME = 1 * 24 * 60 * 60 * 1000; // 1 dia em mili segundos
    private static final String SECRET = "MySecret";
    private static final String TOKEN_PREFIX = "Bearer";
    private static final String HEADER_STRING = "Authorization";
    private final static Logger logger = LoggerFactory.getLogger(TokenAuthenticationService.class);

    public static void addAuthentication(HttpServletResponse response, String username,
            Collection<? extends GrantedAuthority> authorities) {

        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        JWTCreator.Builder builder = JWT.create()
                                        .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                                        .withSubject(username);
        
        boolean isAdmin = authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ADMIN")); 
        String token = builder.withClaim("isAdmin", isAdmin).sign(algorithm);

        logger.debug("Granting access to user {}", username);
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + token);
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        if (token != null) {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();

            try {
                DecodedJWT jwt = verifier.verify(token);
                String user = jwt.getSubject();

                if (user != null) {
                    Collection<GrantedAuthority> authorities = new LinkedList<>();
                    if (jwt.getClaim("isAdmin").asBoolean())
                        authorities.add(new SimpleGrantedAuthority("ADMIN"));
                    else
                        authorities.add(new SimpleGrantedAuthority("USER"));
                        
                    return new UsernamePasswordAuthenticationToken(user, null, authorities);
                }
            } catch (JWTVerificationException e) {
                logger.error(e.getMessage(), e);
                return null;
            }

        }
        return null;
    }
}