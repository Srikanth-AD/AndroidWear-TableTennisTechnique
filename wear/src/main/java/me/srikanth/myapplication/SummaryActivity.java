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
        TextView activeTime = findViewById(R.id.activeTime);
        Intent intent = getIntent();

        final long startTime = intent.getExtras().getLong("startTime");
        final long stopTime = intent.getExtras().getLong("stopTime");
        activeTime.setText(Utils.timeConversion(startTime, stopTime));
    }
}
