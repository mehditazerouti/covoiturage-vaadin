package com.example.covoiturage_vaadin.infrastructure.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Listener des événements d'authentification Spring Security.
 *
 * Écoute les succès et échecs de connexion pour mettre à jour le LoginAttemptService.
 * Permet d'implémenter le rate limiting en comptant les tentatives échouées.
 */
@Component
public class AuthenticationEventListener {

    private final LoginAttemptService loginAttemptService;

    public AuthenticationEventListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Appelé automatiquement après une connexion réussie.
     * Réinitialise le compteur de tentatives échouées.
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        loginAttemptService.loginSucceeded(username);
    }

    /**
     * Appelé automatiquement après une tentative de connexion échouée.
     * Incrémente le compteur de tentatives échouées.
     */
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        loginAttemptService.loginFailed(username);
    }
}
