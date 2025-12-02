package com.example.covoiturage_vaadin.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity // Indique à JPA que c'est une table en base
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String destinationAddress;
    private LocalDateTime departureTime;
    private int totalSeats;
    private int availableSeats; // Le nombre de places restantes
    private boolean isRegular; // Pour distinguer les trajets réguliers/ponctuels [cite: 5]
    
    // Constructeur vide (obligatoire pour JPA)
    public Trip() {
    }

    // Le conducteur du trajet (relation ManyToOne vers l'entité Student)
    // CASCADE : Quand un étudiant est supprimé, tous ses trajets sont supprimés automatiquement
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student driver; 

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