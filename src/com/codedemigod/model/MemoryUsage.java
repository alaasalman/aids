package com.codedemigod.model;

public class MemoryUsage {
	public int ID;
	public long TimeStamp;
	public String Pid;
	public int PSSMemory;
	public int SharedMemory;
	public int PrivateMemory;
	public int DiffPSSMemory;
	public int DiffSharedMemory;
	public int DiffPrivateMemory;

	public String toString() {
		return String
				.format("At %s Process[p:%s]\n\tMemory [PSS: %s Shared:%s Private: %s]\tMemory Diff [PSS: %s Shared: %s Private%s]",
						this.TimeStamp, this.Pid, this.PSSMemory,
						this.SharedMemory, this.PrivateMemory,
						this.DiffPSSMemory, this.DiffSharedMemory,
						this.DiffPrivateMemory);
	}
}
