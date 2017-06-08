package co.bwsc.pass.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import co.bwsc.pass.service.PassService;

/**
 * Created by Ben on 3/30/2017.
 */

public class ServiceStarterReceiver extends BroadcastReceiver {
    public static final String TAG = ServiceStarterReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() called");
        Toast.makeText(context, "Starting service", Toast.LENGTH_LONG).show();
        Intent service = new Intent(context, PassService.class);
        context.startService(service);
    }
}

