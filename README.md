# HWebChat Community Edition

Web-based community chat (Jakarta EE / WebSocket) for Apache Tomcat.

- **Demo:** https://www.hwebchat.de  
- **Issues:** https://github.com/WarPigs1602/HWebChat-Community-Edition/issues  

---

## Features

- Public and private chat rooms (WebSocket real-time)
- Public news page managed through the admin console
- Typing indicators with nickname colors in rooms and private chats (up to five names)
- Nickname completion with Tab in the message field; press Tab again to cycle through matches
- Guest login and registered users
- Offline messages (whisper while offline)
- Emoji support, file uploads
- Moderation (kick, ban, gag, …)
- Admin console, communities / napping rooms
- Built-in skin `native` as JSP templates (`web/WEB-INF/jsp`) with full i18n (German/English) via resource bundles and native EL/JSTL instead of `%placeholders%`
- MySQL / MariaDB, PostgreSQL, or SQLite storage

Statistics use the configured `time_zone`, just like the chat. Previously stored
statistics buckets are not converted when the time zone changes. The repeated
hour at the end of daylight saving time is combined into one bucket.

The news page is available at `news.jsp`. Administrators can create,
edit and delete articles in the admin console; editing preserves the original
author and publication date.
The home page and the community page after login show chat counts, useful links
and, when available, the latest news article. Custom templates can position these sections with
`%homepage_extra%`; otherwise they are appended to the template.

---

## Requirements

| Component | Version / notes |
|-----------|-----------------|
| **JDK** | **21** (source/target in `pom.xml`) |
| **Maven** | 3.8+ |
| **Apache Tomcat** | 10.1+ (Jakarta EE 9+, Servlet 6 / WebSocket) |
| **Database** | MySQL/MariaDB 10.6+, PostgreSQL, or SQLite |
| **Config home** | `~/.homewebcom` (user home of the Tomcat process user) |

---

## Quick start

### 1. Clone

```bash
git clone https://github.com/WarPigs1602/HWebChat-Community-Edition.git
cd HWebChat-Community-Edition
```

### 1a. Or use the pre-built WAR

Alternatively, download the latest release directly:

```text
https://github.com/WarPigs1602/HWebChat-Community-Edition/releases/download/261115/HWebChat_Community_Edition.war
```

Then skip to step 3 (Deploy to Tomcat).

### 2. Build with Maven

