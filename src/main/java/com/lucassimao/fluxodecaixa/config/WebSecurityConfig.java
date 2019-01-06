package com.lucassimao.fluxodecaixa.config;

import com.lucassimao.fluxodecaixa.model.User;
import com.lucassimao.fluxodecaixa.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@Profile("development")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Autowired
    private UserRepository usuarioRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .authorizeRequests()
            .anyRequest().permitAll()
            // .antMatchers("/", "/home","/css/**").permitAll()
            // .anyRequest().authenticated()
            .and()
        .formLogin()
            .loginPage("/login")
            .permitAll()
            .and()
        .logout()
            .permitAll()        
        .and()
            .httpBasic().disable()
            .csrf().disable();

    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("admin")
                        .password(encoder().encode("admin"))
                        .roles("ADMIN");                    
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(11);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void incializaDados() {
        User usu = usuarioRepository.findByEmail("teste@fluxodecaixa.com");
        if (usu == null) {
            usu = new User();
            usu.setName("teste");
            usu.setRole("USER");
            usu.setEncryptedPassword(encoder().encode("123"));
            usu.setEmail("teste@fluxodecaixa.com");
            usuarioRepository.save(usu);
        }
    }    

}