package net.midiandmore.chat;

import java.io.OutputStream;
import jakarta.websocket.Session;
import static java.lang.System.currentTimeMillis;

/**
 * Benutzerdaten werden in Dieser Klasse abgeglegt
 *
 * @author Andreas Pschorn
 */
public final class UsersPrivchat {

    /**
     * @return the newName
     */
    public String getNewName() {
        return newName;
    }

    /**
     * @param newName the newName to set
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }

    /**
     * @return the newTarget
     */
    public String getNewTarget() {
        return newTarget;
    }

    /**
     * @param newTarget the newTarget to set
     */
    public void setNewTarget(String newTarget) {
        this.newTarget = newTarget;
    }

    /**
     * F&uuml;gt neue Chatterdaten hinzu
     *
     * @param master
     * @param name Der Chatter-Nick-Name
     * @param connectionId Die aktuelle Sessionid
     * @param color Die Chatterfarbe
     * @param session
     * @param httpSession
     * @param skin
     */
    public UsersPrivchat(String name, String connectionId, String color, Session session, String skin, String target) {
        setIdleTime(currentTimeMillis());
        setName(name);
        setConnectionId(connectionId);
        setColor(color);
        setSkin(skin);
        setSession(session);
        setTimer(0);
        setTimeoutTimer(0);
        setQuitted(false);
        setTarget(target);     
        setRepeatLine("");
    }

    private boolean quitted = false;
    private String name;
    private String newName;
    private String newTarget;
    private String target;    
    //private boolean visible = true;
    private long loginTime;
    private long idleTime;
    private int timer;
    private int timeoutTimer;
    private String color;
    private boolean quit = false;
    private boolean useJSON = false;
    private boolean JSONfirstLine = true;
    private Session session = null;
    private String serverInfo = null;

    private String quitReason = null;

    /**
     *
     * @return
     */
    protected String getQuitReason() {
        return quitReason;
    }

    /**
     *
     * @param quitReason
     */
    protected void setQuitReason(String quitReason) {
        this.quitReason = quitReason;
    }

    private boolean timeout = false;
    private final boolean scrolling = true;
    private boolean ipv6 = false;
    private boolean refreshOpera = false;
    private boolean flood = false;
    private OutputStream gzos = null;

    /**
     *
     * @return
     */
    public OutputStream getGzos() {
        return gzos;
    }

    /**
     *
     * @param gzos
     */
    public void setGzos(OutputStream gzos) {
        this.gzos = gzos;
    }

    private String skin = null;

    /**
     * Setzt den Skin des Chatstreams
     *
     * @param skin Der Skin
     */
    protected void setSkin(String skin) {
        this.skin = skin;
    }

    /**
     * Ruft den Namen des aktuellen Skins ab
     *
     * @return
     */
    protected String getSkin() {
        return skin;
    }

    /**
     * Sestzt den Floodstatus
     *
     * @param flood Der Floodstatus
     */
    public void setFlood(boolean flood) {
        this.flood = flood;
    }

    /**
     * Fr&auml;gt den aktuellen Floodstatus ab
     *
     * @return
     */
    public boolean getFlood() {
        return flood;
    }

    private boolean relogin = false;

    /**
     * Der Chatternickname
     *
     * @return
     */
    protected String getName() {
        return name;
    }

    /**
     * Setzt den aktuellen Chatternicknamen
     *
     * @param name Der Nick-Name
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Ruft die Loginzeit ab
     *
     * @return
     */
    protected long getLoginTime() {
        return loginTime;
    }

    /**
     * Setzt die Loginzeit
     *
     * @param loginTime Die neue Loginzeit
     */
    protected void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    /**
     * Ruft die aktuelle Chatterspezifische-Idle-Uhrzeit ab
     *
     * @return
     */
    protected long getIdleTime() {
        return idleTime;
    }

    /**
     * Setzt die neue Ruheuhrzeit Chatterspezifische-Idle-Uhrzeit
     *
     * @param idleTime Die neue Chatterspezifische-Idle-Uhrzeit
     */
    protected void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    /**
     * Ruft die aktuelle Farbe im Format RRGGBB ab!
     *
     * @return
     */
    protected String getColor() {
        return color;
    }

    /**
     * Setzt eine neue Chatterfarbe
     *
     * @param color Die neue Farbe
     */
    protected void setColor(String color) {
        this.color = color;
    }

    /**
     * Hat der Chatter den Chat verlassen?
     *
     * @return
     */
    protected boolean isQuit() {
        return quit;
    }

