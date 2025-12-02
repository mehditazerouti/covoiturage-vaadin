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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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
    
    // Champs de recherche
    private final TextField destinationSearchField = new TextField();
    private final DateTimePicker dateFilter = new DateTimePicker();
    private final IntegerField seatsFilter = new IntegerField();
    private final Select<String> typeFilter = new Select<>();

    public TripSearchView(TripService tripService, BookingService bookingService) {
        this.tripService = tripService;
        this.bookingService = bookingService;

        setSizeFull();
        setPadding(false); 
        setSpacing(false);
        
        // AJOUT 1 : Fond global légèrement gris pour faire ressortir la carte blanche
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // --- SECTION 1 : HERO (Zone de recherche colorée) ---
        VerticalLayout heroSection = new VerticalLayout();
        heroSection.setWidthFull();
        heroSection.setPadding(true);
        heroSection.setSpacing(true);
        heroSection.setAlignItems(Alignment.CENTER);
        
        // Fond Primaire avec une légère ombre
        heroSection.getStyle()
            .set("background", "var(--lumo-primary-color)")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
            .set("padding-bottom", "50px") // Un peu plus d'espace pour que la capsule respire
            .set("padding-top", "40px");

        H2 title = new H2("Où voulez-vous aller ?");
        title.getStyle()
            .set("color", "white")
            .set("font-size", "2rem")
            .set("margin-bottom", "var(--lumo-space-m)");

        configureSearchFields();
        HorizontalLayout searchBarContainer = createSearchLayout();
        
        heroSection.add(title, searchBarContainer);

        // --- SECTION 2 : CONTENU GRILLE ---
        VerticalLayout gridSection = new VerticalLayout();
        gridSection.setSizeFull();
        gridSection.setPadding(true);
        gridSection.setMaxWidth("1200px"); // Centré sur grand écran
        gridSection.setAlignSelf(Alignment.CENTER, gridSection);
        
        // Titre discret pour les résultats
        Span resultsLabel = new Span("Trajets disponibles");
        resultsLabel.getStyle()
            .set("font-weight", "bold")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin-bottom", "var(--lumo-space-m)");

        // AJOUT 2 : LE CONTENEUR "CARTE" POUR LA GRILLE
        VerticalLayout gridCard = new VerticalLayout();
        gridCard.setSizeFull();
        gridCard.setPadding(false); // Pas de padding interne pour que la grille touche les bords
        gridCard.setSpacing(false);
        
        // Styles CSS pour l'effet "Clean Card"
        gridCard.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")          // Coins bien arrondis
            .set("box-shadow", "0 10px 40px rgba(0,0,0,0.06)") // Ombre douce, large et diffuse
            .set("overflow", "hidden");            // Important : coupe la grille aux coins arrondis
        
        configureGrid();
        
        // Important : Enlever les bordures par défaut de la grille pour qu'elle fusionne avec la carte
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle().set("border", "none");

        gridCard.add(grid);

        gridSection.add(resultsLabel, gridCard);

        // Initialisation des données
        updateList();

        add(heroSection, gridSection);
    }
    
    private void configureSearchFields() {
        destinationSearchField.setPlaceholder("Destination...");
        destinationSearchField.setClearButtonVisible(true);
        destinationSearchField.setWidth("200px");
        destinationSearchField.addValueChangeListener(e -> updateList());

        dateFilter.setDatePlaceholder("Date min");
        dateFilter.setTimePlaceholder("Heure");
        dateFilter.setWidth("240px");
        dateFilter.addValueChangeListener(e -> updateList());

        seatsFilter.setPlaceholder("Places");
        seatsFilter.setMin(1);
        seatsFilter.setMax(10);
        seatsFilter.setWidth("100px");
        seatsFilter.addValueChangeListener(e -> updateList());

        typeFilter.setItems("Tous", "Réguliers", "Ponctuels");
        typeFilter.setValue("Tous");
        typeFilter.setWidth("140px");
        typeFilter.addValueChangeListener(e -> updateList());
    }

    private HorizontalLayout createSearchLayout() {
        Button searchButton = new Button("Rechercher", VaadinIcon.SEARCH.create());
        searchButton.getStyle().set("cursor", "pointer");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> updateList());

        HorizontalLayout layout = new HorizontalLayout(
            destinationSearchField,
            dateFilter,
            seatsFilter,
            typeFilter,
            searchButton
        );
        
        layout.addClassName("search-capsule");
        layout.setAlignItems(Alignment.BASELINE);
        layout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        layout.setPadding(true);
        layout.setSpacing(true);
        
        // Style "Capsule"
        layout.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 6px 12px rgba(0,0,0,0.15)");

        return layout;
    }

    private void configureGrid() {
        grid.removeAllColumns();
        
        grid.addColumn(trip -> trip.getDriver().getName())
            .setHeader("Conducteur")
            .setAutoWidth(true)
            .setFlexGrow(1);

        grid.addColumn(TripDTO::getDepartureAddress)
            .setHeader("Départ")
            .setAutoWidth(true)
            .setFlexGrow(1);

        grid.addColumn(TripDTO::getDestinationAddress)
            .setHeader("Destination")
            .setAutoWidth(true)
            .setFlexGrow(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm");
        grid.addColumn(trip -> trip.getDepartureTime().format(formatter))
            .setHeader("Date")
            .setAutoWidth(true);

        grid.addColumn(trip -> trip.getAvailableSeats() + " pl.")
            .setHeader("Dispo")
            .setAutoWidth(true);

        grid.addComponentColumn(trip -> new TripTypeBadge(trip.isRegular()))
            .setHeader("Type")
            .setAutoWidth(true);

        grid.addComponentColumn(trip -> {
            Button reserveBtn = new Button("Réserver");
            reserveBtn.getStyle().set("cursor", "pointer");
            reserveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            reserveBtn.addClickListener(e -> {
                TripBookingDialog dialog = new TripBookingDialog(trip, bookingService, this::updateList);
                dialog.open();
            });
            return reserveBtn;
        }).setHeader("Action");
        
        grid.addComponentColumn(trip -> {
             if (tripService.canEditTrip(trip.getId())) {
                Button editBtn = new Button(VaadinIcon.PENCIL.create());
                editBtn.getStyle().set("cursor", "pointer");
                editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                editBtn.addClickListener(e -> {
                    TripEditDialog dialog = new TripEditDialog(tripService, trip, this::updateList);
                    dialog.open();
                });
                return editBtn;
            }
            return new Span();
        }).setHeader("");
    }
    
    private void updateList() {
        String destination = destinationSearchField.getValue();
        java.time.LocalDateTime minDate = dateFilter.getValue();
        Integer minSeats = seatsFilter.getValue();
        Boolean isRegular = null;
        if ("Réguliers".equals(typeFilter.getValue())) isRegular = true;
        else if ("Ponctuels".equals(typeFilter.getValue())) isRegular = false;

        grid.setItems(tripService.searchTripsAdvanced(destination, minDate, minSeats, isRegular));
    }
}