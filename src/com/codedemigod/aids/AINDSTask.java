package com.codedemigod.aids;

import android.content.Context;

public abstract class AINDSTask {
	public int RunEvery=1; //in seconds
	public int Checked=0;
	public abstract void doWork(Context context);
}
