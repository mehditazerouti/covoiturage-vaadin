package com.example.covoiturage_vaadin.ui.view.auth;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.services.AuthenticationService;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Vue d'inscription publique pour les étudiants.
 * Si le code est whitelisté → compte activé immédiatement
 * Sinon → compte créé en attente de validation admin
 */
@Route("register")
@PageTitle("Inscription - Covoiturage")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

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

        // Titre
        H1 title = new H1("Créer un compte");
        Paragraph info = new Paragraph("Utilisez votre code étudiant pour vous inscrire");
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Formulaire
        FormLayout formLayout = createFormLayout();

        // Bouton
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(e -> handleRegistration());

        // Lien vers login
        RouterLink loginLink = new RouterLink("Déjà un compte ? Se connecter", LoginView.class);
        loginLink.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Container
        VerticalLayout container = new VerticalLayout(title, info, formLayout, registerButton, loginLink);
        container.setMaxWidth("500px");
        container.setAlignItems(Alignment.STRETCH);
        container.setPadding(true);
        container.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("box-shadow", "var(--lumo-box-shadow-m)");

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
        passwordField.setMinLength(6);
        passwordField.setWidthFull();

        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setWidthFull();

        formLayout.add(studentCodeField, nameField, emailField, passwordField, confirmPasswordField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        return formLayout;
    }

    private void handleRegistration() {
        // Validation
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

            // Message différent selon l'état du compte
            if (student.isApproved()) {
                Notification.show(
                    "✅ Inscription réussie ! Vous pouvez maintenant vous connecter.",
                    5000,
                    Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show(
                    "⏳ Inscription enregistrée ! Votre compte est en attente de validation par un administrateur. Vous recevrez une confirmation par email.",
                    7000,
                    Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            }

            // Redirection vers login
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));

        } catch (IllegalArgumentException ex) {
            Notification.show("❌ Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validateFields() {
        if (studentCodeField.isEmpty() || nameField.isEmpty() ||
            emailField.isEmpty() || passwordField.isEmpty() || confirmPasswordField.isEmpty()) {

            Notification.show("❌ Tous les champs sont obligatoires", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (emailField.isInvalid()) {
            Notification.show("❌ L'email n'est pas valide", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (passwordField.getValue().length() < 6) {
            Notification.show("❌ Le mot de passe doit contenir au moins 6 caractères", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            Notification.show("❌ Les mots de passe ne correspondent pas", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }
}
