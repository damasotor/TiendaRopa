package com.ropa.tienda.security;

import com.ropa.tienda.service.MongoUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Para que Spring lo gestione y lo podamos inyectar
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private MongoUserDetailsService userDetailsService;

    /**
     * Este método contiene la lógica principal del filtro.
     * Se ejecuta en cada petición HTTP, excepto en las rutas públicas.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Obtener el JWT de la solicitud (encabezado Authorization)
            String jwt = getJwtFromRequest(request);

            // 2. Validar el token y obtener el email (subject)
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userEmail = tokenProvider.getUsernameFromJWT(jwt);

                // 3. Cargar el usuario de la base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // 4. Crear el objeto de autenticación
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Añadir detalles de la solicitud (opcional pero recomendado)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Establecer la autenticación en el contexto de seguridad
                // Esto le dice a Spring Security: "Este usuario ya está logueado y es válido"
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("No se pudo establecer la autenticación del usuario en el contexto de seguridad", ex);
        }

        // Continuar la cadena de filtros (la petición va al controlador)
        filterChain.doFilter(request, response);
    }

    /**
     * Método auxiliar para extraer el token JWT del encabezado "Authorization".
     * Espera el formato: Bearer <token>
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Comprueba si el encabezado Authorization existe y comienza con "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Devuelve la cadena después de "Bearer " (el token puro)
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}