package cinemax.persistence;

import cinemax.model.Film;
import cinemax.model.Proiezione;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Repository per la gestione delle proiezioni su file CSV
 * Formato: codice,titoloFilm,durataMinuti,genere,data,oraInizio,prezzoBiglietto,sala
 *
 * Nota: la classe Proiezione del model non contiene il campo sala; nel CSV viene salvato "1".
 *
 * //@author Wilson Bernal Gaia (766890) [VA]
* // @author Rahmouni Malek (765952) [VA]
 */
public class ProiezioneRepository {

    private final String percorsoFile;

    private static final int CAMPI_MINIMI = 8;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_ORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final int IDX_CODICE = 0;
    private static final int IDX_TITOLO = 1;
    private static final int IDX_DURATA = 2;
    private static final int IDX_GENERE = 3;
    private static final int IDX_DATA = 4;
    private static final int IDX_ORA_INIZIO = 5;
    private static final int IDX_PREZZO = 6;

    public ProiezioneRepository(String percorsoFile) {
        this.percorsoFile = percorsoFile;
    }

    public List<Proiezione> caricaTutte() {
        List<Proiezione> proiezioni = new ArrayList<>();
        List<String> righe;

        try {
            righe = FileManager.leggiRighe(percorsoFile);
        } catch (IOException e) {
            System.err.println("[ProiezioneRepository] Errore lettura file: " + e.getMessage());
            return proiezioni;
        }

        for (String riga : righe) {
            try {
                proiezioni.add(parsaProiezione(riga));
            } catch (Exception e) {
                System.err.println("[ProiezioneRepository] Riga ignorata: " + riga + " | Motivo: " + e.getMessage());
            }
        }

        return proiezioni;
    }

    public void salvaTutte(List<Proiezione> proiezioni) {
        List<String> righe = new ArrayList<>();
        for (Proiezione p : proiezioni) {
            righe.add(convertiInRiga(p));
        }

        try {
            FileManager.scriviRighe(percorsoFile, righe);
        } catch (IOException e) {
            System.err.println("[ProiezioneRepository] Errore scrittura file: " + e.getMessage());
        }
    }

    public void aggiungi(Proiezione proiezione) {
        try {
            FileManager.aggiungiRiga(percorsoFile, convertiInRiga(proiezione));
        } catch (IOException e) {
            System.err.println("[ProiezioneRepository] Errore aggiunta proiezione: " + e.getMessage());
        }
    }

    public void aggiorna(Proiezione proiezioneAggiornata) {
        List<Proiezione> tutte = caricaTutte();
        for (int i = 0; i < tutte.size(); i++) {
            if (tutte.get(i).getCodice().equals(proiezioneAggiornata.getCodice())) {
                tutte.set(i, proiezioneAggiornata);
                break;
            }
        }
        salvaTutte(tutte);
    }

    public void rimuovi(String codice) {
        List<Proiezione> tutte = caricaTutte();
        tutte.removeIf(p -> p.getCodice().equalsIgnoreCase(codice));
        salvaTutte(tutte);
    }

    private Proiezione parsaProiezione(String riga) {
        String[] campi = FileManager.dividiCampi(riga);
        FileManager.verificaCampi(campi, CAMPI_MINIMI, "proiezione");

        String codice = campi[IDX_CODICE].trim();
        String titolo = campi[IDX_TITOLO].trim();
        int durata = Integer.parseInt(campi[IDX_DURATA].trim());
        String genere = campi[IDX_GENERE].trim();
        LocalDate data = LocalDate.parse(campi[IDX_DATA].trim(), FORMATO_DATA);
        LocalTime ora = LocalTime.parse(campi[IDX_ORA_INIZIO].trim(), FORMATO_ORA);
        double prezzo = Double.parseDouble(campi[IDX_PREZZO].trim().replace(',', '.'));

        Film film = new Film(titolo, genere, "Non specificato", 0, durata, 0);
        LocalDateTime dataOra = LocalDateTime.of(data, ora);
        return new Proiezione(codice, film, dataOra, prezzo);
    }

    private String convertiInRiga(Proiezione p) {
        return FileManager.costruisciRiga(
                p.getCodice(),
                p.getFilm().getTitolo(),
                String.valueOf(p.getFilm().getDurataMinuti()),
                p.getFilm().getGenere(),
                p.getDataOra().toLocalDate().format(FORMATO_DATA),
                p.getDataOra().toLocalTime().format(FORMATO_ORA),
                String.format(Locale.US, "%.2f", p.getPrezzoBiglietto()),
                "1"
        );
    }
}
