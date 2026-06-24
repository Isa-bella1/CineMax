package cinemax.persistence;

import cinemax.model.Cliente;
import cinemax.model.Film;
import cinemax.model.Prenotazione;
import cinemax.model.Proiezione;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository per la gestione delle prenotazioni su file CSV
 * Formato consigliato:codice,usernameCliente,codiceProiezione,numeroPosti,dataPrenotazione
 *
 * //@authors Wilson Bernal Gaia (766733) e Rahmouni Malek (765952) [VA] 
 */
public class PrenotazioneRepository {

    private final String percorsoFile;

    private static final int CAMPI_MINIMI = 4;
    private static final int IDX_CODICE = 0;
    private static final int IDX_USERNAME_CLIENTE = 1;
    private static final int IDX_CODICE_PROIEZIONE = 2;
    private static final int IDX_NUMERO_POSTI = 3;
    private static final int IDX_DATA_PRENOTAZIONE = 4;

    public PrenotazioneRepository(String percorsoFile) {
        this.percorsoFile = percorsoFile;
    }

    public List<Prenotazione> caricaTutte() {
        List<Prenotazione> prenotazioni = new ArrayList<>();
        List<String> righe;

        try {
            righe = FileManager.leggiRighe(percorsoFile);
        } catch (IOException e) {
            System.err.println("[PrenotazioneRepository] Errore lettura file: " + e.getMessage());
            return prenotazioni;
        }

        for (String riga : righe) {
            try {
                prenotazioni.add(parsaPrenotazione(riga));
            } catch (Exception e) {
                System.err.println("[PrenotazioneRepository] Riga ignorata: " + riga + " | Motivo: " + e.getMessage());
            }
        }

        return prenotazioni;
    }

    public void salvaTutte(List<Prenotazione> prenotazioni) {
        List<String> righe = new ArrayList<>();
        for (Prenotazione p : prenotazioni) {
            righe.add(convertiInRiga(p));
        }

        try {
            FileManager.scriviRighe(percorsoFile, righe);
        } catch (IOException e) {
            System.err.println("[PrenotazioneRepository] Errore scrittura file: " + e.getMessage());
        }
    }

    public void aggiungi(Prenotazione prenotazione) {
        try {
            FileManager.aggiungiRiga(percorsoFile, convertiInRiga(prenotazione));
        } catch (IOException e) {
            System.err.println("[PrenotazioneRepository] Errore aggiunta prenotazione: " + e.getMessage());
        }
    }

    public void aggiorna(Prenotazione prenotazioneAggiornata) {
        List<Prenotazione> tutte = caricaTutte();
        for (int i = 0; i < tutte.size(); i++) {
            if (tutte.get(i).getCodice().equals(prenotazioneAggiornata.getCodice())) {
                tutte.set(i, prenotazioneAggiornata);
                break;
            }
        }
        salvaTutte(tutte);
    }

    public void rimuovi(String codice) {
        List<Prenotazione> tutte = caricaTutte();
        tutte.removeIf(p -> p.getCodice().equalsIgnoreCase(codice));
        salvaTutte(tutte);
    }

    public List<Prenotazione> cercaPerCliente(String usernameCliente) {
        List<Prenotazione> risultati = new ArrayList<>();
        for (Prenotazione p : caricaTutte()) {
            if (p.getCliente().getUsername().equalsIgnoreCase(usernameCliente)) {
                risultati.add(p);
            }
        }
        return risultati;
    }

    public List<Prenotazione> cercaPerProiezione(String codiceProiezione) {
        List<Prenotazione> risultati = new ArrayList<>();
        for (Prenotazione p : caricaTutte()) {
            if (p.getProiezione().getCodice().equalsIgnoreCase(codiceProiezione)) {
                risultati.add(p);
            }
        }
        return risultati;
    }

    private Prenotazione parsaPrenotazione(String riga) {
        String[] campi = FileManager.dividiCampi(riga);
        FileManager.verificaCampi(campi, CAMPI_MINIMI, "prenotazione");

        String codice = campi[IDX_CODICE].trim();
        String usernameCliente = campi[IDX_USERNAME_CLIENTE].trim();
        String codiceProiezione = campi[IDX_CODICE_PROIEZIONE].trim();
        int numeroPosti = Integer.parseInt(campi[IDX_NUMERO_POSTI].trim());
        LocalDateTime dataPrenotazione = leggiDataPrenotazione(campi);

        Cliente cliente = new Cliente("", "", usernameCliente, "", null, "");
        Film filmSegnaposto = new Film("Proiezione " + codiceProiezione, "", "", 0, 0, 0);
        Proiezione proiezione = new Proiezione(codiceProiezione, filmSegnaposto, LocalDateTime.now(), 0.0);

        return new Prenotazione(codice, cliente, proiezione, numeroPosti, dataPrenotazione);
    }

    private LocalDateTime leggiDataPrenotazione(String[] campi) {
        if (campi.length <= IDX_DATA_PRENOTAZIONE || campi[IDX_DATA_PRENOTAZIONE].trim().isEmpty()) {
            return LocalDateTime.now();
        }

        String valore = campi[IDX_DATA_PRENOTAZIONE].trim();
        if (valore.equalsIgnoreCase("true") || valore.equalsIgnoreCase("false")) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(valore);
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }

    private String convertiInRiga(Prenotazione p) {
        return FileManager.costruisciRiga(
                p.getCodice(),
                p.getCliente().getUsername(),
                p.getProiezione().getCodice(),
                String.valueOf(p.getNumeroPosti()),
                p.getDataPrenotazione().toString()
        );
    }
}
