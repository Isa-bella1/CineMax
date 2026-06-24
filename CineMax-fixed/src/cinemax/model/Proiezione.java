package cinemax.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Rappresenta una proiezione, cioè la programmazione di un {@link Film} in
 * una data e ora specifiche, con un determinato prezzo del biglietto.
 * <p>
 * La sala del cinema è monosala con capienza fissa di
 * {@value #CAPIENZA_SALA} posti (vedi le specifiche di progetto). Il numero
 * di posti effettivamente liberi per una proiezione non è un dato
 * memorizzato in questa classe, ma viene calcolato dal livello di business
 * logic in base al numero di {@link Prenotazione} esistenti per la
 * proiezione: la classe {@code Proiezione} si limita a esporre la capienza
 * massima della sala tramite {@link #getCapienzaSala()}.
 *
 * @author Pornima Macri 766733 [VA]
 */
public class Proiezione {

    /** Capienza fissa della sala del cinema, in numero di posti. */
    public static final int CAPIENZA_SALA = 200;

    /** Identificativo univoco della proiezione. */
    private String codice;

    /** Film proiettato. */
    private Film film;

    /** Data e ora in cui si svolge la proiezione. */
    private LocalDateTime dataOra;

    /** Prezzo del biglietto per questa proiezione, in euro. */
    private double prezzoBiglietto;

    /**
     * Crea una nuova proiezione, generando automaticamente un codice
     * identificativo univoco.
     *
     * @param film film da proiettare
     * @param dataOra data e ora della proiezione
     * @param prezzoBiglietto prezzo del biglietto in euro
     */
    public Proiezione(Film film, LocalDateTime dataOra, double prezzoBiglietto) {
        this(UUID.randomUUID().toString(), film, dataOra, prezzoBiglietto);
    }

    /**
     * Crea una nuova proiezione con un codice identificativo specificato
     * esplicitamente.
     * <p>
     * Questo costruttore è pensato principalmente per il livello di
     * persistenza, quando si ricostruisce una proiezione già esistente a
     * partire dai dati letti da file (in quel caso il codice non deve
     * essere rigenerato, ma riletto cosi' com'è).
     *
     * @param codice codice identificativo univoco della proiezione
     * @param film film da proiettare
     * @param dataOra data e ora della proiezione
     * @param prezzoBiglietto prezzo del biglietto in euro
     */
    public Proiezione(String codice, Film film, LocalDateTime dataOra, double prezzoBiglietto) {
        this.codice = codice;
        this.film = film;
        this.dataOra = dataOra;
        this.prezzoBiglietto = prezzoBiglietto;
    }

    /**
     * Restituisce il codice identificativo univoco della proiezione.
     *
     * @return il codice della proiezione
     */
    public String getCodice() {
        return codice;
    }

    /**
     * Restituisce il film proiettato.
     *
     * @return il film
     */
    public Film getFilm() {
        return film;
    }

    /**
     * Imposta il film proiettato.
     *
     * @param film il nuovo film
     */
    public void setFilm(Film film) {
        this.film = film;
    }

    /**
     * Restituisce la data e l'ora della proiezione.
     *
     * @return la data e ora di proiezione
     */
    public LocalDateTime getDataOra() {
        return dataOra;
    }

    /**
     * Imposta la data e l'ora della proiezione.
     *
     * @param dataOra la nuova data e ora di proiezione
     */
    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    /**
     * Restituisce il prezzo del biglietto per questa proiezione.
     *
     * @return il prezzo del biglietto in euro
     */
    public double getPrezzoBiglietto() {
        return prezzoBiglietto;
    }

    /**
     * Imposta il prezzo del biglietto per questa proiezione.
     *
     * @param prezzoBiglietto il nuovo prezzo del biglietto in euro
     */
    public void setPrezzoBiglietto(double prezzoBiglietto) {
        this.prezzoBiglietto = prezzoBiglietto;
    }

    /**
     * Restituisce la capienza massima della sala.
     * <p>
     * Il numero di posti effettivamente liberi per <em>questa</em>
     * proiezione va calcolato sottraendo a questo valore il totale dei
     * posti già prenotati, informazione che non è responsabilità di questa
     * classe (vedi la documentazione della classe {@link Proiezione}).
     *
     * @return la capienza della sala, pari a {@value #CAPIENZA_SALA}
     */
    public int getCapienzaSala() {
        return CAPIENZA_SALA;
    }

    /**
     * Verifica se questa proiezione si sovrappone temporalmente a un'altra,
     * tenendo conto della durata del film.
     * <p>
     * Due proiezioni si sovrappongono se l'intervallo di tempo occupato
     * dalla prima (dalla sua {@code dataOra} fino a {@code dataOra + durata
     * del film}) ha un'intersezione non vuota con l'intervallo occupato
     * dalla seconda. Questo controllo è pensato per essere usato dal
     * livello di business logic quando si aggiunge una nuova proiezione
     * (vedi {@code aggiungiProiezione()} nelle specifiche di progetto).
     *
     * @param altra l'altra proiezione con cui verificare la sovrapposizione
     * @return {@code true} se le due proiezioni si sovrappongono temporalmente
     */
    public boolean siSovrapponeA(Proiezione altra) {
        LocalDateTime inizioQuesta = this.dataOra;
        LocalDateTime fineQuesta = this.dataOra.plusMinutes(this.film.getDurataMinuti());
        LocalDateTime inizioAltra = altra.dataOra;
        LocalDateTime fineAltra = altra.dataOra.plusMinutes(altra.film.getDurataMinuti());

        return inizioQuesta.isBefore(fineAltra) && inizioAltra.isBefore(fineQuesta);
    }

    /**
     * Due proiezioni sono considerate uguali se hanno lo stesso codice
     * identificativo.
     *
     * @param o l'oggetto con cui confrontare questa proiezione
     * @return {@code true} se {@code o} è una {@code Proiezione} con lo stesso codice
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proiezione)) return false;
        Proiezione altra = (Proiezione) o;
        return Objects.equals(codice, altra.codice);
    }

    /**
     * Calcola l'hash code coerentemente con {@link #equals(Object)},
     * basandosi sul codice della proiezione.
     *
     * @return l'hash code della proiezione
     */
    @Override
    public int hashCode() {
        return Objects.hash(codice);
    }

    /**
     * Restituisce una rappresentazione testuale sintetica della proiezione,
     * utile per il debug e per i log.
     *
     * @return una stringa con codice, titolo del film, data/ora e prezzo
     */
    @Override
    public String toString() {
        return "Proiezione {" +
                "codice='" + codice + '\'' +
                ", film=" + (film != null ? film.getTitolo() : "null") +
                ", dataOra=" + dataOra +
                ", prezzoBiglietto=" + prezzoBiglietto +
                '}';
    }
}
