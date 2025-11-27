package com.example.covoiturage_vaadin.application.ports;

import com.example.covoiturage_vaadin.domain.model.AllowedStudentCode;
import java.util.List;
import java.util.Optional;

/**
 * Port (interface) pour la gestion des codes étudiants autorisés.
 * Définit les opérations disponibles pour la couche Application.
 */
public interface IAllowedStudentCodeRepositoryPort {

    AllowedStudentCode save(AllowedStudentCode code);

    Optional<AllowedStudentCode> findById(Long id);

    Optional<AllowedStudentCode> findByStudentCode(String studentCode);

    List<AllowedStudentCode> findAll();

    void delete(AllowedStudentCode code);

    boolean existsByStudentCode(String studentCode);
}
