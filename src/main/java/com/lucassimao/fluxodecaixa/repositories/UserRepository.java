package com.lucassimao.fluxodecaixa.repositories;


import com.lucassimao.fluxodecaixa.model.User;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

// @Repository
@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface  UserRepository extends CrudRepository<User,Long> {
    
    User findByEmail(String email);

}