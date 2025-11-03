package com.ropa.tienda.config;

import com.ropa.tienda.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // Importación clave
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Importación clave
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Inyecta el filtro JWT
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // 1. Define el encriptador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Define las reglas de CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Añado explícitamente tu puerto de IDE (63342) para asegurar CORS
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",
                "http://127.0.0.1:8080",
                "http://localhost:63342"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 3. Define la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // Habilita CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // *** CLAVE 1: CONFIGURACIÓN SIN ESTADO (STATELESS) ***
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configura las autorizaciones de peticiones
        .authorizeHttpRequests(auth -> auth
            // Permite acceso a la ruta de login y registro
            .requestMatchers("/api/auth/**").permitAll()
            // Permite acceso a productos sin token (solo lectura)
            .requestMatchers("/api/productos", "/api/productos/filtrar", "/api/sucursales").permitAll()
            // Permitir recursos estáticos y páginas HTML
            .requestMatchers(
                "/",
                "/index.html",
                "/login.html",
                "/admin.html",
                "/static/**",
                "/css/**",
                "/js/**",
                "/images/**",
                "/favicon.ico"
            ).permitAll()
            // Rutas específicas para administradores
            .requestMatchers("/api/users/**").hasRole("ADMIN")
            .requestMatchers("/admin.html").hasRole("ADMIN")
            // Rutas para usuarios autenticados (tanto ADMIN como USER)
            .requestMatchers("/api/carrito/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/api/ordenes/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/api/sucursales/**").hasAnyRole("USER", "ADMIN")
            // Cualquier otra solicitud requiere autenticación
            .anyRequest().authenticated()
        )

                // *** CLAVE 2: AÑADE EL FILTRO JWT A LA CADENA ***
                // Ejecuta tu filtro antes del filtro estándar de autenticación de usuario y contraseña
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 4. Exponer el AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
