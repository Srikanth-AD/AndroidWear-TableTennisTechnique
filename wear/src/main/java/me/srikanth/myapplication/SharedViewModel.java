package me.srikanth.myapplication;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<String> mCurrentMode;
    private MutableLiveData<Long> startTime;
    private MutableLiveData<Long> stopTime;

    public MutableLiveData<String> getmCurrentMode() {
        if (mCurrentMode == null) {
            mCurrentMode = new MutableLiveData<>();
        }
        return mCurrentMode;
    }

    public MutableLiveData<Long> getStartTime() {
        if (startTime == null) {
            startTime = new MutableLiveData<>();
        }
        return startTime;
    }

    public MutableLiveData<Long> getStopTime() {
        if (stopTime == null) {
            stopTime = new MutableLiveData<>();
        }
        return stopTime;
    }
}