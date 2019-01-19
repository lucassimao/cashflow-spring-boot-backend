package com.lucassimao.fluxodecaixa.repositories;


import com.lucassimao.fluxodecaixa.model.User;


public interface  CustomUserRepository  {
    
    <S extends User> S save(S entity);

}