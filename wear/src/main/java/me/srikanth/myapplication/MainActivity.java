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

public class MainActivity extends WearableActivity {

    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor mLinearAcceleration;
    private Sensor mGravitySensor;
    public SensorEventListener _SensorEventListener;
    TextView forwardCountTextView;
    TextView rescueCountTextView;
    public String startBackhandPracticeBtnMode = "start";
    Button startBackhandPracticeButton;
    int forwardCount, rescueCount = 0;
    int accelerationPeakValue = 0;
    float gravityPeak = 0.0f;
    private long mLastTime = 0;
    private long accelerationPeakTimestamp = 0;
    private boolean mUp = false;

    private static final float GRAVITY_THRESHOLD = 6.5f; // to differentiate forward versus upward movement
    private static final int MIN_LINEAR_ACCELERATION_AT_PEAK = 15; // minimum acceptable peak acceleration during a rep
    private  static final int MAX_LINEAR_ACCELERATION_AT_REST = 3;  // due to normal hand movement, acceleration may never be zero
    private static final long TIME_THRESHOLD_NS = 2000000000; // in nanoseconds (= 2sec)
    private static final int VIBRATE_FOR_MS = 250; // in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        forwardCountTextView = findViewById(R.id.forwardCountTextView);
        rescueCountTextView = findViewById(R.id.rescueCountTextView);

        forwardCountTextView.setText(String.valueOf(0));
        rescueCountTextView.setText(String.valueOf(0));

                startBackhandPracticeButton = findViewById(R.id.startBackhandPractice);

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        startBackhandPracticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startBackhandPracticeBtnMode.equals("start")) {

                    // reset counters
                    resetCountsPerSession();
                    resetCountsPerRep();

                    if (mGravitySensor != null)
                        mSensorManager.registerListener(_SensorEventListener,
                                mGravitySensor,
                                SensorManager.SENSOR_DELAY_NORMAL);

                    if (mLinearAcceleration != null)
                        mSensorManager.registerListener(_SensorEventListener,
                                mLinearAcceleration,
                                SensorManager.SENSOR_DELAY_NORMAL);

                    startBackhandPracticeBtnMode = "stop";
                    startBackhandPracticeButton.setText(R.string.stop);

                } else {
                    mSensorManager.unregisterListener(_SensorEventListener);
                    startBackhandPracticeBtnMode = "start";
                    startBackhandPracticeButton.setText(R.string.start);
                }
            }
        });
        getSensorData();
    }

    private void getSensorData() {

        _SensorEventListener =  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                mLastTime = event.timestamp;

                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                    if (event.timestamp - accelerationPeakTimestamp > TIME_THRESHOLD_NS) {
                        accelerationPeakValue = 0; // acceleration peak & gravity peak are no longer valid
                        gravityPeak = 0.0f;
                    }

                    // Forward movement
                    if (Math.abs(event.values[0]) > accelerationPeakValue) {
                        accelerationPeakValue = (int) Math.abs(event.values[0]);
                        accelerationPeakTimestamp = event.timestamp;
                    }

                    if (event.timestamp - accelerationPeakTimestamp < TIME_THRESHOLD_NS &&
                            Math.abs(event.values[0]) <= MAX_LINEAR_ACCELERATION_AT_REST &&
                            accelerationPeakValue > MIN_LINEAR_ACCELERATION_AT_PEAK &&
                            gravityPeak < GRAVITY_THRESHOLD) {
                        forwardCount++;
                        resetCountsPerRep();
                        forwardCountTextView.setText(String.valueOf(forwardCount));
                    }
                }

                if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

                    if (Math.abs(event.values[0]) > gravityPeak) {
                        gravityPeak = Math.abs(event.values[0]);
                    }

                    // Upward and downward movement
                    if ((Math.abs(event.values[0]) > GRAVITY_THRESHOLD)) {

                        if (event.timestamp - mLastTime < TIME_THRESHOLD_NS &&
                                mUp != (event.values[0] > 0)) {
                            onStrokeDetected(!mUp, accelerationPeakValue);
                        }
                        mUp = event.values[0] > 0;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    /**
     * Called on detection of a successful down -> up or up -> down movement of hand.
     */
    private void onStrokeDetected(boolean up, int accelerationPeak) {
        // we only count a pair of up and down as one successful movement
        // check for acceleration peak helps avoid simple up/down hand movement without any acceleration
        if (up || accelerationPeak < MIN_LINEAR_ACCELERATION_AT_PEAK) {
            return;
        }
        rescueCount++;
        triggerVibration();
        rescueCountTextView.setText(String.valueOf(rescueCount));
        resetCountsPerRep();
    }

    public void resetCountsPerRep() {
        accelerationPeakValue = 0;
        gravityPeak = 0.0f;
    }

    public void resetCountsPerSession() {
        forwardCount = 0;
        rescueCount = 0;
    }

    public void triggerVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(VIBRATE_FOR_MS);
        }
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