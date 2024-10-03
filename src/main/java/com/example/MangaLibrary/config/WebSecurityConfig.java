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
                        .requestMatchers("/", "/registration", "/login", "/reset-password", "/about", "/faq", "/faqdlc", "/contact-us", "/images/**", "/css/**", "/js/**", "/filter-manga", "/genres-filter", "/authors-filter").permitAll()
                        .requestMatchers("/manga/add", "/manga/edit/{mangaId}", "/manga/delete/{mangaId}",
                                "/genre/add", "/genre/edit/{genreId}", "/genre/delete/{genreId}",
                                "/author/add", "/author/edit/{authorId}", "/author/delete/{authorId}",
                                "/manga/{mangaId}/chapter/add", "/manga/{mangaId}/chapter/edit/{chapterId}", "/manga/{mangaId}/chapter/delete/{chapterId}",
                                "/news/add", "/news/edit/{newsId}", "/news/delete/{newsId}",
                                "/admin-panel", "/admin-dashboard").hasAnyAuthority("ADMIN", "MODERATOR")
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
                .usersByUsernameQuery("select user_name, user_password, enabled from user where user_name=?")
                .authoritiesByUsernameQuery("select user_name, user_role from user where user_name=?");
    }
}