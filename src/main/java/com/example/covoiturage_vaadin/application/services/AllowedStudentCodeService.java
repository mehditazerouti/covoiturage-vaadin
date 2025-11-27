package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.ports.IAllowedStudentCodeRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.AllowedStudentCode;
import com.example.covoiturage_vaadin.domain.model.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des codes étudiants autorisés (whitelist).
 */
@Service
@Transactional(readOnly = true)
public class AllowedStudentCodeService {

    private final IAllowedStudentCodeRepositoryPort repository;

    public AllowedStudentCodeService(IAllowedStudentCodeRepositoryPort repository) {
        this.repository = repository;
    }

    /**
     * Ajoute un nouveau code étudiant à la whitelist.
     */
    @Transactional
    public AllowedStudentCode addAllowedCode(String studentCode, String createdBy) {
        if (repository.existsByStudentCode(studentCode)) {
            throw new IllegalArgumentException("Ce code étudiant est déjà dans la whitelist");
        }

        AllowedStudentCode code = new AllowedStudentCode();
        code.setStudentCode(studentCode);
        code.setCreatedBy(createdBy);
        code.setCreatedAt(LocalDateTime.now());
        return repository.save(code);
    }

    /**
     * Vérifie si un code étudiant est disponible (whitelisté ET non utilisé).
     */
    public boolean isCodeAvailable(String studentCode) {
        return repository.findByStudentCode(studentCode)
                .map(code -> !code.isUsed())
                .orElse(false);
    }

    /**
     * Vérifie si un code étudiant est dans la whitelist (peu importe s'il est utilisé).
     */
    public boolean isCodeWhitelisted(String studentCode) {
        return repository.existsByStudentCode(studentCode);
    }

    /**
     * Marque un code comme utilisé par un étudiant.
     */
    @Transactional
    public void markCodeAsUsed(String studentCode, Student student) {
        AllowedStudentCode code = repository.findByStudentCode(studentCode)
                .orElseThrow(() -> new IllegalArgumentException("Code étudiant non trouvé dans la whitelist"));

        code.markAsUsed(student);
        repository.save(code);
    }

    /**
     * Récupère tous les codes autorisés.
     */
    public List<AllowedStudentCode> findAll() {
        return repository.findAll();
    }

    /**
     * Récupère un code par son identifiant.
     */
    public Optional<AllowedStudentCode> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Récupère un code par le code étudiant.
     */
    public Optional<AllowedStudentCode> findByStudentCode(String studentCode) {
        return repository.findByStudentCode(studentCode);
    }

    /**
     * Sauvegarde ou met à jour un code étudiant.
     */
    @Transactional
    public AllowedStudentCode saveCode(AllowedStudentCode code) {
        return repository.save(code);
    }

    /**
     * Supprime un code autorisé.
     */
    @Transactional
    public void deleteCode(AllowedStudentCode code) {
        if (code.isUsed()) {
            throw new IllegalStateException("Impossible de supprimer un code déjà utilisé");
        }
        repository.delete(code);
    }

    /**
     * Supprime un code par son ID.
     */
    @Transactional
    public void deleteCodeById(Long id) {
        AllowedStudentCode code = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Code non trouvé"));
        deleteCode(code);
    }
}
