package com.example.covoiturage_vaadin.ui.component;


import com.example.covoiturage_vaadin.ui.view.AdminStudentCreationView;
import com.example.covoiturage_vaadin.ui.view.AdminWhitelistView;
import com.example.covoiturage_vaadin.ui.view.StudentView;
import com.example.covoiturage_vaadin.ui.view.TripCreationView;
import com.example.covoiturage_vaadin.ui.view.TripSearchView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;

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
        // Utilisation du composant SideNav (Vaadin 24+)
        SideNav nav = new SideNav();
        
        // Ajout des liens de navigation
        nav.addItem(new SideNavItem("Gestion Étudiants", StudentView.class, VaadinIcon.USERS.create()));
        nav.addItem(new SideNavItem("Rechercher", TripSearchView.class, VaadinIcon.SEARCH.create()));
        nav.addItem(new SideNavItem("Proposer", TripCreationView.class, VaadinIcon.CAR.create()));
        nav.addItem(new SideNavItem("Créer un étudiant", AdminStudentCreationView.class, VaadinIcon.PLUS_CIRCLE.create()));
        nav.addItem(new SideNavItem("Whitelist", AdminWhitelistView.class, VaadinIcon.PLUS_CIRCLE.create()));

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        // On place le bouton de déconnexion en bas de la sidebar
        LogoutButton logout = new LogoutButton();
        // On retire le style "fixed" du LogoutButton car il est maintenant dans un layout
        logout.getStyle().clear(); 
        logout.addClassNames(LumoUtility.Margin.MEDIUM);

        addToDrawer(scroller, logout);
    }
}