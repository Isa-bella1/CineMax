package cinemax.ui;
import cinemax.model.Bigliettaio;
import cinemax.model.Prenotazione;
import cinemax.service.GestoreApp;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Menu per i bigliettai autenticati di CineMax
 *
 * Consente al bigliettaio di visualizzare le prenotazioni del giorno
 * corrente e di cercare prenotazioni specifiche per vari criteri,
 * utili per la gestione dell'accesso in sala
 *
 * @author Serrao Isabella (766930) [VA]
 */
public final class BigliettaioMenu {

    /** Costruttore privato: classe non istanziabile */
    private BigliettaioMenu() {}

   // =========================================================================
   // MENU PRINCIPALE (bigliettaio)
   // =========================================================================

    /**
     * Avvia il menu del bigliettaio e mantiene il loop finché non esegue il logout
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     * @param bigliettaio --> il bigliettaio autenticato
     */
    public static void mostra(Scanner sc, GestoreApp app, Bigliettaio bigliettaio) {
        while (true) {
            MenuUI.stampaIntestazione(
                    "MENU BIGLIETTAIO — " + bigliettaio.getNome()
                            + " " + bigliettaio.getCognome());
            MenuUI.stampaOpzioni(
                    "Prenotazioni di oggi",
                    "Cerca prenotazione"
            );
            MenuUI.stampaOpzioneZero("Logout");

            int scelta = MenuUI.leggiScelta(sc, "Scelta:");

            switch (scelta) {
                case 1: prenotazioniOggi(sc, app);    break;
                case 2: cercaPrenotazione(sc, app);   break;
                case 0:
                    app.logout();
                    MenuUI.stampaSuccesso("Logout effettuato. Arrivederci!");
                    MenuUI.premInvio(sc);
                    return;
                default:
                    MenuUI.stampaErrore("Opzione non valida.");
            }
        }
    }

   // =========================================================================
   // PRENOTAZIONE ODIERNA
   // =========================================================================

    /**
     * Mostra tutte le prenotazioni per le proiezioni in programma nella giornata
     * odierna, con la possibilità di visualizzare i dettagli di ognuna
     *
     * @param sc --> lo Scanner
     * @param app --> il gestore
     */
    private static void prenotazioniOggi(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("PRENOTAZIONI DI OGGI — "
                + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        List<Prenotazione> lista = app.prenotazioniOggi();
        if (lista.isEmpty()) {
            MenuUI.stampaInfo("Nessuna prenotazione per oggi.");
            MenuUI.premInvio(sc);
            return;
        }

        int totPosti = lista.stream().mapToInt(Prenotazione::getNumeroPosti).sum();
        MenuUI.stampaInfo("Totale prenotazioni: " + lista.size()
                + " | Totale posti occupati: " + totPosti);
        System.out.println();
        MenuUI.stampaListaPrenotazioni(lista);

        mostraDettagliDaLista(sc, lista);
    }

    // =========================================================================
    // CERCA PRENOTAZIONE
    // =========================================================================

    /**
     * Mostra la schermata di ricerca avanzata delle prenotazioni con criteri opzionali combinabili
     *
     * @param sc --> lo Scanner
     * @param app --> il gestore
     */
    private static void cercaPrenotazione(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("CERCA PRENOTAZIONE");
        MenuUI.stampaInfo("Inserisci uno o più criteri di ricerca (Invio = nessun filtro).");
        System.out.println();

        String codice   = MenuUI.leggiStringaOpzionale(sc, "Codice prenotazione:");
        String nome     = MenuUI.leggiStringaOpzionale(sc, "Nome cliente:");
        String cognome  = MenuUI.leggiStringaOpzionale(sc, "Cognome cliente:");
        String titolo   = MenuUI.leggiStringaOpzionale(sc, "Titolo film:");
        LocalDate dMin  = MenuUI.leggiDataOpzionale(sc, "Data minima proiezione");
        LocalDate dMax  = MenuUI.leggiDataOpzionale(sc, "Data massima proiezione");

        List<Prenotazione> risultati = app.cercaPrenotazioni(
                codice, nome, cognome, titolo, dMin, dMax);

        MenuUI.stampaIntestazione("RISULTATI RICERCA");
        if (risultati.isEmpty()) {
            MenuUI.stampaInfo("Nessuna prenotazione trovata.");
            MenuUI.premInvio(sc);
            return;
        }

        MenuUI.stampaInfo("Trovate " + risultati.size() + " prenotazione/i:");
        MenuUI.stampaListaPrenotazioni(risultati);
        mostraDettagliDaLista(sc, risultati);
    }

   // =========================================================================
   // UTILITY: visualizza dettagli lista
   // =========================================================================

    /**
     * Offre all'utente la possibilità di selezionare una prenotazione dalla
     * lista corrente e visualizzarne i dettagli completi
     *
     * @param sc --> lo Scanner
     * @param lista --> lista di prenotazioni già visualizzata
     */
    private static void mostraDettagliDaLista(Scanner sc, List<Prenotazione> lista) {
        System.out.println();
        System.out.print("  Visualizza dettagli (numero prenotazione, 0 = torna): ");
        String input = sc.nextLine().trim();
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= lista.size()) {
                MenuUI.stampaIntestazione("DETTAGLI PRENOTAZIONE");
                MenuUI.stampaDettagliPrenotazione(lista.get(idx - 1));
            }
        } catch (NumberFormatException ignored) {}
        MenuUI.premInvio(sc);
    }
}
