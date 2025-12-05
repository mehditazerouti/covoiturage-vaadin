package com.example.covoiturage_vaadin.ui.component.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import java.util.function.BiConsumer;

public class ChangePasswordDialog extends Dialog {

    public ChangePasswordDialog(BiConsumer<String, String> onConfirm) {
        setHeaderTitle("Modifier le mot de passe");
        setWidth("400px");

        PasswordField oldPass = new PasswordField("Ancien mot de passe");
        PasswordField newPass = new PasswordField("Nouveau mot de passe");
        PasswordField confirmPass = new PasswordField("Confirmer le nouveau mot de passe");
        
        oldPass.setWidthFull();
        newPass.setWidthFull();
        confirmPass.setWidthFull();

        VerticalLayout layout = new VerticalLayout(oldPass, newPass, confirmPass);
        layout.setPadding(false);
        add(layout);

        Button saveButton = new Button("Modifier", e -> {
            // Réinitialiser les erreurs
            oldPass.setInvalid(false);
            newPass.setInvalid(false);
            confirmPass.setInvalid(false);

            // Validation des champs vides
            if (oldPass.isEmpty()) {
                oldPass.setInvalid(true);
                oldPass.setErrorMessage("L'ancien mot de passe est requis");
                return;
            }
            if (newPass.isEmpty()) {
                newPass.setInvalid(true);
                newPass.setErrorMessage("Le nouveau mot de passe est requis");
                return;
            }
            if (confirmPass.isEmpty()) {
                confirmPass.setInvalid(true);
                confirmPass.setErrorMessage("Veuillez confirmer le nouveau mot de passe");
                return;
            }

            // Validation de la correspondance des mots de passe
            if (!newPass.getValue().equals(confirmPass.getValue())) {
                confirmPass.setInvalid(true);
                confirmPass.setErrorMessage("Les mots de passe ne correspondent pas");
                Notification.show("Les mots de passe ne correspondent pas", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Validation de la longueur minimale
            if (newPass.getValue().length() < 6) {
                newPass.setInvalid(true);
                newPass.setErrorMessage("Le mot de passe doit contenir au moins 6 caractères");
                return;
            }

            try {
                onConfirm.accept(oldPass.getValue(), newPass.getValue());
                Notification.show("Mot de passe modifié avec succès", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                close();
            } catch (IllegalArgumentException ex) {
                // Ancien mot de passe incorrect
                oldPass.setInvalid(true);
                oldPass.setErrorMessage("Mot de passe incorrect");
                Notification.show("Erreur: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Erreur lors de la modification: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(new Button("Annuler", event -> close()), saveButton);
    }
}