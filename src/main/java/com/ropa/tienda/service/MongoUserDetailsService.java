package com.ropa.tienda.service;

import com.ropa.tienda.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

// Este servicio es el core de la autenticación
@Service
public class MongoUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca el usuario en MongoDB usando el método findByEmail que definiste
        com.ropa.tienda.model.Usuario usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Mapea el objeto Usuario de MongoDB a un objeto UserDetails de Spring Security.
        // Esto es necesario para que Spring Security maneje la autenticación.
        return org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPasswordHash()) // Contraseña cifrada de MongoDB
                // Aquí se mapea el rol (ADMIN, USER) para la autorización
                .authorities(new SimpleGrantedAuthority(usuario.getRol().getNombre())) // 👈 rol como objeto
                .build();
    }
}