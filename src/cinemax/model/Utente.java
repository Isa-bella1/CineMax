package cinemax.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Rappresenta un utente registrato nel sistema CineMax.
 * <p>
 * È una classe <strong>astratta</strong>: non esiste un utente "generico"
 * nel sistema, ma sempre una sua specializzazione concreta, rappresentata
 * dalle sottoclassi {@link Cliente}, {@link Bigliettaio} e
 * {@link Proiezionista}. Ognuna di esse eredita da {@code Utente} i dati
 * anagrafici e le credenziali comuni a tutti i ruoli.
 * <p>
 * La password viene memorizzata già cifrata (es. tramite hash):
 * la classe non si occupa della cifratura, che è responsabilità di chi
 * costruisce l'oggetto (tipicamente il livello di persistenza o di
 * logica applicativa).
 *
 * @author Pornima Macri 766733 [VA]
 */
public abstract class Utente {

    /** Nome di battesimo dell'utente. */
    private String nome;

    /** Cognome dell'utente. */
    private String cognome;

    /** Username univoco usato per il login. */
    private String username;

    /** Password dell'utente, già cifrata (es. hash SHA-256). */
    private String passwordCifrata;

    /** Data di nascita dell'utente. Campo facoltativo: può essere {@code null}. */
    private LocalDate dataNascita;

    /** Luogo di domicilio dell'utente. */
    private String luogoDomicilio;

    /**
     * Crea un nuovo utente con tutti i dati anagrafici e le credenziali.
     *
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username username univoco per il login
     * @param passwordCifrata password già cifrata
     * @param dataNascita data di nascita, può essere {@code null} se non fornita
     * @param luogoDomicilio luogo di domicilio dell'utente
     */
    protected Utente(String nome, String cognome, String username, String passwordCifrata,
                      LocalDate dataNascita, String luogoDomicilio) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.passwordCifrata = passwordCifrata;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    /**
     * Restituisce il ruolo dell'utente all'interno del sistema.
     * <p>
     * Ogni sottoclasse concreta restituisce il proprio {@link Ruolo}: questo
     * metodo astratto permette di conoscere il ruolo di un utente anche
     * quando viene manipolato tramite un riferimento di tipo {@code Utente}
     * (es. durante il login, prima di sapere a quale sottoclasse appartiene).
     *
     * @return il ruolo dell'utente
     */
    public abstract Ruolo getRuolo();

    /**
     * Restituisce il nome dell'utente.
     *
     * @return il nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome dell'utente.
     *
     * @param nome il nuovo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce il cognome dell'utente.
     *
     * @return il cognome
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * Imposta il cognome dell'utente.
     *
     * @param cognome il nuovo cognome
     */
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    /**
     * Restituisce l'username dell'utente.
     *
     * @return l'username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Imposta l'username dell'utente.
     *
     * @param username il nuovo username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Restituisce la password cifrata dell'utente.
     *
     * @return la password, già cifrata
     */
    public String getPasswordCifrata() {
        return passwordCifrata;
    }

    /**
     * Imposta la password cifrata dell'utente.
     * <p>
     * Il chiamante è responsabile di passare una password già cifrata:
     * questa classe non esegue alcuna cifratura.
     *
     * @param passwordCifrata la nuova password, già cifrata
     */
    public void setPasswordCifrata(String passwordCifrata) {
        this.passwordCifrata = passwordCifrata;
    }

    /**
     * Restituisce la data di nascita dell'utente.
     *
     * @return la data di nascita, oppure {@code null} se non specificata
     */
    public LocalDate getDataNascita() {
        return dataNascita;
    }

    /**
     * Imposta la data di nascita dell'utente.
     *
     * @param dataNascita la nuova data di nascita, può essere {@code null}
     */
    public void setDataNascita(LocalDate dataNascita) {
        this.dataNascita = dataNascita;
    }

    /**
     * Restituisce il luogo di domicilio dell'utente.
     *
     * @return il luogo di domicilio
     */
    public String getLuogoDomicilio() {
        return luogoDomicilio;
    }

    /**
     * Imposta il luogo di domicilio dell'utente.
     *
     * @param luogoDomicilio il nuovo luogo di domicilio
     */
    public void setLuogoDomicilio(String luogoDomicilio) {
        this.luogoDomicilio = luogoDomicilio;
    }

    /**
     * Verifica se la password fornita in chiaro, una volta cifrata con lo
     * stesso meccanismo, corrisponde alla password memorizzata.
     * <p>
     * <strong>Nota:</strong> questo metodo si limita a confrontare due
     * stringhe già cifrate; il calcolo dell'hash della password in chiaro è
     * responsabilità del chiamante (tipicamente nel livello di logica
     * applicativa, dove risiede la funzione di cifratura).
     *
     * @param passwordCifrataDaVerificare la password da verificare, già cifrata
     * @return {@code true} se le due password cifrate coincidono, {@code false} altrimenti
     */
    public boolean verificaPassword(String passwordCifrataDaVerificare) {
        return Objects.equals(this.passwordCifrata, passwordCifrataDaVerificare);
    }

    /**
     * Due utenti sono considerati uguali se hanno lo stesso username, dal
     * momento che l'username è il loro identificativo univoco nel sistema.
     *
     * @param o l'oggetto con cui confrontare questo utente
     * @return {@code true} se {@code o} è un {@code Utente} con lo stesso username
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utente)) return false;
        Utente altro = (Utente) o;
        return Objects.equals(username, altro.username);
    }

    /**
     * Calcola l'hash code coerentemente con {@link #equals(Object)},
     * basandosi sull'username.
     *
     * @return l'hash code dell'utente
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    /**
     * Restituisce una rappresentazione testuale sintetica dell'utente,
     * utile per il debug e per i log.
     *
     * @return una stringa con ruolo, nome, cognome e username dell'utente
     */
    @Override
    public String toString() {
        return getRuolo() + " {" +
                "username='" + username + '\'' +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                '}';
    }
}
