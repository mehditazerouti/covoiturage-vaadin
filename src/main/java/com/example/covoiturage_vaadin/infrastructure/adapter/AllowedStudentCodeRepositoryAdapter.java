package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.application.ports.IAllowedStudentCodeRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.AllowedStudentCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter qui implémente le port IAllowedStudentCodeRepositoryPort.
 * Fait le pont entre la couche Application et l'infrastructure JPA.
 */
@Component
public class AllowedStudentCodeRepositoryAdapter implements IAllowedStudentCodeRepositoryPort {

    private final AllowedStudentCodeJpaRepository jpaRepository;

    public AllowedStudentCodeRepositoryAdapter(AllowedStudentCodeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AllowedStudentCode save(AllowedStudentCode code) {
        return jpaRepository.save(code);
    }

    @Override
    public Optional<AllowedStudentCode> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<AllowedStudentCode> findByStudentCode(String studentCode) {
        return jpaRepository.findByStudentCode(studentCode);
    }

    @Override
    public List<AllowedStudentCode> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void delete(AllowedStudentCode code) {
        jpaRepository.delete(code);
    }

    @Override
    public boolean existsByStudentCode(String studentCode) {
        return jpaRepository.existsByStudentCode(studentCode);
    }
}
