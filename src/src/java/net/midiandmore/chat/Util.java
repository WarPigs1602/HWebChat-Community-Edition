package net.midiandmore.chat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.Session;
import static java.lang.Integer.parseInt;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static java.net.IDN.toASCII;
import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Locale.US;
import static java.util.TimeZone.getDefault;
import static java.util.TimeZone.getTimeZone;
import static java.util.logging.Level.WARNING;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static jakarta.json.Json.createObjectBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Logger;
import static net.midiandmore.chat.ChatLog.LOGGING;
import static net.midiandmore.chat.ChatLog.fh;
import static net.midiandmore.chat.ErrorLog.LOG;
import org.apache.commons.text.StringEscapeUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Allgemeine Werkzeuge f&uuml;r den Chat
 *
 * @author Andreas Pschorn
 */
public final class Util implements Software {

    /**
     *
     * @param chatserver
     */
    public Util(Bootstrap chatserver) {
        setRnd(new Random(currentTimeMillis()));
    }

    /**
     * Pr&uuml;ft nach einem Tageswechsel und aktualisier die Logdatei bei
     * Bedarf
     */
    protected void dayChange() {
        var currDate = getCurrentDateReverse();
        var conf = Bootstrap.boot.getConfig();
        if (!currDate.equals(conf.getCurrentDate())) {
            conf.setCurrentDate(currDate);
            LOGGING.removeHandler(fh);
            fh.close();
            Bootstrap.boot.getChatLog().setLog();
        }
    }

    /**
     * Aktuelle Uhrzeit im GMT-Format f&uuml;r HTTP-Header
     *
     * @return
     */
    protected String getHttpDate() {
        var dt = new Date();
        var df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", US);
        df.setTimeZone(getDefault());
        df.setTimeZone(getTimeZone("GMT"));

        return df.format(dt);
    }

    /**
     * Ermittelt die aktuelle Zeitzone
     *
     * @return
     */
    protected String getCurrentTimeZone() {
        var conf = Bootstrap.boot.getConfig();
        var dt = new Date();
        var df = new SimpleDateFormat("z");
        df.setTimeZone(getTimeZone(conf.getString("time_zone")));
        return df.format(dt);
    }

    /**
     * Ermittelt die aktuelle Zeitzone im US-Format (Ggf. &uuml;berfl&uuml;ssig)
     *
     * @return
     */
    protected String getCurrentUsTimeZone() {
        var conf = Bootstrap.boot.getConfig();
        var dt = new Date();
        var df = new SimpleDateFormat("z", US);
        df.setTimeZone(getTimeZone(conf.getString("time_zone")));
        return df.format(dt);
    }

    /**
     * Ermittelt das aktuelle Datum
     *
     * @return
     */
    protected String getCurrentDateReverse() {
        var conf = Bootstrap.boot.getConfig();
        var dt = new Date();
        var df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(getTimeZone(conf.getString("time_zone")));
        return df.format(dt);
    }

    /**
     * Ermittelt die aktuelle Uhrzeit
     *
     * @return
     */
    protected String getCurrentTime() {
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        var dt = new Date();
        var df = new SimpleDateFormat(db.getCommand("time_format"));
        df.setTimeZone(getTimeZone(conf.getString("time_zone")));
        return df.format(dt);
    }

    /**
     * Ermittelt die Uhrzeit in aktueller Zeitzone
     *
     * @param time Vorgegebene Uhrzeit in Millisekunden
     * @return
     */
    protected String getSimpleTime(long time) {
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        var dt = new Date(time);
        var df = new SimpleDateFormat(db.getCommand("time_format"));
        df.setTimeZone(getTimeZone(conf.getString("time_zone")));
        return df.format(dt);
    }

    /**
     * Ermittelt die Uhrzeit und Datum in aktueller Zeitzone
     *
     * @param time Vorgegebene Uhrzeit in Millisekunden
     * @return
     */
    protected String getTime(long time) {
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        var dt = new Date(time);
        var df = new SimpleDateFormat(db.getCommand("time_format_extended"));
        df.setTimeZone(getTimeZone(conf.getString("time_zone")));
        return df.format(dt);
    }

