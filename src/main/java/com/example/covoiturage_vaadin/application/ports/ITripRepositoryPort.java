package com.example.covoiturage_vaadin.application.ports;

import com.example.covoiturage_vaadin.domain.model.Trip;
import java.util.List;
import java.util.Optional;

// Définit les méthodes nécessaires pour la couche Application
public interface ITripRepositoryPort {
    Trip save(Trip trip);
    Optional<Trip> findById(Long id);
    List<Trip> findAll();
    
    // Méthode clé pour le cahier des charges
    List<Trip> findTripsByDestination(String destination); // Rechercher un trajet par adresse [cite: 21]
}