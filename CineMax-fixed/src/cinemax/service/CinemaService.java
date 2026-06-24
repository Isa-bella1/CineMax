package cinemax.service;

import cinemax.model.Bigliettaio;
import cinemax.model.Cliente;
import cinemax.model.Film;
import cinemax.model.Prenotazione;
import cinemax.model.Proiezione;
import cinemax.model.Proiezionista;
import cinemax.model.Utente;
import cinemax.persistence.PrenotazioneRepository;
import cinemax.persistence.ProiezioneRepository;
import cinemax.persistence.UtenteRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Classe di servizio principale dell'applicazione CineMax
 *
 * Contiene la logica applicativa e fa da collegamento tra interfaccia utente
 * e repository. In questo modo i menu non lavorano direttamente sui file CSV,
 * ma chiamano metodi di alto livello come login, registrazione, prenotazione
 * e gestione delle proiezioni.
 *
 * @author Serrao Isabella (766930) [VA]
 */
public class CinemaService {

    private static final String FILE_UTENTI = "data/utenti.csv";
    private static final String FILE_PROIEZIONI = "data/proiezioni.csv";
    private static final String FILE_PRENOTAZIONI = "data/prenotazioni.csv";

    private final UtenteRepository utenteRepository;
    private final ProiezioneRepository proiezioneRepository;
    private final PrenotazioneRepository prenotazioneRepository;

    /**
     * Costruttore usato normalmente dall'applicazione
     */
    public CinemaService() {
        this(new UtenteRepository(FILE_UTENTI),
                new ProiezioneRepository(FILE_PROIEZIONI),
                new PrenotazioneRepository(FILE_PRENOTAZIONI));
    }

    /**
     * Costruttore utile anche per eventuali test
     */
    public CinemaService(UtenteRepository utenteRepository,
                         ProiezioneRepository proiezioneRepository,
                         PrenotazioneRepository prenotazioneRepository) {
        this.utenteRepository = utenteRepository;
        this.proiezioneRepository = proiezioneRepository;
        this.prenotazioneRepository = prenotazioneRepository;
    }

    /**
     * Crea alcuni dati iniziali se i file CSV sono vuoti
     */
    public void inizializzaDatiEsempioSeNecessario() {
        if (caricaUtenti().isEmpty()) {
            String password = hashPassword("1234");

            utenteRepository.aggiungi(new Cliente("Mario", "Rossi", "cliente", password,
                    LocalDate.of(2005, 1, 10), "Roma"));
            utenteRepository.aggiungi(new Bigliettaio("Luigi", "Bianchi", "bigliettaio", password,
                    LocalDate.of(1995, 5, 12), "Roma"));
            utenteRepository.aggiungi(new Proiezionista("Anna", "Verdi", "proiezionista", password,
                    LocalDate.of(1990, 3, 20), "Roma"));

            System.out.println("Creati utenti di esempio. Password per tutti: 1234");
        }

        if (caricaProiezioni().isEmpty()) {
            Film film1 = new Film("Inception", "Fantascienza", "Christopher Nolan", 2010, 148, 13);
            Film film2 = new Film("Inside Out 2", "Animazione", "Kelsey Mann", 2024, 96, 6);

            proiezioneRepository.aggiungi(new Proiezione(film1,
                    LocalDateTime.of(LocalDate.of(2026, 7, 1), LocalTime.of(20, 30)), 8.50));
            proiezioneRepository.aggiungi(new Proiezione(film2,
                    LocalDateTime.of(LocalDate.of(2026, 7, 2), LocalTime.of(18, 0)), 7.00));
        }
    }

    /**
     * Esegue il login. Restituisce null se le credenziali non sono corrette
     */
    public Utente login(String username, String passwordChiara) {
        if (username == null || passwordChiara == null) {
            return null;
        }

        String passwordCifrata = hashPassword(passwordChiara);

        for (Utente utente : caricaUtenti()) {
            if (utente.getUsername().equalsIgnoreCase(username)) {
                boolean passwordCorretta = utente.verificaPassword(passwordCifrata)
                        || utente.verificaPassword(passwordChiara);

                if (passwordCorretta) {
                    return utente;
                }
            }
        }

        return null;
    }

