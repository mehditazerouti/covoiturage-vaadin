package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

public class AdminStudentProfileDialog extends Dialog {

    private final StudentService studentService;
    private final Long studentId;
    private final Runnable onSaveCallback;

    private TextField nameField, usernameField, studentCodeField;
    private EmailField emailField;
    private Select<String> roleSelect;
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

        // Formulaire principal
        FormLayout form = new FormLayout();
        nameField = new TextField("Nom"); nameField.setValue(student.getName());
        emailField = new EmailField("Email"); emailField.setValue(student.getEmail());
        usernameField = new TextField("Nom d'utilisateur"); usernameField.setValue(student.getUsername());
        studentCodeField = new TextField("Code"); studentCodeField.setValue(student.getStudentCode());
        usernameField.setReadOnly(true);
        studentCodeField.setReadOnly(true);
        
        form.add(nameField, emailField, usernameField, studentCodeField);
        

        // Reset Password
        Button resetPwdBtn = new Button("Réinitialiser mot de passe", VaadinIcon.KEY.create());
        resetPwdBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetPwdBtn.addClickListener(e -> openResetPassword());

        content.add(form, new Hr(), resetPwdBtn);
        add(content);

        // Footer
        Button saveBtn = new Button("Enregistrer les modifications", e -> save());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button("Annuler", e -> close()), saveBtn);
    }

    private void save() {
        try {
            // Reconstitution du DTO (simplifiée pour l'exemple)
            StudentDTO original = studentService.getStudentById(studentId).get();
            StudentDTO update = new StudentDTO(
                studentId, nameField.getValue(), emailField.getValue(),
                studentCodeField.getValue(), usernameField.getValue(),
                roleSelect.getValue(), enabledBox.getValue(), approvedBox.getValue(),
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