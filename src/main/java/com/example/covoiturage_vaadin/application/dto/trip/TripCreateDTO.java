package com.example.covoiturage_vaadin.application.dto.trip;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
 *
 * Validations JSR-303 :
 * - @NotBlank : Champ texte obligatoire
 * - @NotNull : Champ non-texte obligatoire
 * - @Size : Longueur min/max pour les adresses
 * - @Min/@Max : Valeurs numériques (places)
 * - @Future : Date dans le futur
 */
public class TripCreateDTO {

    @NotBlank(message = "L'adresse de départ est obligatoire")
    @Size(min = 2, max = 200, message = "L'adresse de départ doit contenir entre 2 et 200 caractères")
    private String departureAddress;

    @NotBlank(message = "L'adresse de destination est obligatoire")
    @Size(min = 2, max = 200, message = "L'adresse de destination doit contenir entre 2 et 200 caractères")
    private String destinationAddress;

    @NotNull(message = "La date et heure de départ sont obligatoires")
    @Future(message = "La date de départ doit être dans le futur")
    private LocalDateTime departureTime;

    @Min(value = 1, message = "Le nombre de places doit être au minimum 1")
    @Max(value = 8, message = "Le nombre de places ne peut pas dépasser 8")
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
