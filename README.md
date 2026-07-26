# HWebChat Community Edition

Web-based community chat (Jakarta EE / WebSocket) for Apache Tomcat.

- **Demo:** https://www.hwebchat.de  
- **Issues:** https://github.com/WarPigs1602/HWebChat-Community-Edition/issues  

---

## Features

- Public and private chat rooms (WebSocket real-time)
- Guest login and registered users
- Offline messages (whisper while offline)
- Emoji support, file uploads
- Moderation (kick, ban, gag, …)
- Admin console, communities / napping rooms
- Skins and templates under `~/.homewebcom` (`native` German, `native_en` English)
- MySQL / MariaDB storage

---

## Requirements

| Component | Version / notes |
|-----------|-----------------|
| **JDK** | **21** (source/target in `pom.xml`) |
| **Maven** | 3.8+ |
| **Apache Tomcat** | 10.1+ (Jakarta EE 9+, Servlet 6 / WebSocket) |
| **MySQL or MariaDB** | 10.6+ recommended |
| **Config home** | `~/.homewebcom` (user home of the Tomcat process user) |

---

## Quick start

### 1. Clone

```bash
git clone https://github.com/WarPigs1602/HWebChat-Community-Edition.git
cd HWebChat-Community-Edition
```

### 2. Build with Maven

```

```bash
cd web/WEB-INF
mvn clean package
```

Artifact:

```text
web/target/HWebChat_Community_Edition.war
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
cp web/target/HWebChat_Community_Edition.war "$CATALINA_HOME/webapps/"
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
http://localhost:8080/HWebChat_Community_Edition/Setup
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
http://localhost:8080/HWebChat_Community_Edition/Start
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
| `~/.homewebcom/config/config.json` | Main settings (SQL, timeouts, status levels, …) |
| `~/.homewebcom/config/hosts.json` | Virtual host → skin |
| `~/.homewebcom/config/commands.json` | Chat command texts |
| `~/.homewebcom/config/paths.json` | URL path names |
| `~/.homewebcom/templates/native/` | German skin (HTML, JS, CSS) |
| `~/.homewebcom/templates/native_en/` | English skin (HTML, JS, CSS) |

The repo copy `web/default-homewebcom/` is a template; on first start the app creates `~/.homewebcom` automatically and copies all files from there.

---

## Development notes

- **Package:** `net.midiandmore.chat`
- **Entry servlet:** `ChatPages` → `/Start`
- **Setup wizard:** `SetupServlet` → `/Setup` (first-start configuration)
- **WebSocket:** `Chat` endpoint (see `@ServerEndpoint` in sources)
- **Upload:** `/UploadFile`
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
Default UI/templates are German. An English skin is available as `.homewebcom/templates/native_en/`, plus matching config files like `help_en.json` and `profile_en.json`. Set the skin in `hosts.json` to switch languages.

**First-start setup?**  
On first request, the app redirects to `/Setup`. The wizard creates `~/.homewebcom`, database tables, the first admin user, and writes a complete `config.json` automatically.

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
