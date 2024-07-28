package net.midiandmore.chat;

import java.io.IOException;
import java.util.logging.FileHandler;
import static java.util.logging.Level.ALL;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Loggt die Chats
 *
 * @author Andreas Pschorn
 */
public class ChatLog {

    private Bootstrap master;
    
    /**
     * Der Logger
     */
    protected static final Logger LOGGING = getLogger("HWebChatLog");
    
    /**
     * Der FileHandler
     */
    protected static FileHandler fh;
    
    /**
     *
     * @param chatserver Der Bootstrap
     */
    protected ChatLog(Bootstrap chatserver) {
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
        sb.append(getMaster().getUtil().getCurrentDateReverse());
        sb.append("-chat.log");
        var chat = sb.toString();
        try {
            fh = new FileHandler(chat, true); 
            fh.setLevel(ALL);
            fh.setFormatter(new ChatFormatter());
            fh.setEncoding(getMaster().getConfig().getString("charset"));
            getLogger("HWebChatLog").addHandler(fh);
            getLogger("HWebChatLog").setLevel(ALL);
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
