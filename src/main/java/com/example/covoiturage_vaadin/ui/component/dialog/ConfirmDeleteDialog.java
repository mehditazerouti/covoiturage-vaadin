package com.example.covoiturage_vaadin.ui.component.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ConfirmDeleteDialog extends Dialog {

    public ConfirmDeleteDialog(String headerTitle, String message, DeleteAction onConfirm, Runnable onSuccess) {
        // Configuration de la fenêtre
        setWidth("400px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        // --- CONTENU VISUEL ---
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(true);
        layout.setPadding(false);

        // Icône d'alerte (Rouge et grande)
        Icon warningIcon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
        warningIcon.setSize("48px");
        warningIcon.setColor("var(--lumo-error-color)");

        H3 title = new H3(headerTitle);
        title.getStyle().set("margin", "0").set("text-align", "center");

        Paragraph text = new Paragraph(message);
        text.getStyle().set("text-align", "center").set("color", "var(--lumo-secondary-text-color)");

        layout.add(warningIcon, title, text);

        // --- BOUTONS ---
        Button cancelButton = new Button("Annuler", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button deleteButton = new Button("Supprimer", e -> {
            try {
                onConfirm.execute();
                Notification.show("✅ Suppression effectuée", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                if (onSuccess != null) onSuccess.run();
                close();
            } catch (Exception ex) {
                Notification.show("❌ Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, deleteButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        add(layout, buttons);
    }

    @FunctionalInterface
    public interface DeleteAction {
        void execute() throws Exception;
    }
}