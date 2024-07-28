package net.midiandmore.chat;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
import static java.lang.System.currentTimeMillis;

/**
 * Benutzerdaten werden in Dieser Klasse abgeglegt
 *
 * @author Andreas Pschorn
 */
public final class Users {

    /**
     * F&uuml;gt neue Chatterdaten hinzu
     *
     * @param master
     * @param name Der Chatter-Nick-Name
     * @param connectionId Die aktuelle Sessionid
     * @param room Der Raum wo sich der Chatter befindet
     * @param referer Referer vom Browser falls vorhanden
     * @param userAgent Der User-Agent des Browsers
     * @param host Der Hostname
     * @param ip Die IP
     * @param color Die Chatterfarbe
     * @param status Der Chatterstatus 0-10
     * @param session
     * @param httpSession
     * @param loginTime Die Loginuhrzeit
     * @param sv Ist der Chatter ein Supervisor?
     * @param serverHost
     * @param skin
     * @param realIp
     * @param realHost
     */
    public Users(Bootstrap master, String name, String connectionId, String room, String referer,
            String userAgent, String host, String ip, String color, int status, Session session, HttpSession httpSession, long loginTime, boolean sv, String serverHost, String skin, String realIp, String realHost, String osName, String osVersion, String browser) {
        setMaster(master);
        setLoginTime(loginTime);
        setIdleTime(currentTimeMillis());
        setName(name);
        setRoom(room);
        setConnectionId(connectionId);
        setColor(color);
        setStatus(status);
        setUserAgent(userAgent);
        setReferer((referer.contains("?")) ? referer.substring(0, referer.indexOf("?")) : referer);
        setHost(host);
        setIp(ip);
        setSupervisor(sv);
        setIPv6(ip.contains(":"));
        setServerHost(serverHost);
        setRefreshOpera(false);
        setRepeatCount(0);
        setRepeatLine("");
        setFloodCount(0);
        setFloodTime(currentTimeMillis() / 1000);
        setFlood(false);
        setSkin(skin);
        setRealIp(realIp);
        setRealHost(realHost);
        setSession(session);
        setTimer(0);
        setTimeoutTimer(0);
        setHttpSession(httpSession);
        setOsName(osName);
        setOsVersion(osVersion);
        setBrowser(browser);
        setQuitted(false);
    }

    private boolean quitted = false;
    private String browser;
    private String osName;
    private String osVersion;
    private String name;
    private String newName;
    //private boolean visible = true;
    private boolean away = false;
    private String awayReason;
    private String userAgent;
    private long loginTime;
    private boolean supervisor = false;
    private long idleTime;
    private int timer;
    private int timeoutTimer;
    private boolean registered = false;
    private boolean chatOnly = false;
    private String referer;
    private String room;
    private String inviteRoom;
    private String color;
    private String lastWhisperedNick;
    private boolean quit = false;
    private String quitReason;
    private String kickReason;
    private String banReason;
    private boolean waitingForRefresh = false;
    private boolean gagged = false;
    private ArrayList<String> ignore = new ArrayList<>();
    private ArrayList<String> friends = new ArrayList<>();
    private String host;
    private String ip;
    private boolean kicked = false;
    private boolean banned = false;
    private boolean timedBanned = false;
    private boolean refreshError = false;
    private boolean useJSON = false;
    private boolean JSONfirstLine = true;
    private Session session = null;
    private HttpSession httpSession = null;
    private String serverInfo = null;
    private String jsid = null;
    /**
     *
     * @return
     */
    public boolean isRefreshError() {
        return refreshError;
    }

    /**
     *
     * @param refreshError
     */
    public void setRefreshError(boolean refreshError) {
        this.refreshError = refreshError;
    }

    private String timedBanReason = null;

    /**
     *
     * @return
     */
    protected String getTimedBanReason() {
        return timedBanReason;
    }

    /**
     *
     * @param timedBanReason
     */
    protected void setTimedBanReason(String timedBanReason) {
        this.timedBanReason = timedBanReason;
    }

    /**
     *
     * @return
     */
    protected boolean isTimedBanned() {
        return timedBanned;
    }

