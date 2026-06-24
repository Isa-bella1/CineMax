package cinemax.service;
import cinemax.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GESTORE PRINCIPALE (applicazione CineMax)
 *
 * Costituisce il livello di servizio (service layer) che fa da
 * intermediario tra la TUI (Terminal User Interface) e i dati persistiti su file CSV. 
 * Gestisce in memoria le liste di {@link Utente}, {@link Proiezione} e
 * {@link Prenotazione}, e le sincronizza con i rispettivi file CSV ad ogni operazione di scrittura.
 *
 * <strong>Nota tecnica:</strong> 
 * Le classi del package {@code cinemax.persistence} presenti nel repository hanno un'API (intermediario)
 * incompatibile con il model layer (es. {@code Film(titolo,durata,genere)}
 * invece di {@code Film(titolo,genere,regista,anno,durataMinuti,etaMinima)},
 * metodi {@code getPasswordHash()} e {@code getUsernameCliente()} non
 * presenti nel model)
 * Per garantire il corretto funzionamento, questo service layer 
 * gestisce direttamente la persistenza CSV usando l'API reale del model
 * 
 * Gaia e Malek hanno allineato i loro repository al model definitivo prima dell'integrazione finale
 *
 * <h3>Formato file CSV</h3>
 * <ul>
 *   <li>{@code utenti.csv}:
 *       {@code TIPO,nome,cognome,username,passwordHash,dataNascita,luogoDomicilio}</li>
 *   <li>{@code proiezioni.csv}:
 *       {@code codice,titolo,genere,regista,anno,durataMinuti,etaMinima,dataOra,prezzoBiglietto}</li>
 *   <li>{@code prenotazioni.csv}:
 *       {@code codice,usernameCliente,codiceProiezione,numeroPosti,dataPrenotazione}</li>
 * </ul>
 * Date e ore nel formato {@code dd/MM/yyyy HH:mm}; date di nascita nel
 * formato {@code yyyy-MM-dd}; campo {@code dataNascita} può essere vuoto
 *
 * @author Serrao Isabella (766930) [VA]
 */

public class GestoreApp {

// -------------------------------------------------------------------------
// COSTANTI DI FORMATO
    
    /** Separatore campi nei file CSV. */
    private static final String SEP = ",";

    /** Formato data-ora per proiezioni e prenotazioni. */
    public static final DateTimeFormatter FMT_DT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Formato data per le date di nascita. */
    private static final DateTimeFormatter FMT_D =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

 // -------------------------------------------------------------------------
 // PERCORSI DEI FILE 
    
    /** Percorso file CSV degli utenti. */
    private final String pathUtenti;

    /** Percorso file CSV delle proiezioni. */
    private final String pathProiezioni;

    /** Percorso file CSV delle prenotazioni. */
    private final String pathPrenotazioni;

  // -------------------------------------------------------------------------
  // STATO IN MEMORIA
    
    /** Lista di tutti gli utenti registrati. */
    private final List<Utente> utenti = new ArrayList<>();

    /** Lista di tutte le proiezioni programmate. */
    private final List<Proiezione> proiezioni = new ArrayList<>();

    /** Lista di tutte le prenotazioni effettuate. */
    private final List<Prenotazione> prenotazioni = new ArrayList<>();

    /** Utente attualmente autenticato, {@code null} se nessuno è loggato. */
    private Utente utenteLoggato = null;

  // =========================================================================
  // COSTRUTTORE
  // =========================================================================

    /**
     * Crea il gestore e carica immediatamente i dati dai file CSV
     *
     * @param pathUtenti --> percorso del file degli utenti
     * @param pathProiezioni --> percorso del file delle proiezioni
     * @param pathPrenotazioni --> Percorso del file delle prenotazioni
     */
    
    public GestoreApp(String pathUtenti, String pathProiezioni, String pathPrenotazioni) {
        this.pathUtenti = pathUtenti;
        this.pathProiezioni = pathProiezioni;
        this.pathPrenotazioni = pathPrenotazioni;
        caricaDati();
    }

   // =========================================================================
   // AUTENTICAZIONE
   // =========================================================================

