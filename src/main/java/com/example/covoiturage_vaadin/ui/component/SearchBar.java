package com.example.covoiturage_vaadin.ui.component;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Barre de recherche style "Capsule Blanche".
 * Force le style via les variables CSS du composant.
 */
public class SearchBar extends TextField {

    public SearchBar() {
        this("Rechercher...");
    }

    public SearchBar(String placeholder) {
        super();
        setPlaceholder(placeholder);
        setPrefixComponent(VaadinIcon.SEARCH.create());
        setClearButtonVisible(true);
        setWidthFull();

        setValueChangeMode(ValueChangeMode.LAZY);
        setValueChangeTimeout(300);

        addClassName("search-bar-capsule");

        // --- STYLE FORCÉ ---
        // On modifie les variables CSS internes du composant TextField
        getStyle()
            // 1. Force le fond à BLANC (écrase le gris par défaut)
            
            // 2. Force une bordure très arrondie (Capsule)
            .set("--vaadin-input-field-border-width", "1px") // Petite bordure fine
            .set("--vaadin-input-field-border-color", "var(--lumo-contrast-10pct)")
            .set("--lumo-border-radius-m", "30px") // Rayon d'arrondi local
            
            // 3. Ajoute l'ombre sur le champ lui-même
            .set("box-shadow", "0 4px 10px rgba(0,0,0,0.05)")
            
            // 4. Ajustements de taille
            .set("padding", "0") // Une bonne hauteur pour une searchbar
            .set("border-radius", "30px");
    }

    public SearchBar(String placeholder, String maxWidth) {
        this(placeholder);
        setMaxWidth(maxWidth);
    }

    public String getSearchValue() {
        String value = getValue();
        return value != null ? value.toLowerCase().trim() : "";
    }

    public boolean isSearchEmpty() {
        return getSearchValue().isEmpty();
    }
}