    /**
     *
     * @param timedBanned
     */
    protected void setTimedBanned(boolean timedBanned) {
        this.timedBanned = timedBanned;
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
    private String realIp = null;
    private String realHost = null;
    private Bootstrap master = null;
    private Timer refreshTimer = null;

    /**
     * Ruft den Refresh-Timer ab
     *
     * @return Der Refresh-Timer
     */
    public Timer getRefreshTimer() {
        return refreshTimer;
    }

    /**
     * Setzt den Refresh-Timer
     *
     * @param refreshTimer Der Refresh-Timer
     */
    public void setRefreshTimer(Timer refreshTimer) {
        this.refreshTimer = refreshTimer;
    }

    /**
     * Der echte Host bei Proxys
     *
     * @return Der echte Host
     */
    public String getRealHost() {
        return realHost;
    }

    /**
     * Setzte den echten Host, wenn man einen Proxy benutzt
     *
     * @param realHost Der echte Host
     */
    public void setRealHost(String realHost) {
        this.realHost = realHost;
    }

    /**
     * Benutzt der Chatter IPv6?
     *
     * @return Benutzt der Chatter IPv6?
     */
    public boolean isIpv6() {
        return ipv6;
    }

    /**
     * Setzt den Flag ob der Chatter IPv6 benutzt
     *
     * @param ipv6 Der IPv6-Flag
     */
    public void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }

    /**
     * Die echte IP bei Proxys
     *
     * @return Der echte IP
     */
    public String getRealIp() {
        return realIp;
    }

    /**
     * Setzt die echte IP bei Proxys
     *
     * @param realIp Die echte IP
     */
    public void setRealIp(String realIp) {
        this.realIp = realIp;
    }

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

    /**
     * Setzt je nach einstellung Wahr oder Falsch ob die prim&auml;re oder
     * sekund&auml;re Refreschseite (Verwendet als Bugfix f&uuml;r &auml;ltere
     * Operaversionen mit Presto- Engine)
     *
     * @param refreshOpera
     */
    public void setRefreshOpera(boolean refreshOpera) {
        this.refreshOpera = refreshOpera;
    }

    /**
     * Fr&auml;gt ab ob Die prim&auml;re oder die sekund&auml;re Refreschseite
     * ben&ouml;tigt wird!
     *
     * @return
     */
    public boolean isRefreshOpera() {
        return refreshOpera;
    }

    /*
     * DOPPELT?
     * Gibt zur&uuml;ck ob der Chatter eine IPv6-Verbindung hat 
     *
    protected boolean isIpv6() {
        return ipv6;
    }

     * Setzt den IPv6-Status
     * @param ipv6 Der IPv6-Status
    private void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }
 
     */
    /**
     * Liefert den aktuellen Server-Host aus
     *
     * @return
     */
    protected String getServerHost() {
        return serverHost;
    }

