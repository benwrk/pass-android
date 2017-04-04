package co.bwsc.pass.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import co.bwsc.pass.service.utility.ServiceUtility;

/**
 * Created by Ben on 3/30/2017.
 */

public class BootCompletedReceiver extends BroadcastReceiver {
    public static final String TAG = BootCompletedReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() called");
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "onReceive() received boot completed action -- starting service");
            ServiceUtility.startPassService(context);
        }
    }
}
