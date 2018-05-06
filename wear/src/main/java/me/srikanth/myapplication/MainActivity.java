package me.srikanth.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        final ListView listview = findViewById(R.id.listview);
        final ArrayList<String> list = new ArrayList<>();
        String[] values = new String[] { getString(R.string.backhand_drive),
                getString(R.string.forehand_drive)};
        Collections.addAll(list, values);


        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        Intent i = new Intent(getApplicationContext(), BackhandDrive.class);
                        startActivity(i);
                        break;
                    case 1:
                        Intent j = new Intent(getApplicationContext(), ForehandDrive.class);
                        startActivity(j);
                        break;
                    default:
                        break;
                }
            }

        });
    }
}