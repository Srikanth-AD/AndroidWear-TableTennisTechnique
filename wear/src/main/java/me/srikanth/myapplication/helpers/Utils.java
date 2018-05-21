package me.srikanth.myapplication.helpers;

import android.content.Context;
import android.os.Vibrator;

public class Utils {

    private static final int VIBRATE_FOR_MS = 250; // in milliseconds

    public static void triggerVibration(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(VIBRATE_FOR_MS);
        }
    }

    // Seconds to hours:minutes:seconds
    public static String timeConversion(long startTime, long stopTime) {

        final int totalSeconds = (int) ((stopTime - startTime ) / 1000);
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;
        return hours + ":" + minutes + ":" + seconds;
    }


}
