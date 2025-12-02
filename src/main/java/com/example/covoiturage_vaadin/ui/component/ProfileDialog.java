package com.example.covoiturage_vaadin.ui.component;

import com.example.covoiturage_vaadin.application.dto.student.ProfileDTO;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

import java.time.format.DateTimeFormatter;

/**
 * Dialog principal de profil utilisateur.
 *
 * Affiche toutes les informations du profil avec possibilité de modification :
 * - Avatar (cliquable → AvatarSelectionDialog)
 * - Nom et email (modifiables inline)
 * - Mot de passe (bouton → ChangePasswordDialog)
 * - Code étudiant (lecture seule)
 * - Statistiques (trajets proposés, réservations)
 * - Date de création du compte
 *
 * Utilisation :
 * <pre>
 * ProfileDialog dialog = new ProfileDialog(studentService, studentId);
 * dialog.open();
 * </pre>
 */
public class ProfileDialog extends Dialog {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    private final StudentService studentService;
    private final Long studentId;

    private ProfileDTO profile;

    // Composants modifiables
    private final Button avatarButton = new Button();
    private final TextField nameField = new TextField("Nom complet");
    private final EmailField emailField = new EmailField("Email");
    private final Button changePasswordButton = new Button("Modifier le mot de passe", VaadinIcon.LOCK.create());

    // Composants en lecture seule
    private final Span tripsCountSpan = new Span();
    private final Span bookingsCountSpan = new Span();
    private final Span createdAtSpan = new Span();

    public ProfileDialog(StudentService studentService, Long studentId) {
        this.studentService = studentService;
        this.studentId = studentId;

        setHeaderTitle("Mon Profil");
        setWidth("550px");
        setHeight("auto");

        loadProfile();
        buildContent();

        // Boutons footer
        Button saveButton = new Button("Enregistrer", e -> handleSave());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Fermer", e -> close());

        getFooter().add(closeButton, saveButton);
    }

    private void loadProfile() {
        profile = studentService.getProfile(studentId);
        if (profile == null) {
            Notification.show("❌ Impossible de charger le profil", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            close();
        }
    }

    private void buildContent() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);

        // Section Avatar
        VerticalLayout avatarSection = createAvatarSection();

        // Section Informations personnelles
        FormLayout personalInfoSection = createPersonalInfoSection();

        // Section Sécurité
        VerticalLayout securitySection = createSecuritySection();

        // Section Statistiques
        VerticalLayout statsSection = createStatsSection();

