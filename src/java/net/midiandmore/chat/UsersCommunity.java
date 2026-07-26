package net.midiandmore.chat;

import jakarta.servlet.http.HttpSession;
import java.io.OutputStream;
import static java.lang.System.currentTimeMillis;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Benutzerdaten werden in Dieser Klasse abgeglegt
 *
 * @author Andreas Pschorn
 */
public final class UsersCommunity {

    /**
     * F&uuml;gt neue Chatterdaten hinzu
     *
     * @param master
     * @param session
     */
    public UsersCommunity(Bootstrap master, HttpSession session) {
        setMaster(master);
        setLoginTime(currentTimeMillis());
        setName((String) session.getAttribute("nick"));
        setConnectionId(session.getId());
        //setStatus((Integer) session.getAttribute("status"));
        setUserAgent((String) session.getAttribute("user-agent"));
        referer = (String) session.getAttribute("referer");
        setReferer((referer.contains("?")) ? referer.substring(0, referer.indexOf("?")) : referer);
        ip = (String) session.getAttribute("ip");
        setIp(ip);
        setSkin((String) session.getAttribute("skin"));
        setTimeoutTimer(0);
        setOsName((String) session.getAttribute("os-name"));
        setOsVersion((String) session.getAttribute("os-version"));
        setBrowser((String) session.getAttribute("browser"));
        setHttpSession(session);
    }

    private HttpSession httpSession;
    private boolean quitted = false;
    private String browser;
    private String osName;
    private String osVersion;
    private String name;
    private String newName;
    //private boolean visible = true;
    private String userAgent;
    private long loginTime;
    private int timeoutTimer;
    private String referer;
    private String host;
    private String ip;
    private String skin = null;
    private String realIp = null;
    private String realHost = null;
    private Bootstrap master = null;

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

    //private Vector<String> invited = new Vector<String>();

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
     * @return the httpSession
     */
    public HttpSession getHttpSession() {
        return httpSession;
    }

    /**
     * @param httpSession the httpSession to set
     */
    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }
}
