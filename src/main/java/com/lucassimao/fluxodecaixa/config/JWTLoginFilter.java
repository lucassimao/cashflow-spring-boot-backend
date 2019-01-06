package com.lucassimao.fluxodecaixa.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucassimao.fluxodecaixa.service.TokenAuthenticationService;

import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    protected JWTLoginFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse arg1)
            throws AuthenticationException, IOException, ServletException {

        if (request.getMethod().equalsIgnoreCase("POST")) {
            TypeReference<Map<String, String>> typeRef;
            typeRef = new TypeReference<Map<String, String>>() {
            };

            try {
                Map<String, String> credentials = new ObjectMapper().readValue(request.getInputStream(), typeRef);
                return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(
                        credentials.get("username"), credentials.get("password")));
            } catch (IOException e) {
                throw new BadCredentialsException("Invalid credentials", e);
            }

        } else
            throw new BadCredentialsException("Invalid credentials");
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
    
        LoggerFactory.getLogger(JWTLoginFilter.class).debug("Login ok {} ", authResult);
        TokenAuthenticationService.addAuthentication(response, authResult.getName(),authResult.getAuthorities());
    }


}