    public boolean usernameEsiste(String username) {
        if (username == null) {
            return false;
        }

        for (Utente utente : caricaUtenti()) {
            if (utente.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public Cliente registraCliente(String nome, String cognome, String username,
                                   String passwordChiara, LocalDate dataNascita,
                                   String luogoDomicilio) {
        if (isVuoto(nome) || isVuoto(cognome) || isVuoto(username) || isVuoto(passwordChiara)) {
            throw new IllegalArgumentException("Nome, cognome, username e password sono obbligatori.");
        }

        if (usernameEsiste(username)) {
            throw new IllegalArgumentException("Username gia' esistente.");
        }

        Cliente cliente = new Cliente(nome, cognome, username, hashPassword(passwordChiara),
                dataNascita, luogoDomicilio == null ? "" : luogoDomicilio);
        utenteRepository.aggiungi(cliente);
        return cliente;
    }

    public List<Proiezione> elencoProiezioni() {
        return caricaProiezioni();
    }

    public List<Proiezione> elencoProiezioniOrdinate() {
        List<Proiezione> proiezioni = caricaProiezioni();
        proiezioni.sort(Comparator.comparing(Proiezione::getDataOra));
        return proiezioni;
    }

    public List<Proiezione> cercaProiezioniPerTitolo(String titolo) {
        List<Proiezione> risultati = new ArrayList<>();
        String filtro = titolo == null ? "" : titolo.toLowerCase();

        for (Proiezione proiezione : caricaProiezioni()) {
            if (proiezione.getFilm().getTitolo().toLowerCase().contains(filtro)) {
                risultati.add(proiezione);
            }
        }

        return risultati;
    }

    public Proiezione trovaProiezione(String codice) {
        if (codice == null) {
            return null;
        }

        for (Proiezione proiezione : caricaProiezioni()) {
            if (proiezione.getCodice().equalsIgnoreCase(codice)) {
                return proiezione;
            }
        }
        return null;
    }

    public Proiezione aggiungiProiezione(String titolo, String genere, String regista,
                                         int anno, int durataMinuti, int etaMinima,
                                         LocalDateTime dataOra, double prezzoBiglietto) {
        if (isVuoto(titolo) || isVuoto(genere) || isVuoto(regista)) {
            throw new IllegalArgumentException("Titolo, genere e regista sono obbligatori.");
        }
        if (durataMinuti <= 0) {
            throw new IllegalArgumentException("La durata deve essere maggiore di zero.");
        }
        if (prezzoBiglietto < 0) {
            throw new IllegalArgumentException("Il prezzo non puo' essere negativo.");
        }
        if (dataOra == null) {
            throw new IllegalArgumentException("La data e ora della proiezione sono obbligatorie.");
        }

        Film film = new Film(titolo, genere, regista, anno, durataMinuti, etaMinima);
        Proiezione nuova = new Proiezione(film, dataOra, prezzoBiglietto);

        if (haSovrapposizioni(nuova, null)) {
            throw new IllegalArgumentException("La proiezione si sovrappone a un'altra gia' presente.");
        }

        proiezioneRepository.aggiungi(nuova);
        return nuova;
    }

    public boolean modificaDataOraProiezione(String codice, LocalDateTime nuovaDataOra) {
        Proiezione proiezione = trovaProiezione(codice);
        if (proiezione == null) {
            return false;
        }
        if (nuovaDataOra == null) {
            throw new IllegalArgumentException("La nuova data e ora sono obbligatorie.");
        }

        Proiezione proposta = new Proiezione(proiezione.getCodice(), proiezione.getFilm(),
                nuovaDataOra, proiezione.getPrezzoBiglietto());

        if (haSovrapposizioni(proposta, proiezione.getCodice())) {
            throw new IllegalArgumentException("La nuova data/ora si sovrappone a un'altra proiezione.");
        }

        proiezione.setDataOra(nuovaDataOra);
        proiezioneRepository.aggiorna(proiezione);
        return true;
    }

    public boolean modificaPrezzoProiezione(String codice, double nuovoPrezzo) {
        Proiezione proiezione = trovaProiezione(codice);
        if (proiezione == null) {
            return false;
        }
        if (nuovoPrezzo < 0) {
            throw new IllegalArgumentException("Il prezzo non puo' essere negativo.");
        }

        proiezione.setPrezzoBiglietto(nuovoPrezzo);
        proiezioneRepository.aggiorna(proiezione);
        return true;
    }

    public boolean rimuoviProiezione(String codice) {
        Proiezione proiezione = trovaProiezione(codice);
        if (proiezione == null) {
            return false;
        }

        if (!prenotazioniProiezione(codice).isEmpty()) {
            throw new IllegalStateException("Non puoi eliminare questa proiezione: esistono prenotazioni collegate.");
        }

        proiezioneRepository.rimuovi(codice);
        return true;
    }

    public Prenotazione prenota(Cliente cliente, String codiceProiezione, int numeroPosti) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente non valido.");
        }
        if (numeroPosti <= 0) {
            throw new IllegalArgumentException("Il numero di posti deve essere maggiore di zero.");
        }

        Proiezione proiezione = trovaProiezione(codiceProiezione);
        if (proiezione == null) {
            throw new IllegalArgumentException("Proiezione non trovata.");
        }

        int postiDisponibili = calcolaPostiDisponibili(codiceProiezione);
        if (numeroPosti > postiDisponibili) {
            throw new IllegalArgumentException("Posti insufficienti per questa proiezione.");
        }

        Prenotazione prenotazione = new Prenotazione(cliente, proiezione, numeroPosti);
        prenotazioneRepository.aggiungi(prenotazione);
        return prenotazione;
    }

    public List<Prenotazione> elencoPrenotazioni() {
        return completaPrenotazioni(caricaPrenotazioni());
    }

    public Prenotazione trovaPrenotazione(String codice) {
        if (codice == null) {
            return null;
        }

        for (Prenotazione prenotazione : caricaPrenotazioni()) {
            if (prenotazione.getCodice().equalsIgnoreCase(codice)) {
                return completaPrenotazione(prenotazione);
            }
        }
        return null;
    }

    public List<Prenotazione> prenotazioniCliente(String usernameCliente) {
        List<Prenotazione> risultati = new ArrayList<>();
        if (usernameCliente == null) {
            return risultati;
        }

        for (Prenotazione prenotazione : caricaPrenotazioni()) {
            if (prenotazione.getCliente() != null
                    && prenotazione.getCliente().getUsername().equalsIgnoreCase(usernameCliente)) {
                risultati.add(completaPrenotazione(prenotazione));
            }
        }
        return risultati;
    }

    public List<Prenotazione> prenotazioniProiezione(String codiceProiezione) {
        List<Prenotazione> risultati = new ArrayList<>();
        if (codiceProiezione == null) {
            return risultati;
        }

        for (Prenotazione prenotazione : caricaPrenotazioni()) {
            if (prenotazione.getProiezione() != null
                    && prenotazione.getProiezione().getCodice().equalsIgnoreCase(codiceProiezione)) {
                risultati.add(completaPrenotazione(prenotazione));
            }
        }
        return risultati;
    }

    public int calcolaPostiPrenotati(String codiceProiezione) {
        int totale = 0;
        for (Prenotazione prenotazione : prenotazioniProiezione(codiceProiezione)) {
            totale += prenotazione.getNumeroPosti();
        }
        return totale;
    }

    public int calcolaPostiDisponibili(String codiceProiezione) {
        return Math.max(0, Proiezione.CAPIENZA_SALA - calcolaPostiPrenotati(codiceProiezione));
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder risultato = new StringBuilder();
            for (byte b : hash) {
                risultato.append(String.format("%02x", b));
            }
            return risultato.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 non disponibile", e);
        }
    }