        mainLayout.add(avatarSection, personalInfoSection, securitySection, statsSection);
        add(mainLayout);
    }

    private VerticalLayout createAvatarSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(false);

        // Avatar cliquable
        VaadinIcon vaadinIcon = VaadinIcon.valueOf(profile.getAvatar());
        Icon icon = vaadinIcon.create();
        icon.setSize("64px");

        avatarButton.setIcon(icon);
        avatarButton.getStyle()
            .set("width", "100px")
            .set("height", "100px")
            .set("border-radius", "50%");
        avatarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        avatarButton.addClickListener(e -> openAvatarSelection());

        Span avatarHint = new Span("Cliquez pour changer");
        avatarHint.getStyle()
            .set("font-size", "var(--lumo-font-size-s)")
            .set("color", "var(--lumo-secondary-text-color)");

        layout.add(avatarButton, avatarHint);
        return layout;
    }

    private FormLayout createPersonalInfoSection() {
        FormLayout formLayout = new FormLayout();

        H4 title = new H4("Informations personnelles");
        title.getStyle().set("margin-top", "var(--lumo-space-m)");

        nameField.setValue(profile.getName());
        nameField.setRequiredIndicatorVisible(true);
        nameField.setWidthFull();

        emailField.setValue(profile.getEmail());
        emailField.setRequiredIndicatorVisible(true);
        emailField.setWidthFull();

        // Code étudiant (lecture seule)
        TextField studentCodeField = new TextField("Code étudiant");
        studentCodeField.setValue(profile.getStudentCode());
        studentCodeField.setReadOnly(true);
        studentCodeField.setWidthFull();

        formLayout.add(title);
        formLayout.setColspan(title, 2);
        formLayout.add(nameField, emailField, studentCodeField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        return formLayout;
    }

    private VerticalLayout createSecuritySection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H4 title = new H4("Sécurité");

        // Affichage du mot de passe en étoiles
        HorizontalLayout passwordLayout = new HorizontalLayout();
        passwordLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        passwordLayout.setWidthFull();

        Span passwordLabel = new Span("Mot de passe : ");
        passwordLabel.getStyle().set("font-weight", "bold");

        Span passwordValue = new Span("**********");
        passwordValue.getStyle().set("font-family", "monospace");

        passwordLayout.add(passwordLabel, passwordValue);

        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        changePasswordButton.addClickListener(e -> openChangePassword());

        layout.add(title, passwordLayout, changePasswordButton);
        return layout;
    }

    private VerticalLayout createStatsSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H4 title = new H4("Statistiques");

        // Trajets proposés
        HorizontalLayout tripsLayout = new HorizontalLayout();
        Icon tripsIcon = VaadinIcon.CAR.create();
        tripsIcon.getStyle().set("color", "var(--lumo-primary-color)");
        Span tripsLabel = new Span("Trajets proposés : ");
        tripsCountSpan.setText(String.valueOf(profile.getTripsCount()));
        tripsCountSpan.getStyle().set("font-weight", "bold");
        tripsLayout.add(tripsIcon, tripsLabel, tripsCountSpan);

        // Réservations effectuées
        HorizontalLayout bookingsLayout = new HorizontalLayout();
        Icon bookingsIcon = VaadinIcon.TICKET.create();
        bookingsIcon.getStyle().set("color", "var(--lumo-success-color)");
        Span bookingsLabel = new Span("Réservations effectuées : ");
        bookingsCountSpan.setText(String.valueOf(profile.getBookingsCount()));
        bookingsCountSpan.getStyle().set("font-weight", "bold");
        bookingsLayout.add(bookingsIcon, bookingsLabel, bookingsCountSpan);

        // Date de création du compte
        HorizontalLayout createdAtLayout = new HorizontalLayout();
        Icon calendarIcon = VaadinIcon.CALENDAR.create();
        calendarIcon.getStyle().set("color", "var(--lumo-contrast-60pct)");
        Span createdAtLabel = new Span("Membre depuis : ");
        createdAtSpan.setText(profile.getCreatedAt().format(DATE_FORMATTER));
        createdAtSpan.getStyle().set("font-style", "italic");
        createdAtLayout.add(calendarIcon, createdAtLabel, createdAtSpan);

        layout.add(title, tripsLayout, bookingsLayout, createdAtLayout);
        return layout;
    }

    private void openAvatarSelection() {
        AvatarSelectionDialog avatarDialog = new AvatarSelectionDialog(selectedAvatar -> {
            try {
                studentService.updateAvatar(studentId, selectedAvatar);
                Notification.show("✅ Avatar modifié avec succès !", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshProfile();
            } catch (Exception e) {
                Notification.show("❌ Erreur : " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        avatarDialog.setCurrentAvatar(profile.getAvatar());
        avatarDialog.open();
    }

    private void openChangePassword() {
        ChangePasswordDialog passwordDialog = new ChangePasswordDialog((oldPwd, newPwd) -> {
            try {
                studentService.changePassword(studentId, oldPwd, newPwd);
                Notification.show("✅ Mot de passe modifié avec succès !", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                Notification.show("❌ Erreur : " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        passwordDialog.open();
    }

    private void handleSave() {
        // Validation
        if (nameField.isEmpty() || emailField.isEmpty()) {
            Notification.show("❌ Le nom et l'email sont obligatoires", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (emailField.isInvalid()) {
            Notification.show("❌ L'email n'est pas valide", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            studentService.updateProfile(studentId, nameField.getValue(), emailField.getValue());
            Notification.show("✅ Profil mis à jour avec succès !", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshProfile();
        } catch (Exception e) {
            Notification.show("❌ Erreur : " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Rafraîchit le profil avec les données à jour.
     */
    private void refreshProfile() {
        loadProfile();
        updateFields();
    }

    /**
     * Met à jour les champs du formulaire avec les données du profil.
     */
    private void updateFields() {
        if (profile != null) {
            // Avatar
            VaadinIcon vaadinIcon = VaadinIcon.valueOf(profile.getAvatar());
            Icon icon = vaadinIcon.create();
            icon.setSize("64px");
            avatarButton.setIcon(icon);

            // Champs modifiables
            nameField.setValue(profile.getName());
            emailField.setValue(profile.getEmail());

            // Stats
            tripsCountSpan.setText(String.valueOf(profile.getTripsCount()));
            bookingsCountSpan.setText(String.valueOf(profile.getBookingsCount()));
            createdAtSpan.setText(profile.getCreatedAt().format(DATE_FORMATTER));
        }
    }
}
