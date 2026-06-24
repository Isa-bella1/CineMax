package cinemax.model;

import java.util.Objects;

/**
 * Rappresenta un film disponibile nel catalogo del cinema.
 * <p>
 * Un {@code Film} contiene esclusivamente i dati anagrafici dell'opera
 * (titolo, genere, regista, anno, durata, età minima del pubblico) e non
 * sa nulla di quando o a quale prezzo viene proiettato: queste informazioni
 * sono responsabilità della classe {@link Proiezione}, che fa riferimento
 * a un'istanza di {@code Film}. In questo modo lo stesso film può essere
 * associato a più proiezioni in giorni e orari diversi, senza duplicarne i
 * dati anagrafici.
 *
 * @author Pornima Macri 766733 [VA]
 */
public class Film {

    /** Titolo del film. */
    private String titolo;

    /** Genere del film (es. "Commedia", "Horror", "Fantascienza"). */
    private String genere;

    /** Regista del film. */
    private String regista;

    /** Anno di uscita del film. */
    private int anno;

    /** Durata del film, espressa in minuti. */
    private int durataMinuti;

    /** Età minima consigliata per il pubblico. */
    private int etaMinima;

    /**
     * Crea un nuovo film con tutti i suoi dati anagrafici.
     *
     * @param titolo titolo del film
     * @param genere genere del film
     * @param regista regista del film
     * @param anno anno di uscita
     * @param durataMinuti durata del film in minuti
     * @param etaMinima età minima consigliata per il pubblico
     */
    public Film(String titolo, String genere, String regista, int anno,
                int durataMinuti, int etaMinima) {
        this.titolo = titolo;
        this.genere = genere;
        this.regista = regista;
        this.anno = anno;
        this.durataMinuti = durataMinuti;
        this.etaMinima = etaMinima;
    }

    /**
     * Restituisce il titolo del film.
     *
     * @return il titolo
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Imposta il titolo del film.
     *
     * @param titolo il nuovo titolo
     */
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    /**
     * Restituisce il genere del film.
     *
     * @return il genere
     */
    public String getGenere() {
        return genere;
    }

    /**
     * Imposta il genere del film.
     *
     * @param genere il nuovo genere
     */
    public void setGenere(String genere) {
        this.genere = genere;
    }

    /**
     * Restituisce il regista del film.
     *
     * @return il regista
     */
    public String getRegista() {
        return regista;
    }

    /**
     * Imposta il regista del film.
     *
     * @param regista il nuovo regista
     */
    public void setRegista(String regista) {
        this.regista = regista;
    }

    /**
     * Restituisce l'anno di uscita del film.
     *
     * @return l'anno di uscita
     */
    public int getAnno() {
        return anno;
    }

    /**
     * Imposta l'anno di uscita del film.
     *
     * @param anno il nuovo anno di uscita
     */
    public void setAnno(int anno) {
        this.anno = anno;
    }

    /**
     * Restituisce la durata del film in minuti.
     *
     * @return la durata in minuti
     */
    public int getDurataMinuti() {
        return durataMinuti;
    }

    /**
     * Imposta la durata del film in minuti.
     *
     * @param durataMinuti la nuova durata in minuti
     */
    public void setDurataMinuti(int durataMinuti) {
        this.durataMinuti = durataMinuti;
    }

    /**
     * Restituisce l'età minima consigliata per il pubblico.
     *
     * @return l'età minima
     */
    public int getEtaMinima() {
        return etaMinima;
    }

    /**
     * Imposta l'età minima consigliata per il pubblico.
     *
     * @param etaMinima la nuova età minima
     */
    public void setEtaMinima(int etaMinima) {
        this.etaMinima = etaMinima;
    }

    /**
     * Due film sono considerati uguali se hanno lo stesso titolo, lo stesso
     * regista e lo stesso anno di uscita: questa combinazione identifica
     * un'opera cinematografica in modo univoco all'interno del catalogo.
     *
     * @param o l'oggetto con cui confrontare questo film
     * @return {@code true} se {@code o} è un {@code Film} con titolo, regista e anno uguali
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Film)) return false;
        Film altro = (Film) o;
        return anno == altro.anno &&
                Objects.equals(titolo, altro.titolo) &&
                Objects.equals(regista, altro.regista);
    }

    /**
     * Calcola l'hash code coerentemente con {@link #equals(Object)},
     * basandosi su titolo, regista e anno.
     *
     * @return l'hash code del film
     */
    @Override
    public int hashCode() {
        return Objects.hash(titolo, regista, anno);
    }

    /**
     * Restituisce una rappresentazione testuale sintetica del film, utile
     * per il debug e per i log.
     *
     * @return una stringa con titolo, genere, regista e anno del film
     */
    @Override
    public String toString() {
        return "Film {" +
                "titolo='" + titolo + '\'' +
                ", genere='" + genere + '\'' +
                ", regista='" + regista + '\'' +
                ", anno=" + anno +
                '}';
    }
}
