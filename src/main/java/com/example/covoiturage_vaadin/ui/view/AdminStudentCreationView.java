package com.example.covoiturage_vaadin.ui.view;

import com.example.covoiturage_vaadin.application.services.SecurityContextService;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * Vue d'administration pour créer des étudiants.
 * L'admin saisit nom, email, code étudiant.
 * Le mot de passe est généré automatiquement et affiché à l'admin.
 */
@Route(value = "admin/create-student", layout = MainLayout.class)
@PageTitle("Créer un Étudiant - Admin")
@RolesAllowed("ADMIN")
public class AdminStudentCreationView extends VerticalLayout {

    private final StudentService studentService;
    private final SecurityContextService securityContext;

    private final TextField nameField = new TextField("Nom complet");
    private final EmailField emailField = new EmailField("Email");
    private final TextField studentCodeField = new TextField("Code étudiant");
    private final Button createButton = new Button("Créer l'étudiant", VaadinIcon.USER_CHECK.create());
    private final Button clearButton = new Button("Effacer");

    public AdminStudentCreationView(StudentService studentService,
                                   SecurityContextService securityContext) {
        this.studentService = studentService;
        this.securityContext = securityContext;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Titre
        H2 title = new H2("Créer un étudiant");
        Paragraph subtitle = new Paragraph("Le mot de passe sera généré automatiquement (4 lettres du nom + 4 premiers caractères du code)");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Formulaire
        FormLayout formLayout = createFormLayout();

        // Boutons
        HorizontalLayout buttons = createButtons();

        // Container
        VerticalLayout container = new VerticalLayout(title, subtitle, formLayout, buttons);
        container.setMaxWidth("600px");
        container.setAlignItems(Alignment.STRETCH);
        container.setPadding(true);
        container.getStyle().set("background", "var(--lumo-base-color)")
                            .set("border-radius", "var(--lumo-border-radius-m)")
                            .set("box-shadow", "var(--lumo-box-shadow-m)");

        add(container);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();

        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("Ex: Salim Bouskine");
        nameField.setWidthFull();

        emailField.setRequiredIndicatorVisible(true);
        emailField.setPlaceholder("Ex: salim.bouskine@dauphine.eu");
        emailField.setWidthFull();

        studentCodeField.setRequiredIndicatorVisible(true);
        studentCodeField.setPlaceholder("Ex: 22405100");
        studentCodeField.setWidthFull();

        formLayout.add(nameField, emailField, studentCodeField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        return formLayout;
    }

    private HorizontalLayout createButtons() {
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> confirmCreateStudent());

        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.addClickListener(e -> clearForm());

        HorizontalLayout buttons = new HorizontalLayout(createButton, clearButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return buttons;
    }

    private void confirmCreateStudent() {
        // Validation
        if (!validateFields()) {
            return;
        }

        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirmer la création");
        confirmDialog.setText("Voulez-vous vraiment créer cet étudiant ?\n\n" +
                              "Nom : " + nameField.getValue() + "\n" +
                              "Email : " + emailField.getValue() + "\n" +
                              "Code : " + studentCodeField.getValue());

        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Annuler");

        confirmDialog.setConfirmText("Créer");
        confirmDialog.setConfirmButtonTheme("primary");

        confirmDialog.addConfirmListener(event -> createStudent());

        confirmDialog.open();
    }

    private void createStudent() {
        try {
            // Récupérer le username de l'admin connecté
            String adminUsername = securityContext.getCurrentUsername()
                    .orElseThrow(() -> new IllegalStateException("Impossible de récupérer l'utilisateur connecté"));

            StudentService.StudentCreationResult result = studentService.createStudentAsAdmin(
                nameField.getValue().trim(),
                emailField.getValue().trim(),
                studentCodeField.getValue().trim(),
                adminUsername  // ✅ Passer l'admin pour traçabilité whitelist
            );

            // Succès : afficher le mot de passe dans un dialog
            showPasswordDialog(result.getStudent().getName(),
                              result.getStudent().getUsername(),
                              result.getPlainPassword());

            // Vider le formulaire
            clearForm();

        } catch (IllegalArgumentException ex) {
            Notification.show("❌ Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (IllegalStateException ex) {
            Notification.show("❌ Erreur système : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showPasswordDialog(String studentName, String username, String password) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("✅ Étudiant créé avec succès");

        H3 successTitle = new H3("L'étudiant a été créé !");
        successTitle.getStyle().set("color", "var(--lumo-success-color)").set("margin-top", "0");

        Paragraph info = new Paragraph("Voici les identifiants de connexion :");
        info.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        // Affichage des identifiants avec style
        VerticalLayout credentialsLayout = new VerticalLayout();
        credentialsLayout.setPadding(true);
        credentialsLayout.setSpacing(false);
        credentialsLayout.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("border", "1px solid var(--lumo-contrast-10pct)");

        Span nameLabel = new Span("Nom : ");
        nameLabel.getStyle().set("font-weight", "bold");
        Span nameValue = new Span(studentName);

        Span usernameLabel = new Span("Nom d'utilisateur : ");
        usernameLabel.getStyle().set("font-weight", "bold");
        Span usernameValue = new Span(username);

        Span passwordLabel = new Span("Mot de passe : ");
        passwordLabel.getStyle().set("font-weight", "bold");
        Span passwordValue = new Span(password);
        passwordValue.getStyle().set("font-family", "monospace")
                                .set("color", "var(--lumo-primary-color)")
                                .set("font-size", "var(--lumo-font-size-l)");

        HorizontalLayout nameLine = new HorizontalLayout(nameLabel, nameValue);
        nameLine.setSpacing(false);
        HorizontalLayout usernameLine = new HorizontalLayout(usernameLabel, usernameValue);
        usernameLine.setSpacing(false);
        HorizontalLayout passwordLine = new HorizontalLayout(passwordLabel, passwordValue);
        passwordLine.setSpacing(false);

        credentialsLayout.add(nameLine, usernameLine, passwordLine);

        Paragraph warning = new Paragraph("⚠️ Notez ce mot de passe, il ne sera plus affiché !");
        warning.getStyle().set("color", "var(--lumo-error-color)")
                         .set("font-weight", "bold");

        Button closeButton = new Button("Fermer", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout layout = new VerticalLayout(successTitle, info, credentialsLayout, warning, closeButton);
        layout.setAlignItems(Alignment.STRETCH);

        dialog.add(layout);
        dialog.setWidth("500px");
        dialog.open();
    }

    private boolean validateFields() {
        if (nameField.isEmpty() || emailField.isEmpty() || studentCodeField.isEmpty()) {
            Notification.show("❌ Tous les champs sont obligatoires", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (emailField.isInvalid()) {
            Notification.show("❌ L'email n'est pas valide", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        studentCodeField.clear();
    }
}
