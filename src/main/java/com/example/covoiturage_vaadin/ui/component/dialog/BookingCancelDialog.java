package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.booking.BookingDTO;
import com.example.covoiturage_vaadin.application.services.BookingService;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import java.time.format.DateTimeFormatter;

/**
 * Dialog de confirmation pour l'annulation d'une réservation.
 * Affiche les détails de la réservation avant confirmation.
 */
public class BookingCancelDialog extends ConfirmDialog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BookingCancelDialog(BookingDTO booking, BookingService bookingService, Runnable onSuccess) {
        setHeader("Annuler la réservation");

        // Message avec détails de la réservation
        String tripInfo = booking.getTrip().getDepartureAddress() + " → " + booking.getTrip().getDestinationAddress();
        String dateInfo = booking.getTrip().getDepartureTime().format(FORMATTER);
        String driverInfo = booking.getTrip().getDriver().getName();

        setText(String.format(
            "Voulez-vous vraiment annuler cette réservation ?\n\n" +
            "📍 Trajet : %s\n" +
            "📅 Date : %s\n" +
            "👤 Conducteur : %s",
            tripInfo, dateInfo, driverInfo
        ));

        setCancelable(true);
        setCancelText("Non, garder");

        setConfirmText("Oui, annuler");
        setConfirmButtonTheme("error primary");

        addConfirmListener(event -> {
            try {
                bookingService.cancelBooking(booking.getId());
                Notification.show("✅ Réservation annulée avec succès", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {
                Notification.show("❌ Erreur : " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}
