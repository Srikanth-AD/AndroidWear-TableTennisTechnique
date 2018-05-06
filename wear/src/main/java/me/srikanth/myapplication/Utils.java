package me.srikanth.myapplication;

import android.content.Context;
import android.os.Vibrator;

public class Utils {

    private static final int VIBRATE_FOR_MS = 250; // in milliseconds

    public final static void triggerVibration(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(VIBRATE_FOR_MS);
        }
    }
}
