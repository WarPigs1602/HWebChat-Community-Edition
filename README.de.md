# HWebChat Community Edition

Webbasierter Community-Chat (Jakarta EE / WebSocket) für Apache Tomcat.

- **Demo:** https://www.hwebchat.de  
- **Issues:** https://github.com/WarPigs1602/HWebChat-Community-Edition/issues  

---

## Features

- Öffentliche und private Chaträume (Echtzeit per WebSocket)
- Gast-Login und registrierte Benutzer
- Offline-Nachrichten (Flüstern, wenn der Empfänger offline ist)
- Emojis, Datei-Uploads
- Moderation (Kick, Ban, Gag, …)
- Adminkonsole, Communities / Napping-Räume
- Skins und Templates unter `~/.homewebcom`
- Speicherung in MySQL / MariaDB

---

## Voraussetzungen

| Komponente | Version / Hinweis |
|------------|-------------------|
| **JDK** | **21** (source/target in der `pom.xml`) |
| **Maven** | 3.8+ |
| **Apache Tomcat** | 10.1+ (Jakarta EE 9+, Servlet 6 / WebSocket) |
| **MySQL oder MariaDB** | empfohlen 10.6+ |
| **Konfig-Home** | `~/.homewebcom` (Home des Users, unter dem Tomcat läuft) |

---

## Schnellstart

### 1. Klonen

```bash
git clone https://github.com/WarPigs1602/HWebChat-Community-Edition.git
cd HWebChat-Community-Edition
```

### 2. Laufzeit-Konfiguration (`~/.homewebcom`)

Die App lädt die Konfiguration aus dem **Home-Verzeichnis des Tomcat-Prozesses**:

```bash
cp -a .homewebcom ~/.homewebcom
# oder in ein bestehendes ~/.homewebcom mergen
```

Mindestens anpassen:

- `~/.homewebcom/config/config.json` — DB, Admin-Passwort, Chat-Einstellungen  
- `~/.homewebcom/config/hosts.json` — Host → Skin  

Beispiel DB-Einträge in `config.json`:

```json
{"name":"sql.host","value":"localhost:3306","description":"Der MySQL Host"},
{"name":"sql.user","value":"hwebchat","description":"Der MySQL Benutzer"},
{"name":"sql.pw","value":"geheim","description":"Das MySQL Passwort"},
{"name":"sql.db","value":"hwebchat","description":"Die MySQL Datenbank"},
{"name":"sql.prefix","value":"hwc_","description":"Der SQL Prefix"}
```

### 3. Datenbank

```bash
mysql -u root -p -e "CREATE DATABASE hwebchat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p hwebchat < database.sql
```

DB-User anlegen:

```bash
mysql -u root -p -e "CREATE USER 'hwebchat'@'localhost' IDENTIFIED BY 'secure_password'; GRANT ALL ON hwebchat.* TO 'hwebchat'@'localhost'; FLUSH PRIVILEGES;"
```

**Collation-Hinweis:** Einheitliche Collation für alle Tabellen (z. B. `utf8mb4_unicode_ci`). Gemischte Collations können JOINs auf Nicks/Nachrichten brechen.

### 4. Mit Maven bauen

```bash
cd src/web/WEB-INF
mvn clean package
```

Artefakt:

```text
src/target/HWebChat_Community_Edition.war
```

Weitere Varianten:

```bash
mvn clean package -DskipTests   # ohne Tests
mvn clean compile               # nur kompilieren
mvn -o package                  # offline (lokales Repo)
```

**NetBeans:** Projekt kann weiterhin geöffnet werden; der Build läuft über diese Maven-`pom.xml`.

### 5. Auf Tomcat deployen

```bash
# Tomcat ggf. stoppen, dann:
cp src/target/HWebChat_Community_Edition.war "$CATALINA_HOME/webapps/"
# Tomcat starten — entpackt das WAR automatisch
```

Oder in deinen bestehenden `webapps`-Pfad (z. B. `/home/du/tomcat/webapps/`).

Nach dem Deploy typischer Context-Pfad:

```text
http://localhost:8080/HWebChat_Community_Edition/
```

Startseite leitet weiter auf `/HWebChat_Community_Edition/Start`.

### 6. Nach Code-Änderungen neu deployen

```bash
cd src/web/WEB-INF
mvn clean package
rm -rf "$CATALINA_HOME/webapps/HWebChat_Community_Edition"
cp ../../target/HWebChat_Community_Edition.war "$CATALINA_HOME/webapps/"
# Tomcat neu starten oder Auto-Redeploy abwarten
```

Templates/Config unter `~/.homewebcom` liegen **nicht** im WAR — dort live editieren; Tomcat-Neustart nur nötig, wenn Klassen neu gebaut wurden.

---

## Konfiguration

| Pfad | Zweck |
|------|--------|
| `~/.homewebcom/config/config.json` | Haupteinstellungen (SQL, Timeouts, Status-Level, …) |
| `~/.homewebcom/config/hosts.json` | Virtual Host → Skin |
| `~/.homewebcom/config/commands.json` | Texte der Chat-Befehle |
| `~/.homewebcom/config/paths.json` | URL-Pfadnamen |
| `~/.homewebcom/templates/native/` | Standard-Skin (HTML, JS, CSS) |

Die Repo-Kopie `.homewebcom/` ist eine Vorlage; **Produktion nutzt `~/.homewebcom`**.

---

## Entwicklung

- **Package:** `net.midiandmore.chat`
- **Einstiegs-Servlet:** `ChatPages` → `/Start`
- **WebSocket:** Klasse `Chat` (`@ServerEndpoint`)
- **Upload:** `/UploadFile`
- **Jakarta EE:** APIs unter `jakarta.*` (nicht `javax.*` für Servlet/WebSocket)

Java-Quellen: `src/src/java/net/midiandmore/chat/`  
Web-Ressourcen: `src/web/`

---

## Sicherheit

- Starke Passwörter für MySQL und Adminkonsole (`admin_password` in der Config)
- Tomcat Manager nicht ungeschützt öffentlich freigeben
- JDK und Tomcat aktuell halten
- Rechte auf `~/.homewebcom/config` einschränken (enthält DB-Passwort)
- HTTPS bevorzugen (Tomcat-SSL oder Reverse-Proxy)

---

## FAQ

**Sprache?**  
UI/Templates sind größtenteils deutsch; Templates und `config.json` anpassen.

**HTTPS?**  
TLS an Tomcat oder Reverse-Proxy (nginx/Caddy). Cookie-Flags `secure` in `web.xml` an das Setup anpassen.

**Falsche DB / keine Nachrichten?**  
`sql.db` / User in **`~/.homewebcom/config/config.json`** prüfen (nicht nur die Repo-Kopie) und mit der importierten Datenbank abgleichen.

**Build schlägt wegen Java-Version fehl?**  
JDK 21 nutzen: `java -version` und `mvn -v` sollten 21 melden.

---

## Lizenz

Siehe [LICENSE](LICENSE).

---

**Viel Spaß mit HWebChat!**