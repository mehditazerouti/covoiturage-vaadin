package com.example.covoiturage_vaadin.application.ports;

import com.example.covoiturage_vaadin.domain.model.Booking;
import java.util.List;
import java.util.Optional;

// Définit les méthodes nécessaires pour la couche Application
public interface IBookingRepositoryPort {
    Booking save(Booking booking);
    Optional<Booking> findById(Long id);
    List<Booking> findAll();
    List<Booking> findByStudentId(Long studentId);
    List<Booking> findByTripId(Long tripId);
    void deleteById(Long id);
    boolean existsByTripIdAndStudentId(Long tripId, Long studentId);
    boolean existsActiveBookingByTripIdAndStudentId(Long tripId, Long studentId);
}
