package ad1024.uw.sms2email;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import javax.mail.internet.MimeMessage;

public class EmailSendTask extends AsyncTask<Void, Void, Void> {

    private Context context;

    public EmailSendTask(Context parent) {
        this.context = parent;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Cursor cursor = null;

        try {

            String where = "";
            cursor = this.context.getContentResolver().query(
                    Uri.parse("content://sms/inbox"),
                    new String[]{"*"},
                    where, null, "date desc");


            if (cursor.getCount() == 0) {
                Log.i("SMSService", "can't find message with query:" + where);
                return null;
            }

            // only read the latest message
            cursor.moveToFirst();

            // todo dual SIM, how to identify message goes into which SIM card?
            // get effective name from simId?

            // sms columns https://stackoverflow.com/questions/4022088/how-many-database-columns-associated-with-a-sms-in-android
            String body = cursor.getString(cursor.getColumnIndex("body"));
            String sender = cursor.getString(cursor.getColumnIndex("address"));
            String dateStamp = cursor.getString(cursor.getColumnIndex("date"));
            String simId = cursor.getString(cursor.getColumnIndex("sim_id"));

//                StringBuffer info = new StringBuffer();
//                for( int i = 0; i < cursor.getColumnCount(); i++) {
//                    info.append("Column: " + cursor.getColumnName(i) + ", ");
//                    info.append("Value: " + cursor.getString(i) + "\n");
//                }
//                Log.i("SMSService", info.toString());


            Date date = new Date(Long.parseLong(dateStamp));

            String title = "New SMS message from " + sender;
            String content = "<p>" + body + "</p>" +
                    "<p> SIM ID: " + simId + "</p>" +
                    "<p> Date: " + date + "</p>";

            MimeMessage newEmail = MailUtils.newEmail(title, content);
            MailUtils.sendEmail(newEmail);

            ToastUtils.makeText("New Email Sent!", Toast.LENGTH_SHORT);

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
