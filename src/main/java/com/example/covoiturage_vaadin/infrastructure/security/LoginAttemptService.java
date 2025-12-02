package com.example.covoiturage_vaadin.infrastructure.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de gestion des tentatives de connexion échouées (Rate Limiting).
 *
 * Politique :
 * - Après 5 tentatives échouées, le compte est bloqué pendant 15 minutes
 * - Les tentatives sont réinitialisées après une connexion réussie
 * - Les données sont stockées en mémoire (non persistées)
 *
 * Sécurité :
 * - Protège contre les attaques par force brute
 * - Protège contre les attaques par dictionnaire
 * - Rate limiting basé sur le username
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    // Map<username, AttemptInfo>
    private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Enregistre une tentative de connexion échouée pour un username.
     *
     * @param username Le nom d'utilisateur
     */
    public void loginFailed(String username) {
        AttemptInfo info = attemptsCache.getOrDefault(username, new AttemptInfo());
        info.incrementAttempts();
        attemptsCache.put(username, info);

        System.out.println("⚠️ Tentative de connexion échouée pour '" + username + "' (" + info.getAttempts() + "/" + MAX_ATTEMPTS + ")");
    }

    /**
     * Réinitialise les tentatives échouées après une connexion réussie.
     *
     * @param username Le nom d'utilisateur
     */
    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
        System.out.println("✅ Connexion réussie pour '" + username + "' - Compteur réinitialisé");
    }

    /**
     * Vérifie si un username est bloqué (trop de tentatives échouées).
     *
     * @param username Le nom d'utilisateur à vérifier
     * @return true si le compte est bloqué, false sinon
     */
    public boolean isBlocked(String username) {
        AttemptInfo info = attemptsCache.get(username);

        if (info == null) {
            return false; // Aucune tentative échouée
        }

        // Vérifier si le délai de blocage est expiré
        if (info.isExpired(LOCKOUT_DURATION_MINUTES)) {
            attemptsCache.remove(username); // Nettoyer les données expirées
            return false;
        }

        // Bloquer si >= MAX_ATTEMPTS
        boolean blocked = info.getAttempts() >= MAX_ATTEMPTS;

        if (blocked) {
            long minutesRemaining = info.getRemainingLockoutMinutes(LOCKOUT_DURATION_MINUTES);
            System.out.println("🔒 Compte '" + username + "' bloqué (encore " + minutesRemaining + " minutes)");
        }

        return blocked;
    }

    /**
     * Obtient le nombre de tentatives restantes avant blocage.
     *
     * @param username Le nom d'utilisateur
     * @return Nombre de tentatives restantes (entre 0 et MAX_ATTEMPTS)
     */
    public int getRemainingAttempts(String username) {
        AttemptInfo info = attemptsCache.get(username);
        if (info == null || info.isExpired(LOCKOUT_DURATION_MINUTES)) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - info.getAttempts());
    }

    /**
     * Classe interne pour stocker les informations d'une tentative.
     */
    private static class AttemptInfo {
        private int attempts = 0;
        private LocalDateTime lastAttemptTime = LocalDateTime.now();

        public void incrementAttempts() {
            this.attempts++;
            this.lastAttemptTime = LocalDateTime.now();
        }

        public int getAttempts() {
            return attempts;
        }

        public boolean isExpired(int lockoutDurationMinutes) {
            return LocalDateTime.now().isAfter(lastAttemptTime.plusMinutes(lockoutDurationMinutes));
        }

        public long getRemainingLockoutMinutes(int lockoutDurationMinutes) {
            LocalDateTime unlockTime = lastAttemptTime.plusMinutes(lockoutDurationMinutes);
            return java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
        }
    }
}
