package com.monkey.expandablebreathingbutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ExpandableBreathingButton publishBtn = (ExpandableBreathingButton) findViewById(R.id.publish_btn);
        publishBtn.setOnButtonItemClickListener(new ExpandableBreathingButton.OnButtonItemClickListener() {
            @Override
            public void onButtonItemClick(int position) {
                Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
