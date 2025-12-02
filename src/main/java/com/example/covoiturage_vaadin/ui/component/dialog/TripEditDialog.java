package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.trip.TripDTO;
import com.example.covoiturage_vaadin.application.services.TripService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

public class TripEditDialog extends Dialog {

    private final TripService tripService;
    private final TripDTO trip;
    private final Runnable onSaveCallback;

    private final TextField departureField = new TextField("Départ");
    private final TextField destinationField = new TextField("Destination");
    private final DateTimePicker timePicker = new DateTimePicker("Date et heure");
    private final IntegerField seatsField = new IntegerField("Places totales");

    public TripEditDialog(TripService tripService, TripDTO trip, Runnable onSaveCallback) {
        this.tripService = tripService;
        this.trip = trip;
        this.onSaveCallback = onSaveCallback;

        setHeaderTitle("Modifier le trajet");
        setWidth("500px");

        // --- FORMULAIRE ---
        FormLayout form = new FormLayout();
        
        departureField.setValue(trip.getDepartureAddress());
        destinationField.setValue(trip.getDestinationAddress());
        timePicker.setValue(trip.getDepartureTime());
        seatsField.setValue(trip.getTotalSeats());
        seatsField.setMin(1);
        seatsField.setMax(9);

        form.add(departureField, destinationField, timePicker, seatsField);
        // Responsive : 2 colonnes sauf sur mobile
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("400px", 2)
        );
        form.setColspan(timePicker, 2);

        add(form);

        // --- BOUTONS ---
        Button deleteButton = new Button("Supprimer", e -> confirmDelete());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.getStyle().set("margin-right", "auto"); // Pousse à gauche

        Button cancelButton = new Button("Annuler", e -> close());
        Button saveButton = new Button("Enregistrer", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(deleteButton, cancelButton, saveButton);
    }

    private void save() {
        if (departureField.isEmpty() || destinationField.isEmpty() || timePicker.isEmpty() || seatsField.isEmpty()) {
            Notification.show("Veuillez remplir tous les champs", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        try {
            tripService.updateTrip(trip.getId(), departureField.getValue(), destinationField.getValue(), timePicker.getValue(), seatsField.getValue());
            Notification.show("Trajet modifié", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            if (onSaveCallback != null) onSaveCallback.run();
            close();
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage());
        }
    }

    private void confirmDelete() {
        new ConfirmDeleteDialog("Supprimer le trajet", "Cette action est irréversible.", 
            () -> tripService.deleteTrip(trip.getId()), 
            () -> {
                if (onSaveCallback != null) onSaveCallback.run();
                close();
            }
        ).open();
    }
}