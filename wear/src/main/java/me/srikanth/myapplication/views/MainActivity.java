package me.srikanth.myapplication.views;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.controllers.StableArrayAdapter;
import me.srikanth.myapplication.models.SharedViewModel;

public class MainActivity extends FragmentActivity {

    private SharedViewModel mModel;
    SensorManager mSensorManager;
    SensorEventListener _SensorEventListener;
    boolean areSensorsWorking = false;
    boolean areSensorsAvailable = false;
    TextView sensorsLowAccuracyTextView;
    TextView headingText;
    ListView listview;
    TextView loadingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        mModel = ViewModelProviders.of(this).get(SharedViewModel.class);
        listview = findViewById(R.id.exercise_list);
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        sensorsLowAccuracyTextView =  findViewById(R.id.sensorsLowAccuracy);
        headingText = findViewById(R.id.headingText);
        loadingTextView = findViewById(R.id.loadingText);

        // Check sensors
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "Checking sensors on a thread");
                checkSensors();
            }
        };

        Thread t = new Thread(r);
        t.start();

        Resources res = getResources();
        String[] ttStrokesArr = res.getStringArray(R.array.tabletennis_exercises);
        List<String> ttStrokesList = new ArrayList<>(Arrays.asList(ttStrokesArr));

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, ttStrokesList);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                String itemName = parent.getItemAtPosition(position).toString();
                Log.d("itemName", itemName);
                switch (itemName) {
                    case "Backhand Drive":
                        Intent i = new Intent(getApplicationContext(), DetectForwardUpwardMove.class);
                        i.putExtra("exerciseName", SharedViewModel.EXERCISE_BACKHAND_DRIVE);
                        startActivity(i);
                        break;
                    case "Forehand Drive":
                        Intent j = new Intent(getApplicationContext(), DetectForwardUpwardMove.class);
                        j.putExtra("exerciseName", SharedViewModel.EXERCISE_FOREHAND_DRIVE);
                        startActivity(j);
                        break;
                    case "Backhand Loop":
                        Intent k = new Intent(getApplicationContext(), DetectForwardUpwardMove.class);
                        k.putExtra("exerciseName", SharedViewModel.EXERCISE_BACKHAND_LOOP);
                        startActivity(k);
                        break;
                    case "Forehand Loop":
                        Intent l = new Intent(getApplicationContext(), DetectForwardUpwardMove.class);
                        l.putExtra("exerciseName", SharedViewModel.EXERCISE_FOREHAND_LOOP);
                        startActivity(l);
                    default:
                        break;
                }
            }

        });
    }

    private void onCheckSensorsComplete() {
        Log.d("onCheckSensorsComplete","onCheckSensorsComplete");
        mSensorManager.unregisterListener(_SensorEventListener);

        Log.d("areSensorsWorking", areSensorsWorking  + "");

        loadingTextView.setVisibility(View.GONE);

        if (areSensorsWorking) {
            sensorsLowAccuracyTextView.setVisibility(View.GONE);
            listview.setVisibility(View.VISIBLE);
            headingText.setVisibility(View.VISIBLE);
        } else {
            sensorsLowAccuracyTextView.setVisibility(View.VISIBLE);
            listview.setVisibility(View.GONE);
            headingText.setVisibility(View.GONE);
        }

    }

    // Check Sensors availability and accuracy
    private void checkSensors() {

        Sensor mGravitySensor;
        Sensor mAccelerometer;

        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mGravitySensor == null || mAccelerometer == null) {
            onCheckSensorsComplete();
        } else {
            areSensorsAvailable = true;
        }

        if (areSensorsAvailable) {

            _SensorEventListener = new SensorEventListener() {

                int counter = 0;
                int gravitySensorAccuracySum = 0;
                int accelerometerSensorAccuracySum = 0;

                @Override
                public void onSensorChanged(SensorEvent event) {

                    counter++;

                    if (counter == 30) { // Sample sensor events
                        if (gravitySensorAccuracySum > 0 || accelerometerSensorAccuracySum > 0) {
                            areSensorsWorking = true;
                        }
                        onCheckSensorsComplete();
                    }


                    switch (event.sensor.getType()) {
                        case Sensor.TYPE_GRAVITY:
                            Log.d("Gravity sensor accuracy", event.accuracy + "");
                            gravitySensorAccuracySum += event.accuracy;
                            break;

                        case Sensor.TYPE_ACCELEROMETER:
                            Log.d("Accelerometer sensor accuracy", event.accuracy + "");
                            accelerometerSensorAccuracySum += event.accuracy;
                            break;

                        default:
                            break;
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };
        }

        mSensorManager.registerListener(_SensorEventListener,
                mGravitySensor,
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(_SensorEventListener,
                mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
    }
}