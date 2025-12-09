package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.application.ports.ITripRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Trip;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class TripRepositoryAdapter implements ITripRepositoryPort {

    private final TripJpaRepository jpaRepository;

    public TripRepositoryAdapter(TripJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Trip save(Trip trip) {
        return jpaRepository.save(trip);
    }

    @Override
    public Optional<Trip> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Trip> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Trip> findTripsByDestination(String destination) {
        // Utilisation de la méthode spécifique de l'interface JPA
        return jpaRepository.findByDestinationAddressContainingIgnoreCase(destination);
    }

    @Override
    public List<Trip> findByDriverId(Long driverId) {
        return jpaRepository.findByDriverId(driverId);
    }
}