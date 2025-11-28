package com.example.covoiturage_vaadin.ui.view;

import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.domain.model.Student;
import com.example.covoiturage_vaadin.ui.component.ConfirmDeleteDialog;
import com.example.covoiturage_vaadin.ui.component.MainLayout; // Import du layout
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/students", layout = MainLayout.class)
@PageTitle("Étudiants - Covoiturage")
@RolesAllowed("ADMIN")
public class AdminStudentView extends VerticalLayout {

    private final StudentService studentService;
    private final Grid<Student> grid = new Grid<>(Student.class, false); // false = pas de colonnes auto

    public AdminStudentView(StudentService studentService) {
        this.studentService = studentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // 1. Titre Moderne
        H2 title = new H2("Annuaire des Étudiants");

        // 2. Configuration de la Grille
        configureGrid();

        // 3. Chargement des données (Filtrées)
        refreshGrid();

        add(title, grid);
    }

	private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        
        // Colonne Avatar + Nom
        grid.addColumn(new ComponentRenderer<>(student -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            
            Avatar avatar = new Avatar(student.getName());
            
            Span name = new Span(student.getName());
            Span email = new Span(student.getEmail());
            email.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");
            
            VerticalLayout column = new VerticalLayout(name, email);
            column.setPadding(false);
            column.setSpacing(false);
            
            row.add(avatar, column);
            return row;
        })).setHeader("Étudiant").setAutoWidth(true);

        // Colonne Code Étudiant
        grid.addColumn(Student::getStudentCode).setHeader("Code Étudiant");

        // --- MODIFICATION ICI : VÉRIFICATION DU RÔLE ADMIN ---
        
        // 1. On récupère l'authentification
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 2. On vérifie si l'utilisateur a le rôle "ROLE_ADMIN"
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));

        // 3. On ajoute la colonne SEULEMENT si c'est un admin
        if (isAdmin) {
            grid.addComponentColumn(student -> {
                String currentUsername = authentication.getName();
                
                Button deleteBtn = new Button(VaadinIcon.TRASH.create());
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

                // Protection contre la suppression de soi-même
                if (student.getUsername().equals(currentUsername)) {
                    deleteBtn.setEnabled(false);
                    deleteBtn.setTooltipText("Vous ne pouvez pas supprimer votre propre compte");
                } else {
                    deleteBtn.addClickListener(e -> {
                        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                            "Supprimer l'étudiant",
                            "Voulez-vous vraiment supprimer l'étudiant \"" + student.getName() + "\" ?",
                            () -> studentService.deleteStudent(student),
                            this::refreshGrid
                        );
                        dialog.open();
                    });
                }
                return deleteBtn;
            }).setHeader("Actions");
        }
    }

    private void refreshGrid() {
        // Filtrer pour n'afficher que les étudiants approuvés (approved=true) et non-admins
        List<Student> students = studentService.getAllStudents().stream()
                .filter(s -> !"ROLE_ADMIN".equals(s.getRole())) // Exclure les admins
                .filter(Student::isApproved) // N'afficher que les étudiants approuvés
                .collect(Collectors.toList());

        grid.setItems(students);
    }
}