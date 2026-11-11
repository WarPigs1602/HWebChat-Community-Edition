package net.midiandmore.chat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

public class ChatStats {
    private Bootstrap master;
    private int currentPeakUsers;
    private String currentPeakRoom;
    private long lastPeakRecord;
    private LocalDate peakDate;

    public ChatStats(Bootstrap master) {
        setMaster(master);
        setCurrentPeakUsers(0);
        setCurrentPeakRoom("");
        setLastPeakRecord(0);
    }

    public void track() {
        var cm = getMaster().getChatManager();
        var db = getMaster().getConfig().getDb();
        var now = System.currentTimeMillis();
        var zone = TimeZone.getTimeZone(getMaster().getConfig().getString("time_zone")).toZoneId();
        var current = Instant.ofEpochMilli(now).atZone(zone);
        if (!current.toLocalDate().equals(peakDate)) {
            peakDate = current.toLocalDate();
            setCurrentPeakUsers(0);
            setCurrentPeakRoom("");
            setLastPeakRecord(0);
        }
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
            db.recordPeak(getCurrentPeakUsers(), getCurrentPeakRoom(), current);
            setLastPeakRecord(now);
        }
        long hourlySince = current.truncatedTo(ChronoUnit.HOURS).withEarlierOffsetAtOverlap()
                .toInstant().toEpochMilli();
        java.util.Map<String, Integer> hourlyMsgs = db.getChatMessageCountsSince(hourlySince);
        long dailySince = current.toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli();
        java.util.Map<String, Integer> dailyMsgs = db.getChatMessageCountsSince(dailySince);
        long monthlySince = current.toLocalDate().withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli();
        java.util.Map<String, Integer> monthlyMsgs = db.getChatMessageCountsSince(monthlySince);
        for (var entry : cm.getRooms().entrySet()) {
            var room = entry.getKey();
            var users = entry.getValue().getUsers().size();
            db.upsertHourly(room, users, users, hourlyMsgs.getOrDefault(room, 0), current);
            db.upsertDaily(room, users, users, dailyMsgs.getOrDefault(room, 0), current);
            db.upsertMonthly(room, users, users, monthlyMsgs.getOrDefault(room, 0), current);
        }
        int totalHourlyMsgs = hourlyMsgs.values().stream().mapToInt(Integer::intValue).sum();
        int totalDailyMsgs = dailyMsgs.values().stream().mapToInt(Integer::intValue).sum();
        int totalMonthlyMsgs = monthlyMsgs.values().stream().mapToInt(Integer::intValue).sum();
        db.upsertHourly("", totalUsers, totalUsers, totalHourlyMsgs, current);
        db.upsertDaily("", totalUsers, totalUsers, totalDailyMsgs, current);
        db.upsertMonthly("", totalUsers, totalUsers, totalMonthlyMsgs, current);
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
