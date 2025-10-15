package com.danialrekhman.orderservicenorcurne.config;

import com.danialrekhman.orderservicenorcurne.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Все POST-запросы разрешены только ADMIN или аутентифицированным пользователям
                        .requestMatchers(HttpMethod.POST, "/api/orders/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/order-items/**").authenticated()

                        // Все PUT и DELETE требуют аутентификации
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**", "/api/order-items/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**", "/api/order-items/**").authenticated()

                        // Доступ к своим заказам — через аутентификацию
                        .requestMatchers(HttpMethod.GET, "/api/orders/my").authenticated()

                        // Доступ к общим данным
                        .requestMatchers(HttpMethod.GET, "/api/orders/**", "/api/order-items/**").authenticated()

                        .requestMatchers("/actuator/**").permitAll()

                        // Остальные запросы — только для авторизованных пользователей
                        .anyRequest().authenticated()
                )
//                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

