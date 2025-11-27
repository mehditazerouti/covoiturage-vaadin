package com.example.covoiturage_vaadin.ui.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

public class LogoutButton extends HorizontalLayout {

    public LogoutButton() {
        // Création du bouton avec une icône
        Button logoutBtn = new Button("Déconnexion", new Icon(VaadinIcon.SIGN_OUT));
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR); // Couleur rouge pour indiquer la sortie

        // Logique de déconnexion
        logoutBtn.addClickListener(e -> {
            // 1. Capturer la référence UI AVANT le logout (car la session sera invalidée)
            UI ui = UI.getCurrent();

            // 2. Invalider la session Spring Security côté serveur
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    null,
                    null
            );

            // 3. Rediriger vers la page de login avec l'UI capturée
            ui.getPage().setLocation("/login");
        });

        add(logoutBtn);

        // --- Style CSS pour le positionnement fixe en bas à droite ---
        this.getStyle().set("position", "fixed");
        this.getStyle().set("bottom", "20px");
        this.getStyle().set("right", "20px");
        
        // Z-index élevé pour s'assurer qu'il flotte au-dessus des autres éléments (comme les grilles)
        this.getStyle().set("z-index", "1000"); 
        
        // Supprimer le padding/spacing par défaut du layout pour être propre
        this.setPadding(false);
        this.setSpacing(false);
    }
}