package ad1024.uw.sms2email;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtils {

    private static String host;
    private static int port;
    private static String email;
    private static String password;
    private static Session session = null;

    public static void init(String _host, int _port, String _email, String _password) {
        host = _host;
        port = _port;
        email = _email;
        password = _password;

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        // Port: 465 (SSL required) or 587 (TLS required)
        props.put("mail.smtp.starttls.enable", "true"); //TLS

        // see https://www.mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/

        session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

//        Session session = Session.getDefaultInstance(props);
//        session.setDebug(true);

        Log.i("SMSService", "initialisation complete.");

    }

    public static MimeMessage newEmail(String title, String content) {
        MimeMessage message = new MimeMessage(session);
        try {
            // using your email to send emails to yourself
            message.setFrom(new InternetAddress(email, "SMS2Email", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, email);
            message.setSubject(title);
            message.setContent(content, "text/html;charset=UTF-8");
            message.setSentDate(new Date());
            message.saveChanges();

            Log.i("SMSService", "email - title: " + title + ", content: " + content);


        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return message;
    }

    public static void sendEmail(MimeMessage mail) {
        Transport transport = null;
        try {
            transport = session.getTransport();
            transport.connect(email, password);
            transport.sendMessage(mail, mail.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            Log.e("SMSService", "sending message failed");
            e.printStackTrace();
        }
    }
}
