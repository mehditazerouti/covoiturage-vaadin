package com.example.covoiturage_vaadin.infrastructure.config;

import com.example.covoiturage_vaadin.ui.view.auth.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration principale de Spring Security pour Vaadin.
 * Étend VaadinWebSecurity pour gérer automatiquement les ressources Vaadin.
 */
@Configuration
@EnableWebSecurity
public class VaadinSecurityConfiguration extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 1. Autoriser /login sans authentification AVANT toute autre config
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/login").permitAll()
        );

        // 2. Configuration Vaadin par défaut (gère CSRF, ressources statiques, etc.)
        super.configure(http);

        // 3. Définir LoginView comme page de login
        setLoginView(http, LoginView.class);
    }

    /**
     * Bean pour l'encodage des mots de passe avec BCrypt.
     * Force 10 par défaut (équilibre sécurité/performance).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
