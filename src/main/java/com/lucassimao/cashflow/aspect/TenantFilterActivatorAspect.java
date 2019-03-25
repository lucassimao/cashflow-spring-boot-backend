package com.lucassimao.cashflow.aspect;

import com.lucassimao.cashflow.config.TenantAuthenticationToken;
import com.lucassimao.cashflow.model.TenantEntity;
import com.lucassimao.cashflow.repositories.TenantAwareRepository;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantFilterActivatorAspect {

    @Before("execution(* com.lucassimao.cashflow.repositories.TenantAwareRepository+.find*(..))") 
    public void before(JoinPoint joinPoint) {
        Logger logger = LoggerFactory.getLogger(TenantFilterActivatorAspect.class);

        Object target = joinPoint.getTarget();
        logger.debug("Enabling filter for {} @ {}", target,joinPoint);

        TenantAwareRepository tenantAwareRepository = (TenantAwareRepository) joinPoint.getTarget();
        TenantAuthenticationToken tenantAuthenticationToken = (TenantAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        tenantAwareRepository.getEntityManager()
            .unwrap(Session.class) 
            .enableFilter(TenantEntity.TENANT_FILTER_NAME) 
            .setParameter(TenantEntity.TENANT_FILTER_ARGUMENT_NAME, tenantAuthenticationToken.getTenantId()); 
    }

}