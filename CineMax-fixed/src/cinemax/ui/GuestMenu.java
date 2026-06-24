package cinemax.ui;
import cinemax.model.Proiezione;
import cinemax.service.GestoreApp;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Menu per gli utenti ospite (non autenticati) di CineMax
 *
 * Permette a chiunque di consultare il palinsesto senza effettuare il
 * login: cercare proiezioni per vari criteri, visualizzarne i dettagli e,
 * se interessato, raggiungere la schermata di registrazione
 *
 * Il flusso standard è:
 * <ol>
 *   <li>L'utente indica (anche parzialmente) un titolo di film dal menu
 *       principale.</li>
 *   <li>Viene mostrato l'elenco delle proiezioni corrispondenti.</li>
 *   <li>Da qui l'ospite può raffinare la ricerca, visualizzare i dettagli
 *       di una proiezione specifica o tornare al menu principale.</li>
 * </ol>
 *
 * @author Serrao Isabella (766930) [VA]
 */
public final class GuestMenu {

    /** Costruttore privato: classe non istanziabile */
    private GuestMenu() {}

   // =========================================================================
   // MENU PRINCIPALE (ospite)
   // =========================================================================

    /**
     * Avvia il menu ospite a partire da una ricerca per titolo già effettuata
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     * @param titoloIniziale --> titolo (anche parziale) inserito al menu principale
     */
    public static void mostra(Scanner sc, GestoreApp app, String titoloIniziale) {
        List<Proiezione> risultati = app.cercaProiezioni(
                titoloIniziale, null, null, null, -1, -1);

        while (true) {
            MenuUI.stampaIntestazione("AREA OSPITE — PALINSESTO");

            if (risultati.isEmpty()) {
                MenuUI.stampaInfo("Nessuna proiezione trovata per: \"" + titoloIniziale + "\".");
            } else {
                MenuUI.stampaInfo("Proiezioni trovate per: \"" + titoloIniziale + "\"");
                MenuUI.stampaListaProiezioni(risultati, app);
            }

            MenuUI.stampaOpzioni(
                    "Cerca proiezioni (con filtri)",
                    "Visualizza dettagli di una proiezione",
                    "Registrati come cliente"
            );
            MenuUI.stampaOpzioneZero("Torna al menu principale");

            int scelta = MenuUI.leggiScelta(sc, "Scelta:");

            switch (scelta) {
                case 1:
                    risultati = cercaConFiltri(sc, app);
                    break;
                case 2:
                    visualizzaDettagli(sc, app, risultati);
                    break;
                case 3:
                    LoginUI.eseguiRegistrazione(sc, app);
                    return; // dopo la registrazione torna al menu principale
                case 0:
                    return;
                default:
                    MenuUI.stampaErrore("Opzione non valida.");
            }
        }
    }

   // =========================================================================
   // CERCA PROIEZIONI CON FILTRI
   // =========================================================================

    /**
     * Mostra la schermata di ricerca avanzata con filtri opzionali combinabili
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     * @return lista di proiezioni corrispondenti ai filtri inseriti
     */
    private static List<Proiezione> cercaConFiltri(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("CERCA PROIEZIONI");
        MenuUI.stampaInfo("Inserisci i criteri di ricerca (Invio = nessun filtro).");
        System.out.println();

        String titolo  = MenuUI.leggiStringaOpzionale(sc, "Titolo del film:");
        String genere  = MenuUI.leggiStringaOpzionale(sc, "Genere (es. Azione, Commedia):");
        LocalDate dMin = MenuUI.leggiDataOpzionale(sc, "Data minima");
        LocalDate dMax = MenuUI.leggiDataOpzionale(sc, "Data massima");
        double pMin    = MenuUI.leggiPrezzoOpzionale(sc, "Prezzo minimo (€)");
        double pMax    = MenuUI.leggiPrezzoOpzionale(sc, "Prezzo massimo (€)");

        List<Proiezione> risultati = app.cercaProiezioni(
                titolo, genere, dMin, dMax, pMin, pMax);

        MenuUI.stampaIntestazione("RISULTATI RICERCA");
        if (risultati.isEmpty()) {
            MenuUI.stampaInfo("Nessuna proiezione corrisponde ai criteri inseriti.");
        } else {
            MenuUI.stampaInfo("Trovate " + risultati.size() + " proiezioni:");
            MenuUI.stampaListaProiezioni(risultati, app);
        }
        MenuUI.premInvio(sc);
        return risultati;
    }

   // =========================================================================
   // VISUALIZZA DETTAGLI PROIEZIONE
   // =========================================================================

    /**
     * Permette all'utente di selezionare una proiezione dall'elenco corrente e visualizzarne i dettagli completi
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     * @param proiezioni --> lista corrente di proiezioni tra cui scegliere
     */
    private static void visualizzaDettagli(Scanner sc, GestoreApp app,
                                           List<Proiezione> proiezioni) {
        if (proiezioni.isEmpty()) {
            MenuUI.stampaErrore("Nessuna proiezione disponibile. Effettua prima una ricerca.");
            MenuUI.premInvio(sc);
            return;
        }

        MenuUI.stampaIntestazione("SELEZIONA PROIEZIONE");
        MenuUI.stampaListaProiezioni(proiezioni, app);

        int scelta = MenuUI.leggiScelta(sc, "Numero proiezione (0 = annulla):");
        if (scelta == 0) return;
        if (scelta < 1 || scelta > proiezioni.size()) {
            MenuUI.stampaErrore("Numero non valido.");
            MenuUI.premInvio(sc);
            return;
        }

        Proiezione p = proiezioni.get(scelta - 1);
        MenuUI.stampaIntestazione("DETTAGLI PROIEZIONE");
        MenuUI.stampaDettagliProiezione(p, app.postiLiberi(p));
        MenuUI.premInvio(sc);
    }
}
