package com.lucassimao.cashflow.config;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Logger logger = LoggerFactory.getLogger(UserFilter.class);

        if (authentication != null && authentication instanceof TenantAuthenticationToken) {
            TenantAuthenticationToken tenantAuthenticationToken = (TenantAuthenticationToken) authentication;
            HttpServletResponse res = (HttpServletResponse) response;
            HttpServletRequest req = (HttpServletRequest) request;
            logger.info("Request URI {} ", req.getRequestURI());

            Pattern pattern = Pattern.compile("/users/([\\d]+)$");
            Matcher matcher = pattern.matcher(req.getRequestURI());
            if(matcher.matches()){
                long id = Long.valueOf(matcher.group(1));
                if (!tenantAuthenticationToken.getTenantId().equals(id)){
                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

}