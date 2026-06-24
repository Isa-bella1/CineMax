package cinemax.model;

import java.time.LocalDate;

/**
 * Rappresenta un proiezionista del cinema, cioè un utente che gestisce il
 * palinsesto, aggiungendo, modificando ed eliminando le proiezioni.
 * <p>
 * Un {@code Proiezionista} non ha attributi propri oltre a quelli ereditati
 * da {@link Utente}.
 *
 * @author Pornima Macri 766733 [VA]
 */
public class Proiezionista extends Utente {

    /**
     * Crea un nuovo proiezionista con tutti i dati anagrafici e le credenziali.
     *
     * @param nome nome del proiezionista
     * @param cognome cognome del proiezionista
     * @param username username univoco per il login
     * @param passwordCifrata password già cifrata
     * @param dataNascita data di nascita, può essere {@code null} se non fornita
     * @param luogoDomicilio luogo di domicilio del proiezionista
     */
    public Proiezionista(String nome, String cognome, String username, String passwordCifrata,
                          LocalDate dataNascita, String luogoDomicilio) {
        super(nome, cognome, username, passwordCifrata, dataNascita, luogoDomicilio);
    }

    /**
     * Restituisce il ruolo {@link Ruolo#PROIEZIONISTA}.
     *
     * @return {@link Ruolo#PROIEZIONISTA}
     */
    @Override
    public Ruolo getRuolo() {
        return Ruolo.PROIEZIONISTA;
    }
}
