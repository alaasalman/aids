package com.codedemigod.aids;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.codedemigod.aids.services.AIDSService;

public class MainActivity extends Activity {

	public AIDSService mBoundService;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundService = ((AIDSService.LocalBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button startBtn = (Button) findViewById(R.id.start_button);
		Button stopBtn = (Button) findViewById(R.id.stop_button);
		Button resetBtn = (Button) findViewById(R.id.reset_button);

		Intent intent = new Intent(MainActivity.this, AIDSService.class);
		bindService(intent, mConnection, Service.BIND_AUTO_CREATE);

		startBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mBoundService.startDetection();
				Toast.makeText(MainActivity.this, "Started IDS service",
						Toast.LENGTH_SHORT).show();
			}
		});

		stopBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mBoundService.stopDetection();
				Toast.makeText(MainActivity.this, "Stopped IDS service",
						Toast.LENGTH_SHORT).show();
			}
		});

		resetBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mBoundService.stopDetection();
				mBoundService.resetData();

				Toast.makeText(MainActivity.this,
						"Stopped IDS service and reset table",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBoundService != null) {
			unbindService(mConnection);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
