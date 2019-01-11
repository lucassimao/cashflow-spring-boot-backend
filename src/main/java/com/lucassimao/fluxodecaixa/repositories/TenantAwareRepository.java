package com.lucassimao.fluxodecaixa.repositories;

import javax.persistence.EntityManager;

/**
 * Interface created in order to mark a repository that must be 
 * augmented with the {@link com.lucassimao.fluxodecaixa.aspect.BookEntryGroupRepositoryAspect} aspect.
 * The requirement is that the implementing class must provide the current persistence context throgh the method getEntityManager
 */
public interface TenantAwareRepository {

    EntityManager getEntityManager();
}