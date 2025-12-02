package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.services.AllowedStudentCodeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class WhitelistCodeDialog extends Dialog {

    public WhitelistCodeDialog(AllowedStudentCodeService codeService, String currentUsername, Runnable onSuccess) {
        setHeaderTitle("Nouveau code autorisé");
        setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        Paragraph hint = new Paragraph("Ajoutez un code étudiant qui sera autorisé à s'inscrire.");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");

        TextField codeField = new TextField("Code étudiant");
        codeField.setPlaceholder("Ex: 22405100");
        codeField.setWidthFull();
        codeField.focus();

        layout.add(hint, codeField);
        add(layout);

        Button saveButton = new Button("Ajouter", e -> {
            String val = codeField.getValue();
            if (val == null || val.length() < 5) {
                Notification.show("Code invalide (min 5 caractères)", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                codeService.addAllowedCode(val.trim(), currentUsername);
                Notification.show("Code ajouté", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                if (onSuccess != null) onSuccess.run();
                close();
            } catch (Exception ex) {
                Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        // Support Entrée
        codeField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> saveButton.click());

        Button cancelButton = new Button("Annuler", e -> close());

        getFooter().add(cancelButton, saveButton);
    }
}