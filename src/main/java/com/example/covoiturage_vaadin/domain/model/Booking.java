package com.example.covoiturage_vaadin.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Trip trip;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    private LocalDateTime bookedAt;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    // Constructeur vide (obligatoire pour JPA)
    public Booking() {
    }

    // Constructeur avec paramètres
    public Booking(Trip trip, Student student) {
        this.trip = trip;
        this.student = student;
        this.bookedAt = LocalDateTime.now();
        this.status = BookingStatus.CONFIRMED; // Par défaut, confirmée immédiatement
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
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

    // Méthode métier : Annuler la réservation
    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    // Méthode métier : Vérifier si la réservation est active
    public boolean isActive() {
        return this.status == BookingStatus.CONFIRMED || this.status == BookingStatus.PENDING;
    }
}
