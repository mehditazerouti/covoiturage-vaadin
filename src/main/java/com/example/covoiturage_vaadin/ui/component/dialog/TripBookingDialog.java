package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.trip.TripDTO;
import com.example.covoiturage_vaadin.application.services.BookingService;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import java.time.format.DateTimeFormatter;

/**
 * Dialog de confirmation pour la réservation d'un trajet.
 * Affiche les détails du trajet avant confirmation.
 */
public class TripBookingDialog extends ConfirmDialog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TripBookingDialog(TripDTO trip, BookingService bookingService, Runnable onSuccess) {
        setHeader("Confirmer la réservation");

        // Message avec détails du trajet
        String tripInfo = trip.getDepartureAddress() + " → " + trip.getDestinationAddress();
        String dateInfo = trip.getDepartureTime().format(FORMATTER);
        String driverInfo = trip.getDriver().getName();
        String seatsInfo = trip.getAvailableSeats() + " place(s) disponible(s)";
        String regularInfo = trip.isRegular() ? "Oui" : "Non";

        setText(String.format(
            "Voulez-vous réserver ce trajet ?\n\n" +
            "📍 Trajet : %s\n" +
            "📅 Date : %s\n" +
            "👤 Conducteur : %s\n" +
            "💺 Places : %s\n" +
            "🔁 Trajet régulier : %s",
            tripInfo, dateInfo, driverInfo, seatsInfo, regularInfo
        ));

        setCancelable(true);
        setCancelText("Annuler");

        setConfirmText("Réserver");
        setConfirmButtonTheme("success primary");

        addConfirmListener(event -> {
            try {
                bookingService.createBooking(trip.getId());
                Notification.show("✅ Réservation effectuée avec succès !", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                Notification.show("❌ Erreur : " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}
