package ad1024.uw.sms2email;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


public class SMSTransferService extends Service {
    private SMSObserver mObserver;

    public SMSTransferService() {
    }

    @Override
    public void onCreate() {
        mObserver = new SMSObserver(new Handler(), this);
        Log.i("SMSService", "Started");
        getContentResolver().registerContentObserver(Uri.parse("content://sms/inbox"),
                true, mObserver);
        super.onCreate();


        SharedPreferences preferences = getSharedPreferences("email_storage", MODE_PRIVATE);

        String host =  preferences.getString(Consts.Preference.SERVER_ADDRESS, "");
        int port = Integer.parseInt(preferences.getString(Consts.Preference.SERVER_PORT, ""));
        String email = preferences.getString(Consts.Preference.EMAIL, "");
        String password = preferences.getString(Consts.Preference.PASSWORD, "");

        MailUtils.init(host, port, email, password);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SMSService", "Destroyed");
        getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
