package com.codedemigod.model;

public class CPUUsage {
	public int ID;
	public long TimeStamp;
	public String Pid;
	public String CPUUsage;

	public String toString() {
		return String.format("At %s Process[p:%s] using %s CPU",
				this.TimeStamp, this.Pid, this.CPUUsage);
	}
}