    /**
     * Berechnet aus zwei Zeiten die aktuelle Differenz und gibt Sie in
     * Sekunden, Minuten, Stunden, Tagen und Wochen wieder aus
     *
     * @param firstTime Erste Zeit in Millisekunden
     * @param secondTime Zweite Zeit in Millisekunden
     * @return
     */
    protected String getCalculatedTime(long firstTime, long secondTime) {
        var output = new StringBuilder();
        var calculatedTime = getTimeAsArray(firstTime, secondTime);
        var weeks = calculatedTime[0];
        var days = calculatedTime[1];
        var hours = calculatedTime[2];
        var minutes = calculatedTime[3];
        var seconds = calculatedTime[4];
        var decimalFormat = new DecimalFormat("00");

        if (weeks > 0) {
            output.append(decimalFormat.format(weeks));
            output.append(" Weeks");
        }

        if ((days > 0) || (weeks > 0)) {
            if (weeks > 0) {
                output.append(" ");
            }

            output.append(decimalFormat.format(days));
            output.append(" Days");
        }

        if ((hours > 0) || (days > 0) || (weeks > 0)) {
            if ((days > 0) || (weeks > 0)) {
                output.append(" ");
            }

            output.append(decimalFormat.format(hours));
            output.append(" Hours");
        }

        if ((minutes > 0) || (hours > 0) || (days > 0) || (weeks > 0)) {
            if ((hours > 0) || (days > 0) || (weeks > 0)) {
                output.append(" ");
            }

            output.append(decimalFormat.format(minutes));
            output.append(" Minutes");
        }

        if ((minutes > 0) || (hours > 0) || (days > 0) || (weeks > 0)) {
            output.append(" ");
        }

        output.append(decimalFormat.format(seconds));
        output.append(" Seconds");

        return output.toString();
    }

    /**
     * Berechnet aus zwei Zeiten die aktuelle Differenz und gibt Sie als
     * long-Array wieder aus
     *
     * @param firstTime Erste Zeit in Millisekunden
     * @param secondTime Zweite Zeit in Millisekunden
     * @return
     */
    protected long[] getTimeAsArray(long firstTime, long secondTime) {
        long[] timeArray = {0, 0, 0, 0, 0};
        var calculatedTime = (firstTime - secondTime) / 1000;

        timeArray[0] = calculatedTime / (7 * 60 * 60 * 24);
        calculatedTime = calculatedTime % (7 * 60 * 60 * 24);
        timeArray[1] = calculatedTime / (60 * 60 * 24);
        calculatedTime = calculatedTime % (60 * 60 * 24);
        timeArray[2] = calculatedTime / (60 * 60);
        calculatedTime = calculatedTime % (60 * 60);
        timeArray[3] = calculatedTime / (60);
        timeArray[4] = calculatedTime % (60);

        return timeArray;
    }

    /**
     * Erzeugt zuf&auml;llig Farben im Hexadezimalen Format
     *
     * @return
     */
    protected String createRandomColor() {
        var conf = Bootstrap.boot.getConfig();
        var sb = new StringBuilder();
        for (var i = 0; i < 6; i++) {
            var col = HEX[round(getRnd().nextFloat() * 15)];

            while (conf.getString("bright_" + Integer.valueOf(i)).indexOf(col) != -1) {
                col = HEX[round(getRnd().nextFloat() * 15)];
            }
            sb.append(col);
        }
        return sb.toString();
    }

    /**
     * Erzeugt eine zuf&auml;llige Sessionid
     *
     * @return
     */
    protected String createRandomId() {
        var sb = new StringBuilder();
        var id = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        for (var i = 0; i < 32; i++) {
            var col = id[round(getRnd().nextFloat() * (id.length - 1))];
            sb.append(col);
        }
        return sb.toString();
    }

    /**
     * Erzeugt eine zuf&auml;lliges Passwort
     *
     * @return
     */
    protected String createRandomPassword() {
        var sb = new StringBuilder();
        var id = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        for (var i = 0; i < 8; i++) {
            var col = id[round(getRnd().nextFloat() * (id.length - 1))];
            sb.append(col);
        }
        return sb.toString();
    }

    /**
     * Erzeugt aus einem StackTrace einen String
     *
     * @param e Die Exception
     * @return
     */
    protected String getStackTrace(Exception e) {
        var sb = new StringBuilder();
        var ste = e.getStackTrace();
        for (var st : ste) {
            var text = String.valueOf(st);

            text = text.replace("\t", "    ");
            text = text.replace(" ", "&nbsp;");
            sb.append(text);
            sb.append("\r\n");
        }

        return sb.toString();
    }

    /**
     * Liefert eine Exception als HTML-formatierten Text aus
     *
     * @param e Die Exception
     * @return
     */
    protected String getHtmlException(Exception e) {
        var sb = new StringBuilder();
        var st = new StringTokenizer(getStackTrace(e), "\r\n");

        while (st.hasMoreTokens()) {
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append(st.nextToken());
            sb.append("<br>");
        }

        return sb.toString();
    }

    /**
     * Entfernt BBCodes
     *
     * @param value Der Text zum parsen
     * @return
     */
    protected String removeBb(String value) {
        value = value.replaceAll("\\[b\\](.*?)\\[\\/b\\]", "$1");
        value = value.replaceAll("\\[i\\](.*?)\\[\\/i\\]", "$1");
        value = value.replaceAll("\\[u\\](.*?)\\[\\/u\\]", "$1");
        value = value.replaceAll("\\[s\\](.*?)\\[\\/s\\]", "$1");
        value = value.replaceAll("\\[center\\](.*?)\\[\\/center\\]", "$1");
        value = value.replaceAll("\\[quote\\](.*?)\\[\\/quote\\]", "$1");
        value = value.replaceAll("\\[code\\](.*?)\\[\\/code\\]", "$1");
        value = value.replaceAll("\\[color=(.*?)\\](.*?)\\[\\/color\\]", "$2");
        value = value.replaceAll("\\[url=(.*?)\\](.*?)\\[\\/url\\]", "$1");
        value = value.replaceAll("\\[url=\\](.*?)\\[\\/url\\]", "$1");
        return value;
    }

