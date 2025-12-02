package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.trip.TripDTO;
import com.example.covoiturage_vaadin.application.services.BookingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;

public class TripBookingDialog extends Dialog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy à HH:mm");

    public TripBookingDialog(TripDTO trip, BookingService bookingService, Runnable onSuccess) {
        setHeaderTitle("Confirmer la réservation");
        setWidth("450px");

        // --- RÉCAPITULATIF DU TRAJET (Style Ticket) ---
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        // Trajet (Gros et Gras)
        HorizontalLayout routeLayout = new HorizontalLayout(
            new Icon(VaadinIcon.MAP_MARKER),
            new H3(trip.getDepartureAddress() + " → " + trip.getDestinationAddress())
        );
        routeLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        routeLayout.getThemeList().add("spacing-s");

        // Détails (Liste avec icônes)
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(true);
        details.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "12px");

        details.add(createDetailRow(VaadinIcon.CALENDAR, "Date", trip.getDepartureTime().format(FORMATTER)));
        details.add(createDetailRow(VaadinIcon.USER, "Conducteur", trip.getDriver().getName()));
        details.add(createDetailRow(VaadinIcon.CAR, "Places", trip.getAvailableSeats() + " disponibles"));
        
        if (trip.isRegular()) {
            Span regularBadge = new Span("Trajet Régulier");
            regularBadge.getElement().getThemeList().add("badge pill success");
            details.add(new Div(regularBadge)); // Div wrapper pour margin
        }

        content.add(routeLayout, details);
        add(content);

        // --- PIED DE PAGE ---
        Button cancelButton = new Button("Annuler", e -> close());
        
        Button confirmButton = new Button("Réserver", e -> {
            try {
                bookingService.createBooking(trip.getId());
                Notification.show("✅ Réservation réussie !", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                if (onSuccess != null) onSuccess.run();
                close();
            } catch (Exception ex) {
                Notification.show("❌ " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(cancelButton, confirmButton);
    }

    private HorizontalLayout createDetailRow(VaadinIcon icon, String label, String value) {
        Icon i = icon.create();
        i.setSize("16px");
        i.setColor("var(--lumo-secondary-text-color)");

        Span l = new Span(label + ":");
        l.getStyle().set("color", "var(--lumo-secondary-text-color)").set("width", "90px");

        Span v = new Span(value);
        v.getStyle().set("font-weight", "500");

        HorizontalLayout row = new HorizontalLayout(i, l, v);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        return row;
    }
}