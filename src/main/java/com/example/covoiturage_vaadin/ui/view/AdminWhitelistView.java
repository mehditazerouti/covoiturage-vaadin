package com.example.covoiturage_vaadin.ui.view;

import com.example.covoiturage_vaadin.application.services.AllowedStudentCodeService;
import com.example.covoiturage_vaadin.application.services.SecurityContextService;
import com.example.covoiturage_vaadin.domain.model.AllowedStudentCode;
import com.example.covoiturage_vaadin.ui.component.ConfirmDeleteDialog;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.example.covoiturage_vaadin.ui.component.WhitelistCodeDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;

/**
 * Vue d'administration pour gérer la whitelist des codes étudiants.
 * Accessible uniquement aux administrateurs.
 */
@Route(value = "admin/whitelist", layout = MainLayout.class)
@PageTitle("Gestion Whitelist - Admin")
@RolesAllowed("ADMIN")
public class AdminWhitelistView extends VerticalLayout {

    private final AllowedStudentCodeService codeService;
    private final SecurityContextService securityContext;

    private final Grid<AllowedStudentCode> grid = new Grid<>(AllowedStudentCode.class, false);
    private final Button addButton = new Button("Ajouter un code", VaadinIcon.PLUS.create());

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminWhitelistView(AllowedStudentCodeService codeService,
                             SecurityContextService securityContext) {
        this.codeService = codeService;
        this.securityContext = securityContext;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Titre
        H2 title = new H2("Gestion des codes étudiants autorisés");

        // Configuration de la grille
        configureGrid();

        // Barre d'outils
        HorizontalLayout toolbar = createToolbar();

        add(title, toolbar, grid);

        // Charger les données
        refreshGrid();
    }

    private HorizontalLayout createToolbar() {
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openAddCodeDialog());

        HorizontalLayout toolbar = new HorizontalLayout(addButton);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        // Colonne Code étudiant
        grid.addColumn(AllowedStudentCode::getStudentCode)
            .setHeader("Code étudiant")
            .setSortable(true)
            .setAutoWidth(true);

        // Colonne Utilisé
        grid.addComponentColumn(code -> {
            Span badge = new Span(code.isUsed() ? "Oui" : "Non");
            if (code.isUsed()) {
                badge.getElement().getThemeList().add("badge success");
            } else {
                badge.getElement().getThemeList().add("badge");
            }
            return badge;
        }).setHeader("Utilisé").setAutoWidth(true);

        // Colonne Étudiant (si utilisé)
        grid.addColumn(code -> {
            if (code.isUsed() && code.getUsedBy() != null) {
                return code.getUsedBy().getName();
            }
            return "-";
        }).setHeader("Utilisé par").setAutoWidth(true);

        // Colonne Créé par
        grid.addColumn(AllowedStudentCode::getCreatedBy)
            .setHeader("Ajouté par")
            .setAutoWidth(true);

        // Colonne Date de création
        grid.addColumn(code -> {
            if (code.getCreatedAt() != null) {
                return code.getCreatedAt().format(DATE_FORMATTER);
            }
            return "";
        }).setHeader("Date création").setAutoWidth(true);

        // Colonne Actions
        grid.addComponentColumn(code -> {
            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            deleteBtn.setEnabled(!code.isUsed()); // Désactivé si déjà utilisé
            deleteBtn.setTooltipText(code.isUsed() ? "Impossible de supprimer un code utilisé" : "Supprimer");
            deleteBtn.addClickListener(e -> {
                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                    "Supprimer le code",
                    "Voulez-vous vraiment supprimer le code \"" + code.getStudentCode() + "\" ?",
                    () -> codeService.deleteCode(code),
                    this::refreshGrid
                );
                dialog.open();
            });
            return deleteBtn;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void openAddCodeDialog() {
        String username = securityContext.getCurrentUsername().orElse("UNKNOWN");
        WhitelistCodeDialog dialog = new WhitelistCodeDialog(codeService, username, this::refreshGrid);
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(codeService.findAll());
    }
}
