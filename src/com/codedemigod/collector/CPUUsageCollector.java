package com.codedemigod.collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

import com.codedemigod.aids.AIDSDBHelper;
import com.codedemigod.aids.AINDSTask;
import com.codedemigod.model.CPUUsage;

public class CPUUsageCollector extends AINDSTask {
	Pattern pidPattern = Pattern.compile("^\\s*(\\d+)\\s*");
	Pattern cpuUsagePattern = Pattern.compile("(\\d+)%");

	public CPUUsageCollector(){
		this.RunEvery = 5;
	}
	
	public void doWork(Context context) {
		AIDSDBHelper aidsDBHelper = AIDSDBHelper.getInstance(context);

		// get cpu usage for processes
		try {
			Process topProcess = Runtime.getRuntime().exec("top -n 1 -d 0");

			BufferedReader bufferedStream = new BufferedReader(
					new InputStreamReader(topProcess.getInputStream()));
			String tLine = null;

			bufferedStream.readLine(); // skip over 1st empty line
			bufferedStream.readLine(); // second empty line
			bufferedStream.readLine(); // skip over USER and SYSTEM CPU
			bufferedStream.readLine(); // skip over USER and NICE
			bufferedStream.readLine(); // skip over column titles

			while ((tLine = bufferedStream.readLine()) != null) {
				Matcher pidMatcher = pidPattern.matcher(tLine);
				Matcher cpuMatcher = cpuUsagePattern.matcher(tLine);

				if (!pidMatcher.find() || !cpuMatcher.find()) {
					// can't find pid or cpu usage, probably line we're not
					// interested in
					continue;
				}

				String pid = pidMatcher.group();
				String cpuUsage = cpuMatcher.group();

				if (tLine.contains("root")) {
					continue;
				}

				// int pidInt = Integer.parseInt(pid.replaceAll("\\s", ""));

				CPUUsage cu = new CPUUsage();
				cu.TimeStamp = System.currentTimeMillis();
				cu.Pid = pid.replaceAll("\\s", "");
				cu.CPUUsage = cpuUsage.substring(0, cpuUsage.length() - 1); //cpu usage minus the % char
				
				aidsDBHelper.insertCPUUsage(cu);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
