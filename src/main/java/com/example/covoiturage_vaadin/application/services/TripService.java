package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.dto.trip.TripDTO;
import com.example.covoiturage_vaadin.application.dto.mapper.TripMapper;
import com.example.covoiturage_vaadin.application.ports.ITripRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Trip;
import com.example.covoiturage_vaadin.domain.model.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class TripService {

    private final ITripRepositoryPort tripRepository;
    private final StudentService studentService;
    private final SecurityContextService securityContext;
    private final TripMapper tripMapper;

    public TripService(ITripRepositoryPort tripRepository,
                      StudentService studentService,
                      SecurityContextService securityContext,
                      TripMapper tripMapper) {
        this.tripRepository = tripRepository;
        this.studentService = studentService;
        this.securityContext = securityContext;
        this.tripMapper = tripMapper;
    }

    /**
     * Cas d'usage : Proposer un trajet.
     * Le conducteur est automatiquement assigné depuis le SecurityContext.
     * @return TripDTO du trajet créé
     */
    @Transactional
    public TripDTO proposeTrip(String departure, String destination, LocalDateTime time, int seats, boolean isRegular) {
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
        Trip savedTrip = tripRepository.save(newTrip);

        // 4. Convertir en DTO avant de retourner
        return tripMapper.toDTO(savedTrip);
    }
    
    /**
     * Cas d'usage : Rechercher un trajet par destination.
     * @return Liste de TripDTO
     */
    @Transactional(readOnly = true)
    public List<TripDTO> searchTrips(String destination) {
        return tripRepository.findTripsByDestination(destination).stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir tous les trajets pour l'affichage initial.
     * @return Liste de TripDTO
     */
    @Transactional(readOnly = true)
    public List<TripDTO> findAllTrips() {
        return tripRepository.findAll().stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cas d'usage : Recherche avancée avec filtres multiples.
     * @return Liste de TripDTO filtrée
     */
    @Transactional(readOnly = true)
    public List<TripDTO> searchTripsAdvanced(String destination, LocalDateTime minDate, Integer minSeats, Boolean isRegular) {
        List<Trip> trips = tripRepository.findAll();

        // Filtre par destination (insensible à la casse)
        if (destination != null && !destination.trim().isEmpty()) {
            String destLower = destination.toLowerCase();
            trips = trips.stream()
                .filter(trip -> trip.getDestinationAddress().toLowerCase().contains(destLower))
                .toList();
        }

        // Filtre par date minimum
        if (minDate != null) {
            trips = trips.stream()
                .filter(trip -> trip.getDepartureTime().isAfter(minDate) || trip.getDepartureTime().isEqual(minDate))
                .toList();
        }

        // Filtre par nombre de places minimum
        if (minSeats != null && minSeats > 0) {
            trips = trips.stream()
                .filter(trip -> trip.getAvailableSeats() >= minSeats)
                .toList();
        }

        // Filtre par type de trajet (régulier/ponctuel)
        if (isRegular != null) {
            trips = trips.stream()
                .filter(trip -> trip.isRegular() == isRegular)
                .toList();
        }

        // Convertir en DTO avant de retourner
        return trips.stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cas d'usage : Mettre à jour un trajet existant.
     * @return TripDTO du trajet mis à jour
     */
    @Transactional
    public TripDTO updateTrip(Long tripId, String departure, String destination, LocalDateTime time, int totalSeats) {
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

        Trip updatedTrip = tripRepository.save(trip);

        // Convertir en DTO avant de retourner
        return tripMapper.toDTO(updatedTrip);
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