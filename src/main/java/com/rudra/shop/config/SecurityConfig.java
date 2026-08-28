package com.rudra.shop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import com.rudra.shop.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService userDetailsService;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // 1. Disable CSRF for easier testing (or enable it later for production)
                                .csrf(csrf -> csrf.disable())

                                // 2. Session Management
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                                                .invalidSessionUrl("/login"))

                                // 3. Configure Permissions
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/products", "/product/**", "/register", "/login",
                                                                "/css/**", "/images/**",
                                                                "/favicon.ico", "/api/auth/check",
                                                                "/error",
                                                                "/about", "/contact", "/terms", "/privacy", "/refund",
                                                                "/shipping",
                                                                "/wishlist", "/wishlist/toggle",
                                                                "/cart", "/checkout", "/checkout/**", "/api/cart/**", "/api/webhooks/**")
                                                .permitAll()
                                                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                                                .anyRequest().authenticated())

                                // 4. Configure Form Login
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true)
                                                .permitAll())

                                // 5. THE MERGED LOGOUT (Database + Cookie Cleanup)
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("CLOTH_SHOP_SESSION")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
                return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
                AuthenticationManagerBuilder authenticationManagerBuilder = http
                                .getSharedObject(AuthenticationManagerBuilder.class);
                authenticationManagerBuilder
                                .userDetailsService(userDetailsService)
                                .passwordEncoder(passwordEncoder());
                return authenticationManagerBuilder.build();
        }
}