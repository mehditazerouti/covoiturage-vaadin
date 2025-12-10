package com.example.covoiturage_vaadin.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant un trajet de covoiturage.
 *
 * Validations JSR-303 appliquées sur les champs pour garantir
 * l'intégrité des données au niveau de la couche domaine.
 */
@Entity
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'adresse de destination est obligatoire")
    @Size(min = 2, max = 200, message = "L'adresse de destination doit contenir entre 2 et 200 caractères")
    @Column(nullable = false)
    private String destinationAddress;

    @NotNull(message = "La date et heure de départ sont obligatoires")
    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Min(value = 1, message = "Le nombre total de places doit être au minimum 1")
    @Max(value = 8, message = "Le nombre total de places ne peut pas dépasser 8")
    private int totalSeats;

    @Min(value = 0, message = "Le nombre de places disponibles ne peut pas être négatif")
    private int availableSeats; // Le nombre de places restantes

    private boolean isRegular; // Pour distinguer les trajets réguliers/ponctuels
    
    // Constructeur vide (obligatoire pour JPA)
    public Trip() {
    }

    // Le conducteur du trajet (relation ManyToOne vers l'entité Student)
    // CASCADE : Quand un étudiant est supprimé, tous ses trajets sont supprimés automatiquement
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student driver;

    @NotBlank(message = "L'adresse de départ est obligatoire")
    @Size(min = 2, max = 200, message = "L'adresse de départ doit contenir entre 2 et 200 caractères")
    @Column(nullable = false)
    private String departureAddress;
    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }

    public String getDepartureAddress() {
        return departureAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public Student getDriver() {
        return driver;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public boolean isRegular() {
        return isRegular;
    }

     public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }
    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
    public void setRegular(boolean isRegular) {
        this.isRegular = isRegular;
    }
    public void setDriver(Student driver) {
        this.driver = driver;
    }
    public void setDepartureAddress(String departureAddress) {
        this.departureAddress = departureAddress;
    }
    


    
    // Méthodes métier :
    public boolean bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            return true;
        }
        return false;
    }
}