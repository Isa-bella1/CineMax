================================================================================
  CineMax — Sistema di Gestione Cinema
  Laboratorio Interdisciplinare A — a.a. 2025/2026
  Università degli Studi dell'Insubria
================================================================================

REQUISITI DI SISTEMA
--------------------
- Java JDK 11 o superiore (consigliato JDK 17+)
  Download: https://adoptium.net/
- Sistema operativo: Windows / macOS / Linux (multipiattaforma)

Verifica installazione Java:
  java -version
  javac -version


STRUTTURA DEL REPOSITORY
-------------------------
CineMax/
├── src/
│   └── cinemax/
│       ├── CineMax.java                  ← classe main (punto di ingresso)
│       ├── model/                        ← classi di dominio (Pornima Macri)
│       │   ├── Film.java
│       │   ├── Proiezione.java
│       │   ├── Prenotazione.java
│       │   ├── Utente.java (astratta)
│       │   ├── Cliente.java
│       │   ├── Bigliettaio.java
│       │   ├── Proiezionista.java
│       │   └── Ruolo.java
│       ├── persistence/                  ← persistenza CSV (Gaia Wilson Bernal, Malek Rahmouni)
│       │   ├── FileManager.java
│       │   ├── ProiezioneRepository.java
│       │   ├── PrenotazioneRepository.java
│       │   └── UtenteRepository.java
│       ├── service/                      ← service layer (Isabella Serrao)
│       │   └── GestoreApp.java
│       └── ui/                           ← interfaccia terminale (Isabella Serrao)
│           ├── MenuUI.java
│           ├── LoginUI.java
│           ├── GuestMenu.java
│           ├── ClienteMenu.java
│           ├── BigliettaioMenu.java
│           └── ProiezionistaMenu.java
├── data/
│   ├── utenti.csv                        ← utenti (2 proiezionisti, 5 bigliettai, clienti)
│   ├── proiezioni.csv                    ← proiezioni programmate
│   └── prenotazioni.csv                  ← prenotazioni effettuate
├── bin/                                  ← file .class e .jar (generati dalla compilazione)
├── doc/                                  ← documentazione (JavaDoc, manuali PDF)
├── lib/                                  ← librerie esterne (se necessarie)
├── autori.txt
└── README.txt


COMPILAZIONE
------------
Aprire un terminale (cmd su Windows, Terminal su macOS/Linux) nella cartella
radice del progetto (dove si trova questo README.txt).

1. Creare la directory di output per i .class (se non esiste):
   Windows:  mkdir bin
   Linux/Mac: mkdir -p bin

2. Compilare tutti i sorgenti:
   Windows:
     javac -encoding UTF-8 -d bin src\cinemax\model\*.java src\cinemax\service\*.java src\cinemax\ui\*.java src\cinemax\CineMax.java

   Linux/Mac:
     javac -encoding UTF-8 -d bin src/cinemax/model/*.java src/cinemax/service/*.java src/cinemax/ui/*.java src/cinemax/CineMax.java

   In caso di errori di compilazione, verificare la versione Java:
     javac -version   (deve essere 11 o superiore)


ESECUZIONE
----------
IMPORTANTE: eseguire dalla cartella radice del progetto affinché i file CSV
in data/ vengano trovati correttamente.

  java -cp bin cinemax.CineMax


CREAZIONE DEL FILE .JAR
-----------------------
Dopo la compilazione:

  jar --create --file bin/CineMax.jar --main-class cinemax.CineMax -C bin .

Esecuzione del JAR (sempre dalla cartella radice):

  java -jar bin/CineMax.jar


ESECUZIONE DA ECLIPSE
---------------------
1. File → Import → Existing Projects into Workspace → selezionare la cartella CineMax
2. Verificare che i sorgenti siano in src/ e il build path sia configurato su bin/
3. Tasto destro su CineMax.java → Run As → Java Application
4. Assicurarsi che la "Working directory" nelle Run Configurations sia impostata
   su "${workspace_loc:Cine-Max}" (cartella radice del progetto)


UTENTI DI ESEMPIO
-----------------
Tutti gli utenti di esempio usano la password: password123

  Ruolo           Username             Password
  --------------- -------------------- ---------------
  Proiezionista   mario.rossi          password123
  Proiezionista   anna.bianchi         password123
  Bigliettaio     luigi.verdi          password123
  Bigliettaio     giulia.ferrari       password123
  Bigliettaio     marco.russo          password123
  Bigliettaio     sofia.esposito       password123
  Bigliettaio     andrea.romano        password123
  Cliente         laura.conti          password123


FORMATO DEI FILE CSV
--------------------
utenti.csv:
  TIPO,nome,cognome,username,passwordHash,dataNascita,luogoDomicilio
  - TIPO: CLIENTE | BIGLIETTAIO | PROIEZIONISTA
  - passwordHash: SHA-256 della password in chiaro
  - dataNascita: yyyy-MM-dd oppure vuoto

proiezioni.csv:
  codice,titolo,genere,regista,anno,durataMinuti,etaMinima,dataOra,prezzoBiglietto
  - codice: UUID univoco
  - dataOra: dd/MM/yyyy HH:mm

prenotazioni.csv:
  codice,usernameCliente,codiceProiezione,numeroPosti,dataPrenotazione
  - codice: UUID univoco
  - dataPrenotazione: dd/MM/yyyy HH:mm

Le righe che iniziano con # sono commenti e vengono ignorate.


NOTE TECNICHE
-------------
- Le password vengono cifrate con SHA-256 prima del salvataggio.
- I codici di proiezione e prenotazione sono UUID generati automaticamente.
- La capacità della sala è fissa: 200 posti (Proiezione.CAPIENZA_SALA).
- Il controllo di sovrapposizione tra proiezioni tiene conto della durata del film.
- Le prenotazioni si possono cancellare solo se la proiezione è ancora futura.
- Le prenotazioni si possono modificare (cambio proiezione) solo se
  sia la vecchia che la nuova proiezione sono future.
- Le proiezioni si possono modificare/eliminare solo se non hanno prenotazioni.

NOTA SULLA PERSISTENCE LAYER:
  I file in src/cinemax/persistence/ (scritti da Gaia e Malek) sono in fase di
  allineamento con il model layer definitivo. Il GestoreApp gestisce direttamente
  la persistenza CSV finché i repository non saranno aggiornati all'API del model.


GENERAZIONE JAVADOC
-------------------
  Windows:
    javadoc -encoding UTF-8 -charset UTF-8 -d doc -sourcepath src -subpackages cinemax

  Linux/Mac:
    javadoc -encoding UTF-8 -charset UTF-8 -d doc -sourcepath src -subpackages cinemax

La documentazione HTML viene generata nella cartella doc/.


================================================================================
  Autore README: Serrao Isabella (766930) [VA] — Laboratorio Interdisciplinare A 2025/2026
================================================================================
