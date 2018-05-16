package me.srikanth.myapplication;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<String> mCurrentMode;

    public MutableLiveData<String> getmCurrentMode() {
        if (mCurrentMode == null) {
            mCurrentMode = new MutableLiveData<>();
        }
        return mCurrentMode;
    }


}