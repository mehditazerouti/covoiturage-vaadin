package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.ports.IBookingRepositoryPort;
import com.example.covoiturage_vaadin.application.ports.ITripRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Booking;
import com.example.covoiturage_vaadin.domain.model.BookingStatus;
import com.example.covoiturage_vaadin.domain.model.Student;
import com.example.covoiturage_vaadin.domain.model.Trip;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final IBookingRepositoryPort bookingRepository;
    private final ITripRepositoryPort tripRepository;
    private final StudentService studentService;
    private final SecurityContextService securityContext;

    public BookingService(IBookingRepositoryPort bookingRepository,
                         ITripRepositoryPort tripRepository,
                         StudentService studentService,
                         SecurityContextService securityContext) {
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.studentService = studentService;
        this.securityContext = securityContext;
    }

    // Cas d'usage : Créer une réservation
    @Transactional
    public Booking createBooking(Long tripId) {
        // 1. Récupérer l'utilisateur connecté
        String username = securityContext.getCurrentUsername()
            .orElseThrow(() -> new IllegalStateException("Aucun utilisateur authentifié"));

        Student student = studentService.getStudentByUsername(username)
            .orElseThrow(() -> new IllegalStateException("Étudiant non trouvé"));

        // 2. Récupérer le trajet
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new IllegalArgumentException("Trajet non trouvé"));

        // 3. Vérifications métier
        // 3.1 Un étudiant ne peut pas réserver son propre trajet
        if (trip.getDriver().getId().equals(student.getId())) {
            throw new IllegalStateException("Vous ne pouvez pas réserver votre propre trajet");
        }

        // 3.2 Vérifier si l'étudiant a déjà une réservation active pour ce trajet
        if (bookingRepository.existsActiveBookingByTripIdAndStudentId(tripId, student.getId())) {
            throw new IllegalStateException("Vous avez déjà une réservation active pour ce trajet");
        }

        // 3.3 Vérifier qu'il reste des places disponibles
        if (trip.getAvailableSeats() <= 0) {
            throw new IllegalStateException("Il n'y a plus de places disponibles pour ce trajet");
        }

        // 4. Réserver une place (méthode métier de Trip)
        boolean seatBooked = trip.bookSeat();
        if (!seatBooked) {
            throw new IllegalStateException("Impossible de réserver une place");
        }

        // 5. Sauvegarder le trajet avec les places mises à jour
        tripRepository.save(trip);

        // 6. Créer la réservation
        Booking booking = new Booking(trip, student);
        return bookingRepository.save(booking);
    }

    // Cas d'usage : Annuler une réservation
    @Transactional
    public void cancelBooking(Long bookingId) {
        // 1. Récupérer la réservation
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        // 2. Vérifier que l'utilisateur connecté est bien celui qui a fait la réservation
        String username = securityContext.getCurrentUsername()
            .orElseThrow(() -> new IllegalStateException("Aucun utilisateur authentifié"));

        Student student = studentService.getStudentByUsername(username)
            .orElseThrow(() -> new IllegalStateException("Étudiant non trouvé"));

        boolean isOwner = booking.getStudent().getId().equals(student.getId());
        boolean isAdmin = securityContext.hasRole("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("Vous n'avez pas la permission d'annuler cette réservation");
        }

        // 3. Vérifier que la réservation n'est pas déjà annulée
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cette réservation est déjà annulée");
        }

        // 4. Libérer la place (re-incrémenter availableSeats)
        Trip trip = booking.getTrip();
        trip.setAvailableSeats(trip.getAvailableSeats() + 1);
        tripRepository.save(trip);

        // 5. Marquer la réservation comme annulée
        booking.cancel();
        bookingRepository.save(booking);
    }

    // Cas d'usage : Récupérer les réservations de l'utilisateur connecté
    public List<Booking> getMyBookings() {
        String username = securityContext.getCurrentUsername()
            .orElseThrow(() -> new IllegalStateException("Aucun utilisateur authentifié"));

        Student student = studentService.getStudentByUsername(username)
            .orElseThrow(() -> new IllegalStateException("Étudiant non trouvé"));

        return bookingRepository.findByStudentId(student.getId());
    }

    // Cas d'usage : Récupérer les réservations d'un trajet (pour le conducteur/admin)
    public List<Booking> getBookingsByTrip(Long tripId) {
        return bookingRepository.findByTripId(tripId);
    }

    // Récupérer toutes les réservations (admin)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}
