package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.domain.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// L'interface Spring Data JPA
public interface TripJpaRepository extends JpaRepository<Trip, Long> {
    
    // Requête générée automatiquement par Spring Data pour le besoin de recherche
    List<Trip> findByDestinationAddressContainingIgnoreCase(String destinationAddress);

    // Trouver les trajets par conducteur (pour le système de messagerie)
    List<Trip> findByDriverId(Long driverId);
}