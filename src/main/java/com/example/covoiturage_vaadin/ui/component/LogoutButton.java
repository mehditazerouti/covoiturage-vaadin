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
        setWidthFull();
        setPadding(false);
        setSpacing(false);

        // Création d'un bouton large et moderne
        Button logoutBtn = new Button("Déconnexion", new Icon(VaadinIcon.SIGN_OUT));
        logoutBtn.setWidthFull();
        
        // Style subtil : texte rouge, fond transparent (Ghost), icône à droite
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        // Customisation CSS pour un look "Danger doux"
        logoutBtn.getStyle()
            .set("color", "var(--lumo-error-text-color)")
            .set("cursor", "pointer")
            .set("font-weight", "500")
            .set("justify-content", "flex-start"); // Aligner texte à gauche comme les menus
        
        // Ajout d'un fond léger au survol (géré par Vaadin par défaut, mais on peut renforcer)
        logoutBtn.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        logoutBtn.addClickListener(e -> {
            UI ui = UI.getCurrent();
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    null,
                    null
            );
            ui.getPage().setLocation("/login");
        });

        add(logoutBtn);
    }
}