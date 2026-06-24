package cinemax.model;

import java.time.LocalDate;

/**
 * Rappresenta un cliente del cinema, cioè un utente che può cercare
 * proiezioni ed effettuare prenotazioni di posti.
 * <p>
 * Un {@code Cliente} non ha attributi propri oltre a quelli ereditati da
 * {@link Utente}: la sua specificità sta nel comportamento che il sistema
 * gli consente (gestito a livello di logica applicativa), non nei dati che
 * porta con sé.
 *
 * @author Pornima Macri 766733 [VA]
 */
public class Cliente extends Utente {

    /**
     * Crea un nuovo cliente con tutti i dati anagrafici e le credenziali.
     *
     * @param nome nome del cliente
     * @param cognome cognome del cliente
     * @param username username univoco per il login
     * @param passwordCifrata password già cifrata
     * @param dataNascita data di nascita, può essere {@code null} se non fornita
     * @param luogoDomicilio luogo di domicilio del cliente
     */
    public Cliente(String nome, String cognome, String username, String passwordCifrata,
                    LocalDate dataNascita, String luogoDomicilio) {
        super(nome, cognome, username, passwordCifrata, dataNascita, luogoDomicilio);
    }

    /**
     * Restituisce il ruolo {@link Ruolo#CLIENTE}.
     *
     * @return {@link Ruolo#CLIENTE}
     */
    @Override
    public Ruolo getRuolo() {
        return Ruolo.CLIENTE;
    }
}
