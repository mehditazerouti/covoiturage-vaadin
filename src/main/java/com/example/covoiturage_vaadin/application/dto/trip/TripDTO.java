package com.example.covoiturage_vaadin.application.dto.trip;

import com.example.covoiturage_vaadin.application.dto.student.StudentListDTO;
import java.time.LocalDateTime;

/**
 * DTO pour afficher les détails d'un trajet.
 *
 * Utilisé pour :
 * - Afficher la liste des trajets dans TripSearchView
 * - Afficher les détails d'un trajet dans BookingDTO
 * - Formulaire d'édition d'un trajet (TripEditDialog)
 *
 * Particularités :
 * - Le conducteur est un StudentListDTO (pas l'entité complète)
 * - Évite les références circulaires et le EAGER loading excessif
 * - Contient toutes les informations nécessaires pour l'affichage
 */
public class TripDTO {
    private Long id;
    private String departureAddress;
    private String destinationAddress;
    private LocalDateTime departureTime;
    private int totalSeats;
    private int availableSeats;
    private boolean isRegular;
    private StudentListDTO driver;  // ⚠️ Pas l'entité Student complète !

    // Constructeur vide
    public TripDTO() {}

    // Constructeur complet
    public TripDTO(Long id, String departureAddress, String destinationAddress,
                   LocalDateTime departureTime, int totalSeats, int availableSeats,
                   boolean isRegular, StudentListDTO driver) {
        this.id = id;
        this.departureAddress = departureAddress;
        this.destinationAddress = destinationAddress;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.isRegular = isRegular;
        this.driver = driver;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartureAddress() {
        return departureAddress;
    }

    public void setDepartureAddress(String departureAddress) {
        this.departureAddress = departureAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public boolean isRegular() {
        return isRegular;
    }

    public void setRegular(boolean regular) {
        isRegular = regular;
    }

    public StudentListDTO getDriver() {
        return driver;
    }

    public void setDriver(StudentListDTO driver) {
        this.driver = driver;
    }
}
