package com.example.covoiturage_vaadin.infrastructure.config;

import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.domain.model.Student;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Initialise les données par défaut au démarrage de l'application.
 * Crée un compte administrateur si aucun n'existe.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final StudentService studentService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(StudentService studentService, PasswordEncoder passwordEncoder) {
        this.studentService = studentService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Créer un compte admin par défaut si aucun n'existe
        if (!studentService.existsByUsername("admin")) {
            Student admin = new Student();
            admin.setName("Administrateur");
            admin.setEmail("admin@dauphine.eu");
            admin.setStudentCode("ADMIN001");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            admin.setCreatedAt(LocalDateTime.now());

            studentService.saveStudent(admin);

            System.out.println("✅ Compte administrateur créé : admin / admin123");
        }
    }
}
