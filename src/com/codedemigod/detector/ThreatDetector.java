package com.codedemigod.detector;

import java.util.Calendar;

import android.content.Context;

import com.codedemigod.aids.AIDSDBHelper;
import com.codedemigod.aids.AINDSTask;
import com.codedemigod.model.APackage;
import com.codedemigod.model.Alert;

/*
 * This is the watchdog, responsible for iterating over our packages
 * and issuing alerts when we exceed a threshold.
 */
public class ThreatDetector extends AINDSTask {

	private final String TAG = IEDetector.class.getName();

	public ThreatDetector(){
		this.RunEvery = 180;
	}
	
	@Override
	public void doWork(Context context) {
		AIDSDBHelper aidsDBHelper = AIDSDBHelper.getInstance(context);
		
		for(APackage pkg: aidsDBHelper.getPackage()){
			
			if(pkg.Threat_Numeric > 0.7){
				//package has high threat
				Alert al = new Alert();
				al.TimeStamp = Calendar.getInstance().getTimeInMillis();
				al.Notes = String.format(
						"High threat detected for package %s",
						pkg.Name);
				aidsDBHelper.insertAlert(al);
			}
		}

	}

}
