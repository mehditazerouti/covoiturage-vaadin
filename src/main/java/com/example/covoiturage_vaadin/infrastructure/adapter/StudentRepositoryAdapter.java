package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.application.ports.IStudentRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Student;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

// Cette classe est l'ADAPTATEUR qui relie la couche Application à la DB (via JPA)
@Component
public class StudentRepositoryAdapter implements IStudentRepositoryPort {

    // Injection de l'interface Spring Data JPA
    private final StudentJpaRepository jpaRepository; 

    public StudentRepositoryAdapter(StudentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Student save(Student student) {
        return jpaRepository.save(student);
    }

    @Override
    public void delete(Student student) {
        jpaRepository.delete(student);
    }

    @Override
    public Optional<Student> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Student> findAll() {
        return jpaRepository.findAll();
    }

    // Implémentations des nouvelles méthodes pour l'authentification

    @Override
    public Optional<Student> findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public Optional<Student> findByStudentCode(String studentCode) {
        return jpaRepository.findByStudentCode(studentCode);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}