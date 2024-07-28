package net.midiandmore.chat;

import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Der Thread-Pool
 * Diese Klasse ist ein &Uuml;berbleibsel eines alten Thread-Pool und deswegen noch
 * vorhanden, kann bei Bedarf sp&auml;ter entfernt werden :)
 * @author Andreas Pschorn
 */
public class ThreadPool {
    private final ExecutorService  execu;

    /**
     * 
     * @param j
     */
    public ThreadPool(int j) {
        execu = newFixedThreadPool(j);
    }

    /**
     * 
     * @param runnable
     */
    protected void execute(Runnable runnable) {
        execu.execute(runnable);
    }
}
