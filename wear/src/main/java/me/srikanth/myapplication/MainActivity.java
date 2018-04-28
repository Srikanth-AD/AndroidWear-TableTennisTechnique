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
        gravityValueTextView = findViewById(R.id.sessionResults);

        startBackhandPracticeButton = findViewById(R.id.startBackhandPractice);
        startBackhandPracticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startBackhandPracticeBtnMode.equals("start")) {
                    gravityValueTextView.setText("");
                    mSensorManager.registerListener(_SensorEventListener, mGravitySensor, 500000);
                    mSensorManager.registerListener(_SensorEventListener, mAccelerometerSensor, 500000);
                    startBackhandPracticeBtnMode = "stop";
                    startBackhandPracticeButton.setText("Stop");
                } else {
                    mSensorManager.unregisterListener(_SensorEventListener);
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
        gravityValueTextView = findViewById(R.id.sessionResults);

        _SensorEventListener =  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                    gravityValue = event.values[1] + "";
                }

                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                    if (Math.abs(event.values[1]) > 7 && (Double.parseDouble(gravityValue) > 4.4)) {
                        rescueCount++;
                        triggerVibration();
                    }

                    if (Math.abs(event.values[0]) > 7 && (Double.parseDouble(gravityValue) < 4.4)) {
                        forwardCount++;
                    }

                    gravityValueTextView.setText("Forward: " + forwardCount + " Rescue: " + rescueCount);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //Log.d(TAG, accuracy + "");
            }
        };
    }

    public void triggerVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(150);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(_SensorEventListener);
    }
}
