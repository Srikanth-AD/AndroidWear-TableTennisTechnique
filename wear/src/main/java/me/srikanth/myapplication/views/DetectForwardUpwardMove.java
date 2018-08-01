package me.srikanth.myapplication.views;

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
import android.support.v4.app.FragmentTransaction;
import android.support.wear.ambient.AmbientModeSupport;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import me.srikanth.myapplication.R;
import me.srikanth.myapplication.controllers.Utils;
import me.srikanth.myapplication.models.SharedViewModel;

import static me.srikanth.myapplication.views.TimerFragment.TIMER_MODE_STARTED;

public class DetectForwardUpwardMove extends FragmentActivity implements AmbientModeSupport.AmbientCallbackProvider {

    private SensorManager mSensorManager;
    private Sensor mGravitySensor;
    private Sensor mAccelerometer;
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
    private static final long TIME_THRESHOLD_NS = 1500000000; // in nanoseconds (= 1.5sec)
    List<Integer> peakAccelerations = new ArrayList<>();
    float gravityXValue = 0.0f;

    /*
     * Declare an ambient mode controller, which will be used by
     * the activity to determine if the current mode is ambient.
     */
    private AmbientModeSupport.AmbientController mAmbientController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_forward_upward_move);

        mAmbientController = AmbientModeSupport.attach(this);
        mModel = ViewModelProviders.of(this).get(SharedViewModel.class);

        Intent intent = getIntent();
        mModel.getCurrentExercise().setValue(intent.getExtras().getString("exerciseName"));

        headingText = findViewById(R.id.headingText);
        headingText.setText(mModel.getCurrentExercise().getValue());

        resetCountsPerSession();
        initSensors();
        getSensorList();

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
                        newTimerModeName.equals(TIMER_MODE_STARTED)) {
                    resetCountsPerSession();
                }

                // Started or Resumed
                if (newTimerModeName != null &&
                        (newTimerModeName.equals(TIMER_MODE_STARTED) ||
                        newTimerModeName.equals(TimerFragment.TIMER_MODE_RESUMED))
                        ) {

                    registerListeners();
                }

                // Paused or Stopped
                if (newTimerModeName != null &&
                        (newTimerModeName.equals(TimerFragment.TIMER_MODE_PAUSED) ||
                                newTimerModeName.equals(TimerFragment.TIMER_MODE_STOPPED))
                        ) {

                    mSensorManager.unregisterListener(_SensorEventListener);
                }

                // On Stop, display summary
                if (newTimerModeName != null &&
                        newTimerModeName.equals(TimerFragment.TIMER_MODE_STOPPED)) {

                    // At this state, practice has stopped hence,
                    // FLAG_KEEP_SCREEN_ON can be cleared to save battery
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        Log.d("getAmbientCallback", "MyAmbientCallback");
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {

        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
            mModel.getIsAmbinetModeEnabled().setValue(true);
        }

        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
            Log.d("MyAmbientCallback", "onExitAmbient");
            mModel.getIsAmbinetModeEnabled().setValue(false);
        }

        @Override
        public void onUpdateAmbient() {
            super.onUpdateAmbient();
            Log.d("MyAmbientCallback", "onUpdateAmbient");
        }
    }


    private void getSensorData() {

        _SensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                Log.d("sensor event type and accuracy",  event.sensor.getType() + " " + event.accuracy + "");

                switch (event.sensor.getType()) {

                    case Sensor.TYPE_GRAVITY:

                        Log.d("Gravity X, Y axis",
                                event.values[0] + " " + event.values[1]);

                        gravityXValue = event.values[0];

                        if (Math.abs(event.values[0]) > gravityPeak) {
                            gravityPeak = Math.abs(event.values[0]);
                        }
                        break;

                    case Sensor.TYPE_ACCELEROMETER:

                        Log.d("Accelerometer values: X, Y",
                                event.values[0] +  " " + event.values[1]);

                        Log.d("Converted - Linear acceleration value",
                                accelerometerToLinearAcc(event.values[0], gravityXValue) + "");

                        // Convert accelerometer reading to linear acceleration
                        event.values[0] = accelerometerToLinearAcc(event.values[0], gravityXValue);

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

                            Log.d("Gravity Peak", gravityPeak + "");
                            if (gravityPeak <= GRAVITY_THRESHOLD) {
                                incrementForwardCount();
                            } else {
                                incrementRescueCount();
                            }

                            peakAccelerations.add(accelerationPeakValue);
                            resetPeakValuesPerRep();
                        }
                        break;

                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        Log.d("Linear acceleration - X axis", event.values[0] + "");
                        break;

                    default:
                        Log.d("Unknown sensor",  String.valueOf(event.sensor.getType()));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.d("accuracy change" , sensor.getType() + " " + accuracy);
            }
        };
    }

    // Convert accelerometer reading to linear acceleration - for ONE SPECIFIC AXIS
    // Ref: https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-linear
    private float accelerometerToLinearAcc(float accelerometerValue, float gravityValue) {
        float alpha = 0.8f;

        // Isolate the force of gravity with the low-pass filter.
        gravityValue = alpha * gravityValue + (1 - alpha) * accelerometerValue;

        // Remove the gravity contribution with the high-pass filter.
        return accelerometerValue - gravityValue;
    }

    // Get list of sensors available on the wearable device
    private void getSensorList() {
        final List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor type : deviceSensors){
            Log.d("sensors",type.getStringType());
        }
    }

    private void initSensors() {
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void registerListeners() {
        if (mGravitySensor != null) {
            mSensorManager.registerListener(_SensorEventListener,
                    mGravitySensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

        if (mAccelerometer != null) {
            mSensorManager.registerListener(_SensorEventListener,
                    mAccelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
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
    protected void onResume() {
        super.onResume();

        // On resume: if practice is in progress, register listeners
        if (mModel.getCurrentTimerMode().getValue() != null &&
                (mModel.getCurrentTimerMode().getValue().equals(TimerFragment.TIMER_MODE_STARTED)
                        ||
                mModel.getCurrentTimerMode().getValue().equals(TimerFragment.TIMER_MODE_RESUMED)
                )
            ) {
            registerListeners();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(_SensorEventListener);
    }
}
