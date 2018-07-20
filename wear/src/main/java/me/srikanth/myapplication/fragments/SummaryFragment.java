package me.srikanth.myapplication.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.helpers.Utils;
import me.srikanth.myapplication.models.SharedViewModel;

public class SummaryFragment extends Fragment {

    private SharedViewModel mModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

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
                }
            }
        };

        mModel.getCurrentTimerMode().observe(this, timerObserver);
        mModel.getAvgPeakAcceleration().observe(this, avgPeakAccelerationObserver);
        return view;
    }
}