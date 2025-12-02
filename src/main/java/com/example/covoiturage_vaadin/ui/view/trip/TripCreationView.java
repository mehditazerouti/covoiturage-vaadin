package com.example.covoiturage_vaadin.ui.view.trip;

import com.example.covoiturage_vaadin.application.services.TripService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.Duration;

@Route(value = "proposer-trajet", layout = MainLayout.class)
@PageTitle("Proposer un trajet - Covoiturage")
@PermitAll
public class TripCreationView extends VerticalLayout {

    private final TripService tripService;
    
    // Champs
    private final TextField departureField = new TextField("Point de départ");
    private final TextField destinationField = new TextField("Destination");
    private final DateTimePicker timePicker = new DateTimePicker("Date et heure du départ");
    private final IntegerField seatsField = new IntegerField("Places disponibles");
    private final Checkbox regularCheckbox = new Checkbox("Trajet régulier (Hebdomadaire)");

    public TripCreationView(TripService tripService) {
        this.tripService = tripService;

        setSizeFull();
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        // Fond global gris
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // --- CARTE DU FORMULAIRE ---
        VerticalLayout formCard = new VerticalLayout();
        formCard.setMaxWidth("600px");
        formCard.setWidth("100%");
        formCard.setPadding(true);
        formCard.setSpacing(true);
        
        // Style Clean Card
        formCard.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "0 10px 40px rgba(0,0,0,0.06)")
            .set("padding", "var(--lumo-space-xl)");

        // Titre et Sous-titre
        H2 title = new H2("Proposer un trajet");
        title.getStyle().set("margin-top", "0").set("color", "var(--lumo-primary-text-color)");
        
        Paragraph subtitle = new Paragraph("Partagez votre route et réduisez vos frais.");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-bottom", "var(--lumo-space-l)");

        // Configuration des champs
        configureFields();
        
        // Layout du formulaire (2 colonnes)
        FormLayout formLayout = new FormLayout();
        formLayout.add(departureField, destinationField);
        formLayout.add(timePicker, 2); // Prend toute la largeur
        formLayout.add(seatsField, regularCheckbox);
        
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        // Bouton d'action
        Button proposeButton = new Button("Publier le trajet", VaadinIcon.PAPERPLANE.create()); // Petite erreur ici corrigée ci-dessous
        proposeButton.setIcon(VaadinIcon.PAPERPLANE.create());
        proposeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        proposeButton.setWidthFull();
        proposeButton.getStyle()
            .set("margin-top", "var(--lumo-space-m)")
            .set("cursor","pointer");
        
        proposeButton.addClickListener(e -> handleProposeTrip());

        formCard.add(title, subtitle, formLayout, proposeButton);
        add(formCard);
    }

    private void configureFields() {
        departureField.setPlaceholder("Ex: Paris, Porte Dauphine");
        departureField.setRequiredIndicatorVisible(true);
        
        destinationField.setPlaceholder("Ex: Versailles");
        destinationField.setRequiredIndicatorVisible(true);
        
        timePicker.setStep(Duration.ofMinutes(15));
        timePicker.setMin(LocalDateTime.now());
        
        seatsField.setMin(1);
        seatsField.setMax(8);
        seatsField.setValue(3);
        
        regularCheckbox.getStyle().set("margin-top", "var(--lumo-space-s)");
    }

    private void handleProposeTrip() {
        if (departureField.isEmpty() || destinationField.isEmpty() || timePicker.isEmpty()) {
            Notification.show("Veuillez remplir tous les champs obligatoires.", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            tripService.proposeTrip(
                departureField.getValue(),
                destinationField.getValue(),
                timePicker.getValue(),
                seatsField.getValue(),
                regularCheckbox.getValue()
            );

            Notification.show("Trajet publié avec succès !", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            clearForm();

        } catch (IllegalStateException ex) {
            Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception ex) {
            Notification.show("Erreur inattendue : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        departureField.clear();
        destinationField.clear();
        timePicker.clear();
        seatsField.setValue(3);
        regularCheckbox.setValue(false);
    }
}