package com.codedemigod.aids.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.codedemigod.aids.AIDSDBHelper;
import com.codedemigod.aids.AINDSTask;
import com.codedemigod.analyzer.GAnalyzer;
import com.codedemigod.analyzer.IEAnalyzer;
import com.codedemigod.collector.CPUUsageCollector;
import com.codedemigod.collector.EventCollector;
import com.codedemigod.collector.MemoryCollector;
import com.codedemigod.collector.NetworkCollector;
import com.codedemigod.collector.ProcessCollector;
import com.codedemigod.detector.GDetector;
import com.codedemigod.detector.IEDetector;
import com.codedemigod.detector.ThreatDetector;
import com.codedemigod.model.APackage;

public class AIDSService extends Service {
	private static String TAG = AIDSService.class.getName();
	private static int TIMER = 5000; // 5 sec trigger timer
	BroadcastReceiver eventCollector = null;

	private Timer triggerTimer;
	private TimerTask triggerTimerTask;
	private IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public AIDSService getService() {
			return AIDSService.this;
		}
	}

	public AIDSService() {
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		startDetection();
		
		return START_STICKY;
	}
	
	public void startDetection(){
		eventCollector = new EventCollector();

		AIDSDBHelper aidsDB = AIDSDBHelper.getInstance(this);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);

		registerReceiver(eventCollector, filter);

		IntentFilter packageFilter = new IntentFilter();
		packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		packageFilter.addDataScheme("package");

		registerReceiver(eventCollector, packageFilter);

		PackageManager pMgr = this.getPackageManager();
		List<PackageInfo> pInfoList = pMgr
				.getInstalledPackages(PackageManager.GET_PERMISSIONS);

		for (PackageInfo pi : pInfoList) {

			APackage pkg = APackage.InstanceFromPackageInfo(pi);
			aidsDB.insertPackage(pkg);
		}

		final ArrayList<AINDSTask> idsTasks = new ArrayList<AINDSTask>();

		idsTasks.add(new ProcessCollector());
		idsTasks.add(new CPUUsageCollector());
		idsTasks.add(new MemoryCollector());
		idsTasks.add(new NetworkCollector());
		idsTasks.add(new IEAnalyzer());
		idsTasks.add(new IEDetector());
		idsTasks.add(new GAnalyzer());
		idsTasks.add(new GDetector());
		idsTasks.add(new ThreatDetector());
		
		triggerTimerTask = new TimerTask() {
			@Override
			public void run() {
				for (AINDSTask t : idsTasks) {
					t.Checked++;

					if (t.Checked * (TIMER / 1000) >= t.RunEvery) {
						// TODO run this in a thread?
						Log.i(TAG, String.format("Running %s", t.toString()));
						t.doWork(AIDSService.this);
						t.Checked = 0;
					}
				}
			}
		};		
		
		triggerTimer = new Timer();
		triggerTimer.scheduleAtFixedRate(triggerTimerTask, 0, TIMER);
	}

	public void stopDetection(){
		if (triggerTimer != null) {
			triggerTimer.cancel();
		}

		if (eventCollector != null) {
			
			unregisterReceiver(eventCollector);
			eventCollector = null;
		}
	}
	
	public void resetData(){
		AIDSDBHelper aidsDB = AIDSDBHelper.getInstance(this);
		aidsDB.resetAllData();
	}
	
	public void onDestroy() {
		if (triggerTimer != null) {
			triggerTimer.cancel();
		}

		if (eventCollector != null) {
			unregisterReceiver(eventCollector);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
}
