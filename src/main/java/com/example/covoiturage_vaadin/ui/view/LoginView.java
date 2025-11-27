package com.example.covoiturage_vaadin.ui.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed; // <-- NOUVEL IMPORT

/**
 * Vue de connexion standard Vaadin/Spring Security.
 * Utilise le composant LoginForm pour déléguer l'authentification à Spring Security.
 * L'accès public est géré par setLoginView() dans VaadinSecurityConfiguration.
 */
@Route("login")
@PageTitle("Connexion - Covoiturage")
@AnonymousAllowed // <-- AJOUTEZ CECI POUR DÉCLARER LA VUE PUBLIQUE
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        // Configuration de la mise en page
        addClassName("login-view");
        setSizeFull(); 
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Configuration du formulaire de connexion
        // C'est l'étape CRUCIALE : on pointe l'action du formulaire vers l'URL par défaut 
        // de Spring Security pour le POST de login.
        login.setAction("login"); 
        
        // Cacher le bouton 'Mot de passe oublié' (optionnel)
        login.setForgotPasswordButtonVisible(false);

        // Ajout des composants
        add(
            new H1("Covoiturage Étudiant"), 
            login
        );
    }
    
    /**
     * Méthode appelée par Vaadin avant d'entrer dans la vue.
     * Utilisée ici pour afficher le message d'erreur si la connexion échoue.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // Vérifie si Spring Security a redirigé avec un paramètre d'erreur (login échoué)
        if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true); // Active l'affichage du message d'erreur dans le LoginForm
        }
        
        // Si la connexion réussit (Spring Security reçoit le POST), Spring redirige 
        // automatiquement vers la route principale ("/") ou la page initialement demandée.
    }
}