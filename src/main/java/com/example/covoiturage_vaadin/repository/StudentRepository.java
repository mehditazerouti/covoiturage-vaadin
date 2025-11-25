package com.example.covoiturage_vaadin.repository;

import com.example.covoiturage_vaadin.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends
	JpaRepository<Student, Long> {
}