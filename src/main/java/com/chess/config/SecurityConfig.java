package com.chess.config;

import com.chess.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security.
 * 
 * - Deshabilita CSRF (no necesario para APIs REST stateless)
 * - Configura sesión como STATELESS (JWT es stateless)
 * - Permite acceso público a /api/auth/** (login/register)
 * - Protege /api/game/** con autenticación JWT
 * - Agrega el filtro JWT antes del filtro de Spring
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF (API REST stateless)
            .csrf(csrf -> csrf.disable())

            // Configurar CORS
            .cors(Customizer.withDefaults())

            // Sesión stateless (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (login, register)
                .requestMatchers("/api/auth/**").permitAll()
                // Todos los demás endpoints requieren autenticación
                .requestMatchers("/api/game/**").authenticated()
                // Cualquier otro request es permitido (e.g., health check)
                .anyRequest().permitAll()
            )

            // Agregar filtro JWT
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt para hashear contraseñas.
     * Nunca almacenar contraseñas en texto plano.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
