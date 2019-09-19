package ad1024.uw.sms2email;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class SMSObserver extends ContentObserver {
    private Handler mHandler;
    private Context context;

    public SMSObserver(Handler handler, Context context) {
        super(handler);
        this.mHandler = handler;
        this.context = context;
    }



    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange);
        Log.i("SMSService", "Change Detected, uri: " + uri.toString());

        // some android phone would trigger onChange with this uri
        if (uri.toString().equals("content://sms/raw")) {
            Log.i("SMSService", "raw has been dropped.");
            return;
        }

        if(uri.toString().matches("content://sms/\\d+$")) {
            Log.i("SMSService", "handling new message...");
            new EmailSendTask(this.context).execute();
        }

    }
}
