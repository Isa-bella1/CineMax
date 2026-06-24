package cinemax.persistence;

import cinemax.model.Bigliettaio;
import cinemax.model.Cliente;
import cinemax.model.Proiezionista;
import cinemax.model.Utente;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository per la gestione degli utenti su file CSV
 * Formato: TIPO,nome,cognome,username,passwordHash,dataNascita,luogoDomicilio
 *
 * //@authors Wilson Bernal Gaia (766733) e Rahmouni Malek (765952) [VA]
 */
public class UtenteRepository {

    private final String percorsoFile;

    private static final int CAMPI_MINIMI = 5;
    private static final int IDX_TIPO = 0;
    private static final int IDX_NOME = 1;
    private static final int IDX_COGNOME = 2;
    private static final int IDX_USERNAME = 3;
    private static final int IDX_PASSWORD = 4;
    private static final int IDX_DATA_NASCITA = 5;
    private static final int IDX_LUOGO_DOMICILIO = 6;

    public UtenteRepository(String percorsoFile) {
        this.percorsoFile = percorsoFile;
    }

    public List<Utente> caricaTutti() {
        List<Utente> utenti = new ArrayList<>();
        List<String> righe;

        try {
            righe = FileManager.leggiRighe(percorsoFile);
        } catch (IOException e) {
            System.err.println("[UtenteRepository] Errore lettura file: " + e.getMessage());
            return utenti;
        }

        for (String riga : righe) {
            try {
                Utente utente = parsaUtente(riga);
                if (utente != null) {
                    utenti.add(utente);
                }
            } catch (Exception e) {
                System.err.println("[UtenteRepository] Riga ignorata: " + riga + " | Motivo: " + e.getMessage());
            }
        }

        return utenti;
    }

    public void salvaTutti(List<Utente> utenti) {
        List<String> righe = new ArrayList<>();
        for (Utente utente : utenti) {
            righe.add(convertiInRiga(utente));
        }

        try {
            FileManager.scriviRighe(percorsoFile, righe);
        } catch (IOException e) {
            System.err.println("[UtenteRepository] Errore scrittura file: " + e.getMessage());
        }
    }

    public void aggiungi(Utente utente) {
        try {
            FileManager.aggiungiRiga(percorsoFile, convertiInRiga(utente));
        } catch (IOException e) {
            System.err.println("[UtenteRepository] Errore aggiunta utente: " + e.getMessage());
        }
    }

    private Utente parsaUtente(String riga) {
        String[] campi = FileManager.dividiCampi(riga);
        FileManager.verificaCampi(campi, CAMPI_MINIMI, "utente");

        String tipo = campi[IDX_TIPO].trim().toUpperCase();
        String username = campi[IDX_USERNAME].trim();
        String passwordCifrata = campi[IDX_PASSWORD].trim();
        String nome = campi[IDX_NOME].trim();
        String cognome = campi[IDX_COGNOME].trim();
        LocalDate dataNascita = leggiDataNascita(campi);
        String luogoDomicilio = campi.length > IDX_LUOGO_DOMICILIO ? campi[IDX_LUOGO_DOMICILIO].trim() : "";

        switch (tipo) {
            case "CLIENTE":
                return new Cliente(nome, cognome, username, passwordCifrata, dataNascita, luogoDomicilio);
            case "BIGLIETTAIO":
                return new Bigliettaio(nome, cognome, username, passwordCifrata, dataNascita, luogoDomicilio);
            case "PROIEZIONISTA":
                return new Proiezionista(nome, cognome, username, passwordCifrata, dataNascita, luogoDomicilio);
            default:
                System.err.println("[UtenteRepository] Tipo utente sconosciuto: " + tipo);
                return null;
        }
    }

    private LocalDate leggiDataNascita(String[] campi) {
        if (campi.length <= IDX_DATA_NASCITA || campi[IDX_DATA_NASCITA].trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(campi[IDX_DATA_NASCITA].trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String convertiInRiga(Utente utente) {
        String tipo;
        if (utente instanceof Cliente) {
            tipo = "CLIENTE";
        } else if (utente instanceof Bigliettaio) {
            tipo = "BIGLIETTAIO";
        } else if (utente instanceof Proiezionista) {
            tipo = "PROIEZIONISTA";
        } else {
            tipo = "SCONOSCIUTO";
        }

        String dataNascita = utente.getDataNascita() != null ? utente.getDataNascita().toString() : "";
        String luogoDomicilio = utente.getLuogoDomicilio() != null ? utente.getLuogoDomicilio() : "";

        return FileManager.costruisciRiga(
                tipo,
                utente.getNome(),
                utente.getCognome(),
                utente.getUsername(),
                utente.getPasswordCifrata(),
                dataNascita,
                luogoDomicilio
        );
    }
}
