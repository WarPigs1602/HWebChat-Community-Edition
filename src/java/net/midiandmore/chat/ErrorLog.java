package net.midiandmore.chat;

import java.io.IOException;
import java.util.logging.FileHandler;
import static java.util.logging.Level.ALL;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import java.util.logging.SimpleFormatter;

/**
 * Loggt die Chats
 *
 * @author Andreas Pschorn
 */
public class ErrorLog {

    private Bootstrap master;
    
    /**
     *
     */
    protected static final Logger LOG = getLogger("HWebErrorChatLog");
    
    private FileHandler fh;
    /**
     *
     * @param chatserver Der Bootstrap
     */
    protected ErrorLog(Bootstrap chatserver) {
        setMaster(chatserver);
    }

    /**
     * Setzt den Log
     */
    protected void setLog() {
        var sb = new StringBuilder(); 
        sb.append(getMaster().getConfig().getUh());
        sb.append(getMaster().getConfig().getFs());
        sb.append(".homewebcom");
        sb.append(getMaster().getConfig().getFs());
        sb.append("log");
        sb.append(getMaster().getConfig().getFs());
        sb.append("error.log");
        var chat = sb.toString();
        try {
            fh = new FileHandler(chat, true);
            fh.setLevel(ALL);
            fh.setFormatter(new SimpleFormatter());
            fh.setEncoding(getMaster().getConfig().getString("charset"));
            getLogger("HWebErrorChatLog").addHandler(fh);
            getLogger("HWebErrorChatLog").setLevel(ALL);
        } catch (SecurityException | IOException e) {
        }
    }

    /**
     *
     * @param chatserver
     */
    private void setMaster(Bootstrap chatserver) {
        this.master = chatserver;
    }

    /**
     *
     * @return
     */
    private Bootstrap getMaster() {
        return this.master;
    }
}
