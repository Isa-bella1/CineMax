package cinemax.model;

import java.time.LocalDate;

/**
 * Rappresenta un bigliettaio del cinema, cioè un utente che può cercare e
 * visualizzare le prenotazioni effettuate dai clienti, ad esempio per la
 * gestione dell'accesso in sala.
 * <p>
 * Un {@code Bigliettaio} non ha attributi propri oltre a quelli ereditati
 * da {@link Utente}.
 *
 * @author Pornima Macri 766733 [VA]
 */
public class Bigliettaio extends Utente {

    /**
     * Crea un nuovo bigliettaio con tutti i dati anagrafici e le credenziali.
     *
     * @param nome nome del bigliettaio
     * @param cognome cognome del bigliettaio
     * @param username username univoco per il login
     * @param passwordCifrata password già cifrata
     * @param dataNascita data di nascita, può essere {@code null} se non fornita
     * @param luogoDomicilio luogo di domicilio del bigliettaio
     */
    public Bigliettaio(String nome, String cognome, String username, String passwordCifrata,
                        LocalDate dataNascita, String luogoDomicilio) {
        super(nome, cognome, username, passwordCifrata, dataNascita, luogoDomicilio);
    }

    /**
     * Restituisce il ruolo {@link Ruolo#BIGLIETTAIO}.
     *
     * @return {@link Ruolo#BIGLIETTAIO}
     */
    @Override
    public Ruolo getRuolo() {
        return Ruolo.BIGLIETTAIO;
    }
}
