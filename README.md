# HWebChat

Web-based community chat for Apache Tomcat, built with Jakarta EE, JSP and WebSocket.

[Deutsch](README.de.md) · [Demo](https://www.hwebchat.de) · [Releases](https://github.com/WarPigs1602/HWebChat/releases) · [Issues](https://github.com/WarPigs1602/HWebChat/issues)

## Features

### Chat and community

- Real-time room and private chats via WebSocket; guest and registered-user login, rooms and napping rooms.
- Typing indicators with nickname colors, Tab nickname completion, emoji and offline messages.
- Moderation with kick, ban and gag; configurable rooms, commands and permissions.
- User profiles with guestbooks and profile-picture uploads, account and password management, friends and messages.
- Forum with categories, threads, replies and moderation. Visitors can read; writing requires a registered community session and the relevant permissions.
- News, statistics and a points-based toplist. Administrators can create, edit and delete news; edits preserve the original author and publication date.
- Home and signed-in community pages show chat counts, useful links and the latest news when available.
- German and English interface: a language cookie takes precedence, followed by the `lang` parameter and browser preferences; English is the fallback. Automatic detection does not create a language cookie. The chat language can change without reloading; filters and collapsed user groups are preserved.
- Light and dark themes; the selection is stored in the `theme` cookie.

### Administration and design

- Administration console for users, rooms, modules, news, configuration, command permissions, help, emoticons, hosts, mail and MIME types.
- Console controls and messages support German and English; use its language menu to switch (this returns to the console home page). Configuration values and custom content remain as entered.
- WYSIWYG theme creator with preview, colors, typography, logo and custom CSS for skins.
- WYSIWYG page editor with preview and sanitized HTML; public pages use `/page.jsp?slug=<name>`.
- Shared, configurable navigation for public and community pages, including custom links, visibility, labels, destinations and ordering.
- SQLite, MySQL/MariaDB and PostgreSQL storage; browser-based first-start setup.

## Modules

The **Modules** section of the administration console controls the following settings. Defaults apply to a new installation; `1` enables and `0` disables an option. Settings are saved in `~/.homewebcom/config/modules.json` for the Tomcat process user.

| Setting | Default | Purpose |
|---|---|---|
| `chat` | `1` | Chat login and real-time chat rooms. |
| `board` | `1` | Forum with public reading and registered-user participation. |
| `news` | `1` | Public news page and news administration. |
| `stats` | `1` | Statistics page with message, user, room and activity charts. |
| `toplist` | `1` | Ranking of registered users by points. |
| `direct_chat_login` | `0` | When chat is enabled, use direct chat login instead of community login. |

Disabled modules are removed from the navigation; their corresponding chat login, forum, news, statistics or toplist pages return 404. When `news` is disabled, its console administration is also unavailable. `direct_chat_login` only applies while `chat` is enabled; with direct login enabled, the community login page is unavailable.

Navigating away from a chat, including with the browser's Back button, closes its connection. When a direct chat session disconnects, its session is ended and signing in again is required. Disconnecting from a chat entered through the community does not end the community session.

## Requirements

| Component | Requirement |
|---|---|
| Java | JDK 21 for building; Java 21 for running |
| Maven | 3.8+ for building |
| Servlet container | Apache Tomcat 11.x (Servlet 6.1, JSP 4.0, WebSocket 2.2); Tomcat 10.1 does not provide these API versions |
| Database | SQLite, MySQL/MariaDB or PostgreSQL |
| Mail | Reachable SMTP server: setup sends a test email to the first user |
| Storage | Writable `~/.homewebcom` in the Tomcat process user's home directory |

PostgreSQL requires `pgcrypto`; setup attempts to create the extension, so the database user needs the necessary privileges or an administrator must install it beforehand. For MySQL/MariaDB and PostgreSQL, create the database and credentials before setup.

## Quick start

1. **Build the WAR** from the repository root, or download `HWebChat.war` from [Releases](https://github.com/WarPigs1602/HWebChat/releases):

   ```bash
   git clone https://github.com/WarPigs1602/HWebChat.git
   cd HWebChat
   mvn -f web/WEB-INF/pom.xml clean package
   ```

   The build creates `target/HWebChat.war` at the repository root. Maven can also be run from `web/WEB-INF`; in that directory the artifact is `../../target/HWebChat.war`.

2. **Deploy** `target/HWebChat.war` to your Tomcat 11 `webapps` directory using your server's deployment procedure. With the default WAR name and Tomcat configuration, the context is `/HWebChat`:

   ```text
   http://localhost:8080/HWebChat/
   ```

   Renaming the WAR or configuring the context changes this path. For upgrades, back up your data and follow your Tomcat deployment procedure; do not delete the data directory when replacing the application.

3. **Run setup** at `http://localhost:8080/HWebChat/setup.jsp` before exposing the installation publicly. Choose SQLite (the default), MySQL or PostgreSQL; supply SMTP settings, the first chat user, console path, room and legal information. The wizard copies the initial configuration and data from `web/default-homewebcom/`, creates database tables and the first administrator, tests SMTP and writes the configuration. Sign in with that account to access the console.

4. Open `http://localhost:8080/HWebChat/start.jsp` and sign in. The console is at `/HWebChat/console.jsp` by default, or at the `console.path` selected during setup.

Setup creates the predefined rooms Development, Exil, Lounge and Staff-Lounge; it does not create an arbitrary room entered as the default. If you choose another default room, create it in the console.

Setup is complete only when both `~/.homewebcom/config/config.json` and `~/.homewebcom/config/.setup_complete` exist. A failed setup may already have written files or database records, and retrying copies defaults over existing files; inspect and back them up before retrying.

## Administration

### Pages and navigation

In the console, choose **WYSIWYG-Seitenersteller**, enter a URL name, title and content, preview and save. A URL name uses lowercase letters, digits and hyphens and cannot be changed after creation; saved pages are publicly accessible at `/page.jsp?slug=<name>`. HTML is sanitized, so unsupported markup and attributes are removed. Add a navigation link separately if desired.

Under **Navigation**, configure existing items or add custom links, including links to your pages. Filter the main menu and the login and language submenus; arrows reorder items within their menu. Adding and reordering items requires JavaScript. Empty label or destination fields on built-in items retain their translated label or default behavior. Internal destinations begin with `/` and are relative to the application context; external links support `http`, `https` and `mailto`. Custom destinations turn the login, chat and language menus into ordinary links. Module and session restrictions still apply.

Public and community pages use the navigation appropriate for the active registered-user, guest or signed-out session. Embedded pages hide their own navigation inside a chat tab.

### Configuration and data

Runtime files are under `~/.homewebcom` for the Tomcat process user:

| Path | Purpose |
|---|---|
| `config/config.json` | Main settings, database, time zone, legal details and `console.path` |
| `config/modules.json` | Module and direct-login switches |
| `config/navigation.json` | Shared navigation configuration |
| `config/pages.json` | Custom pages |
| `config/themes.json` | Theme settings per skin |
| `config/hosts.json` | Virtual-host-to-skin mappings |
| `config/cmdcfg.json` | Command permissions |
| `config/mail.json` | SMTP settings |
| `data/hwebchat.sqlite` | Default SQLite database, unless another path is configured |

The copy in [web/default-homewebcom/](web/default-homewebcom/) supplies first-start defaults, not the live configuration. JSP layouts and templates are packaged in the WAR, not loaded from `~/.homewebcom/templates`; change them in the source and redeploy. Back up the runtime directory **and**, for a server database, the database separately.

Command texts are in [messages.properties](src/resources/i18n/messages.properties) (German) and [messages_en.properties](src/resources/i18n/messages_en.properties) (English). Editing them requires rebuilding and redeploying; the console only configures command permissions. Existing runtime `commands.json` and `commands_en.json` files are left untouched but no longer read. Copy any custom texts you want to keep into the language bundles before upgrading.

For manual database configuration, `sql.type` is `sqlite`, `mysql` or `postgresql`. SQLite uses `sql.path` and ignores `sql.user` and `sql.pw`; PostgreSQL uses `sql.host` (`host:port`), `sql.db`, `sql.user` and `sql.pw`. The `legal.*` settings fill the imprint and privacy pages; an empty phone number is hidden. Changing `console.path` changes the console address and makes the default address unavailable.

Statistics use `time_zone`; previously stored buckets are not converted when it changes, and the repeated hour at the end of daylight saving time is combined. Random nickname colors target at least 4.5:1 contrast against the dark theme, subject to the `bright_0`–`bright_5` restrictions; custom room colors may differ.

## Operation and security

- Choose strong, distinct passwords for administrator accounts and the database. Console access requires a registered account with at least the configured `status_admin`; the former separate console credentials are ignored, including on existing installations. Restrict access to setup during installation and to `~/.homewebcom`, which contains credentials and data.
- Use HTTPS for the site and WebSocket traffic, keep Java and Tomcat updated, and protect the Tomcat Manager. Align the session cookie `secure` setting in [web.xml](web/WEB-INF/web.xml) with your HTTPS deployment.
- For temporary debugging only, `-Dhwebchat.showStackTrace=true` displays available stack traces on the 500 page. They can contain sensitive information: do not enable this on a publicly accessible server, and disable it afterwards.
- If the build reports a Java version error, check `java -version` and `mvn -v`. For database issues, check the live `config/config.json`, connection details and permissions rather than only the repository defaults.

## Development

- Java sources: [src/java/net/midiandmore/chat/](src/java/net/midiandmore/chat/); web resources: [web/](web/); Maven project: [pom.xml](web/WEB-INF/pom.xml). The WebSocket endpoint is `/Chat`; profile-picture uploads use `/upload.jsp`.
- Pages use JSP, EL/JSTL and the shared [layout.tag](web/WEB-INF/tags/layout.tag). Its body is scriptless: compute values before the layout and use EL/JSTL inside it. German and English resource bundles are in [src/resources/i18n/](src/resources/i18n/); JSON configuration strings can still use their supported `%...%` placeholders.
- Documents, scripts and stylesheets have endpoints under `/content/`. Community-only documents such as `/content/rules_com.jsp?community=true` require a community session. Custom content needs a corresponding JSP endpoint.
- Legacy `/content.jsp?service=...&doc=...`, `community_` JSPs, `/Start`, `/Setup` and `/UploadFile` URLs are no longer served. Old `paths.json` and `path_*` settings are no longer used; update custom links. Some legacy frame templates still contain unsupported `?page=` links for the former HTTP chat input.

## License

See [LICENSE](LICENSE).
