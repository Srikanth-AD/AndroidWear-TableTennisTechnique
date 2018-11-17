package me.srikanth.myapplication;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerListenerService";

    private static final String DATA_ITEM_RECEIVED_PATH = "/practiceSummary";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                DataItem item = event.getDataItem();

                Log.d(TAG, item.getUri() + "");

                if (item.getUri().getPath().compareTo(DATA_ITEM_RECEIVED_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Log.d(TAG, dataMap + "");
                    if (dataMap.getString("exerciseName") == null) {
                        return;
                    }

                    // Write practice summary to Firebase
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                            .child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/practiceSummary");
                    dbRef.push().setValue(dataMap.getString("exerciseName") , new DatabaseReference.CompletionListener() {
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.i("Save", "fail");
                                Toast.makeText(getApplicationContext(), "Failed to save, please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }
    }

}
