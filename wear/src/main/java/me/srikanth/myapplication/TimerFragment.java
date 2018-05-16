package me.srikanth.myapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class TimerFragment extends Fragment {

    // Modes: inactive, started, paused, resumed, stopped
    private static final String MODE_INACTIVE = "inactive";
    private static final String MODE_STARTED = "started";
    private static final String MODE_PAUSED = "paused";
    private static final String MODE_RESUMED = "resumed";
    private static final String MODE_STOPPED = "stopped";
    private String currentMode = MODE_INACTIVE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        final Button startBtn = view.findViewById(R.id.btn_start);
        final Button pauseBtn = view.findViewById(R.id.btn_pause);
        final Button resumeBtn = view.findViewById(R.id.btn_resume);
        final Button stopBtn = view.findViewById(R.id.btn_stop);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentMode = MODE_STARTED;
                startBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentMode = MODE_PAUSED;
                pauseBtn.setVisibility(View.GONE);
                resumeBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.VISIBLE);
            }
        });

        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentMode = MODE_RESUMED;
                resumeBtn.setVisibility(View.GONE);
                stopBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentMode = MODE_STOPPED;
                resumeBtn.setVisibility(View.GONE);
                stopBtn.setVisibility(View.GONE);
                startBtn.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }
}
