package com.example.covoiturage_vaadin.ui.view;

import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.application.services.TripService;
import com.example.covoiturage_vaadin.domain.model.Student;
import com.example.covoiturage_vaadin.ui.component.LogoutButton;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.example.covoiturage_vaadin.ui.component.MainLayout; // Import du layout
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.util.List;

@Route(value = "proposer-trajet", layout = MainLayout.class)
@PageTitle("Proposer un trajet - Covoiturage")
@PermitAll
public class TripCreationView extends VerticalLayout {

    private final TripService tripService;
    private final StudentService studentService;

    // Vaadin injecte automatiquement les services
    public TripCreationView(TripService tripService, StudentService studentService) {
        this.tripService = tripService;
        this.studentService = studentService;

        // --- CHAMPS DE FORMULAIRE ---
        
        // Simuler le conducteur (dans un vrai cas, ce serait l'étudiant connecté via SSO [cite: 18, 29])
        ComboBox<Student> driverSelect = new ComboBox<>("Conducteur (pour test)");
        List<Student> students = studentService.getAllStudents();
        driverSelect.setItems(students);
        driverSelect.setItemLabelGenerator(student -> student.getName() + " (" + student.getEmail() + ")");
        
        TextField departureField = new TextField("Point de départ");
        TextField destinationField = new TextField("Destination");
        DateTimePicker timePicker = new DateTimePicker("Date et heure du départ");
        IntegerField seatsField = new IntegerField("Nombre de places offertes");
        seatsField.setMin(1);
        seatsField.setValue(1);

        Button proposeButton = new Button("Proposer le Trajet");
        
        proposeButton.addClickListener(e -> {
            // Logique de validation rapide
            if (driverSelect.getValue() == null || departureField.isEmpty() || destinationField.isEmpty() || timePicker.isEmpty()) {
                Notification.show("Veuillez remplir tous les champs obligatoires.", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            Long driverId = driverSelect.getValue().getId();
            LocalDateTime departureTime = timePicker.getValue();
            int seats = seatsField.getValue();

            try {
                // APPEL AU CAS D'USAGE (COUCHE APPLICATION)
                tripService.proposeTrip(
                    driverId, 
                    departureField.getValue(), 
                    destinationField.getValue(), 
                    departureTime, 
                    seats, 
                    false // Pour l'instant on met 'false' pour "trajet ponctuel"
                );
                Notification.show("Trajet proposé avec succès !", 3000, Notification.Position.MIDDLE);
                
                // Réinitialiser le formulaire
                departureField.clear();
                destinationField.clear();
                timePicker.clear();

            } catch (IllegalArgumentException ex) {
                Notification.show("Erreur: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        add(driverSelect, departureField, destinationField, timePicker, seatsField, proposeButton);
    }
}