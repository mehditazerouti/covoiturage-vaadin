package com.example.covoiturage_vaadin.ui.view.admin;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.ui.component.dialog.AdminStudentProfileDialog;
import com.example.covoiturage_vaadin.ui.component.dialog.ConfirmDeleteDialog;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.example.covoiturage_vaadin.ui.component.SearchBar;
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
import com.vaadin.flow.data.provider.ListDataProvider;
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
    private final Grid<StudentDTO> grid = new Grid<>(StudentDTO.class, false);
    private final SearchBar searchBar = new SearchBar("Rechercher par nom, email ou code...");
    private ListDataProvider<StudentDTO> dataProvider;

    public AdminStudentView(StudentService studentService) {
        this.studentService = studentService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // HEADER
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
            .set("background", "white")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");

        H2 title = new H2("Annuaire des Étudiants");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem");
        header.add(title);

        // CONTENU
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        
        // CARTE
        VerticalLayout gridCard = new VerticalLayout();
        gridCard.setSizeFull();
        gridCard.setPadding(false);
        gridCard.setSpacing(false);
        
        gridCard.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "var(--lumo-box-shadow-s)")
            .set("overflow", "hidden");

        HorizontalLayout toolbar = new HorizontalLayout(searchBar);
        toolbar.setWidthFull();
        toolbar.setPadding(true);
        searchBar.setMaxWidth("400px");
        searchBar.getStyle().set("margin", "0");

        configureSearchBar();
        configureGrid();

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        gridCard.add(toolbar, grid);
        mainContent.add(gridCard);
        add(header, mainContent);
        
        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(new ComponentRenderer<>(student -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            row.setSpacing(true);
            
            Avatar avatar = new Avatar(student.getName());
            avatar.addThemeVariants(com.vaadin.flow.component.avatar.AvatarVariant.LUMO_SMALL);
            
            Span name = new Span(student.getName());
            name.getStyle().set("font-weight", "500");
            
            Span email = new Span(student.getEmail());
            email.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");
            
            VerticalLayout column = new VerticalLayout(name, email);
            column.setPadding(false);
            column.setSpacing(false);
            
            row.add(avatar, column);
            return row;
        })).setHeader("Étudiant").setAutoWidth(true);

        grid.addColumn(StudentDTO::getStudentCode)
            .setHeader("Code Étudiant")
            .setAutoWidth(true);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (isAdmin) {
            grid.addComponentColumn(student -> {
                HorizontalLayout actionsLayout = new HorizontalLayout();
                
                Button viewBtn = new Button(VaadinIcon.EYE.create());
                viewBtn.getStyle().set("cursor", "pointer");
                viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                viewBtn.setTooltipText("Voir le profil");
                viewBtn.addClickListener(e -> {
                    new AdminStudentProfileDialog(studentService, student.getId(), this::refreshGrid).open();
                });

                Button deleteBtn = new Button(VaadinIcon.TRASH.create());
                deleteBtn.getStyle().set("cursor", "pointer");
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                
                if (student.getUsername().equals(authentication.getName())) {
                    deleteBtn.setEnabled(false);
                } else {
                    deleteBtn.addClickListener(e -> {
                        new ConfirmDeleteDialog("Supprimer", 
                            "Voulez-vous supprimer " + student.getName() + " ?", 
                            () -> studentService.deleteStudentById(student.getId()), 
                            this::refreshGrid).open();
                    });
                }

                actionsLayout.add(viewBtn, deleteBtn);
                return actionsLayout;
            }).setHeader("Actions").setAutoWidth(true);
        }
    }

    private void configureSearchBar() {
        searchBar.addValueChangeListener(e -> applyFilter());
    }

    private void applyFilter() {
        if (dataProvider != null) {
            dataProvider.clearFilters();
            String searchTerm = searchBar.getSearchValue();
            if (!searchBar.isSearchEmpty()) {
                dataProvider.addFilter(student -> {
                    String name = student.getName().toLowerCase();
                    String email = student.getEmail().toLowerCase();
                    String code = student.getStudentCode().toLowerCase();
                    return name.contains(searchTerm) || email.contains(searchTerm) || code.contains(searchTerm);
                });
            }
        }
    }

    private void refreshGrid() {
        List<StudentDTO> students = studentService.getAllStudents().stream()
                .filter(s -> !"ROLE_ADMIN".equals(s.getRole()))
                .filter(StudentDTO::isApproved)
                .collect(Collectors.toList());

        dataProvider = new ListDataProvider<>(students);
        grid.setDataProvider(dataProvider);
        applyFilter();
    }
}