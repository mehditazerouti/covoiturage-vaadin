package com.example.covoiturage_vaadin.application.dto.mapper;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.dto.student.StudentListDTO;
import com.example.covoiturage_vaadin.application.dto.student.StudentCreateDTO;
import com.example.covoiturage_vaadin.application.dto.student.ProfileDTO;
import com.example.covoiturage_vaadin.domain.model.Student;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper pour convertir Student Entity ↔ DTO.
 *
 * Ce mapper centralise toutes les conversions entre les entités Student
 * et leurs différentes représentations DTO.
 *
 * Utilisé par :
 * - StudentService (pour retourner des DTO)
 * - TripMapper (pour convertir le driver)
 * - BookingMapper (pour convertir l'étudiant)
 */
@Component
public class StudentMapper {

    /**
     * Convertit une entité Student en StudentDTO (complet, sans password).
     *
     * @param student L'entité Student à convertir
     * @return StudentDTO ou null si student est null
     */
    public StudentDTO toDTO(Student student) {
        if (student == null) {
            return null;
        }

        return new StudentDTO(
            student.getId(),
            student.getName(),
            student.getEmail(),
            student.getStudentCode(),
            student.getUsername(),
            student.getRole(),
            student.isEnabled(),
            student.isApproved(),
            student.getCreatedAt(),
            student.getAvatar()
        );
    }

    /**
     * Convertit une entité Student en StudentListDTO (version minimale).
     * Utilisé pour afficher un étudiant dans une liste ou comme référence.
     *
     * @param student L'entité Student à convertir
     * @return StudentListDTO ou null si student est null
     */
    public StudentListDTO toListDTO(Student student) {
        if (student == null) {
            return null;
        }

        return new StudentListDTO(
            student.getId(),
            student.getName(),
            student.getEmail()
        );
    }

    /**
     * Convertit un StudentCreateDTO en entité Student.
     * ⚠️ Le password doit être hashé avec BCrypt AVANT d'appeler cette méthode.
     *
     * @param dto Le DTO de création
     * @param hashedPassword Le mot de passe déjà hashé avec BCrypt
     * @return Une nouvelle entité Student (non persistée)
     */
    public Student toEntity(StudentCreateDTO dto, String hashedPassword) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setStudentCode(dto.getStudentCode());
        student.setUsername(dto.getUsername());
        student.setPassword(hashedPassword);  // ⚠️ Déjà hashé !
        student.setRole("ROLE_USER");  // Rôle par défaut
        student.setEnabled(false);  // Désactivé par défaut (sera activé après validation)
        student.setApproved(false);  // Non approuvé par défaut
        student.setCreatedAt(LocalDateTime.now());

        return student;
    }

    /**
     * Met à jour une entité Student existante avec les données d'un StudentDTO.
     * ⚠️ Ne modifie PAS le password (sécurité).
     * ⚠️ Ne modifie PAS le studentCode (immuable).
     *
     * @param student L'entité existante à mettre à jour
     * @param dto Le DTO contenant les nouvelles données
     */
    public void updateEntity(Student student, StudentDTO dto) {
        if (student == null || dto == null) {
            return;
        }

        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setUsername(dto.getUsername());
        student.setRole(dto.getRole());
        student.setEnabled(dto.isEnabled());
        student.setApproved(dto.isApproved());
        // Le password n'est PAS mis à jour ici (sécurité)
        // Le studentCode n'est PAS mis à jour (immuable)
    }

    /**
     * Convertit une entité Student en ProfileDTO avec statistiques.
     * ⚠️ Les statistiques (tripsCount, bookingsCount) doivent être calculées par le service.
     *
     * @param student L'entité Student à convertir
     * @param tripsCount Nombre de trajets proposés
     * @param bookingsCount Nombre de réservations effectuées
     * @return ProfileDTO ou null si student est null
     */
    public ProfileDTO toProfileDTO(Student student, long tripsCount, long bookingsCount) {
        if (student == null) {
            return null;
        }

        return new ProfileDTO(
            student.getId(),
            student.getName(),
            student.getEmail(),
            student.getStudentCode(),
            student.getUsername(),
            student.getAvatar(),
            student.getCreatedAt(),
            tripsCount,
            bookingsCount
        );
    }
}