    /**
     * Setzt den aktuellen Server-Host
     *
     * @param serverHost Der Server-Host
     */
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }
    private boolean relogin = false;
    private String serverHost;

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
     * Verwendet der Chatter IPv6?
     *
     * @return
     */
    protected boolean isIPv6() {
        return this.ipv6;
    }

    /**
     * Setzt den IPv6-Status
     *
     * @param ipv6 Der IPv6-Status
     */
    protected void setIPv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }

    /**
     * Wurde der Chatter aus dem Chat gekickt?
     *
     * @return
     */
    protected boolean isKicked() {
        return this.kicked;
    }

    /**
     * Setzt den aktuellen Kickstatus
     *
     * @param kicked Wurde der Chatter gekickt?
     */
    protected void setKicked(boolean kicked) {
        this.kicked = kicked;
    }

    /**
     * Wurde der Chatter gebannt?
     *
     * @return
     */
    protected boolean isBanned() {
        return this.banned;
    }

    /**
     * Setzt den aktuellen Bannstatus
     *
     * @param banned Wurde der Chatter gebannt?
     */
    protected void setBanned(boolean banned) {
        this.banned = banned;
    }

    /*
     * F&uuml;r sp&auml;tere Benutzung geplant
     * 
     * Ist der Chatter sichtbar?
    protected boolean isVisible() {
        return visible;
    }

     * Setzt den Sichbarkeitsstatus
     * @param visible Der Sichtbarkeitsstatus
    protected void setVisible(boolean visible) {
        this.visible = visible;
    }
     */
    /**
     * Ist der Chatter abgemeldet?
     *
     * @return
     */
    protected boolean isAway() {
        return away;
    }

    /**
     * Setzt den Abmeldestatus
     *
     * @param away Abmeldestatus
     */
    protected void setAway(boolean away) {
        this.away = away;
    }

    /**
     * Ruft den Grund der Abmeldung ab
     *
     * @return
     */
    protected String getAwayReason() {
        return awayReason;
    }

    /**
     * Setzt den Abmeldegrund
     *
     * @param awayReason Der Abmeldegrund
     */
    protected void setAwayReason(String awayReason) {
        this.awayReason = awayReason;
    }

    /**
     * Ruft den User-Agent ab
     *
     * @return
     */
    protected String getUserAgent() {
        return userAgent;
    }

    /**
     * Setzt den User-Agent
     *
     * @param userAgent Der User-Agent
     */
    protected void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
     * Ist der Chatter ein Supervisor?
     *
     * @return
     */
    protected boolean isSupervisor() {
        return supervisor;
    }

    /**
     * Setzt den Supervisor-Status
     *
     * @param supervisor Der neue Supervisor-Status
     */
    protected void setSupervisor(boolean supervisor) {
        this.supervisor = supervisor;
    }

    /**
     * Ist der Chatter ein Superuser?
     *
     * @return
     */
    protected boolean isSuperuser() {
        return getStatus() >= 3;
    }

    /**
     * Hat der Chatter Voice?
     *
     * @return
     */
    protected boolean isVoice() {
        return getStatus() >= 2;
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
     * Ist der Chatter registriert?
     *
     * @return
     */
    protected boolean isRegistered() {
        return registered;
    }

    /**
     * Setzt de Registrierungsstatus
     *
     * @param registered Der Registrierungsstatus
     */
    protected void setRegistered(boolean registered) {
        this.registered = registered;
    }

    /**
     * Ruft den aktuellen Referer ab
     *
     * @return
     */
    protected String getReferer() {
        return referer;
    }

    /**
     * Setzt den neuen Referer
     *
     * @param referer Der Referer
     */
    protected void setReferer(String referer) {
        this.referer = referer;
    }

    /**
     * Ruft den Raum wohin der Chatter einegeladen wurde ab!
     *
     * @return
     */
    protected String getInviteRoom() {
        return inviteRoom;
    }

    /**
     * Setzt den Raum wohin der Chatter eingeladen wurde!
     *
     * @param inviteRoom Der Zielraum
     */
    protected void setInviteRoom(String inviteRoom) {
        this.inviteRoom = inviteRoom;
    }

    /**
     * Ruft den aktuellen Raum wo der Chatter ist ab!
     *
     * @return
     */
    protected String getRoom() {
        return room;
    }

    /**
     * Setzt den neuen Raum wo der Chatter jetzt ist!
     *
     * @param room Der Zielraum
     */
    protected void setRoom(String room) {
        this.room = room;
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
     * Ruft den zuletzt angefl&uuml;sterten Nick ab
     *
     * @return
     */
    protected String getLastWhisperedNick() {
        return lastWhisperedNick;
    }

    /**
     * Setzt den aktuellen Nick als zuletzt angefl&uuml;stert
     *
     * @param lastWhisperedNick Der Zielnick
     */
    protected void setLastWhisperedNick(String lastWhisperedNick) {
        this.lastWhisperedNick = lastWhisperedNick;
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

    /**
     * Der Kickgrund
     *
     * @return
     */
    protected String getKickReason() {
        return kickReason;
    }

    /**
     * Setzt den Kickgrund
     *
     * @param kickReason Der Kickgrund
     */
    protected void setKickReason(String kickReason) {
        this.kickReason = kickReason;
    }

    /**
     * Der Banngrund
     *
     * @return
     */
    protected String getBanReason() {
        return banReason;
    }

    /**
     * Setzt den Banngrund
     *
     * @param banReason Der Banngrund
     */
    protected void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    /**
     * Ruft den Logoutgrund ab
     *
     * @return
     */
    protected String getQuitReason() {
        return quitReason;
    }

    /**
     * Setzt den Logoutgrund
     *
     * @param quitReason Der Logoutgrund
     */
    protected void setQuitReason(String quitReason) {
        this.quitReason = quitReason;
    }

    /**
     * Wartet der Chatter auf einen Chatstreamrefresh
     *
     * @return
     */
    protected boolean isWaitingForRefresh() {
        return waitingForRefresh;
    }

    /**
     * Setzt den Chatstreamrefreshstatus
     *
     * @param waitingForRefresh der Chatstreamrefrehstatus
     */
    protected void setWaitingForRefresh(boolean waitingForRefresh) {
        this.waitingForRefresh = waitingForRefresh;
    }

    /**
     * Ist der Chatter geknebelt?
     *
     * @return
     */
    protected boolean isGagged() {
        return gagged;
    }

    /**
     * Ent-/knebelt den Chatter
     *
     * @param gagged Der Knebelstatus
     */
    protected void setGagged(boolean gagged) {
        this.gagged = gagged;
    }

    /**
     * Ruft die Liste der Ignorierten ab!
     *
     * @return
     */
    protected ArrayList<String> getIgnore() {
        return ignore;
    }

    /**
     * Setzt eine neue Ignorierliste
     *
     * @param ignore Die Ignorierliste
     */
    protected void setIgnore(ArrayList<String> ignore) {
        this.ignore = ignore;
    }

    /**
     * Die Freundesliste
     *
     * @return
     */
    protected ArrayList<String> getFriends() {
        return friends;
    }

    /**
     * Setzt eine neue Freundesliste
     *
     * @param friends Die neue Freundesliste
     */
    protected void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }

    /**
     * Ruft den Chatterhostname ab
     *
     * @return
     */
    protected String getHost() {
        return host;
    }

    /**
     * Setzt den Chatterhostnamen
     *
     * @param host Der Chatterhostname
     */
    protected void setHost(String host) {
        this.host = host;
    }

    /**
     * Ruft die Chatterip ab
     *
     * @return
     */
    protected String getIp() {
        return ip;
    }

    /**
     * Setzt die Chatterip
     *
     * @param ip Die Chatterip
     */
    protected void setIp(String ip) {
        this.ip = ip;
    }

    /*
     * Wird gescrollt?
    protected boolean isScrolling() {
        return scrolling;
    }

     * Setzt den Scrollstatus
     * @param scrollign Der Scrollstatus
    protected void setScrolling(boolean scrolling) {
        this.scrolling = scrolling;
    }
     */
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
    private int status;

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
     * Der aktuelle Chaterstatus (0-11)
     *
     * @return
     */
    protected int getStatus() {
        return status;
    }

    /**
     * Setzt den Chatterstatus (0-11)
     *
     * @param status Der Chatterstatus
     */
    protected void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the master
     */
    public Bootstrap getMaster() {
        return master;
    }

    /**
     * @param master the master to set
     */
    public void setMaster(Bootstrap master) {
        this.master = master;
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
     * @return the httpSession
     */
    protected HttpSession getHttpSession() {
        return httpSession;
    }

    /**
     * @param httpSession the httpSession to set
     */
    protected void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
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
     * @return the osName
     */
    protected String getOsName() {
        return osName;
    }

    /**
     * @param osName the osName to set
     */
    protected void setOsName(String osName) {
        this.osName = osName;
    }

    /**
     * @return the osVersion
     */
    protected String getOsVersion() {
        return osVersion;
    }

    /**
     * @param osVersion the osVersion to set
     */
    protected void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    /**
     * @return the browser
     */
    protected String getBrowser() {
        return browser;
    }

    /**
     * @param browser the browser to set
     */
    protected void setBrowser(String browser) {
        this.browser = browser;
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
     * @return the jsid
     */
    public String getJsid() {
        return jsid;
    }

    /**
     * @param jsid the jsid to set
     */
    public void setJsid(String jsid) {
        this.jsid = jsid;
    }

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
     * @return the chatOnly
     */
    public boolean isChatOnly() {
        return chatOnly;
    }

    /**
     * @param chatOnly the chatOnly to set
     */
    public void setChatOnly(boolean chatOnly) {
        this.chatOnly = chatOnly;
    }
}
