package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.ports.IStudentRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Student;

import org.springframework.transaction.annotation.Transactional; // <-- NOUVEL IMPORT

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    private final IStudentRepositoryPort studentRepository;

    public StudentService(IStudentRepositoryPort studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    @Transactional
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional
    public Student saveStudent(Student student) {
        // Logique métier: validation, événements...
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Student student) {
        studentRepository.delete(student);
    }
}