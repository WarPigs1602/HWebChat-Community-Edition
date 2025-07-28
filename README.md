# HWebChat Community Edition

HWebChat ist eine Community-basierte Chat-Lösung für das Web.  
**Hinweis:** Die Chat-Konfiguration ist aktuell auf Deutsch, kann aber angepasst werden.

## Voraussetzungen

- **.homewebcom**-Verzeichnis muss im Benutzerverzeichnis liegen.
- **Netbeans IDE** wird empfohlen zum Kompilieren (im `src`-Verzeichnis).
- **MySQL** oder **MariaDB** für die Datenbank.

## Installation

1. Klone dieses Repository:
   ```bash
   git clone https://github.com/WarPigs1602/HWebChat-Community-Edition.git
   ```
2. Lege das Verzeichnis `.homewebcom` in deinem Benutzerverzeichnis an (falls nicht vorhanden).
3. Öffne das Projekt mit Netbeans und kompiliere es im `src`-Verzeichnis.
4. Importiere die `database.sql` Datei in deine MySQL oder MariaDB Datenbank:
   ```bash
   mysql -u <user> -p <datenbankname> < path/to/database.sql
   ```
5. Passe ggf. die Konfiguration an deine Bedürfnisse an (Standard: Deutsch).

## Konfiguration

- Die Konfigurationsdateien befinden sich im `.homewebcom`-Verzeichnis.
- Sprache, Zugangsdaten und weitere Einstellungen können dort angepasst werden.

## Nutzung

Nach erfolgreicher Installation und Konfiguration kann der Chat im Browser aufgerufen werden.

## Support

Für Fragen oder Beiträge zur Community:  
> GitHub Issues oder Pull Requests gerne einreichen!

---

**Viel Spaß mit HWebChat!**
