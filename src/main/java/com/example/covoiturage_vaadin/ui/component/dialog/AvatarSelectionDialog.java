package com.example.covoiturage_vaadin.ui.component.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AvatarSelectionDialog extends Dialog {

    private static final String[] AVAILABLE_AVATARS = {"USER", "MALE", "FEMALE"};

    private final Consumer<String> onSelect;
    private String selectedAvatar = "USER";
    private final Map<String, Div> avatarButtons = new HashMap<>();

    public AvatarSelectionDialog(Consumer<String> onSelect) {
        this.onSelect = onSelect;

        setHeaderTitle("Choisir un avatar");
        setWidth("450px"); // Un peu plus large pour l'espacement

        VerticalLayout content = createContent();
        
        // Pied de page modernisé
        Button cancelButton = new Button("Annuler", e -> close());
        Button selectButton = new Button("Valider", e -> handleSelect());
        selectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        getFooter().add(cancelButton, selectButton);
        add(content);
    }

    private VerticalLayout createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER); // Tout centrer

        // Petit texte explicatif plus discret
        Span hint = new Span("Sélectionnez l'icône qui vous correspond le mieux");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)");
        hint.getStyle().set("font-size", "var(--lumo-font-size-s)");

        HorizontalLayout avatarGrid = new HorizontalLayout();
        avatarGrid.setSpacing(true);
        avatarGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        avatarGrid.getStyle().set("padding", "20px 0"); // Espace pour l'animation

        for (String avatarName : AVAILABLE_AVATARS) {
            Div avatarButton = createAvatarButton(avatarName);
            avatarButtons.put(avatarName, avatarButton);
            avatarGrid.add(avatarButton);
        }

        // Initialisation de l'état visuel correct
        updateVisualSelection();

        layout.add(hint, avatarGrid);
        return layout;
    }

    private Div createAvatarButton(String avatarName) {
        VaadinIcon vaadinIcon = VaadinIcon.valueOf(avatarName);
        Icon icon = vaadinIcon.create();
        icon.setSize("40px");
        // On s'assure que l'icône elle-même n'a aucune marge parasite
        icon.getStyle().set("margin", "0");

        // On utilise un Div au lieu d'un Button pour un contrôle total
        Div avatarContainer = new Div(icon);
        
        avatarContainer.getStyle()
            // --- DIMENSIONS & FORME ---
            .set("width", "90px")
            .set("height", "90px")
            .set("border-radius", "50%")
            
            // --- CENTRAGE PARFAIT (Flexbox sans interférence) ---
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            
            // --- INTERACTIVITÉ ---
            .set("cursor", "pointer")  // La main au survol
            .set("user-select", "none") // Empêche de sélectionner l'icône comme du texte
            .set("box-sizing", "border-box") // Important pour que la bordure ne change pas la taille
            
            // --- ESTHÉTIQUE ---
            .set("border", "3px solid transparent")
            .set("transition", "all 0.2s ease-in-out");

        // Rendre le Div cliquable
        avatarContainer.addClickListener(e -> selectAvatar(avatarName));

        return avatarContainer;
    }

    private void selectAvatar(String avatarName) {
        this.selectedAvatar = avatarName;
        updateVisualSelection();
    }

    /**
     * Met à jour le style de tous les boutons en fonction de la sélection actuelle.
     * Cette méthode centralise la logique visuelle.
     */
    private void updateVisualSelection() {
        avatarButtons.forEach((name, div) -> {
            boolean isSelected = name.equals(selectedAvatar);
            
            // On modifie directement le style du Div
            if (isSelected) {
                div.getStyle()
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-color)")
                    .set("border-color", "var(--lumo-primary-color)")
                    .set("transform", "scale(1.1)")
                    .set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)"); // Légère ombre portée
            } else {
                div.getStyle()
                    .set("background-color", "var(--lumo-contrast-5pct)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("border-color", "transparent")
                    .set("transform", "scale(1)")
                    .set("box-shadow", "none");
            }
        });
    }

    private void handleSelect() {
        if (onSelect != null) {
            onSelect.accept(selectedAvatar);
        }
        close();
    }
    
    public void setCurrentAvatar(String avatar) {
        this.selectedAvatar = avatar != null ? avatar : "USER";
        // Si le dialogue est déjà ouvert, on met à jour visuellement
        if (avatarButtons.size() > 0) {
            updateVisualSelection();
        }
    }
}