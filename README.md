# HWebChat Community Edition

Web-based community chat (Jakarta EE / WebSocket) for Apache Tomcat.

> **Note:** This project is **legacy** and receives limited support. Prefer more modern alternatives for new projects.

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
- Skins and templates under `~/.homewebcom`
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

### 2. Runtime config (`~/.homewebcom`)

The app loads configuration from the **home directory of the user running Tomcat**:

```bash
cp -a .homewebcom ~/.homewebcom
# or merge into an existing ~/.homewebcom
```

Edit at least:

- `~/.homewebcom/config/config.json` — DB credentials, admin password, chat settings  
- `~/.homewebcom/config/hosts.json` — host → skin mapping  

Example DB settings in `config.json`:

```json
{"name":"sql.host","value":"localhost:3306","description":"MySQL host"},
{"name":"sql.user","value":"hwebchat","description":"MySQL user"},
{"name":"sql.pw","value":"secret","description":"MySQL password"},
{"name":"sql.db","value":"hwebchat","description":"MySQL database"},
{"name":"sql.prefix","value":"hwc_","description":"Table prefix"}
```

### 3. Database

```bash
mysql -u root -p -e "CREATE DATABASE hwebchat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p hwebchat < database.sql
```

Create a DB user matching `config.json` and grant rights on that database.

Create the database user:
```bash
mysql -u root -p -e "CREATE USER 'hwebchat'@'localhost' IDENTIFIED BY 'secure_password'; GRANT ALL ON hwebchat.* TO 'hwebchat'@'localhost'; FLUSH PRIVILEGES;"
```

**Collation tip:** Prefer one collation for all tables (e.g. `utf8mb4_unicode_ci`). Mixing collations can break JOINs on nicknames/messages.

### 4. Build with Maven

```bash
cd src/web/WEB-INF
mvn clean package
```

Artifact:

```text
src/target/HWebChat_Community_Edition.war
```

Useful variants:

```bash
mvn clean package -DskipTests   # skip tests
mvn clean compile               # compile only
mvn -o package                  # offline (local repo only)
```

**NetBeans:** You can still open the project; the build is defined by this Maven `pom.xml` (not only Ant).

### 5. Deploy to Tomcat

```bash
# Stop Tomcat if needed, then:
cp src/target/HWebChat_Community_Edition.war "$CATALINA_HOME/webapps/"
# Start Tomcat — it expands the WAR automatically
```

Or copy into your existing `webapps` path (e.g. `/home/you/tomcat/webapps/`).

After deploy, context path is typically:

```text
http://localhost:8080/HWebChat_Community_Edition/
```

Start page redirects to `/HWebChat_Community_Edition/Start`.

### 6. Redeploy after code changes

```bash
cd src/web/WEB-INF
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
| `~/.homewebcom/templates/native/` | Default skin (HTML, JS, CSS) |

Repo copy `.homewebcom/` is a template; **production uses `~/.homewebcom`**.

---

## Development notes

- **Package:** `net.midiandmore.chat`
- **Entry servlet:** `ChatPages` → `/Start`
- **WebSocket:** `Chat` endpoint (see `@ServerEndpoint` in sources)
- **Upload:** `/UploadFile`
- **Jakarta EE:** `jakarta.*` APIs (not `javax.*` for Servlet/WebSocket)

Java sources: `src/src/java/net/midiandmore/chat/`  
Web resources: `src/web/`

---

## Security

- Strong passwords for MySQL and admin console (`admin_password` in config)
- Do not expose Tomcat Manager publicly without auth
- Keep JDK and Tomcat updated
- Restrict permissions on `~/.homewebcom/config` (contains DB password)
- Prefer HTTPS (Tomcat SSL connector or reverse proxy)

---

## FAQ

**Language?**  
UI/templates ship largely in German; adjust templates and `config.json` as needed.

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
