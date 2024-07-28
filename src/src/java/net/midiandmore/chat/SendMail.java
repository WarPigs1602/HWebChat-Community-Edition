package net.midiandmore.chat;

import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Klasse zum E-Mail versenden!
 * Die Konfiguration des SMTP-Servers erfolgt &uuml;ber die Konfigurationsdatei mail.xml!
 *
 * @author Andreas Pschorn
 */
public final class SendMail implements Software {


    /**
     *
     * @param master
     */
    public SendMail(Bootstrap master) {
    }

    /**
     * Versendet E-Mails
     * 
     * @param msg Die Nachricht
     * @param subject Der Betreff
     * @param to Die Zieladresse
     * @throws MessagingException
     */
    protected void sendEmail(String msg, String subject, String to) throws MessagingException {
        final Properties p = Bootstrap.boot.getConfig().getDb().getMail();
        Session session;
        if (p.get("use_auth").equals("true")) {
            session = Session.getInstance(p,
                    new jakarta.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(p.getProperty("username"), p.getProperty("password"));
                }
            });
        } else {
           session = Session.getInstance(p);
        }
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(p.getProperty("from-mail-address")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(msg);
        Transport.send(message);
    }
}
