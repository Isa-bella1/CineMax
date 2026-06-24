package cinemax.ui;
import cinemax.model.Film;
import cinemax.model.Proiezione;
import cinemax.model.Proiezionista;
import cinemax.service.GestoreApp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Menu per i proiezionisti autenticati di CineMax
 *
 * Consente al proiezionista di gestire il palinsesto del cinema:
 * aggiungere nuove proiezioni, modificare la data di quelle esistenti
 * (se non hanno prenotazioni) ed eliminarle
 *
 * @author Serrao Isabella (766930) [VA]
 */
public final class ProiezionistaMenu {

    /** Costruttore privato: classe non istanziabile */
    private ProiezionistaMenu() {}

   // =========================================================================
   // MENU PRINCIPALE PROIEZIONISTA
   // =========================================================================

    /**
     * Avvia il menu del proiezionista e mantiene il loop finché non esegue il logout
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     * @param proiezionista --> il proiezionista autenticato
     */
    public static void mostra(Scanner sc, GestoreApp app, Proiezionista proiezionista) {
        while (true) {
            MenuUI.stampaIntestazione(
                    "MENU PROIEZIONISTA — " + proiezionista.getNome()
                            + " " + proiezionista.getCognome());
            MenuUI.stampaOpzioni(
                    "Visualizza tutte le proiezioni",
                    "Aggiungi nuova proiezione",
                    "Modifica data di una proiezione",
                    "Elimina una proiezione"
            );
            MenuUI.stampaOpzioneZero("Logout");

            int scelta = MenuUI.leggiScelta(sc, "Scelta:");

            switch (scelta) {
                case 1: visualizzaProiezioni(sc, app); break;
                case 2: aggiungiProiezione(sc, app);   break;
                case 3: modificaProiezione(sc, app);   break;
                case 4: eliminaProiezione(sc, app);    break;
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
   // VISUALIZZA PROIEZIONI
   // =========================================================================

    /**
     * Mostra l'elenco completo di tutte le proiezioni programmate, con la
     * possibilità di visualizzare i dettagli di una specifica
     *
     * @param sc -->  lo Scanner
     * @param app --> il gestore
     */
    private static void visualizzaProiezioni(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("TUTTE LE PROIEZIONI");
        List<Proiezione> tutte = app.getTutteProiezioni();
        if (tutte.isEmpty()) {
            MenuUI.stampaInfo("Nessuna proiezione in programma.");
            MenuUI.premInvio(sc);
            return;
        }
        MenuUI.stampaInfo("Proiezioni in programma: " + tutte.size());
        MenuUI.stampaListaProiezioni(tutte, app);

        System.out.print("  Visualizza dettagli (numero, 0 = torna): ");
        String input = sc.nextLine().trim();
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= tutte.size()) {
                Proiezione p = tutte.get(idx - 1);
                MenuUI.stampaDettagliProiezione(p, app.postiLiberi(p));
            }
        } catch (NumberFormatException ignored) {}
        MenuUI.premInvio(sc);
    }

   // =========================================================================
   // AGGIUNGI PROIEZIONE
   // =========================================================================

    /**
     * Guida il proiezionista nell'inserimento di una nuova proiezione,
     * raccogliendo tutti i dati del film e della programmazione
     * Verifica automaticamente che non vi siano sovrapposizioni
     *
     * @param sc --> lo Scanner
     * @param app --> il gestore
     */
    private static void aggiungiProiezione(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("AGGIUNGI PROIEZIONE");
        MenuUI.stampaInfo("Inserisci i dati del film e della proiezione.");
        System.out.println();

        // Dati film
        String titolo  = MenuUI.leggiStringa(sc, "Titolo film:");
        String genere  = MenuUI.leggiStringa(sc, "Genere (es. Azione, Commedia, Dramma):");
        String regista = MenuUI.leggiStringa(sc, "Regista:");
        int anno       = leggiAnno(sc);
        int durata     = leggiDurata(sc);
        int etaMin     = leggiEtaMinima(sc);

        // Dati proiezione
        System.out.println();
        LocalDateTime dataOra = MenuUI.leggiDataOra(sc, "Data e ora della proiezione");
        double prezzo  = MenuUI.leggiPrezzo(sc, "Prezzo biglietto (€):");

        Film film = new Film(titolo, genere, regista, anno, durata, etaMin);
        boolean ok = app.aggiungiProiezione(film, dataOra, prezzo);

        if (ok) {
            MenuUI.stampaSuccesso("Proiezione aggiunta: \"" + titolo + "\" il "
                    + dataOra.format(MenuUI.FMT_DT));
        } else {
            MenuUI.stampaErrore("Impossibile aggiungere la proiezione: si sovrappone a una già esistente.\n"
                    + "  Ricorda che la durata del film viene considerata nel calcolo della sovrapposizione.");
        }
        MenuUI.premInvio(sc);
    }

   // =========================================================================
   // MODIFICA PROIEZIONE
   // =========================================================================

    /**
     * Permette al proiezionista di modificare la data e ora di una proiezione
     * esistente. La modifica è consentita solo se la proiezione non ha
     * prenotazioni associate e il nuovo orario non crea sovrapposizioni
     *
     * @param sc --> lo Scanner
     * @param app --> il gestore
     */
    private static void modificaProiezione(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("MODIFICA DATA PROIEZIONE");
        List<Proiezione> tutte = app.getTutteProiezioni();
        if (tutte.isEmpty()) {
            MenuUI.stampaInfo("Nessuna proiezione disponibile.");
            MenuUI.premInvio(sc);
            return;
        }

        MenuUI.stampaListaProiezioni(tutte, app);
        int scelta = MenuUI.leggiScelta(sc, "Seleziona proiezione da modificare (0 = annulla):");
        if (scelta == 0) return;
        if (scelta < 1 || scelta > tutte.size()) {
            MenuUI.stampaErrore("Numero non valido.");
            MenuUI.premInvio(sc);
            return;
        }

        Proiezione target = tutte.get(scelta - 1);
        MenuUI.stampaInfo("Proiezione selezionata:");
        MenuUI.stampaDettagliProiezione(target, app.postiLiberi(target));

        LocalDateTime nuovaDataOra = MenuUI.leggiDataOra(sc,
                "Nuova data e ora della proiezione");

        boolean ok = app.modificaProiezione(target.getCodice(), nuovaDataOra);
        if (ok) {
            MenuUI.stampaSuccesso("Data aggiornata: " + nuovaDataOra.format(MenuUI.FMT_DT));
        } else {
            MenuUI.stampaErrore("Modifica non consentita. Verifica che:\n"
                    + "  - La proiezione non abbia prenotazioni associate.\n"
                    + "  - Il nuovo orario non si sovrapponga ad altre proiezioni.");
        }
        MenuUI.premInvio(sc);
    }

   // =========================================================================
   // ELIMINA PROIEZIONE
   // =========================================================================

    /**
     * Permette al proiezionista di eliminare una proiezione dal palinsesto
     * L'eliminazione è consentita solo se la proiezione non ha prenotazioni
     *
     * @param sc --> lo Scanner
     * @param app --> il gestore
     */
    private static void eliminaProiezione(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("ELIMINA PROIEZIONE");
        List<Proiezione> tutte = app.getTutteProiezioni();
        if (tutte.isEmpty()) {
            MenuUI.stampaInfo("Nessuna proiezione disponibile.");
            MenuUI.premInvio(sc);
            return;
        }

        MenuUI.stampaListaProiezioni(tutte, app);
        int scelta = MenuUI.leggiScelta(sc, "Seleziona proiezione da eliminare (0 = annulla):");
        if (scelta == 0) return;
        if (scelta < 1 || scelta > tutte.size()) {
            MenuUI.stampaErrore("Numero non valido.");
            MenuUI.premInvio(sc);
            return;
        }

        Proiezione target = tutte.get(scelta - 1);
        MenuUI.stampaDettagliProiezione(target, app.postiLiberi(target));

        System.out.print("  Confermi l'eliminazione? (s/n): ");
        String conf = sc.nextLine().trim();
        if (!conf.equalsIgnoreCase("s")) {
            MenuUI.stampaInfo("Eliminazione annullata.");
            MenuUI.premInvio(sc);
            return;
        }

        boolean ok = app.eliminaProiezione(target.getCodice());
        if (ok) {
            MenuUI.stampaSuccesso("Proiezione \"" + target.getFilm().getTitolo()
                    + "\" eliminata dal palinsesto.");
        } else {
            MenuUI.stampaErrore("Eliminazione non consentita: "
                    + "esistono prenotazioni per questa proiezione.");
        }
        MenuUI.premInvio(sc);
    }

   // =========================================================================
   // INPUT SPECIALIZZATI
   // =========================================================================

    /**
     * Legge l'anno di uscita del film, verificando che sia un valore plausibile
     *
     * @param sc --> lo Scanner
     * @return l'anno inserito
     */
    private static int leggiAnno(Scanner sc) {
        while (true) {
            System.out.print("  Anno di uscita: ");
            try {
                int anno = Integer.parseInt(sc.nextLine().trim());
                if (anno >= 1888 && anno <= LocalDateTime.now().getYear() + 2) return anno;
                MenuUI.stampaErrore("Anno non valido.");
            } catch (NumberFormatException e) {
                MenuUI.stampaErrore("Inserisci un numero intero.");
            }
        }
    }

    /**
     * Legge la durata del film in minuti (deve essere positiva)
     *
     * @param sc --> lo Scanner
     * @return la durata in minuti
     */
    private static int leggiDurata(Scanner sc) {
        return MenuUI.leggiInteroPositivo(sc, "Durata (minuti)");
    }

    /**
     * Legge l'età minima del pubblico (0 = per tutti)
     *
     * @param sc --> lo Scanner
     * @return l'età minima
     */
    private static int leggiEtaMinima(Scanner sc) {
        while (true) {
            System.out.print("  Età minima pubblico (0 = per tutti): ");
            try {
                int eta = Integer.parseInt(sc.nextLine().trim());
                if (eta >= 0) return eta;
                MenuUI.stampaErrore("L'età minima non può essere negativa.");
            } catch (NumberFormatException e) {
                MenuUI.stampaErrore("Inserisci un numero intero (es. 0, 14, 18).");
            }
        }
    }
}
