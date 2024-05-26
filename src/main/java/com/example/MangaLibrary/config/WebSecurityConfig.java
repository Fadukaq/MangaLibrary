package com.example.MangaLibrary.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Autowired
    private DataSource dataSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/registration","/manga","/activate/*", "/images/**","/css/**").permitAll()
                        .requestMatchers("/manga/add").hasAuthority("ADMIN")
                        .requestMatchers("/manga/edit/{id}").hasAuthority("ADMIN")
                        .requestMatchers("/manga/delete/{id}").hasAuthority("ADMIN")
                        .requestMatchers("/genre/add").hasAuthority("ADMIN")
                        .requestMatchers("/genre/edit/{id}").hasAuthority("ADMIN")
                        .requestMatchers("/genre/delete/{id}").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/manga", true)
                        .permitAll()
                )
                .logout((logout) -> logout.permitAll());

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .usersByUsernameQuery("select user_name, user_password, enabled from User where user_name=?")
                .authoritiesByUsernameQuery("select user_name, user_role from user where user_name=?");
    }
}