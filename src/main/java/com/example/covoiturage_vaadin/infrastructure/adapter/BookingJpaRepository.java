package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.domain.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingJpaRepository extends JpaRepository<Booking, Long> {

    // Trouver toutes les réservations d'un étudiant
    List<Booking> findByStudentId(Long studentId);

    // Trouver toutes les réservations d'un trajet
    List<Booking> findByTripId(Long tripId);

    // Vérifier si un étudiant a déjà réservé un trajet
    boolean existsByTripIdAndStudentId(Long tripId, Long studentId);

    // Vérifier si un étudiant a une réservation active (CONFIRMED ou PENDING) pour un trajet
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
           "WHERE b.trip.id = :tripId AND b.student.id = :studentId " +
           "AND (b.status = 'CONFIRMED' OR b.status = 'PENDING')")
    boolean existsActiveBookingByTripIdAndStudentId(@Param("tripId") Long tripId, @Param("studentId") Long studentId);
}
