package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.booking.BookingDTO;
import com.example.covoiturage_vaadin.application.services.BookingService;
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

import java.time.format.DateTimeFormatter;

public class BookingCancelDialog extends Dialog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BookingCancelDialog(BookingDTO booking, BookingService bookingService, Runnable onSuccess) {
        setHeaderTitle("Annuler la réservation");
        setWidth("450px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        Paragraph warning = new Paragraph("Êtes-vous sûr de vouloir annuler ce trajet ?");
        
        // Carte info du trajet
        VerticalLayout infoCard = new VerticalLayout();
        infoCard.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "8px")
            .set("padding", "var(--lumo-space-m)");
            
        H3 route = new H3(booking.getTrip().getDepartureAddress() + " → " + booking.getTrip().getDestinationAddress());
        route.getStyle().set("margin", "0").set("font-size", "1.1em");
        
        HorizontalLayout details = new HorizontalLayout(
            new Icon(VaadinIcon.CALENDAR), new Paragraph(booking.getTrip().getDepartureTime().format(FORMATTER)),
            new Icon(VaadinIcon.USER), new Paragraph(booking.getTrip().getDriver().getName())
        );
        details.setAlignItems(FlexComponent.Alignment.CENTER);
        details.getStyle().set("font-size", "var(--lumo-font-size-s)");

        infoCard.add(route, details);
        content.add(warning, infoCard);
        add(content);

        Button keepButton = new Button("Non, garder", e -> close());
        keepButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button cancelButton = new Button("Oui, annuler", e -> {
            try {
                bookingService.cancelBooking(booking.getId());
                Notification.show("Réservation annulée.", 3000, Notification.Position.MIDDLE);
                if (onSuccess != null) onSuccess.run();
                close();
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        getFooter().add(keepButton, cancelButton);
    }
}