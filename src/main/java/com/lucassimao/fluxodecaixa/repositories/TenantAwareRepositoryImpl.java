package com.lucassimao.fluxodecaixa.repositories;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Default and only implementation of {@link TenantAwareRepository} just to
 * obtain by injection the persistence context that will be augmented by
 * {@link com.lucassimao.fluxodecaixa.aspect.BookEntryGroupRepositoryAspect}
 * aspect
 */
public class TenantAwareRepositoryImpl implements TenantAwareRepository{

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

}