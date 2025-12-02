package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.dto.student.ProfileDTO;
import com.example.covoiturage_vaadin.application.dto.mapper.StudentMapper;
import com.example.covoiturage_vaadin.application.ports.IStudentRepositoryPort;
import com.example.covoiturage_vaadin.application.ports.ITripRepositoryPort;
import com.example.covoiturage_vaadin.application.ports.IBookingRepositoryPort;
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
    private final ITripRepositoryPort tripRepository;
    private final IBookingRepositoryPort bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final AllowedStudentCodeService codeService;
    private final StudentMapper studentMapper;

    public StudentService(IStudentRepositoryPort studentRepository,
                         ITripRepositoryPort tripRepository,
                         IBookingRepositoryPort bookingRepository,
                         PasswordEncoder passwordEncoder,
                         AllowedStudentCodeService codeService,
                         StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.tripRepository = tripRepository;
        this.bookingRepository = bookingRepository;
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
     *
     * ⚠️ IMPORTANT : Si le code n'est pas dans la whitelist, il sera automatiquement ajouté.
     * Logique : Quand un admin crée manuellement un étudiant, c'est une validation implicite du code.
     *
     * @param name Nom complet de l'étudiant
     * @param email Email de l'étudiant
     * @param studentCode Code étudiant
     * @param adminUsername Username de l'admin qui crée l'étudiant (pour traçabilité whitelist)
     */
    @Transactional
    public StudentCreationResult createStudentAsAdmin(String name, String email, String studentCode, String adminUsername) {
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

        // ✅ AUTO-WHITELIST : Si le code n'est pas dans la whitelist, l'ajouter automatiquement
        // Logique : Création manuelle par admin = validation implicite du code étudiant
        if (!codeService.isCodeWhitelisted(studentCode)) {
            codeService.addAllowedCode(studentCode, adminUsername);
        }

        // Marquer le code comme utilisé par cet étudiant
        codeService.markCodeAsUsed(studentCode, savedStudent);

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

    // ========== MÉTHODES DE GESTION DE PROFIL ==========

    /**
     * Récupère le profil complet d'un étudiant avec statistiques.
     *
     * @param studentId L'ID de l'étudiant
     * @return ProfileDTO avec stats, ou null si l'étudiant n'existe pas
     */
    @Transactional(readOnly = true)
    public ProfileDTO getProfile(Long studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return null;
        }

        // Calculer les statistiques
        long tripsCount = tripRepository.findAll().stream()
            .filter(trip -> trip.getDriver() != null && trip.getDriver().getId().equals(studentId))
            .count();

        long bookingsCount = bookingRepository.findAll().stream()
            .filter(booking -> booking.getStudent() != null && booking.getStudent().getId().equals(studentId))
            .count();

        return studentMapper.toProfileDTO(student, tripsCount, bookingsCount);
    }

    /**
     * Met à jour le profil d'un étudiant (nom et email uniquement).
     *
     * @param studentId L'ID de l'étudiant
     * @param name Nouveau nom
     * @param email Nouvel email
     * @return StudentDTO mis à jour
     */
    @Transactional
    public StudentDTO updateProfile(Long studentId, String name, String email) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé"));

        // Vérifier que l'email n'est pas déjà utilisé par un autre étudiant
        if (!student.getEmail().equals(email) && existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        student.setName(name);
        student.setEmail(email);

        Student savedStudent = studentRepository.save(student);
        return studentMapper.toDTO(savedStudent);
    }

    /**
     * Met à jour l'avatar d'un étudiant.
     *
     * @param studentId L'ID de l'étudiant
     * @param avatar Nom de l'icône Vaadin (USER, MALE, FEMALE)
     * @return StudentDTO mis à jour
     */
    @Transactional
    public StudentDTO updateAvatar(Long studentId, String avatar) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé"));

        student.setAvatar(avatar);

        Student savedStudent = studentRepository.save(student);
        return studentMapper.toDTO(savedStudent);
    }

    /**
     * Change le mot de passe d'un étudiant.
     *
     * @param studentId L'ID de l'étudiant
     * @param oldPassword Ancien mot de passe (en clair)
     * @param newPassword Nouveau mot de passe (en clair)
     * @throws IllegalArgumentException Si l'ancien mot de passe est incorrect
     */
    @Transactional
    public void changePassword(Long studentId, String oldPassword, String newPassword) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé"));

        // Vérifier que l'ancien mot de passe est correct
        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect");
        }

        // Hasher et sauvegarder le nouveau mot de passe
        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);
    }

    /**
     * Met à jour TOUS les champs d'un étudiant (réservé aux admins).
     * Contrairement à updateProfile(), cette méthode permet de modifier :
     * - Le code étudiant
     * - Le username
     * - Le rôle (USER/ADMIN)
     * - L'état enabled
     * - L'état approved
     *
     * @param updatedStudent DTO contenant les nouvelles valeurs
     * @return StudentDTO mis à jour
     * @throws IllegalArgumentException Si validation échoue ou étudiant non trouvé
     */
    @Transactional
    public StudentDTO updateStudentAdmin(StudentDTO updatedStudent) {
        Student student = studentRepository.findById(updatedStudent.getId())
            .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé"));

        // Vérifier unicité de l'email (si changé)
        if (!student.getEmail().equals(updatedStudent.getEmail()) && existsByEmail(updatedStudent.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Vérifier unicité du username (si changé)
        if (!student.getUsername().equals(updatedStudent.getUsername()) && existsByUsername(updatedStudent.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }

        // Vérifier unicité du code étudiant (si changé)
        if (!student.getStudentCode().equals(updatedStudent.getStudentCode())) {
            Optional<Student> existingStudent = studentRepository.findByStudentCode(updatedStudent.getStudentCode());
            if (existingStudent.isPresent() && !existingStudent.get().getId().equals(student.getId())) {
                throw new IllegalArgumentException("Ce code étudiant est déjà utilisé");
            }
        }

        // Mettre à jour tous les champs
        student.setName(updatedStudent.getName());
        student.setEmail(updatedStudent.getEmail());
        student.setUsername(updatedStudent.getUsername());
        student.setStudentCode(updatedStudent.getStudentCode());
        student.setRole(updatedStudent.getRole());
        student.setEnabled(updatedStudent.isEnabled());
        student.setApproved(updatedStudent.isApproved());

        Student savedStudent = studentRepository.save(student);
        return studentMapper.toDTO(savedStudent);
    }
}
