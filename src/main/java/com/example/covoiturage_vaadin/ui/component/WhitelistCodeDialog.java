package com.example.covoiturage_vaadin.ui.component;

import com.example.covoiturage_vaadin.application.services.AllowedStudentCodeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Dialog pour ajouter un code étudiant à la whitelist.
 * Formulaire avec validation et gestion des erreurs.
 */
public class WhitelistCodeDialog extends Dialog {

    private final TextField codeField;
    private final AllowedStudentCodeService codeService;
    private final String currentUsername;
    private final Runnable onSuccess;

    public WhitelistCodeDialog(AllowedStudentCodeService codeService, String currentUsername, Runnable onSuccess) {
        this.codeService = codeService;
        this.currentUsername = currentUsername;
        this.onSuccess = onSuccess;

        setHeaderTitle("Ajouter un code étudiant");

        // Champ de saisie
        codeField = new TextField("Code étudiant");
        codeField.setPlaceholder("Ex: 22405100");
        codeField.setWidthFull();
        codeField.setRequired(true);
        codeField.setMinLength(5);
        codeField.setMaxLength(20);

        // Listener pour la touche ENTER
        codeField.addKeyPressListener(event -> {
            if (event.getKey().getKeys().contains("Enter")) {
                handleSave();
            }
        });

        // Boutons
        Button saveButton = new Button("Ajouter", e -> handleSave());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        // Layout principal
        VerticalLayout layout = new VerticalLayout(codeField, buttons);
        layout.setPadding(false);
        layout.setSpacing(true);

        add(layout);

        // Focus automatique sur le champ
        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                codeField.focus();
            }
        });
    }

    private void handleSave() {
        String code = codeField.getValue();

        // Validation
        if (code == null || code.trim().isEmpty()) {
            Notification.show("⚠️ Veuillez entrer un code étudiant", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            codeField.focus();
            return;
        }

        if (code.trim().length() < 5) {
            Notification.show("⚠️ Le code doit contenir au moins 5 caractères", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            codeField.focus();
            return;
        }

        // Tentative d'ajout
        try {
            codeService.addAllowedCode(code.trim(), currentUsername);
            Notification.show("✅ Code ajouté avec succès", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            if (onSuccess != null) {
                onSuccess.run();
            }
            close();
        } catch (IllegalArgumentException ex) {
            Notification.show("❌ Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            codeField.focus();
        }
    }
}
