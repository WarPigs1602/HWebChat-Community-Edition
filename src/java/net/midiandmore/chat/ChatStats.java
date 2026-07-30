package net.midiandmore.chat;

import java.util.Date;

public class ChatStats {
    private Bootstrap master;
    private int currentPeakUsers;
    private String currentPeakRoom;
    private long lastPeakRecord;

    public ChatStats(Bootstrap master) {
        setMaster(master);
        setCurrentPeakUsers(0);
        setCurrentPeakRoom("");
        setLastPeakRecord(0);
    }

    public void track() {
        var cm = getMaster().getChatManager();
        var db = getMaster().getConfig().getDb();
        var now = new Date().getTime();
        int totalUsers = cm.getUsers().size();
        String peakRoom = "";
        int roomPeak = 0;
        for (var entry : cm.getRooms().entrySet()) {
            var size = entry.getValue().getUsers().size();
            if (size > roomPeak) {
                roomPeak = size;
                peakRoom = entry.getKey();
            }
        }
        if (totalUsers > getCurrentPeakUsers()) {
            setCurrentPeakUsers(totalUsers);
            setCurrentPeakRoom("");
        }
        if (roomPeak > 0) {
            setCurrentPeakRoom(peakRoom);
        }
        if (totalUsers == 0 && getCurrentPeakUsers() > 0) {
            setCurrentPeakUsers(totalUsers);
        }
        if (now - getLastPeakRecord() > 60000) {
            db.recordPeak(getCurrentPeakUsers(), getCurrentPeakRoom());
            setLastPeakRecord(now);
        }
        long hourlySince = Math.max(0, now - 60000);
        java.util.Map<String, Integer> hourlyMsgs = db.getChatMessageCountsSince(hourlySince);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long dailySince = cal.getTimeInMillis();
        java.util.Map<String, Integer> dailyMsgs = db.getChatMessageCountsSince(dailySince);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        long monthlySince = cal.getTimeInMillis();
        java.util.Map<String, Integer> monthlyMsgs = db.getChatMessageCountsSince(monthlySince);
        for (var entry : cm.getRooms().entrySet()) {
            var room = entry.getKey();
            var users = entry.getValue().getUsers().size();
            db.upsertHourly(room, users, users, hourlyMsgs.getOrDefault(room, 0));
            db.upsertDaily(room, users, users, dailyMsgs.getOrDefault(room, 0));
            db.upsertMonthly(room, users, users, monthlyMsgs.getOrDefault(room, 0));
        }
    }

    protected Bootstrap getMaster() {
        return master;
    }

    protected void setMaster(Bootstrap master) {
        this.master = master;
    }

    protected int getCurrentPeakUsers() {
        return currentPeakUsers;
    }

    protected void setCurrentPeakUsers(int currentPeakUsers) {
        this.currentPeakUsers = currentPeakUsers;
    }

    protected String getCurrentPeakRoom() {
        return currentPeakRoom;
    }

    protected void setCurrentPeakRoom(String currentPeakRoom) {
        this.currentPeakRoom = currentPeakRoom;
    }

    protected long getLastPeakRecord() {
        return lastPeakRecord;
    }

    protected void setLastPeakRecord(long lastPeakRecord) {
        this.lastPeakRecord = lastPeakRecord;
    }
}
