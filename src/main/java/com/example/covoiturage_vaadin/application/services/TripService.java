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
    private final SecurityContextService securityContext;

    public TripService(ITripRepositoryPort tripRepository, StudentService studentService, SecurityContextService securityContext) {
        this.tripRepository = tripRepository;
        this.studentService = studentService;
        this.securityContext = securityContext;
    }

    // Cas d'usage : Proposer un trajet
    // Le conducteur est automatiquement assigné depuis le SecurityContext
    @Transactional
    public Trip proposeTrip(String departure, String destination, LocalDateTime time, int seats, boolean isRegular) {
        // 1. Récupérer le conducteur depuis SecurityContext
        String username = securityContext.getCurrentUsername()
            .orElseThrow(() -> new IllegalStateException("Aucun utilisateur authentifié"));

        Student driver = studentService.getStudentByUsername(username)
            .orElseThrow(() -> new IllegalStateException("Étudiant non trouvé"));

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

    // Cas d'usage : Mettre à jour un trajet existant
    @Transactional
    public Trip updateTrip(Long tripId, String departure, String destination, LocalDateTime time, int totalSeats) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new IllegalArgumentException("Trajet non trouvé"));

        // Vérifier les permissions (conducteur ou admin)
        String currentUsername = securityContext.getCurrentUsername()
            .orElseThrow(() -> new IllegalStateException("Aucun utilisateur authentifié"));

        Student currentUser = studentService.getStudentByUsername(currentUsername)
            .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));

        boolean isDriver = trip.getDriver().getId().equals(currentUser.getId());
        boolean isAdmin = securityContext.hasRole("ADMIN");

        if (!isDriver && !isAdmin) {
            throw new IllegalStateException("Vous n'avez pas la permission de modifier ce trajet");
        }

        // Mise à jour des champs
        trip.setDepartureAddress(departure);
        trip.setDestinationAddress(destination);
        trip.setDepartureTime(time);

        // Mise à jour du nombre de places avec validation
        int currentAvailable = trip.getAvailableSeats();
        int currentTotal = trip.getTotalSeats();
        int bookedSeats = currentTotal - currentAvailable;

        if (totalSeats < bookedSeats) {
            throw new IllegalArgumentException("Impossible de réduire le nombre de places en dessous du nombre de réservations (" + bookedSeats + ")");
        }

        trip.setTotalSeats(totalSeats);
        trip.setAvailableSeats(totalSeats - bookedSeats);

        return tripRepository.save(trip);
    }

    // Cas d'usage : Supprimer un trajet
    @Transactional
    public void deleteTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new IllegalArgumentException("Trajet non trouvé"));

        // Vérifier les permissions (conducteur ou admin)
        String currentUsername = securityContext.getCurrentUsername()
            .orElseThrow(() -> new IllegalStateException("Aucun utilisateur authentifié"));

        Student currentUser = studentService.getStudentByUsername(currentUsername)
            .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));

        boolean isDriver = trip.getDriver().getId().equals(currentUser.getId());
        boolean isAdmin = securityContext.hasRole("ADMIN");

        if (!isDriver && !isAdmin) {
            throw new IllegalStateException("Vous n'avez pas la permission de supprimer ce trajet");
        }

        tripRepository.deleteById(tripId);
    }

    // Vérifier si l'utilisateur peut modifier un trajet
    @Transactional(readOnly = true)
    public boolean canEditTrip(Long tripId) {
        return tripRepository.findById(tripId)
            .map(trip -> {
                String currentUsername = securityContext.getCurrentUsername().orElse(null);
                if (currentUsername == null) {
                    return false;
                }

                return studentService.getStudentByUsername(currentUsername)
                    .map(currentUser -> {
                        boolean isDriver = trip.getDriver().getId().equals(currentUser.getId());
                        boolean isAdmin = securityContext.hasRole("ADMIN");
                        return isDriver || isAdmin;
                    })
                    .orElse(false);
            })
            .orElse(false);
    }
}