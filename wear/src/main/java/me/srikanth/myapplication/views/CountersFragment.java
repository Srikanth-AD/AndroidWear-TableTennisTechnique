package me.srikanth.myapplication.views;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.models.SharedViewModel;

public class CountersFragment extends Fragment {

    private SharedViewModel mModel;
    TextView stat1LabelTextView;
    TextView stat1TextView;
    TextView stat2LabelTextView;
    TextView stat2TextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_counters, container, false);
        mModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        stat1TextView = view.findViewById(R.id.stat1);
        stat1LabelTextView = view.findViewById(R.id.stat1Label);
        stat2TextView = view.findViewById(R.id.stat2);
        stat2LabelTextView = view.findViewById(R.id.stat2Label);

        final Observer<String> currentExerciseObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

                switch (s) {
                    case SharedViewModel.EXERCISE_BACKHAND_DRIVE:
                        stat1LabelTextView.setText(R.string.drive);
                        stat2LabelTextView.setText(R.string.loop_drive);
                        break;

                    case SharedViewModel.EXERCISE_FOREHAND_DRIVE:
                        stat1LabelTextView.setText(R.string.drive);
                        stat2LabelTextView.setText(R.string.loop_drive);
                        break;

                    case SharedViewModel.EXERCISE_BACKHAND_LOOP:
                    case SharedViewModel.EXERCISE_FOREHAND_LOOP:
                        stat1LabelTextView.setText(R.string.loop);
                        stat2LabelTextView.setText(R.string.loop_drive);
                        break;

                    default:
                        break;
                }
            }
        };

        final Observer<Integer> forwardCountObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer newForwardCount) {

                // Backhand or Forehand Drive
                if (mModel.getCurrentExercise().getValue() != null &&
                        (mModel.getCurrentExercise().getValue().equals(SharedViewModel.EXERCISE_BACKHAND_DRIVE) ||
                            mModel.getCurrentExercise().getValue().equals(SharedViewModel.EXERCISE_FOREHAND_DRIVE))) {
                    stat1TextView.setText(String.valueOf(newForwardCount));
                }

                // Backhand or Forehand Loop
                if (mModel.getCurrentExercise().getValue() != null &&
                        (mModel.getCurrentExercise().getValue().equals(SharedViewModel.EXERCISE_BACKHAND_LOOP) ||
                                mModel.getCurrentExercise().getValue().equals(SharedViewModel.EXERCISE_FOREHAND_LOOP))) {
                    stat2TextView.setText(String.valueOf(newForwardCount));
                }
            }
        };

        final Observer<Integer> rescueCountObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer newRescueCount) {

                // Backhand or Forehand Drive
                if (mModel.getCurrentExercise().getValue() != null &&
                        (mModel.getCurrentExercise().getValue().equals(SharedViewModel.EXERCISE_BACKHAND_DRIVE) ||
                            mModel.getCurrentExercise().getValue().equals(SharedViewModel.EXERCISE_FOREHAND_DRIVE))) {
                    stat2TextView.setText(String.valueOf(newRescueCount));
                }

                // Backhand or Forehand Loop
                if (mModel.getCurrentExercise().getValue() != null &&
                        (mModel.getCurrentExercise().getValue().equals(SharedViewModel.EXERCISE_BACKHAND_LOOP) ||
                                mModel.getCurrentExercise().getValue().equals(SharedViewModel.EXERCISE_FOREHAND_LOOP))) {
                    stat1TextView.setText(String.valueOf(newRescueCount));
                }

            }
        };

        // Switch colors based on Ambient mode: enabled or not
        final Observer<Boolean> ambientModeObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean newIsAmbientModeValue) {
                Log.d("CountersFragment: ambientModeObserver", newIsAmbientModeValue + "");

                if (newIsAmbientModeValue != null && newIsAmbientModeValue) {
                    stat1TextView.setTextColor(Color.WHITE);
                    stat2TextView.setTextColor(Color.WHITE);
                } else {
                    stat1TextView.setTextColor(getResources().getColor(R.color.stat1_color, null));
                    stat2TextView.setTextColor(getResources().getColor(R.color.stat2_color, null));
                }
            }
        };

        mModel.getCurrentExercise().observe(this, currentExerciseObserver);
        mModel.getForwardCount().observe(this, forwardCountObserver);
        mModel.getRescueCount().observe(this, rescueCountObserver);
        mModel.getIsAmbinetModeEnabled().observe(this, ambientModeObserver);

        return view;
    }
}
