package me.srikanth.myapplication.fragments;

import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.srikanth.myapplication.R;
import me.srikanth.myapplication.models.SharedViewModel;

public class CountersFragment extends Fragment {

    private SharedViewModel mModel;
    TextView forwardCountTextView;
    TextView rescueCountTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_counters, container, false);

        forwardCountTextView = view.findViewById(R.id.forwardCountTextView);
        rescueCountTextView = view.findViewById(R.id.rescueCountTextView);
        mModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        final Observer<Integer> forwardCountObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer newForwardCount) {
                Log.d("newForwardCount", newForwardCount + "");
                forwardCountTextView.setText(String.valueOf(newForwardCount));
            }
        };

        final Observer<Integer> rescueCountObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer newRescueCount) {
                Log.d("newRescueCount", newRescueCount + "");
                rescueCountTextView.setText(String.valueOf(newRescueCount));
            }
        };

        mModel.getForwardCount().observe(this, forwardCountObserver);
        mModel.getRescueCount().observe(this, rescueCountObserver);

        return view;
    }
}
