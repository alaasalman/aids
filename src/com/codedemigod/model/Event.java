package com.codedemigod.model;

public class Event {
	public int ID;
	public long TimeStamp;
	public Event_Type Type;
	public String More;

	public enum Event_Type {
		SCREEN_OFF, SCREEN_ON, APP_INSTALL
	};

	public String toString() {
		return String.format("At %s Event[%s] with %s", this.TimeStamp,
				this.Type, this.More);
	}
}
