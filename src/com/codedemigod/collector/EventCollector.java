package com.codedemigod.collector;

import com.codedemigod.aids.AIDSDBHelper;
import com.codedemigod.model.APackage;
import com.codedemigod.model.Event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class EventCollector extends BroadcastReceiver {
	private static final String TAG = EventCollector.class.getName(); 

	@Override
	public void onReceive(Context context, Intent intent) {
		AIDSDBHelper aidsDBHelper = AIDSDBHelper.getInstance(context);
		Event ev = new Event();
		ev.TimeStamp = System.currentTimeMillis();
		
		if(intent.getAction() == Intent.ACTION_SCREEN_ON){
			ev.Type = Event.Event_Type.SCREEN_ON;
			Log.i("EVENT", "SCREEN ON");
		}
		else if(intent.getAction() == Intent.ACTION_SCREEN_OFF){
			ev.Type = Event.Event_Type.SCREEN_OFF;
			Log.i("EVENT", "SCREEN OFF");
		}
		else if(intent.getAction() == Intent.ACTION_PACKAGE_ADDED){
			ev.Type = Event.Event_Type.APP_INSTALL;
			Log.i("EVENT", "APP INSTALL");
			
			try {
				String appInstall = intent.getDataString();
				String pkgName = appInstall.substring(8);
				PackageManager pkgMgr = context.getPackageManager();
				PackageInfo pInfo = pkgMgr.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
				
				APackage pkg = APackage.InstanceFromPackageInfo(pInfo);
				aidsDBHelper.insertPackage(pkg);
				
				Log.i(TAG, String.format("installed %s", pkg));
			}
			catch(Exception ex){
				ex.printStackTrace();
				Log.w(TAG, "Error in getting newly installed package");
			}
		}
		
		aidsDBHelper.insertEvent(ev);
	}


}
