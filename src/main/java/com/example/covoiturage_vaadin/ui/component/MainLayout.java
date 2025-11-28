package com.example.covoiturage_vaadin.ui.component;


import com.example.covoiturage_vaadin.ui.view.AdminStudentCreationView;
import com.example.covoiturage_vaadin.ui.view.AdminStudentView;
import com.example.covoiturage_vaadin.ui.view.AdminWhitelistView;
import com.example.covoiturage_vaadin.ui.view.MyBookingsView;
import com.example.covoiturage_vaadin.ui.view.PendingStudentsView;
import com.example.covoiturage_vaadin.ui.view.TripCreationView;
import com.example.covoiturage_vaadin.ui.view.TripSearchView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Le layout principal qui contient la Sidebar et le Header.
 * Toutes les vues utiliseront ce layout via @Route(layout = MainLayout.class)
 */
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Covoit' Étudiant");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM,
            LumoUtility.FontWeight.BOLD
        );

        // Le Toggle est le bouton "hamburger" pour ouvrir/fermer le menu sur mobile
        var header = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(new DrawerToggle(), logo);
        header.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
            LumoUtility.Padding.Vertical.NONE,
            LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);

        // Section principale (pour tous les utilisateurs)
        SideNav mainNav = new SideNav();
        mainNav.addItem(new SideNavItem("Rechercher Trajet", TripSearchView.class, VaadinIcon.SEARCH.create()));
        mainNav.addItem(new SideNavItem("Proposer Trajet", TripCreationView.class, VaadinIcon.CAR.create()));
        mainNav.addItem(new SideNavItem("Mes Réservations", MyBookingsView.class, VaadinIcon.BOOKMARK.create()));

        drawerContent.add(mainNav);

        // Section administration (uniquement pour les admins)
        if (isUserAdmin()) {
            Hr separator = new Hr();
            separator.getStyle().set("margin", "var(--lumo-space-m) 0");

            Span adminLabel = new Span("Administration");
            adminLabel.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("padding", "0 var(--lumo-space-m)")
                .set("margin", "var(--lumo-space-s) 0");

            SideNav adminNav = new SideNav();
            adminNav.addItem(new SideNavItem("Annuaire Étudiants", AdminStudentView.class, VaadinIcon.USERS.create()));
            adminNav.addItem(new SideNavItem("Créer un Étudiant", AdminStudentCreationView.class, VaadinIcon.USER_CARD.create()));
            adminNav.addItem(new SideNavItem("Codes Étudiants", AdminWhitelistView.class, VaadinIcon.MODAL_LIST.create()));
            adminNav.addItem(new SideNavItem("Étudiants en Attente", PendingStudentsView.class, VaadinIcon.HOURGLASS.create()));

            drawerContent.add(separator, adminLabel, adminNav);
        }

        Scroller scroller = new Scroller(drawerContent);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        // Bouton de déconnexion en bas de la sidebar
        LogoutButton logout = new LogoutButton();
        logout.getStyle().clear();
        logout.addClassNames(LumoUtility.Margin.MEDIUM);

        addToDrawer(scroller, logout);
    }

    /**
     * Vérifie si l'utilisateur connecté a le rôle ADMIN
     */
    private boolean isUserAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
    }
}