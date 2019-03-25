package com.lucassimao.cashflow.service;

import com.lucassimao.cashflow.config.TenantUserDetails;
import com.lucassimao.cashflow.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
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
      com.lucassimao.cashflow.model.User usu = usuarioRepository.findByEmail(username);
      if (usu == null) {
        throw new UsernameNotFoundException(username);
      }

      UserDetails userDetails = User.builder().username(username)
                                    .roles(usu.getRole())
                                    .password(usu.getEncryptedPassword())
                                    .build();

      return new TenantUserDetails(usu.getId(), userDetails);
  }

}