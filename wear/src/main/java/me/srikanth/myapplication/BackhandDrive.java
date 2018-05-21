package me.srikanth.myapplication;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager;

public class BackhandDrive extends FragmentActivity {

    private SensorManager mSensorManager;
    private Sensor mLinearAcceleration;
    private Sensor mGravitySensor;
    public SensorEventListener _SensorEventListener;
    int forwardCount, rescueCount = 0;
    int accelerationPeakValue = 0;
    float gravityPeak = 0.0f;
    private long accelerationPeakTimestamp = 0;
    private SharedViewModel mModel;

    private static final float GRAVITY_THRESHOLD = 5.4f; // to differentiate forward versus upward movement
    private static final int MIN_LINEAR_ACCELERATION_AT_PEAK = 11; // minimum acceptable peak acceleration during a rep
    private  static final int MAX_LINEAR_ACCELERATION_AT_REST = 3;  // due to normal hand movement, acceleration may never be zero
    private static final long TIME_THRESHOLD_NS = 1800000000; // in nanoseconds (= 2sec)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backhand_drive);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // @todo add ambient mode support
        mModel = ViewModelProviders.of(this).get(SharedViewModel.class);

        resetCountsPerSession();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        final Observer<String> timerObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newModeName) {

                Log.d("Timer mode", newModeName);
                resetPeakValuesPerRep();

                // Started
                if (newModeName != null &&
                        newModeName.equals(TimerFragment.TIMER_MODE_STARTED)) {
                    resetCountsPerSession();
                    mModel.getCurrentExercise().setValue(SharedViewModel.EXERCISE_BACKHAND_DRIVE);
                }

                // Started or Resumed
                if (newModeName != null &&
                        (newModeName.equals(TimerFragment.TIMER_MODE_STARTED) ||
                        newModeName.equals(TimerFragment.TIMER_MODE_RESUMED))
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
                if (newModeName != null &&
                        (newModeName.equals(TimerFragment.TIMER_MODE_PAUSED) ||
                                newModeName.equals(TimerFragment.TIMER_MODE_STOPPED))
                        ) {

                    mSensorManager.unregisterListener(_SensorEventListener);
                }

                // On Stop, display summary
                if (newModeName != null && newModeName.equals(TimerFragment.TIMER_MODE_STOPPED)) {

                    Intent i = new Intent(getApplicationContext(), SummaryActivity.class);
                    i.putExtra("exerciseName", mModel.getCurrentExercise().getValue());
                    i.putExtra("startTime", mModel.getStartTime().getValue());
                    i.putExtra("stopTime", mModel.getStopTime().getValue());
                    i.putExtra("forwardCount", mModel.getForwardCount().getValue());
                    i.putExtra("rescueCount", mModel.getRescueCount().getValue());

                    mModel.getCurrentExercise().setValue(""); // reset
                    startActivity(i);
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

                    case  Sensor.TYPE_GRAVITY:
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
        Utils.triggerVibration(this);
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
