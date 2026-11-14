# HWebChat Community Edition

Webbasierter Community-Chat (Jakarta EE / WebSocket) für Apache Tomcat.

- **Demo:** https://www.hwebchat.de  
- **Issues:** https://github.com/WarPigs1602/HWebChat-Community-Edition/issues  

---

## Features

- Öffentliche und private Chaträume (Echtzeit per WebSocket)
- Öffentliche Newsseite mit Verwaltung in der Adminkonsole
- Schreibanzeige mit Nickfarben für Räume und Privatchats (bis zu fünf Namen)
- Nick-Vervollständigung mit Tab in der Eingabezeile; mehrfaches Tab wechselt zwischen Treffern
- Gast-Login und registrierte Benutzer
- Offline-Nachrichten (Flüstern, wenn der Empfänger offline ist)
- Emojis, Datei-Uploads
- Moderation (Kick, Ban, Gag, …)
- Adminkonsole, Communities / Napping-Räume
- Eingebauter Skin `native` als JSP-Templates (`web/WEB-INF/jsp`) mit vollständiger i18n (Deutsch/Englisch) über Resource Bundles und nativem EL/JSTL statt `%Platzhaltern%`
- Speicherung in MySQL / MariaDB, PostgreSQL oder SQLite

Die Statistik verwendet wie der Chat die Einstellung `time_zone` aus der Konfiguration.
Bereits gespeicherte Statistikintervalle werden bei einer Änderung der Zeitzone nicht umgerechnet.
Bei der Zeitumstellung im Herbst werden die beiden gleich benannten Stunden zusammengefasst.

Die Newsseite ist über `news.jsp` erreichbar. In der Adminkonsole können
Administratoren Beiträge erstellen, bearbeiten und löschen; Veröffentlichungsdatum
und ursprünglicher Autor bleiben beim Bearbeiten erhalten.
Die Startseite und die Community-Seite nach dem Login zeigen Kennzahlen, hilfreiche
Links und, falls vorhanden, die neueste News. In eigenen Templates kann
`%homepage_extra%` die Position dieser Bereiche festlegen; fehlt der Platzhalter,
werden sie am Ende eingefügt.

---

## Voraussetzungen

| Komponente | Version / Hinweis |
|------------|-------------------|
| **JDK** | **21** (source/target in der `pom.xml`) |
| **Maven** | 3.8+ |
| **Apache Tomcat** | 10.1+ (Jakarta EE 9+, Servlet 6 / WebSocket) |
| **Datenbank** | MySQL/MariaDB 10.6+, PostgreSQL oder SQLite |
| **Konfig-Home** | `~/.homewebcom` (Home des Users, unter dem Tomcat läuft) |

---

## Schnellstart

### 1. Klonen

```bash
git clone https://github.com/WarPigs1602/HWebChat-Community-Edition.git
cd HWebChat-Community-Edition
```

### 1a. Oder das fertige WAR verwenden

Alternativ das aktuelle Release direkt herunterladen:

```text
https://github.com/WarPigs1602/HWebChat-Community-Edition/releases/download/261115/HWebChat_Community_Edition.war
```

Danach weiter mit Schritt 3 (Auf Tomcat deployen).

### 2. Mit Maven bauen

```bash
cd web/WEB-INF
mvn clean package
```

