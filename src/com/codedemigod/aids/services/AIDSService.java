package com.codedemigod.aids.services;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codedemigod.aids.AIDSDBHelper;
import com.codedemigod.model.ProcessStats;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

public class AIDSService extends Service {
	private static String TAG = AIDSService.class.getName();
	private TimerTask statsTimerTask;
	private Timer statsTimer;
	
    public AIDSService() {
    }

    
    public int onStartCommand(Intent intent, int flags, int startId){
    
    	statsTimerTask = new TimerTask() {
			
			@Override
			public void run() {
				ActivityManager actManager = (ActivityManager) AIDSService.this.getSystemService(ACTIVITY_SERVICE);
				List<RunningAppProcessInfo> runningProcessList = actManager.getRunningAppProcesses();
				Pattern pidPattern = Pattern.compile("^\\s*(\\d+)\\s*");
				Pattern cpuUsagePattern = Pattern.compile("(\\d+)%");
				
				AIDSDBHelper aidsDBHelper = new AIDSDBHelper(AIDSService.this);
				
				long timeSnapshot = System.currentTimeMillis();
				
				Log.i(TAG, "Running AIDS stats collector");
				
				SparseArray<String> processUsages = new SparseArray<String>(); 
				
				//get cpu usage for processes
				try {
					Process topProcess = Runtime.getRuntime().exec("top -n 1 -d 0");
					
					BufferedReader bufferedStream = new BufferedReader(new InputStreamReader(topProcess.getInputStream()));
					String tLine = null;
					
					bufferedStream.readLine(); //skip over 1st empty line
					bufferedStream.readLine(); //second empty line
					bufferedStream.readLine(); //skip over USER and SYSTEM CPU
					bufferedStream.readLine(); //skip over USER and NICE
					bufferedStream.readLine(); //skip over column titles
					
					while((tLine = bufferedStream.readLine()) != null){
						try{
							
							Matcher pidMatcher = pidPattern.matcher(tLine);
							Matcher cpuMatcher = cpuUsagePattern.matcher(tLine);
							
							if(!pidMatcher.find() || !cpuMatcher.find()){
								//can't find pid or cpu usage, probably line we're not interested in
								continue;
							}
						
							String pid = pidMatcher.group();
							String cpuUsage = cpuMatcher.group();
							
							if(tLine.contains("root")){
								continue;
							}

							processUsages.put(Integer.parseInt(pid.replaceAll("\\s", "")), cpuUsage);
						}
						catch(StringIndexOutOfBoundsException eiobe){
							continue;
						}
					}
						
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//get running processes
				for(RunningAppProcessInfo pInfo: runningProcessList){
					ProcessStats pStats = new ProcessStats();
					
					pStats.TimeSnapshot = timeSnapshot;
					pStats.Uid = String.valueOf(pInfo.uid);
					pStats.Name = pInfo.processName;
					
					//get latest stats for this process to calculate diffs
					ProcessStats oldestPStats = aidsDBHelper.getLatestProcessStats(pStats.Uid);
					
					//get the diff between last stat and now. returned numbers are per android session since last reboot
					pStats.TxBytes = TrafficStats.getUidTxBytes(pInfo.uid);
					pStats.RxBytes = TrafficStats.getUidRxBytes(pInfo.uid);
					
					if(pStats.TxBytes == 0 || pStats.TxBytes == -1){
						pStats.DiffTxBytes = 0;
					}
					else if(pStats.TxBytes < oldestPStats.TxBytes){
						pStats.DiffTxBytes = pStats.TxBytes;
					}
					else {
						pStats.DiffTxBytes = pStats.TxBytes - oldestPStats.TxBytes;
					}
					
					if(pStats.RxBytes == 0 || pStats.RxBytes == -1){
						pStats.DiffRxBytes = 0;
					}
					else if(pStats.RxBytes < oldestPStats.RxBytes){
						pStats.DiffRxBytes = pStats.RxBytes;
					}
					else {
						pStats.DiffRxBytes = pStats.RxBytes - oldestPStats.RxBytes;
					}
					
					pStats.CPUUsage = processUsages.get(pInfo.pid);
					
					aidsDBHelper.insertStats(pStats);
					
					Log.i(TAG, String.format("Storing: %s-%s [%s - %s at %s] from reporting %s - %s", pInfo.pid, pStats.Name, pStats.DiffTxBytes, pStats.DiffRxBytes, pStats.CPUUsage, pStats.TxBytes, pStats.RxBytes));
				}
				
				
			}
		};
    	
		statsTimer = new Timer();
		statsTimer.scheduleAtFixedRate(statsTimerTask, 0, 5000);
		
    	return START_STICKY;
    }
    
    public void onDestroy(){
    	statsTimer.cancel();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
