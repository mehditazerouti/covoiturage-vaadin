package com.example.covoiturage_vaadin.ui.view.admin;

import com.example.covoiturage_vaadin.application.dto.student.StudentDTO;
import com.example.covoiturage_vaadin.application.services.AuthenticationService;
import com.example.covoiturage_vaadin.application.services.SecurityContextService;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.example.covoiturage_vaadin.ui.component.SearchBar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/pending-students", layout = MainLayout.class)
@PageTitle("Étudiants en Attente - Admin")
@RolesAllowed("ADMIN")
public class PendingStudentsView extends VerticalLayout {

    private final StudentService studentService;
    private final AuthenticationService authService;
    private final SecurityContextService securityContext;

    private final Grid<StudentDTO> grid = new Grid<>(StudentDTO.class, false);
    private final SearchBar searchBar = new SearchBar("Rechercher...");
    private ListDataProvider<StudentDTO> dataProvider;

    public PendingStudentsView(StudentService studentService,
                              AuthenticationService authService,
                              SecurityContextService securityContext) {
        this.studentService = studentService;
        this.authService = authService;
        this.securityContext = securityContext;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // --- 1. HEADER ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
            .set("background", "white")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        
        H2 title = new H2("Validations en attente");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem");
        
        Span subtitle = new Span("Inscriptions nécessitant une approbation manuelle");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");
        
        titles.add(title, subtitle);

        header.add(titles);

        // --- 2. CONTENU ---
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(true);

        // --- 3. CARTE GRILLE ---
        VerticalLayout gridCard = new VerticalLayout();
        gridCard.setSizeFull();
        gridCard.setPadding(false);
        gridCard.setSpacing(false);
        gridCard.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "0 10px 40px rgba(0,0,0,0.06)")
            .set("overflow", "hidden");

        // Toolbar
        HorizontalLayout toolbar = new HorizontalLayout(searchBar);
        toolbar.setWidthFull();
        toolbar.setPadding(true);
        toolbar.setAlignItems(Alignment.CENTER);
        
        searchBar.setWidth("100%");
        searchBar.setMaxWidth("400px");
        searchBar.getStyle().set("margin", "0"); // Reset marge

        configureGrid();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle().set("border", "none");

        gridCard.add(toolbar, grid);
        mainContent.add(gridCard);
        add(header, mainContent);

        configureSearchBar();
        refreshGrid();
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

    private void configureGrid() {
        grid.addColumn(StudentDTO::getName).setHeader("Nom").setSortable(true).setAutoWidth(true);
        grid.addColumn(StudentDTO::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(StudentDTO::getStudentCode).setHeader("Code").setAutoWidth(true);
        
        grid.addColumn(student -> student.getCreatedAt() != null ? student.getCreatedAt().toLocalDate().toString() : "")
            .setHeader("Date").setAutoWidth(true);

        grid.addComponentColumn(student -> {
            Button approveBtn = new Button(VaadinIcon.CHECK.create());
            approveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            approveBtn.setTooltipText("Approuver");
            approveBtn.addClickListener(e -> confirmApprove(student));

            Button rejectBtn = new Button(VaadinIcon.CLOSE.create());
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            rejectBtn.setTooltipText("Rejeter");
            rejectBtn.addClickListener(e -> confirmReject(student));

            HorizontalLayout actions = new HorizontalLayout(approveBtn, rejectBtn);
            return actions;
        }).setHeader("Actions");
    }

    private void confirmApprove(StudentDTO student) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Approuver ?");
        dialog.setText("Ajouter " + student.getName() + " à la whitelist ?");
        dialog.setConfirmText("Oui, approuver");
        dialog.setConfirmButtonTheme("success primary");
        dialog.setCancelable(true);
        dialog.addConfirmListener(e -> {
            try {
                String admin = securityContext.getCurrentUsername().orElse("UNKNOWN");
                authService.approveStudentById(student.getId(), admin);
                Notification.show("Approuvé !", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid();
            } catch (Exception ex) { Notification.show("Erreur: " + ex.getMessage()); }
        });
        dialog.open();
    }

    private void confirmReject(StudentDTO student) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Rejeter ?");
        dialog.setText("Supprimer définitivement la demande de " + student.getName() + " ?");
        dialog.setConfirmText("Rejeter");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelable(true);
        dialog.addConfirmListener(e -> {
            try {
                studentService.deleteStudentById(student.getId());
                Notification.show("Supprimé.", 3000, Notification.Position.MIDDLE);
                refreshGrid();
            } catch (Exception ex) { Notification.show("Erreur: " + ex.getMessage()); }
        });
        dialog.open();
    }

    private void refreshGrid() {
        List<StudentDTO> pending = studentService.getAllStudents().stream().filter(s -> !s.isApproved()).collect(Collectors.toList());
        dataProvider = new ListDataProvider<>(pending);
        grid.setDataProvider(dataProvider);
        applyFilter();
    }
}