Artefakt:

 ```text
 target/HWebChat_Community_Edition.war
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
 cp target/HWebChat_Community_Edition.war "$CATALINA_HOME/webapps/"
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
http://localhost:8080/HWebChat_Community_Edition/setup.jsp
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
http://localhost:8080/HWebChat_Community_Edition/start.jsp
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
| `~/.homewebcom/templates/native/`, `native_en/` | Reine Skripte und Stylesheets (die Seiten-Templates sind JSPs im WAR) |

Der Setup-Assistent unterstützt alle drei Datenbank-Backends. SQLite ist die Voreinstellung. Für eine manuelle Konfiguration setzt `sql.type` den Wert `sqlite`, `mysql` oder `postgresql`. PostgreSQL verwendet `sql.host` (`host:port`), `sql.db`, `sql.user` und `sql.pw`; `pgcrypto` muss in der Datenbank installiert sein (der Setup-Assistent versucht, die Erweiterung anzulegen). SQLite verwendet `sql.path` und standardmäßig `~/.homewebcom/data/hwebchat.sqlite`; `sql.user` und `sql.pw` werden ignoriert.

Die Repo-Kopie `web/default-homewebcom/` ist eine Vorlage; beim Erststart erstellt die App automatisch `~/.homewebcom` und kopiert alle Dateien dorthin.

Zufallsfarben für neue Benutzer und Gäste erreichen mindestens 4,5:1 Kontrast zum dunklen Standarddesign. Die Zeichenbeschränkungen `bright_0` bis `bright_5` gelten weiterhin; wenn sie keine ausreichend helle Farbe zulassen, muss die Konfiguration angepasst werden. Individuelle Raumfarben können einen anderen Kontrast ergeben.

Im Chat-Tab eingebettete Seiten blenden ihre eigene Navigationsleiste aus; außerhalb des Chat-Tabs bleibt sie sichtbar. Dies gilt auch für bereits kopierte Seitendesigns mit einer `nav.navbar` direkt im `body`.

Die Navigationsleiste bietet ein dunkles Alternativdesign zum hellen Standard („Design“-Menü, gespeichert im Cookie `theme`). Der Teil eines Stylesheets hinter dem Kommentar `/* theme:dark` gehört nur zum dunklen Design und entfällt im hellen. Im Chat wechselt das Sprachmenü die Sprache in Echtzeit über die offene WebSocket-Verbindung (ohne Neuladen); Elemente mit `data-i18n="<Command-Schlüssel>"`, der Sende-Button und die Benutzerliste werden übersetzt. Filter und eingeklappte Gruppen bleiben erhalten.

Öffentliche und Community-Seiten wählen die Navigation anhand der aktiven Community-Sitzung: registrierter Benutzer, Gast oder nicht angemeldet. Abgelaufene Sitzungen erhalten die öffentliche Navigation.

---

## Entwicklung

- **Package:** `net.midiandmore.chat`
- **Einstiegsseite:** `/start.jsp`; weitere Seiten sind direkt erreichbar, z. B. `/login.jsp`, `/login_chat.jsp`, `/account.jsp`, `/account_com.jsp`, `/board.jsp`, `/profile.jsp`, `/register.jsp`, `/news.jsp` und `/console.jsp`.
- **Inhalte:** Dokumente, Skripte und Stylesheets sind die JSPs unter `/content/`, z. B. `/content/rules.jsp`, `/content/script_chat.jsp` und `/content/style_page.jsp`; jede JSP enthält ihr Template. Dokumente nur für angemeldete Community-Nutzer werden mit `community=true` aufgerufen, z. B. `/content/rules_com.jsp?community=true`; ohne Community-Sitzung erscheint die Timeout-Seite. Skripte und Stylesheets ohne eigenes Template (jQuery, Bootstrap, …) liefert die jeweilige JSP aus dem Skin aus. Die frühere Adresse `/content.jsp?service=...&doc=...` und die `community_`-JSPs entfallen; eigene Links müssen angepasst werden.
- Dienste ohne jemals vorhandenen Inhalt (`chatstream`, `fader`, `refreshstream`, `refresh_error`) haben keinen Endpunkt und antworten mit der 404-Seite. Eigene Dokument-, Skript- oder Stylesheet-Vorlagen benötigen einen entsprechenden JSP-Endpunkt unter `/content/`.
- **Setup-Assistent:** `/setup.jsp` (Logik in `SetupService`)
- **WebSocket:** Klasse `Chat` (`@ServerEndpoint`)
- **Upload:** `/upload.jsp` (Multipart-POST)
- Die Seiten rufen gemeinsame Java-Methoden über `Web` auf. Eine direkt aufgerufene Seite beginnt mit `Web.begin(...)`: Das prüft das Setup, übernimmt die Sprache und stellt die Template-Werte als Request-Attribute bereit. Webpfade sind fest vorgegeben und berücksichtigen den Kontextpfad der Anwendung. Vorhandene `paths.json`-Dateien und `path_*`-Einträge in `config.json` werden nicht mehr verwendet oder gelöscht; die Pfadverwaltung entfällt. Alte eigene Links müssen gegebenenfalls angepasst werden.
- Einige alte Frame-Vorlagen verwenden weiterhin nicht unterstützte `?page=`-Links für die frühere HTTP-Chateingabe; diese sind durch die festen Pfadvorgaben nicht funktionsfähig.
- Die bisherigen URLs `/Start`, `/Setup` und `/UploadFile` werden nicht mehr bereitgestellt.
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
Standard sind deutsche UI/Templates. Englisch steht über dieselben JSP-Templates bereit, ergänzt um passende Configs wie `help_en.json` und `profile_en.json`. Die Sprache wird automatisch erkannt (`lang`-Cookie, `lang`-Parameter, `Accept-Language`) und kann im Frontend umgestellt werden.

### JSP-Templates und i18n

- Seiten sind JSPs. Die JSPs im Web-Wurzelverzeichnis (`memory.jsp`, `captcha.jsp`, `webchat_stats.jsp`, `link.jsp`, `logout.jsp`, `top.jsp`, `emot.jsp`, `help.jsp`, `read_message.jsp`, `stats.jsp`, `image.jsp`, `news.jsp`, `password.jsp`, `register.jsp`, `login.jsp`, `login_chat.jsp`, `profile.jsp`, `account.jsp`, `account_com.jsp`, `board.jsp`, `console.jsp`) und in `content/` verarbeiten ihre Anfragen direkt. Einige binden weiterhin gemeinsames Markup aus `web/WEB-INF/jsp` ein; andere Seiten verwenden dort ebenfalls noch Templates: `pages`, `console`, `mail`, `system` und `includes`.
- Eine Seite packt ihren Inhalt mit `<t:layout design="start">…</t:layout>` ein. [layout.tag](web/WEB-INF/tags/layout.tag) ist das einzige Layout: Es setzt `includes/header.jsp` (`<head>`), `includes/navbar.jsp` (Seiten-, Konsolen- und Logo-Navigation) und `includes/footer.jsp` (rechtliche Links und Skripte) um den Inhalt zusammen. Der Design-Name wählt nur die Parameter (Titel, Stylesheets, Navigationsvariante, Wrapper, Footer-Teile) aus der Liste am Anfang des Tags; `includes/navbar-chat.jsp` ist die Navigation im Chatfenster.
- Werte werden per EL gelesen, es gibt kein `%Platzhalter%`-Ersetzen mehr: `${nick}`, `${sid}`, `${skin}`, `${paths.login}` (fest vorgegebener Pfad), `${com.design_nav_start}` (commands.json), `${help.away}`, `${profile.hobby}`, `${SERVER_SOFTWARE}`, … Seitenspezifische Werte setzt Java mit `view.set("name", wert)` auf dem `View` aus `getTemplate(...)`; ein `View` kann Wert eines anderen `View` sein und sieht dann dessen Werte.
- Texte stehen in den Resource Bundles `src/resources/i18n/messages.properties` (Deutsch, Standard) und `messages_en.properties` und werden mit `<fmt:message key="…"/>` ausgegeben. Meldungen mit Werten nutzen `{0}`, `{1}`, … und `<fmt:param value="${…}"/>` (in solchen Meldungen `'` als `''` und `{` als `'{'` schreiben). Eine weitere Sprache entsteht mit `messages_<lang>.properties` und denselben Schlüsseln.
- Zeichenketten in JSON-Konfigurationsdateien und den festen Pfadvorgaben können weiterhin `%name%`, `%PATH_[name]%` und `%com[name]%` verwenden; diese werden beim Lesen aufgelöst. Parameterlose JSP-Pfade enden nicht mit `?`; bestehende Konfigurationen, die Parameter mit `&` anhängen, bleiben kompatibel.
- Templates unter `~/.homewebcom/templates` werden nicht mehr verwendet, außer für die reinen Skripte und Stylesheets (jQuery, Bootstrap, …) in `native` / `native_en`. Das Layout ändert man in den JSPs im WAR (bzw. mit einem angepassten WAR).
- Öffentliche Seiten im Web-Root: `index.jsp` (Welcome-Datei, leitet auf `start.jsp` weiter) sowie `404.jsp` und `500.jsp` (Fehlerseiten); sie nutzen die oben beschriebene Spracherkennung.

**Erststart-Setup?**  
Beim ersten Aufruf verweist die App auf `/setup.jsp`. Der Assistent erstellt automatisch `~/.homewebcom`, alle Datenbanktabellen, den ersten Admin-Benutzer und schreibt eine vollständige `config.json`.

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
