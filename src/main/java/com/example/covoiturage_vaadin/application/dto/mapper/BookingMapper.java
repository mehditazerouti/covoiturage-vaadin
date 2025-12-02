package com.example.covoiturage_vaadin.application.dto.mapper;

import com.example.covoiturage_vaadin.application.dto.booking.BookingDTO;
import com.example.covoiturage_vaadin.domain.model.Booking;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir Booking Entity → DTO.
 *
 * Ce mapper centralise toutes les conversions entre les entités Booking
 * et leurs représentations DTO.
 *
 * Dépendances :
 * - TripMapper (pour convertir le trip)
 * - StudentMapper (pour convertir l'étudiant)
 *
 * Utilisé par :
 * - BookingService (pour retourner des DTO)
 *
 * Note :
 * - Il n'y a pas de BookingCreateDTO car la création se fait avec tripId uniquement
 * - L'étudiant est récupéré automatiquement via SecurityContext
 */
@Component
public class BookingMapper {

    private final TripMapper tripMapper;
    private final StudentMapper studentMapper;

    public BookingMapper(TripMapper tripMapper, StudentMapper studentMapper) {
        this.tripMapper = tripMapper;
        this.studentMapper = studentMapper;
    }

    /**
     * Convertit une entité Booking en BookingDTO.
     * Le trip est converti en TripDTO (avec driver simplifié).
     * L'étudiant est converti en StudentListDTO.
     *
     * Structure résultante :
     * BookingDTO → TripDTO → StudentListDTO (driver)
     *           → StudentListDTO (student)
     *
     * @param booking L'entité Booking à convertir
     * @return BookingDTO ou null si booking est null
     */
    public BookingDTO toDTO(Booking booking) {
        if (booking == null) {
            return null;
        }

        return new BookingDTO(
            booking.getId(),
            tripMapper.toDTO(booking.getTrip()),           // ⚠️ Conversion du trip complet
            studentMapper.toListDTO(booking.getStudent()),  // ⚠️ Conversion de l'étudiant (minimal)
            booking.getBookedAt(),
            booking.getStatus()
        );
    }
}
