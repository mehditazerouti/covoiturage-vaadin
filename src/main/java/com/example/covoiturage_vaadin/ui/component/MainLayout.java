package com.example.covoiturage_vaadin.ui.component;

import com.example.covoiturage_vaadin.application.services.SecurityContextService;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.ui.component.dialog.ProfileDialog;
import com.example.covoiturage_vaadin.ui.view.admin.AdminStudentCreationView;
import com.example.covoiturage_vaadin.ui.view.admin.AdminStudentView;
import com.example.covoiturage_vaadin.ui.view.admin.AdminWhitelistView;
import com.example.covoiturage_vaadin.ui.view.trip.MyBookingsView;
import com.example.covoiturage_vaadin.ui.view.admin.PendingStudentsView;
import com.example.covoiturage_vaadin.ui.view.trip.TripCreationView;
import com.example.covoiturage_vaadin.ui.view.trip.TripSearchView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Le layout principal modernisé.
 */
public class MainLayout extends AppLayout {

    private final StudentService studentService;
    private final SecurityContextService securityContextService;

    public MainLayout(StudentService studentService, SecurityContextService securityContextService) {
        this.studentService = studentService;
        this.securityContextService = securityContextService;
        
        // Configuration de base
        setPrimarySection(Section.DRAWER);
        createHeader();
        createDrawer();
        
        // Enlever le fond gris par défaut du drawer pour un look plus clean
        this.getElement().getStyle().set("--vaadin-app-layout-drawer-overlay-background-color", "white");
    }

    private void createHeader() {
        // Logo avec icône
        Icon logoIcon = VaadinIcon.CAR.create();
        logoIcon.setColor("var(--lumo-primary-color)");
        logoIcon.addClassName(LumoUtility.Margin.Right.SMALL);

        H1 logoText = new H1("Covoit' Étudiant");
        logoText.getStyle()
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("margin", "0");

        HorizontalLayout logoLayout = new HorizontalLayout(logoIcon, logoText);
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Bouton Profil (Rond et minimaliste)
        Button profileButton = new Button(VaadinIcon.USER.create());
        profileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        profileButton.setTooltipText("Mon Profil");
        profileButton.getStyle()
            .set("border-radius", "50%")
            .set("width", "40px")
            .set("height", "40px")
            .set("background-color", "var(--lumo-contrast-5pct)"); // Fond gris léger
            
        profileButton.addClickListener(e -> openProfileDialog());

        // Header Container
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logoLayout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logoLayout); // Pousse le profil à droite
        header.add(profileButton);
        
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.SMALL, LumoUtility.Padding.Horizontal.MEDIUM);
        
        // Style "Clean Header" : Blanc + Ombre douce
        header.getStyle()
            .set("background", "white")
            .set("box-shadow", "0 2px 10px rgba(0,0,0,0.03)")
            .set("border-bottom", "1px solid var(--lumo-contrast-5pct)");

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setPadding(true); // Un peu de padding autour du menu
        drawerContent.setSpacing(true);
        drawerContent.setSizeFull();

        // 1. Navigation Principale
        SideNav mainNav = new SideNav();
        mainNav.setLabel("Navigation");
        mainNav.addItem(new SideNavItem("Rechercher", TripSearchView.class, VaadinIcon.SEARCH.create()));
        mainNav.addItem(new SideNavItem("Proposer un trajet", TripCreationView.class, VaadinIcon.PLUS_CIRCLE.create()));
        mainNav.addItem(new SideNavItem("Mes réservations", MyBookingsView.class, VaadinIcon.BOOKMARK.create()));

        drawerContent.add(mainNav);

        // 2. Navigation Admin (Conditionnelle)
        if (isUserAdmin()) {
            SideNav adminNav = new SideNav();
            adminNav.setLabel("Administration");
            adminNav.setCollapsible(true); // On peut replier le menu admin
            
            adminNav.addItem(new SideNavItem("Annuaire", AdminStudentView.class, VaadinIcon.USERS.create()));
            adminNav.addItem(new SideNavItem("Créer étudiant", AdminStudentCreationView.class, VaadinIcon.PLUS_CIRCLE.create()));
            adminNav.addItem(new SideNavItem("Whitelist", AdminWhitelistView.class, VaadinIcon.MODAL_LIST.create()));
            
            // Badge pour les validations en attente (Optionnel mais classe)
            SideNavItem pendingItem = new SideNavItem("Validations", PendingStudentsView.class, VaadinIcon.HOURGLASS.create());
            adminNav.addItem(pendingItem);

            drawerContent.add(adminNav);
        }

        Scroller scroller = new Scroller(drawerContent);
        scroller.addClassName(LumoUtility.Padding.SMALL);
        
        // Pour pousser le bouton logout vers le bas
        VerticalLayout drawerContainer = new VerticalLayout(scroller);
        drawerContainer.setSizeFull();
        drawerContainer.setPadding(false);
        drawerContainer.setSpacing(false);
        
        // 3. Bouton Déconnexion (Intégré en bas)
        LogoutButton logout = new LogoutButton();
        
        // Container footer pour le bouton
        VerticalLayout footer = new VerticalLayout(logout);
        footer.setPadding(true);
        footer.getStyle().set("border-top", "1px solid var(--lumo-contrast-5pct)");

        addToDrawer(drawerContainer, footer);
    }

    private void openProfileDialog() {
        var usernameOpt = securityContextService.getCurrentUsername();
        if (usernameOpt.isEmpty()) return;

        var studentOpt = studentService.getStudentByUsername(usernameOpt.get());
        if (studentOpt.isPresent()) {
            new ProfileDialog(studentService, studentOpt.get().getId()).open();
        } else {
            Notification.show("Profil introuvable", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean isUserAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}