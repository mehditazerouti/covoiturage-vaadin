package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.ports.ITripRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Trip;
import com.example.covoiturage_vaadin.domain.model.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- NOUVEL IMPORT
import java.util.List;
import java.time.LocalDateTime;

@Service
public class TripService {

    private final ITripRepositoryPort tripRepository;
    private final StudentService studentService;

    public TripService(ITripRepositoryPort tripRepository, StudentService studentService) {
        this.tripRepository = tripRepository;
        this.studentService = studentService;
    }

    // Cas d'usage : Proposer un trajet
    // @Transactional n'est pas strictement nécessaire ici si proposeTrip ne fait que save
    // mais il est ajouté pour s'assurer que l'entité Driver est chargée si la méthode avait plus de logique.
    @Transactional
    public Trip proposeTrip(Long driverId, String departure, String destination, LocalDateTime time, int seats, boolean isRegular) {
        // 1. Récupérer l'entité Driver (logique métier/coordination)
        Student driver = studentService.getStudentById(driverId)
             .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        // 2. Créer l'entité Trip
        Trip newTrip = new Trip();
        newTrip.setDriver(driver);
        newTrip.setDepartureAddress(departure);
        newTrip.setDestinationAddress(destination);
        newTrip.setDepartureTime(time);
        newTrip.setTotalSeats(seats);
        newTrip.setAvailableSeats(seats);
        newTrip.setRegular(isRegular);

        // 3. Persister via le Port
        return tripRepository.save(newTrip);
    }
    
    // Cas d'usage : Rechercher un trajet
    @Transactional(readOnly = true)
    public List<Trip> searchTrips(String destination) {
        // Logique de recherche (peut inclure des règles du domaine/rules ici)
        return tripRepository.findTripsByDestination(destination);
    }

    // Obtenir tous les trajets pour l'affichage initial
    @Transactional(readOnly = true)
    public List<Trip> findAllTrips() {
        return tripRepository.findAll();
    }
}