package com.example.covoiturage_vaadin.ui.view.trip;

import com.example.covoiturage_vaadin.application.dto.trip.TripDTO;
import com.example.covoiturage_vaadin.application.services.BookingService;
import com.example.covoiturage_vaadin.application.services.TripService;
import com.example.covoiturage_vaadin.ui.component.dialog.TripBookingDialog;
import com.example.covoiturage_vaadin.ui.component.dialog.TripEditDialog;
import com.example.covoiturage_vaadin.ui.component.badge.TripTypeBadge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
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
    private final Grid<TripDTO> grid = new Grid<>(TripDTO.class);
    private final TextField destinationSearchField = new TextField();
    private final DateTimePicker dateFilter = new DateTimePicker();
    private final IntegerField seatsFilter = new IntegerField();
    private final Select<String> typeFilter = new Select<>();

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
        HorizontalLayout filters = createFiltersLayout();
        add(title, filters, grid);
    }
    
    private void configureSearchForm() {
        // Champ destination
        destinationSearchField.setPlaceholder("Destination...");
        destinationSearchField.setClearButtonVisible(true);
        destinationSearchField.setWidth("200px");
        destinationSearchField.addValueChangeListener(e -> updateList());

        // Filtre date/heure
        dateFilter.setLabel("À partir du");
        dateFilter.setDatePlaceholder("Date minimum");
        dateFilter.setTimePlaceholder("Heure");
        dateFilter.setWidth("220px");
        dateFilter.addValueChangeListener(e -> updateList());

        // Filtre places minimum
        seatsFilter.setLabel("Places min.");
        seatsFilter.setPlaceholder("Min");
        seatsFilter.setClearButtonVisible(true);
        seatsFilter.setMin(1);
        seatsFilter.setMax(10);
        seatsFilter.setWidth("120px");
        seatsFilter.addValueChangeListener(e -> updateList());

        // Filtre type de trajet
        typeFilter.setLabel("Type de trajet");
        typeFilter.setItems("Tous", "Réguliers", "Ponctuels");
        typeFilter.setValue("Tous");
        typeFilter.setWidth("150px");
        typeFilter.addValueChangeListener(e -> updateList());
    }

    private HorizontalLayout createFiltersLayout() {
        Button searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> updateList());

        HorizontalLayout layout = new HorizontalLayout(
            destinationSearchField,
            dateFilter,
            seatsFilter,
            typeFilter,
            searchButton
        );
        layout.setDefaultVerticalComponentAlignment(Alignment.END);
        layout.setWidthFull();
        return layout;
    }

    private void configureGrid() {
        // Définir les colonnes à afficher
        grid.removeAllColumns();
        
        // Colonne pour le conducteur (récupère le nom de l'étudiant via la relation)
        grid.addColumn(trip -> trip.getDriver().getName())
            .setHeader("Conducteur");

        grid.addColumn(TripDTO::getDepartureAddress)
            .setHeader("Départ");

        grid.addColumn(TripDTO::getDestinationAddress)
            .setHeader("Destination");

        // Formatage de la date et heure (avec un motif français)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        grid.addColumn(trip -> trip.getDepartureTime().format(formatter))
            .setHeader("Date & Heure");

        grid.addColumn(TripDTO::getAvailableSeats)
            .setHeader("Places restantes");

        // Colonne Type (Régulier/Ponctuel)
        grid.addComponentColumn(trip ->
            new TripTypeBadge(trip.isRegular())
        ).setHeader("Type").setAutoWidth(true);

        // Colonne pour "Réserver"
        grid.addComponentColumn(trip -> {
            Button reserveBtn = new Button("Réserver");
            reserveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            reserveBtn.addClickListener(e -> {
                TripBookingDialog dialog = new TripBookingDialog(trip, bookingService, this::updateList);
                dialog.open();
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
        // Récupérer les valeurs des filtres
        String destination = destinationSearchField.getValue();

        // Convertir LocalDateTime en LocalDateTime pour le filtre de date
        java.time.LocalDateTime minDate = dateFilter.getValue();

        Integer minSeats = seatsFilter.getValue();

        // Convertir la sélection du type en Boolean (null = tous)
        Boolean isRegular = null;
        String typeValue = typeFilter.getValue();
        if ("Réguliers".equals(typeValue)) {
            isRegular = true;
        } else if ("Ponctuels".equals(typeValue)) {
            isRegular = false;
        }

        // Appel à la recherche avancée
        grid.setItems(tripService.searchTripsAdvanced(destination, minDate, minSeats, isRegular));
    }
}