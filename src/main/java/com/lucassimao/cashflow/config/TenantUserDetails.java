package com.lucassimao.cashflow.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class TenantUserDetails extends User {

    private static final long serialVersionUID = -6173270432928160552L;
    private Long tenantId;

    public TenantUserDetails(Long tenantId, UserDetails userDetails) {
        super(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(),
                userDetails.isAccountNonExpired(), userDetails.isCredentialsNonExpired(),
                userDetails.isAccountNonLocked(), userDetails.getAuthorities());
        this.tenantId = tenantId;
    }

    public Long getTenantId() {
        return tenantId;
    }

}