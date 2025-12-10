package com.example.covoiturage_vaadin.ui.view.auth;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Connexion - Covoiturage")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final TextField usernameField = new TextField("Code étudiant");
    private final PasswordField passwordField = new PasswordField("Mot de passe");
    private final Button loginButton = new Button("Se connecter");

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // --- STYLE DASHBOARD (Fond gris clair) ---
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // --- EN-TÊTE DE LA CARTE ---
        Icon logoIcon = VaadinIcon.CAR.create();
        logoIcon.setSize("40px");
        logoIcon.setColor("var(--lumo-primary-color)");

        H1 title = new H1("Covoit' Étudiant");
        title.getStyle()
                .set("margin", "var(--lumo-space-xs) 0 var(--lumo-space-s) 0")
                .set("font-size", "1.8rem")
                .set("color", "var(--lumo-header-text-color)");

        Span subtitle = new Span("La mobilité connectée pour Dauphine");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout header = new VerticalLayout(logoIcon, title, subtitle);
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(false);
        header.setPadding(false);

        // --- CHAMPS PERSONNALISÉS ---
        usernameField.setWidthFull();
        passwordField.setWidthFull();

        // Gestion de la validation "silencieuse" (enlève le rouge quand on tape)
        usernameField.addValueChangeListener(e -> usernameField.setInvalid(false));
        passwordField.addValueChangeListener(e -> passwordField.setInvalid(false));

        // Bouton de connexion
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.getStyle().set("cursor", "pointer");
        loginButton.addClickListener(e -> login());
        // Permettre de valider avec la touche Entrée
        loginButton.addClickShortcut(Key.ENTER);

        // --- CARTE CONTENEUR ---
        VerticalLayout card = new VerticalLayout();
        card.add(header, usernameField, passwordField, loginButton);

        // Lien inscription
        RouterLink registerLink = new RouterLink("Pas encore de compte ? Créer un compte", RegisterView.class);
        registerLink.getStyle()
                .set("margin-top", "var(--lumo-space-s)")
                .set("font-weight", "500")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-primary-color)");
        
        card.add(registerLink);
        card.setAlignSelf(Alignment.CENTER, registerLink);

        // --- STYLE CLEAN CARD ---
        card.setMaxWidth("450px");
        card.setWidth("90%");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.STRETCH); // Important pour que les champs prennent toute la largeur
        
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 10px 40px rgba(0,0,0,0.06)")
                .set("padding", "var(--lumo-space-xl)");

        add(card);
    }

    private void login() {
        // 1. Validation visuelle simple (rouge si vide)
        boolean isValid = true;
        if (usernameField.isEmpty()) {
            usernameField.setInvalid(true);
            isValid = false;
        }
        if (passwordField.isEmpty()) {
            passwordField.setInvalid(true);
            isValid = false;
        }

        if (!isValid) return;

        // 2. Soumission manuelle à Spring Security via JavaScript
        // Cela permet de garder la mécanique standard (POST /login) sans utiliser le composant LoginForm
        UI.getCurrent().getPage().executeJs(
            "const form = document.createElement('form');" +
            "form.method = 'POST';" +
            "form.action = 'login';" +
            "const userField = document.createElement('input');" +
            "userField.type = 'hidden';" +
            "userField.name = 'username';" +
            "userField.value = $0;" +
            "form.appendChild(userField);" +
            "const passField = document.createElement('input');" +
            "passField.type = 'hidden';" +
            "passField.name = 'password';" +
            "passField.value = $1;" +
            "form.appendChild(passField);" +
            "document.body.appendChild(form);" +
            "form.submit();",
            usernameField.getValue(),
            passwordField.getValue()
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Gestion de l'erreur renvoyée par Spring Security (paramètre ?error)
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            // On affiche une notification ou on met les champs en rouge
            usernameField.setInvalid(true);
            passwordField.setInvalid(true);
            Notification.show("Identifiants incorrects", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            event.forwardTo("");
            return;
        }
    }
}