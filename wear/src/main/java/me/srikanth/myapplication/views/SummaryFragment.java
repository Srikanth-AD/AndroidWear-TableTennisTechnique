package me.srikanth.myapplication.views;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.controllers.Utils;
import me.srikanth.myapplication.models.SharedViewModel;

public class SummaryFragment extends Fragment {

    private SharedViewModel mModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        final PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/practiceSummary");

        final TextView exerciseNameTextView = view.findViewById(R.id.exerciseName);
        final TextView activeTimeTextView = view.findViewById(R.id.activeTime);
        final TextView stat1LabelTextView = view.findViewById(R.id.stat1Label);
        final TextView stat2LabelTextView = view.findViewById(R.id.stat2Label);
        final TextView stat1TextView = view.findViewById(R.id.stat1);
        final TextView stat2TextView = view.findViewById(R.id.stat2);
        final TextView avgPeakAccelerationTextView = view.findViewById(R.id.avgPeakAcceleration);

        final Observer<String> timerObserver = new Observer<String>() {

            @Override
            public void onChanged(@Nullable final String newTimerModeName) {

                Log.d("new timer mode", newTimerModeName + "");
                if (newTimerModeName != null &&
                        newTimerModeName.equals(TimerFragment.TIMER_MODE_STOPPED))
                {

                    if (mModel.getCurrentExercise().getValue() == null ) {
                        Log.d("Exercise name", "is null");
                        return;
                    }

                    dataMapRequest.getDataMap()
                            .putString("exerciseName",
                                    mModel.getCurrentExercise().getValue() != null ?
                                            mModel.getCurrentExercise().getValue() : "");

                    dataMapRequest.getDataMap()
                            .putInt("forwardCount",
                                    mModel.getForwardCount().getValue() != null ?
                                            mModel.getForwardCount().getValue() : 0);
                    dataMapRequest.getDataMap()
                            .putInt("rescueCount",
                                    mModel.getRescueCount().getValue() != null ?
                                            mModel.getRescueCount().getValue() : 0);


                    exerciseNameTextView.setText(mModel.getCurrentExercise().getValue());

                    Log.d("forward count", mModel.getForwardCount().getValue() + "");

                    if (mModel.getStartTime().getValue() != null &&
                            mModel.getStopTime().getValue() != null) {
                        activeTimeTextView.setText(Utils.timeConversion(
                                mModel.getStartTime().getValue(), mModel.getStopTime().getValue()));
                    }

                    switch (mModel.getCurrentExercise().getValue()) {

                        case SharedViewModel.EXERCISE_BACKHAND_LOOP:
                        case SharedViewModel.EXERCISE_FOREHAND_LOOP:

                            stat1LabelTextView.setText(R.string.loop_count);
                            stat2LabelTextView.setText(R.string.loop_drive_count);
                            stat1TextView.setText(String.valueOf(mModel.getRescueCount().getValue()));
                            stat2TextView.setText(String.valueOf(mModel.getForwardCount().getValue()));
                            break;

                        case SharedViewModel.EXERCISE_FOREHAND_DRIVE:
                        case SharedViewModel.EXERCISE_BACKHAND_DRIVE:

                            stat1LabelTextView.setText(R.string.drive_count);
                            stat2LabelTextView.setText(R.string.loop_drive_count);
                            stat1TextView.setText(String.valueOf(mModel.getForwardCount().getValue()));
                            stat2TextView.setText(String.valueOf(mModel.getRescueCount().getValue()));
                            break;

                        default:
                            break;
                    }
                }
            }
        };

        final Observer<Integer> avgPeakAccelerationObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer newAvgPeakAcceleration) {
                if (newAvgPeakAcceleration != null) {

                    avgPeakAccelerationTextView.setText(String.valueOf(Utils.convertms2tomph(
                            newAvgPeakAcceleration)));
                    avgPeakAccelerationTextView.append(" " + getString(R.string.mph));

                    dataMapRequest.getDataMap().putInt("avgPeakAcceleration",
                            newAvgPeakAcceleration > 0 ?
                                    Utils.convertms2tomph(newAvgPeakAcceleration) : 0);

                    // Send summary to connected phone
                    PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
                    putDataRequest.setUrgent();
                    Task<DataItem> putTask = Wearable.getDataClient(getActivity()).putDataItem(putDataRequest);

                    //new SendDataToPhoneTask().execute();
                }
            }
        };

        mModel.getCurrentTimerMode().observe(this, timerObserver);
        mModel.getAvgPeakAcceleration().observe(this, avgPeakAccelerationObserver);

        return view;
    }

    public void sendSummaryToWearable() {
        Log.d("SummaryFragment", "sendSummaryToWearable()");


        //dataMapRequest.getDataMap().putInt("forwardCount", mModel.getForwardCount().getValue() != null ? mModel.getForwardCount().getValue() : 0);
        //dataMapRequest.getDataMap().putInt("rescueCount", mModel.getRescueCount().getValue() != null ? mModel.getRescueCount().getValue() : 0);
        // @todo log putTask results
    }

    private class SendDataToPhoneTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Log.d("SendDataToPhoneTask", node);
                sendSummaryToPhone(node);
            }
            return null;
        }
    }

    @WorkerThread
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getActivity()).getConnectedNodes();

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            List<Node> nodes = Tasks.await(nodeListTask);

            for (Node node : nodes) {
                results.add(node.getId());
            }

        } catch (ExecutionException exception) {
            Log.e("SummaryFragment", "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e("SummaryFragment", "Interrupt occurred: " + exception);
        }

        return results;
    }

    @WorkerThread
    private void sendSummaryToPhone(String node) {

        Task<Integer> sendMessageTask =
                Wearable.getMessageClient(getActivity()).sendMessage(node, "/data-item-received", new byte[2]);

        try {
            // Block on a task and get the result synchronously
            // (because this is on a background thread).
            Integer result = Tasks.await(sendMessageTask);
            Log.d("sendSummaryToPhone", "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e("sendSummaryToPhone", "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e("sendSummaryToPhone", "Interrupt occurred: " + exception);
        }
    }
}