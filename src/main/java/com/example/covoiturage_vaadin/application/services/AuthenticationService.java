package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.dto.mapper.StudentMapper;
import com.example.covoiturage_vaadin.domain.model.Student;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service gérant l'inscription des étudiants.
 * Gère la logique de whitelist et d'approbation.
 */
@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private final StudentService studentService;
    private final AllowedStudentCodeService codeService;
    private final PasswordEncoder passwordEncoder;
    private final StudentMapper studentMapper;

    public AuthenticationService(StudentService studentService,
                                AllowedStudentCodeService codeService,
                                PasswordEncoder passwordEncoder,
                                StudentMapper studentMapper) {
        this.studentService = studentService;
        this.codeService = codeService;
        this.passwordEncoder = passwordEncoder;
        this.studentMapper = studentMapper;
    }

    /**
     * Inscription d'un étudiant.
     *
     * Logique :
     * - Si le code est whitelisté → approved=true, enabled=true, marquer code comme utilisé
     * - Si le code n'est PAS whitelisté → approved=false, enabled=false (en attente validation admin)
     *
     * @return StudentDTO (sans password) de l'étudiant inscrit
     */
    @Transactional
    public StudentDTO registerStudent(String studentCode, String name, String email, String password) {
        // Validations
        if (studentService.existsByStudentCode(studentCode)) {
            throw new IllegalArgumentException("Ce code étudiant est déjà utilisé");
        }
        if (studentService.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Username = code étudiant
        String username = studentCode;
        if (studentService.existsByUsername(username)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }

        // Vérifier si le code est dans la whitelist
        boolean isWhitelisted = codeService.isCodeAvailable(studentCode);

        // Créer l'étudiant
        Student student = new Student();
        student.setStudentCode(studentCode);
        student.setName(name);
        student.setEmail(email);
        student.setUsername(username);
        student.setPassword(passwordEncoder.encode(password));
        student.setRole("ROLE_USER");
        student.setCreatedAt(LocalDateTime.now());

        if (isWhitelisted) {
            // Code whitelisté → approuvé directement
            student.setApproved(true);
            student.setEnabled(true);
        } else {
            // Code NON whitelisté → en attente de validation admin
            student.setApproved(false);
            student.setEnabled(false); // Ne peut pas se connecter tant que non approuvé
        }

        Student savedStudent = studentService.saveStudent(student);

        // Si whitelisté, marquer le code comme utilisé
        if (isWhitelisted) {
            codeService.markCodeAsUsed(studentCode, savedStudent);
        }

        // Convertir en DTO avant de retourner (sécurité : pas de password)
        return studentMapper.toDTO(savedStudent);
    }

    /**
     * Approuver un étudiant en attente.
     * Whitelist son code et active son compte.
     */
    @Transactional
    public void approveStudent(Student student, String adminUsername) {
        if (student.isApproved()) {
            throw new IllegalStateException("Cet étudiant est déjà approuvé");
        }

        // Whitelist le code étudiant
        if (!codeService.isCodeWhitelisted(student.getStudentCode())) {
            codeService.addAllowedCode(student.getStudentCode(), adminUsername);
        }

        // Marquer le code comme utilisé
        codeService.markCodeAsUsed(student.getStudentCode(), student);

        // Activer le compte
        student.setApproved(true);
        student.setEnabled(true);
        studentService.saveStudent(student);
    }

    /**
     * Approuver un étudiant par son ID (version publique pour les vues).
     * @param studentId L'ID de l'étudiant à approuver
     * @param adminUsername Le nom d'utilisateur de l'admin qui approuve
     */
    @Transactional
    public void approveStudentById(Long studentId, String adminUsername) {
        Student student = studentService.getStudentByUsername(adminUsername)
                .orElseThrow(() -> new IllegalStateException("Admin non trouvé"));

        Student studentToApprove = studentService.getStudentEntityById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé"));

        approveStudent(studentToApprove, adminUsername);
    }
}
