package com.example.covoiturage_vaadin.ui.view.admin;

import com.example.covoiturage_vaadin.application.services.SecurityContextService;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
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
        setPadding(false);
        setSpacing(false);
        // Fond gris global
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // --- 1. HEADER DÉTACHÉ ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
            .set("background", "white")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");

        H2 title = new H2("Créer un étudiant");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem");
        header.add(title);

        // --- 2. CONTENU PRINCIPAL ---
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setAlignItems(Alignment.CENTER); // Centrer la carte
        mainContent.setJustifyContentMode(JustifyContentMode.CENTER);

        // --- 3. CARTE FORMULAIRE ---
        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("600px");
        card.setWidth("100%");
        card.setPadding(true);
        card.setSpacing(true);
        
        // Style Clean Card
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "0 10px 40px rgba(0,0,0,0.06)")
            .set("padding", "var(--lumo-space-xl)");

        Paragraph subtitle = new Paragraph("Le mot de passe sera généré automatiquement (4 lettres du nom + 4 premiers caractères du code)");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");

        FormLayout formLayout = createFormLayout();
        HorizontalLayout buttons = createButtons();

        card.add(subtitle, formLayout, buttons);
        
        mainContent.add(card);
        add(header, mainContent);
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
        createButton.getStyle().set("cursor", "pointer");
        createButton.addClickListener(e -> confirmCreateStudent());

        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.getStyle().set("cursor", "pointer");
        clearButton.addClickListener(e -> clearForm());

        HorizontalLayout buttons = new HorizontalLayout(createButton, clearButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN); // Espacement élégant
        buttons.getStyle().set("margin-top", "var(--lumo-space-m)");
        
        return buttons;
    }

    private void confirmCreateStudent() {
        if (!validateFields()) return;

        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirmer la création");
        confirmDialog.setText("Voulez-vous vraiment créer cet étudiant ?\n" + nameField.getValue());
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Créer");
        confirmDialog.setConfirmButtonTheme("primary");
        confirmDialog.addConfirmListener(event -> createStudent());
        confirmDialog.open();
    }

    private void createStudent() {
        try {
            String adminUsername = securityContext.getCurrentUsername()
                    .orElseThrow(() -> new IllegalStateException("Impossible de récupérer l'utilisateur connecté"));

            StudentService.StudentCreationResult result = studentService.createStudentAsAdmin(
                nameField.getValue().trim(),
                emailField.getValue().trim(),
                studentCodeField.getValue().trim(),
                adminUsername
            );

            showPasswordDialog(result.getStudent().getName(), result.getStudent().getUsername(), result.getPlainPassword());
            clearForm();

        } catch (Exception ex) {
            Notification.show("❌ Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showPasswordDialog(String studentName, String username, String password) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("✅ Étudiant créé");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        // Zone de mot de passe stylisée
        VerticalLayout credentialsBox = new VerticalLayout();
        credentialsBox.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "8px")
            .set("padding", "var(--lumo-space-m)")
            .set("border", "1px solid var(--lumo-contrast-10pct)");

        credentialsBox.add(
            new Span("Nom d'utilisateur : " + username),
            createPasswordSpan(password)
        );

        layout.add(new Paragraph("Notez ces identifiants, ils ne seront plus affichés."), credentialsBox);

        Button closeButton = new Button("Fermer", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.setWidthFull();
        
        layout.add(closeButton);
        dialog.add(layout);
        dialog.open();
    }
    
    private Span createPasswordSpan(String password) {
        Span span = new Span("Mot de passe : " + password);
        span.getStyle()
            .set("font-family", "monospace")
            .set("font-weight", "bold")
            .set("font-size", "1.1em")
            .set("color", "var(--lumo-primary-color)");
        return span;
    }

    private boolean validateFields() {
        boolean isValid = true;
        if (nameField.isEmpty()) { nameField.setInvalid(true); isValid = false; }
        if (emailField.isEmpty() || emailField.isInvalid()) { emailField.setInvalid(true); isValid = false; }
        if (studentCodeField.isEmpty()) { studentCodeField.setInvalid(true); isValid = false; }
        return isValid;
    }

    private void clearForm() {
        nameField.clear(); nameField.setInvalid(false);
        emailField.clear(); emailField.setInvalid(false);
        studentCodeField.clear(); studentCodeField.setInvalid(false);
    }
}