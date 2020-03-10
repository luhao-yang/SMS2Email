package ad1024.uw.sms2email;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            String simId = "";
            if(cursor.getColumnIndex("sim_id") != -1) {
                simId = cursor.getString(cursor.getColumnIndex("sim_id"));
            }

//                StringBuffer info = new StringBuffer();
//                for( int i = 0; i < cursor.getColumnCount(); i++) {
//                    info.append("Column: " + cursor.getColumnName(i) + ", ");
//                    info.append("Value: " + cursor.getString(i) + "\n");
//                }
//                Log.i("SMSService", info.toString());


            Date date = new Date(Long.parseLong(dateStamp));

            MetaData metaData = extractMetaData(body);

            String title = "无主题";
            if(metaData.isValid()) {
                title = metaData.getSenderName() + "验证码" + metaData.getVerificationCode();
            }
            String content = "<p>" + body + "</p>" +
                    "<p> From: " + sender + "</p>" +
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

    private MetaData extractMetaData(String body) {
        Pattern codePattern1 = Pattern.compile("(\\d{6})");
        Pattern codePattern2 = Pattern.compile("(\\d{4})");

        Pattern namePattern = Pattern.compile("(【.+】)");

        MetaData metaData = new MetaData();

        if(body.contains("验证码") ) {
            String rest = body.substring(body.indexOf("验证码"));// 从验证码之后开始查找
            Matcher matcher1 = codePattern1.matcher(rest);
            Matcher matcher2 = codePattern2.matcher(rest);

            if (matcher1.find()) {
                metaData.setVerificationCode(matcher1.group(0));
            } else if (matcher2.find()) {
                metaData.setVerificationCode(matcher2.group(0));
            }
        }

        Matcher nameMatcher = namePattern.matcher(body);
        if (nameMatcher.find()) {
            metaData.setSenderName(nameMatcher.group(0));
        }

        return metaData;
    }


    class MetaData {
        private String verificationCode= "";
        private String senderName = "";

        public void setVerificationCode(String verificationCode) {
            this.verificationCode = verificationCode;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getVerificationCode() {
            return verificationCode;
        }

        public String getSenderName() {
            return senderName;
        }
        public boolean isValid() {
            return !verificationCode.isEmpty() && !senderName.isEmpty();
        }
    }
}
