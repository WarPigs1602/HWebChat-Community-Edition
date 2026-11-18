# HWebChat

Webbasierter Community-Chat für Apache Tomcat, entwickelt mit Jakarta EE, JSP und WebSocket.

[English](README.md) · [Demo](https://www.hwebchat.de) · [Releases](https://github.com/WarPigs1602/HWebChat/releases) · [Issues](https://github.com/WarPigs1602/HWebChat/issues)

## Funktionen

### Chat und Community

- Raum- und Privatchats in Echtzeit per WebSocket; Gast- und Benutzer-Login, Räume und Napping-Räume.
- Schreibanzeige mit Nickfarben, Nick-Vervollständigung per Tab, Emojis und Offline-Nachrichten.
- Moderation mit Kick, Ban und Gag; konfigurierbare Räume, Befehle und Berechtigungen.
- Benutzerprofile mit Gästebuch und Profilbild-Uploads, Konto- und Passwortverwaltung, Freunde und Nachrichten.
- Forum mit Kategorien, Themen, Antworten und Moderation. Besucher können lesen; zum Schreiben sind eine registrierte Community-Sitzung und die entsprechenden Berechtigungen nötig.
- News, Statistik und eine punktbasierte Topliste. Administratoren können News erstellen, bearbeiten und löschen; ursprünglicher Autor und Veröffentlichungsdatum bleiben beim Bearbeiten erhalten.
- Startseite und angemeldete Community-Seite zeigen Chat-Kennzahlen, hilfreiche Links und, falls vorhanden, die neuesten News.
- Deutsche und englische Oberfläche: Das Sprach-Cookie hat Vorrang, danach folgen der Parameter `lang` und die Browsereinstellungen; Englisch ist der Standard. Die automatische Erkennung setzt kein Sprach-Cookie. Im Chat ist der Sprachwechsel ohne Neuladen möglich; Filter und eingeklappte Benutzergruppen bleiben erhalten.
- Helles und dunkles Design; die Auswahl wird im Cookie `theme` gespeichert.

### Administration und Gestaltung

- Adminkonsole für Benutzer, Räume, Module, News, Konfiguration, Befehlsrechte, Hilfe, Emoticons, Hosts, Mail und MIME-Typen.
- Bedienelemente und Meldungen der Konsole unterstützen Deutsch und Englisch; das Sprachmenü wechselt die Sprache und führt zur Konsolenstartseite. Konfigurationswerte und eigene Inhalte bleiben unverändert.
- WYSIWYG-Theme-Creator mit Vorschau, Farben, Typografie, Logo und eigenem CSS für Skins.
- WYSIWYG-Seitenersteller mit Vorschau und bereinigtem HTML; öffentliche Seiten sind unter `/page.jsp?slug=<name>` erreichbar.
- Gemeinsame, konfigurierbare Navigation für öffentliche und Community-Seiten mit eigenen Links, Sichtbarkeit, Beschriftung, Zielen und Reihenfolge.
- Speicherung in SQLite, MySQL/MariaDB und PostgreSQL; browserbasierter Erststart-Assistent.

## Module

Im Bereich **Module** der Adminkonsole lassen sich folgende Einstellungen verwalten. Die Standardwerte gelten für eine neue Installation; `1` aktiviert und `0` deaktiviert eine Option. Gespeichert wird in `~/.homewebcom/config/modules.json` des Tomcat-Prozessbenutzers.

| Einstellung | Standard | Zweck |
|---|---|---|
| `chat` | `1` | Chat-Login und Chaträume in Echtzeit. |
| `board` | `1` | Forum mit öffentlichem Lesen und Beteiligung registrierter Benutzer. |
| `news` | `1` | Öffentliche Newsseite und Newsverwaltung. |
| `stats` | `1` | Statistikseite mit Nachrichten-, Benutzer-, Raum- und Aktivitätsdiagrammen. |
| `toplist` | `1` | Rangliste registrierter Benutzer nach Punkten. |
| `direct_chat_login` | `0` | Bei aktiviertem Chat direkten Chat-Login statt Community-Login verwenden. |

Deaktivierte Module verschwinden aus der Navigation; die zugehörigen Chat-Login-, Forum-, News-, Statistik- und Toplisten-Seiten antworten mit 404. Ist `news` deaktiviert, ist auch dessen Verwaltung in der Konsole nicht verfügbar. `direct_chat_login` wirkt nur bei aktiviertem `chat`; beim direkten Login ist die Community-Login-Seite nicht verfügbar.

Beim Verlassen eines Chats, auch über die Zurück-Taste des Browsers, wird die Verbindung geschlossen. Beim Trennen einer direkten Chat-Sitzung wird die Sitzung beendet; für einen erneuten Zugang ist eine Anmeldung erforderlich. Beim Verlassen eines über die Community betretenen Chats bleibt die Community-Sitzung bestehen.

## Voraussetzungen

| Komponente | Voraussetzung |
|---|---|
| Java | JDK 21 zum Bauen; Java 21 zum Betrieb |
| Maven | 3.8+ zum Bauen |
| Servlet-Container | Apache Tomcat 11.x (Servlet 6.1, JSP 4.0, WebSocket 2.2); Tomcat 10.1 stellt diese API-Versionen nicht bereit |
| Datenbank | SQLite, MySQL/MariaDB oder PostgreSQL |
| Mail | Erreichbarer SMTP-Server: Das Setup sendet eine Testmail an den ersten Benutzer |
| Speicher | Schreibbares `~/.homewebcom` im Home-Verzeichnis des Tomcat-Prozessbenutzers |

PostgreSQL benötigt `pgcrypto`; das Setup versucht die Erweiterung anzulegen. Der Datenbankbenutzer benötigt dafür die erforderlichen Rechte, oder ein Administrator installiert sie vorab. Für MySQL/MariaDB und PostgreSQL müssen Datenbank und Zugangsdaten vor dem Setup angelegt werden.

## Schnellstart

1. **WAR bauen** im Repository-Wurzelverzeichnis oder `HWebChat.war` aus den [Releases](https://github.com/WarPigs1602/HWebChat/releases) herunterladen:

   ```bash
   git clone https://github.com/WarPigs1602/HWebChat.git
   cd HWebChat
   mvn -f web/WEB-INF/pom.xml clean package
   ```

   Das Ergebnis liegt im Repository unter `target/HWebChat.war`. Maven kann auch aus `web/WEB-INF` gestartet werden; von dort lautet der Pfad `../../target/HWebChat.war`.

2. **Deployen:** `target/HWebChat.war` nach dem für den Server vorgesehenen Verfahren in das Tomcat-11-Verzeichnis `webapps` deployen. Bei Standard-WAR-Namen und Standardkonfiguration lautet der Kontextpfad `/HWebChat`:

   ```text
   http://localhost:8080/HWebChat/
   ```

   Ein umbenanntes WAR oder eine eigene Kontextkonfiguration ändert diesen Pfad. Vor Updates Daten sichern und das Tomcat-Deploy-Verfahren befolgen; beim Ersetzen der Anwendung das Datenverzeichnis nicht löschen.

3. **Setup ausführen:** `http://localhost:8080/HWebChat/setup.jsp` aufrufen, bevor die Installation öffentlich erreichbar ist. SQLite (Voreinstellung), MySQL oder PostgreSQL auswählen und SMTP, ersten Chat-Benutzer, Konsolenpfad, Raum und rechtliche Angaben eintragen. Der Assistent kopiert Erststart-Konfiguration und Daten aus `web/default-homewebcom/`, erstellt Datenbanktabellen und den ersten Administrator, testet SMTP und schreibt die Konfiguration. Mit diesem Konto anmelden, um die Konsole zu nutzen.

4. `http://localhost:8080/HWebChat/start.jsp` öffnen und anmelden. Die Konsole liegt standardmäßig unter `/HWebChat/console.jsp` beziehungsweise unter dem im Setup gewählten `console.path`.

Das Setup legt die vordefinierten Räume Development, Exil, Lounge und Staff-Lounge an, aber keinen beliebig als Standard eingetragenen Raum. Falls ein anderer Standardraum gewählt wird, diesen in der Konsole anlegen.

Das Setup gilt erst als abgeschlossen, wenn sowohl `~/.homewebcom/config/config.json` als auch `~/.homewebcom/config/.setup_complete` vorhanden sind. Ein fehlgeschlagenes Setup kann bereits Dateien oder Datenbankeinträge angelegt haben; beim erneuten Versuch werden vorhandene Dateien mit Vorlagen überschrieben. Vorher prüfen und sichern.

## Administration

### Seiten und Navigation

In der Konsole **WYSIWYG-Seitenersteller** wählen, URL-Namen, Titel und Inhalt eingeben, Vorschau prüfen und speichern. Der URL-Name besteht aus Kleinbuchstaben, Ziffern und Bindestrichen und ist nach dem Erstellen nicht änderbar; gespeicherte Seiten sind unter `/page.jsp?slug=<name>` öffentlich erreichbar. HTML wird bereinigt, nicht unterstützte Markups und Attribute werden entfernt. Bei Bedarf separat einen Navigationslink hinzufügen.

Unter **Navigation** bestehende Punkte konfigurieren oder eigene Links ergänzen, etwa auf eigene Seiten. Filter zeigen das Hauptmenü sowie die Login- und Sprach-Untermenüs; Pfeile sortieren innerhalb des jeweiligen Menüs. Hinzufügen und Sortieren benötigen JavaScript. Leere Beschriftungen oder Ziele bestehender Punkte behalten die Übersetzung beziehungsweise das bisherige Verhalten. Interne Ziele beginnen mit `/` und sind relativ zum Anwendungskontext; externe Links unterstützen `http`, `https` und `mailto`. Eigene Ziele machen Login-, Chat- und Sprachmenü zu normalen Links. Modul- und Sitzungsbedingungen gelten weiterhin.

Öffentliche und Community-Seiten zeigen die zur aktiven Sitzung passende Navigation für registrierte Benutzer, Gäste oder nicht angemeldete Besucher. In einem Chat-Tab eingebettete Seiten blenden ihre eigene Navigation aus.

### Konfiguration und Daten

Die Laufzeitdateien liegen unter `~/.homewebcom` des Tomcat-Prozessbenutzers:

| Pfad | Zweck |
|---|---|
| `config/config.json` | Haupteinstellungen, Datenbank, Zeitzone, rechtliche Angaben und `console.path` |
| `config/modules.json` | Modul- und Direkt-Login-Schalter |
| `config/navigation.json` | Gemeinsame Navigation |
| `config/pages.json` | Eigene Seiten |
| `config/themes.json` | Theme-Einstellungen je Skin |
| `config/hosts.json` | Zuordnung virtueller Hosts zu Skins |
| `config/cmdcfg.json` | Befehlsberechtigungen |
| `config/mail.json` | SMTP-Einstellungen |
| `data/hwebchat.sqlite` | Standard-SQLite-Datenbank, sofern kein anderer Pfad konfiguriert ist |

Die Kopie unter [web/default-homewebcom/](web/default-homewebcom/) liefert Erststart-Vorlagen und ist nicht die laufende Konfiguration. JSP-Layouts und -Templates liegen im WAR und werden nicht aus `~/.homewebcom/templates` geladen; Änderungen erfolgen im Quellcode und erfordern ein erneutes Deployment. Das Laufzeitverzeichnis **und** bei Serverdatenbanken zusätzlich die Datenbank sichern.

Die Befehlstexte stehen in [messages.properties](src/resources/i18n/messages.properties) (Deutsch) und [messages_en.properties](src/resources/i18n/messages_en.properties) (Englisch). Änderungen erfordern einen neuen Build und ein erneutes Deployment; in der Konsole lassen sich nur noch die Befehlsrechte konfigurieren. Vorhandene Laufzeitdateien `commands.json` und `commands_en.json` bleiben unverändert, werden aber nicht mehr gelesen. Eigene Texte bei Bedarf vor dem Upgrade in die Sprachdateien übernehmen.

Für manuelle Datenbankkonfiguration ist `sql.type` gleich `sqlite`, `mysql` oder `postgresql`. SQLite verwendet `sql.path` und ignoriert `sql.user` und `sql.pw`; PostgreSQL verwendet `sql.host` (`host:port`), `sql.db`, `sql.user` und `sql.pw`. Die `legal.*`-Einstellungen füllen Impressum und Datenschutz; eine leere Telefonnummer wird ausgeblendet. Eine Änderung von `console.path` ändert die Konsolenadresse und macht die Standardadresse unerreichbar.

Die Statistik verwendet `time_zone`; bereits gespeicherte Intervalle werden bei Änderungen nicht umgerechnet, und die doppelte Stunde am Ende der Sommerzeit wird zusammengefasst. Zufällige Nickfarben streben mindestens 4,5:1 Kontrast zum dunklen Design an, abhängig von den Einschränkungen `bright_0`–`bright_5`; individuelle Raumfarben können abweichen.

## Betrieb und Sicherheit

- Starke, unterschiedliche Passwörter für Administratorkonten und Datenbank wählen. Zugriff auf die Konsole erhalten registrierte Konten ab dem konfigurierten `status_admin`; die bisherigen separaten Konsolenzugangsdaten werden auch bei bestehenden Installationen ignoriert. Zugriff auf das Setup während der Installation und auf `~/.homewebcom` mit Zugangsdaten und Daten einschränken.
- HTTPS für Website und WebSocket-Verbindungen verwenden, Java und Tomcat aktuell halten und den Tomcat Manager schützen. Die `secure`-Einstellung des Sitzungscookies in [web.xml](web/WEB-INF/web.xml) an das HTTPS-Deployment anpassen.
- Nur zur kurzfristigen Fehlersuche zeigt `-Dhwebchat.showStackTrace=true` vorhandene Stacktraces auf der 500-Seite. Diese können vertrauliche Informationen enthalten: nicht auf öffentlich erreichbaren Servern aktivieren und danach wieder abschalten.
- Bei Java-Buildfehlern `java -version` und `mvn -v` prüfen. Bei Datenbankproblemen die laufende `config/config.json`, Verbindungsdaten und Berechtigungen prüfen, nicht nur die Vorlagen im Repository.

## Entwicklung

- Java-Quellen: [src/java/net/midiandmore/chat/](src/java/net/midiandmore/chat/); Web-Ressourcen: [web/](web/); Maven-Projekt: [pom.xml](web/WEB-INF/pom.xml). Der WebSocket-Endpunkt ist `/Chat`; Profilbild-Uploads erfolgen über `/upload.jsp`.
- Seiten verwenden JSP, EL/JSTL und das gemeinsame [layout.tag](web/WEB-INF/tags/layout.tag). Dessen Inhalt ist skriptlos: Werte vor dem Layout berechnen und darin EL/JSTL verwenden. Deutsche und englische Resource Bundles liegen unter [src/resources/i18n/](src/resources/i18n/); JSON-Konfigurationen können weiterhin unterstützte `%...%`-Platzhalter enthalten.
- Dokumente, Skripte und Stylesheets besitzen Endpunkte unter `/content/`. Community-Dokumente wie `/content/rules_com.jsp?community=true` benötigen eine Community-Sitzung. Eigene Inhalte benötigen einen entsprechenden JSP-Endpunkt.
- Die früheren URLs `/content.jsp?service=...&doc=...`, `community_`-JSPs, `/Start`, `/Setup` und `/UploadFile` werden nicht mehr bereitgestellt. Alte `paths.json`- und `path_*`-Einstellungen werden nicht mehr verwendet; eigene Links müssen angepasst werden. Manche alten Frame-Templates enthalten weiterhin nicht unterstützte `?page=`-Links für die frühere HTTP-Chateingabe.

## Lizenz

Siehe [LICENSE](LICENSE).