    private boolean haSovrapposizioni(Proiezione nuova, String codiceDaIgnorare) {
        for (Proiezione esistente : caricaProiezioni()) {
            if (codiceDaIgnorare != null && esistente.getCodice().equalsIgnoreCase(codiceDaIgnorare)) {
                continue;
            }
            if (nuova.siSovrapponeA(esistente)) {
                return true;
            }
        }
        return false;
    }

    private Prenotazione completaPrenotazione(Prenotazione prenotazione) {
        Cliente cliente = prenotazione.getCliente();
        if (cliente != null) {
            Utente utente = trovaUtente(cliente.getUsername());
            if (utente instanceof Cliente) {
                cliente = (Cliente) utente;
            }
        }

        Proiezione proiezione = prenotazione.getProiezione();
        if (proiezione != null) {
            Proiezione completa = trovaProiezione(proiezione.getCodice());
            if (completa != null) {
                proiezione = completa;
            }
        }

        return new Prenotazione(prenotazione.getCodice(), cliente, proiezione,
                prenotazione.getNumeroPosti(), prenotazione.getDataPrenotazione());
    }

    private List<Prenotazione> completaPrenotazioni(List<Prenotazione> prenotazioni) {
        List<Prenotazione> complete = new ArrayList<>();
        for (Prenotazione prenotazione : prenotazioni) {
            complete.add(completaPrenotazione(prenotazione));
        }
        return complete;
    }

    private Utente trovaUtente(String username) {
        if (username == null) {
            return null;
        }

        for (Utente utente : caricaUtenti()) {
            if (utente.getUsername().equalsIgnoreCase(username)) {
                return utente;
            }
        }
        return null;
    }

    private boolean isVuoto(String testo) {
        return testo == null || testo.trim().isEmpty();
    }

    @SuppressWarnings("unchecked")
    private List<Utente> caricaUtenti() {
        return new ArrayList<Utente>(utenteRepository.caricaTutti());
    }

    @SuppressWarnings("unchecked")
    private List<Proiezione> caricaProiezioni() {
        return new ArrayList<Proiezione>(proiezioneRepository.caricaTutte());
    }

    @SuppressWarnings("unchecked")
    private List<Prenotazione> caricaPrenotazioni() {
        return new ArrayList<Prenotazione>(prenotazioneRepository.caricaTutte());
    }
}
