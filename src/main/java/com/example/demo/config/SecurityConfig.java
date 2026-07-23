package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.example.demo.service.CustomOAuth2UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SessionRegistry sessionRegistry() {
                return new SessionRegistryImpl();
        }

        @Bean
        public static HttpSessionEventPublisher httpSessionEventPublisher() {
                return new HttpSessionEventPublisher();
        }

        @Bean
        public AuthenticationSuccessHandler roleBasedSuccessHandler() {
                return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
                        boolean isAdmin = authentication.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                        response.sendRedirect(isAdmin ? "/admin/dashboard" : "/home");
                };
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                // Expose /error endpoint to stop Spring Security from redirecting to
                                                // login on
                                                // failure
                                                .requestMatchers("/", "/home", "/search", "/tags", "/tags/**",
                                                                "/auth/**", "/assets/**", "/error", "/blog", "/blog/**",
                                                                "/forgot-password", "/reset-password", "/system-rules", "/oauth2/**",
                                                                "/login/oauth2/**", "/question", "/question/**", "/question-detail",
                                                                "/trending" , "/accept-terms")
                                                .permitAll()
                                                .requestMatchers("/admin/**", "/api/admin/**", "/dashboard")
                                                .hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/auth/login")
                                                .usernameParameter("email")
                                                .passwordParameter("password")
                                                .successHandler(roleBasedSuccessHandler())
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/auth/login")
                                                .successHandler(roleBasedSuccessHandler())
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService)))
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/auth/login?logout=true")
                                                .invalidateHttpSession(true))
                                .sessionManagement(session -> {
                                        session.maximumSessions(-1).sessionRegistry(sessionRegistry());
                                })
                                .csrf(csrf -> csrf.disable());

                return http.build();
        }
}