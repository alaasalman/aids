package com.codedemigod.model;

public class NetworkUsage {
	public int ID;
	public long TimeStamp;
	public String Uid;
	public long TxBytes;
	public long TxPackets;
	public long TxTcpPackets;
	public long TxUdpPackets;
	public long RxBytes;
	public long RxPackets;
	public long RxTcpPackets;
	public long RxUdpPackets;
	public long DiffTxBytes;
	public long DiffRxBytes;

	public String toString() {
		return String
				.format("At %s Process[u:%s]n\tNetwork [Tx: %s - Rx: %s]\tNetwork Diff [Tx: %s - Rx: %s] Txp %s Txtp %s Txup %s Rxp %s Rxtp %s Rxup %s",
						this.TimeStamp, this.Uid, this.TxBytes, this.RxBytes,
						this.DiffTxBytes, this.DiffRxBytes, this.TxPackets,
						this.TxTcpPackets, this.TxUdpPackets, this.RxPackets,
						this.RxTcpPackets, this.RxUdpPackets);
	}
}
