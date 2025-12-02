package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.dto.mapper.StudentMapper;
import com.example.covoiturage_vaadin.application.ports.IStudentRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Student;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private final IStudentRepositoryPort studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AllowedStudentCodeService codeService;
    private final StudentMapper studentMapper;

    public StudentService(IStudentRepositoryPort studentRepository,
                         PasswordEncoder passwordEncoder,
                         AllowedStudentCodeService codeService,
                         StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.codeService = codeService;
        this.studentMapper = studentMapper;
    }

    // ========== MÉTHODES PUBLIQUES RETOURNANT DES DTO ==========

    /**
     * Récupère un étudiant par son ID (version DTO, sans password).
     * Utilisé par les vues pour afficher les détails d'un étudiant.
     */
    @Transactional(readOnly = true)
    public Optional<StudentDTO> getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(studentMapper::toDTO);
    }

    /**
     * Récupère tous les étudiants (version DTO, sans password).
     * Utilisé par AdminStudentView pour afficher la liste.
     */
    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ========== MÉTHODES INTERNES RETOURNANT DES ENTITÉS ==========
    // Ces méthodes sont utilisées par Spring Security et d'autres services internes

    /**
     * Récupère l'entité Student complète par ID (usage interne uniquement).
     * ⚠️ Cette méthode expose le password hashé, à utiliser avec précaution.
     */
    @Transactional(readOnly = true)
    public Optional<Student> getStudentEntityById(Long id) {
        return studentRepository.findById(id);
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

    /**
     * Supprime un étudiant par son ID (version publique pour les vues).
     * @param id L'ID de l'étudiant à supprimer
     */
    @Transactional
    public void deleteStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé"));
        deleteStudent(student);
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
     * Classe pour retourner l'étudiant créé (DTO, sans password hashé) + le mot de passe en clair.
     * Utilisé uniquement lors de la création par admin pour afficher le mot de passe généré.
     */
    public static class StudentCreationResult {
        private final StudentDTO student;
        private final String plainPassword;

        public StudentCreationResult(StudentDTO student, String plainPassword) {
            this.student = student;
            this.plainPassword = plainPassword;
        }

        public StudentDTO getStudent() {
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

        // Convertir en DTO avant de retourner (sécurité : pas de password)
        StudentDTO studentDTO = studentMapper.toDTO(savedStudent);

        return new StudentCreationResult(studentDTO, plainPassword);
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