    /**
     * Esegue il login con username e password in chiaro
     * La password viene cifrata (SHA-256) prima del confronto
     *
     * @param username username dell'utente
     * @param password password in chiaro
     * @return l'utente autenticato, {@code null} se le credenziali sono errate
     */
    public Utente login(String username, String password) {
        String hash = hashPassword(password);
        for (Utente u : utenti) {
            if (u.getUsername().equals(username) && u.verificaPassword(hash)) {
                utenteLoggato = u;
                return u;
            }
        }
        return null;
    }

    /**
     * Esegue il logout dell'utente corrente
     */
    public void logout() {
        utenteLoggato = null;
    }

    /**
     * Restituisce l'utente attualmente loggato
     *
     * @return l'utente loggato, {@code null} se nessuno è autenticato
     */
    public Utente getUtenteLoggato() {
        return utenteLoggato;
    }

   // =========================================================================
   // REGISTRAZIONE
   // =========================================================================

    /**
     * Registra un nuovo cliente nel sistema, verificando che l'username
     * non sia già in uso
     *
     * @param nome --> nome del cliente
     * @param cognome --> cognome del cliente
     * @param username --> username scelto
     * @param password --> password in chiaro (cifrata internamente con SHA-256)
     * @param dataNascita --> data di nascita, può essere {@code null}
     * @param luogoDomicilio --> luogo di domicilio
     * @return {@code true}  --> se la registrazione è avvenuta con successo,
     *         {@code false} --> se l'username è già in uso
     */
    public boolean registraCliente(String nome, String cognome, String username,
                                   String password, LocalDate dataNascita,
                                   String luogoDomicilio) {
        for (Utente u : utenti) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        Cliente c = new Cliente(nome, cognome, username,
                hashPassword(password), dataNascita, luogoDomicilio);
        utenti.add(c);
        salvaUtenti();
        return true;
    }

   // =========================================================================
   // PROIEZIONI - ACCESSO (guest e utenti registrati)
   // =========================================================================

    /**
     * Restituisce tutte le proiezioni disponibili nel sistema
     *
     * @return lista non modificabile di tutte le proiezioni
     */
    public List<Proiezione> getTutteProiezioni() {
        return Collections.unmodifiableList(proiezioni);
    }

