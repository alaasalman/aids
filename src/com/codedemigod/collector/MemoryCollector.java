package com.codedemigod.collector;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Debug;

import com.codedemigod.aids.AIDSDBHelper;
import com.codedemigod.aids.AINDSTask;
import com.codedemigod.model.MemoryUsage;

public class MemoryCollector extends AINDSTask {
	
	public MemoryCollector(){
		this.RunEvery = 5;
	}
	
	public void doWork(Context context) {
		AIDSDBHelper aidsDBHelper = AIDSDBHelper.getInstance(context);
		ActivityManager actManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningProcessList = actManager
				.getRunningAppProcesses();
		int[] pids = new int[runningProcessList.size()];

		// get running processes and their pids
		for (int i = 0; i < runningProcessList.size(); i++) {
			RunningAppProcessInfo pInfo = runningProcessList.get(i);
			pids[i] = pInfo.pid;
		}

		// get memory usage for pids, annoying I know..
		Debug.MemoryInfo[] memoryInfos = actManager.getProcessMemoryInfo(pids);
		long timestamp = System.currentTimeMillis();

		for (int i = 0; i < memoryInfos.length; i++) {
			Debug.MemoryInfo memInfo = memoryInfos[i];
			int pid = pids[i];

			MemoryUsage mu = new MemoryUsage();
			mu.TimeStamp = timestamp;
			mu.Pid = String.valueOf(pid);
			mu.PSSMemory = memInfo.getTotalPss();
			mu.SharedMemory = memInfo.getTotalSharedDirty();
			mu.PrivateMemory = memInfo.getTotalPrivateDirty();
			
			aidsDBHelper.insertMemoryUsage(mu);
		}

	}
}
