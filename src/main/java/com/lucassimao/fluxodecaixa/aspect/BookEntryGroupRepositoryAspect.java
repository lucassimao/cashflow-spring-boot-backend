package com.lucassimao.fluxodecaixa.aspect;

import com.lucassimao.fluxodecaixa.config.TenantAuthenticationToken;
import com.lucassimao.fluxodecaixa.model.TenantEntity;
import com.lucassimao.fluxodecaixa.repositories.TenantAwareRepository;

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
public class BookEntryGroupRepositoryAspect {

    @Before("execution(* com.lucassimao.fluxodecaixa.repositories.TenantAwareRepository+.*(..))") 
    public void before(JoinPoint joinPoint) {
        TenantAuthenticationToken tenantAuthenticationToken = (TenantAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Logger logger = LoggerFactory.getLogger(BookEntryGroupRepositoryAspect.class);

        Object target = joinPoint.getTarget();
        logger.debug("Enabling filter for {} ", target);

        TenantAwareRepository tenantAwareRepository = (TenantAwareRepository) joinPoint.getTarget();
        tenantAwareRepository.getEntityManager()
            .unwrap(Session.class) 
            .enableFilter(TenantEntity.TENANT_FILTER_NAME) 
            .setParameter(TenantEntity.TENANT_FILTER_ARGUMENT_NAME, tenantAuthenticationToken.getTenantId()); 
    }

}