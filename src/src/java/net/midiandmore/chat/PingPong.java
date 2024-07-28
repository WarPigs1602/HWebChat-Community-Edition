package net.midiandmore.chat;

import java.util.TimerTask;

/**
 * Die Tageswechsel-Timer-Klasse
 *
 * @author Andreas Pschorn
 */
public class PingPong extends TimerTask {

    private Bootstrap master;

    /**
     *
     * @param master
     */
    public PingPong(Bootstrap master) {
        setMaster(master);
    }

    /**
     * Verwaltet den Tageswechsel
     */
    @Override
    public void run() {
        getMaster().getChatManager().pingPong();
    }

    private Bootstrap getMaster() {
        return master;
    }

    private void setMaster(Bootstrap master) {
        this.master = master;
    }
}
