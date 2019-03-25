package com.lucassimao.cashflow.repositories;


import com.lucassimao.cashflow.model.User;


public interface  CustomUserRepository  {
    
    <S extends User> S save(S entity);

}