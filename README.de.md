# HWebChat

Webbasierter Community-Chat für Apache Tomcat, entwickelt mit Jakarta EE, JSP und WebSocket.

[English](README.md) · [Demo](https://www.hwebchat.de) · [Releases](https://github.com/WarPigs1602/HWebChat-Community-Edition/releases) · [Issues](https://github.com/WarPigs1602/HWebChat-Community-Edition/issues)

## Funktionen

### Chat und Community

- Öffentliche Räume können als Gast oder mit einem Benutzerkonto betreten werden; Nachrichten und Privatchats werden per WebSocket in Echtzeit übertragen. Administratoren konfigurieren Raumthemen, Zugangsbeschränkungen und Aussehen.
- Registrierte Benutzer können einen persönlichen Napping-Raum mit eigenem Titel und eigenen Farben erstellen.
- Schreibanzeige, Nickfarben, Farbauswahl, Nick-Vervollständigung per Tab und ein Emoji-Katalog machen Unterhaltungen übersichtlicher und persönlicher.
- Slash-Befehle decken Chat- und Moderationsaktionen ab. Die Chat-Hilfe zeigt die für den jeweiligen Berechtigungsstatus verfügbaren Befehle; Administratoren konfigurieren Befehlsrechte und raumspezifische Berechtigungen. Zur Moderation gehören Kick, Ban und Gag.
- Registrierte Benutzer können Kontodaten und öffentliche Profile bearbeiten, Profilbilder hochladen, Gästebucheinträge verwalten, ihre Freundesliste pflegen und private Nachrichten senden. Nachrichten an abwesende Benutzer bleiben bis zu deren nächstem Besuch erhalten.
- Forum mit Kategorien, Foren, Themen, Antworten und Moderation. Besucher können Foren lesen, die für Gäste freigegeben sind; zum Schreiben sind eine registrierte Community-Sitzung und die entsprechenden Berechtigungen nötig.
- News, Statistiken und eine punktbasierte Topliste. Administratoren können News veröffentlichen, bearbeiten und löschen; beim Bearbeiten bleiben ursprünglicher Autor und Veröffentlichungsdatum erhalten. Statistiken zeigen Chat-Aktivität über die Zeit, stark genutzte Räume und Benutzerhöchststände.
- Öffentliche Startseite und angemeldete Community-Seite zeigen Chat-Kennzahlen, hilfreiche Links und, falls vorhanden, die neuesten News.
- Registrierte Benutzer können bei aktiviertem Benutzerhomepage-Modul eigene öffentliche Homepages mit einem bereinigten WYSIWYG-Editor gestalten.
- Deutsche und englische Oberfläche: Auf öffentlichen Seiten hat das Cookie `lang` Vorrang vor dem Parameter `lang` und den Browsereinstellungen; Englisch ist der Standard. Im Chat wird zusätzlich die Sitzungssprache verwendet, bevor auf die Browsersprache zurückgegriffen wird. Die automatische Erkennung setzt kein Sprach-Cookie. Im Chat ist der Sprachwechsel ohne Neuladen möglich; Filter und eingeklappte Benutzergruppen bleiben erhalten.
- Host-basiertes Erscheinungsbild: Jedem virtuellen Host kann ein Skin zugeordnet werden, sodass verschiedene Hostnamen unterschiedliche Themes verwenden. Nicht zugeordnete Hosts verwenden den konfigurierten Standard-Skin.

### Administration und Gestaltung

- Adminkonsole für Benutzer, Räume, Module, News, Konfiguration, Befehlsrechte, Hilfe, Emoticons, eigene Fun-Befehle, virtuelle Hosts, Mail und MIME-Typen.
- Optionaler Dateimanager für Administratoren mit privatem Dateiordner, Upload, Download, Löschen, Dateititeln und zentral gepflegten Kategorien.
- Optionales öffentliches Downloadarchiv für einzeln freigegebene Dateien; Dateien werden nach Kategorie gruppiert und sind filterbar.
- Bedienelemente und Meldungen der Konsole unterstützen Deutsch und Englisch; das Sprachmenü wechselt die Sprache und führt zur Konsolenstartseite. Konfigurationswerte und eigene Inhalte bleiben unverändert.
- WYSIWYG-Theme-Creator mit Live-Vorschau, Farben, Typografie, Logo und eigenem CSS pro Skin. Skins in der Konsole erstellen oder bearbeiten und anschließend unter **Hosts** virtuellen Hosts zuordnen.
- WYSIWYG-Seitenersteller mit Vorschau und bereinigtem HTML; öffentliche Seiten sind unter `/page.jsp?slug=<name>` erreichbar.
- Gemeinsame, konfigurierbare Navigation für öffentliche und Community-Seiten mit eigenen Links, Sichtbarkeit, Beschriftung, Zielen und Reihenfolge.
- Speicherung in SQLite, MySQL/MariaDB und PostgreSQL; browserbasierter Erststart-Assistent.

## Module

Im Bereich **Module** der Adminkonsole lassen sich folgende Einstellungen verwalten. Die angegebenen Standardwerte gelten für Neuinstallationen und für Einstellungen, die in `modules.json` fehlen; `1` aktiviert und `0` deaktiviert eine Option. Gespeichert wird in `~/.homewebcom/config/modules.json` des Tomcat-Prozessbenutzers.

| Einstellung | Standard | Zweck |
|---|---|---|
| `chat` | `1` | Chat-Login und Chaträume in Echtzeit. |
| `board` | `1` | Forum; das Lesen durch Gäste kann pro Forum aktiviert werden, zum Schreiben sind registrierte Benutzer und die entsprechenden Berechtigungen nötig. |
| `news` | `1` | Öffentliche Newsseite und Newsverwaltung. |
| `stats` | `1` | Statistikseite mit Nachrichten-, Benutzer-, Raum- und Aktivitätsdiagrammen. |
| `toplist` | `1` | Rangliste registrierter Benutzer nach Punkten. |
| `user_homepage` | `0` | Benutzer können eigene öffentliche Homepages erstellen und unter `/~<Nick>/` veröffentlichen. |
| `file_manager` | `0` | Privater Dateispeicher für Administratoren; öffentlich abrufbar sind Dateien nur über das separat aktivierte Archiv und nach ausdrücklicher Freigabe. |
| `download_archive` | `0` | Öffentliches Downloadarchiv für im Dateimanager ausdrücklich freigegebene Dateien; benötigt `file_manager`. |
| `direct_chat_login` | `0` | Bei aktiviertem Chat direkten Chat-Login statt Community-Login verwenden. |

Deaktivierte Module verschwinden aus der Navigation; die zugehörigen Chat-Login-, Forum-, News-, Statistik-, Toplisten-, Benutzerhomepage-, Dateimanager- und Downloadarchiv-Seiten antworten mit 404. Ist `news` deaktiviert, ist auch dessen Verwaltung in der Konsole nicht verfügbar. `user_homepage`, `file_manager` und `download_archive` sind standardmäßig deaktiviert. Bei aktiviertem `user_homepage` können registrierte Benutzer ihre öffentliche Homepage in den Kontoeinstellungen bearbeiten. Der Dateimanager verwaltet private Dateien unter `~/.homewebcom/files` (maximal 10 MiB pro Datei, 100 MiB Gesamtspeicher und 1.000 Dateien insgesamt). Das öffentliche Downloadarchiv erfordert beide aktivierten Module; nur ausdrücklich freigegebene Dateien sind öffentlich herunterladbar. `direct_chat_login` wirkt nur bei aktiviertem `chat`; beim direkten Login ist die Community-Login-Seite nicht verfügbar.

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

1. **WAR bauen** im Repository-Wurzelverzeichnis oder `HWebChat.war` aus den [Releases](https://github.com/WarPigs1602/HWebChat-Community-Edition/releases) herunterladen:

   ```bash
   git clone https://github.com/WarPigs1602/HWebChat.git
   cd HWebChat-Community-Edition
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

Das Setup legt die vordefinierten Räume Development, Exil, Lounge und Staff-Lounge an, aber keinen im Feld **Standardraum** eingetragenen Raum (dort steht anfangs `Lobby`). Als Standard einen der angelegten Räume wählen oder einen eigenen Raum vor seiner Nutzung in der Konsole erstellen.

Das Setup gilt erst als abgeschlossen, wenn sowohl `~/.homewebcom/config/config.json` als auch `~/.homewebcom/config/.setup_complete` vorhanden sind. Ein fehlgeschlagenes Setup kann bereits Dateien oder Datenbankeinträge angelegt haben; beim erneuten Versuch werden vorhandene Dateien mit Vorlagen überschrieben. Vorher prüfen und sichern.

## Administration

### Seiten und Navigation

In der Konsole **WYSIWYG-Seiten** wählen, URL-Namen, Titel und Inhalt eingeben, Vorschau prüfen und speichern. Der URL-Name besteht aus Kleinbuchstaben, Ziffern und Bindestrichen und ist nach dem Erstellen nicht änderbar; gespeicherte Seiten sind unter `/page.jsp?slug=<name>` öffentlich erreichbar. HTML wird bereinigt, nicht unterstützte Markups und Attribute werden entfernt. Bei Bedarf separat einen Navigationslink hinzufügen.

Unter **Navigation** bestehende Punkte konfigurieren oder eigene Links ergänzen, etwa auf eigene Seiten. Filter zeigen das Hauptmenü sowie die Login- und Sprach-Untermenüs; Pfeile sortieren innerhalb des jeweiligen Menüs. Hinzufügen und Sortieren benötigen JavaScript. Leere Beschriftungen oder Ziele bestehender Punkte behalten die Übersetzung beziehungsweise das bisherige Verhalten. Interne Ziele beginnen mit `/` und sind relativ zum Anwendungskontext; externe Links unterstützen `http`, `https` und `mailto`. Eigene Ziele machen Login-, Chat- und Sprachmenü zu normalen Links. Modul- und Sitzungsbedingungen gelten weiterhin.

Öffentliche und Community-Seiten zeigen die zur aktiven Sitzung passende Navigation für registrierte Benutzer, Gäste oder nicht angemeldete Besucher. In einem Chat-Tab eingebettete Seiten blenden ihre eigene Navigation aus.

### Dateimanager und Downloadarchiv

`file_manager` und `download_archive` müssen in der Adminkonsole beide aktiviert werden; beide sind standardmäßig ausgeschaltet. Administratoren können Dateien im privaten Ordner `~/.homewebcom/files` hochladen, herunterladen und löschen. Für jede Datei lassen sich ein Titel und optional eine Kategorie vergeben. Kategorien werden zentral im Dateimanager verwaltet; Kategorien, die noch Dateien zugeordnet sind, können nicht gelöscht werden.

Dateien bleiben privat, bis sie einzeln freigegeben werden. Vor der Freigabe ist ein Titel erforderlich. Das öffentliche Archiv ist unter `/downloads.jsp` erreichbar, gruppiert Dateien nach Kategorien und bietet einen Kategorienfilter. Downloads werden als Datei-Anhang ausgeliefert; der private Speicherordner wird nicht öffentlich bereitgestellt.

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
| `files/` | Private Dateien sowie Titel-, Kategorie- und Freigabemetadaten des Dateimanagers |

Die Kopie unter [web/default-homewebcom/](web/default-homewebcom/) liefert Erststart-Vorlagen und ist nicht die laufende Konfiguration. JSP-Layouts und -Templates liegen im WAR und werden nicht aus `~/.homewebcom/templates` geladen; Änderungen erfolgen im Quellcode und erfordern ein erneutes Deployment. Das Laufzeitverzeichnis **und** bei Serverdatenbanken zusätzlich die Datenbank sichern.

Die Befehlstexte stehen in [messages.properties](src/resources/i18n/messages.properties) (Deutsch) und [messages_en.properties](src/resources/i18n/messages_en.properties) (Englisch). Änderungen erfordern einen neuen Build und ein erneutes Deployment; in der Konsole lassen sich nur noch die Befehlsrechte konfigurieren. Vorhandene Laufzeitdateien `commands.json` und `commands_en.json` bleiben unverändert, werden aber nicht mehr gelesen. Eigene Texte bei Bedarf vor dem Upgrade in die Sprachdateien übernehmen.

Für manuelle Datenbankkonfiguration ist `sql.type` gleich `sqlite`, `mysql` oder `postgresql`. SQLite verwendet `sql.path` und ignoriert `sql.user` und `sql.pw`; PostgreSQL verwendet `sql.host` (`host:port`), `sql.db`, `sql.user` und `sql.pw`. Die `legal.*`-Einstellungen füllen Impressum und Datenschutz; eine leere Telefonnummer wird ausgeblendet. Eine Änderung von `console.path` ändert die Konsolenadresse und macht die Standardadresse unerreichbar.

Die Statistik verwendet `time_zone`; bereits gespeicherte Intervalle werden bei Änderungen nicht umgerechnet, und die doppelte Stunde am Ende der Sommerzeit wird zusammengefasst. Zufällige Nickfarben werden für gute Lesbarkeit erzeugt; die Einstellungen `bright_0`–`bright_5` beschränken Farbkanalwerte, und individuelle Raumfarben können den Kontrast beeinflussen.

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
