package cinemax;

import cinemax.model.*;
import cinemax.service.GestoreApp;
import cinemax.ui.*;

import java.util.Scanner;


/**
 * Classe principale dell'applicazione CineMax
 *
 * Contiene il metodo {@link #main(String[])} che avvia l'applicazione,
 * inizializza il {@link GestoreApp} con i percorsi dei file CSV e gestisce
 * il loop del menu iniziale, instradando l'utente verso il menu appropriato
 * in base al ruolo
 *
 * <h3>Menu iniziale</h3>
 * <ul>
 *   <li><b>Login</b>: autentica un utente esistente e lo reindirizza al menu
 *       del proprio ruolo (Cliente, Bigliettaio o Proiezionista).</li>
 *   <li><b>Registrazione</b>: crea un nuovo account cliente.</li>
 *   <li><b>Ospite</b>: accede al palinsesto senza autenticazione, indicando
 *       il titolo (anche parziale) di un film.</li>
 *   <li><b>Esci</b>: chiude l'applicazione.</li>
 * </ul>
 *
 * <h3>Percorsi dei file di dati</h3>
 * I file CSV vengono cercati nella directory {@code data/} relativa alla
 * directory di lavoro corrente (da cui si esegue il JAR)
 *
 * @author Serrao Isabella (766930) [VA]
 */
public class CineMax {

    /** Percorso del file CSV degli utenti */
    private static final String PATH_UTENTI = "data/utenti.csv";

    /** Percorso del file CSV delle proiezioni */
    private static final String PATH_PROIEZIONI = "data/proiezioni.csv";

    /** Percorso del file CSV delle prenotazioni */
    private static final String PATH_PRENOTAZIONI = "data/prenotazioni.csv";

    /**
     * Punto di ingresso dell'applicazione.
     *
     * Inizializza il gestore dell'applicazione, crea lo Scanner per l'input da terminale e avvia il loop del menu principale
     *
     * @param args --> argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        GestoreApp app = new GestoreApp(PATH_UTENTI, PATH_PROIEZIONI, PATH_PRENOTAZIONI);
        Scanner sc = new Scanner(System.in);

        MenuUI.stampaLogo();
        System.out.println("  Benvenuto in CineMax — il sistema di gestione del cinema!");
        System.out.println("  " + MenuUI.LINEA_SOTTILE);

        boolean esci = false;
        while (!esci) {
            MenuUI.stampaIntestazione("MENU PRINCIPALE");
            MenuUI.stampaOpzioni(
                    "Login",
                    "Registrati come cliente",
                    "Accedi come ospite (cerca un film)"
            );
            MenuUI.stampaOpzioneZero("Esci");

            int scelta = MenuUI.leggiScelta(sc, "Scelta:");

            switch (scelta) {
                case 1:
                    gestisciLogin(sc, app);
                    break;

                case 2:
                    LoginUI.eseguiRegistrazione(sc, app);
                    break;

                case 3:
                    gestisciOspite(sc, app);
                    break;

                case 0:
                    esci = true;
                    break;

                default:
                    MenuUI.stampaErrore("Opzione non valida. Scegli tra 0 e 3.");
            }
        }

        MenuUI.stampaIntestazione("ARRIVEDERCI");
        System.out.println("  Grazie per aver usato CineMax. A presto!");
        System.out.println("  " + MenuUI.LINEA);
        sc.close();
    }

   // =========================================================================
   // GESTIONE LOGIN E ROUTING PER RUOLO
   // =========================================================================

    /**
     * Esegue il login e reindirizza l'utente autenticato al menu del proprio ruolo
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     */
    private static void gestisciLogin(Scanner sc, GestoreApp app) {
        Utente utente = LoginUI.eseguiLogin(sc, app);
        if (utente == null) return; // utente ha scelto di tornare indietro

        switch (utente.getRuolo()) {
            case CLIENTE:
                ClienteMenu.mostra(sc, app, (Cliente) utente);
                break;
            case BIGLIETTAIO:
                BigliettaioMenu.mostra(sc, app, (Bigliettaio) utente);
                break;
            case PROIEZIONISTA:
                ProiezionistaMenu.mostra(sc, app, (Proiezionista) utente);
                break;
            default:
                MenuUI.stampaErrore("Ruolo non riconosciuto: " + utente.getRuolo());
        }
    }

   // =========================================================================
   // GESTIONE ACCESSO (ospite)
   // =========================================================================

    /**
     * Chiede all'ospite il titolo (anche parziale) di un film e avvia il menu ospite
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     */
    private static void gestisciOspite(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("ACCESSO OSPITE");
        System.out.println();
        System.out.print("  Inserisci il titolo (o parte del titolo) di un film: ");
        String titolo = sc.nextLine().trim();
        if (titolo.isEmpty()) {
            MenuUI.stampaInfo("Titolo non inserito. Tornando al menu principale.");
            return;
        }
        GuestMenu.mostra(sc, app, titolo);
    }
}
