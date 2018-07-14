package me.srikanth.myapplication.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.helpers.Utils;
import me.srikanth.myapplication.models.SharedViewModel;

public class TimerFragment extends Fragment {

    private SharedViewModel model;

    // Modes: inactive, started, paused, resumed, stopped
    public static final String TIMER_MODE_INACTIVE = "inactive";
    public static final String TIMER_MODE_STARTED = "started";
    public static final String TIMER_MODE_PAUSED = "paused";
    public static final String TIMER_MODE_RESUMED = "resumed";
    public static final String TIMER_MODE_STOPPED = "stopped";

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

        return view;
    }
}
