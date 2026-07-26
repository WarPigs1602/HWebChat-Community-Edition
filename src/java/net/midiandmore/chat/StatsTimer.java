package net.midiandmore.chat;

import java.util.TimerTask;

public class StatsTimer extends TimerTask {

    private Bootstrap master;

    public StatsTimer(Bootstrap master) {
        setMaster(master);
    }

    @Override
    public void run() {
        var cm = getMaster().getChatManager();
        var db = getMaster().getConfig().getDb();
        var users = cm.getUsers().size();
        var rooms = cm.getRooms().size();
        db.recordStats(users, rooms);
        for (var entry : cm.getRooms().entrySet()) {
            var roomName = entry.getKey();
            var roomUsers = entry.getValue().getUsers().size();
            db.recordRoomStats(roomName, roomUsers);
        }
    }

    private Bootstrap getMaster() {
        return master;
    }

    private void setMaster(Bootstrap master) {
        this.master = master;
    }
}
