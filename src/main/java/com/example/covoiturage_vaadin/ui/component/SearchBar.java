package com.example.covoiturage_vaadin.ui.component;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Composant réutilisable de barre de recherche.
 *
 * Caractéristiques :
 * - TextField avec icône de recherche
 * - Recherche en temps réel (ValueChangeMode.LAZY)
 * - Placeholder personnalisable
 * - Style cohérent avec l'application
 *
 * Utilisation :
 * <pre>
 * SearchBar searchBar = new SearchBar("Rechercher par nom, email ou code...");
 * searchBar.addValueChangeListener(e -> {
 *     String searchTerm = e.getValue();
 *     // Logique de filtrage
 * });
 * </pre>
 */
public class SearchBar extends TextField {

    /**
     * Constructeur avec placeholder par défaut.
     */
    public SearchBar() {
        this("Rechercher...");
    }

    /**
     * Constructeur avec placeholder personnalisé.
     *
     * @param placeholder Le texte du placeholder
     */
    public SearchBar(String placeholder) {
        super();

        // Configuration de base
        setPlaceholder(placeholder);
        setPrefixComponent(VaadinIcon.SEARCH.create());
        setClearButtonVisible(true);
        setWidthFull();

        // Recherche en temps réel avec délai (évite trop de requêtes)
        setValueChangeMode(ValueChangeMode.LAZY);
        setValueChangeTimeout(300); // 300ms de délai

        // Style
        getStyle()
            .set("max-width", "400px")
            .set("margin-bottom", "var(--lumo-space-m)");

        // Attributs d'accessibilité
        getElement().setAttribute("aria-label", "Champ de recherche");
    }

    /**
     * Constructeur avec placeholder et largeur max personnalisés.
     *
     * @param placeholder Le texte du placeholder
     * @param maxWidth La largeur maximale (ex: "500px", "100%")
     */
    public SearchBar(String placeholder, String maxWidth) {
        this(placeholder);
        getStyle().set("max-width", maxWidth);
    }

    /**
     * Retourne la valeur de recherche en minuscules (pour filtrage insensible à la casse).
     *
     * @return La valeur de recherche en lowercase, ou chaîne vide si null
     */
    public String getSearchValue() {
        String value = getValue();
        return value != null ? value.toLowerCase().trim() : "";
    }

    /**
     * Vérifie si la recherche est vide.
     *
     * @return true si le champ est vide ou ne contient que des espaces
     */
    public boolean isSearchEmpty() {
        return getSearchValue().isEmpty();
    }
}
