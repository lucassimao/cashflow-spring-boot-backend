package com.lucassimao.cashflow.config;

import java.util.List;

import javax.annotation.PostConstruct;

import com.lucassimao.cashflow.model.User;
import com.lucassimao.cashflow.repositories.UserRepository;
import com.lucassimao.cashflow.service.MyUserDetailsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Autowired
    private UserRepository usuarioRepository;

    @Autowired
    private Environment environment;

    private boolean isDevelopmentEnv = true;


    @Autowired
    private MyUserDetailsService userDetailsService;

    @PostConstruct
    public void init(){
        this.isDevelopmentEnv = this.environment.acceptsProfiles(Profiles.of("development"));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.addFilterBefore(new JWTLoginFilter("/login", authenticationManager()),UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JWTAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        http
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .antMatchers(HttpMethod.POST, "/users").permitAll()
                .antMatchers(HttpMethod.GET, "/users").hasAuthority(User.ROLE_ADMIN)
                .anyRequest().authenticated()
                .and()
                    .httpBasic().disable()
                .cors()
                .and()
                    .requestCache().disable()
                .logout().disable()
                .csrf().disable();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());

        // auth.inMemoryAuthentication()
        // .withUser("admin")
        //     .password(encoder().encode("admin"))
        //     .roles("ADMIN");             
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());
        return authProvider;
    }    

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(11);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void incializaDados() {
        User usu = usuarioRepository.findByEmail("admin@mycashflow.com");
        if (isDevelopmentEnv && usu == null) {
            Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
            usu = new User();
            usu.setName("admin da silva");
            usu.setRole(User.ROLE_ADMIN);
            usu.setEncryptedPassword(encoder().encode("123"));
            usu.setEmail("admin@mycashflow.com");
            ((CrudRepository<User,Long>)usuarioRepository).save(usu);
            logger.info("development Admin user created with id: {}", usu.getId());
        }
    }    


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        if (isDevelopmentEnv){
            configuration.setAllowedOrigins(List.of("*"));
        } else 
            configuration.setAllowedOrigins(List.of("https://mycashflow.com"));

        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("POST","GET","DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }    

    
    @Bean
    public FilterRegistrationBean<UserFilter> userFilter(){
        FilterRegistrationBean<UserFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UserFilter());
        registrationBean.addUrlPatterns("/users/*");
        registrationBean.setOrder(1);
        return registrationBean;    
    }    

}