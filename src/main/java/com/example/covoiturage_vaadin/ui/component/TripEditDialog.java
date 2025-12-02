package com.example.covoiturage_vaadin.ui.component;

import com.example.covoiturage_vaadin.application.dto.trip.TripDTO;
import com.example.covoiturage_vaadin.application.services.TripService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

public class TripEditDialog extends Dialog {

    private final TripService tripService;
    private final TripDTO trip;
    private final Runnable onSaveCallback;

    private final TextField departureField = new TextField("Point de départ");
    private final TextField destinationField = new TextField("Destination");
    private final DateTimePicker timePicker = new DateTimePicker("Date et heure du départ");
    private final IntegerField seatsField = new IntegerField("Nombre de places totales");

    public TripEditDialog(TripService tripService, TripDTO trip, Runnable onSaveCallback) {
        this.tripService = tripService;
        this.trip = trip;
        this.onSaveCallback = onSaveCallback;

        setHeaderTitle("Modifier le trajet");
        setModal(true);
        setDraggable(false);
        setResizable(false);

        configureForm();
        configureButtons();
    }

    private void configureForm() {
        // Pré-remplir les champs avec les valeurs actuelles
        departureField.setValue(trip.getDepartureAddress());
        departureField.setRequired(true);
        departureField.setWidthFull();

        destinationField.setValue(trip.getDestinationAddress());
        destinationField.setRequired(true);
        destinationField.setWidthFull();

        timePicker.setValue(trip.getDepartureTime());
        timePicker.setWidthFull();

        seatsField.setValue(trip.getTotalSeats());
        seatsField.setMin(1);
        seatsField.setRequired(true);
        seatsField.setWidthFull();
        seatsField.setHelperText("Places actuellement disponibles: " + trip.getAvailableSeats() + "/" + trip.getTotalSeats());

        VerticalLayout formLayout = new VerticalLayout(
            departureField,
            destinationField,
            timePicker,
            seatsField
        );
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        add(formLayout);
    }

    private void configureButtons() {
        Button saveButton = new Button("Valider", e -> saveTrip());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button deleteButton = new Button("Supprimer", e -> deleteTrip());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Annuler", e -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, deleteButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);

        add(buttonLayout);
    }

    private void saveTrip() {
        // Validation
        if (departureField.isEmpty() || destinationField.isEmpty() || timePicker.isEmpty() || seatsField.isEmpty()) {
            Notification.show("Veuillez remplir tous les champs", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            tripService.updateTrip(
                trip.getId(),
                departureField.getValue(),
                destinationField.getValue(),
                timePicker.getValue(),
                seatsField.getValue()
            );

            Notification.show("Trajet modifié avec succès", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            close();
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

        } catch (IllegalArgumentException ex) {
            Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (IllegalStateException ex) {
            Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteTrip() {
        // Créer un dialog de confirmation
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmer la suppression");

        VerticalLayout content = new VerticalLayout();
        content.add("Êtes-vous sûr de vouloir supprimer ce trajet ?");
        content.add("Cette action est irréversible.");
        confirmDialog.add(content);

        Button confirmButton = new Button("Oui, supprimer", e -> {
            try {
                tripService.deleteTrip(trip.getId());

                Notification.show("Trajet supprimé avec succès", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                confirmDialog.close();
                close();
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }

            } catch (IllegalStateException ex) {
                Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        confirmDialog.getFooter().add(buttonLayout);

        confirmDialog.open();
    }
}
