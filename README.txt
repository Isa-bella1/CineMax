CineMax - Laboratorio Interdisciplinare A - a.a. 2025/2026
Università degli Studi dell'Insubria

=== REQUISITI DI SISTEMA ===

- Java Development Kit (JDK) versione 17 o superiore
- Sistema operativo: Windows, macOS o Linux (multipiattaforma)

=== STRUTTURA DEL REPOSITORY ===

  autori.txt       - informazioni sugli autori
  README.txt       - questo file
  src/             - codice sorgente Java
  bin/             - file eseguibile (.jar)
  data/            - file CSV dei dati
  doc/             - manuali PDF e JavaDoc generata
  lib/             - eventuali librerie esterne (non presenti)

=== COMPILAZIONE (da Eclipse) ===

1. Aprire Eclipse
2. File > Import > Existing Projects into Workspace
3. Selezionare la cartella radice del progetto
4. Il progetto si chiama "Cine-Max" nell'Eclipse workspace
5. Per esportare il JAR: File > Export > Java > Runnable JAR file
   - Launch configuration: CineMax - Cine-Max
   - Export destination: bin/CineMax.jar

=== ESECUZIONE ===

Dalla cartella radice del progetto, eseguire:

  java -jar bin/CineMax.jar

IMPORTANTE: il comando va eseguito DALLA CARTELLA RADICE del progetto,
non dalla cartella bin/. Il programma cerca i file di dati nel percorso
relativo data/ (es. data/utenti.csv). Se avviato da un'altra cartella
il login fallirà perché il file utenti non viene trovato.

=== CREDENZIALI DI TEST ===

Password di default per tutti gli utenti precaricati: password

Proiezionisti:
  username: mario.rossi
  username: anna.bianchi

Bigliettai:
  username: luigi.verdi
  username: giulia.ferrari
  username: marco.russo
  username: sofia.esposito
  username: andrea.romano

Clienti:
  username: laura.conti