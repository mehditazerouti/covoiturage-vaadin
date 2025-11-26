package com.example.covoiturage_vaadin.infrastructure.adapter;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.covoiturage_vaadin.domain.model.Student;

public interface StudentJpaRepository extends
	JpaRepository<Student, Long> {
}