    /**
     * Cerca proiezioni applicando i criteri forniti in modo combinato
     * Un parametro {@code null} o vuoto (stringhe) / negativo (numeri)
     * indica che quel criterio non viene applicato
     *
     * @param titolo --> parte del titolo del film (case-insensitive)
     * @param genere --> genere esatto del film (case-insensitive)
     * @param dataMin --> data minima della proiezione (inclusiva), {@code null} = nessun limite
     * @param dataMax --> data massima della proiezione (inclusiva), {@code null} = nessun limite
     * @param prezzoMin --> prezzo minimo in euro ({@code -1} = nessun limite)
     * @param prezzoMax --> prezzo massimo in euro ({@code -1} = nessun limite)
     * @return lista di proiezioni che soddisfano tutti i criteri
     */
    public List<Proiezione> cercaProiezioni(String titolo, String genere,
                                            LocalDate dataMin, LocalDate dataMax,
                                            double prezzoMin, double prezzoMax) {
        return proiezioni.stream().filter(p -> {
            Film f = p.getFilm();
            if (nonVuoto(titolo) &&
                !f.getTitolo().toLowerCase().contains(titolo.toLowerCase()))
                return false;
            if (nonVuoto(genere) &&
                !f.getGenere().equalsIgnoreCase(genere))
                return false;
            LocalDate data = p.getDataOra().toLocalDate();
            if (dataMin != null && data.isBefore(dataMin)) return false;
            if (dataMax != null && data.isAfter(dataMax))  return false;
            if (prezzoMin >= 0 && p.getPrezzoBiglietto() < prezzoMin) return false;
            if (prezzoMax >= 0 && p.getPrezzoBiglietto() > prezzoMax) return false;
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Calcola il numero di posti liberi per una proiezione, sottraendo dalla
     * capienza totale il totale dei posti già prenotati
     *
     * @param proiezione --> la proiezione di cui calcolare la disponibilità
     * @return numero di posti ancora disponibili
     */
    public int postiLiberi(Proiezione proiezione) {
        int occupati = prenotazioni.stream()
                .filter(p -> p.getProiezione().getCodice().equals(proiezione.getCodice()))
                .mapToInt(Prenotazione::getNumeroPosti)
                .sum();
        return Proiezione.CAPIENZA_SALA - occupati;
    }

    /**
     * Cerca una proiezione per codice.
     *
     * @param codice --> codice della proiezione
     * @return la proiezione trovata, {@code null} se non esiste
     */
    public Proiezione trovaProiezione(String codice) {
        return proiezioni.stream()
                .filter(p -> p.getCodice().equals(codice))
                .findFirst().orElse(null);
    }

   // =========================================================================
   // PROIEZIONI - GESTIONE (solo Proiezionista)
   // =========================================================================

    /**
     * Aggiunge una nuova proiezione al palinsesto, verificando che non si
     * sovrapponga a proiezioni già esistenti
     *
     * @param film --> film da proiettare
     * @param dataOra --> data e ora di inizio
     * @param prezzoBiglietto --> prezzo del biglietto in euro
     * @return {@code true}  se aggiunta con successo,
     *         {@code false} se si sovrappone a una proiezione esistente
     */
    public boolean aggiungiProiezione(Film film, LocalDateTime dataOra,
                                      double prezzoBiglietto) {
        Proiezione nuova = new Proiezione(film, dataOra, prezzoBiglietto);
        for (Proiezione esistente : proiezioni) {
            if (esistente.siSovrapponeA(nuova)) return false;
        }
        proiezioni.add(nuova);
        salvaProiezioni();
        return true;
    }

    /**
     * Modifica data e ora di una proiezione esistente
     * (Consentita solo se non ha prenotazioni e il nuovo orario non si sovrappone ad altre proiezioni)
     *
     * @param codice --> codice della proiezione da modificare
     * @param nuovaDataOra --> nuova data e ora
     * @return {@code true} se la modifica è avvenuta con successo,
     *         {@code false} altrimenti (non trovata, ha prenotazioni, sovrapposizione)
     */
    public boolean modificaProiezione(String codice, LocalDateTime nuovaDataOra) {
        Proiezione target = trovaProiezione(codice);
        if (target == null) return false;
        if (!prenotazioniPerProiezione(codice).isEmpty()) return false;
        Proiezione simulata = new Proiezione(codice, target.getFilm(),
                nuovaDataOra, target.getPrezzoBiglietto());
        for (Proiezione p : proiezioni) {
            if (!p.getCodice().equals(codice) && p.siSovrapponeA(simulata))
                return false;
        }
        target.setDataOra(nuovaDataOra);
        salvaProiezioni();
        return true;
    }

    /**
     * Elimina una proiezione dal palinsesto
     * (Consentita solo se non esistono prenotazioni per quella proiezione)
     *
     * @param --> codice codice della proiezione da eliminare
     * @return {@code true} se eliminata con successo,
     *         {@code false} se non trovata o con prenotazioni associate
     */
    public boolean eliminaProiezione(String codice) {
        if (!prenotazioniPerProiezione(codice).isEmpty()) return false;
        boolean rimosso = proiezioni.removeIf(p -> p.getCodice().equals(codice));
        if (rimosso) salvaProiezioni();
        return rimosso;
    }

   // =========================================================================
   // PRENOTAZIONI - GESTIONE (solo Cliente)
   // =========================================================================

    /**
     * Crea una nuova prenotazione per un cliente, verificando la disponibilità dei posti richiesti
     *
     * @param cliente --> cliente che prenota
     * @param proiezione --> proiezione da prenotare
     * @param numeroPosti--> numero di posti richiesti
     * @return {@code true} se la prenotazione è stata creata,
     *         {@code false} se i posti richiesti eccedono quelli disponibili
     */
    public boolean creaPrenotazione(Cliente cliente, Proiezione proiezione,
                                    int numeroPosti) {
        if (numeroPosti <= 0) return false;
        if (postiLiberi(proiezione) < numeroPosti) return false;
        prenotazioni.add(new Prenotazione(cliente, proiezione, numeroPosti));
        salvaPrenotazioni();
        return true;
    }

    /**
     * Modifica la proiezione associata a una prenotazione (cambio data).
     * (Sia la vecchia che la nuova proiezione devono essere in data futura)
     *
     * @param codice --> codice della prenotazione da modificare
     * @param nuovaProiezione --> nuova proiezione scelta
     * @return {@code true} se la modifica è avvenuta con successo,
     *         {@code false} altrimenti
     */
    public boolean modificaPrenotazione(String codice, Proiezione nuovaProiezione) {
        Prenotazione target = trovaPrenotazione(codice);
        if (target == null) return false;
        LocalDateTime ora = LocalDateTime.now();
        if (!target.getProiezione().getDataOra().isAfter(ora)) return false;
        if (!nuovaProiezione.getDataOra().isAfter(ora)) return false;
        if (postiLiberi(nuovaProiezione) < target.getNumeroPosti()) return false;
        target.setProiezione(nuovaProiezione);
        salvaPrenotazioni();
        return true;
    }

    /**
     * Cancella una prenotazione
     * Consentita solo se la proiezione è in data futura (non si può cancellare dopo il film).
     *
     * @param codice --> codice della prenotazione da cancellare
     * @return {@code true} se cancellata con successo, {@code false} altrimenti
     */
    public boolean eliminaPrenotazione(String codice) {
        Prenotazione target = trovaPrenotazione(codice);
        if (target == null) return false;
        if (!target.getProiezione().getDataOra().isAfter(LocalDateTime.now()))
            return false;
        prenotazioni.remove(target);
        salvaPrenotazioni();
        return true;
    }

    /**
     * Restituisce tutte le prenotazioni di un cliente
     *
     * @param username --> username del cliente
     * @return lista delle prenotazioni del cliente
     */
    public List<Prenotazione> prenotazioniCliente(String username) {
        return prenotazioni.stream()
                .filter(p -> p.getCliente().getUsername().equals(username))
                .collect(Collectors.toList());
    }

    /**
     * Cerca una prenotazione per codice
     *
     * @param codice --> codice della prenotazione
     * @return la prenotazione trovata, {@code null} se non esiste
     */
    public Prenotazione trovaPrenotazione(String codice) {
        return prenotazioni.stream()
                .filter(p -> p.getCodice().equals(codice))
                .findFirst().orElse(null);
    }

   // =========================================================================
   // PRENOTAZIONI - RICERCA (solo Bigliettaio)
   // =========================================================================

    /**
     * Restituisce le prenotazioni per le proiezioni del giorno odierno
     *
     * @return lista delle prenotazioni di oggi
     */
    public List<Prenotazione> prenotazioniOggi() {
        LocalDate oggi = LocalDate.now();
        return prenotazioni.stream()
                .filter(p -> p.getProiezione().getDataOra().toLocalDate().equals(oggi))
                .collect(Collectors.toList());
    }

    /**
     * Cerca prenotazioni combinando criteri opzionali
     *
     * @param codice --> parte del codice prenotazione
     * @param nomeCliente --> parte del nome cliente (case-insensitive)
     * @param cognomeCliente --> parte del cognome cliente (case-insensitive)
     * @param titoloFilm --> parte del titolo film (case-insensitive)
     * @param dataMin --> data minima proiezione, {@code null} = nessun limite
     * @param dataMax --> data massima proiezione, {@code null} = nessun limite
     * @return lista di prenotazioni corrispondenti
     */
    public List<Prenotazione> cercaPrenotazioni(String codice, String nomeCliente,
                                                String cognomeCliente, String titoloFilm,
                                                LocalDate dataMin, LocalDate dataMax) {
        return prenotazioni.stream().filter(p -> {
            if (nonVuoto(codice) &&
                !p.getCodice().toLowerCase().contains(codice.toLowerCase()))
                return false;
            Cliente c = p.getCliente();
            if (nonVuoto(nomeCliente) &&
                !c.getNome().toLowerCase().contains(nomeCliente.toLowerCase()))
                return false;
            if (nonVuoto(cognomeCliente) &&
                !c.getCognome().toLowerCase().contains(cognomeCliente.toLowerCase()))
                return false;
            if (nonVuoto(titoloFilm) &&
                !p.getProiezione().getFilm().getTitolo().toLowerCase()
                        .contains(titoloFilm.toLowerCase()))
                return false;
            LocalDate data = p.getProiezione().getDataOra().toLocalDate();
            if (dataMin != null && data.isBefore(dataMin)) return false;
            if (dataMax != null && data.isAfter(dataMax))  return false;
            return true;
        }).collect(Collectors.toList());
    }

   // =========================================================================
   // UTILITY PRIVATI
   // =========================================================================

    /**
     * Restituisce le prenotazioni associate a una specifica proiezione
     *
     * @param codiceProiezione --> codice della proiezione
     * @return lista delle prenotazioni per quella proiezione
     */
    private List<Prenotazione> prenotazioniPerProiezione(String codiceProiezione) {
        return prenotazioni.stream()
                .filter(p -> p.getProiezione().getCodice().equals(codiceProiezione))
                .collect(Collectors.toList());
    }

    /**
     * Verifica se una stringa è non nulla e non vuota
     *
     * @param s --> la stringa da verificare
     * @return {@code true} se la stringa contiene almeno un carattere
     */
    private boolean nonVuoto(String s) {
        return s != null && !s.isBlank();
    }

   // =========================================================================
   // HASH PASSWORD
   // =========================================================================

    /**
     * Calcola l'hash SHA-256 di una password in chiaro
     *
     * @param password --> la password in chiaro
     * @return la stringa esadecimale dell'hash SHA-256
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // fallback (non dovrebbe mai accadere)
        }
    }

   // =========================================================================
   // CARICAMENTO DATI
   // =========================================================================

    /**
     * Carica tutti i dati dai file CSV all'avvio dell'applicazione
     */
    private void caricaDati() {
        caricaUtenti();
        caricaProiezioni();
        caricaPrenotazioni();
    }

    /**
     * Carica gli utenti dal file CSV
     * Formato riga: {@code TIPO,nome,cognome,username,passwordHash,dataNascita,luogoDomicilio}
     */
    private void caricaUtenti() {
        for (String riga : leggiRighe(pathUtenti)) {
            try {
                String[] c = riga.split(SEP, -1);
                if (c.length < 7) continue;
                String tipo     = c[0].trim().toUpperCase();
                String nome     = c[1].trim();
                String cognome  = c[2].trim();
                String username = c[3].trim();
                String hash     = c[4].trim();
                String dataN    = c[5].trim();
                String luogo    = c[6].trim();
                LocalDate dn    = dataN.isEmpty() ? null : LocalDate.parse(dataN, FMT_D);
                switch (tipo) {
                    case "CLIENTE":
                        utenti.add(new Cliente(nome, cognome, username, hash, dn, luogo));
                        break;
                    case "BIGLIETTAIO":
                        utenti.add(new Bigliettaio(nome, cognome, username, hash, dn, luogo));
                        break;
                    case "PROIEZIONISTA":
                        utenti.add(new Proiezionista(nome, cognome, username, hash, dn, luogo));
                        break;
                    default:
                        System.err.println("[GestoreApp] Tipo utente sconosciuto: " + tipo);
                }
            } catch (Exception e) {
                System.err.println("[GestoreApp] Riga utente ignorata: " + riga);
            }
        }
    }

    /**
     * Carica le proiezioni dal file CSV
     * Formato riga: {@code codice,titolo,genere,regista,anno,durataMinuti,etaMinima,dataOra,prezzoBiglietto}
     */
    private void caricaProiezioni() {
        for (String riga : leggiRighe(pathProiezioni)) {
            try {
                String[] c  = riga.split(SEP, -1);
                if (c.length < 9) continue;
                String codice  = c[0].trim();
                String titolo  = c[1].trim();
                String genere  = c[2].trim();
                String regista = c[3].trim();
                int anno       = Integer.parseInt(c[4].trim());
                int durata     = Integer.parseInt(c[5].trim());
                int etaMin     = Integer.parseInt(c[6].trim());
                LocalDateTime dt = LocalDateTime.parse(c[7].trim(), FMT_DT);
                double prezzo  = Double.parseDouble(c[8].trim());
                Film film = new Film(titolo, genere, regista, anno, durata, etaMin);
                proiezioni.add(new Proiezione(codice, film, dt, prezzo));
            } catch (Exception e) {
                System.err.println("[GestoreApp] Riga proiezione ignorata: " + riga);
            }
        }
    }

    /**
     * Carica le prenotazioni dal file CSV, risolvendo i riferimenti a utenti
     * e proiezioni tramite le mappe costruite sulle liste già caricate
     * Formato riga: {@code codice,usernameCliente,codiceProiezione,numeroPosti,dataPrenotazione}
     */
    private void caricaPrenotazioni() {
        Map<String, Cliente> clientiMap = new HashMap<>();
        for (Utente u : utenti) {
            if (u instanceof Cliente) clientiMap.put(u.getUsername(), (Cliente) u);
        }
        Map<String, Proiezione> proiezioniMap = new HashMap<>();
        for (Proiezione p : proiezioni) proiezioniMap.put(p.getCodice(), p);

        for (String riga : leggiRighe(pathPrenotazioni)) {
            try {
                String[] c = riga.split(SEP, -1);
                if (c.length < 5) continue;
                String codice    = c[0].trim();
                String usrCl     = c[1].trim();
                String codPr     = c[2].trim();
                int posti        = Integer.parseInt(c[3].trim());
                LocalDateTime dt = LocalDateTime.parse(c[4].trim(), FMT_DT);
                Cliente cl = clientiMap.get(usrCl);
                Proiezione pr = proiezioniMap.get(codPr);
                if (cl == null || pr == null) {
                    System.err.println("[GestoreApp] Riferimento non risolto: " + riga);
                    continue;
                }
                prenotazioni.add(new Prenotazione(codice, cl, pr, posti, dt));
            } catch (Exception e) {
                System.err.println("[GestoreApp] Riga prenotazione ignorata: " + riga);
            }
        }
    }

   // =========================================================================
   // SALVATAGGIO DATI
   // =========================================================================

    /**
     * Salva tutti gli utenti nel file CSV
     */
    private void salvaUtenti() {
        List<String> righe = new ArrayList<>();
        for (Utente u : utenti) {
            String tipo;
            if (u instanceof Cliente) tipo = "CLIENTE";
            else if (u instanceof Bigliettaio) tipo = "BIGLIETTAIO";
            else tipo = "PROIEZIONISTA";
            String dn = u.getDataNascita() != null ? u.getDataNascita().format(FMT_D) : "";
            righe.add(String.join(SEP, tipo, u.getNome(), u.getCognome(),
                    u.getUsername(), u.getPasswordCifrata(), dn, u.getLuogoDomicilio()));
        }
        scriviRighe(pathUtenti, righe);
    }

    /**
     * Salva tutte le proiezioni nel file CSV
     */
    private void salvaProiezioni() {
        List<String> righe = new ArrayList<>();
        for (Proiezione p : proiezioni) {
            Film f = p.getFilm();
            righe.add(String.join(SEP,
                    p.getCodice(), f.getTitolo(), f.getGenere(), f.getRegista(),
                    String.valueOf(f.getAnno()), String.valueOf(f.getDurataMinuti()),
                    String.valueOf(f.getEtaMinima()),
                    p.getDataOra().format(FMT_DT),
                    String.valueOf(p.getPrezzoBiglietto())));
        }
        scriviRighe(pathProiezioni, righe);
    }

    /**
     * Salva tutte le prenotazioni nel file CSV
     */
    private void salvaPrenotazioni() {
        List<String> righe = new ArrayList<>();
        for (Prenotazione p : prenotazioni) {
            righe.add(String.join(SEP,
                    p.getCodice(),
                    p.getCliente().getUsername(),
                    p.getProiezione().getCodice(),
                    String.valueOf(p.getNumeroPosti()),
                    p.getDataPrenotazione().format(FMT_DT)));
        }
        scriviRighe(pathPrenotazioni, righe);
    }

   // =========================================================================
   // I/O FILE CSV
   // =========================================================================

    /**
     * Legge le righe di un file CSV, ignorando righe vuote e commenti ({@code #})
     *
     * @param percorso --> percorso del file
     * @return lista di righe grezze
     */
    private List<String> leggiRighe(String percorso) {
        List<String> out = new ArrayList<>();
        File file = new File(percorso);
        if (!file.exists()) return out;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String riga;
            while ((riga = br.readLine()) != null) {
                riga = riga.trim();
                if (!riga.isEmpty() && !riga.startsWith("#")) out.add(riga);
            }
        } catch (IOException e) {
            System.err.println("[GestoreApp] Errore lettura " + percorso + ": " + e.getMessage());
        }
        return out;
    }

    /**
     * Sovrascrive un file CSV con le righe fornite, creando file e directory se non esistono
     *
     * @param percorso --> percorso del file
     * @param righe --> righe da scrivere
     */
    private void scriviRighe(String percorso, List<String> righe) {
        File file = new File(percorso);
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false),
                        StandardCharsets.UTF_8))) {
            for (String r : righe) { bw.write(r); bw.newLine(); }
        } catch (IOException e) {
            System.err.println("[GestoreApp] Errore scrittura " + percorso + ": " + e.getMessage());
        }
    }
}
