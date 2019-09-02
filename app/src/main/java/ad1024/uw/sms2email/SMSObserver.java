package ad1024.uw.sms2email;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static android.content.Context.MODE_PRIVATE;

public class SMSObserver extends ContentObserver {
    private Handler mHandler;
    private Context context;

    public SMSObserver(Handler handler, Context context) {
        super(handler);
        this.mHandler = handler;
        this.context = context;
    }

    private class EmailSendTask extends AsyncTask<Void, Void, Void> {

        private Context context;

        public EmailSendTask(Context parent) {
            this.context = parent;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor cursor = null;
            Log.i("SMSService", "Handling new message");
            try {
                // sms columns https://stackoverflow.com/questions/4022088/how-many-database-columns-associated-with-a-sms-in-android
                // todo dual sim cards
                String where = "read = 0";
                cursor = this.context.getContentResolver().query(
                        Uri.parse("content://sms/inbox"),
                        new String[]{"_id", "address", "body", "date"},
                        where, null, "date desc");
                final String title = "New SMS message has come";

                if (cursor.getCount() == 0) {
                    return null;
                }

                cursor.moveToFirst();

                String body = cursor.getString(cursor.getColumnIndex("body"));
                String sender = cursor.getString(cursor.getColumnIndex("address"));
                String date = cursor.getString(cursor.getColumnIndex("date"));

                final SharedPreferences preferences = this.context.getSharedPreferences("email_storage", MODE_PRIVATE);

                Properties props = new Properties();
                props.put("mail.transport.protocol", "smtp");
                props.put("mail.smtp.host", preferences.getString(Consts.Preference.SERVER_ADDRESS,
                        "smtp.gmail.com"));
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", Integer.parseInt(preferences.getString(Consts.Preference.SERVER_PORT, "587")));
                // Port: 465 (SSL required) or 587 (TLS required)

                props.put("mail.smtp.starttls.enable", "true"); //TLS

                // see https://www.mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/


                Log.i("SMSService", "Properities: " + props.toString());

//                Session session = Session.getDefaultInstance(props);
//                session.setDebug(true);

                final String from = preferences.getString(Consts.Preference.EMAIL, "");
                final String password = preferences.getString(Consts.Preference.PASSWORD, "");

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });


                Log.i("SMSService", "email sent to : " + from + ", content: " + body);

//                MimeMessage newEmail = MailUtils.createNewEmail(
//                        session,
//                        from,
//                        title,
//                        body,
//                        from);
//
//                MailUtils.sendEmail(
//                        from,
//                        password,
//                        session,
//                        newEmail);

                ToastUtils.makeText("Email sent!", Toast.LENGTH_SHORT);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.i("SMSObserver", "Change Detected");
        new EmailSendTask(this.context).execute();
        super.onChange(selfChange);
    }
}
