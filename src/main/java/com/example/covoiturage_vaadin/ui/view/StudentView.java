// Dans src/main/java/.../ui/view/StudentView.java

package com.example.covoiturage_vaadin.ui.view;

import com.example.covoiturage_vaadin.domain.model.Student;
import com.example.covoiturage_vaadin.application.services.StudentService; // <-- NOUVEAU
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class StudentView extends VerticalLayout {
	// ANCIEN : private final StudentJpaRepository repo;
	private final StudentService studentService; // <-- Injectez le Service
	
	// ANCIEN : public StudentView(StudentJpaRepository repo) {
	public StudentView(StudentService studentService) { // <-- Constructeur corrigé
		this.studentService = studentService;
		Grid<Student> grid = new Grid<>(Student.class);
		
		// Utilisation du Service
		grid.setItems(studentService.getAllStudents()); // <-- Appel au Service

		// Ajouter une colonne avec un bouton de suppression pour chaque ligne
		grid.addComponentColumn(student -> {
			Button deleteBtn = new Button("Supprimer", e -> {
				// Utilisation du Service
				studentService.deleteStudent(student); 
				grid.setItems(studentService.getAllStudents());
			});
			return deleteBtn;
		}).setHeader("Actions");

		TextField name = new TextField("Nom");
		TextField email = new TextField("Email");
		Button save = new Button("Ajouter l'étudiant", e -> {
			// Utilisation du Service
			studentService.saveStudent(new Student(name.getValue(), email.getValue())); 
			grid.setItems(studentService.getAllStudents());
		});
		add(name, email, save, grid);
	}
}