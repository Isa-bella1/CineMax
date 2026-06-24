package cinemax.ui;
import cinemax.model.Cliente;
import cinemax.model.Prenotazione;
import cinemax.model.Proiezione;
import cinemax.service.GestoreApp;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Menu per i clienti autenticati di CineMax
 *
 * Permette al cliente di visualizzare le proprie prenotazioni,
 * cercare nuove proiezioni e prenotarle, modificare o cancellare
 * prenotazioni esistenti e disconnettersi dal sistema
 *
 * @author Serrao Isabella (766930) [VA]
 */
public final class ClienteMenu {

    /** Costruttore privato: classe non istanziabile */
    private ClienteMenu() {}

   // =========================================================================
   // MENU PRINCIPALE (cliente)
   // =========================================================================

    /**
     * Avvia il menu del cliente e mantiene il loop finché non esegue il logout
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     * @param cliente --> il cliente autenticato
     */
    public static void mostra(Scanner sc, GestoreApp app, Cliente cliente) {
        while (true) {
            MenuUI.stampaIntestazione(
                    "MENU CLIENTE — " + cliente.getNome() + " " + cliente.getCognome());
            MenuUI.stampaOpzioni(
                    "Le mie prenotazioni",
                    "Cerca proiezioni e prenota",
                    "Modifica una prenotazione",
                    "Cancella una prenotazione"
            );
            MenuUI.stampaOpzioneZero("Logout");

            int scelta = MenuUI.leggiScelta(sc, "Scelta:");

            switch (scelta) {
                case 1: visualizzaPrenotazioni(sc, app, cliente); break;
                case 2: cercaEPrenota(sc, app, cliente);          break;
                case 3: modificaPrenotazione(sc, app, cliente);   break;
                case 4: cancellaPrenotazione(sc, app, cliente);   break;
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
   // VISUALIZZA PRENOTAZIONI
   // =========================================================================

    /**
     * Mostra tutte le prenotazioni del cliente corrente
     *
     * @param sc --> lo Scanner
     * @param apP --> il gestore
     * @param cliente --> il cliente
     */
    private static void visualizzaPrenotazioni(Scanner sc, GestoreApp app, Cliente cliente) {
        MenuUI.stampaIntestazione("LE MIE PRENOTAZIONI");
        List<Prenotazione> prenotazioni = app.prenotazioniCliente(cliente.getUsername());
        if (prenotazioni.isEmpty()) {
            MenuUI.stampaInfo("Non hai ancora effettuato prenotazioni.");
        } else {
            MenuUI.stampaInfo("Hai " + prenotazioni.size() + " prenotazione/i:");
            MenuUI.stampaListaPrenotazioni(prenotazioni);

            // Offri la possibilità di vedere i dettagli
            System.out.println();
            System.out.print("  Vuoi vedere i dettagli di una prenotazione? (numero o 0 = no): ");
            String input = sc.nextLine().trim();
            try {
                int idx = Integer.parseInt(input);
                if (idx >= 1 && idx <= prenotazioni.size()) {
                    MenuUI.stampaDettagliPrenotazione(prenotazioni.get(idx - 1));
                }
            } catch (NumberFormatException ignored) {}
        }
        MenuUI.premInvio(sc);
    }

   // =========================================================================
   // CERCA E PRENOTA
   // =========================================================================

    /**
     * Permette al cliente di cercare proiezioni tramite filtri e prenotare i posti desiderati
     *
     * @param sc --> lo Scanner
     * @param app --> il gestore
     * @param cliente --> il cliente
     */
    private static void cercaEPrenota(Scanner sc, GestoreApp app, Cliente cliente) {
        MenuUI.stampaIntestazione("CERCA E PRENOTA");
        MenuUI.stampaInfo("Inserisci i criteri di ricerca (Invio = nessun filtro).");
        System.out.println();

        String titolo  = MenuUI.leggiStringaOpzionale(sc, "Titolo del film:");
        String genere  = MenuUI.leggiStringaOpzionale(sc, "Genere:");
        LocalDate dMin = MenuUI.leggiDataOpzionale(sc, "Data minima");
        LocalDate dMax = MenuUI.leggiDataOpzionale(sc, "Data massima");
        double pMin    = MenuUI.leggiPrezzoOpzionale(sc, "Prezzo minimo (€)");
        double pMax    = MenuUI.leggiPrezzoOpzionale(sc, "Prezzo massimo (€)");

        List<Proiezione> risultati = app.cercaProiezioni(
                titolo, genere, dMin, dMax, pMin, pMax);

        MenuUI.stampaIntestazione("RISULTATI RICERCA");
        if (risultati.isEmpty()) {
            MenuUI.stampaInfo("Nessuna proiezione trovata con i criteri inseriti.");
            MenuUI.premInvio(sc);
            return;
        }
        MenuUI.stampaListaProiezioni(risultati, app);

        int scelta = MenuUI.leggiScelta(sc, "Seleziona proiezione (0 = annulla):");
        if (scelta == 0) return;
        if (scelta < 1 || scelta > risultati.size()) {
            MenuUI.stampaErrore("Numero non valido.");
            MenuUI.premInvio(sc);
            return;
        }

        Proiezione proiezione = risultati.get(scelta - 1);
        MenuUI.stampaIntestazione("DETTAGLI PROIEZIONE");
        int liberi = app.postiLiberi(proiezione);
        MenuUI.stampaDettagliProiezione(proiezione, liberi);

        if (liberi == 0) {
            MenuUI.stampaErrore("Spiacente, questa proiezione è esaurita.");
            MenuUI.premInvio(sc);
            return;
        }

        int numPosti = MenuUI.leggiInteroPositivo(sc, "Quanti posti vuoi prenotare?");
        if (numPosti > liberi) {
            MenuUI.stampaErrore("Posti richiesti (" + numPosti
                    + ") superiori a quelli disponibili (" + liberi + ").");
            MenuUI.premInvio(sc);
            return;
        }

        boolean ok = app.creaPrenotazione(cliente, proiezione, numPosti);
        if (ok) {
            double totale = numPosti * proiezione.getPrezzoBiglietto();
            MenuUI.stampaSuccesso("Prenotazione effettuata! Posti: " + numPosti
                    + " — Totale: " + String.format("%.2f", totale) + " €");
        } else {
            MenuUI.stampaErrore("Prenotazione fallita. Verifica la disponibilità.");
        }
        MenuUI.premInvio(sc);
    }

   // =========================================================================
   // MODIFICA PRENOTAZIONE
   // =========================================================================

    /**
     * Permette al cliente di spostare una propria prenotazione su un'altra proiezione (cambio data)
     * Entrambe le proiezioni devono essere future
     *
     * @param sc --> lo Scanner
     * @param app --> il gestore
     * @param cliente --> il cliente
     */
    private static void modificaPrenotazione(Scanner sc, GestoreApp app, Cliente cliente) {
        MenuUI.stampaIntestazione("MODIFICA PRENOTAZIONE");

        List<Prenotazione> mie = app.prenotazioniCliente(cliente.getUsername());
        if (mie.isEmpty()) {
            MenuUI.stampaInfo("Non hai prenotazioni da modificare.");
            MenuUI.premInvio(sc);
            return;
        }

        MenuUI.stampaListaPrenotazioni(mie);
        int scelta = MenuUI.leggiScelta(sc, "Seleziona prenotazione (0 = annulla):");
        if (scelta == 0) return;
        if (scelta < 1 || scelta > mie.size()) {
            MenuUI.stampaErrore("Numero non valido.");
            MenuUI.premInvio(sc);
            return;
        }

        Prenotazione target = mie.get(scelta - 1);
        MenuUI.stampaInfo("Prenotazione selezionata:");
        MenuUI.stampaDettagliPrenotazione(target);

        // Mostra proiezioni future dello stesso film
        MenuUI.stampaInfo("Cerca una nuova proiezione (Invio = nessun filtro):");
        System.out.println();
        String titolo  = MenuUI.leggiStringaOpzionale(sc, "Titolo film:");
        String genere  = MenuUI.leggiStringaOpzionale(sc, "Genere:");
        LocalDate dMin = MenuUI.leggiDataOpzionale(sc, "Data minima");
        LocalDate dMax = MenuUI.leggiDataOpzionale(sc, "Data massima");

        List<Proiezione> proiezioniDisp = app.cercaProiezioni(titolo, genere, dMin, dMax, -1, -1);
        if (proiezioniDisp.isEmpty()) {
            MenuUI.stampaInfo("Nessuna proiezione trovata.");
            MenuUI.premInvio(sc);
            return;
        }

        MenuUI.stampaListaProiezioni(proiezioniDisp, app);
        int sceltaP = MenuUI.leggiScelta(sc, "Seleziona nuova proiezione (0 = annulla):");
        if (sceltaP == 0) return;
        if (sceltaP < 1 || sceltaP > proiezioniDisp.size()) {
            MenuUI.stampaErrore("Numero non valido.");
            MenuUI.premInvio(sc);
            return;
        }

        Proiezione nuova = proiezioniDisp.get(sceltaP - 1);
        boolean ok = app.modificaPrenotazione(target.getCodice(), nuova);
        if (ok) {
            MenuUI.stampaSuccesso("Prenotazione modificata! Nuova data: "
                    + nuova.getDataOra().format(MenuUI.FMT_DT));
        } else {
            MenuUI.stampaErrore("Modifica non consentita. Verifica che:\n"
                    + "  - La proiezione corrente sia ancora futura.\n"
                    + "  - La nuova proiezione sia futura e abbia posti liberi.");
        }
        MenuUI.premInvio(sc);
    }

   // =========================================================================
   // CANCELLA PRENOTAZIONE
   // =========================================================================

    /**
     * Permette al cliente di cancellare una propria prenotazione
     * La proiezione associata deve essere in data futura
     *
     * @param sc --> lo Scanner
     * @param app -->il gestore
     * @param cliente --> il cliente
     */
    private static void cancellaPrenotazione(Scanner sc, GestoreApp app, Cliente cliente) {
        MenuUI.stampaIntestazione("CANCELLA PRENOTAZIONE");

        List<Prenotazione> mie = app.prenotazioniCliente(cliente.getUsername());
        if (mie.isEmpty()) {
            MenuUI.stampaInfo("Non hai prenotazioni da cancellare.");
            MenuUI.premInvio(sc);
            return;
        }

        MenuUI.stampaListaPrenotazioni(mie);
        int scelta = MenuUI.leggiScelta(sc, "Seleziona prenotazione da cancellare (0 = annulla):");
        if (scelta == 0) return;
        if (scelta < 1 || scelta > mie.size()) {
            MenuUI.stampaErrore("Numero non valido.");
            MenuUI.premInvio(sc);
            return;
        }

        Prenotazione target = mie.get(scelta - 1);
        MenuUI.stampaDettagliPrenotazione(target);

        // Conferma
        System.out.print("  Confermi la cancellazione? (s/n): ");
        String conf = sc.nextLine().trim();
        if (!conf.equalsIgnoreCase("s")) {
            MenuUI.stampaInfo("Cancellazione annullata.");
            MenuUI.premInvio(sc);
            return;
        }

        boolean ok = app.eliminaPrenotazione(target.getCodice());
        if (ok) {
            MenuUI.stampaSuccesso("Prenotazione cancellata con successo.");
        } else {
            MenuUI.stampaErrore("Cancellazione non consentita: la proiezione è già passata.");
        }
        MenuUI.premInvio(sc);
    }
}
