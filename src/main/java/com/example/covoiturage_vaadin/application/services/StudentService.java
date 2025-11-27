package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.ports.IStudentRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Student;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    private final IStudentRepositoryPort studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AllowedStudentCodeService codeService;

    public StudentService(IStudentRepositoryPort studentRepository,
                         PasswordEncoder passwordEncoder,
                         AllowedStudentCodeService codeService) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.codeService = codeService;
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
        // Libérer le code whitelist si l'étudiant en a utilisé un
        String studentCode = student.getStudentCode();
        if (studentCode != null && codeService.isCodeWhitelisted(studentCode)) {
            codeService.findByStudentCode(studentCode).ifPresent(code -> {
                if (code.isUsed() && code.getUsedBy() != null && code.getUsedBy().getId().equals(student.getId())) {
                    // Remettre le code comme non utilisé
                    code.setUsed(false);
                    code.setUsedBy(null);
                    codeService.saveCode(code);
                }
            });
        }

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

    @Transactional(readOnly = true)
    public boolean existsByStudentCode(String studentCode) {
        return studentRepository.findByStudentCode(studentCode).isPresent();
    }

    /**
     * Classe pour retourner l'étudiant créé + le mot de passe en clair
     */
    public static class StudentCreationResult {
        private final Student student;
        private final String plainPassword;

        public StudentCreationResult(Student student, String plainPassword) {
            this.student = student;
            this.plainPassword = plainPassword;
        }

        public Student getStudent() {
            return student;
        }

        public String getPlainPassword() {
            return plainPassword;
        }
    }

    /**
     * Création d'un étudiant par un admin.
     * Génère automatiquement le mot de passe : 4 lettres du nom + 4 premiers caractères du code.
     * Exemple : nom="Bouskine", code="22405100" → password="bous2240"
     */
    @Transactional
    public StudentCreationResult createStudentAsAdmin(String name, String email, String studentCode) {
        // Validations
        if (existsByStudentCode(studentCode)) {
            throw new IllegalArgumentException("Ce code étudiant est déjà utilisé");
        }
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Générer username (code étudiant par défaut)
        String username = studentCode;
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }

        // Générer le mot de passe : 4 lettres du nom + 4 premiers caractères du code
        String plainPassword = generatePassword(name, studentCode);

        // Créer l'étudiant
        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setStudentCode(studentCode);
        student.setUsername(username);
        student.setPassword(passwordEncoder.encode(plainPassword));
        student.setRole("ROLE_USER");
        student.setEnabled(true);
        student.setApproved(true); // Créé par admin = auto-approuvé
        student.setCreatedAt(LocalDateTime.now());

        Student savedStudent = studentRepository.save(student);

        // Si le code étudiant est dans la whitelist, le marquer comme utilisé
        if (codeService.isCodeWhitelisted(studentCode)) {
            codeService.markCodeAsUsed(studentCode, savedStudent);
        }

        return new StudentCreationResult(savedStudent, plainPassword);
    }

    /**
     * Génère un mot de passe : 4 premières lettres du nom + 4 premiers caractères du code.
     * Tout en minuscules.
     * Exemple : "Bouskine" + "22405100" → "bous2240"
     * Exemple : "Li" + "123" → "li12" (prend ce qui existe)
     */
    private String generatePassword(String name, String studentCode) {
        // Prendre les 4 premières lettres du nom (ou moins si nom court)
        String namePart = name.length() >= 4 ? name.substring(0, 4) : name;

        // Prendre les 4 premiers caractères du code (ou moins si code court)
        String codePart = studentCode.length() >= 4 ? studentCode.substring(0, 4) : studentCode;

        return (namePart + codePart).toLowerCase();
    }
}
