package com.example.covoiturage_vaadin.ui.view.auth;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n; // <-- Pour la traduction
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("login")
@PageTitle("Connexion - Covoiturage")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        setSizeFull(); 
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // 1. Configuration de l'action (Spring Security)
        login.setAction("login"); 
        
        // 2. Traduction en Français
        login.setI18n(createFrenchI18n()); // <-- On applique la traduction ici

        login.setForgotPasswordButtonVisible(false);

        // Lien vers l'inscription
        RouterLink registerLink = new RouterLink("Pas encore de compte ? S'inscrire", RegisterView.class);
        registerLink.getStyle()
            .set("margin-top", "var(--lumo-space-m)")
            .set("text-align", "center");

        add(new H1("Plateforme Covoiturage"), login, registerLink);
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // 3. Redirection si déjà connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Si l'utilisateur existe, est authentifié, et n'est PAS un utilisateur anonyme
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            // On le redirige vers la page d'accueil ("")
            beforeEnterEvent.forwardTo("");
            return;
        }

        // 4. Gestion des erreurs de connexion
        if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }

    // Méthode utilitaire pour créer la traduction française
    private LoginI18n createFrenchI18n() {
        LoginI18n i18n = LoginI18n.createDefault();

        // Textes du formulaire
        LoginI18n.Form form = i18n.getForm();
        form.setTitle("Connexion");
        form.setUsername("Nom d'utilisateur");
        form.setPassword("Mot de passe");
        form.setSubmit("Se connecter");
        form.setForgotPassword("Mot de passe oublié");
        i18n.setForm(form);

        // Messages d'erreur
        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("Échec de la connexion");
        errorMessage.setMessage("Vérifiez que votre email et votre mot de passe sont corrects.");
        i18n.setErrorMessage(errorMessage);

        return i18n;
    }
}