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
            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Notification.show("Champs obligatoires", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (!newPass.getValue().equals(confirmPass.getValue())) {
                Notification.show("Les mots de passe ne correspondent pas", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                onConfirm.accept(oldPass.getValue(), newPass.getValue());
                close();
            } catch (Exception ex) {
                // L'erreur sera gérée par le callback si nécessaire, ou affichée ici
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(new Button("Annuler", event -> close()), saveButton);
    }
}