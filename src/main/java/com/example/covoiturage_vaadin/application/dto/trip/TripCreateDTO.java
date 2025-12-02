package com.example.covoiturage_vaadin.application.dto.trip;

import java.time.LocalDateTime;

/**
 * DTO pour créer un nouveau trajet.
 *
 * Utilisé pour :
 * - Formulaire de création de trajet (TripCreationView)
 *
 * Particularités :
 * - Ne contient PAS le conducteur (auto-assigné via SecurityContext)
 * - Le service TripService récupère automatiquement l'utilisateur connecté
 * - availableSeats est initialisé automatiquement = totalSeats
 *
 * Note :
 * - Ce DTO ne doit JAMAIS être retourné par une API/Service
 * - Utilisé uniquement en INPUT (création)
 */
public class TripCreateDTO {
    private String departureAddress;
    private String destinationAddress;
    private LocalDateTime departureTime;
    private int totalSeats;
    private boolean isRegular;

    // Constructeur vide
    public TripCreateDTO() {}

    // Constructeur complet
    public TripCreateDTO(String departureAddress, String destinationAddress,
                         LocalDateTime departureTime, int totalSeats, boolean isRegular) {
        this.departureAddress = departureAddress;
        this.destinationAddress = destinationAddress;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.isRegular = isRegular;
    }

    // Getters et Setters
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

    public boolean isRegular() {
        return isRegular;
    }

    public void setRegular(boolean regular) {
        isRegular = regular;
    }
}
