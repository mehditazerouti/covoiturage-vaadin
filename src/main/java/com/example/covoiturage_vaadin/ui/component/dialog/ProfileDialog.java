package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.student.ProfileDTO;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

public class ProfileDialog extends Dialog {

    private final StudentService studentService;
    private final Long studentId;
    private ProfileDTO profile;

    private final Button avatarButton = new Button();
    private final TextField nameField = new TextField("Nom");
    private final EmailField emailField = new EmailField("Email");
    private final Span statsText = new Span();

    public ProfileDialog(StudentService studentService, Long studentId) {
        this.studentService = studentService;
        this.studentId = studentId;

        setHeaderTitle("Mon Profil");
        setWidth("500px");

        loadProfile();
        if (profile == null) return;

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.CENTER); // Centrer l'avatar

        // --- AVATAR ---
        configureAvatar();
        content.add(avatarButton);
        
        Span changeHint = new Span("Modifier l'avatar");
        changeHint.getStyle().set("font-size", "var(--lumo-font-size-xs)").set("color", "var(--lumo-secondary-text-color)");
        content.add(changeHint);

        content.add(new Hr()); // Séparateur

        // --- FORMULAIRE ---
        FormLayout form = new FormLayout();
        form.setWidthFull();
        
        nameField.setValue(profile.getName());
        emailField.setValue(profile.getEmail());
        TextField codeField = new TextField("Code étudiant");
        codeField.setValue(profile.getStudentCode());
        codeField.setReadOnly(true);

        form.add(nameField, emailField, codeField);
        content.add(form);

        // --- STATS & SÉCURITÉ ---
        H4 statsTitle = new H4("Statistiques & Sécurité");
        statsTitle.getStyle().set("margin-top", "var(--lumo-space-m)");
        
        statsText.setText(String.format("%d trajets proposés • %d réservations", 
            profile.getTripsCount(), profile.getBookingsCount()));
        statsText.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Button pwdButton = new Button("Changer mot de passe", VaadinIcon.LOCK.create(), e -> openChangePassword());
        pwdButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        content.add(statsTitle, statsText, pwdButton);
        
        // Alignement gauche pour le bas
        content.setAlignItems(FlexComponent.Alignment.STRETCH);
        // Reset l'alignement centré juste pour l'avatar
        avatarButton.getStyle().set("align-self", "center");
        changeHint.getStyle().set("align-self", "center");

        add(content);

        // --- FOOTER ---
        Button saveBtn = new Button("Enregistrer", e -> save());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button("Fermer", e -> close()), saveBtn);
    }

    private void configureAvatar() {
        VaadinIcon icon = VaadinIcon.valueOf(profile.getAvatar());
        avatarButton.setIcon(icon.create());
        avatarButton.setHeight("80px");
        avatarButton.setWidth("80px");
        avatarButton.getStyle().set("border-radius", "50%").set("padding", "0");
        avatarButton.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_PRIMARY);
        avatarButton.addClickListener(e -> {
            new AvatarSelectionDialog(av -> {
                studentService.updateAvatar(studentId, av);
                loadProfile();
                configureAvatar(); // Refresh visual
            }).open();
        });
    }

    private void save() {
        try {
            studentService.updateProfile(studentId, nameField.getValue(), emailField.getValue());
            Notification.show("Profil mis à jour", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show(e.getMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadProfile() {
        this.profile = studentService.getProfile(studentId);
    }

    private void openChangePassword() {
        new ChangePasswordDialog((o, n) -> studentService.changePassword(studentId, o, n)).open();
    }
}