

> **Achtung:** Dieses Projekt wird als **Legacy** betrachtet und erhält nur noch eingeschränkten Support. Für neue Projekte wird empfohlen, nach aktuellen Alternativen zu suchen.

# HWebChat Community Edition

HWebChat ist eine Community-basierte Chat-Lösung für das Web.
**Hinweis:** Die Chat-Konfiguration ist aktuell auf Deutsch, kann aber angepasst werden.

## Features

- Öffentliche und private Chat-Räume
- Benutzerregistrierung und Authentifizierung
- Unterstützung für Emojis und Dateiuploads
- Moderationsfunktionen (z.B. Kick/Ban)
- Anpassbare Konfiguration (Sprache, Rechte, etc.)
- Websocket-basierte Kommunikation für Echtzeit-Chat
- Datenbankgestützte Speicherung (MySQL/MariaDB)

## Beispiel-Konfiguration

Im Unterordner `config` von `.homewebcom` befindet sich die Datei `config.json` mit einer Struktur wie folgt:

```json
[
   {"name": "admin_password", "value": "passwort", "description": "Das Passwort der Adminkonsole"},
   {"name": "sql.host", "value": "localhost:3306", "description": "Der MySQL Host"},
   {"name": "sql.user", "value": "user", "description": "Der MySQL Benutzer"},
   {"name": "sql.pw", "value": "password", "description": "Das MySQL Passwort"},
   {"name": "sql.db", "value": "hwebchat", "description": "Die MySQL Datenbank"},
   {"name": "language", "value": "de", "description": "Die Spracheinstellung (z.B. 'de' oder 'en')"},
   {"name": "max_nick_length", "value": "20", "description": "Maximale Nicklänge"}
   // ... weitere Einstellungen ...
]
```

Jeder Eintrag ist ein Objekt mit `name`, `value` und `description`. Du kannst bestehende Werte anpassen oder neue hinzufügen. Siehe die mitgelieferte `config.json` für alle verfügbaren Optionen.

## Sicherheitshinweise

- Setze sichere Passwörter für Datenbank und Admin-Accounts.
- Stelle sicher, dass der Tomcat-Server nicht öffentlich zugänglich ist, falls nicht gewünscht.
- Halte deine Java- und Tomcat-Version aktuell.
- Prüfe regelmäßig die Zugriffsrechte im Unterordner `.homewebcom/config`.

## FAQ

**Frage:** Kann ich die Sprache auf Englisch umstellen?
Antwort: Ja, passe die Einstellung `language` in der Konfiguration an.

**Frage:** Wie kann ich weitere Emojis hinzufügen?
Antwort: Lege neue Emoji-Fonts im Verzeichnis `web/fonts/` ab und passe ggf. die Konfiguration an.

**Frage:** Wie kann ich den Chat auf HTTPS umstellen?
Antwort: Konfiguriere Tomcat entsprechend für SSL (siehe Tomcat-Dokumentation).

## Kontakt & Community

- Codeberg Issues: https://codeberg.org/WarPigs1602/HWebChat-Community-Edition/issues
- Pull Requests willkommen!
- Für größere Beiträge oder Support: Siehe Issues oder Projektseite auf Codeberg.

## Screenshots & Links

![Screenshot Chat-Oberfläche](https://codeberg.org/WarPigs1602/HWebChat-Community-Edition/raw/branch/main/screenshots/chat.png)

Weitere Infos und aktuelle Versionen: https://codeberg.org/WarPigs1602/HWebChat-Community-Edition


## Voraussetzungen

- **Apache Tomcat** ist für die Ausführung erforderlich.
- Das Verzeichnis **.homewebcom** muss im Benutzerverzeichnis liegen.
- **Netbeans IDE** wird zum Kompilieren empfohlen (im `src`-Verzeichnis).
- **MySQL** oder **MariaDB** für die Datenbank.

## Installation

1. Klone dieses Repository:
   ```bash
   git clone https://codeberg.org/WarPigs1602/HWebChat-Community-Edition.git
   ```
2. Lege das Verzeichnis `.homewebcom` in deinem Benutzerverzeichnis an (falls nicht vorhanden).
3. Öffne das Projekt mit Netbeans und kompiliere es im `src`-Verzeichnis zu einer **.war-Datei**.
4. Importiere die Datei `database.sql` in deine MySQL- oder MariaDB-Datenbank:
   ```bash
   mysql -u <user> -p <datenbankname> < path/to/database.sql
   ```
5. Passe die Konfiguration im Verzeichnis `.homewebcom` nach Bedarf an (Standard: Deutsch).
6. Deploye die erstellte **.war-Datei** in deinen **Tomcat**-Server (`webapps`-Verzeichnis).

## Konfiguration

- Die Konfigurationsdateien befinden sich im Unterordner `config` im Verzeichnis `.homewebcom` (also z.B. `~/.homewebcom/config`).
- Sprache, Zugangsdaten und weitere Einstellungen können dort angepasst werden.

## Nutzung

Nach erfolgreicher Installation und Konfiguration kann der Chat im Browser über deinen Tomcat-Server aufgerufen werden
(z.B. unter `http://localhost:8080/HWebChat-Community-Edition`).

## Support

Für Fragen oder Beiträge zur Community:
> Codeberg Issues oder Pull Requests gerne einreichen!

---

**Viel Spaß mit HWebChat!**
