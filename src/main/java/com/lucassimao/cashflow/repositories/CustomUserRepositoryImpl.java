package com.lucassimao.cashflow.repositories;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.lucassimao.cashflow.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;


@Transactional
public class  CustomUserRepositoryImpl implements CustomUserRepository  {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public <S extends User> S save(S entity) {
        Logger logger = LoggerFactory.getLogger(CustomUserRepositoryImpl.class);
        logger.debug("Registrando novo usuário {}",  entity);
        
        entity.setRole(User.ROLE_USER);
        entity.setEncryptedPassword(passwordEncoder.encode(entity.getEncryptedPassword()));
        entityManager.persist(entity);
        return entity;
    }
    

}