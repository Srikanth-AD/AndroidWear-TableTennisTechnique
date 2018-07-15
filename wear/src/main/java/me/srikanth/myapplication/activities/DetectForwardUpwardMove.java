package me.srikanth.myapplication.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.fragments.CountersFragment;
import me.srikanth.myapplication.fragments.SummaryFragment;
import me.srikanth.myapplication.fragments.TimerFragment;
import me.srikanth.myapplication.models.SharedViewModel;
import me.srikanth.myapplication.helpers.Utils;

public class DetectForwardUpwardMove extends FragmentActivity {

    private SensorManager mSensorManager;
    private Sensor mLinearAcceleration;
    private Sensor mGravitySensor;
    public SensorEventListener _SensorEventListener;
    int forwardCount, rescueCount = 0;
    int accelerationPeakValue = 0;
    float gravityPeak = 0.0f;
    private long accelerationPeakTimestamp = 0;
    private SharedViewModel mModel;
    TextView headingText;
    private static final float GRAVITY_THRESHOLD = 5.5f; // to differentiate forward versus upward movement
    private static final int MIN_LINEAR_ACCELERATION_AT_PEAK = 11; // minimum acceptable peak acceleration during a rep
    private  static final int MAX_LINEAR_ACCELERATION_AT_REST = 3;  // due to normal hand movement, acceleration may never be zero
    private static final long TIME_THRESHOLD_NS = 1800000000; // in nanoseconds (= 2sec)
    List<Integer> peakAccelerations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_forward_upward_move);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // @todo add ambient mode support
        mModel = ViewModelProviders.of(this).get(SharedViewModel.class);

        Intent intent = getIntent();
        mModel.getCurrentExercise().setValue(intent.getExtras().getString("exerciseName"));

        headingText = findViewById(R.id.headingText);
        headingText.setText(mModel.getCurrentExercise().getValue());

        resetCountsPerSession();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        final SummaryFragment summaryFragment = (SummaryFragment)
                getSupportFragmentManager().findFragmentById(R.id.summary_fragment);
        final CountersFragment countersFragment = (CountersFragment)
                getSupportFragmentManager().findFragmentById(R.id.counters_fragment);
        final TimerFragment timerFragment = (TimerFragment)
                getSupportFragmentManager().findFragmentById(R.id.timer_fragment);

        ft.hide(summaryFragment).commit();

        final Observer<String> timerObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newTimerModeName) {

                Log.d("Timer mode", newTimerModeName);
                resetPeakValuesPerRep();

                // Started
                if (newTimerModeName != null &&
                        newTimerModeName.equals(TimerFragment.TIMER_MODE_STARTED)) {
                    resetCountsPerSession();
                }

                // Started or Resumed
                if (newTimerModeName != null &&
                        (newTimerModeName.equals(TimerFragment.TIMER_MODE_STARTED) ||
                        newTimerModeName.equals(TimerFragment.TIMER_MODE_RESUMED))
                        ) {

                    if (mGravitySensor != null) {
                        mSensorManager.registerListener(_SensorEventListener,
                                mGravitySensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }

                    if (mLinearAcceleration != null) {
                        mSensorManager.registerListener(_SensorEventListener,
                                mLinearAcceleration,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }
                }

                // Paused or Stopped
                if (newTimerModeName != null &&
                        (newTimerModeName.equals(TimerFragment.TIMER_MODE_PAUSED) ||
                                newTimerModeName.equals(TimerFragment.TIMER_MODE_STOPPED))
                        ) {

                    mSensorManager.unregisterListener(_SensorEventListener);
                }

                // On Stop, display summary
                if (newTimerModeName != null && newTimerModeName.equals(TimerFragment.TIMER_MODE_STOPPED)) {

                    mModel.getAvgPeakAcceleration().setValue((int) Utils.average(peakAccelerations));

                    // Display: Summary
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.hide(countersFragment);
                    ft1.hide(timerFragment);
                    ft1.show(summaryFragment).commit();
                }
            }
        };

        mModel.getCurrentTimerMode().observe(this, timerObserver);
        getSensorData();
    }

    private void getSensorData() {

        _SensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                switch (event.sensor.getType()) {

                    case Sensor.TYPE_GRAVITY:
                        if (Math.abs(event.values[0]) > gravityPeak) {
                            gravityPeak = Math.abs(event.values[0]);
                        }
                        break;

                    case Sensor.TYPE_LINEAR_ACCELERATION:

                        // acceleration peak & gravity peak are no longer valid after TIME_THRESHOLD_NS
                        if (event.timestamp - accelerationPeakTimestamp > TIME_THRESHOLD_NS) {
                            resetPeakValuesPerRep();
                        }

                        if (Math.abs(event.values[0]) > accelerationPeakValue) {
                            accelerationPeakValue = (int) Math.abs(event.values[0]);
                            accelerationPeakTimestamp = event.timestamp;
                        }

                        if (event.timestamp - accelerationPeakTimestamp < TIME_THRESHOLD_NS &&
                                Math.abs(event.values[0]) <= MAX_LINEAR_ACCELERATION_AT_REST &&
                                accelerationPeakValue > MIN_LINEAR_ACCELERATION_AT_PEAK) {

                            if (gravityPeak <= GRAVITY_THRESHOLD) {
                                incrementForwardCount();
                            } else {
                                incrementRescueCount();
                            }

                            peakAccelerations.add(accelerationPeakValue);
                            resetPeakValuesPerRep();
                        }
                        break;

                    default:
                        Log.d("Unknown sensor",  String.valueOf(event.sensor.getType()));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    private void incrementForwardCount() {
        forwardCount++;
        mModel.getForwardCount().setValue(forwardCount);
    }

    private void incrementRescueCount() {
        rescueCount++;
        mModel.getRescueCount().setValue(rescueCount);
    }

    private void resetPeakValuesPerRep() {
        accelerationPeakValue = 0;
        gravityPeak = 0.0f;
    }

    private void resetCountsPerSession() {
        forwardCount = 0;
        rescueCount = 0;
        mModel.getForwardCount().setValue(forwardCount);
        mModel.getRescueCount().setValue(rescueCount);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(_SensorEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(_SensorEventListener);
    }
}
