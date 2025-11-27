package com.example.covoiturage_vaadin.application.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityContextService {

    /**
     * R\u00e9cup\u00e8re le nom d'utilisateur de l'utilisateur actuellement authentifi\u00e9
     * @return Optional contenant le username, ou empty si non authentifi\u00e9
     */
    public Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // Ignorer "anonymousUser" (cas de non-authentification)
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        return Optional.ofNullable(authentication.getName());
    }

    /**
     * V\u00e9rifie si l'utilisateur courant poss\u00e8de un r\u00f4le sp\u00e9cifique
     * @param role Le r\u00f4le \u00e0 v\u00e9rifier (ex: "ADMIN" ou "ROLE_ADMIN")
     * @return true si l'utilisateur a ce r\u00f4le
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Normaliser le nom du r\u00f4le (ajouter ROLE_ si absent)
        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(normalizedRole));
    }

    /**
     * V\u00e9rifie si un utilisateur est actuellement authentifi\u00e9
     * @return true si authentifi\u00e9
     */
    public boolean isUserAuthenticated() {
        return getCurrentUsername().isPresent();
    }
}
