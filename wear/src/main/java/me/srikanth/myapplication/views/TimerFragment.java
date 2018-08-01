package me.srikanth.myapplication.views;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.controllers.Utils;
import me.srikanth.myapplication.models.SharedViewModel;

public class TimerFragment extends Fragment {

    private SharedViewModel model;

    // Modes: inactive, started, paused, resumed, stopped
    public static final String TIMER_MODE_INACTIVE = "inactive";
    public static final String TIMER_MODE_STARTED = "started";
    public static final String TIMER_MODE_PAUSED = "paused";
    public static final String TIMER_MODE_RESUMED = "resumed";
    public static final String TIMER_MODE_STOPPED = "stopped";
    private String currentTimerMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getCurrentTimerMode().setValue(TIMER_MODE_INACTIVE);

        final ImageButton startBtn = view.findViewById(R.id.btn_start);
        final ImageButton pauseBtn = view.findViewById(R.id.btn_pause);
        final ImageButton resumeBtn = view.findViewById(R.id.btn_resume);
        final ImageButton stopBtn = view.findViewById(R.id.btn_stop);
        final TextView startBtnLabel = view.findViewById(R.id.label_btn_start);
        final TextView pauseBtnLabel = view.findViewById(R.id.label_btn_pause);
        final TextView resumeBtnLabel = view.findViewById(R.id.label_btn_resume);
        final TextView stopBtnLabel = view.findViewById(R.id.label_btn_stop);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.getStartTime().setValue(System.currentTimeMillis());
                model.getCurrentTimerMode().setValue(TIMER_MODE_STARTED);

                startBtn.setVisibility(View.GONE);
                startBtnLabel.setVisibility(View.GONE);

                pauseBtn.setVisibility(View.VISIBLE);
                pauseBtnLabel.setVisibility(View.VISIBLE);

                Utils.triggerVibration(getActivity());
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.getCurrentTimerMode().setValue(TIMER_MODE_PAUSED);

                pauseBtn.setVisibility(View.GONE);
                pauseBtnLabel.setVisibility(View.GONE);

                resumeBtn.setVisibility(View.VISIBLE);
                resumeBtnLabel.setVisibility(View.VISIBLE);

                stopBtn.setVisibility(View.VISIBLE);
                stopBtnLabel.setVisibility(View.VISIBLE);

                Utils.triggerVibration(getActivity());
            }
        });

        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.getCurrentTimerMode().setValue(TIMER_MODE_RESUMED);

                resumeBtn.setVisibility(View.GONE);
                resumeBtnLabel.setVisibility(View.GONE);

                stopBtn.setVisibility(View.GONE);
                stopBtnLabel.setVisibility(View.GONE);

                pauseBtn.setVisibility(View.VISIBLE);
                pauseBtnLabel.setVisibility(View.VISIBLE);

                Utils.triggerVibration(getActivity());
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.getStopTime().setValue(System.currentTimeMillis());
                model.getCurrentTimerMode().setValue(TIMER_MODE_STOPPED);

                resumeBtn.setVisibility(View.GONE);
                resumeBtnLabel.setVisibility(View.GONE);

                stopBtn.setVisibility(View.GONE);
                stopBtnLabel.setVisibility(View.GONE);

                startBtn.setVisibility(View.VISIBLE);
                startBtnLabel.setVisibility(View.VISIBLE);

                Utils.triggerVibration(getActivity());
            }
        });

        // Switch action button display on or off based on Ambient mode: enabled or not
        final Observer<Boolean> ambientModeObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean newIsAmbientModeValue) {
                Log.d("TimerFragment: ambientModeObserver", newIsAmbientModeValue + "");

                // Ambient mode: on
                if (newIsAmbientModeValue != null && newIsAmbientModeValue) {

                    // TIMER_MODE_INACTIVE
                    if (currentTimerMode.equals(TIMER_MODE_INACTIVE)) {
                        startBtn.setVisibility(View.INVISIBLE);
                        startBtnLabel.setVisibility(View.INVISIBLE);
                    }

                    // TIMER_MODE_STARTED
                    if (currentTimerMode.equals(TIMER_MODE_STARTED)) {

                        pauseBtn.setVisibility(View.INVISIBLE);
                        pauseBtnLabel.setVisibility(View.INVISIBLE);
                    }

                    // TIMER_MODE_PAUSED
                    if (currentTimerMode.equals(TIMER_MODE_PAUSED)) {

                        resumeBtn.setVisibility(View.INVISIBLE);
                        resumeBtnLabel.setVisibility(View.INVISIBLE);

                        stopBtn.setVisibility(View.INVISIBLE);
                        stopBtnLabel.setVisibility(View.INVISIBLE);
                    }

                    // TIMER_MODE_RESUMED
                    if (currentTimerMode.equals(TIMER_MODE_RESUMED)) {

                        pauseBtn.setVisibility(View.INVISIBLE);
                        pauseBtnLabel.setVisibility(View.INVISIBLE);
                    }

                    // TIMER_MODE_STOPPED
                    if (currentTimerMode.equals(TIMER_MODE_STOPPED)) {
                        startBtn.setVisibility(View.INVISIBLE);
                        startBtnLabel.setVisibility(View.INVISIBLE);
                    }

                } else {

                    // Ambient mode: off
                    // TIMER_MODE_INACTIVE
                    if (currentTimerMode.equals(TIMER_MODE_INACTIVE)) {
                        startBtn.setVisibility(View.VISIBLE);
                        startBtnLabel.setVisibility(View.VISIBLE);
                    }

                    // TIMER_MODE_STARTED
                    if (currentTimerMode.equals(TIMER_MODE_STARTED)) {

                        pauseBtn.setVisibility(View.VISIBLE);
                        pauseBtnLabel.setVisibility(View.VISIBLE);
                    }

                    // TIMER_MODE_PAUSED
                    if (currentTimerMode.equals(TIMER_MODE_PAUSED)) {

                        resumeBtn.setVisibility(View.VISIBLE);
                        resumeBtnLabel.setVisibility(View.VISIBLE);

                        stopBtn.setVisibility(View.VISIBLE);
                        stopBtnLabel.setVisibility(View.VISIBLE);
                    }

                    // TIMER_MODE_RESUMED
                    if (currentTimerMode.equals(TIMER_MODE_RESUMED)) {

                        pauseBtn.setVisibility(View.VISIBLE);
                        pauseBtnLabel.setVisibility(View.VISIBLE);
                    }

                    // TIMER_MODE_STOPPED
                    if (currentTimerMode.equals(TIMER_MODE_STOPPED)) {
                        startBtn.setVisibility(View.VISIBLE);
                        startBtnLabel.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        // Set current timer mode to a local variable for use in ambientModeObserver
        final Observer<String> timerModeObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newTimerModeValue) {
                Log.d("TimerFragment: TimerModeObserver - newTimerModeValue",
                        newTimerModeValue + "");
                currentTimerMode = newTimerModeValue;
            }
        };

        model.getCurrentTimerMode().observe(this, timerModeObserver);
        model.getIsAmbinetModeEnabled().observe(this, ambientModeObserver);

        return view;
    }
}
