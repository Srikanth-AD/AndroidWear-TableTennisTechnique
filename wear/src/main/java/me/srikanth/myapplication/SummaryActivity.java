package me.srikanth.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

public class SummaryActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        TextView exerciseNameTextView = findViewById(R.id.exerciseName);
        TextView activeTimeTextView = findViewById(R.id.activeTime);
        TextView forwardCountTextView = findViewById(R.id.forwardCountTextView);
        TextView rescueCountTextView = findViewById(R.id.rescueCountTextView);

        Intent intent = getIntent();
        final String exerciseName = intent.getExtras().getString("exerciseName");
        final long startTime = intent.getExtras().getLong("startTime");
        final long stopTime = intent.getExtras().getLong("stopTime");
        final int forwardCount = intent.getExtras().getInt("forwardCount");
        final int rescueCount = intent.getExtras().getInt("rescueCount");

        exerciseNameTextView.setText(exerciseName);
        activeTimeTextView.setText(Utils.timeConversion(startTime, stopTime));
        forwardCountTextView.setText(String.valueOf(forwardCount));
        rescueCountTextView.setText(String.valueOf(rescueCount));
    }
}
