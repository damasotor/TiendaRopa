package com.ropa.tienda.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm; // <-- Necesaria para v0.9.1
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import javax.crypto.spec.SecretKeySpec; // <-- Necesaria para v0.9.1 (Generar clave)
import javax.xml.bind.DatatypeConverter; // <-- Necesaria para v0.9.1 (Convertir clave)
import java.util.Date;

@Component
public class JwtTokenProvider {

    // CAMBIO 1: Clave secreta fija (string base64) y su implementación
    // DEBE ser de al menos 256 bits (32 caracteres base64).
    private final String secret = "EsteEsUnSecretoMuyLargoParaTiendaRopaMongoDB";
    private final long jwtExpirationInMs = 604800000L; // 7 días

    // Generar la clave de firma (compatible con v0.9.1)
    private Key getSigningKey() {
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secret);
        return new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    // Genera el token a partir de la información de autenticación
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // CAMBIO 2: Uso de signWith(algoritmo, clave)
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("rol", userDetails.getAuthorities().iterator().next().getAuthority())
                .signWith(SignatureAlgorithm.HS512, getSigningKey()) // <-- SINTAXIS V0.9.1
                .compact();
    }

    // Método para obtener el email (subject) del token
    public String getUsernameFromJWT(String token) {
        // CAMBIO 3: Uso de setSigningKey(clave)
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey()) // <-- SINTAXIS V0.9.1
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Método para validar la firma y expiración del token
    public boolean validateToken(String authToken) {
        try {
            // CAMBIO 4: Uso de setSigningKey(clave)
            Jwts.parser()
                    .setSigningKey(getSigningKey()) // <-- SINTAXIS V0.9.1
                    .parseClaimsJws(authToken);
            return true;
        } catch (Exception ex) {
            System.err.println("Error al validar el JWT: " + ex.getMessage());
            return false;
        }
    }
}