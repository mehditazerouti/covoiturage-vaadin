package com.example.covoiturage_vaadin.ui.component.badge;

import com.vaadin.flow.component.html.Span;

public class TripTypeBadge extends Span {

    public TripTypeBadge(boolean isRegular) {
        // Ajout d'une petite icône pour le visuel
        if (isRegular) {
            setText("Régulier");
            getElement().getThemeList().add("badge success pill"); // Vert + Rond
            // Optionnel : Ajouter une icône de répétition via API Element si voulu
        } else {
            setText("Ponctuel");
            getElement().getThemeList().add("badge contrast pill"); // Gris + Rond
        }
        
        getStyle()
            .set("padding", "0.3em 0.8em")
            .set("font-weight", "500");
    }
}