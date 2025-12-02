package com.example.covoiturage_vaadin.ui.view;

import com.example.covoiturage_vaadin.application.dto.booking.BookingDTO;
import com.example.covoiturage_vaadin.application.services.BookingService;
import com.example.covoiturage_vaadin.domain.model.BookingStatus;
import com.example.covoiturage_vaadin.ui.component.BookingCancelDialog;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.example.covoiturage_vaadin.ui.component.StatusBadge;
import com.example.covoiturage_vaadin.ui.component.TripTypeBadge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;

@Route(value = "mes-reservations", layout = MainLayout.class)
@PageTitle("Mes réservations - Covoiturage")
@PermitAll
public class MyBookingsView extends VerticalLayout {

    private final BookingService bookingService;
    private final Grid<BookingDTO> grid = new Grid<>(BookingDTO.class);

    public MyBookingsView(BookingService bookingService) {
        this.bookingService = bookingService;

        H2 title = new H2("Mes réservations");

        // Configuration de la grille
        configureGrid();

        // Afficher les réservations au démarrage
        updateList();

        add(title, grid);
    }

    private void configureGrid() {
        grid.removeAllColumns();

        // Colonne pour le trajet (Départ → Destination)
        grid.addColumn(booking ->
            booking.getTrip().getDepartureAddress() + " → " + booking.getTrip().getDestinationAddress()
        ).setHeader("Trajet").setAutoWidth(true);

        // Colonne pour la date et heure
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        grid.addColumn(booking ->
            booking.getTrip().getDepartureTime().format(formatter)
        ).setHeader("Date & Heure").setAutoWidth(true);

        // Colonne pour le conducteur
        grid.addColumn(booking ->
            booking.getTrip().getDriver().getName()
        ).setHeader("Conducteur").setAutoWidth(true);

        // Colonne pour le nombre de places totales du trajet
        grid.addColumn(booking ->
            booking.getTrip().getAvailableSeats() + "/" + booking.getTrip().getTotalSeats() + " places"
        ).setHeader("Places dispo").setAutoWidth(true);

        // Colonne Type (Régulier/Ponctuel)
        grid.addComponentColumn(booking ->
            new TripTypeBadge(booking.getTrip().isRegular())
        ).setHeader("Type").setAutoWidth(true);

        // Colonne pour la date de réservation
        grid.addColumn(booking ->
            booking.getBookedAt().format(formatter)
        ).setHeader("Réservé le").setAutoWidth(true);

        // Colonne pour le statut
        grid.addComponentColumn(booking ->
            new StatusBadge(booking.getStatus())
        ).setHeader("Statut").setAutoWidth(true);

        // Colonne Actions
        grid.addComponentColumn(booking -> {
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING) {
                Button cancelBtn = new Button("Annuler");
                cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                cancelBtn.addClickListener(e -> {
                    BookingCancelDialog dialog = new BookingCancelDialog(booking, bookingService, this::updateList);
                    dialog.open();
                });
                return cancelBtn;
            }
            Span cancelled = new Span("—");
            cancelled.getStyle().set("color", "var(--lumo-disabled-text-color)");
            cancelled.getStyle().set("font-style", "italic");
            return cancelled;
        }).setHeader("Actions").setAutoWidth(true);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void updateList() {
        grid.setItems(bookingService.getMyBookings());
    }
}
