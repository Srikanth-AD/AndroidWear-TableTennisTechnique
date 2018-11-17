package me.srikanth.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements
        CapabilityClient.OnCapabilityChangedListener {

    private static final String TAG = "MainMobileActivity";

    // Name of capability listed in Wear app's wear.xml.
    // This should be named differently in Phone app's capability.
    private static final String CAPABILITY_WEAR_APP = "verify_remote_wear_app";
    private static final String START_ACTIVITY_PATH = "/start-activity";

    private View mStartActivityBtn;
    private TextView mInformationTextView;
    private Button mRemoteOpenButton;
    private Set<Node> mWearNodesWithApp;
    private List<Node> mAllConnectedNodes;

    // Result from sending RemoteIntent to wear device(s) to open app in play/app store.
    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(TAG, "onReceiveResult: " + resultCode);

            if (resultCode == RemoteIntent.RESULT_OK) {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.play_store_request_success),
                        Toast.LENGTH_SHORT);
                toast.show();

            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.play_store_request_failed),
                        Toast.LENGTH_LONG);
                toast.show();

            } else {
                throw new IllegalStateException("Unexpected result " + resultCode);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInformationTextView = findViewById(R.id.information_text_view);
        mRemoteOpenButton = findViewById(R.id.remote_open_button);

        mInformationTextView.setText(getString(R.string.checking_wear_devices_for_app));

        mRemoteOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayStoreOnWearDevicesWithoutApp();
            }
        });

        mStartActivityBtn = findViewById(R.id.start_wearable_activity);


        // test intent
        Intent intent = new Intent(this, FirebaseUIActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        Wearable.getCapabilityClient(this).removeListener(this, CAPABILITY_WEAR_APP);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        mStartActivityBtn.setEnabled(true);

        Wearable.getCapabilityClient(this).addListener(this, CAPABILITY_WEAR_APP);

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
            Log.d(TAG, getString(R.string.no_wear_devices_linked));
            mInformationTextView.setText(getString(R.string.no_wear_devices_linked));
            mRemoteOpenButton.setVisibility(View.INVISIBLE);

        } else if (mWearNodesWithApp.isEmpty()) {
            Log.d(TAG, getString(R.string.missing_wear_app_on_all_wear_devices));
            mInformationTextView.setText(getString(R.string.missing_wear_app_on_all_wear_devices));
            mRemoteOpenButton.setVisibility(View.VISIBLE);

        } else if (mWearNodesWithApp.size() < mAllConnectedNodes.size()) {
            // TODO: Add your code to communicate with the wear app(s) via
            // Wear APIs (MessageApi, DataApi, etc.)

            Log.d(TAG, getString(R.string.wear_app_installed_some_devices));
            mInformationTextView.setText(getString(R.string.wear_app_installed_some_devices));
            mRemoteOpenButton.setVisibility(View.VISIBLE);

        } else {
            // TODO: Add your code to communicate with the wear app(s) via
            // Wear APIs (MessageApi, DataApi, etc.)

            Log.d(TAG, getString(R.string.wear_app_installed_all_devices));
            mInformationTextView.setText(getString(R.string.wear_app_installed_all_devices));
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
                            .setData(Uri.parse(getString(R.string.play_store_app_uri)));

            for (Node node : nodesWithoutApp) {
                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intent,
                        mResultReceiver,
                        node.getId());
            }
        }
    }

    /** Sends an RPC to start a fullscreen Activity on the wearable. */
    public void onStartWearableActivityClick(View view) {
        Log.d(TAG, "Generating RPC");

        // Trigger an AsyncTask that will query for a list of connected nodes and send a
        // "start-activity" message to each connected node.
        new StartWearableActivityTask().execute();
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }
    }

    @WorkerThread
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

        try {
            // Block on a task and get the result synchronously
            // (because this is on a background thread).
            List<Node> nodes = Tasks.await(nodeListTask);

            for (Node node : nodes) {
                results.add(node.getId());
            }

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }

        return results;
    }

    @WorkerThread
    private void sendStartActivityMessage(String node) {

        Task<Integer> sendMessageTask =
                Wearable.getMessageClient(this).sendMessage(node, START_ACTIVITY_PATH, new byte[0]);

        try {
            // Block on a task and get the result synchronously
            // (because this is on a background thread).
            Integer result = Tasks.await(sendMessageTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }
    }

}