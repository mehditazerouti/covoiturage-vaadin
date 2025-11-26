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
}