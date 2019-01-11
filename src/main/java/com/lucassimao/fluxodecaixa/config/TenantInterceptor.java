package com.lucassimao.fluxodecaixa.config;

import java.io.Serializable;
import java.util.Arrays;

import com.lucassimao.fluxodecaixa.model.TenantEntity;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Hibernate interceptor used to set tenantId on subclasses of com.lucassimao.fluxodecaixa.model.TenantEntity
 * 
 * @since 10/01/19
 */
public class TenantInterceptor extends EmptyInterceptor{
 
    private static final long serialVersionUID = 1L;

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        return addTenantIdIfObjectIsTenantEntity(entity, state, propertyNames);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        return addTenantIdIfObjectIsTenantEntity(entity, currentState, propertyNames);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        addTenantIdIfObjectIsTenantEntity(entity, state, propertyNames);
    }

    private boolean addTenantIdIfObjectIsTenantEntity(Object entity, Object[] state, String[] propertyName){

        Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);
        logger.debug("entity {} state {} propertyName {}", entity,Arrays.toString(state),Arrays.toString(propertyName));

        if(entity instanceof TenantEntity){                                    
            for (int index = 0; index < propertyName.length; index++) {        
                if(propertyName[index].equals(TenantEntity.TENANT_FILTER_ARGUMENT_NAME)){     
                    TenantAuthenticationToken tenantAuthenticationToken = (TenantAuthenticationToken) SecurityContextHolder
                            .getContext().getAuthentication();
                    state[index] =  tenantAuthenticationToken.getTenantId();
                    return true;   
                }
            }
            throw new ClassCastException();                                   
        }
        return false;
    }

}