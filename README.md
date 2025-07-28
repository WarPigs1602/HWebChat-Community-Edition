# HWebChat Community Edition

HWebChat ist eine Community-basierte Chat-Lösung für das Web.  
**Hinweis:** Die Chat-Konfiguration ist aktuell auf Deutsch, kann aber angepasst werden.

## Voraussetzungen

- **Apache Tomcat** ist für die Ausführung erforderlich.
- **.homewebcom**-Verzeichnis muss im Benutzerverzeichnis liegen.
- **Netbeans IDE** wird empfohlen zum Kompilieren (im `src`-Verzeichnis).
- **MySQL** oder **MariaDB** für die Datenbank.

## Installation

1. Klone dieses Repository:
   ```bash
   git clone https://github.com/WarPigs1602/HWebChat-Community-Edition.git
   ```
2. Lege das Verzeichnis `.homewebcom` in deinem Benutzerverzeichnis an (falls nicht vorhanden).
3. Öffne das Projekt mit Netbeans und kompiliere es im `src`-Verzeichnis zu einer **.war-Datei**.
4. Importiere die `database.sql` Datei in deine MySQL oder MariaDB Datenbank:
   ```bash
   mysql -u <user> -p <datenbankname> < path/to/database.sql
   ```
5. Passe ggf. die Konfiguration im Verzeichnis `.homewebcom` an deine Bedürfnisse an (Standard: Deutsch).
6. Deploye die erstellte **.war-Datei** in deinen **Tomcat**-Server (`webapps`-Verzeichnis).

## Konfiguration

- Die Konfigurationsdateien befinden sich im `.homewebcom`-Verzeichnis.
- Sprache, Zugangsdaten und weitere Einstellungen können dort angepasst werden.

## Nutzung

Nach erfolgreicher Installation und Konfiguration kann der Chat im Browser über deinen Tomcat-Server aufgerufen werden  
(z.B. unter `http://localhost:8080/HWebChat-Community-Edition`).

## Support

Für Fragen oder Beiträge zur Community:  
> GitHub Issues oder Pull Requests gerne einreichen!

---

**Viel Spaß mit HWebChat!**
