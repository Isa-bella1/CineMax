package cinemax.model;

/**
 * Rappresenta il ruolo di un {@link Utente} all'interno del sistema CineMax.
 * <p>
 * Ogni utente registrato nel sistema possiede esattamente uno di questi
 * ruoli, che ne determina le funzionalità accessibili una volta effettuato
 * il login.
 *
 * @author Pornima Macri 766733 [VA]
 */
public enum Ruolo {

    /** Utente che può cercare proiezioni ed effettuare prenotazioni. */
    CLIENTE,

    /** Utente che gestisce le prenotazioni alla cassa del cinema. */
    BIGLIETTAIO,

    /** Utente che gestisce il palinsesto delle proiezioni. */
    PROIEZIONISTA
}
