package cinemax.ui;
import cinemax.model.Prenotazione;
import cinemax.model.Proiezione;
import cinemax.service.GestoreApp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Classe di utilità per la gestione dell'interfaccia terminale (TUI) di CineMax
 *
 * Fornisce metodi statici per stampare intestazioni, menu numerati e
 * messaggi di stato, e per leggere input dall'utente in modo sicuro
 * (con ripetizione in caso di input non valido). 
 * Tutti i metodi sono statici: questa classe non deve essere istanziata
 *
 * @author Serrao Isabella (766930) [VA]
 */
public final class MenuUI {

    /** Separatore orizzontale principale */
    public static final String LINEA = "=".repeat(58);

    /** Separatore orizzontale secondario */
    public static final String LINEA_SOTTILE = "-".repeat(58);

    /** Formato data-ora per la visualizzazione */
    public static final DateTimeFormatter FMT_DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Formato per l'input di data e ora da parte dell'utente */
    private static final DateTimeFormatter INPUT_DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Formato per l'input di sola data da parte dell'utente */
    private static final DateTimeFormatter INPUT_D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Costruttore privato: classe non istanziabile */
    private MenuUI() {}

   // =========================================================================
   // INTESTAZIONI E STRUTTURA MENU
   // =========================================================================

    /**
     * Stampa l'intestazione principale dell'applicazione
     */
    public static void stampaLogo() {
        System.out.println("\n" + LINEA);
        System.out.println("          ░█████╗░██╗███╗░░██╗███████╗███╗░░░███╗░█████╗░██╗░░██╗");
        System.out.println("          ██╔══██╗██║████╗░██║██╔════╝████╗░████║██╔══██╗╚██╗██╔╝");
        System.out.println("          ██║░░╚═╝██║██╔██╗██║█████╗░░██╔████╔██║███████║░╚███╔╝░");
        System.out.println("          ██║░░██╗██║██║╚████║██╔══╝░░██║╚██╔╝██║██╔══██║░██╔██╗░");
        System.out.println("          ╚█████╔╝██║██║░╚███║███████╗██║░╚═╝░██║██║░░██║██╔╝╚██╗");
        System.out.println("          ░╚════╝░╚═╝╚═╝░░╚══╝╚══════╝╚═╝░░░░╚═╝╚═╝░░╚═╝╚═╝░░╚═╝");
        System.out.println(LINEA);
    }

    /**
     * Stampa una intestazione di sezione con il titolo fornito
     *
     * @param titolo --> il titolo della sezione
     */
    public static void stampaIntestazione(String titolo) {
        System.out.println("\n" + LINEA);
        int padding = Math.max(0, (58 - titolo.length()) / 2);
        System.out.println(" ".repeat(padding) + titolo);
        System.out.println(LINEA);
    }

