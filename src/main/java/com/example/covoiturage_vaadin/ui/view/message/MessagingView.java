package com.example.covoiturage_vaadin.ui.view.message;

import com.example.covoiturage_vaadin.application.dto.message.ConversationDTO;
import com.example.covoiturage_vaadin.application.services.MessageService;
import com.example.covoiturage_vaadin.ui.component.MainLayout;
import com.example.covoiturage_vaadin.ui.component.SearchBar;
import com.example.covoiturage_vaadin.ui.component.dialog.ConversationDialog;
import com.example.covoiturage_vaadin.ui.component.dialog.NewMessageDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Route(value = "messages", layout = MainLayout.class)
@PageTitle("Messagerie - Covoiturage")
@PermitAll
public class MessagingView extends VerticalLayout {

    private final MessageService messageService;
    private final Grid<ConversationDTO> grid = new Grid<>(ConversationDTO.class);
    private final SearchBar searchBar = new SearchBar("Rechercher une conversation...");
    private ListDataProvider<ConversationDTO> dataProvider;

    public MessagingView(MessageService messageService) {
        this.messageService = messageService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // --- 1. HEADER ---
        HorizontalLayout header = createHeader();

        // --- 2. CONTENU PRINCIPAL ---
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setMaxWidth("900px");
        mainContent.getStyle().set("margin", "0 auto");

        // --- 3. BARRE DE RECHERCHE ---
        HorizontalLayout searchSection = new HorizontalLayout(searchBar);
        searchSection.setWidthFull();
        searchSection.setPadding(false);
        searchSection.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        searchBar.setWidthFull();
        searchBar.addValueChangeListener(e -> applyFilter());

        // --- 4. CARTE DE GRILLE ---
        VerticalLayout gridCard = new VerticalLayout();
        gridCard.setSizeFull();
        gridCard.setPadding(false);
        gridCard.setSpacing(false);
        gridCard.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "0 10px 40px rgba(0,0,0,0.06)")
            .set("overflow", "hidden");

        configureGrid();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle().set("border", "none");

        gridCard.add(grid);
        mainContent.add(searchSection, gridCard);
        add(header, mainContent);

        updateList();
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.getStyle()
            .set("background", "white")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");

        // Titre + Badge non-lus
        HorizontalLayout titleSection = new HorizontalLayout();
        titleSection.setAlignItems(Alignment.CENTER);
        titleSection.setSpacing(true);

        H2 title = new H2("Messagerie");
        title.getStyle().set("margin", "0").set("font-size", "1.5rem");

        int unreadCount = messageService.getTotalUnreadCount();
        if (unreadCount > 0) {
            Span badge = new Span(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
            badge.getElement().getThemeList().add("badge error pill");
            badge.getStyle().set("margin-left", "var(--lumo-space-s)");
            titleSection.add(title, badge);
        } else {
            titleSection.add(title);
        }

        // Bouton Nouveau message
        Button newMessageBtn = new Button("Nouveau message", new Icon(VaadinIcon.PLUS));
        newMessageBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newMessageBtn.addClickListener(e -> openNewMessageDialog());

        header.add(titleSection, newMessageBtn);
        return header;
    }

    private void configureGrid() {
        grid.removeAllColumns();

        // Colonne Avatar + Nom
        grid.addComponentColumn(conv -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            layout.setSpacing(true);

            // Avatar
            Icon avatar = VaadinIcon.USER.create();
            avatar.setSize("32px");
            avatar.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-color)")
                .set("border-radius", "50%")
                .set("padding", "6px");

            // Nom
            Span name = new Span(conv.getOtherParticipant().getName());
            name.getStyle().set("font-weight", "500");

            layout.add(avatar, name);
            return layout;
        }).setHeader("Contact").setFlexGrow(1).setAutoWidth(true);

        // Colonne Dernier message
        grid.addComponentColumn(conv -> {
            String preview = conv.getLastMessagePreview();
            if (preview == null || preview.isEmpty()) {
                preview = "Aucun message";
            }
            if (conv.isLastMessageFromMe()) {
                preview = "Vous : " + preview;
            }

            Span previewSpan = new Span(preview);
            previewSpan.getStyle()
                .set("color", conv.hasUnreadMessages() ? "var(--lumo-body-text-color)" : "var(--lumo-secondary-text-color)")
                .set("font-weight", conv.hasUnreadMessages() ? "500" : "400")
                .set("max-width", "300px")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");

            return previewSpan;
        }).setHeader("Dernier message").setFlexGrow(2).setAutoWidth(true);

        // Colonne Date
        grid.addColumn(conv -> formatDate(conv.getLastMessageAt()))
            .setHeader("Date").setAutoWidth(true);

        // Colonne Non-lus
        grid.addComponentColumn(conv -> {
            if (conv.hasUnreadMessages()) {
                Span badge = new Span(String.valueOf(conv.getUnreadCount()));
                badge.getElement().getThemeList().add("badge error pill small");
                return badge;
            }
            return new Span();
        }).setHeader("").setAutoWidth(true).setWidth("60px");

        // Colonne Actions
        grid.addComponentColumn(conv -> {
            Button viewBtn = new Button("Voir", new Icon(VaadinIcon.ENVELOPE_OPEN));
            viewBtn.getStyle().set("cursor","pointer");
            // viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
            viewBtn.addClickListener(e -> openConversation(conv));
            return viewBtn;
        }).setHeader("").setAutoWidth(true);

        // Click sur une ligne ouvre la conversation
        grid.addItemClickListener(e -> openConversation(e.getItem()));
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());

        if (daysBetween == 0) {
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (daysBetween == 1) {
            return "Hier";
        } else if (daysBetween < 7) {
            return dateTime.format(DateTimeFormatter.ofPattern("EEEE"));
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    private void openConversation(ConversationDTO conversation) {
        ConversationDialog dialog = new ConversationDialog(messageService, conversation.getId(), this::updateList);
        dialog.open();
    }

    private void openNewMessageDialog() {
        NewMessageDialog dialog = new NewMessageDialog(messageService, this::updateList);
        dialog.open();
    }

    private void applyFilter() {
        if (dataProvider != null) {
            dataProvider.clearFilters();
            String searchTerm = searchBar.getSearchValue();
            if (!searchBar.isSearchEmpty()) {
                dataProvider.addFilter(conv ->
                    conv.getOtherParticipant().getName().toLowerCase().contains(searchTerm) ||
                    conv.getOtherParticipant().getEmail().toLowerCase().contains(searchTerm) ||
                    (conv.getLastMessagePreview() != null && conv.getLastMessagePreview().toLowerCase().contains(searchTerm))
                );
            }
        }
    }

    private void updateList() {
        List<ConversationDTO> conversations = messageService.getMyConversations();
        dataProvider = new ListDataProvider<>(conversations);
        grid.setDataProvider(dataProvider);
        applyFilter();
    }
}
