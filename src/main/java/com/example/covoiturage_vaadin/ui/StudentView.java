package com.example.covoiturage_vaadin.ui;

import com.example.covoiturage_vaadin.Student;

import com.example.covoiturage_vaadin.repository.StudentRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class StudentView extends VerticalLayout {
	private final StudentRepository repo;
	public StudentView(StudentRepository repo) {
		this.repo = repo;
		Grid<Student> grid = new Grid<>(Student.class);
		grid.setItems(repo.findAll());

		// Ajouter une colonne avec un bouton de suppression pour chaque ligne
		grid.addComponentColumn(student -> {
			Button deleteBtn = new Button("Supprimer", e -> {
				repo.delete(student);
				grid.setItems(repo.findAll());
			});
			return deleteBtn;
		}).setHeader("Actions");

		TextField name = new TextField("Nom");
		TextField email = new TextField("Email");
		Button save = new Button("Ajouter l'étudiant", e -> {
			repo.save(new Student(null,
					name.getValue(),
					email.getValue()));
					grid.setItems(repo.findAll());
		});
		add(name, email, save, grid);
	}
}
