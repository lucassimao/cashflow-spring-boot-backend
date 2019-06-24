package com.lucassimao.cashflow.repositories;

import javax.persistence.EntityManager;
import com.lucassimao.cashflow.model.User;
import org.springframework.data.rest.core.annotation.RestResource;


public interface  CustomUserRepository  {
    
    <S extends User> S save(S entity);

    @RestResource(exported=false)
    EntityManager getEntityManager();

}