```

```bash
cd web/WEB-INF
mvn clean package
```

 Artifact:

 ```text
 target/HWebChat_Community_Edition.war
 ```

 Useful variants:

 ```bash
 mvn clean package -DskipTests   # skip tests
 mvn clean compile               # compile only
 mvn -o package                  # offline (local repo only)
 ```

 **NetBeans:** You can still open the project; the build is defined by this Maven `pom.xml` (not only Ant).

 ### 3. Deploy to Tomcat

 ```bash
 # Stop Tomcat if needed, then:
 cp target/HWebChat_Community_Edition.war "$CATALINA_HOME/webapps/"
 # Start Tomcat — it expands the WAR automatically
 ```

Or copy into your existing `webapps` path (e.g. `/home/you/tomcat/webapps/`).

After deploy, context path is typically:

```text
http://localhost:8080/HWebChat_Community_Edition/
```

### 4. First-start setup

On first request, the app detects that `~/.homewebcom` is missing and redirects you to the built-in setup wizard:

```text
http://localhost:8080/HWebChat_Community_Edition/setup.jsp
```

The setup wizard will:

1. Create `~/.homewebcom` and copy all templates/configs from the WAR
2. Let you configure database, SMTP mail, admin console, and the first user
3. Create all required database tables automatically
4. Create the first chat user (with admin + forum moderator rights)
5. Create the configured default room
6. Write a complete `config.json` with all required settings

After setup, the start page is available at:

```text
http://localhost:8080/HWebChat_Community_Edition/start.jsp
```

### 5. Redeploy after code changes

```bash
cd web/WEB-INF
mvn clean package
rm -rf "$CATALINA_HOME/webapps/HWebChat_Community_Edition"
cp ../../target/HWebChat_Community_Edition.war "$CATALINA_HOME/webapps/"
# restart Tomcat or wait for auto-redeploy
```

Templates/config under `~/.homewebcom` are **not** inside the WAR — edit them live; restart Tomcat only if classes/config loaders cache aggressively.

---

## Configuration reference

| Path | Purpose |
|------|---------|
| `~/.homewebcom/config/config.json` | Main settings (SQL, timeouts, status levels, …); `legal.name`, `legal.street`, `legal.zip`, `legal.city`, `legal.phone`, `legal.email` fill the imprint and privacy pages (empty phone = hidden) |
| `~/.homewebcom/config/hosts.json` | Virtual Host → skin |
| `~/.homewebcom/config/commands.json` | Chat command texts |
| `~/.homewebcom/templates/native/`, `native_en/` | Plain scripts and stylesheets (the page templates are JSPs in the WAR) |

The setup wizard supports all three database backends. SQLite is the default. For manual configuration, set `sql.type` to `sqlite`, `mysql`, or `postgresql`. PostgreSQL uses `sql.host` (`host:port`), `sql.db`, `sql.user`, and `sql.pw`; `pgcrypto` must be installed in the database (the setup wizard attempts to install it). SQLite uses `sql.path`, defaulting to `~/.homewebcom/data/hwebchat.sqlite`; `sql.user` and `sql.pw` are ignored.

The repo copy `web/default-homewebcom/` is a template; on first start the app creates `~/.homewebcom` automatically and copies all files from there.

Random colors for new users and guests have at least 4.5:1 contrast against the dark theme. The `bright_0` through `bright_5` digit restrictions still apply; adjust them if they prevent sufficiently light colors. Custom room colors may have different contrast.

Pages embedded in a chat tab hide their own navigation bar; it remains visible outside the chat tab. This also applies to previously copied page designs with a `nav.navbar` directly inside `body`.

The navigation bar offers a dark alternative to the light default theme ("Theme" menu, stored in the `theme` cookie). The part of a style sheet after the comment `/* theme:dark` belongs to the dark theme only and is left out for the light theme. In the chat, the language menu changes the language in real time over the open WebSocket connection (no reload); elements with `data-i18n="<command key>"`, the send button and the user list are translated. Filters and collapsed groups are preserved.

Public and community pages select their navigation according to the active community session: registered user, guest, or signed out. Expired sessions receive the public navigation.

---

## Development notes

- **Package:** `net.midiandmore.chat`
- **Entry page:** `/start.jsp`; other pages are directly accessible, e.g. `/login.jsp`, `/login_chat.jsp`, `/account.jsp`, `/account_com.jsp`, `/board.jsp`, `/register.jsp`, `/news.jsp` and `/console.jsp`.
- **Content:** Documents, scripts and style sheets are the JSPs below `/content/`, for example `/content/rules.jsp`, `/content/script_chat.jsp` and `/content/style_page.jsp`; each JSP contains its template. Documents that are only meant for logged-in community users are called with `community=true`, for example `/content/rules_com.jsp?community=true`; without a community session the timeout page is shown. Scripts and style sheets without a template of their own (jQuery, Bootstrap, …) are delivered from the skin by their JSP. The former `/content.jsp?service=...&doc=...` URL and the `community_` JSPs are removed; custom links must be updated.
- Services that never had content (`chatstream`, `fader`, `refreshstream`, `refresh_error`) have no endpoint and answer with the 404 page. Custom document, script or stylesheet templates require a corresponding JSP endpoint under `/content/`.
- **Setup wizard:** `/setup.jsp` (logic in `SetupService`)
- **WebSocket:** `Chat` endpoint (see `@ServerEndpoint` in sources)
- **Upload:** `/upload.jsp` (multipart POST)
- Pages call the shared Java methods through `Web`. A page that is requested directly starts with `Web.begin(...)`, which checks the setup, takes over the language and provides the template values as request attributes. Web paths are fixed and take the application's context path into account. Existing `paths.json` files and `path_*` entries in `config.json` are no longer used or deleted; the path editor is removed. Existing custom links may need updating.
- Some legacy frame templates still use unsupported `?page=` links for the former HTTP chat input; fixed paths do not make those links functional.
- The old `/Start`, `/Setup` and `/UploadFile` URLs are no longer served.
- **Jakarta EE:** `jakarta.*` APIs (not `javax.*` for Servlet/WebSocket)

Java sources: `src/java/net/midiandmore/chat/`  
Web resources: `web/`

---

## Security

- Strong passwords for MySQL and admin console (set during first-start setup)
- Do not expose Tomcat Manager publicly without auth
- Keep JDK and Tomcat updated
- Restrict permissions on `~/.homewebcom/config` (contains DB password)
- Prefer HTTPS (Tomcat SSL connector or reverse proxy)

---

## FAQ

**Language?**  
Default UI/templates are German. English is available through the same JSP templates, plus matching config files like `help_en.json` and `profile_en.json`. The language is automatically detected (`lang` cookie, `lang` parameter, `Accept-Language`) and can be changed in the frontend.

### JSP templates and i18n

- Pages are JSPs. The JSPs in the web root (`memory.jsp`, `captcha.jsp`, `webchat_stats.jsp`, `link.jsp`, `logout.jsp`, `top.jsp`, `emot.jsp`, `help.jsp`, `read_message.jsp`, `stats.jsp`, `image.jsp`, `news.jsp`, `password.jsp`, `register.jsp`, `login.jsp`, `login_chat.jsp`, `profile.jsp`, `account.jsp`, `account_com.jsp`, `board.jsp`, `console.jsp`) and in `content/` handle their requests directly. Some still include shared markup from `web/WEB-INF/jsp`; other page handlers also continue to use templates there: `pages`, `console`, `mail`, `system` and `includes`.
- A page wraps its content with `<t:layout design="start">…</t:layout>`. [layout.tag](web/WEB-INF/tags/layout.tag) is the only layout: it composes `includes/header.jsp` (`<head>`), `includes/navbar.jsp` (site, console and logo-only navigation) and `includes/footer.jsp` (legal links and scripts) around the content. The design name only selects the parameters (title, style sheets, navigation variant, wrapper, footer parts) in the list at the top of the tag; `includes/navbar-chat.jsp` is the navigation of the chat window.
- Values are read with EL, no `%placeholder%` replacement is involved: `${nick}`, `${sid}`, `${skin}`, `${paths.login}` (fixed path), `${com.design_nav_start}` (commands.json), `${help.away}`, `${profile.hobby}`, `${SERVER_SOFTWARE}`, … Page specific values are set in Java with `view.set("name", value)` on the `View` returned by `getTemplate(...)`; a `View` can be used as value of another `View`, which then sees the values of the surrounding one.
- Texts live in the resource bundles `src/resources/i18n/messages.properties` (German, default) and `messages_en.properties` and are printed with `<fmt:message key="…"/>`. Messages with values use `{0}`, `{1}`, … and `<fmt:param value="${…}"/>` (quote `'` as `''` and `{` as `'{'` in such messages). Add a language with `messages_<lang>.properties` using the same keys.
- Strings in JSON configuration files and fixed path definitions can still use `%name%`, `%PATH_[name]%` and `%com[name]%`; these are resolved when read. JSP paths without parameters do not end in `?`; existing configurations appending parameters with `&` remain compatible.
- Templates below `~/.homewebcom/templates` are not used any more except for the plain scripts and style sheets (jQuery, Bootstrap, …) in `native` / `native_en`. Change the JSPs in the WAR (or deploy an adapted WAR) to change the layout.
- Public pages in the web root: `index.jsp` (welcome file, redirects to `start.jsp`) and `404.jsp` and `500.jsp` (error pages); they follow the language detection above.

**First-start setup?**  
On first request, the app links to `/setup.jsp`. The wizard creates `~/.homewebcom`, database tables, the first admin user, and writes a complete `config.json` automatically.

**HTTPS?**  
Terminate TLS on Tomcat or a reverse proxy (nginx/Caddy). Align `secure` cookie flags in `web.xml` with your setup.

**Wrong DB / empty messages?**  
Confirm `sql.db` / user in **`~/.homewebcom/config/config.json`** (not only the repo copy) matches the database you imported.

**Build fails on Java version?**  
Use JDK 21: `java -version` and `mvn -v` should both report 21.

---

## License

See [LICENSE](LICENSE).

---

**Enjoy HWebChat!**
