package com.example.covoiturage_vaadin.infrastructure.security;

import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.domain.model.Student;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service qui fait le pont entre Spring Security et notre domaine Student.
 * Spring Security utilise ce service pour charger les détails d'un utilisateur lors de l'authentification.
 *
 * Intègre le rate limiting pour bloquer les comptes après trop de tentatives échouées.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final StudentService studentService;
    private final LoginAttemptService loginAttemptService;

    public UserDetailsServiceImpl(StudentService studentService, LoginAttemptService loginAttemptService) {
        this.studentService = studentService;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. RATE LIMITING : Vérifier si le compte est bloqué
        if (loginAttemptService.isBlocked(username)) {
            throw new LockedException(
                "Compte temporairement bloqué après trop de tentatives échouées. Réessayez dans 15 minutes."
            );
        }

        // 2. Récupérer l'étudiant depuis la base
        Student student = studentService.getStudentByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));

        // 3. Convertir notre Student en UserDetails (format attendu par Spring Security)
        return User.builder()
                .username(student.getUsername())
                .password(student.getPassword())
                .roles(student.getRole().replace("ROLE_", ""))  // Spring ajoute automatiquement "ROLE_"
                .disabled(!student.isEnabled())
                .build();
    }
}
