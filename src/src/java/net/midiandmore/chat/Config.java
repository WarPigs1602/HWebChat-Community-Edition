package net.midiandmore.chat;

import jakarta.json.Json;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import static java.lang.System.getProperty;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Properties;
import static jakarta.json.Json.createReader;
import jakarta.json.JsonObjectBuilder;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static net.midiandmore.chat.Bootstrap.fatalError;

/**
 * Die Chatkonfiguration
 *
 * @author Andreas Pschorn
 */
public final class Config {

    private Database db;
    private String fs;
    private Bootstrap master;
    private Properties p;
    private String uh;
    private String currentDate;

    /**
     * Damit erh&auml;t man das aktuelle Datum
     *
     * @return Das aktuelle Datum
     */
    public String getCurrentDate() {
        return currentDate;
    }

    /**
     * Damit setzt man das aktuelle Datum
     *
     * @param currentDate Das aktuelle Datum
     */
    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    /**
     *
     * @param chatserver Der Bootstrap
     */
    protected Config(Bootstrap chatserver) {
        setMaster(chatserver);
        hash();
    }

    /**
     * L&auml;dt die Chatkonfiguration
     */
    protected void hash() {
        try {
            out.printf("* Loading Properties: ");
            setFs(getProperty("file.separator"));
            setUh(getProperty("user.home"));
            setP(new Properties());
            out.printf("Done.\r\n* Loading MySQL driver: ");
            setDb(new Database(getMaster()));
            out.printf("Done.\r\n* Loading system configuration ");
            setP(loadDataFromJSONasProperties("config.json", "name", "value"));
            out.printf("Done.\r\n* Loading MySQL configuration: ");
            getDb().loadConfig(getP());
            out.printf("Done.\r\n* Loading permanent bans: ");
            getDb().loadBans(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            out.printf("Done.\r\n* Loading temporary bans: ");
            getDb().setTimedBans(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            out.printf("Done.\r\n* Loading commands ");
            getDb().setCom(loadDataFromJSON("commands.json", "name", "command"));
            out.printf("Done.\r\n* Loading commands configurations ");
            getDb().setCmd(loadDataFromJSON("cmdcfg.json", "name", "status"));
            out.printf("Done.\r\n* Loading smilies ");
            getDb().setSmilies(loadDataFromJSON("emot.json", "name", "emot"));
            out.printf("Done.\r\n* Loading paths ");
            getDb().setPaths(loadDataFromJSON("paths.json", "name", "path"));
            out.printf("Done.\r\n* Loading fun commands ");
            getDb().setFun(loadDataFromJSON("fun.json", "command", "content", "description"));
            out.printf("Done.\r\n* Loading host-list for styles ");
            getDb().setHosts(loadDataFromJSON("hosts.json", "host", "skin"));
            out.printf("Done.\r\n* Loading mime types ");
            getDb().setMimeTypes(loadDataFromJSON("mime-types.json", "suffix", "type"));
            out.printf("Done.\r\n* Loading profile configuration ");
            getDb().setProfile(loadDataFromJSON("profile.json", "title", "text"));
            out.printf("Done.\r\n* Loading help configuration ");
            getDb().setHelp(loadDataFromJSON("help.json", "name", "html"));
            out.printf("Done.\r\n* Loading mail configuration ");
            getDb().setMail(loadDataFromJSONasProperties("mail.json", "name", "value"));
            out.printf("Done.\r\n");
        } catch (NullPointerException npe) {
            fatalError(npe);
        } catch (Exception e) {
            fatalError(e);
        }
    }

    /*
    protected void createConfigFile(Hashtable<String, String> h, String fileName, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(getUh());
        sb.append(getFs());
        sb.append(".hwc");
        sb.append(getFs());
        sb.append("config");
        sb.append(getFs());
        sb.append(fileName);
        String confFile = sb.toString();
        System.out.printf("Create file %s:\r\n", confFile);
        try {
            FileOutputStream xmlStream = new FileOutputStream(confFile);
            Properties tmp = new Properties();            
            tmp.putAll(h);
            //tmp.store(xmlStream, title);
            tmp.storeToXML(xmlStream, title, getString("charset"));
            xmlStream.close();
        } catch (IOException ioe) {
            System.err.printf("Cannot create file %s: %s\r\n", confFile, ioe.getLocalizedMessage());
        }
    }
    
    protected void createConfigFile(Properties p, String fileName, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(getUh());
        sb.append(getFs());
        sb.append(".hwc");
        sb.append(getFs());
        sb.append("config");
        sb.append(getFs());
        sb.append(fileName);
        String confFile = sb.toString();
        System.out.printf("Create file %s:\r\n", confFile);
        try {
            FileOutputStream xmlStream = new FileOutputStream(confFile);
            Properties tmp = new Properties();            
            tmp.putAll(p);
            //tmp.store(xmlStream, title);
            tmp.storeToXML(xmlStream, title, getString("charset"));
            xmlStream.close();
        } catch (IOException ioe) {
            System.err.printf("Cannot create file %s: %s\r\n", confFile, ioe.getLocalizedMessage());
        }
    }    
     */
    /**
     * L&auml;dt eine Konfigurationsdatei aus einer JSON Dateit
     *
     * @param file Die erhaltenen Properties
     * @param obj Die erste Konfiguration
     * @param obj2 Die zweite Konfiguration
     * @return Die Properties der DAtei
     */
    protected Properties loadDataFromJSONasProperties(String file, String obj, String obj2) {
        var ar = new Properties();
        try {
            var conf = getProperty("config.dir");
            if (conf == null) {
                var sb = new StringBuilder();

                sb.append(getUh());
                sb.append(getFs());
                sb.append(".homewebcom");
                sb.append(getFs());
                sb.append("config");
                sb.append(getFs());
                sb.append(file);
                conf = sb.toString();
            }
            out.printf("on path \"%s\" (Element: %s %s): ", conf, obj, obj2);
            InputStream is = new FileInputStream(conf);
            var rdr = createReader(is);
            var results = rdr.readArray();
            var i = 0;
            for (var jsonValue : results) {
                var jobj = results.getJsonObject(i);
                ar.put(jobj.getString(obj), jobj.getString(obj2));
                // Zum schauen
                //System.out.printf("%s %s\n", jobj.getString(obj), jobj.getString(obj2));
                i++;
            }
        } catch (FileNotFoundException fne) {
            fatalError(fne);
        }
        return ar;
    }

    /**
     * L&auml;dt eine Konfigurationsdatei aus einer JSON Dateit
     *
     * @param file Die erhaltenen Properties
     * @param ar
     * @param name
     * @param value
     * @param description
     * @param obj Die erste Konfiguration
     * @param obj2 Die zweite Konfiguration
     * @return Die Properties der DAtei
     */
    protected void saveDataToJSON(String file, TreeMap<String, String[]> ar, String name, String value, String description) {
        FileWriter is = null;
        var conf = getProperty("config.dir");
        if (conf == null) {
            var sb = new StringBuilder();

            sb.append(getUh());
            sb.append(getFs());
            sb.append(".homewebcom");
            sb.append(getFs());
            sb.append("config");
            sb.append(getFs());
            sb.append(file);
            conf = sb.toString();
        }
        try {
            is = new FileWriter(conf);
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        PrintWriter pw = new PrintWriter(is);
        int i = 0;
        pw.println("[");
        for (var jsonValue : ar.keySet()) {
            JsonObjectBuilder obj = Json.createObjectBuilder();
            obj.add(name, jsonValue);
            obj.add(value, ar.get(jsonValue)[0]);
            obj.add(description, ar.get(jsonValue)[1]);
            pw.print(obj.build().toString());
            i++;
            if (ar.size() != i) {
                pw.println(",");
            } else {
                pw.println("");
            }
        }
        pw.println("]");

        try {
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * L&auml;dt eine Konfigurationsdatei aus einer JSON Dateit
     *
     * @param file Die erhaltenen Properties
     * @param obj Die erste Konfiguration
     * @param obj2 Die zweite Konfiguration
     * @return Die Properties der DAtei
     */
    protected void saveDataToJSON(String file, TreeMap<String, String> ar, String name, String value) {
        FileWriter is = null;
        var conf = getProperty("config.dir");
        if (conf == null) {
            var sb = new StringBuilder();

            sb.append(getUh());
            sb.append(getFs());
            sb.append(".homewebcom");
            sb.append(getFs());
            sb.append("config");
            sb.append(getFs());
            sb.append(file);
            conf = sb.toString();
        }
        try {
            is = new FileWriter(conf);
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        PrintWriter pw = new PrintWriter(is);
        int i = 0;
        pw.println("[");
        for (var jsonValue : ar.keySet()) {
            JsonObjectBuilder obj = Json.createObjectBuilder();

            obj.add(name, jsonValue);
            obj.add(value, ar.get(jsonValue));
            pw.print(obj.build().toString());
            i++;
            if (ar.size() != i) {
                pw.println(",");
            } else {
                pw.println("");
            }
        }
        pw.println("]");
        try {
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * L&auml;dt eine Konfigurationsdatei aus einer JSON Dateit
     *
     * @param file Die erhaltenen Properties
     * @param obj Die erste Konfiguration
     * @param obj2 Die zweite Konfiguration
     * @return Die Properties der DAtei
     */
    protected TreeMap<String, String[]> loadDataFromJSON(String file, String obj, String obj2, String obj3) {
        var ar = new TreeMap<String, String[]>();
        try {
            var conf = getProperty("config.dir");
            if (conf == null) {
                var sb = new StringBuilder();

                sb.append(getUh());
                sb.append(getFs());
                sb.append(".homewebcom");
                sb.append(getFs());
                sb.append("config");
                sb.append(getFs());
                sb.append(file);
                conf = sb.toString();
            }
            out.printf("on path \"%s\" (Element: %s %s %s): ", conf, obj, obj2, obj3);
            InputStream is = new FileInputStream(conf);
            var rdr = createReader(is);
            var results = rdr.readArray();
            var i = 0;
            for (var jsonValue : results) {
                var jobj = results.getJsonObject(i);
                String[] obje = {jobj.getString(obj2), jobj.getString(obj3)};
                ar.put(jobj.getString(obj), obje);
                // Zum schauen
                //System.out.printf("%s %s\n", jobj.getString(obj), jobj.getString(obj2));
                i++;
            }
        } catch (FileNotFoundException fne) {
            fatalError(fne);
        }
        return ar;
    }

    /**
     * L&auml;dt eine Konfigurationsdatei aus einer JSON Dateit
     *
     * @param file Die erhaltenen Properties
     * @param obj Die erste Konfiguration
     * @param obj2 Die zweite Konfiguration
     * @return Die Properties der DAtei
     */
    protected TreeMap<String, String> loadDataFromJSON(String file, String obj, String obj2) {
        var ar = new TreeMap<String, String>();
        try {
            var conf = getProperty("config.dir");
            if (conf == null) {
                var sb = new StringBuilder();

                sb.append(getUh());
                sb.append(getFs());
                sb.append(".homewebcom");
                sb.append(getFs());
                sb.append("config");
                sb.append(getFs());
                sb.append(file);
                conf = sb.toString();
            }
            out.printf("on path \"%s\" (Element: %s %s): ", conf, obj, obj2);
            InputStream is = new FileInputStream(conf);
            var rdr = createReader(is);
            var results = rdr.readArray();
            var i = 0;
            for (var jsonValue : results) {
                var jobj = results.getJsonObject(i);
                ar.put(jobj.getString(obj), jobj.getString(obj2));
                // Zum schauen
                //System.out.printf("%s %s\n", jobj.getString(obj), jobj.getString(obj2));
                i++;
            }
        } catch (FileNotFoundException fne) {
            fatalError(fne);
        }
        return ar;
    }

    /**
     * L&auml;dt eine Konfigurationsdatei
     *
     * @param file Die erhaltenen Properties
     * @return Die Properties der DAtei
     */
    protected Properties loadData(String file) {
        var prop = new Properties();
        try {
            var conf = getProperty("config.dir");
            if (conf == null) {
                var sb = new StringBuilder();

                sb.append(getUh());
                sb.append(getFs());
                sb.append(".homewebcom");
                sb.append(getFs());
                sb.append("config");
                sb.append(getFs());
                sb.append(file);
                conf = sb.toString();
            }
            out.printf("on path \"%s\": ", conf);
            prop.loadFromXML(new FileInputStream(conf));
        } catch (FileNotFoundException fne) {
            fatalError(fne);
        } catch (IOException ioe) {
            fatalError(ioe);
        }
        return prop;
    }

    /**
     * Liefert einen Konfigurationsparameter als String aus
     *
     * @param key Der Konfigurations-Key
     * @return Der Konfigurationsparameter als String
     */
    protected String getString(String key) {
        return getP().getProperty(key);
    }

    /**
     * Liefert einen Konfigurationsparameter als Integer aus
     *
     * @param key Der Konfigurations-Key
     * @return Der Parameter als Integer
     */
    protected int getInt(String key) {
        return Integer.valueOf(getString(key));
    }

    /**
     * Liefert einen Konfigurationsparameter als Long aus
     *
     * @param key Der Konfigurations-Key
     * @return Der Parameter als Long
     */
    protected long getLong(String key) {
        return Long.valueOf(getString(key));
    }

    /**
     * Die Bootstrap-Klasse abfragen
     *
     * @return Die Bootstrap-Klasse
     */
    protected Bootstrap getMaster() {
        return master;
    }

    /**
     * Setzt die Bootstrap-Klasse
     *
     * @param master Die Bootstrap-Klasse
     */
    protected void setMaster(Bootstrap master) {
        this.master = master;
    }

    /**
     * Der File-Separator
     *
     * @return Der File-Separator
     */
    protected String getFs() {
        return fs;
    }

    /**
     * Setzt den File-Separator
     *
     * @param fs Der File-Separator
     */
    protected void setFs(String fs) {
        this.fs = fs;
    }

    /**
     * Das User-Home-Verzeichnis
     *
     * @return Das User-Home Verzeichnis
     */
    protected String getUh() {
        return uh;
    }

    /**
     * Setzt das User-Home-Verzeichnis
     *
     * @param uh Das User-Home-Verzeichnis
     */
    protected void setUh(String uh) {
        this.uh = uh;
    }

    /**
     * Die Chatkonfigurationsdaten
     *
     * @return Die Chatkonfigurationsdaten
     */
    protected Properties getP() {
        return p;
    }

    /**
     * Setzt die Chatkonfigurationsdaten
     *
     * @param p Die Chatkonfigurationsdaten
     */
    protected void setP(Properties p) {
        this.p = p;
    }

    /**
     * Die Chatdatenbank
     *
     * @return Die Datenbank
     */
    protected Database getDb() {
        return db;
    }

    /**
     * Setzt die Chatdatenbank
     *
     * @param db Die Chatdatenbank
     */
    protected void setDb(Database db) {
        this.db = db;
    }
}
