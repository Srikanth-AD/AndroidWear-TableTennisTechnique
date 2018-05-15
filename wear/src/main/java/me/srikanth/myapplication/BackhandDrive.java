package me.srikanth.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BackhandDrive extends WearableActivity {

    private SensorManager mSensorManager;
    private Sensor mLinearAcceleration;
    private Sensor mGravitySensor;
    public SensorEventListener _SensorEventListener;
    TextView forwardCountTextView;
    TextView rescueCountTextView;
    Button startBackhandPracticeButton;
    public boolean isPracticeOngoing = false;
    int forwardCount, rescueCount = 0;
    int accelerationPeakValue = 0;
    float gravityPeak = 0.0f;
    private long accelerationPeakTimestamp = 0;

    private static final float GRAVITY_THRESHOLD = 5.2f; // to differentiate forward versus upward movement
    private static final int MIN_LINEAR_ACCELERATION_AT_PEAK = 15; // minimum acceptable peak acceleration during a rep
    private  static final int MAX_LINEAR_ACCELERATION_AT_REST = 3;  // due to normal hand movement, acceleration may never be zero
    private static final long TIME_THRESHOLD_NS = 2000000000; // in nanoseconds (= 2sec)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backhand_drive);
        setAmbientEnabled();

        forwardCountTextView = findViewById(R.id.forwardCountTextView);
        rescueCountTextView = findViewById(R.id.rescueCountTextView);
        startBackhandPracticeButton = findViewById(R.id.startBackhandPractice);

        resetCountsPerSession();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        startBackhandPracticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPracticeOngoing) {

                    resetCountsPerSession();
                    resetPeakValuesPerRep();

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

                    startBackhandPracticeButton.setText(R.string.stop);

                } else {
                    mSensorManager.unregisterListener(_SensorEventListener);
                    startBackhandPracticeButton.setText(R.string.start);
                }
                isPracticeOngoing = !isPracticeOngoing;
            }
        });
        getSensorData();
    }

    private void getSensorData() {

        _SensorEventListener =  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

                    if (Math.abs(event.values[0]) > gravityPeak) {
                        gravityPeak = Math.abs(event.values[0]);
                    }
                }

                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

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
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    private void incrementForwardCount() {
        forwardCount++;
        forwardCountTextView.setText(String.valueOf(forwardCount));
    }

    private void incrementRescueCount() {
        Utils.triggerVibration(this);
        rescueCount++;
        rescueCountTextView.setText(String.valueOf(rescueCount));
    }

    private void resetPeakValuesPerRep() {
        accelerationPeakValue = 0;
        gravityPeak = 0.0f;
    }

    private void resetCountsPerSession() {
        forwardCount = 0;
        rescueCount = 0;
        forwardCountTextView.setText(String.valueOf(forwardCount));
        rescueCountTextView.setText(String.valueOf(rescueCount));
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
