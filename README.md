

> **Note:** This project is considered **legacy** and only receives limited support. For new projects, it is recommended to look for more up-to-date alternatives.

# HWebChat Community Edition

HWebChat is a community-based chat solution for the web.
**Note:** The chat configuration is currently in German but can be customized.

## Features

- Public and private chat rooms
- User registration and authentication
- Emoji and file upload support
- Moderation features (e.g. kick/ban)
- Customizable configuration (language, permissions, etc.)
- WebSocket-based real-time chat
- Database-backed storage (MySQL/MariaDB)

## Example Configuration

In the `config` subdirectory of `.homewebcom`, you will find a file `config.json` with a structure like this:

```json
[
   {"name": "admin_password", "value": "passwort", "description": "The password for the admin console"},
   {"name": "sql.host", "value": "localhost:3306", "description": "The MySQL host"},
   {"name": "sql.user", "value": "user", "description": "The MySQL user"},
   {"name": "sql.pw", "value": "password", "description": "The MySQL password"},
   {"name": "sql.db", "value": "hwebchat", "description": "The MySQL database name"},
   {"name": "language", "value": "en", "description": "The language setting (e.g. 'en' or 'de')"},
   {"name": "max_nick_length", "value": "20", "description": "Maximum nickname length"}
   // ... more settings ...
]
```

Each entry is an object with `name`, `value`, and `description`. You can edit or add settings as needed. See the provided `config.json` for all available options.

## Security Notes

- Use strong passwords for database and admin accounts.
- Make sure your Tomcat server is not publicly accessible unless intended.
- Keep your Java and Tomcat versions up to date.
- Regularly check permissions of the `.homewebcom/config` directory.

## FAQ

**Q:** Can I change the language to German?
A: Yes, set the `language` option in the configuration.

**Q:** How can I add more emojis?
A: Add new emoji fonts to the `web/fonts/` directory and adjust the configuration if needed.

**Q:** How do I enable HTTPS for the chat?
A: Configure Tomcat for SSL (see Tomcat documentation).

## Contact & Community

- Codeberg Issues: https://github.com/WarPigs1602/HWebChat-Community-Edition/issues
- Pull requests welcome!
- For larger contributions or support: see Issues or the project page on Codeberg.

## Screenshots & Links

![Screenshot chat interface](https://github.com/WarPigs1602/HWebChat-Community-Edition/raw/branch/main/screenshots/chat.png)

More info and latest versions: https://github.com/WarPigs1602/HWebChat-Community-Edition


## Requirements

- **Apache Tomcat** is required to run the application.
- A **.homewebcom** directory must exist in your user home directory.
- **Netbeans IDE** is recommended for compiling (in the `src` directory).
- **MySQL** or **MariaDB** for the database.

## Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/WarPigs1602/HWebChat-Community-Edition.git
   ```
2. Create the `.homewebcom` directory in your user home directory (if it does not exist).
3. Open the project with Netbeans and compile it in the `src` directory to a **.war file**.
4. Import the `database.sql` file into your MySQL or MariaDB database:
   ```bash
   mysql -u <user> -p <databasename> < path/to/database.sql
   ```
5. Adjust the configuration in the `config` subdirectory of the `.homewebcom` directory as needed (default: German).
6. Deploy the generated **.war file** to your **Tomcat** server (`webapps` directory).

## Configuration

- The configuration files are located in the `config` subdirectory inside the `.homewebcom` directory (i.e. `~/.homewebcom/config`).
- Language, credentials, and other settings can be adjusted there.

## Usage

After successful installation and configuration, the chat can be accessed in your browser via your Tomcat server
(e.g. at `http://localhost:8080/HWebChat-Community-Edition`).

## Support

For questions or community contributions:
> Please submit Codeberg Issues or Pull Requests!

---

**Enjoy HWebChat!**
