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
                        .requestMatchers("/", "/registration", "/manga", "/activate/*", "/reset-password", "/about", "/faq", "/contact-us", "/images/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/manga/add").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/manga/edit/{mangaId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/manga/delete/{mangaId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/genre/add").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/genre/edit/{genreId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/genre/delete/{genreId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/author/add").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/author/edit/{authorId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/author/delete/{authorId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/manga/{mangaId}/chapter/add").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/manga/{mangaId}/chapter/edit/{chapterId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/manga/{mangaId}/chapter/delete/{chapterId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/news/add").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/news/edit/{newsId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/news/delete/{newsId}").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/admin-panel").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .failureUrl("/login?error=true")
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