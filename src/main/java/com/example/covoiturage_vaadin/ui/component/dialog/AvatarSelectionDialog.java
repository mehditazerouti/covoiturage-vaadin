package com.example.covoiturage_vaadin.ui.component.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Consumer;

/**
 * Dialog de sélection d'avatar parmi les icônes Vaadin disponibles.
 *
 * Utilisation :
 * <pre>
 * AvatarSelectionDialog dialog = new AvatarSelectionDialog(selectedAvatar -> {
 *     // Traitement de l'avatar sélectionné
 *     studentService.updateAvatar(studentId, selectedAvatar);
 * });
 * dialog.open();
 * </pre>
 */
public class AvatarSelectionDialog extends Dialog {

    // Avatars disponibles (icônes Vaadin)
    private static final String[] AVAILABLE_AVATARS = {"USER", "MALE", "FEMALE"};

    private final Consumer<String> onSelect;
    private String selectedAvatar = "USER";

    public AvatarSelectionDialog(Consumer<String> onSelect) {
        this.onSelect = onSelect;

        setHeaderTitle("Choisir un avatar");
        setWidth("400px");

        // Contenu
        VerticalLayout content = createContent();

        // Boutons
        Button selectButton = new Button("Sélectionner", e -> handleSelect());
        selectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> close());

        getFooter().add(cancelButton, selectButton);
        add(content);
    }

    private VerticalLayout createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H3 subtitle = new H3("Sélectionnez votre avatar :");
        subtitle.getStyle().set("margin-top", "0");

        HorizontalLayout avatarGrid = new HorizontalLayout();
        avatarGrid.setSpacing(true);
        avatarGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        for (String avatarName : AVAILABLE_AVATARS) {
            Button avatarButton = createAvatarButton(avatarName);
            avatarGrid.add(avatarButton);
        }

        layout.add(subtitle, avatarGrid);
        return layout;
    }

    private Button createAvatarButton(String avatarName) {
        VaadinIcon vaadinIcon = VaadinIcon.valueOf(avatarName);
        Icon icon = vaadinIcon.create();
        icon.setSize("48px");

        Button button = new Button(icon);
        button.getStyle()
            .set("width", "80px")
            .set("height", "80px")
            .set("border-radius", "50%");

        // Style par défaut (USER sélectionné)
        if (avatarName.equals(selectedAvatar)) {
            button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }

        button.addClickListener(e -> selectAvatar(avatarName, button));

        return button;
    }

    private void selectAvatar(String avatarName, Button clickedButton) {
        selectedAvatar = avatarName;

        // Mettre à jour les styles de tous les boutons
        getChildren()
            .filter(component -> component instanceof VerticalLayout)
            .flatMap(layout -> layout.getChildren())
            .filter(component -> component instanceof HorizontalLayout)
            .flatMap(hLayout -> hLayout.getChildren())
            .filter(component -> component instanceof Button)
            .forEach(button -> {
                Button btn = (Button) button;
                btn.getThemeNames().clear();
                if (btn == clickedButton) {
                    btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                } else {
                    btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                }
            });
    }

    private void handleSelect() {
        if (onSelect != null) {
            onSelect.accept(selectedAvatar);
        }
        close();
    }

    /**
     * Définit l'avatar actuellement sélectionné.
     *
     * @param avatar Nom de l'avatar (USER, MALE, FEMALE)
     */
    public void setCurrentAvatar(String avatar) {
        this.selectedAvatar = avatar != null ? avatar : "USER";
    }
}
