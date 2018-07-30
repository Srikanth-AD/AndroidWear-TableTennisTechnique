package me.srikanth.myapplication.models;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {

    // List of exercises
    public static final String EXERCISE_BACKHAND_DRIVE = "Backhand Drive";
    public static final String EXERCISE_BACKHAND_LOOP = "Backhand Loop";
    public static final String EXERCISE_BACKHAND_PUSH = "Backhand Push";
    public static final String EXERCISE_FOREHAND_DRIVE = "Forehand Drive";
    public static final String EXERCISE_FOREHAND_LOOP = "Forehand Loop";
    public static final String EXERCISE_FOREHAND_PUSH = "Forehand Push";

    private MutableLiveData<String> currentExercise;
    private MutableLiveData<String> currentTimerMode;
    private MutableLiveData<Long> startTime;
    private MutableLiveData<Long> stopTime;
    private MutableLiveData<Integer> forwardCount;
    private MutableLiveData<Integer> rescueCount;
    private MutableLiveData<Integer> avgPeakAcceleration;
    private MutableLiveData<Boolean> isAmbinetModeEnabled;

    public MutableLiveData<String> getCurrentExercise() {
        if (currentExercise == null) {
            currentExercise = new MutableLiveData<>();
        }
        return currentExercise;
    }

    public MutableLiveData<String> getCurrentTimerMode() {
        if (currentTimerMode == null) {
            currentTimerMode = new MutableLiveData<>();
        }
        return currentTimerMode;
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

    public MutableLiveData<Integer> getForwardCount() {
        if (forwardCount == null) {
            forwardCount = new MutableLiveData<>();
            forwardCount.setValue(0);
        }
        return forwardCount;
    }

    public MutableLiveData<Integer> getRescueCount() {
        if (rescueCount == null) {
            rescueCount = new MutableLiveData<>();
            forwardCount.setValue(0);
        }
        return rescueCount;
    }

    public MutableLiveData<Integer> getAvgPeakAcceleration() {
        if (avgPeakAcceleration == null) {
            avgPeakAcceleration = new MutableLiveData<>();
            avgPeakAcceleration.setValue(0);
        }
        return avgPeakAcceleration;
    }

    public MutableLiveData<Boolean> getIsAmbinetModeEnabled() {
        if (isAmbinetModeEnabled == null) {
            isAmbinetModeEnabled = new MutableLiveData<>();
            isAmbinetModeEnabled.setValue(false);
        }
        return isAmbinetModeEnabled;
    }
}