    /**
     * Entfernt HTML-Tags
     *
     * @param value Der Text zum parsen
     * @return
     */
    protected String removeHtml(String value) {
        value = value.replaceAll("\\<[^>]*>", "");
        value = value.replace("&lt;", "<");
        value = value.replace("&gt;", ">");
        value = value.replace("&quot;", "\"");
        value = value.replace("&#0037;", "%");
        value = value.replace("&amp;", "&");
        return value;
    }

    /**
     * Parst BBCodes
     *
     * @param value Der Text zum parsen
     * @return
     */
    protected String parseBb(String value) {
        var conf = Bootstrap.boot.getConfig();
        value = value.replace("\n", "<br>\n");
        value = value.replaceAll("\\[b\\](.*?)\\[\\/b\\]", "<b>$1</b>");
        value = value.replaceAll("\\[i\\](.*?)\\[\\/i\\]", "<i>$1</i>");
        value = value.replaceAll("\\[u\\](.*?)\\[\\/u\\]", "<span style=\"text-decoration:underline;\">$1</span>");
        value = value.replaceAll("\\[s\\](.*?)\\[\\/s\\]", "<span style=\"text-decoration:line-trough;\">$1</span>");
        value = value.replaceAll("\\[center\\](.*?)\\[\\/center\\]", "<p style=\"text-align:center;\">$1</p>");
        value = value.replaceAll("\\[quote\\](.*?)\\[\\/quote\\]", "<blockquote>$1</blockquote>");
        value = value.replaceAll("\\[code\\](.*?)\\[\\/code\\]", "<pre>$1</pre>");
        value = value.replaceAll("\\[color=(.*?)\\](.*?)\\[\\/color\\]", "<span style=\"color: #$1\">$2</span>");
        value = value.replaceAll("\\[url=(.*?)\\](.*?)\\[\\/url\\]", "<a href=\"" + conf.getString("path_hwebchat") + "?page=" + conf.getString("path_link") + "&url=$1\" target=\"_blank\">$2</a>");
        value = value.replaceAll("\\[url\\](.*?)\\[\\/url\\]", "<a href=\"" + conf.getString("path_hwebchat") + "?page=" + conf.getString("path_link") + "&url=$1\" target=\"_blank\">$1</a>");
        value = value.replaceAll("\\[(.*?)\\](.*?)\\[\\/(.*?)\\]", "$2");
        return value;
    }

    /**
     * Parst BBCodes
     *
     * @param value Der Text zum parsen
     * @return
     */
    protected String parseBbChat(String value) {
        var conf = Bootstrap.boot.getConfig();
        value = value.replace("\n", "<br>\n");
        value = value.replaceAll("\\[b\\](.*?)\\[\\/b\\]", "<b>$1</b>");
        value = value.replaceAll("\\[i\\](.*?)\\[\\/i\\]", "<i>$1</i>");
        value = value.replaceAll("\\[u\\](.*?)\\[\\/u\\]", "<span style=\"text-decoration:underline;\">$1</span>");
        value = value.replaceAll("\\[s\\](.*?)\\[\\/s\\]", "<span style=\"text-decoration:line-trough;\">$1</span>");
        value = value.replaceAll("\\[color=(.*?)\\](.*?)\\[\\/color\\]", "<span style=\"color: #$1\">$2</span>");
        value = value.replaceAll("\\[url=(.*?)\\](.*?)\\[\\/url\\]", "<a href=\"" + conf.getString("path_hwebchat") + "?page=" + conf.getString("path_link") + "&url=$1\" target=\"_blank\">$2</a>");
        value = value.replaceAll("\\[url\\](.*?)\\[\\/url\\]", "<a href=\"" + conf.getString("path_hwebchat") + "?page=" + conf.getString("path_link") + "&url=$1\" target=\"_blank\">$1</a>");
        value = value.replaceAll("\\[(.*?)\\](.*?)\\[\\/(.*?)\\]", "$2");
        return value;
    }

    /**
     * Schutz vor m&ouml;glichen XSS-Attacken bei Parametern und User-Agent
     *
     * @param value Der Text zum parsen
     * @return
     */
    protected String parseHtml(String value) {
        value = StringEscapeUtils.escapeHtml4(value);
        return value.replace("\\/", "/");
    }

