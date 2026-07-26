package net.midiandmore.chat;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.err;
import static java.lang.System.getProperty;
import static java.lang.System.out;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.Timer;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static net.midiandmore.chat.ErrorLog.LOG;

/**
 * Die Start-Klasse des Chats
 *
 * @author Andreas Pschorn
 */
public final class Bootstrap implements Software {

    private ChatLog chatLog;
    private ErrorLog errorLog;
    private boolean setupNeeded;

    /**
     * Ruft den Fehler-Log ab
     *
     * @return Der Fehler-Log
     */
    public ErrorLog getErrorLog() {
        return errorLog;
    }

    /**
     * Setzt den Fehler-Log
     *
     * @param errorLog Der Fehler-Log
     */
    public void setErrorLog(ErrorLog errorLog) {
        this.errorLog = errorLog;
    }

    /**
     * Ruft den Chat-Log ab
     *
     * @return Der Chat-Log
     */
    public ChatLog getChatLog() {
        return chatLog;
    }

    /**
     * Setzt den Chat-Log
     *
     * @param chatLog Der Chat-Log
     */
    public void setChatLog(ChatLog chatLog) {
        this.chatLog = chatLog;
    }

    /**
     *
     */
    protected static Bootstrap boot;
    private Captcha captcha;
    private ChatManager chatManager;
    private ChatServices chatServices;
    private Commands commands;
    private Config config;
    private long startTime;
    private ThreadPool threadPool;
    private Util util;
    private Timer dayChange;
    private SendMail sendMail;
    private Timer pingPong;
    private Timer statsTimer;

    protected SendMail getSendMail() {
        return sendMail;
    }

    /**
     *
     * @param sendMail
     */
    protected void setSendMail(SendMail sendMail) {
        this.sendMail = sendMail;
    }

    /**
     * Der Tageswechsel-Timer
     *
     * @return Der Tageswechsel-Timer
     */
    public Timer getDayChange() {
        return dayChange;
    }

    /**
     * Setzt den Tageswechsel-Timer
     *
     * @param dayChange Der Tageswechsel-Timer
     */
    public void setDayChange(Timer dayChange) {
        this.dayChange = dayChange;
    }

    /**
     * Der Bootstrap
     */
    public Bootstrap() {
        boot = this;
    }

