package me.srikanth.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor mGravitySensor;
    private Sensor mAccelerometerSensor;
    public SensorEventListener _SensorEventListener;
    TextView sessionResultsTextView;
    public String startBackhandPracticeBtnMode = "start";
    Button startBackhandPracticeButton;
    int forwardCount, rescueCount = 0;
    int gravityPeakValue = 0;
    int accelerationPeakValue = 0;

    private static final int GRAVITY_THRESHOLD = 4; // to differentiate forward versus upward movement
    private static final int LINEAR_ACCELERATION_MIN_PEAK_THRESHOLD = 14;
    private  static final int LINEAR_ACCELERATION_AT_REST = 1;  // due to hand movement, acceleration may never be zero

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        sessionResultsTextView = findViewById(R.id.sessionResults);

        try {
            startBackhandPracticeButton = findViewById(R.id.startBackhandPractice);
            startBackhandPracticeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (startBackhandPracticeBtnMode.equals("start")) {

                        // reset counters
                        resetCountsPerRepetition();
                        forwardCount = 0;
                        rescueCount = 0;

                        sessionResultsTextView.setText("");
                        mSensorManager.registerListener(_SensorEventListener, mGravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
                        mSensorManager.registerListener(_SensorEventListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void getSensorData() {
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        _SensorEventListener =  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

                    if (event.values[1] > gravityPeakValue) {
                        gravityPeakValue = (int) event.values[1];
                    }
                }

                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                    Log.d(TAG, "Linear Acc. X: " + Math.abs(event.values[0]));

                    if (Math.abs(event.values[0]) > accelerationPeakValue) {
                        accelerationPeakValue = (int) Math.abs(event.values[0]);
                    }

                    // Forward rep. and reset
                    if (Math.abs(event.values[0]) < LINEAR_ACCELERATION_AT_REST
                            && accelerationPeakValue > LINEAR_ACCELERATION_MIN_PEAK_THRESHOLD
                            && gravityPeakValue < GRAVITY_THRESHOLD) {

                        forwardCount++;
                        resetCountsPerRepetition();

                        // On count increment, update results on UI
                        sessionResultsTextView.setText("Forward: " + forwardCount + " Rescue: " + rescueCount);

                        Log.d(TAG, "Forward count: " + forwardCount);
                    }

                    if (Math.abs(event.values[0]) < LINEAR_ACCELERATION_AT_REST &&
                            accelerationPeakValue > LINEAR_ACCELERATION_MIN_PEAK_THRESHOLD &&
                            gravityPeakValue > GRAVITY_THRESHOLD) {

                        rescueCount++;
                        resetCountsPerRepetition();

                        triggerVibration();

                        // On count change, update results
                        sessionResultsTextView.setText("Forward: " + forwardCount + " Rescue: " + rescueCount);
                    }
                }

                Log.d(TAG, "Peak Gravity: " + gravityPeakValue  + " Peak Acceleration: " + accelerationPeakValue);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    public void resetCountsPerRepetition() {
        gravityPeakValue = 0;
        accelerationPeakValue = 0;
    }

    public void triggerVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(150);
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