package com.example.covoiturage_vaadin.ui.component.badge;

import com.vaadin.flow.component.html.Span;

/**
 * Composant réutilisable pour afficher un badge de type de trajet.
 * Affiche "Régulier" (vert) ou "Ponctuel" (gris).
 */
public class TripTypeBadge extends Span {

    public TripTypeBadge(boolean isRegular) {
        setText(isRegular ? "Régulier" : "Ponctuel");

        if (isRegular) {
            getElement().getThemeList().add("badge success");
        } else {
            getElement().getThemeList().add("badge");
        }
    }
}
