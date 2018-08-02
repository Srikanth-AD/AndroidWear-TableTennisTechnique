package me.srikanth.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        CapabilityClient.OnCapabilityChangedListener, DataClient.OnDataChangedListener {

    private static final String TAG = "MainMobileActivity";
    TextView recentPracticeExerciseNameTextView;

    private static final String WELCOME_MESSAGE = "Welcome to the Table Tennis Technique app which allows to track stroke counts " +
            "and acceleration during practice sessions. \n\n" +
            "This is a phone companion for the wear app. \n\n";

    private static final String CHECKING_MESSAGE =
            WELCOME_MESSAGE + "Checking your Wear devices for app...\n";

    private static final String NO_DEVICES =
            WELCOME_MESSAGE
                    + "You have no Wear devices linked to your phone at this time.\n";

    private static final String MISSING_ALL_MESSAGE =
            WELCOME_MESSAGE
                    + "You are missing the Wear app on all your Wear devices, please click on the "
                    + "button below to install it on those device(s).\n";

    private static final String INSTALLED_SOME_DEVICES_MESSAGE =
            WELCOME_MESSAGE
                    + "Table Tennis Technique Wear app installed on some your device(s) (%s)!\n\nYou can now use it on your Wearable device.\n\n"
                    + "To install this Wear app on the other devices, please click on the button "
                    + "below.\n";

    private static final String INSTALLED_ALL_DEVICES_MESSAGE =
            WELCOME_MESSAGE
                    + "Open the Table Tennis Technique app on your wearable device and start your practice!";

    // Name of capability listed in Wear app's wear.xml.
    // IMPORTANT NOTE: This should be named differently than your Phone app's capability.
    private static final String CAPABILITY_WEAR_APP = "verify_remote_wear_app";

    // Links to Wear app (Play Store).
    private static final String PLAY_STORE_APP_URI =
            "market://details?id=me.srikanth.myapplication";

    // Result from sending RemoteIntent to wear device(s) to open app in play/app store.
    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(TAG, "onReceiveResult: " + resultCode);

            if (resultCode == RemoteIntent.RESULT_OK) {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        "Play Store Request to Wear device successful.",
                        Toast.LENGTH_SHORT);
                toast.show();

            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        "Play Store Request Failed. Wear device(s) may not support Play Store, "
                                + " that is, the Wear device may be version 1.0.",
                        Toast.LENGTH_LONG);
                toast.show();

            } else {
                throw new IllegalStateException("Unexpected result " + resultCode);
            }
        }
    };

    private TextView mInformationTextView;
    private Button mRemoteOpenButton;

    private Set<Node> mWearNodesWithApp;
    private List<Node> mAllConnectedNodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recentPracticeExerciseNameTextView = findViewById(R.id.recent_practice_exercise_name);

        mInformationTextView = findViewById(R.id.information_text_view);
        mRemoteOpenButton = findViewById(R.id.remote_open_button);

        mInformationTextView.setText(CHECKING_MESSAGE);

        mRemoteOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayStoreOnWearDevicesWithoutApp();
            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        Wearable.getCapabilityClient(this).removeListener(this, CAPABILITY_WEAR_APP);
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        Wearable.getCapabilityClient(this).addListener(this, CAPABILITY_WEAR_APP);
        Wearable.getDataClient(this).addListener(this);

        // Initial request for devices with our capability, aka, our Wear app installed.
        findWearDevicesWithApp();

        // Initial request for all Wear devices connected (with or without our capability).
        // Additional Note: Because there isn't a listener for ALL Nodes added/removed from network
        // that isn't deprecated, we simply update the full list when the Google API Client is
        // connected and when capability changes come through in the onCapabilityChanged() method.
        findAllWearDevices();
    }

    /*
     * Updates UI when capabilities change (install/uninstall wear app).
     */
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): " + capabilityInfo);

        mWearNodesWithApp = capabilityInfo.getNodes();

        // Because we have an updated list of devices with/without our app, we need to also update
        // our list of active Wear devices.
        findAllWearDevices();

        verifyNodeAndUpdateUI();
    }

    private void findWearDevicesWithApp() {
        Log.d(TAG, "findWearDevicesWithApp()");

        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(this)
                .getCapability(CAPABILITY_WEAR_APP, CapabilityClient.FILTER_ALL);

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {

                if (task.isSuccessful()) {
                    Log.d(TAG, "Capability request succeeded.");

                    CapabilityInfo capabilityInfo = task.getResult();
                    mWearNodesWithApp = capabilityInfo.getNodes();

                    Log.d(TAG, "Capable Nodes: " + mWearNodesWithApp);

                    verifyNodeAndUpdateUI();

                } else {
                    Log.d(TAG, "Capability request failed to return any results.");
                }
            }
        });
    }

    private void findAllWearDevices() {
        Log.d(TAG, "findAllWearDevices()");

        Task<List<Node>> NodeListTask = Wearable.getNodeClient(this).getConnectedNodes();

        NodeListTask.addOnCompleteListener(new OnCompleteListener<List<Node>>() {
            @Override
            public void onComplete(Task<List<Node>> task) {

                if (task.isSuccessful()) {
                    Log.d(TAG, "Node request succeeded.");
                    mAllConnectedNodes = task.getResult();

                } else {
                    Log.d(TAG, "Node request failed to return any results.");
                }

                verifyNodeAndUpdateUI();
            }
        });
    }

    private void verifyNodeAndUpdateUI() {
        Log.d(TAG, "verifyNodeAndUpdateUI()");

        if ((mWearNodesWithApp == null) || (mAllConnectedNodes == null)) {
            Log.d(TAG, "Waiting on Results for both connected nodes and nodes with app");

        } else if (mAllConnectedNodes.isEmpty()) {
            Log.d(TAG, NO_DEVICES);
            mInformationTextView.setText(NO_DEVICES);
            mRemoteOpenButton.setVisibility(View.INVISIBLE);

        } else if (mWearNodesWithApp.isEmpty()) {
            Log.d(TAG, MISSING_ALL_MESSAGE);
            mInformationTextView.setText(MISSING_ALL_MESSAGE);
            mRemoteOpenButton.setVisibility(View.VISIBLE);

        } else if (mWearNodesWithApp.size() < mAllConnectedNodes.size()) {
            // TODO: Add your code to communicate with the wear app(s) via
            // Wear APIs (MessageApi, DataApi, etc.)

            String installMessage =
                    String.format(INSTALLED_SOME_DEVICES_MESSAGE, mWearNodesWithApp);
            Log.d(TAG, installMessage);
            mInformationTextView.setText(installMessage);
            mRemoteOpenButton.setVisibility(View.VISIBLE);

        } else {
            // TODO: Add your code to communicate with the wear app(s) via
            // Wear APIs (MessageApi, DataApi, etc.)

            String installMessage =
                    String.format(INSTALLED_ALL_DEVICES_MESSAGE, mWearNodesWithApp);
            Log.d(TAG, installMessage);
            mInformationTextView.setText(installMessage);
            mRemoteOpenButton.setVisibility(View.INVISIBLE);

        }
    }

    private void openPlayStoreOnWearDevicesWithoutApp() {
        Log.d(TAG, "openPlayStoreOnWearDevicesWithoutApp()");

        // Create a List of Nodes (Wear devices) without your app.
        ArrayList<Node> nodesWithoutApp = new ArrayList<>();

        for (Node node : mAllConnectedNodes) {
            if (!mWearNodesWithApp.contains(node)) {
                nodesWithoutApp.add(node);
            }
        }

        if (!nodesWithoutApp.isEmpty()) {
            Log.d(TAG, "Number of nodes without app: " + nodesWithoutApp.size());

            Intent intent =
                    new Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse(PLAY_STORE_APP_URI));

            for (Node node : nodesWithoutApp) {
                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intent,
                        mResultReceiver,
                        node.getId());
            }
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("onDataChanged", dataEvents + "");

        LinearLayout recentPracticeLinearLayout = findViewById(R.id.recent_practice_wrapper);
        TextView recentPracticeAccelerationValue = findViewById(R.id.recent_practice_acceleration_value);
        TextView recentPracticeStat1Label = findViewById(R.id.recent_practice_stat1Label);
        TextView recentPracticeStat1Value = findViewById(R.id.recent_practice_stat1Value);
        TextView recentPracticeStat2Label = findViewById(R.id.recent_practice_stat2Label);
        TextView recentPracticeStat2Value = findViewById(R.id.recent_practice_stat2Value);

        for (DataEvent event : dataEvents) {
            Log.d("event type", event.getType() + "");
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/practiceSummary") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    if (dataMap.getInt("avgPeakAcceleration") > 0) {

                        recentPracticeLinearLayout.setVisibility(View.VISIBLE);
                        recentPracticeExerciseNameTextView.setText(dataMap.getString("exerciseName"));

                        recentPracticeAccelerationValue.setText(String.valueOf(dataMap.getInt("avgPeakAcceleration")));
                        recentPracticeAccelerationValue.append(" mph");

                        if (dataMap.getString("exerciseName").toLowerCase().contains("drive")) {
                            Log.d(dataMap.getString("exerciseName"), "drive");
                            recentPracticeStat1Label.setText("Drive count");
                            recentPracticeStat1Value.setText(String.valueOf(dataMap.getInt("forwardCount")));

                            recentPracticeStat2Label.setText("Loop / Drive count");
                            recentPracticeStat2Value.setText(String.valueOf(dataMap.getInt("rescueCount")));
                        } else {
                            Log.d(dataMap.getString("exerciseName"), "loop");
                            recentPracticeStat1Label.setText("Loop count");
                            recentPracticeStat1Value.setText(String.valueOf(dataMap.getInt("rescueCount")));

                            recentPracticeStat2Label.setText("Loop / Drive count");
                            recentPracticeStat2Value.setText(String.valueOf(dataMap.getInt("forwardCount")));
                        }
                        //summaryTextView.append(String.valueOf(dataMap.getInt("forwardCount")));
                        //summaryTextView.append(String.valueOf(dataMap.getInt("rescueCount")));
                        //summaryTextView.append(String.valueOf(dataMap.getInt("avgPeakAcceleration")));
                    }
                }
            }
        }
    }

}