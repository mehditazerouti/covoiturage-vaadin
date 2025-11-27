package com.example.covoiturage_vaadin.infrastructure.adapter;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.covoiturage_vaadin.domain.model.Student;

import java.util.Optional;

public interface StudentJpaRepository extends
	JpaRepository<Student, Long> {

	// Méthodes pour l'authentification
	Optional<Student> findByUsername(String username);
	Optional<Student> findByStudentCode(String studentCode);
	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
}