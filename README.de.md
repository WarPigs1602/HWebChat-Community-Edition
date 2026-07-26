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
- Skins und Templates unter `~/.homewebcom` (`native` Deutsch, `native_en` Englisch)
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

### 2. Mit Maven bauen

```bash
cd web/WEB-INF
mvn clean package
```

Artefakt:

```text
web/target/HWebChat_Community_Edition.war
```

Weitere Varianten:

```bash
mvn clean package -DskipTests   # ohne Tests
mvn clean compile               # nur kompilieren
mvn -o package                  # offline (lokales Repo)
```

**NetBeans:** Projekt kann weiterhin geöffnet werden; der Build läuft über diese Maven-`pom.xml`.

### 3. Auf Tomcat deployen

```bash
# Tomcat ggf. stoppen, dann:
cp web/target/HWebChat_Community_Edition.war "$CATALINA_HOME/webapps/"
# Tomcat starten — entpackt das WAR automatisch
```

Oder in deinen bestehenden `webapps`-Pfad (z. B. `/home/du/tomcat/webapps/`).

Nach dem Deploy typischer Context-Pfad:

```text
http://localhost:8080/HWebChat_Community_Edition/
```

### 4. Erststart-Setup

Beim ersten Aufruf erkennt die App, dass `~/.homewebcom` fehlt, und leitet automatisch auf den integrierten Setup-Assistenten weiter:

```text
http://localhost:8080/HWebChat_Community_Edition/Setup
```

Der Setup-Assistent:

1. Erstellt `~/.homewebcom` und kopiert alle Templates/Configs aus dem WAR
2. Fragt Datenbank, SMTP-Mail, Adminkonsole und den ersten Benutzer ab
3. Legt alle benötigten Datenbanktabellen automatisch an
4. Legt den ersten Chat-Benutzer an (mit Admin- und Forum-Moderator-Rechten)
5. Legt den konfigurierten Standardraum an
6. Schreibt eine vollständige `config.json` mit allen Einstellungen

Nach dem Setup ist die Startseite erreichbar unter:

```text
http://localhost:8080/HWebChat_Community_Edition/Start
```

### 5. Nach Code-Änderungen neu deployen

```bash
cd web/WEB-INF
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
| `~/.homewebcom/templates/native/` | Deutsch-Skin (HTML, JS, CSS) |
| `~/.homewebcom/templates/native_en/` | Englisch-Skin (HTML, JS, CSS) |

Die Repo-Kopie `web/default-homewebcom/` ist eine Vorlage; beim Erststart erstellt die App automatisch `~/.homewebcom` und kopiert alle Dateien dorthin.

---

## Entwicklung

- **Package:** `net.midiandmore.chat`
- **Einstiegs-Servlet:** `ChatPages` → `/Start`
- **Setup-Assistent:** `SetupServlet` → `/Setup` (Erststart-Konfiguration)
- **WebSocket:** Klasse `Chat` (`@ServerEndpoint`)
- **Upload:** `/UploadFile`
- **Jakarta EE:** APIs unter `jakarta.*` (nicht `javax.*` für Servlet/WebSocket)

Java-Quellen: `src/java/net/midiandmore/chat/`  
Web-Ressourcen: `web/`

---

## Sicherheit

- Starke Passwörter für MySQL und Adminkonsole (werden beim Erststart-Setup vergeben)
- Tomcat Manager nicht ungeschützt öffentlich freigeben
- JDK und Tomcat aktuell halten
- Rechte auf `~/.homewebcom/config` einschränken (enthält DB-Passwort)
- HTTPS bevorzugen (Tomcat-SSL oder Reverse-Proxy)

---

## FAQ

**Sprache?**  
Standard sind deutsche UI/Templates. Ein englischer Skin liegt unter `.homewebcom/templates/native_en/` bereit, ergänzt um passende Configs wie `help_en.json` und `profile_en.json`. Die Einstellung des Skins erfolgt in der `hosts.json`.

**Erststart-Setup?**  
Beim ersten Aufruf leitet die App auf `/Setup` weiter. Der Assistent erstellt automatisch `~/.homewebcom`, alle Datenbanktabellen, den ersten Admin-Benutzer und schreibt eine vollständige `config.json`.

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