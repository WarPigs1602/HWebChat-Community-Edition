package net.midiandmore.chat;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.err;
import static java.lang.System.getProperty;
import static java.lang.System.out;
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

    /**
     *
     * @return
     */
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
        out.printf("Done.\r\n");
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

    /**
     * @param pingPong the pingPong to set
     */
    protected void setPingPong(Timer pingPong) {
        this.pingPong = pingPong;
    }

}
