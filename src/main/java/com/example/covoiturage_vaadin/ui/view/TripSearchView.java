package com.example.covoiturage_vaadin.ui.view;

import com.example.covoiturage_vaadin.application.services.TripService;
import com.example.covoiturage_vaadin.domain.model.Trip;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.router.PageTitle;
import java.time.format.DateTimeFormatter;

@Route("rechercher-trajet")
@PageTitle("Rechercher un trajet")
@PermitAll
public class TripSearchView extends VerticalLayout {

    private final TripService tripService;
    private final Grid<Trip> grid = new Grid<>(Trip.class);
    private final TextField destinationSearchField = new TextField();

    // Le Service est injecté automatiquement par Spring
    public TripSearchView(TripService tripService) {
        this.tripService = tripService;
        
        // Configuration de la grille
        configureGrid();

        // Configuration du formulaire de recherche
        configureSearchForm();
        
        // Afficher tous les trajets au démarrage (ou aucun, selon préférence)
        updateList(); 

        // Disposition
        add(new HorizontalLayout(destinationSearchField, createSearchButton()), grid);
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
            
        // Ajoutez une colonne d'action pour "Réserver"
        grid.addComponentColumn(trip -> {
            Button reserveBtn = new Button("Réserver");
            reserveBtn.addClickListener(e -> {
                // Ici, vous ajouteriez la logique de réservation et de mise à jour des places
                // Par souci de simplicité, on n'ajoute qu'une notification pour l'instant
                com.vaadin.flow.component.notification.Notification.show(
                    "Fonctionnalité de réservation à implémenter pour le trajet n°" + trip.getId(), 
                    3000, 
                    com.vaadin.flow.component.notification.Notification.Position.MIDDLE
                );
            });
            return reserveBtn;
        }).setHeader("Action");
        
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