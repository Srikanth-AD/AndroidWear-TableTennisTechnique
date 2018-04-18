package me.srikanth.myapplication;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wear.widget.BoxInsetLayout;
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
    public SensorEventListener _SensorEventListener1;
    private String gravityValue = "";
    TextView gravityValueTextView;
    public String startBackhandPracticeBtnMode = "start";
    Button startBackhandPracticeButton;
    int forwardCount, rescueCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        gravityValueTextView = findViewById(R.id.gravityValue);

        startBackhandPracticeButton = findViewById(R.id.startBackhandPractice);
        startBackhandPracticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startBackhandPracticeBtnMode.equals("start")) {
                    gravityValueTextView.setText("");
                    mSensorManager.registerListener(_SensorEventListener, mGravitySensor, 500000);
                    mSensorManager.registerListener(_SensorEventListener1, mAccelerometerSensor, 500000);
                    startBackhandPracticeBtnMode = "stop";
                    startBackhandPracticeButton.setText("Stop");
                } else {
                    mSensorManager.unregisterListener(_SensorEventListener);
                    mSensorManager.unregisterListener(_SensorEventListener1);
                    forwardCount = 0; // reset counters
                    rescueCount = 0;
                    startBackhandPracticeBtnMode = "start";
                    startBackhandPracticeButton.setText("Start");
                }
            }
        });

        getSensorData();
    }

    private void getSensorData() {
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravityValueTextView = findViewById(R.id.gravityValue);
        final BoxInsetLayout parentLayout = findViewById(R.id.parent);

        _SensorEventListener =  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //Log.d(TAG, event.sensor.getType() + "");
                if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

                    if (! (event.values[0] + "").equals(gravityValue)) {

                        parentLayout.setBackgroundColor(Color.DKGRAY);

                        gravityValue = event.values[0] + "";
                        //gravityValueTextView.setText(gravityValue);
                        //Log.d(TAG, gravityValue);
                    }

                }
                else
                    Log.d(TAG, "Unknown sensor type");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.d(TAG, accuracy + "");
            }
        };

        _SensorEventListener1 =  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                    //Log.d(TAG, Math.abs(event.values[0]) + " " + Math.abs(event.values[1]) +  " g: " + gravityValue);

                    if (Math.abs(event.values[1]) > 7 && (Double.parseDouble(gravityValue) > 4.6)) {
                        //Log.d(TAG, "g: " + gravityValue + " " + " accelerate Y axis: " + " " + event.values[1]);
                        rescueCount++;
                        triggerVibration();
                        parentLayout.setBackgroundColor(Color.RED);
                    }

                    if (Math.abs(event.values[0]) > 7 && (Double.parseDouble(gravityValue) < 4.6)) {
                        forwardCount++;
                    }

                    gravityValueTextView.setText("Forward: " + forwardCount + " Rescue: " + rescueCount);
                }
                else
                    Log.d(TAG, "Unknown sensor type");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    public void triggerVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 100};
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(_SensorEventListener);
        mSensorManager.unregisterListener(_SensorEventListener1);
    }
}
