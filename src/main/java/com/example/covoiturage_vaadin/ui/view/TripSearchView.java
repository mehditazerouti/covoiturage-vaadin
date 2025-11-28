package com.example.covoiturage_vaadin.ui.view;

import com.example.covoiturage_vaadin.application.services.BookingService;
import com.example.covoiturage_vaadin.application.services.TripService;
import com.example.covoiturage_vaadin.domain.model.Trip;
import com.example.covoiturage_vaadin.ui.component.TripEditDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.router.PageTitle;
import java.time.format.DateTimeFormatter;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Rechercher un trajet - Covoiturage")
@PermitAll
public class TripSearchView extends VerticalLayout {

    private final TripService tripService;
    private final BookingService bookingService;
    private final Grid<Trip> grid = new Grid<>(Trip.class);
    private final TextField destinationSearchField = new TextField();

    // Les Services sont injectés automatiquement par Spring
    public TripSearchView(TripService tripService, BookingService bookingService) {
        this.tripService = tripService;
        this.bookingService = bookingService;

        H2 title = new H2("Rechercher un trajet");
        
        // Configuration de la grille
        configureGrid();

        // Configuration du formulaire de recherche
        configureSearchForm();
        
        // Afficher tous les trajets au démarrage (ou aucun, selon préférence)
        updateList(); 

        // Disposition
        add(title, new HorizontalLayout(destinationSearchField, createSearchButton()), grid);
    }
    
    private void configureSearchForm() {
        destinationSearchField.setPlaceholder("Entrez la destination...");
        destinationSearchField.setClearButtonVisible(true);
        // Ajoute un écouteur pour déclencher la recherche lorsque l'utilisateur tape ENTER
        destinationSearchField.addValueChangeListener(e -> updateList());
    }
    
    private Button createSearchButton() {
        Button searchButton = new Button("Rechercher");
        // Ajoute un écouteur pour déclencher la recherche sur clic
        searchButton.addClickListener(e -> updateList());
        return searchButton;
    }

    private void configureGrid() {
        // Définir les colonnes à afficher
        grid.removeAllColumns();
        
        // Colonne pour le conducteur (récupère le nom de l'étudiant via la relation)
        grid.addColumn(trip -> trip.getDriver().getName())
            .setHeader("Conducteur");
            
        grid.addColumn(Trip::getDepartureAddress)
            .setHeader("Départ");
            
        grid.addColumn(Trip::getDestinationAddress)
            .setHeader("Destination");

        // Formatage de la date et heure (avec un motif français)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        grid.addColumn(trip -> trip.getDepartureTime().format(formatter))
            .setHeader("Date & Heure");
            
        grid.addColumn(Trip::getAvailableSeats)
            .setHeader("Places restantes");

        // Colonne pour "Réserver"
        grid.addComponentColumn(trip -> {
            Button reserveBtn = new Button("Réserver");
            reserveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            reserveBtn.addClickListener(e -> {
                try {
                    bookingService.createBooking(trip.getId());
                    Notification.show("Réservation effectuée avec succès !", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    updateList(); // Rafraîchir la liste pour mettre à jour les places disponibles
                } catch (IllegalStateException | IllegalArgumentException ex) {
                    Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            return reserveBtn;
        }).setHeader("Réserver");

        // Colonne pour "Modifier" (visible uniquement pour le conducteur ou l'admin)
        grid.addComponentColumn(trip -> {
            if (tripService.canEditTrip(trip.getId())) {
                Button editBtn = new Button("Modifier");
                editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
                editBtn.addClickListener(e -> {
                    TripEditDialog dialog = new TripEditDialog(tripService, trip, this::updateList);
                    dialog.open();
                });
                return editBtn;
            }
            // Afficher un texte grisé si pas de permission
            Span noAction = new Span("—");
            noAction.getStyle().set("color", "var(--lumo-disabled-text-color)");
            noAction.getStyle().set("font-style", "italic");
            return noAction;
        }).setHeader("Actions");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }
    
    private void updateList() {
        String filterText = destinationSearchField.getValue();
        
        if (filterText != null && !filterText.trim().isEmpty()) {
            // APPEL AU CAS D'USAGE (COUCHE APPLICATION)
            grid.setItems(tripService.searchTrips(filterText));
        } else {
            // Afficher tous les trajets si le champ est vide
            grid.setItems(tripService.findAllTrips());
        }
    }
}