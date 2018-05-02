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
    private Sensor mLinearAcceleration;
    private Sensor mAccelerometer;
    public SensorEventListener _SensorEventListener;
    TextView sessionResultsTextView;
    public String startBackhandPracticeBtnMode = "start";
    Button startBackhandPracticeButton;
    int forwardCount, rescueCount = 0;
    double accelerometerYPeakValue = 0;
    int accelerationPeakValue = 0;

    private static final double ACCELEROMETER_THRESHOLD = 5.4; // to differentiate forward versus upward movement
    private static final int MIN_LINEAR_ACCELERATION_AT_PEAK = 15; // minimum acceptable peak acceleration during a rep
    private  static final int MAX_LINEAR_ACCELERATION_AT_REST = 2;  // due to hand movement, acceleration may never be zero

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        sessionResultsTextView = findViewById(R.id.sessionResults);
        startBackhandPracticeButton = findViewById(R.id.startBackhandPractice);

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        startBackhandPracticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startBackhandPracticeBtnMode.equals("start")) {

                    // reset counters
                    resetCountsPerSession();
                    resetCountsPerRepetition();

                    sessionResultsTextView.setText("");

                    if (mAccelerometer != null)
                        mSensorManager.registerListener(_SensorEventListener,
                                mAccelerometer,
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

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    if (event.values[1] > accelerometerYPeakValue)
                        accelerometerYPeakValue = event.values[1];
                }

                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                    if (Math.abs(event.values[0]) > accelerationPeakValue)
                        accelerationPeakValue = (int) Math.abs(event.values[0]);

                    // Forward rep. and reset
                    if (Math.abs(event.values[0]) <= MAX_LINEAR_ACCELERATION_AT_REST
                            && accelerationPeakValue > MIN_LINEAR_ACCELERATION_AT_PEAK
                            && accelerometerYPeakValue < ACCELEROMETER_THRESHOLD) {

                        forwardCount++;
                        Log.d(TAG, "Accelerometer Y Peak: " + accelerometerYPeakValue);
                        Log.d(TAG, "Forward count: " + forwardCount);
                        resetCountsPerRepetition();

                        sessionResultsTextView.setText("Forward: "
                                .concat(String.valueOf(forwardCount))
                                .concat(" Rescue: ")
                                .concat(String.valueOf(rescueCount)));

                    }

                    if (Math.abs(event.values[0]) < MAX_LINEAR_ACCELERATION_AT_REST &&
                            accelerationPeakValue > MIN_LINEAR_ACCELERATION_AT_PEAK &&
                            accelerometerYPeakValue > ACCELEROMETER_THRESHOLD) {

                        rescueCount++;
                        Log.d(TAG, "Accelerometer Y Peak: " + accelerometerYPeakValue);
                        Log.d(TAG, "Rescue count: " + rescueCount);
                        resetCountsPerRepetition();
                        triggerVibration();

                        sessionResultsTextView.setText("Forward: "
                                .concat(String.valueOf(forwardCount))
                                .concat(" Rescue: ")
                                .concat(String.valueOf(rescueCount)));
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    public void resetCountsPerRepetition() {
        accelerometerYPeakValue = 0;
        accelerationPeakValue = 0;
    }

    public void resetCountsPerSession() {
        forwardCount = 0;
        rescueCount = 0;
    }

    public void triggerVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(135);
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