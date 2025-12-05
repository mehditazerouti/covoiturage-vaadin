package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

public class AdminStudentProfileDialog extends Dialog {

    private final StudentService studentService;
    private final Long studentId;
    private final Runnable onSaveCallback;

    private TextField nameField, usernameField, studentCodeField;
    private EmailField emailField;
    private Checkbox enabledBox, approvedBox;

    public AdminStudentProfileDialog(StudentService studentService, Long studentId, Runnable onSaveCallback) {
        this.studentService = studentService;
        this.studentId = studentId;
        this.onSaveCallback = onSaveCallback;

        setHeaderTitle("Édition Étudiant (Admin)");
        setWidth("600px");

        StudentDTO student = studentService.getStudentById(studentId).orElse(null);
        if (student == null) { close(); return; }

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // === SECTION 1 : Informations utilisateur ===
        H3 infoTitle = new H3("Informations utilisateur");
        infoTitle.getStyle().set("margin-top", "0");
        infoTitle.getStyle().set("margin-bottom", "10px");
        infoTitle.getStyle().set("color", "#1676F3");

        FormLayout infoForm = new FormLayout();
        nameField = new TextField("Nom");
        nameField.setValue(student.getName());

        emailField = new EmailField("Email");
        emailField.setValue(student.getEmail());

        usernameField = new TextField("Nom d'utilisateur");
        usernameField.setValue(student.getUsername());
        usernameField.setReadOnly(true);

        studentCodeField = new TextField("Code étudiant");
        studentCodeField.setValue(student.getStudentCode());
        studentCodeField.setReadOnly(true);

        infoForm.add(nameField, emailField, usernameField, studentCodeField);

        // Bouton réinitialisation mot de passe
        Button resetPwdBtn = new Button("Réinitialiser le mot de passe", VaadinIcon.KEY.create());
        resetPwdBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetPwdBtn.addClickListener(e -> openResetPassword());
        resetPwdBtn.getStyle().set("margin-top", "10px");

        content.add(infoTitle, infoForm, resetPwdBtn, new Hr());

        // === SECTION 2 : Administration ===
        H3 adminTitle = new H3("Administration");
        adminTitle.getStyle().set("margin-top", "10px");
        adminTitle.getStyle().set("margin-bottom", "10px");
        adminTitle.getStyle().set("color", "#1676F3");

        enabledBox = new Checkbox("Compte activé");
        enabledBox.setValue(student.isEnabled());

        approvedBox = new Checkbox("Approuvé");
        approvedBox.setValue(student.isApproved());

        VerticalLayout adminLayout = new VerticalLayout(enabledBox, approvedBox);
        adminLayout.setPadding(false);
        adminLayout.setSpacing(false);

        content.add(adminTitle, adminLayout);
        add(content);

        // Footer
        Button saveBtn = new Button("Enregistrer les modifications", e -> save());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button("Annuler", e -> close()), saveBtn);
    }

    private void save() {
        try {
            // Reconstitution du DTO (conserve le rôle original)
            StudentDTO original = studentService.getStudentById(studentId).get();
            StudentDTO update = new StudentDTO(
                studentId, nameField.getValue(), emailField.getValue(),
                studentCodeField.getValue(), usernameField.getValue(),
                original.getRole(), // Conserver le rôle existant
                enabledBox.getValue(), approvedBox.getValue(),
                original.getCreatedAt(), original.getAvatar()
            );

            studentService.updateStudentAdmin(update);
            Notification.show("Sauvegardé", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            if (onSaveCallback != null) onSaveCallback.run();
            close();
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openResetPassword() {
        new ChangePasswordDialog((o, n) -> studentService.changePassword(studentId, o, n)).open();
    }
}