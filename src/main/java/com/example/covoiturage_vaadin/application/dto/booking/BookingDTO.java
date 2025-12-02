package com.example.covoiturage_vaadin.application.dto.booking;

import com.example.covoiturage_vaadin.application.dto.student.StudentListDTO;
import com.example.covoiturage_vaadin.application.dto.trip.TripDTO;
import com.example.covoiturage_vaadin.domain.model.BookingStatus;
import java.time.LocalDateTime;

/**
 * DTO pour afficher une réservation avec détails complets.
 *
 * Utilisé pour :
 * - Afficher la liste des réservations dans MyBookingsView
 * - Afficher les détails d'une réservation dans les dialogs
 * - Afficher les réservations d'un trajet (pour le conducteur)
 *
 * Particularités :
 * - Le trajet est un TripDTO (contient le conducteur simplifié)
 * - L'étudiant est un StudentListDTO (version minimale)
 * - Évite complètement les références circulaires
 * - Structure : BookingDTO → TripDTO → StudentListDTO (driver)
 *                          → StudentListDTO (student)
 *
 * Avantages :
 * - Pas de EAGER loading sur les relations
 * - Pas de problème de sérialisation JSON
 * - Données optimisées pour l'affichage
 */
public class BookingDTO {
    private Long id;
    private TripDTO trip;
    private StudentListDTO student;
    private LocalDateTime bookedAt;
    private BookingStatus status;

    // Constructeur vide
    public BookingDTO() {}

    // Constructeur complet
    public BookingDTO(Long id, TripDTO trip, StudentListDTO student,
                      LocalDateTime bookedAt, BookingStatus status) {
        this.id = id;
        this.trip = trip;
        this.student = student;
        this.bookedAt = bookedAt;
        this.status = status;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TripDTO getTrip() {
        return trip;
    }

    public void setTrip(TripDTO trip) {
        this.trip = trip;
    }

    public StudentListDTO getStudent() {
        return student;
    }

    public void setStudent(StudentListDTO student) {
        this.student = student;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    // Méthodes utilitaires (similaires à l'entité)

    /**
     * Vérifie si la réservation est active (non annulée).
     * @return true si le statut est CONFIRMED ou PENDING
     */
    public boolean isActive() {
        return this.status == BookingStatus.CONFIRMED || this.status == BookingStatus.PENDING;
    }
}
