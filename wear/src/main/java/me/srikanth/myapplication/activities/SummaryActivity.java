package me.srikanth.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.helpers.Utils;
import me.srikanth.myapplication.models.SharedViewModel;

public class SummaryActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        TextView exerciseNameTextView = findViewById(R.id.exerciseName);
        TextView activeTimeTextView = findViewById(R.id.activeTime);
        TextView stat1LabelTextView = findViewById(R.id.stat1Label);
        TextView stat2LabelTextView = findViewById(R.id.stat2Label);
        TextView stat1TextView = findViewById(R.id.stat1);
        TextView stat2TextView = findViewById(R.id.stat2);
        TextView avgPeakAccelerationTextView = findViewById(R.id.avgPeakAcceleration);

        Intent intent = getIntent();
        final String exerciseName = intent.getExtras().getString("exerciseName");
        final long startTime = intent.getExtras().getLong("startTime");
        final long stopTime = intent.getExtras().getLong("stopTime");
        final int forwardCount = intent.getExtras().getInt("forwardCount");
        final int rescueCount = intent.getExtras().getInt("rescueCount");
        final int avgPeakAcceleration = intent.getExtras().getInt("avgPeakAcceleration");

        exerciseNameTextView.setText(exerciseName);
        activeTimeTextView.setText(Utils.timeConversion(startTime, stopTime));

        avgPeakAccelerationTextView.setText(String.valueOf(Utils.convertms2tomph(avgPeakAcceleration)));
        avgPeakAccelerationTextView.append(" "); // @todo clean up using string templates
        avgPeakAccelerationTextView.append(getText(R.string.mph));

        if (exerciseName == null ) {
            Log.d("Exercise name", "is null");
            return;
        }

        switch (exerciseName) {

            case SharedViewModel.EXERCISE_BACKHAND_LOOP:
            case SharedViewModel.EXERCISE_FOREHAND_LOOP:

                stat1LabelTextView.setText(R.string.loop_count);
                stat2LabelTextView.setText(R.string.loop_drive_count);
                stat1TextView.setText(String.valueOf(rescueCount));
                stat2TextView.setText(String.valueOf(forwardCount));
                break;

            case SharedViewModel.EXERCISE_FOREHAND_DRIVE:

                stat1LabelTextView.setText(R.string.drive_count);
                stat2LabelTextView.setText(R.string.loop_drive_count);
                stat1TextView.setText(String.valueOf(forwardCount));
                stat2TextView.setText(String.valueOf(rescueCount));
                break;

            case SharedViewModel.EXERCISE_BACKHAND_DRIVE:

                stat1LabelTextView.setText(R.string.drive_count);
                stat2LabelTextView.setText(R.string.rescue_count);
                stat1TextView.setText(String.valueOf(forwardCount));
                stat2TextView.setText(String.valueOf(rescueCount));
                break;

            default:
                break;
        }
    }

}
