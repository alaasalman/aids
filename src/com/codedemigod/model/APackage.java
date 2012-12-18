package com.codedemigod.model;

import java.util.Arrays;
import java.util.List;

import android.content.pm.PackageInfo;

public class APackage {
	public int ID;
	public long TimeStamp;
	public String Name;
	public long InstallTime;
	public long UpdateTime;
	public String[] RequestedPermissions;
	public String[] RequestedFeatures;
	public long FirstSeen;
	public Threat_Type Threat;
	public float Threat_Numeric;
	public String ProcessName;
	public String Uid;

	public enum Threat_Type {
		GREEN, YELLOW, ORANGE, RED
	};

	public static final String[] DANGEROUS_PERMISSIONS = new String[] {
			"android.permission.PROCESS_OUTGOING_CALLS",
			"com.android.browser.permission.READ_HISTORY_BOOKMARKS",
			"android.permission.READ_CONTACTS",
			"android.permission.PROCESS_OUTGOING_CALLS",
			"android.permission.INTERNET", "android.permission.CAMERA",
			"android.permission.CALL_PHONE", "android.permission.SEND_SMS",
			"android.permission.RECEIVE_SMS",
			"android.permission.READ_SOCIAL_STREAM",
			"android.permission.READ_SMS",
			"android.permission.READ_PHONE_STATE" };

	public String toString() {
		String pStr = String
				.format("At %s APackage[%s - p:%s uid:%s] installed at %s, updated at %s, first seen at %s, with threat of %s %s\n",
						this.TimeStamp, this.Name, this.ProcessName, this.Uid,
						this.InstallTime, this.UpdateTime, this.FirstSeen,
						this.Threat.name(), this.Threat_Numeric);

		StringBuilder sb = new StringBuilder(pStr);

		if (this.RequestedPermissions != null) {
			sb.append("Permissions:\n");
			for (String rP : this.RequestedPermissions) {
				sb.append(String.format("\t%s\n", rP));
			}
		}

		if (this.RequestedFeatures != null) {
			sb.append("Features:\n");
			for (String rF : this.RequestedFeatures) {
				sb.append(String.format("\t%s\n", rF));
			}
		}

		return sb.toString();
	}

	/*
	 * Creates an APackage instance from a PackageInfo. This will be used on IDS
	 * first launch and on app install event.
	 */
	public static APackage InstanceFromPackageInfo(PackageInfo pInfo) {
		APackage pkg = new APackage();

		pkg.TimeStamp = System.currentTimeMillis();
		pkg.Name = pInfo.packageName;
		pkg.InstallTime = pInfo.firstInstallTime;
		pkg.UpdateTime = pInfo.lastUpdateTime;
		pkg.Threat_Numeric = 0.0f;
		pkg.RequestedPermissions = pInfo.requestedPermissions;

		if (pkg.RequestedPermissions != null) {
			List<String> danPermissions = Arrays.asList(DANGEROUS_PERMISSIONS);

			// check if app is requesting any dangerous permission, for each one
			// add 0.05
			// TODO is this correct to treat each permission equally?
			for (String perm : pkg.RequestedPermissions) {
				if (danPermissions.contains(perm)) {
					pkg.Threat_Numeric += 0.05f;
				}
			}
		}

		if (pInfo.reqFeatures != null) {
			pkg.RequestedFeatures = new String[pInfo.reqFeatures.length];

			for (int i = 0; i < pkg.RequestedFeatures.length; i++) {
				pkg.RequestedFeatures[i] = pInfo.reqFeatures[i].name;
			}
		}

		pkg.Threat = Threat_Type.GREEN;
		
		if(pInfo.applicationInfo != null){
			pkg.Uid = String.valueOf(pInfo.applicationInfo.uid);
			pkg.ProcessName = pInfo.applicationInfo.processName;
		}
		
		return pkg;
	}
}
