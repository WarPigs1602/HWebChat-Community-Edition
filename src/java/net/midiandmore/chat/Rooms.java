package net.midiandmore.chat;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Verwaltet Raumdaten
 *
 * @author WarPigs
 */
public final class Rooms {

    private boolean allowSmilies;
    private Bootstrap master;
    private boolean moderated;
    private String name;
    private boolean open;
    private boolean standard;
    private String topic;
    private String owner;
    private ArrayList<String> users;
    private ArrayList<String> sus;

    /**
     * Erzeugt einen neuen Raum
     *
     * @param master
     * @param name Raumname
     * @param open Raum offen oder nicht
     */
    public Rooms(Bootstrap master, String name, boolean open) {
        setUsers(new ArrayList<>());
        setName(name);
        setOpen(open);
        setMaster(master);
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        setTopic(null);
        checkForStandardRoom();
        setSus(new ArrayList<>());
        if (db.roomExists(name)) {
            var su = db.getRoomData(name, "su");
            for (var s : su.split(" ")) {
                getSus().add(s.toLowerCase());
            }
            var chanOwner = db.getRoomData(name, "owner");
            setOwner(chanOwner != null ? chanOwner.toLowerCase() : "");
        } else {
            setOwner("");
        }
    }

    /**
     *
     */
    protected void optimizeUserList() {
        var e = getUsers();
        Set<String> set = new LinkedHashSet<>(e);
        setUsers(new ArrayList<>(set));
        getUsers().sort(null);
    }

    /**
     * Ist der Raum ein Standardraum?
     */
    private void checkForStandardRoom() {
        var conf = getMaster().getConfig();
        var db = conf.getDb();
        if (db.roomExists(getName())) {
            setAllowSmilies(!db.getRoomData(getName(), "allow_smilies").equals("0"));
            setStandard(!db.getRoomData(getName(), "standard").equals("0"));
            var t = db.getRoomData(getName(), "topic");
            if (t.length() >= 1) {
                setTopic(t);
            }
        } else {
            setStandard(false);
            setAllowSmilies(true);
        }
    }

    /**
     * Ist hier nur ein Chatter?
     *
     * @return
     */
    protected boolean isLastUser() {
        return getUsers().size() == 1;
    }

    /**
     * Entfernt einen Chatter
     *
     * @param name Chattername
     */
    protected void remove(String name) {
        getUsers().remove(name.toLowerCase());
    }

    /**
     * Ist der Chatter noch Online?
     *
     * @param name Chattername
     * @return
     */
    protected boolean isOnline(String name) {
        return getUsers().contains(name.toLowerCase());
    }

    /**
     * Raumname abfragen
     *
     * @return
     */
    protected String getName() {
        return name;
    }

    /**
     * Raumname vergeben
     *
     * @param name Raumname
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Chatterlistenvektor abrufen
     *
     * @return
     */
    protected ArrayList<String> getUsers() {
        return users;
    }

    /**
     * Chatterlistenvektor erzeugen
     *
     * @param users Neuer Chatterlistenvektor
     */
    protected void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    /**
     * Ist der Raum offen?
     *
     * @return
     */
    protected boolean isOpen() {
        return open;
    }

    /**
     * &Ouml;ffnet/Schlie&szlig;t den Raum bei Bedarf
     *
     * @param open true/false
     */
    protected void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * Ist der Raum ein Standardraum?
     *
     * @return
     */
    protected boolean isStandard() {
        return standard;
    }

    /**
     * Den Raum als Standardraum vergeben
     *
     * @param standard true/false
     */
    protected void setStandard(boolean standard) {
        this.standard = standard;
    }

    /**
     * Raumthema abrufen
     *
     * @return
     */
    protected String getTopic() {
        return topic;
    }

    /**
     * Raumthema vergeben
     *
     * @param topic Raumthema
     */
    protected void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     *
     * @return
     */
    public Bootstrap getMaster() {
        return master;
    }

    /**
     *
     * @param master
     */
    public void setMaster(Bootstrap master) {
        this.master = master;
    }

    /**
     * Ist der Raum moderiert?
     *
     * @return
     */
    protected boolean isModerated() {
        return moderated;
    }

    /**
     * Moderation de-/aktivieren
     *
     * @param moderated true/false
     */
    protected void setModerated(boolean moderated) {
        this.moderated = moderated;
    }

    /**
     * Erlaubt der Raum die nutzung von Emoticons?
     *
     * @return
     */
    protected boolean isAllowSmilies() {
        return allowSmilies;
    }

    /**
     * Erlauben und verbieten von Emoticons
     *
     * @param allowSmilies true/false
     */
    protected void setAllowSmilies(boolean allowSmilies) {
        this.allowSmilies = allowSmilies;
    }

    /**
     * @return the sus
     */
    protected ArrayList<String> getSus() {
        return sus;
    }

    /**
     * @param sus the sus to set
     */
    protected void setSus(ArrayList<String> sus) {
        this.sus = sus;
    }

    /**
     * @return the owner
     */
    protected String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    protected void setOwner(String owner) {
        this.owner = owner;
    }
}
