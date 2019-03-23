package com.lucassimao.fluxodecaixa.repositories;

import javax.persistence.EntityManager;

import org.springframework.data.rest.core.annotation.RestResource;


/**
 * Interface created in order to mark a repository that must be 
 * augmented with the {@link com.lucassimao.fluxodecaixa.aspect.TenantFilterActivatorAspect} aspect.
 * The requirement is that the implementing class must provide the current persistence context throgh the method getEntityManager
 */
public interface TenantAwareRepository {

    @RestResource(exported=false)
    EntityManager getEntityManager();


}