    /**
     * Schutz vor m&ouml;glichen XSS-Attacken bei Parametern und User-Agent
     *
     * @param value Der Text zum parsen
     * @return
     */
    protected String parseHtmlReverse(String value) {
        if (value.contains("&lt;")) {
            value = value.replace("&lt;", "<");
        }
        if (value.contains(">")) {
            value = value.replace("&gt;", ">");
        }
        if (value.contains("&quot;")) {
            value = value.replace("&quot;", "\"");
        }
        if (value.contains("&#0037")) {
            value = value.replace("&#0037;", "%");
        }
        return value;
    }

    protected String preReplace(String value) {
        value = replaceLinks(value);
        value = parseBbChat(value);
        return value;
    }

    /**
     * Schutz vor m&ouml;glichen XSS-Attacken bei Parametern und User-Agent
     *
     * @param value Der Text zum parsen
     * @return
     */
    protected String encodeHtml(String value) {
        var conf = Bootstrap.boot.getConfig();
        try {
            value = URLEncoder.encode(value, conf.getString("charset"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }

    /**
     * Schutz vor m&ouml;glichen XSS-Attacken bei Parametern und User-Agent
     *
     * @param value Der Text zum parsen
     * @return
     */
    protected String decodeHtml(String value) {
        var conf = Bootstrap.boot.getConfig();
        try {
            value = URLDecoder.decode(value, conf.getString("charset"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }

    /**
     * Den Browser aus dem User-Agent ermitteln
     *
     * Andreas Pschorn: Provisorum da ich mit Regex noch nicht zurechtkomme
     *
     * @param userAgent der User-Agent
     * @return
     */
    protected String getBrowser(String userAgent, String browser) {
        var sb = new StringBuilder();
        var version = "unknown";
        String name = null;
        if (!browser.equals("")) {
            browser = browser.replace("&quot;", "\"");
            var br = browser.split("[,;\"]");
            for (var brw : br) {
                var brs = brw.trim();
                if (brs.isEmpty()) {
                    continue;
                }
                if (name != null) {
                    try {
                        if (parseInt(brs) != -1) {
                            version = brs;
                            break;
                        }
                    } catch (NumberFormatException nfe) {

                    }
                } else {
                    if (brs.toLowerCase().contains("edg")) {
                        name = brs;
                    } else if (brs.toLowerCase().contains("chrome")) {
                        name = brs;
                    } else if (brs.toLowerCase().contains("chromium")) {
                        name = brs;
                    } else if (brs.toLowerCase().contains("brave")) {
                        name = brs;
                    } else if (brs.toLowerCase().contains("opera")) {
                        name = brs;
                    } else if (brs.toLowerCase().contains("safari")) {
                        name = brs;
                    }
                }
            }
        }
        if (name == null) {
            var userAgentParser = new UserAgentParser(userAgent);
            var bn = userAgentParser.getBrowserName();
            var bv = userAgentParser.getBrowserVersion();
            bn = bn != null ? bn : "unknown";
            bv = bv != null ? bv : "unknown";
            sb.append(bn);
            sb.append(" ");
            sb.append(bv);
        } else {
            sb.append(name);
            sb.append(" ");
            sb.append(version);

        }
        return sb.toString();
    }

    /**
     * Das Betriebssystem aus dem User-Agent ermitteln
     *
     * Andreas Pschorn: Provisorum da ich mit Regex noch nicht zurechtkomme
     *
     * @param userAgent der User-Agent
     * @param req
     * @return
     */
    protected String getOs(String userAgent, String uaOs, String uaVersion) {
        var userAgentParser = new UserAgentParser(userAgent);
        var sb = new StringBuilder();
        var os = userAgentParser.getBrowserOperatingSystem();
        os = os != null || os.equals("") ? os : "unknown";
        uaOs = uaOs.replace("&quot;", "\"");
        uaOs = uaOs.replaceAll("[\"]", "");
        uaOs = uaOs.trim();
        if (!uaOs.equalsIgnoreCase("unknown")) {
            sb.append(uaOs);
            sb.append(" ");
            var ver = uaVersion;
            sb.append(ver);
            var version = parseInt(!ver.equals("") ? ver.split("[.]")[0] : "-1");
            if (version >= 13) {
                sb.append("11");
            } else if (version > 0) {
                sb.append("10");
            } else {
                sb.append("unknown");
            }
        } else if (os.toLowerCase().contains("nt 5.0")) {
            sb.append("Windows 2000");
        } else if (os.toLowerCase().contains("nt 5.1")) {
            sb.append("Windows XP");
        } else if (os.toLowerCase().contains("nt 5.2")) {
            sb.append("Windows 2003 Server");
        } else if (os.toLowerCase().contains("nt 6.0")) {
            sb.append("Windows Vista");
        } else if (os.toLowerCase().contains("nt 6.1")) {
            sb.append("Windows 7");
        } else if (os.toLowerCase().contains("nt 6.2")) {
            sb.append("Windows 8");
        } else if (os.toLowerCase().contains("nt 6.3")) {
            sb.append("Windows 8.1");
        } else if (os.toLowerCase().contains("nt 10.0")) {
            sb.append("Windows 10");
        } else {
            sb.append(os);
        }
        return sb.toString();
    }

    /**
     * Tauscht Links im Text aus!
     *
     * @param text Der Text zum parsen ;)
     * @return
     */
    protected String replaceLinks(String text) {
        var conf = Bootstrap.boot.getConfig();
        if (text.contains("://")) {
            String[] parsedText = null;
            parsedText = text.split("[ ]");
            var sb = new StringBuilder();
            for (var part : parsedText) {
                if (part.startsWith("http://") || part.startsWith("https://") || part.startsWith("ftp://") || part.startsWith("irc://")) {
                    sb.append("<a href=");
                    sb.append(conf.getString("path_hwebchat"));
                    sb.append("?page=");
                    sb.append(conf.getString("path_link"));
                    sb.append("&url=");
                    sb.append(encodeHtml(parseIdn(part).replace("&amp;", "&")));
                    sb.append(" target=_blank>");
                    sb.append(part);
                    sb.append("</a>");
                } else {
                    sb.append(part);
                }
                sb.append(" ");
            }
            text = sb.toString().trim();
        }
        return text;
    }

    /**
     * Tauscht im Text die Emoticons aus
     *
     * @param text Der Text zum austauschen
     * @return
     */
    protected String replaceSmilies(String text) {
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        var p = db.getSmilies();
        for (var key : p.keySet()) {
            if (!text.toLowerCase().contains(key.toLowerCase())) {
                continue;
            }

            // Andreas Pschorn: Bugfix beim schreien!
            if (!text.contains(key.toUpperCase())) {
                text = text.replace(key, p.get(key));
            } else {
                text = text.replace(key.toUpperCase(), p.get(key));
            }
        }
        text = text.replace("%path_file%", conf.getString("path_file"));
        return text;
    }

    /**
     * Tauscht die Pfade im Text aus im Format %PATH_[pfad]%
     *
     * @param text Der Text
     * @return
     */
    protected String replacePaths(String text) {
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        var p = db.getPaths();
        for (var key : p.keySet()) {
            text = text.replaceAll("%PATH_\\[" + key + "\\]%", replaceNoCookies(p.get(key)));
        }
        text = text.replaceAll("%PATH_\\[(.*?)\\]%", "");
        return text;
    }

    /**
     * Tauscht die Dateipfade aus
     *
     * @param text Der Text zum parsen
     * @return
     */
    protected String replaceFilePaths(String text) {
        var conf = Bootstrap.boot.getConfig();
        text = text.replace("%path_account%", conf.getString("path_account"));
        text = text.replace("%path_account_com%", conf.getString("path_account_com"));
        text = text.replace("%path_message%", conf.getString("path_message"));
        text = text.replace("%path_chat%", conf.getString("path_chat"));
        text = text.replace("%path_profile%", conf.getString("path_profile"));
        text = text.replace("%path_emot%", conf.getString("path_emot"));
        text = text.replace("%path_help%", conf.getString("path_help"));
        text = text.replace("%path_output%", conf.getString("path_output"));
        text = text.replace("%path_start%", conf.getString("path_start"));
        text = text.replace("%path_image%", conf.getString("path_image"));
        text = text.replace("%path_memory%", conf.getString("path_memory"));
        text = text.replace("%path_captcha%", conf.getString("path_captcha"));
        text = text.replace("%path_register%", conf.getString("path_reg_form"));
        text = text.replace("%path_password%", conf.getString("path_password"));
        text = text.replace("%path_login%", conf.getString("path_login"));
        text = text.replace("%path_logout%", conf.getString("path_logout"));
        text = text.replace("%path_link%", conf.getString("path_link"));
        text = text.replace("%path_file%", conf.getString("path_file"));
        text = text.replace("%path_toplist%", conf.getString("path_toplist"));
        text = text.replace("%path_webchat%", conf.getString("path_webchat"));
        text = text.replace("%path_hwebchat%", conf.getString("path_hwebchat"));
        text = text.replace("%path_chat%", conf.getString("path_chat"));
        text = text.replace("%path_login_chat%", conf.getString("path_login_chat"));
        text = text.replace("%path_upload%", conf.getString("path_upload"));
        text = text.replace("%path_console_index%", conf.getString("path_console"));
        text = text.replace("%path_board%", conf.getString("path_board"));
        return text;
    }

    /**
     * Tauscht die Sessionid aus falls in der Chatkonfiguration Cookies
     * deaktivert sind ansonsten verschwindet die An- zeige der Sessionid
     *
     * @param url Die Url zum Parsen
     * @return
     */
    protected String replaceNoCookies(String url) {
        var conf = Bootstrap.boot.getConfig();
        if (url.contains("%sid_nocookie%")) {
            var sb = new StringBuilder();

            if (conf.getString("use_cookies").equals("0")) {
                if (url.contains("?")) {
                    sb.append("&sid=%sid%");
                } else {
                    sb.append("?sid=%sid%");
                }
            }

            url = url.replace("%sid_nocookie%", sb.toString());
        }

        return url;
    }

    /**
     * Ermittelt das Alter aus Tag, Monat und Jahr der Geburt
     *
     * @param day Tag
     * @param month Monat
     * @param year Jahr
     * @return
     */
    protected int getAge(String day, String month, String year) {
        var format = new SimpleDateFormat("d.M.yyyy");
        format.setLenient(false);
        var sb = new StringBuilder();
        sb.append(day);
        sb.append(".");
        sb.append(month);
        sb.append(".");
        sb.append(year);
        Date date = null;
        var age = -1;
        try {
            date = format.parse(sb.toString());
            age = getAge(date);
        } catch (ParseException pe) {
            age = -1;
        }
        return age;
    }

    /**
     * Ermittelt das Alter aus dem Geburtsdatum
     *
     * @param date Geburtsdatum
     */
    private int getAge(Date date) {
        var birthd = new GregorianCalendar();
        birthd.setTime(date);
        birthd.setLenient(false);
        var today = new GregorianCalendar();
        today.setLenient(false);
        var year = today.get(YEAR) - birthd.get(YEAR);
        if (today.get(MONTH) <= birthd.get(MONTH) && today.get(DATE) < birthd.get(DATE)) {
            year -= 1;
        }
        if (year < 0) {
            year = -1;
        }
        return year;
    }

    /**
     * Hat der Chatter Geburtstag?
     *
     * @param dat
     * @return
     */
    protected boolean hasBirthday(String day, String month, String year) {
        var format = new SimpleDateFormat("d.M.yyyy");
        format.setLenient(false);
        var sb = new StringBuilder();
        sb.append(day);
        sb.append(".");
        sb.append(month);
        sb.append(".");
        sb.append(year);
        Date date = null;
        var birthday = false;
        try {
            date = format.parse(sb.toString());
            birthday = hasBirthday(date);
        } catch (ParseException pe) {
        }
        return birthday;
    }

    /**
     * Ermittelt den Geburtstag aus dem Geburtsdatum
     *
     * @param date Geburtsdatum
     */
    private boolean hasBirthday(Date date) {
        var birthday = false;
        var birthd = new GregorianCalendar();
        birthd.setTime(date);
        birthd.setLenient(false);
        var today = new GregorianCalendar();
        today.setLenient(false);
        if (today.get(MONTH) == birthd.get(MONTH) && today.get(DATE) == birthd.get(DATE)) {
            birthday = true;
        }
        return birthday;
    }

    /**
     * Ermittelt die G&uuml;ltigkeit des Datums
     *
     * @param day Tag
     * @param month Monat
     * @param year Jahr
     * @return
     */
    protected boolean isValidDate(String day, String month, String year) {
        var format = new SimpleDateFormat("d.M.yyyy");
        format.setLenient(false);
        var sb = new StringBuilder();
        sb.append(day);
        sb.append(".");
        sb.append(month);
        sb.append(".");
        sb.append(year);
        var valid = false;
        try {
            var date = format.parse(sb.toString());
            valid = true;
        } catch (ParseException e) {
        }
        return valid;
    }

    /**
     *
     * @param master
     */
    protected void setMaster(Bootstrap master) {
        master = master;
    }
    private Random rnd;

    /**
     *
     * @return
     */
    public Random getRnd() {
        return rnd;
    }

    /**
     *
     * @param rnd
     */
    public void setRnd(Random rnd) {
        this.rnd = rnd;
    }

    public static String wildcardToRegex(String wildcard) {
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                // escape special regexp-characters
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return (s.toString());
    }

    /**
     * Pr&uuml;ft Wildcards
     *
     * @param text Der Originaltext zum pr&uuml;fen
     * @param input Der String der &uuml;berpr&uuml;ft
     * @return
     */
    public boolean wildcardMatch(String text, String input) {
        return text.matches(wildcardToRegex(input));
    }

    /**
     * Tausche die Serverinfos im Text aus
     *
     * @param text Der Text zum parsen
     * @return
     */
    protected String replaceServerInfo(String text) {
        text = text.replace("%SERVER_SOFTWARE%", SERVER_SOFTWARE);
        text = text.replace("%SERVER_VERSION%", SERVER_VERSION);
        text = text.replace("%SERVER_STATUS%", SERVER_STATUS);
        text = text.replace("%SERVER_PROJECT_NAME%", SERVER_PROJECT_NAME);
        text = text.replace("%SERVER_VENDOR%", SERVER_VENDOR);
        text = text.replace("%SERVER_HOMEPAGE%", SERVER_HOMEPAGE);
        text = text.replace("%SERVER_YEAR%", SERVER_YEAR);
        text = text.replace("%SERVER_MAIL%", SERVER_MAIL);
        return text;
    }

    /**
     * Liefert den Stream des Servers aus
     *
     * @param data der Text
     * @param out Die Output KLasse
     */
    protected void submitContent(String data, HttpServletResponse out) {
        try {

            var pw = out.getWriter();
            pw.print(data);
            pw.flush();
        } catch (IOException ex) {
        }

    }

    /**
     * Tauscht standardangaben aus f&uuml;r Templates
     *
     * @param text Der Text zum austauschen
     * @param nick Der Nickname
     * @param sid Die Sessionid
     * @param skin Der Raum
     * @param room
     * @param reason Der Grund
     * @param host
     * @return
     */
    protected String replaceDefaultReplacements(String text, String nick, String sid, String skin, String room, String reason, String host) {
        var conf = Bootstrap.boot.getConfig();
        var cm = Bootstrap.boot.getChatManager();
        var db = conf.getDb();
        text = text.replace("%nick%", nick);
        text = text.replace("%sid%", sid);
        text = text.replace("%skin%", skin);
        text = text.replace("%room%", room);
        text = text.replace("%reason%", reason);
        text = text.replace("%host%", host);
        text = text.replace("%ulist%", cm.getUserList(sid, skin));
        text = text.replace("%path_file%", conf.getString("path_file"));
        text = text.replace("%reg_count%", String.valueOf(db.countChatter()));
        text = text.replace("%user_count%", String.valueOf(cm.getUserSizeInChat()));
        text = text.replace("%user_count_community%", String.valueOf(cm.getUsersCommunity().size()));
        return text;
    }

    /**
     * Zeigt die aktuell hinterlegten Nachrichten an :)
     *
     * @param sid
     * @param request
     * @return
     */
    protected String readCookie(String sid, HttpServletRequest request) {
        var conf = Bootstrap.boot.getConfig();
        if (sid == null) {
            sid = "";
        }
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                var name = cookie.getName();
                if (name.equals(conf.getString("session_cookie"))) {
                    sid = cookie.getValue();
                    break;
                }
            }
        }
        return sid;
    }

    /**
     * Parst den Host und ermittelt den Skin
     *
     * @param skin Der vorgegebene Skin
     * @param request
     * @return
     */
    protected String[] parseHost(String skin, HttpServletRequest request) {
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        var host = request.getHeader("host");
        host = host != null ? host : "";
        host = host.endsWith(":" + conf.getString("server_port")) ? host.substring(0, host.lastIndexOf(":" + conf.getString("server_port"))) : host;
        skin = skin != null ? skin : "";
        if (skin.equals("")) {
            skin = host.equals("") ? conf.getString("default_skin") : (db.getHosts().get(host) == null ? conf.getString("default_skin") : db.getHosts().get(host));
        }
        String[] arr = {host, skin};
        return arr;
    }

    /**
     * Parst einen Link in IDN;
     *
     * @param url Die URL
     * @return
     */
    protected String parseIdn(String url) {
        var sb = new StringBuilder();
        var sep = url.indexOf(" ");
        if (sep != -1) {
            url = url.substring(0, sep);
        }
        var prot = url.indexOf("//");
        if (prot != -1) {
            var domain = url.substring(prot + 2);
            var end = domain.indexOf("/");
            if (end != -1) {
                var uri = domain.substring(end);
                domain = domain.substring(0, end);
                sb.append(url.substring(0, prot));
                sb.append("//");
                sb.append(toASCII(domain));
                sb.append(uri);
            } else {
                sb.append(url.substring(0, prot));
                sb.append("//");
                sb.append(toASCII(domain));
            }
        } else {
            sb.append(url);
        }
        return sb.toString();
    }

    /**
     * Ermittelt den content-type
     *
     * @param document Die URI
     * @return
     */
    protected String getContentType(String document) {
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        String suffix = null;
        if (document.contains(".")) {
            suffix = document.substring(document.lastIndexOf(".") + 1, document.length()).toLowerCase();
        } else {
            suffix = "default";
        }
        var type = db.getMimeTypes().get(suffix);
        if (type == null) {
            type = db.getMimeTypes().get("default");
        }
        return type;
    }

    /**
     * Verk&uuml;tzt die URI
     *
     * @param uri Die URI
     * @return
     */
    protected String shortUri(String uri) {
        var conf = Bootstrap.boot.getConfig();
        return uri.substring(conf.getString("path_file").length(), uri.length());
    }

    /**
     * ERmittelt den aktuellen Pfad der Datei
     *
     * @param uri Die URI
     * @return
     */
    protected String getFilePath(String uri) {
        var conf = Bootstrap.boot.getConfig();
        uri = uri.replace("/", conf.getFs());
        uri = uri.replace("\\", conf.getFs());
        var sb = new StringBuilder();
        sb.append(conf.getUh());
        sb.append(conf.getFs());
        sb.append(".homewebcom");
        sb.append(conf.getFs());
        sb.append("htdocs");
        sb.append(conf.getFs());
        sb.append(uri);
        return sb.toString();
    }

    /**
     * Vergleicht Pfade um Sicherheitsprobleme zu vermeiden!
     *
     * @param path
     * @return
     */
    protected boolean compareFilePath(String path) {
        var conf = Bootstrap.boot.getConfig();
        var f = new File(path);
        var sb = new StringBuilder();
        sb.append(conf.getUh());
        sb.append(conf.getFs());
        sb.append(".hwc");
        sb.append(conf.getFs());
        sb.append("htdocs");
        var valid = false;
        try {
            if (f.getCanonicalPath().equals(sb.toString())) {
                valid = true;
            }
            sb.append(conf.getFs());
            if (f.getCanonicalPath().startsWith(sb.toString())) {
                valid = true;
            }
        } catch (IOException ioe) {
            LOG.log(WARNING, "Error:", ioe);
        }
        return valid;
    }

    /**
     * Erzeugt eine Auswahl f&uuml;r das Geschlecht!
     *
     * @param text Der Text
     * @param gender Das Geschlecht
     * @return
     */
    protected String parseGender(String text, String gender) {
        switch (gender) {
            case "m" -> {
                text = text.replace("%selected_no%", "");
                text = text.replace("%selected_male%", " selected");
                text = text.replace("%selected_female%", "");
            }
            case "f" -> {
                text = text.replace("%selected_no%", "");
                text = text.replace("%selected_male%", "");
                text = text.replace("%selected_female%", " selected");
            }
            default -> {
                text = text.replace("%selected_no%", " selected");
                text = text.replace("%selected_male%", "");
                text = text.replace("%selected_female%", "");
            }
        }
        return text;
    }

    /**
     * Erzeugt eine Liste von Hilfeinhalten
     *
     * @param text Der Text
     * @param status Der Status
     * @return
     */
    protected String parseHelp(String text, int status) {
        var conf = Bootstrap.boot.getConfig();
        var db = conf.getDb();
        for (var key : db.getCmd().keySet()) {
            var elem = db.getCmd().get(key);
            int st = Integer.valueOf(elem);
            var value = db.getHelp().get(key);
            if (value != null) {
                text = text.replaceFirst("%HELP_\\[(.*?)\\]%", st <= status ? value : "");
            }
        }
        var sb = new StringBuilder();
        db.getFun().keySet().forEach((key) -> {
            var value = db.getFun().get(key)[1];
            if (value != null) {
                sb.append("<tr><td>/");
                sb.append(key);
                sb.append(" &lt;nick&gt;</td><td>&nbsp;</td><td>");
                sb.append(value);
                sb.append("</td></tr>\r\n");
            }
        });
        text = text.replace("%HELP_[fun]%", sb.toString());
        text = text.replaceAll("%HELP_\\[(.*?)\\]%", "");
        return text;
    }

    /**
     * Erzeugt einen GZIP-komprimierten Inhalt aus einen String
     *
     * @param data Der String
     * @return Der Komprimierte Inhalt
     * @throws IOException
     */
    protected byte[] compressStringToGZIP(String data) throws IOException {
        var conf = Bootstrap.boot.getConfig();
        byte[] compressed;
        try (var bos = new ByteArrayOutputStream(data.length())) {
            try (var gzip = new GZIPOutputStream(bos)) {
                gzip.write(data.getBytes(conf.getString("charset")));
            }
            compressed = bos.toByteArray();
        }
        return compressed;
    }

    /**
     * Erzeugt einen ZIP-komprimierten Inhalt aus einen String
     *
     * @param data Der String
     * @return Der Komprimierte Inhalt
     * @throws IOException
     */
    protected byte[] compressStringToDeflater(String data) throws IOException {
        var conf = Bootstrap.boot.getConfig();
        byte[] compressed;
        try (var bos = new ByteArrayOutputStream(data.length())) {
            try (var zip = new DeflaterOutputStream(bos)) {
                zip.write(data.getBytes(conf.getString("charset")));
            }
            compressed = bos.toByteArray();
        }
        return compressed;
    }

    /**
     *
     * @param text
     * @param session
     * @param category
     * @param target
     */
    protected void sendText(String text, Session session, String category, String target) {
        var br = new BufferedReader(new StringReader(text));

        try {
            String tok = null;
            while ((tok = br.readLine()) != null) {
                if (tok.isEmpty()) {
                    continue;
                }
                session.getBasicRemote().sendText(createObjectBuilder()
                        .add("category", category)
                        .add("target", target)
                        .add("message", tok)
                        .build().toString());
            }
        } catch (IOException ioe) {
        }
    }
}
