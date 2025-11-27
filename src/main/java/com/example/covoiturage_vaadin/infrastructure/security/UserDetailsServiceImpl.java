package com.example.covoiturage_vaadin.infrastructure.security;

import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.domain.model.Student;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service qui fait le pont entre Spring Security et notre domaine Student.
 * Spring Security utilise ce service pour charger les détails d'un utilisateur lors de l'authentification.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final StudentService studentService;

    public UserDetailsServiceImpl(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Récupérer l'étudiant depuis la base
        Student student = studentService.getStudentByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));

        // Convertir notre Student en UserDetails (format attendu par Spring Security)
        return User.builder()
                .username(student.getUsername())
                .password(student.getPassword())
                .roles(student.getRole().replace("ROLE_", ""))  // Spring ajoute automatiquement "ROLE_"
                .disabled(!student.isEnabled())
                .build();
    }
}
