package co.bwsc.pass.service.utility;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import co.bwsc.pass.service.PassService;

/**
 * Created by Ben on 3/30/2017.
 */

public class ServiceUtility {
    public static final String TAG = ServiceUtility.class.getSimpleName();
    public static void startPassService(Context context) {
        Log.d(TAG, "startPassService called - Context: " + context.getClass().getSimpleName());

        context.startService(new Intent(context, PassService.class));
    }
}
