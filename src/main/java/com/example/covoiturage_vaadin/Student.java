package com.example.covoiturage_vaadin;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Student {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String email;
	// getters/setters
	
	// Constructeur vide (obligatoire pour JPA)
    public Student() {
    }
    
    // Constructeur avec paramètres (optionnel)
    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public Student(Long id, String name, String email) {
    	this.id = id;
        this.name = name;
        this.email = email;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
