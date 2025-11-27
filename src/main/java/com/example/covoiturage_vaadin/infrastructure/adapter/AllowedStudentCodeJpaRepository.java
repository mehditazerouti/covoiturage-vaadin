package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.domain.model.AllowedStudentCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA pour AllowedStudentCode.
 * Spring Data génère automatiquement l'implémentation.
 */
public interface AllowedStudentCodeJpaRepository extends JpaRepository<AllowedStudentCode, Long> {

    Optional<AllowedStudentCode> findByStudentCode(String studentCode);

    boolean existsByStudentCode(String studentCode);
}
