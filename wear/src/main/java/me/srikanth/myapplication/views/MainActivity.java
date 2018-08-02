package me.srikanth.myapplication.views;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wear.ambient.AmbientModeSupport;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.controllers.StableArrayAdapter;
import me.srikanth.myapplication.models.SharedViewModel;

/**
 * Checks if the phone app is installed on remote device. If it is not, allows user to open app
 * listing on the phone's Play or App Store.
 */
public class MainActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider,
        CapabilityClient.OnCapabilityChangedListener {

    private SharedViewModel mModel;
    SensorManager mSensorManager;
    SensorEventListener _SensorEventListener;
    boolean areSensorsWorking = false;
    boolean areSensorsAvailable = false;
    TextView sensorsLowAccuracyTextView;
    TextView headingText;
    ListView listview;
    TextView loadingTextView;

    private static final String TAG = "MainWearActivity";
    private static final String WELCOME_MESSAGE = "Welcome to our Wear app!\n\n";
    private static final String MISSING_MESSAGE =
            WELCOME_MESSAGE
                    + "You are missing the required phone app, please click on the button below to "
                    + "install it on your phone.\n";

    private static final String INSTALLED_MESSAGE =
            WELCOME_MESSAGE
                    + "Mobile app installed on your %s!\n\nYou can now use MessageApi, "
                    + "DataApi, etc.";

    // Name of capability listed in Phone app's wear.xml.
    // IMPORTANT NOTE: This should be named differently than your Wear app's capability.
    private static final String CAPABILITY_PHONE_APP = "verify_remote_phone_app";
    private Node mAndroidPhoneNodeWithApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        // Enables Ambient mode.
        AmbientModeSupport.attach(this);

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


    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        Wearable.getCapabilityClient(this).removeListener(this, CAPABILITY_PHONE_APP);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        Wearable.getCapabilityClient(this).addListener(this, CAPABILITY_PHONE_APP);

        checkIfPhoneHasApp();
    }

    /*
     * Updates (UI) when capabilities change (install/uninstall phone app).
     */
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): " + capabilityInfo);

        mAndroidPhoneNodeWithApp = pickBestNodeId(capabilityInfo.getNodes());
        verifyNodeAndUpdateUI();
    }

    private void checkIfPhoneHasApp() {
        Log.d(TAG, "checkIfPhoneHasApp()");

        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(this)
                .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_ALL);

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {

                if (task.isSuccessful()) {
                    Log.d(TAG, "Capability request succeeded.");
                    CapabilityInfo capabilityInfo = task.getResult();
                    mAndroidPhoneNodeWithApp = pickBestNodeId(capabilityInfo.getNodes());

                } else {
                    Log.d(TAG, "Capability request failed to return any results.");
                }

                verifyNodeAndUpdateUI();
            }
        });
    }

    private void verifyNodeAndUpdateUI() {

        if (mAndroidPhoneNodeWithApp != null) {

            // TODO: Add code to communicate with the phone app via
            // Wear APIs (MessageApi, DataApi, etc.)

            String installMessage =
                    String.format(INSTALLED_MESSAGE, mAndroidPhoneNodeWithApp.getDisplayName());
            Log.d(TAG, installMessage);

        } else {
            Log.d(TAG, MISSING_MESSAGE);
        }
    }

    /*
     * There should only ever be one phone in a node set (much less w/ the correct capability), so
     * Grab the first one (which should be the only one).
     */
    private Node pickBestNodeId(Set<Node> nodes) {
        Log.d(TAG, "pickBestNodeId(): " + nodes);

        Node bestNodeId = null;
        // Find a nearby node/phone or pick one arbitrarily. Realistically, there is only one phone.
        for (Node node : nodes) {
            bestNodeId = node;
        }
        return bestNodeId;
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);

            Log.d(TAG, "onEnterAmbient() " + ambientDetails);
            // In our case, the assets are already in black and white, so we don't update UI.
        }

        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();

            Log.d(TAG, "onExitAmbient()");
            // In our case, the assets are already in black and white, so we don't update UI.
        }
    }
}