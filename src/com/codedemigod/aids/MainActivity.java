package com.codedemigod.aids;

import com.codedemigod.aids.services.AIDSService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button startBtn = (Button)findViewById(R.id.start_button);
        Button stopBtn = (Button)findViewById(R.id.stop_button);
        Button resetBtn = (Button)findViewById(R.id.reset_button);
        
        startBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AIDSService.class);
				MainActivity.this.startService(intent);
				
				Toast.makeText(MainActivity.this, "Started IDS service", Toast.LENGTH_SHORT).show();
			}
		});
        
        stopBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AIDSService.class);
				MainActivity.this.stopService(intent);
				
				Toast.makeText(MainActivity.this, "Stopped IDS service", Toast.LENGTH_SHORT).show();
			}
		});
        
        resetBtn.setOnClickListener(new OnClickListener() {
        	
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AIDSService.class);
				MainActivity.this.stopService(intent);
				AIDSDBHelper aidsDBHelper = new AIDSDBHelper(MainActivity.this);
				
				if(aidsDBHelper.resetAllStats()){
					Toast.makeText(MainActivity.this, "Stopped IDS service and reset table", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(MainActivity.this, "Stopped IDS service but reset signaled an error", Toast.LENGTH_SHORT).show();
				}
			}
		});
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
