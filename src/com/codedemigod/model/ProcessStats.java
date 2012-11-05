package com.codedemigod.model;

public class ProcessStats {
	public long TimeSnapshot;
	public String Name;
	public String Uid;
	public String CPUUsage;
	public long TxBytes;
	public long RxBytes;
	public long DiffTxBytes;
	public long DiffRxBytes;
	public int PSSMemory;
	public int SharedMemory;
	public int PrivateMemory;
	public int DiffPSSMemory;
	public int DiffSharedMemory;
	public int DiffPrivateMemory;

	public String toString() {
		return String
				.format("Stats at %s\nProcess %s-%s:\n\tCPU %s\n\tNetwork [Tx: %s - Rx: %s]\tNetwork Diff [Tx: %s - Rx: %s]\n\tMemory [PSS: %s Shared:%s Private: %s]\tMemory Diff [PSS: %s Shared: %s Private%s]",
						this.TimeSnapshot, this.Uid, this.Name, this.CPUUsage,
						this.TxBytes, this.RxBytes, this.DiffTxBytes,
						this.DiffRxBytes, this.PSSMemory, this.SharedMemory,
						this.PrivateMemory, this.DiffPSSMemory,
						this.DiffSharedMemory, this.DiffPrivateMemory);
	}
}