    /**
     * Setzt den Chatter als ausgeloggt falls n&ouml;tig
     *
     * @param quit Ist der Chatter ausgeloggt?
     */
    protected void setQuit(boolean quit) {
        this.quit = quit;
    }

    /**
     * Hat der Chatter einen Timeout erhalten?
     *
     * @return
     */
    protected boolean isTimeout() {
        return timeout;
    }

    /**
     * Setzt den Timetoustatus
     *
     * @param timeout Der Timeoutstatus
     */
    protected void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }

    /**
     * Loggt der Chatter sich neu ein?
     *
     * @return
     */
    protected boolean isRelogin() {
        return relogin;
    }

    /**
     * Setzt den Reloginstatus
     *
     * @param relogin Reloginstatus (true falls Relogin)
     */
    protected void setRelogin(boolean relogin) {
        this.relogin = relogin;
    }

    private int repeatCount = 0;
    private String repeatLine;
    private int floodCount = 0;
    private long floodTime;

    /**
     * Die Anzahl der Wiederholungen!
     *
     * @return
     */
    protected int getRepeatCount() {
        return repeatCount;
    }

    /**
     * Setzt die neue Anzahl der Wiederholungen
     *
     * @param repeatCount Die Anzahl der Wiederholungen
     */
    protected void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    /**
     * Die letzte geschriebene Zeile
     *
     * @return
     */
    protected String getRepeatLine() {
        return repeatLine;
    }

    /**
     * Setzt eine neue Zeile als zuletzt geschrieben
     *
     * @param repeatLine Die letzte Zeile
     */
    protected void setRepeatLine(String repeatLine) {
        this.repeatLine = repeatLine;
    }

    /**
     * Der Floodz&auml;hler
     *
     * @return
     */
    protected int getFloodCount() {
        return floodCount;
    }

    /**
     * Aktualisert den Floodz&auml;hler
     *
     * @param floodCount Neuer Floodz&auml;hler
     */
    protected void setFloodCount(int floodCount) {
        this.floodCount = floodCount;
    }

    /**
     * Die Flood-Zeit
     *
     * @return
     */
    protected long getFloodTime() {
        return floodTime;
    }

    /**
     * Die neue Flood-Zeit
     *
     * @param floodTime Die Flood-Zeit
     */
    protected void setFloodTime(long floodTime) {
        this.floodTime = floodTime;
    }
    //private Vector<String> invited = new Vector<String>();

    ;
    private String connectionId;

    /*
     * &Uuml;berfl&uuml;ssig
    protected Vector<String> getInvited() {
        return invited;
    }

    protected void setInvited(Vector<String> invited) {
        this.invited = invited;
    }
     */
    /**
     * Die Sessionid
     *
     * @return
     */
    protected String getConnectionId() {
        return connectionId;
    }

    /**
     * Setzt die Sessionid
     *
     * @param connectionId Die neue Sessionid
     */
    protected void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * @return the useJSON
     */
    protected boolean isUseJSON() {
        return useJSON;
    }

    /**
     * @param useJSON the useJSON to set
     */
    protected void setUseJSON(boolean useJSON) {
        this.useJSON = useJSON;
    }

    /**
     * @return the JSONfirstLine
     */
    protected boolean isJSONfirstLine() {
        return JSONfirstLine;
    }

    /**
     * @param JSONfirstLine the JSONfirstLine to set
     */
    protected void setJSONfirstLine(boolean JSONfirstLine) {
        this.JSONfirstLine = JSONfirstLine;
    }

    /**
     * @return the session
     */
    protected Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    protected void setSession(Session session) {
        this.session = session;
    }

    /**
     * @return the timer
     */
    protected int getTimer() {
        return timer;
    }

    /**
     * @param timer the timer to set
     */
    protected void setTimer(int timer) {
        this.timer = timer;
    }

    /**
     * @param timeoutTimer the timeoutTimer to set
     */
    protected void setTimeoutTimer(int timeoutTimer) {
        this.timeoutTimer = timeoutTimer;
    }

    /**
     * @return the timeoutTimer
     */
    protected int getTimeoutTimer() {
        return timeoutTimer;
    }

    /**
     * @return the serverInfo
     */
    protected String getServerInfo() {
        return serverInfo;
    }

    /**
     * @param serverInfo the serverInfo to set
     */
    protected void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * @return the quitted
     */
    public synchronized boolean isQuitted() {
        return quitted;
    }

    /**
     * @param quitted the quitted to set
     */
    public synchronized void setQuitted(boolean quitted) {
        this.quitted = quitted;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }
}
