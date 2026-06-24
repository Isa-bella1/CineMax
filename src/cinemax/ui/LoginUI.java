package cinemax.ui;
import cinemax.model.Utente;
import cinemax.service.GestoreApp;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Classe che gestisce le schermate di login e registrazione della TUI
 *
 * Fornisce metodi statici per l'autenticazione degli utenti esistenti
 * e la registrazione di nuovi clienti. Tutti i metodi sono statici
 *
 * @author Serrao Isabella (766930) [VA]
 */
public final class LoginUI {

    /** Costruttore privato: classe non istanziabile */
    private LoginUI() {}

   // =========================================================================
   // LOGIN
   // =========================================================================

    /**
     * Mostra la schermata di login e gestisce l'autenticazione
     * Permette all'utente di riprovare in caso di credenziali errate,
     * oppure di tornare al menu principale digitando "0"
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     * @return l'utente autenticato, oppure {@code null} se l'utente torna indietro
     */
    public static Utente eseguiLogin(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("LOGIN");

        while (true) {
            System.out.println();
            MenuUI.stampaInfo("Inserisci le tue credenziali (0 per tornare indietro).");
            System.out.println();

            System.out.print("  Username: ");
            String username = sc.nextLine().trim();
            if (username.equals("0")) return null;

            System.out.print("  Password: ");
            String password = sc.nextLine().trim();
            if (password.equals("0")) return null;

            Utente utente = app.login(username, password);
            if (utente != null) {
                MenuUI.stampaSuccesso("Benvenuto/a, " + utente.getNome()
                        + " " + utente.getCognome()
                        + " [" + utente.getRuolo() + "]!");
                MenuUI.premInvio(sc);
                return utente;
            } else {
                MenuUI.stampaErrore("Username o password errati. Riprova.");
            }
        }
    }

   // =========================================================================
   // REGISTRAZIONE
   // =========================================================================

    /**
     * Mostra il modulo di registrazione di un nuovo cliente
     * Verifica che l'username non sia già in uso e che le due password coincidano prima di procedere
     *
     * @param sc --> lo Scanner per la lettura dell'input
     * @param app --> il gestore dell'applicazione
     * @return {@code true} se la registrazione è avvenuta con successo,
     *         {@code false} se l'utente ha annullato
     */
    public static boolean eseguiRegistrazione(Scanner sc, GestoreApp app) {
        MenuUI.stampaIntestazione("REGISTRAZIONE NUOVO CLIENTE");
        MenuUI.stampaInfo("Compila i seguenti campi (0 in qualsiasi campo per annullare).");
        System.out.println();

        // Nome
        System.out.print("  Nome: ");
        String nome = sc.nextLine().trim();
        if (nome.equals("0")) return false;
        if (nome.isEmpty()) { MenuUI.stampaErrore("Il nome è obbligatorio."); return false; }

        // Cognome
        System.out.print("  Cognome: ");
        String cognome = sc.nextLine().trim();
        if (cognome.equals("0")) return false;
        if (cognome.isEmpty()) { MenuUI.stampaErrore("Il cognome è obbligatorio."); return false; }

        // Username
        System.out.print("  Username: ");
        String username = sc.nextLine().trim();
        if (username.equals("0")) return false;
        if (username.isEmpty()) { MenuUI.stampaErrore("Lo username è obbligatorio."); return false; }

        // Password (con conferma)
        String password;
        while (true) {
            System.out.print("  Password: ");
            password = sc.nextLine().trim();
            if (password.equals("0")) return false;
            if (password.isEmpty()) {
                MenuUI.stampaErrore("La password è obbligatoria.");
                continue;
            }
            System.out.print("  Conferma password: ");
            String conferma = sc.nextLine().trim();
            if (password.equals(conferma)) break;
            MenuUI.stampaErrore("Le password non coincidono. Riprova.");
        }

        // Data di nascita (opzionale)
        LocalDate dataNascita = MenuUI.leggiDataNascita(sc);

        // Luogo di domicilio
        System.out.print("  Luogo di domicilio: ");
        String luogo = sc.nextLine().trim();
        if (luogo.equals("0")) return false;
        if (luogo.isEmpty()) luogo = "N/D";

        // Tentativo di registrazione
        boolean ok = app.registraCliente(nome, cognome, username, password,
                dataNascita, luogo);
        if (ok) {
            MenuUI.stampaSuccesso("Registrazione completata! Ora puoi effettuare il login.");
        } else {
            MenuUI.stampaErrore("Username '" + username + "' già in uso. Scegli un altro username.");
        }
        MenuUI.premInvio(sc);
        return ok;
    }
}
