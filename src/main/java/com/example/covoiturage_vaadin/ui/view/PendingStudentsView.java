package com.example.covoiturage_vaadin.ui.view;

import com.example.covoiturage_vaadin.application.services.AuthenticationService;
import com.example.covoiturage_vaadin.application.services.SecurityContextService;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.domain.model.Student;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Vue d'administration pour valider ou rejeter les étudiants en attente.
 * Affiche uniquement les étudiants avec approved=false.
 */
@Route(value = "admin/pending-students", layout = MainLayout.class)
@PageTitle("Étudiants en Attente - Admin")
@RolesAllowed("ADMIN")
public class PendingStudentsView extends VerticalLayout {

    private final StudentService studentService;
    private final AuthenticationService authService;
    private final SecurityContextService securityContext;

    private final Grid<Student> grid = new Grid<>(Student.class, false);

    public PendingStudentsView(StudentService studentService,
                              AuthenticationService authService,
                              SecurityContextService securityContext) {
        this.studentService = studentService;
        this.authService = authService;
        this.securityContext = securityContext;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Titre
        H2 title = new H2("Étudiants en attente de validation");
        Span subtitle = new Span("Ces étudiants se sont inscrits avec un code non whitelisté");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Configuration de la grille
        configureGrid();

        add(title, subtitle, grid);

        // Charger les données
        refreshGrid();
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        // Colonne Nom
        grid.addColumn(Student::getName)
            .setHeader("Nom")
            .setSortable(true)
            .setAutoWidth(true);

        // Colonne Email
        grid.addColumn(Student::getEmail)
            .setHeader("Email")
            .setAutoWidth(true);

        // Colonne Code étudiant
        grid.addColumn(Student::getStudentCode)
            .setHeader("Code étudiant")
            .setAutoWidth(true);

        // Colonne Date
        grid.addColumn(student -> {
            if (student.getCreatedAt() != null) {
                return student.getCreatedAt().toLocalDate().toString();
            }
            return "";
        }).setHeader("Date inscription").setAutoWidth(true);

        // Colonne Actions
        grid.addComponentColumn(student -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            // Bouton Approuver
            Button approveBtn = new Button("Approuver", VaadinIcon.CHECK.create());
            approveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            approveBtn.addClickListener(e -> confirmApprove(student));

            // Bouton Rejeter
            Button rejectBtn = new Button("Rejeter", VaadinIcon.CLOSE.create());
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            rejectBtn.addClickListener(e -> confirmReject(student));

            actions.add(approveBtn, rejectBtn);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void confirmApprove(Student student) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Approuver l'étudiant");
        confirmDialog.setText(
            "Voulez-vous vraiment approuver l'étudiant suivant ?\n\n" +
            "Nom : " + student.getName() + "\n" +
            "Email : " + student.getEmail() + "\n" +
            "Code : " + student.getStudentCode() + "\n\n" +
            "→ Son code sera ajouté à la whitelist et son compte sera activé."
        );

        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Annuler");

        confirmDialog.setConfirmText("Approuver");
        confirmDialog.setConfirmButtonTheme("success primary");

        confirmDialog.addConfirmListener(event -> {
            try {
                String adminUsername = securityContext.getCurrentUsername().orElse("UNKNOWN");
                authService.approveStudent(student, adminUsername);

                Notification.show(
                    "✅ Étudiant approuvé avec succès ! Il peut maintenant se connecter.",
                    5000,
                    Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                refreshGrid();
            } catch (Exception e) {
                Notification.show("❌ Erreur : " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        confirmDialog.open();
    }

    private void confirmReject(Student student) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Rejeter l'étudiant");
        confirmDialog.setText(
            "Voulez-vous vraiment rejeter l'étudiant suivant ?\n\n" +
            "Nom : " + student.getName() + "\n" +
            "Email : " + student.getEmail() + "\n" +
            "Code : " + student.getStudentCode() + "\n\n" +
            "⚠️ Cette action est irréversible. Le compte sera supprimé définitivement."
        );

        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Annuler");

        confirmDialog.setConfirmText("Rejeter et supprimer");
        confirmDialog.setConfirmButtonTheme("error primary");

        confirmDialog.addConfirmListener(event -> {
            try {
                studentService.deleteStudent(student);

                Notification.show(
                    "✅ Étudiant rejeté et supprimé.",
                    3000,
                    Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_CONTRAST);

                refreshGrid();
            } catch (Exception e) {
                Notification.show("❌ Erreur : " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        confirmDialog.open();
    }

    private void refreshGrid() {
        // Filtrer pour n'afficher que les étudiants NON approuvés (pending)
        List<Student> pendingStudents = studentService.getAllStudents().stream()
            .filter(s -> !s.isApproved())
            .collect(Collectors.toList());

        grid.setItems(pendingStudents);

        // Message si aucun étudiant en attente
        if (pendingStudents.isEmpty()) {
            Span emptyMessage = new Span("✅ Aucun étudiant en attente de validation");
            emptyMessage.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic")
                .set("padding", "var(--lumo-space-m)");
            // Note : Vaadin Grid ne supporte pas directement les empty states,
            // mais on pourrait l'ajouter avec un layout conditionnel si nécessaire
        }
    }
}
