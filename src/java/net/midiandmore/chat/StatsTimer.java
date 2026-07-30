package net.midiandmore.chat;

import java.util.TimerTask;

public class StatsTimer extends TimerTask {
    private Bootstrap master;

    public StatsTimer(Bootstrap master) {
        setMaster(master);
    }

    @Override
    public void run() {
        try {
            getMaster().getChatStats().track();
        } catch (Exception e) {
            Bootstrap.logError(e);
        }
    }

    private Bootstrap getMaster() {
        return master;
    }

    private void setMaster(Bootstrap master) {
        this.master = master;
    }
}
