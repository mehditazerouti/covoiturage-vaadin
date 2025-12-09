package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.message.ContactDTO;
import com.example.covoiturage_vaadin.application.services.MessageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.List;

/**
 * Dialog pour envoyer un nouveau message à un contact.
 * Affiche la liste des contacts éligibles (personnes avec qui on a partagé un covoiturage).
 */
public class NewMessageDialog extends Dialog {

    private final MessageService messageService;
    private final Runnable onSuccess;

    private final Grid<ContactDTO> contactGrid = new Grid<>(ContactDTO.class);
    private final TextField searchField = new TextField();
    private final TextArea messageField = new TextArea();

    private ContactDTO selectedContact = null;
    private ListDataProvider<ContactDTO> dataProvider;

    public NewMessageDialog(MessageService messageService, Runnable onSuccess) {
        this.messageService = messageService;
        this.onSuccess = onSuccess;

        setHeaderTitle("Nouveau message");
        setWidth("600px");
        setHeight("70vh");

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);

        // Section recherche
        HorizontalLayout searchSection = createSearchSection();

        // Liste des contacts
        VerticalLayout contactSection = createContactSection();

        // Zone de message
        VerticalLayout messageSection = createMessageSection();

        content.add(searchSection, contactSection, messageSection);
        add(content);

        // Boutons de pied de page
        Button cancelBtn = new Button("Annuler", e -> close());
        Button sendBtn = new Button("Envoyer", e -> sendMessage());
        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(cancelBtn, sendBtn);

        // Charger les contacts
        loadContacts();
    }

    private HorizontalLayout createSearchSection() {
        HorizontalLayout section = new HorizontalLayout();
        section.setWidthFull();
        section.setAlignItems(FlexComponent.Alignment.CENTER);

        searchField.setPlaceholder("Rechercher un contact...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.addValueChangeListener(e -> applyFilter());

        section.add(searchField);
        return section;
    }

    private VerticalLayout createContactSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("flex-grow", "1");

        Span label = new Span("Sélectionnez un destinataire");
        label.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("margin-bottom", "var(--lumo-space-xs)");

        configureContactGrid();
        contactGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        contactGrid.setHeight("200px");

        section.add(label, contactGrid);
        return section;
    }

    private void configureContactGrid() {
        contactGrid.removeAllColumns();

        // Colonne Avatar + Nom
        contactGrid.addComponentColumn(contact -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);

            Icon avatar = VaadinIcon.USER.create();
            avatar.setSize("28px");
            avatar.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-color)")
                .set("border-radius", "50%")
                .set("padding", "4px");

            VerticalLayout info = new VerticalLayout();
            info.setPadding(false);
            info.setSpacing(false);

            Span name = new Span(contact.getName());
            name.getStyle().set("font-weight", "500");

            Span email = new Span(contact.getEmail());
            email.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-xs)");

            info.add(name, email);
            layout.add(avatar, info);

            return layout;
        }).setHeader("Contact").setFlexGrow(1);

        // Colonne contexte
        contactGrid.addColumn(contact -> contact.getTripContext() != null ? contact.getTripContext() : "")
            .setHeader("Covoiturage").setAutoWidth(true);

        // Sélection
        contactGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        contactGrid.addSelectionListener(e -> {
            selectedContact = e.getFirstSelectedItem().orElse(null);
            updateSelectedContactDisplay();
        });
    }

    private VerticalLayout createMessageSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        Span label = new Span("Message");
        label.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)");

        messageField.setPlaceholder("Écrivez votre message...");
        messageField.setWidthFull();
        messageField.setHeight("100px");
        messageField.setMaxLength(2000);

        section.add(label, messageField);
        return section;
    }

    private void updateSelectedContactDisplay() {
        // On pourrait ajouter un affichage du contact sélectionné si besoin
    }

    private void applyFilter() {
        if (dataProvider != null) {
            dataProvider.clearFilters();
            String searchTerm = searchField.getValue().toLowerCase().trim();
            if (!searchTerm.isEmpty()) {
                dataProvider.addFilter(contact ->
                    contact.getName().toLowerCase().contains(searchTerm) ||
                    contact.getEmail().toLowerCase().contains(searchTerm) ||
                    (contact.getTripContext() != null && contact.getTripContext().toLowerCase().contains(searchTerm))
                );
            }
        }
    }

    private void loadContacts() {
        List<ContactDTO> contacts = messageService.getContactableUsers();

        if (contacts.isEmpty()) {
            Div emptyState = new Div();
            emptyState.getElement().setProperty("innerHTML",
                "<div style='text-align: center; padding: 20px; color: var(--lumo-secondary-text-color);'>" +
                "<span style='font-size: 48px;'>&#128100;</span><br/>" +
                "<strong>Aucun contact disponible</strong><br/>" +
                "<span style='font-size: var(--lumo-font-size-s);'>Vous pourrez contacter les personnes avec qui vous aurez partagé un covoiturage.</span>" +
                "</div>");
            add(emptyState);
            contactGrid.setVisible(false);
            return;
        }

        dataProvider = new ListDataProvider<>(contacts);
        contactGrid.setDataProvider(dataProvider);
    }

    private void sendMessage() {
        if (selectedContact == null) {
            Notification.show("Veuillez sélectionner un destinataire", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        String content = messageField.getValue();
        if (content == null || content.trim().isEmpty()) {
            Notification.show("Le message ne peut pas être vide", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            messageService.sendMessage(selectedContact.getId(), content.trim());
            Notification.show("Message envoyé !", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            if (onSuccess != null) onSuccess.run();
            close();
        } catch (Exception ex) {
            Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
