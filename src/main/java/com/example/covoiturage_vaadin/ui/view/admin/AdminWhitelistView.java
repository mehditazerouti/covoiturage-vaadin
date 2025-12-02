package com.example.covoiturage_vaadin.ui.view.admin;

import com.example.covoiturage_vaadin.application.services.AllowedStudentCodeService;
import com.example.covoiturage_vaadin.application.services.SecurityContextService;
import com.example.covoiturage_vaadin.domain.model.AllowedStudentCode;
import com.example.covoiturage_vaadin.ui.component.dialog.ConfirmDeleteDialog;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.example.covoiturage_vaadin.ui.component.SearchBar;
import com.example.covoiturage_vaadin.ui.component.dialog.WhitelistCodeDialog;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "admin/whitelist", layout = MainLayout.class)
@PageTitle("Gestion Whitelist - Admin")
@RolesAllowed("ADMIN")
public class AdminWhitelistView extends VerticalLayout {

    private final AllowedStudentCodeService codeService;
    private final SecurityContextService securityContext;

    private final Grid<AllowedStudentCode> grid = new Grid<>(AllowedStudentCode.class, false);
    private final SearchBar searchBar = new SearchBar("Rechercher par code étudiant...");
    private final Button addButton = new Button("Ajouter un code", VaadinIcon.PLUS.create());
    private ListDataProvider<AllowedStudentCode> dataProvider;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminWhitelistView(AllowedStudentCodeService codeService,
                             SecurityContextService securityContext) {
        this.codeService = codeService;
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

        H2 title = new H2("Whitelist");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem");
    

        header.add(title);

        // --- 2. CONTENU PRINCIPAL ---
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

        // --- BARRE D'OUTILS ---
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setPadding(true);
        
        // CORRECTION ALIGNEMENT : Centre verticalement et écarte horizontalement
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Config SearchBar
        searchBar.setMaxWidth("400px");
        // Important : reset des marges pour éviter le décalage
        searchBar.getStyle().set("margin", "0");

        // Config Bouton
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openAddCodeDialog());
        // Important : reset des marges
        addButton.getStyle().set("margin", "0").set("cursor", "pointer");

        toolbar.add(searchBar, addButton);

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
                dataProvider.addFilter(code -> {
                    String studentCode = code.getStudentCode().toLowerCase();
                    String createdBy = code.getCreatedBy() != null ? code.getCreatedBy().toLowerCase() : "";
                    String usedBy = (code.isUsed() && code.getUsedBy() != null)
                        ? code.getUsedBy().getName().toLowerCase() : "";
                    return studentCode.contains(searchTerm) || createdBy.contains(searchTerm) || usedBy.contains(searchTerm);
                });
            }
        }
    }

    private void configureGrid() {
        grid.addColumn(AllowedStudentCode::getStudentCode).setHeader("Code").setSortable(true).setAutoWidth(true);

        grid.addComponentColumn(code -> {
            Span badge = new Span(code.isUsed() ? "Utilisé" : "Libre");
            badge.getElement().getThemeList().add(code.isUsed() ? "badge success" : "badge contrast");
            return badge;
        }).setHeader("État").setAutoWidth(true);

        grid.addColumn(code -> code.isUsed() && code.getUsedBy() != null ? code.getUsedBy().getName() : "-")
            .setHeader("Utilisé par").setAutoWidth(true);

        grid.addColumn(AllowedStudentCode::getCreatedBy).setHeader("Créateur").setAutoWidth(true);

        grid.addColumn(code -> code.getCreatedAt() != null ? code.getCreatedAt().format(DATE_FORMATTER) : "")
            .setHeader("Date").setAutoWidth(true);

        grid.addComponentColumn(code -> {
            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.setEnabled(!code.isUsed());
            deleteBtn.addClickListener(e -> {
                new ConfirmDeleteDialog("Supprimer", "Voulez-vous supprimer le code " + code.getStudentCode() + " ?", 
                    () -> codeService.deleteCode(code), this::refreshGrid).open();
            });
            return deleteBtn;
        }).setHeader("Actions");
    }

    private void openAddCodeDialog() {
        String username = securityContext.getCurrentUsername().orElse("UNKNOWN");
        new WhitelistCodeDialog(codeService, username, this::refreshGrid).open();
    }

    private void refreshGrid() {
        dataProvider = new ListDataProvider<>(codeService.findAll());
        grid.setDataProvider(dataProvider);
        applyFilter();
    }
}