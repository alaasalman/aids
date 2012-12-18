package com.codedemigod.collector;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import com.codedemigod.aids.AIDSDBHelper;
import com.codedemigod.aids.AINDSTask;
import com.codedemigod.model.Process;

public class ProcessCollector extends AINDSTask {

	public ProcessCollector(){
		this.RunEvery = 5;
	}
	
	public void doWork(Context context) {
		ActivityManager actManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningProcessList = actManager
				.getRunningAppProcesses();
		AIDSDBHelper aidsDBHelper = AIDSDBHelper.getInstance(context);
		
		// get running processes
		for (RunningAppProcessInfo pInfo : runningProcessList) {
			Process p = new Process();

			p.TimeStamp = System.currentTimeMillis();
			p.Uid = String.valueOf(pInfo.uid);
			p.Pid = String.valueOf(pInfo.pid);
			p.Name = pInfo.processName;
			
			aidsDBHelper.insertProcess(p);
		}
	}
}
