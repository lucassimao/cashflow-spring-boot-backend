package com.lucassimao.cashflow.repositories;


import com.lucassimao.cashflow.model.User;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface  UserRepository extends CrudRepository<User,Long> ,CustomUserRepository{
    
    User findByEmail(String email);

}