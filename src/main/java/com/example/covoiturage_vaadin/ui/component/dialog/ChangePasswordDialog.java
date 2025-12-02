package com.example.covoiturage_vaadin.ui.component.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;

import java.util.function.BiConsumer;

/**
 * Dialog pour changer le mot de passe d'un étudiant.
 *
 * Utilisation :
 * <pre>
 * ChangePasswordDialog dialog = new ChangePasswordDialog((oldPwd, newPwd) -> {
 *     studentService.changePassword(studentId, oldPwd, newPwd);
 *     Notification.show("Mot de passe modifié avec succès !");
 * });
 * dialog.open();
 * </pre>
 */
public class ChangePasswordDialog extends Dialog {

    private final BiConsumer<String, String> onConfirm;

    private final PasswordField oldPasswordField = new PasswordField("Ancien mot de passe");
    private final PasswordField newPasswordField = new PasswordField("Nouveau mot de passe");
    private final PasswordField confirmPasswordField = new PasswordField("Confirmer le nouveau mot de passe");

    public ChangePasswordDialog(BiConsumer<String, String> onConfirm) {
        this.onConfirm = onConfirm;

        setHeaderTitle("Changer le mot de passe");
        setWidth("450px");

        // Contenu
        VerticalLayout content = createContent();

        // Boutons
        Button saveButton = new Button("Modifier", e -> handleSave());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> close());

        getFooter().add(cancelButton, saveButton);
        add(content);
    }

    private VerticalLayout createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H3 subtitle = new H3("Entrez votre nouveau mot de passe");
        subtitle.getStyle().set("margin-top", "0");

        Span info = new Span("Le mot de passe doit contenir au moins 4 caractères.");
        info.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");

        // Formulaire
        FormLayout formLayout = new FormLayout();

        oldPasswordField.setRequiredIndicatorVisible(true);
        oldPasswordField.setPlaceholder("Entrez votre ancien mot de passe");
        oldPasswordField.setWidthFull();

        newPasswordField.setRequiredIndicatorVisible(true);
        newPasswordField.setPlaceholder("Entrez votre nouveau mot de passe");
        newPasswordField.setWidthFull();
        newPasswordField.setMinLength(4);
        newPasswordField.setHelperText("Minimum 4 caractères");

        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setPlaceholder("Confirmez votre nouveau mot de passe");
        confirmPasswordField.setWidthFull();

        formLayout.add(oldPasswordField, newPasswordField, confirmPasswordField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        layout.add(subtitle, info, formLayout);
        return layout;
    }

    private void handleSave() {
        // Validation
        if (!validateFields()) {
            return;
        }

        String oldPassword = oldPasswordField.getValue();
        String newPassword = newPasswordField.getValue();

        try {
            if (onConfirm != null) {
                onConfirm.accept(oldPassword, newPassword);
            }
            close();
        } catch (Exception e) {
            Notification.show("❌ Erreur : " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validateFields() {
        if (oldPasswordField.isEmpty()) {
            Notification.show("❌ Veuillez entrer votre ancien mot de passe", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (newPasswordField.isEmpty()) {
            Notification.show("❌ Veuillez entrer un nouveau mot de passe", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (newPasswordField.getValue().length() < 4) {
            Notification.show("❌ Le nouveau mot de passe doit contenir au moins 4 caractères", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (confirmPasswordField.isEmpty()) {
            Notification.show("❌ Veuillez confirmer le nouveau mot de passe", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (!newPasswordField.getValue().equals(confirmPasswordField.getValue())) {
            Notification.show("❌ Les mots de passe ne correspondent pas", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    /**
     * Réinitialise le formulaire.
     */
    private void clearFields() {
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @Override
    public void close() {
        clearFields();
        super.close();
    }
}