    /**
     * Stampa un menu numerato. 
     * Le opzioni vengono numerate da 1 in su; l'opzione 0 ("Indietro" o "Esci") deve essere gestita dal chiamante.
     *
     * @param opzioni --> le opzioni del menu
     */
    public static void stampaOpzioni(String... opzioni) {
        for (int i = 0; i < opzioni.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, opzioni[i]);
        }
        System.out.println(LINEA_SOTTILE);
    }

    /**
     * Stampa l'opzione 0 con il testo fornito (tipicamente "Indietro" o "Esci")
     *
     * @param testo --> testo dell'opzione zero
     */
    public static void stampaOpzioneZero(String testo) {
        System.out.printf("  [0] %s%n", testo);
        System.out.println(LINEA_SOTTILE);
    }

   // =========================================================================
   // MESSAGGI DI STATO
   // =========================================================================

    /**
     * Stampa un messaggio di errore formattato
     *
     * @param messaggio --> testo dell'errore
     */
    public static void stampaErrore(String messaggio) {
        System.out.println("\n  ✗ ERRORE: " + messaggio);
    }

    /**
     * Stampa un messaggio di successo formattato
     *
     * @param messaggio --> testo del messaggio
     */
    public static void stampaSuccesso(String messaggio) {
        System.out.println("\n  ✓ " + messaggio);
    }

    /**
     * Stampa un messaggio informativo
     *
     * @param messaggio --> testo del messaggio
     */
    public static void stampaInfo(String messaggio) {
        System.out.println("  " + messaggio);
    }

    /**
     * Chiede all'utente di premere Invio per continuare
     *
     * @param sc --> lo Scanner da cui leggere
     */
    public static void premInvio(Scanner sc) {
        System.out.print("\n  Premi Invio per continuare...");
        sc.nextLine();
    }

   // =========================================================================
   // LETTURA INPUT
   // =========================================================================

    /**
     * Legge una scelta intera dall'utente, ripetendo finché l'input è valido
     *
     * @param sc --> lo Scanner da cui leggere
     * @param prompt --> il testo del prompt
     * @return l'intero inserito
     */
    public static int leggiScelta(Scanner sc, String prompt) {
        while (true) {
            System.out.print("\n  " + prompt + " ");
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                stampaErrore("Inserisci un numero valido.");
            }
        }
    }

    /**
     * Legge una stringa obbligatoria (non vuota), ripetendo se l'input è vuoto
     *
     * @param sc --> lo Scanner da cui leggere
     * @param prompt --> il testo del prompt
     * @return la stringa inserita
     */
    public static String leggiStringa(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt + " ");
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            stampaErrore("Il campo non può essere vuoto.");
        }
    }

    /**
     * Legge una stringa opzionale (può essere vuota)
     * Se l'utente preme Invio senza inserire nulla, restituisce stringa vuota
     *
     * @param sc --> lo Scanner da cui leggere
     * @param prompt --> il testo del prompt
     * @return la stringa inserita, oppure stringa vuota
     */
    public static String leggiStringaOpzionale(Scanner sc, String prompt) {
        System.out.print("  " + prompt + " ");
        return sc.nextLine().trim();
    }

    /**
     * Legge una data nel formato {@code dd/MM/yyyy} (opzionale: invio vuoto → {@code null})
     *
     * @param sc --> lo Scanner da cui leggere
     * @param prompt --> il testo del prompt
     * @return la data, oppure {@code null} se l'input è vuoto
     */
    public static LocalDate leggiDataOpzionale(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (dd/MM/yyyy, Invio = nessun filtro): ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) return null;
            try {
                return LocalDate.parse(input, INPUT_D);
            } catch (DateTimeParseException e) {
                stampaErrore("Formato non valido. Usa dd/MM/yyyy (es. 01/07/2026).");
            }
        }
    }

    /**
     * Legge una data e ora nel formato {@code dd/MM/yyyy HH:mm} (obbligatoria)
     *
     * @param sc --> lo Scanner da cui leggere
     * @param prompt --> il testo del prompt
     * @return la data-ora inserita
     */
    public static LocalDateTime leggiDataOra(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (dd/MM/yyyy HH:mm): ");
            try {
                return LocalDateTime.parse(sc.nextLine().trim(), INPUT_DT);
            } catch (DateTimeParseException e) {
                stampaErrore("Formato non valido. Usa dd/MM/yyyy HH:mm (es. 01/07/2026 21:00).");
            }
        }
    }

    /**
     * Legge una data di nascita nel formato {@code dd/MM/yyyy} (opzionale)
     *
     * @param sc --> lo Scanner da cui leggere
     * @return la data, oppure {@code null}
     */
    public static LocalDate leggiDataNascita(Scanner sc) {
        return leggiDataOpzionale(sc, "Data di nascita");
    }

    /**
     * Legge un prezzo decimale opzionale. Input vuoto → {@code -1.0}
     *
     * @param sc --> lo Scanner da cui leggere
     * @param prompt --> il testo del prompt
     * @return il prezzo inserito, oppure {@code -1.0}
     */
    public static double leggiPrezzoOpzionale(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (Invio = nessun filtro): ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) return -1.0;
            try {
                double v = Double.parseDouble(input.replace(',', '.'));
                if (v >= 0) return v;
                stampaErrore("Il prezzo deve essere non negativo.");
            } catch (NumberFormatException e) {
                stampaErrore("Inserisci un numero decimale (es. 8.50).");
            }
        }
    }

    /**
     * Legge un prezzo decimale obbligatorio (positivo)
     *
     * @param sc --> lo Scanner da cui leggere
     * @param prompt --> il testo del prompt
     * @return il prezzo inserito
     */
    public static double leggiPrezzo(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (es. 9.50): ");
            try {
                double v = Double.parseDouble(sc.nextLine().trim().replace(',', '.'));
                if (v > 0) return v;
                stampaErrore("Il prezzo deve essere maggiore di zero.");
            } catch (NumberFormatException e) {
                stampaErrore("Inserisci un numero decimale (es. 9.50).");
            }
        }
    }

    /**
     * Legge un intero positivo obbligatorio
     *
     * @param sc --> lo Scanner da cui leggere
     * @param prompt --> il testo del prompt
     * @return l'intero positivo inserito
     */
    public static int leggiInteroPositivo(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v > 0) return v;
                stampaErrore("Il valore deve essere maggiore di zero.");
            } catch (NumberFormatException e) {
                stampaErrore("Inserisci un numero intero.");
            }
        }
    }

   // =========================================================================
   // VISUALIZZAZIONE ENTITA' DI DOMINIO
   // =========================================================================

    /**
     * Stampa i dettagli completi di una proiezione con il numero di posti liberi
     *
     * @param p --> la proiezione da visualizzare
     * @param postiLiberi --> numero di posti ancora disponibili
     */
    public static void stampaDettagliProiezione(Proiezione p, int postiLiberi) {
        System.out.println(LINEA_SOTTILE);
        System.out.printf("  Codice        : %s%n", p.getCodice());
        System.out.printf("  Titolo        : %s (%d)%n",
                p.getFilm().getTitolo(), p.getFilm().getAnno());
        System.out.printf("  Genere        : %s%n", p.getFilm().getGenere());
        System.out.printf("  Regista       : %s%n", p.getFilm().getRegista());
        System.out.printf("  Durata        : %d min%n", p.getFilm().getDurataMinuti());
        System.out.printf("  Età minima    : %d anni%n", p.getFilm().getEtaMinima());
        System.out.printf("  Data e ora    : %s%n", p.getDataOra().format(FMT_DT));
        System.out.printf("  Biglietto     : %.2f €%n", p.getPrezzoBiglietto());
        System.out.printf("  Posti liberi  : %d / %d%n",
                postiLiberi, Proiezione.CAPIENZA_SALA);
        System.out.println(LINEA_SOTTILE);
    }

    /**
     * Stampa una riga riassuntiva di una proiezione in un elenco numerato
     *
     * @param n --> indice (visualizzato tra parentesi quadre)
     * @param p --> la proiezione
     * @param postiLiberi --> posti disponibili
     */
    public static void stampaRigaProiezione(int n, Proiezione p, int postiLiberi) {
        System.out.printf("  [%2d] %-28s | %s | %5.2f€ | %3d posti liberi%n",
                n,
                troncaTesto(p.getFilm().getTitolo(), 28),
                p.getDataOra().format(FMT_DT),
                p.getPrezzoBiglietto(),
                postiLiberi);
    }

    /**
     * Stampa la lista di proiezioni con indici per la selezione dell'utente
     *
     * @param proiezioni --> lista di proiezioni
     * @param app --> gestore dell'app (per calcolare i posti liberi)
     */
    public static void stampaListaProiezioni(List<Proiezione> proiezioni, GestoreApp app) {
        if (proiezioni.isEmpty()) {
            stampaInfo("Nessuna proiezione trovata.");
            return;
        }
        System.out.println(LINEA_SOTTILE);
        System.out.printf("  %4s %-28s %-18s %8s %12s%n",
                "N.", "Titolo", "Data/Ora", "Prezzo", "Posti liberi");
        System.out.println(LINEA_SOTTILE);
        for (int i = 0; i < proiezioni.size(); i++) {
            stampaRigaProiezione(i + 1, proiezioni.get(i),
                    app.postiLiberi(proiezioni.get(i)));
        }
        System.out.println(LINEA_SOTTILE);
    }

    /**
     * Stampa i dettagli completi di una prenotazione
     *
     * @param pren --> la prenotazione da visualizzare
     */
    public static void stampaDettagliPrenotazione(Prenotazione pren) {
        System.out.println(LINEA_SOTTILE);
        System.out.printf("  Codice prenot. : %s%n", pren.getCodice());
        System.out.printf("  Cliente        : %s %s (@%s)%n",
                pren.getCliente().getNome(), pren.getCliente().getCognome(),
                pren.getCliente().getUsername());
        System.out.printf("  Film           : %s%n", pren.getProiezione().getFilm().getTitolo());
        System.out.printf("  Data proiezione: %s%n",
                pren.getProiezione().getDataOra().format(FMT_DT));
        System.out.printf("  Posti          : %d%n", pren.getNumeroPosti());
        System.out.printf("  Costo unitario : %.2f €%n",
                pren.getProiezione().getPrezzoBiglietto());
        System.out.printf("  Costo totale   : %.2f €%n", pren.calcolaCostoTotale());
        System.out.printf("  Prenotata il   : %s%n",
                pren.getDataPrenotazione().format(FMT_DT));
        System.out.println(LINEA_SOTTILE);
    }

    /**
     * Stampa una riga riassuntiva di una prenotazione in un elenco numerato
     *
     * @param n --> indice
     * @param pren --> la prenotazione
     */
    public static void stampaRigaPrenotazione(int n, Prenotazione pren) {
        String codiceBreve = pren.getCodice().length() > 8
                ? pren.getCodice().substring(0, 8) + "…"
                : pren.getCodice();
        System.out.printf("  [%2d] %s | %-24s | %s | %2d posti | %.2f€%n",
                n, codiceBreve,
                troncaTesto(pren.getProiezione().getFilm().getTitolo(), 24),
                pren.getProiezione().getDataOra().format(FMT_DT),
                pren.getNumeroPosti(),
                pren.calcolaCostoTotale());
    }

    /**
     * Stampa la lista di prenotazioni con indici per la selezione dell'utente
     *
     * @param prenotazioni --> lista di prenotazioni
     */
    public static void stampaListaPrenotazioni(List<Prenotazione> prenotazioni) {
        if (prenotazioni.isEmpty()) {
            stampaInfo("Nessuna prenotazione trovata.");
            return;
        }
        System.out.println(LINEA_SOTTILE);
        for (int i = 0; i < prenotazioni.size(); i++) {
            stampaRigaPrenotazione(i + 1, prenotazioni.get(i));
        }
        System.out.println(LINEA_SOTTILE);
    }

   // =========================================================================
   // UTILITY
   // =========================================================================

    /**
     * Tronca un testo a una lunghezza massima, aggiungendo "…" se necessario
     *
     * @param testo --> testo da troncare
     * @param max --> lunghezza massima
     * @return il testo (eventualmente troncato)
     */
    private static String troncaTesto(String testo, int max) {
        if (testo == null) return "";
        return testo.length() <= max ? testo : testo.substring(0, max - 1) + "…";
    }
}
