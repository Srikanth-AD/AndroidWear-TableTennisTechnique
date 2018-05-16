package me.srikanth.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        final ListView listview = findViewById(R.id.listview);
        Resources res = getResources();
        String[] ttStrokesArr = res.getStringArray(R.array.tabletennis_strokes);
        List<String> ttStrokesList = new ArrayList<>(Arrays.asList(ttStrokesArr));

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, ttStrokesList);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                String itemName = parent.getItemAtPosition(position).toString();
                switch (itemName) {
                    case "Backhand Drive":
                        Intent i = new Intent(getApplicationContext(), BackhandDrive.class);
                        startActivity(i);
                        break;
                    case "Forehand Drive":
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