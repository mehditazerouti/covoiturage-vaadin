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

    @Transactional(readOnly = true)
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    @Transactional(readOnly = true)
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

    // Nouvelles méthodes pour l'authentification

    @Transactional(readOnly = true)
    public Optional<Student> getStudentByUsername(String username) {
        return studentRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentByStudentCode(String studentCode) {
        return studentRepository.findByStudentCode(studentCode);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return studentRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }
}