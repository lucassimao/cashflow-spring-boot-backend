package com.lucassimao.fluxodecaixa.service;

import com.lucassimao.fluxodecaixa.model.User;
import com.lucassimao.fluxodecaixa.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

  @Autowired
  private UserRepository usuarioRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User usu = usuarioRepository.findByEmail(username);
    if (usu == null) {
      throw new UsernameNotFoundException(username);
    }

    return org.springframework.security.core.userdetails.User
          .builder().username(username).roles(usu.getRole()).password(usu.getEncryptedPassword()).build();
  }

}