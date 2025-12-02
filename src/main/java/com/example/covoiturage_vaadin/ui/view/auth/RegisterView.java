package com.example.covoiturage_vaadin.ui.view.auth;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.services.AuthenticationService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Inscription - Covoiturage")
@AnonymousAllowed
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthenticationService authService;

    private final TextField studentCodeField = new TextField("Code étudiant");
    private final TextField nameField = new TextField("Nom complet");
    private final EmailField emailField = new EmailField("Email");
    private final PasswordField passwordField = new PasswordField("Mot de passe");
    private final PasswordField confirmPasswordField = new PasswordField("Confirmer le mot de passe");
    private final Button registerButton = new Button("S'inscrire");

    public RegisterView(AuthenticationService authService) {
        this.authService = authService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        // --- STYLE DASHBOARD (Fond gris clair) ---
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // Titre
        H1 title = new H1("Créer un compte");
        title.getStyle().set("font-size", "1.8rem").set("margin-top", "0");
        
        Paragraph info = new Paragraph("Utilisez votre code étudiant pour vous inscrire");
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Formulaire
        FormLayout formLayout = createFormLayout();

        // Bouton
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.getStyle().set("cursor","pointer");
        registerButton.addClickListener(e -> handleRegistration());
        registerButton.addClickShortcut(Key.ENTER);

        // Lien vers login
        RouterLink loginLink = new RouterLink("Déjà un compte ? Se connecter", LoginView.class);
        loginLink.getStyle()
            .set("margin-top", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("color", "var(--lumo-primary-color)");

        // --- CARTE CONTENEUR CLEAN CARD ---
        VerticalLayout container = new VerticalLayout(title, info, formLayout, registerButton, loginLink);
        container.setMaxWidth("500px");
        container.setWidth("90%");
        container.setAlignItems(Alignment.STRETCH);
        container.setPadding(true);
        container.setSpacing(true);
        
        container.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "0 10px 40px rgba(0,0,0,0.06)")
            .set("padding", "var(--lumo-space-xl)");

        add(container);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();

        studentCodeField.setRequiredIndicatorVisible(true);
        studentCodeField.setPlaceholder("Ex: 22405100");
        studentCodeField.setWidthFull();

        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("Ex: Salim Bouskine");
        nameField.setWidthFull();

        emailField.setRequiredIndicatorVisible(true);
        emailField.setPlaceholder("Ex: salim.bouskine@dauphine.eu");
        emailField.setWidthFull();

        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setMinLength(8);
        passwordField.setHelperText("Min. 8 caractères : 1 majuscule, 1 minuscule, 1 chiffre");
        passwordField.setWidthFull();

        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setWidthFull();

        // Reset validation au changement
        studentCodeField.addValueChangeListener(e -> e.getSource().setInvalid(false));
        nameField.addValueChangeListener(e -> e.getSource().setInvalid(false));
        emailField.addValueChangeListener(e -> e.getSource().setInvalid(false));
        passwordField.addValueChangeListener(e -> e.getSource().setInvalid(false));

        formLayout.add(studentCodeField, nameField, emailField, passwordField, confirmPasswordField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        return formLayout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            beforeEnterEvent.forwardTo("");
            return;
        }

        // if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
        //     login.setError(true);
        // }
    }

    private void handleRegistration() {
        if (!validateFields()) {
            return;
        }

        try {
            StudentDTO student = authService.registerStudent(
                studentCodeField.getValue().trim(),
                nameField.getValue().trim(),
                emailField.getValue().trim(),
                passwordField.getValue()
            );

            if (student.isApproved()) {
                Notification.show(
                    "✅ Inscription réussie ! Vous pouvez maintenant vous connecter.",
                    5000,
                    Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show(
                    "⏳ Inscription enregistrée ! En attente de validation admin.",
                    7000,
                    Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            }

            getUI().ifPresent(ui -> ui.navigate(LoginView.class));

        } catch (IllegalArgumentException ex) {
            // ⚠️ SÉCURITÉ : Message générique pour éviter l'énumération de comptes
            // Ne jamais exposer si un email/code/username existe déjà
            Notification.show(
                "❌ Inscription impossible. Vérifiez vos informations ou contactez un administrateur.",
                5000,
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);

            // Log l'erreur réelle côté serveur (visible uniquement dans les logs)
            System.err.println("Erreur d'inscription : " + ex.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (studentCodeField.isEmpty()) { studentCodeField.setInvalid(true); isValid = false; }
        if (nameField.isEmpty()) { nameField.setInvalid(true); isValid = false; }
        if (emailField.isEmpty() || emailField.isInvalid()) { emailField.setInvalid(true); isValid = false; }

        // Validation du mot de passe : longueur ET complexité
        String password = passwordField.getValue();
        if (password.isEmpty() || !isPasswordStrong(password)) {
            passwordField.setInvalid(true);
            passwordField.setErrorMessage("Min. 8 caractères : 1 majuscule, 1 minuscule, 1 chiffre");
            isValid = false;
        }

        if (confirmPasswordField.isEmpty()) { confirmPasswordField.setInvalid(true); isValid = false; }

        if (!isValid) {
            return false;
        }

        // Vérifier que les mots de passe correspondent
        if (!password.equals(confirmPasswordField.getValue())) {
            confirmPasswordField.setInvalid(true);
            Notification.show("Les mots de passe ne correspondent pas", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    /**
     * Vérifie la force du mot de passe.
     * Requis : 8+ caractères, 1 majuscule, 1 minuscule, 1 chiffre
     */
    private boolean isPasswordStrong(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasUppercase && hasLowercase && hasDigit;
    }
}