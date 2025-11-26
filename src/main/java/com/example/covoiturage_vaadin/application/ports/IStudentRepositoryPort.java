package com.example.covoiturage_vaadin.application.ports;

import com.example.covoiturage_vaadin.domain.model.Student;
import java.util.List;
import java.util.Optional;

public interface IStudentRepositoryPort {
    Student save(Student student);
    void delete(Student student);
    Optional<Student> findById(Long id);
    List<Student> findAll();
}