package com.codedemigod.model;

public class Process {
	public int ID;
	public long TimeStamp;
	public String Name;
	public String Uid;
	public String Pid;

	public String toString() {
		return String.format("At %s Process[u:%s - n:%s] has ID %s", this.TimeStamp,
				this.Uid, this.Name, this.Pid);
	}
}
