package com.example.covoiturage_vaadin.application.dto.mapper;

import com.example.covoiturage_vaadin.application.dto.trip.TripDTO;
import com.example.covoiturage_vaadin.application.dto.trip.TripCreateDTO;
import com.example.covoiturage_vaadin.domain.model.Trip;
import com.example.covoiturage_vaadin.domain.model.Student;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir Trip Entity ↔ DTO.
 *
 * Ce mapper centralise toutes les conversions entre les entités Trip
 * et leurs différentes représentations DTO.
 *
 * Dépendances :
 * - StudentMapper (pour convertir le driver)
 *
 * Utilisé par :
 * - TripService (pour retourner des DTO)
 * - BookingMapper (pour convertir le trip dans BookingDTO)
 */
@Component
public class TripMapper {

    private final StudentMapper studentMapper;

    public TripMapper(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    /**
     * Convertit une entité Trip en TripDTO.
     * Le driver est converti en StudentListDTO (pas l'entité complète).
     *
     * @param trip L'entité Trip à convertir
     * @return TripDTO ou null si trip est null
     */
    public TripDTO toDTO(Trip trip) {
        if (trip == null) {
            return null;
        }

        return new TripDTO(
            trip.getId(),
            trip.getDepartureAddress(),
            trip.getDestinationAddress(),
            trip.getDepartureTime(),
            trip.getTotalSeats(),
            trip.getAvailableSeats(),
            trip.isRegular(),
            studentMapper.toListDTO(trip.getDriver())  // ⚠️ Conversion du driver
        );
    }

    /**
     * Convertit un TripCreateDTO en entité Trip.
     * ⚠️ Le driver doit être assigné via le paramètre (récupéré depuis SecurityContext).
     * ⚠️ availableSeats est initialisé automatiquement = totalSeats.
     *
     * @param dto Le DTO de création
     * @param driver Le conducteur (utilisateur connecté)
     * @return Une nouvelle entité Trip (non persistée)
     */
    public Trip toEntity(TripCreateDTO dto, Student driver) {
        if (dto == null) {
            return null;
        }

        Trip trip = new Trip();
        trip.setDepartureAddress(dto.getDepartureAddress());
        trip.setDestinationAddress(dto.getDestinationAddress());
        trip.setDepartureTime(dto.getDepartureTime());
        trip.setTotalSeats(dto.getTotalSeats());
        trip.setAvailableSeats(dto.getTotalSeats());  // Au début, toutes les places sont disponibles
        trip.setRegular(dto.isRegular());
        trip.setDriver(driver);  // ⚠️ Conducteur passé en paramètre

        return trip;
    }

    /**
     * Met à jour une entité Trip existante avec les données d'un TripDTO.
     * ⚠️ Ne modifie PAS le driver (sécurité : seul le conducteur peut modifier son trajet).
     * ⚠️ Ne modifie PAS availableSeats directement (géré par les réservations).
     *
     * @param trip L'entité existante à mettre à jour
     * @param dto Le DTO contenant les nouvelles données
     */
    public void updateEntity(Trip trip, TripDTO dto) {
        if (trip == null || dto == null) {
            return;
        }

        trip.setDepartureAddress(dto.getDepartureAddress());
        trip.setDestinationAddress(dto.getDestinationAddress());
        trip.setDepartureTime(dto.getDepartureTime());
        trip.setTotalSeats(dto.getTotalSeats());
        trip.setRegular(dto.isRegular());
        // Le driver n'est PAS mis à jour (sécurité)
        // availableSeats n'est PAS mis à jour ici (géré par BookingService)
    }
}
