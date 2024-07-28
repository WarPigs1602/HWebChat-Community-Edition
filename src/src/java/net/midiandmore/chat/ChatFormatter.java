package net.midiandmore.chat;


import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * MeinCustomFormatter formatiert den Protokollsatz wie folgt: Datum Version
 * lokalisierte Nachricht mit Parametern
 */
public class ChatFormatter extends Formatter {

    /**
     * Der ChatFormatter
     */
    public ChatFormatter() {
        super();
    }

    /**
     *
     * @param record
     * @return
     */
    @Override
    public String format(LogRecord record) {

		// Zeichenfolgepuffer für formatierten Datensatz erstellen.
        // Mit Datum anfangen.
        var sb = new StringBuilder();
        // Datum aus dem Protokollsatz abrufen und dem Puffer hinzufügen
        var date = new Date(record.getMillis());
        sb.append(date.toString());
        sb.append(": ");

	// Formatierte Nachricht abrufen (einschließlich Lokalisierung
        // und Substitution von Parametern) und dem Puffer hinzufügen
        sb.append(formatMessage(record));
        sb.append("\n");

        return sb.toString();
    }
}
