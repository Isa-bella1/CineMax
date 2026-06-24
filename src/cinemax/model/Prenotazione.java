package cinemax.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Rappresenta la prenotazione di uno o più posti, da parte di un
 * {@link Cliente}, per una determinata {@link Proiezione}.
 * <p>
 * Ogni prenotazione possiede un codice identificativo univoco, generato
 * automaticamente alla creazione, che permette ai bigliettai di
 * recuperarla rapidamente (vedi {@code cercaPrenotazione()} nelle
 * specifiche di progetto). Il costo totale della prenotazione non viene
 * memorizzato esplicitamente, ma calcolato a partire dal numero di posti
 * prenotati e dal prezzo del biglietto della proiezione associata, in modo
 * da restare sempre coerente anche se il prezzo della proiezione dovesse
 * cambiare.
 *
 * @author Pornima Macri 766733 [VA]
 */
public class Prenotazione {

    /** Codice identificativo univoco della prenotazione. */
    private String codice;

    /** Cliente che ha effettuato la prenotazione. */
    private Cliente cliente;

    /** Proiezione a cui si riferisce la prenotazione. */
    private Proiezione proiezione;

    /** Numero di posti prenotati. */
    private int numeroPosti;

    /** Data e ora in cui la prenotazione è stata effettuata. */
    private LocalDateTime dataPrenotazione;

    /**
     * Crea una nuova prenotazione, generando automaticamente un codice
     * identificativo univoco e impostando la data di prenotazione
     * all'istante corrente.
     *
     * @param cliente cliente che effettua la prenotazione
     * @param proiezione proiezione per cui si prenota
     * @param numeroPosti numero di posti da prenotare
     */
    public Prenotazione(Cliente cliente, Proiezione proiezione, int numeroPosti) {
        this(UUID.randomUUID().toString(), cliente, proiezione, numeroPosti, LocalDateTime.now());
    }

    /**
     * Crea una nuova prenotazione con codice e data di prenotazione
     * specificati esplicitamente.
     * <p>
     * Questo costruttore è pensato principalmente per il livello di
     * persistenza, quando si ricostruisce una prenotazione già esistente a
     * partire dai dati letti da file.
     *
     * @param codice codice identificativo univoco della prenotazione
     * @param cliente cliente che ha effettuato la prenotazione
     * @param proiezione proiezione a cui si riferisce la prenotazione
     * @param numeroPosti numero di posti prenotati
     * @param dataPrenotazione data e ora in cui la prenotazione è stata effettuata
     */
    public Prenotazione(String codice, Cliente cliente, Proiezione proiezione,
                         int numeroPosti, LocalDateTime dataPrenotazione) {
        this.codice = codice;
        this.cliente = cliente;
        this.proiezione = proiezione;
        this.numeroPosti = numeroPosti;
        this.dataPrenotazione = dataPrenotazione;
    }

    /**
     * Restituisce il codice identificativo univoco della prenotazione.
     *
     * @return il codice della prenotazione
     */
    public String getCodice() {
        return codice;
    }

    /**
     * Restituisce il cliente che ha effettuato la prenotazione.
     *
     * @return il cliente
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Restituisce la proiezione a cui si riferisce la prenotazione.
     *
     * @return la proiezione
     */
    public Proiezione getProiezione() {
        return proiezione;
    }

    /**
     * Imposta la proiezione a cui si riferisce la prenotazione.
     * <p>
     * Usato tipicamente da {@code modificaPrenotazione()} quando il
     * cliente cambia la proiezione prenotata (es. cambio data, vedi
     * specifiche di progetto).
     *
     * @param proiezione la nuova proiezione
     */
    public void setProiezione(Proiezione proiezione) {
        this.proiezione = proiezione;
    }

    /**
     * Restituisce il numero di posti prenotati.
     *
     * @return il numero di posti
     */
    public int getNumeroPosti() {
        return numeroPosti;
    }

    /**
     * Imposta il numero di posti prenotati.
     *
     * @param numeroPosti il nuovo numero di posti
     */
    public void setNumeroPosti(int numeroPosti) {
        this.numeroPosti = numeroPosti;
    }

    /**
     * Restituisce la data e l'ora in cui la prenotazione è stata effettuata.
     *
     * @return la data e ora di prenotazione
     */
    public LocalDateTime getDataPrenotazione() {
        return dataPrenotazione;
    }

    /**
     * Calcola il costo totale della prenotazione, moltiplicando il numero
     * di posti prenotati per il prezzo del biglietto della proiezione
     * associata.
     *
     * @return il costo totale della prenotazione, in euro
     */
    public double calcolaCostoTotale() {
        return numeroPosti * proiezione.getPrezzoBiglietto();
    }

    /**
     * Due prenotazioni sono considerate uguali se hanno lo stesso codice
     * identificativo.
     *
     * @param o l'oggetto con cui confrontare questa prenotazione
     * @return {@code true} se {@code o} è una {@code Prenotazione} con lo stesso codice
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prenotazione)) return false;
        Prenotazione altra = (Prenotazione) o;
        return Objects.equals(codice, altra.codice);
    }

    /**
     * Calcola l'hash code coerentemente con {@link #equals(Object)},
     * basandosi sul codice della prenotazione.
     *
     * @return l'hash code della prenotazione
     */
    @Override
    public int hashCode() {
        return Objects.hash(codice);
    }

    /**
     * Restituisce una rappresentazione testuale sintetica della
     * prenotazione, utile per il debug e per i log.
     *
     * @return una stringa con codice, cliente, proiezione e numero di posti
     */
    @Override
    public String toString() {
        return "Prenotazione {" +
                "codice='" + codice + '\'' +
                ", cliente=" + (cliente != null ? cliente.getUsername() : "null") +
                ", proiezione=" + (proiezione != null ? proiezione.getCodice() : "null") +
                ", numeroPosti=" + numeroPosti +
                '}';
    }
}
