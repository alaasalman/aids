package com.codedemigod.model;

public class IEModel {
	public int ID;
	public long FromTimeStamp;
	public long ToTimeStamp;
	public String ProcessName;
	public int CPULow;
	public int CPUMid;
	public int CPUHigh;
	public int CPUCounter;
	public int RxBytes;
	public int TxBytes;
	public int Age;

	public String toString() {
		return String
				.format("Model from %s to %s for %s: CPU_Low %s CPU_Mid %s CPU_High %s count %s Rx %s Tx %s with Age %s",
						this.FromTimeStamp, this.ToTimeStamp, this.ProcessName,
						this.CPULow, this.CPUMid, this.CPUHigh,
						this.CPUCounter, this.RxBytes, this.TxBytes, this.Age);
	}
}
