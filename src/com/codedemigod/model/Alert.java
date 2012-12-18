package com.codedemigod.model;

public class Alert {
	public int ID;
	public long TimeStamp;
	public String Notes;

	public String toString() {
		return String.format("At %s notes %s", this.TimeStamp, this.Notes);
	}
}
