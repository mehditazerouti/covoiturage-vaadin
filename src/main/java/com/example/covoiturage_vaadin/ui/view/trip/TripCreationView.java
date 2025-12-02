package com.example.covoiturage_vaadin.ui.view.trip;

import com.example.covoiturage_vaadin.application.services.TripService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;

@Route(value = "proposer-trajet", layout = MainLayout.class)
@PageTitle("Proposer un trajet - Covoiturage")
@PermitAll
public class TripCreationView extends VerticalLayout {

    private final TripService tripService;
    private final TextField departureField = new TextField("Point de départ");
    private final TextField destinationField = new TextField("Destination");
    private final DateTimePicker timePicker = new DateTimePicker("Date et heure du départ");
    private final IntegerField seatsField = new IntegerField("Nombre de places offertes");
    private final Checkbox regularCheckbox = new Checkbox("Trajet régulier");

    // Vaadin injecte automatiquement le service
    public TripCreationView(TripService tripService) {
        this.tripService = tripService;

        H2 title = new H2("Proposer un trajet");

        // Configuration des champs
        seatsField.setMin(1);
        seatsField.setValue(1);
        regularCheckbox.setValue(false);

        Button proposeButton = new Button("Proposer le Trajet");

        proposeButton.addClickListener(e -> {
            // Validation
            if (departureField.isEmpty() || destinationField.isEmpty() || timePicker.isEmpty()) {
                Notification.show("Veuillez remplir tous les champs obligatoires.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            LocalDateTime departureTime = timePicker.getValue();
            int seats = seatsField.getValue();

            try {
                // APPEL AU CAS D'USAGE - Le conducteur est auto-assigné depuis SecurityContext
                tripService.proposeTrip(
                    departureField.getValue(),
                    destinationField.getValue(),
                    departureTime,
                    seats,
                    regularCheckbox.getValue()
                );

                Notification.show("Trajet proposé avec succès !", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Réinitialiser le formulaire
                clearForm();

            } catch (IllegalStateException ex) {
                Notification.show("Erreur : Vous devez être connecté", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        add(title, departureField, destinationField, timePicker, seatsField, regularCheckbox, proposeButton);
    }

    private void clearForm() {
        departureField.clear();
        destinationField.clear();
        timePicker.clear();
        seatsField.setValue(1);
        regularCheckbox.setValue(false);
    }
}