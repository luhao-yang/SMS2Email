package ad1024.uw.sms2email;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import javax.mail.internet.MimeMessage;

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

                // todo dual sim cards
                String where = "read = 0";
                cursor = this.context.getContentResolver().query(
                        Uri.parse("content://sms/inbox"),
                        new String[]{"_id", "address", "body", "date"},
                        where, null, "date desc");


                if (cursor.getCount() == 0) {
                    Log.i("SMSService", "can't find message");
                    return null;
                }

                cursor.moveToFirst();

                // sms columns https://stackoverflow.com/questions/4022088/how-many-database-columns-associated-with-a-sms-in-android
                String body = cursor.getString(cursor.getColumnIndex("body"));
                String sender = cursor.getString(cursor.getColumnIndex("address"));
                String dateStamp = cursor.getString(cursor.getColumnIndex("date"));

//                StringBuffer info = new StringBuffer();
//                for( int i = 0; i < cursor.getColumnCount(); i++) {
//                    info.append("Column: " + cursor.getColumnName(i) + ", ");
//                    info.append("Value: " + cursor.getString(i) + "\n");
//                }
//                Log.i("SMSService", info.toString());


                Date date = new Date(Long.parseLong(dateStamp));

                String title = "New SMS message from " + sender;
                String content = "<p>" + body + "</p> <p>" +
                        "date: " + date + ""
                        +"</p>";

                MimeMessage newEmail = MailUtils.newEmail(title, content);
                MailUtils.sendEmail(newEmail);

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
        Log.i("SMSService", "Change Detected, selfChange: " + selfChange);
        new EmailSendTask(this.context).execute();
        super.onChange(selfChange);
    }
}
