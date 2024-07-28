package net.midiandmore.chat;

import java.util.TimerTask;

/**
 * Die Tageswechsel-Timer-Klasse
 *
 * @author Andreas Pschorn
 */
public class DayChangeTimer extends TimerTask {

    private Bootstrap master;

    /**
     *
     * @param master
     */
    public DayChangeTimer(Bootstrap master) {
        setMaster(master);
    }

    /**
     * Verwaltet den Tageswechsel
     */
    @Override
    public void run() {
        getMaster().getUtil().dayChange();
    }

    private Bootstrap getMaster() {
        return master;
    }

    private void setMaster(Bootstrap master) {
        this.master = master;
    }
}
