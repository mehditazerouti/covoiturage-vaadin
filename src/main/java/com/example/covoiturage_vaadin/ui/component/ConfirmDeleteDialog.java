package com.example.covoiturage_vaadin.ui.component;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Dialog de confirmation générique pour les suppressions.
 * Affiche un dialog avec un message personnalisé et gère les erreurs automatiquement.
 */
public class ConfirmDeleteDialog extends ConfirmDialog {

    /**
     * Crée un dialog de confirmation de suppression.
     *
     * @param header Titre du dialog (ex: "Supprimer l'étudiant")
     * @param text Message du dialog (ex: "Voulez-vous vraiment supprimer...")
     * @param onConfirm Action à exécuter en cas de confirmation
     * @param onSuccess Callback optionnel à exécuter après succès
     */
    public ConfirmDeleteDialog(String header, String text, DeleteAction onConfirm, Runnable onSuccess) {
        setHeader(header);
        setText(text);

        setCancelable(true);
        setCancelText("Annuler");

        setConfirmText("Supprimer");
        setConfirmButtonTheme("error primary");

        addConfirmListener(event -> {
            try {
                onConfirm.execute();
                Notification.show("✅ Suppression effectuée avec succès", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {
                Notification.show("❌ Erreur : " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    /**
     * Interface fonctionnelle pour l'action de suppression.
     * Permet de lancer des exceptions qui seront gérées automatiquement.
     */
    @FunctionalInterface
    public interface DeleteAction {
        void execute() throws Exception;
    }
}
