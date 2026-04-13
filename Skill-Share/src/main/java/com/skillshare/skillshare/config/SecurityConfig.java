package com.skillshare.skillshare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final com.skillshare.skillshare.security.CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(com.skillshare.skillshare.security.CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF purely for API (swagger testing) endpoints, keep enabled for
                                // everything else (web forms)
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/ws/**"))

                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers(
                                                                "/",
                                                                "/about",
                                                                "/register",
                                                                "/login",
                                                                "/api/auth/**",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**")
                                                .permitAll()
                                                // Admin endpoints
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                // Protected endpoints (everything else including /profile)
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .usernameParameter("email") // Login via email
                                                .passwordParameter("password")
                                                .successHandler(successHandler)
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());

                return http.build();
        }
}
