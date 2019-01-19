package com.lucassimao.fluxodecaixa.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lucassimao.fluxodecaixa.config.TenantAuthenticationToken;
import com.lucassimao.fluxodecaixa.config.TenantUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class TokenAuthenticationService {
    private static final long EXPIRATION_TIME = 1 * 24 * 60 * 60 * 1000; // 1 dia em mili segundos
    private static final String SECRET = "MySecret";
    private static final String TOKEN_PREFIX = "Bearer";
    private static final String HEADER_STRING = "Authorization";
    private final static Logger logger = LoggerFactory.getLogger(TokenAuthenticationService.class);
    private static final String IS_ADMIN_CLAIM = "isAdmin";
    private static final String TENANT_ID_CLAIM = "tenantId";


    public static void addAuthentication(HttpServletResponse response,Authentication authentication) {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        TenantUserDetails userDetail = (TenantUserDetails) authentication.getPrincipal();
        String username = authentication.getName();

        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        JWTCreator.Builder builder = JWT.create()
                                        .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                                        .withSubject(username);
        
        boolean isAdmin = authorities.stream().anyMatch(authority -> "ADMIN".equals(authority.getAuthority())); 
        String token = builder.withClaim(TENANT_ID_CLAIM, userDetail.getTenantId())
                              .withClaim(IS_ADMIN_CLAIM, isAdmin)
                              .sign(algorithm);

        logger.debug("Granting access to username {}", username);
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + token);
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        if (token != null) {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();

            try {
                DecodedJWT jwt = verifier.verify(token);
                String username = jwt.getSubject();

                if (username != null) {
                    Collection<GrantedAuthority> authorities;
                    if (jwt.getClaim(IS_ADMIN_CLAIM).asBoolean())
                        authorities = List.of(new SimpleGrantedAuthority("ADMIN"));
                    else
                        authorities = List.of(new SimpleGrantedAuthority("USER"));
                        
                    Long tenantId = jwt.getClaim(TENANT_ID_CLAIM).asLong();
                    return new TenantAuthenticationToken( tenantId, username, null, authorities);
                }
            } catch (JWTVerificationException e) {
                logger.error(e.getMessage(), e);
                return null;
            }

        }
        return null;
    }
}