    /**
     * Initialsiert den Chat
     *
     * @throws Exception Die Exception
     */
    protected void init() throws Exception {
        setStartTime(currentTimeMillis());
        out.printf("%s %s-%s\r\n", SERVER_SOFTWARE, SERVER_VERSION, SERVER_STATUS);
        out.printf("Project: %s\r\n", SERVER_PROJECT_NAME);
        out.printf("(c) %s by %s\r\n", SERVER_YEAR, SERVER_VENDOR);
        out.printf("All rights reserved.\r\n\r\n");
        out.printf("Running on: %s (%s)\r\n", getProperty("os.name"), getProperty("os.arch"));
        var homeWebCom = getProperty("user.home") + getProperty("file.separator") + ".homewebcom";
        var configFile = homeWebCom + getProperty("file.separator") + "config" + getProperty("file.separator") + "config.json";
        var configDir = new java.io.File(homeWebCom + getProperty("file.separator") + "config");
        if (!configDir.exists() || !new java.io.File(configFile).exists()) {
            out.printf("* First start detected - setting up .homewebcom directory...\r\n");
            setupNeeded = true;
            var baseDir = new java.io.File(homeWebCom);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            var dataDir = new java.io.File(homeWebCom + getProperty("file.separator") + "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            var htdocsDir = new java.io.File(homeWebCom + getProperty("file.separator") + "htdocs");
            if (!htdocsDir.exists()) {
                htdocsDir.mkdirs();
            }
            var picturesDir = new java.io.File(homeWebCom + getProperty("file.separator") + "pictures");
            if (!picturesDir.exists()) {
                picturesDir.mkdirs();
            }
            var templatesDir = new java.io.File(homeWebCom + getProperty("file.separator") + "templates");
            if (!templatesDir.exists()) {
                templatesDir.mkdirs();
            }
            copyDefaultContent(homeWebCom);
            out.printf("* First start detected - setup required.\r\n");
            out.printf("* Setup directories created. Please visit /Setup to complete configuration.\r\n");
            setChatServices(new ChatServices(this));
            setCaptcha(new Captcha(this));
            return;
        }
        setConfig(new Config(this));
        setThreadPool(new ThreadPool(getConfig().getInt("pool_max")));
        out.printf("* Starting chat: ");
        setChatManager(new ChatManager(this));
        setUtil(new Util(this));
        setCommands(new Commands(this));
        setChatServices(new ChatServices(this));
        getConfig().setCurrentDate(getUtil().getCurrentDateReverse());
        getConfig().getDb().delAllSessions();
        setCaptcha(new Captcha(this));
        setErrorLog(new ErrorLog(this));
        getErrorLog().setLog();
        setChatLog(new ChatLog(this));
        getChatLog().setLog();
        setSendMail(new SendMail(this));
        setDayChange(new Timer());
        getDayChange().scheduleAtFixedRate(new DayChangeTimer(this), 1000, 1000);
        setPingPong(new Timer());
        getPingPong().scheduleAtFixedRate(new PingPong(this), 0, 1000);
        setStatsTimer(new Timer());
        getStatsTimer().scheduleAtFixedRate(new StatsTimer(this), 0, 900000);
        out.printf("Done.\r\n");
    }

    private void copyDefaultContent(String homeWebCom) {
        var context = getClass().getClassLoader();
        var fs = getProperty("file.separator");
        var paths = new String[]{"config", "data", "htdocs", "pictures", "templates"};
        for (var path : paths) {
            var resourcePath = "/default-homewebcom/" + path;
            var url = context.getResource(resourcePath);
            if (url == null) {
                continue;
            }
            var targetDir = new java.io.File(homeWebCom + fs + path);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            try {
                var resourceStream = context.getResourceAsStream(resourcePath);
                if (resourceStream == null) {
                    continue;
                }
                var content = new java.util.Scanner(resourceStream, "UTF-8").useDelimiter("\\A").next();
                if (path.equals("config") && content != null && !content.isBlank()) {
                    var writer = new java.io.FileWriter(new java.io.File(targetDir, "config.json"));
                    writer.write(content);
                    writer.close();
                }
            } catch (Exception e) {
                out.printf("* Warning: Could not copy default %s: %s\r\n", path, e.getMessage());
            }
        }
    }

    /**
     * Zeigt einen fatalen Fehler auf und beendet den Chat
     *
     * @param e Exception
     */
    protected static synchronized void fatalError(Exception e) {
        err.printf("\r\n* Guru Meditation: %s\r\n", e.getLocalizedMessage());
        e.printStackTrace(err);
        err.printf("\r\n* Chat halted to prevent damage or security issues...\r\n");
    }

    /**
     * Zeigt einen fatalen Fehler auf und beendet den Chat und Loggt Ihn
     *
     * @param e Exception
     */
    protected static synchronized void logFatalError(Exception e) {
        err.printf("\r\n* Guru Meditation: %s\r\n", e.getLocalizedMessage());
        LOG.log(SEVERE, "Fatal Error:", e);
        LOG.log(SEVERE, "Chat halted to prevent damage or security issues...");
        err.printf("\r\n* Chat halted to prevent damage or security issues...\r\n");
    }

    /**
     * Fehler!
     *
     * @param e Exception
     */
    protected static void logError(Exception e) {
        LOG.log(WARNING, "Error:", e);
    }

    /**
     * Ermittelt die Startzeit
     *
     * @return Startzeit
     */
    protected long getStartTime() {
        return startTime;
    }

    /**
     * Setzt die Startzeit
     *
     * @param time Startzeit
     */
    protected void setStartTime(long time) {
        startTime = time;
    }

    /**
     * ChatServices-Klasse
     *
     * @return ChatServices-Klasse
     */
    protected ChatServices getChatServices() {
        return chatServices;
    }

    /**
     * Setzt die ChatServices-Klasse
     *
     * @param chatServices Die ChatServices-Klasse
     */
    protected void setChatServices(ChatServices chatServices) {
        this.chatServices = chatServices;
    }

    /**
     *
     * @return
     */
    protected Commands getCommands() {
        return commands;
    }

    /**
     *
     * @param commands
     */
    protected void setCommands(Commands commands) {
        this.commands = commands;
    }

    /**
     *
     * @return
     */
    protected Config getConfig() {
        return config;
    }

    /**
     *
     * @param config
     */
    protected void setConfig(Config config) {
        this.config = config;
    }

    /**
     *
     * @return
     */
    protected ThreadPool getThreadPool() {
        return threadPool;
    }

    /**
     *
     * @param threadPool
     */
    protected void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    /**
     *
     * @return
     */
    protected Util getUtil() {
        return util;
    }

    /**
     *
     * @param util
     */
    protected void setUtil(Util util) {
        this.util = util;
    }

    /**
     *
     * @return
     */
    protected Captcha getCaptcha() {
        return captcha;
    }

    /**
     *
     * @param captcha
     */
    protected void setCaptcha(Captcha captcha) {
        this.captcha = captcha;
    }

    /**
     *
     * @return
     */
    protected ChatManager getChatManager() {
        return chatManager;
    }

    /**
     *
     * @param chatManager
     */
    protected void setChatManager(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    /**
     * @return the pingPong
     */
    protected Timer getPingPong() {
        return pingPong;
    }

    protected void setPingPong(Timer pingPong) {
        this.pingPong = pingPong;
    }

    protected Timer getStatsTimer() {
        return statsTimer;
    }

    protected void setStatsTimer(Timer statsTimer) {
        this.statsTimer = statsTimer;
    }

    protected boolean isSetupNeeded() {
        return setupNeeded;
    }

    protected void setSetupNeeded(boolean setupNeeded) {
        this.setupNeeded = setupNeeded;
    }

}
