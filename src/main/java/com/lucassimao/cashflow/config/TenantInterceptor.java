package com.lucassimao.cashflow.config;

import java.io.Serializable;
import java.util.Arrays;

import com.lucassimao.cashflow.model.TenantEntity;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Hibernate interceptor used to set tenantId on subclasses of
 * com.lucassimao.cashflow.model.TenantEntity
 * 
 * @since 10/01/19
 */
public class TenantInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        boolean wasModified = false;
        if (entity instanceof TenantEntity){
            wasModified = addTenantId(entity, state, propertyNames);
            validateTenantState(state, propertyNames);
        }
        return wasModified;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
            String[] propertyNames, Type[] types) {
        validateTenantState(currentState, propertyNames);
        return false;
    }

    private void validateTenantState(Object[] state, String[] propertyNames) {
        SecurityContext context = SecurityContextHolder.getContext();
        TenantAuthenticationToken tenantAuthenticationToken = (TenantAuthenticationToken) context.getAuthentication();
        Long tenantId = tenantAuthenticationToken.getTenantId();

        for (int index = 0; index < propertyNames.length; index++) {
            if (propertyNames[index].equals(TenantEntity.TENANT_ID_PROPERTY_NAME)) {
                Long entityTenantId = (Long) state[index];

                if (!entityTenantId.equals(tenantId))
                    throw new RuntimeException("invalid tenant");
                else
                    break;
            } else if (state[index] instanceof TenantEntity) {
                TenantEntity association = (TenantEntity) state[index];
                logger.debug("field tenantId = {} | request tenantId = {}", association.getTenantId(), tenantId);

                if (!tenantId.equals(association.getTenantId()))
                    throw new IllegalArgumentException("Invalid value for field " + propertyNames[index]);
            }
        }
    }

    private boolean addTenantId(Object entity, Object[] state, String[] propertyName) {
        logger.debug("entity {} state {} propertyName {}", entity, Arrays.toString(state),
                Arrays.toString(propertyName));

        if (entity instanceof TenantEntity) {
            for (int index = 0; index < propertyName.length; index++) {
                if (propertyName[index].equals(TenantEntity.TENANT_ID_PROPERTY_NAME)) {
                    TenantAuthenticationToken tenantAuthenticationToken = (TenantAuthenticationToken) SecurityContextHolder
                            .getContext().getAuthentication();
                    state[index] = tenantAuthenticationToken.getTenantId();
                    return true;
                }
            }
            throw new ClassCastException();
        }
        return false;
    }

}