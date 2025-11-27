package com.example.covoiturage_vaadin.infrastructure.config;

import com.example.covoiturage_vaadin.application.services.AllowedStudentCodeService;
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
 * Crée un compte administrateur et des codes étudiants pré-autorisés.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final StudentService studentService;
    private final AllowedStudentCodeService codeService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(StudentService studentService,
                          AllowedStudentCodeService codeService,
                          PasswordEncoder passwordEncoder) {
        this.studentService = studentService;
        this.codeService = codeService;
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
            admin.setApproved(true); // Admin est toujours approuvé
            admin.setCreatedAt(LocalDateTime.now());

            studentService.saveStudent(admin);

            System.out.println("✅ Compte administrateur créé : admin / admin123");
        }

        // Ajouter des codes étudiants pré-autorisés pour les tests
        if (codeService.findAll().isEmpty()) {
            String[] defaultCodes = {"22405100", "22405101", "22405102"};

            for (String code : defaultCodes) {
                try {
                    codeService.addAllowedCode(code, "SYSTEM");
                    System.out.println("✅ Code étudiant whitelisté : " + code);
                } catch (IllegalArgumentException e) {
                    // Code déjà existant, on ignore
                }
            }
        }
    }
}
