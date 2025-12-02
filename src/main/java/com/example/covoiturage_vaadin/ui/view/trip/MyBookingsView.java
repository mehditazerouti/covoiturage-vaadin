package com.example.covoiturage_vaadin.ui.view.trip;

import com.example.covoiturage_vaadin.application.dto.booking.BookingDTO;
import com.example.covoiturage_vaadin.application.services.BookingService;
import com.example.covoiturage_vaadin.domain.model.BookingStatus;
import com.example.covoiturage_vaadin.ui.component.dialog.BookingCancelDialog;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.example.covoiturage_vaadin.ui.component.badge.StatusBadge;
import com.example.covoiturage_vaadin.ui.component.badge.TripTypeBadge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        // Fond gris global
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // --- 1. HEADER (Détaché) ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
            .set("background", "white")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");

        H2 title = new H2("Mes réservations");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem");
        
        header.add(title);

        // --- 2. CONTENU PRINCIPAL ---
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setMaxWidth("1200px"); // Centré sur grand écran
        mainContent.setAlignSelf(Alignment.CENTER, mainContent);

        // --- 3. CARTE DE GRILLE ---
        VerticalLayout gridCard = new VerticalLayout();
        gridCard.setSizeFull();
        gridCard.setPadding(false);
        gridCard.setSpacing(false);
        
        // Style Clean Card
        gridCard.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "0 10px 40px rgba(0,0,0,0.06)")
            .set("overflow", "hidden"); // Coins arrondis pour la grille

        configureGrid();
        
        // Enlever les bordures par défaut pour fusionner avec la carte
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle().set("border", "none");

        gridCard.add(grid);

        mainContent.add(gridCard);
        
        add(header, mainContent);

        // Charger les données
        updateList();
    }

    private void configureGrid() {
        grid.removeAllColumns();

        // Colonne Trajet
        grid.addColumn(booking ->
            booking.getTrip().getDepartureAddress() + " → " + booking.getTrip().getDestinationAddress()
        ).setHeader("Trajet").setAutoWidth(true).setFlexGrow(1);

        // Colonne Date & Heure
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm");
        grid.addColumn(booking ->
            booking.getTrip().getDepartureTime().format(formatter)
        ).setHeader("Départ").setAutoWidth(true);

        // Colonne Conducteur
        grid.addColumn(booking ->
            booking.getTrip().getDriver().getName()
        ).setHeader("Conducteur").setAutoWidth(true);

        // Colonne Type
        grid.addComponentColumn(booking ->
            new TripTypeBadge(booking.getTrip().isRegular())
        ).setHeader("Type").setAutoWidth(true);

        // Colonne Statut
        grid.addComponentColumn(booking ->
            new StatusBadge(booking.getStatus())
        ).setHeader("Statut").setAutoWidth(true);

        // Colonne Actions
        grid.addComponentColumn(booking -> {
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING) {
                Button cancelBtn = new Button("Annuler");
                cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                cancelBtn.addClickListener(e -> {
                    BookingCancelDialog dialog = new BookingCancelDialog(booking, bookingService, this::updateList);
                    dialog.open();
                });
                return cancelBtn;
            }
            Span cancelled = new Span("—");
            cancelled.getStyle().set("color", "var(--lumo-disabled-text-color)");
            return cancelled;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void updateList() {
        grid.setItems(bookingService.getMyBookings());
    }
}