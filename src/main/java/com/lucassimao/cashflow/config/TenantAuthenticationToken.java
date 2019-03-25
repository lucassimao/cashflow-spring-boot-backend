package com.lucassimao.cashflow.config;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class TenantAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;
    private Long tenantId;

    public TenantAuthenticationToken(Long tenantId, Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {    
        super(principal,credentials,authorities);
        this.tenantId = tenantId;
    }

    public Long getTenantId() {
        return tenantId;
    }
}