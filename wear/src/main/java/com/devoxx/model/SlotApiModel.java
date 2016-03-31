package com.devoxx.model;


public class SlotApiModel  {

	/* nullability is mutually exclusive with talk field */
	private BreakApiModel slotBreak;

	/* nullability is mutually exclusive with talk break */
	private TalkFullApiModel talk;

	private String roomId;
	private String roomSetup;
	private String toTime;
	private String fromTime;
	private String roomName;
	private String day;
	private boolean notAllocated;
	private long fromTimeMillis;
	private long toTimeMillis;
	private int roomCapacity;

	public BreakApiModel getSlotBreak() {
		return slotBreak;
	}

	public void setSlotBreak(BreakApiModel slotBreak) {
		this.slotBreak = slotBreak;
	}

	public TalkFullApiModel getTalk() {
		return talk;
	}

	public void setTalk(TalkFullApiModel talk) {
		this.talk = talk;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getRoomSetup() {
		return roomSetup;
	}

	public void setRoomSetup(String roomSetup) {
		this.roomSetup = roomSetup;
	}

	public String getToTime() {
		return toTime;
	}

	public void setToTime(String toTime) {
		this.toTime = toTime;
	}

	public String getFromTime() {
		return fromTime;
	}

	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public boolean isNotAllocated() {
		return notAllocated;
	}

	public void setNotAllocated(boolean notAllocated) {
		this.notAllocated = notAllocated;
	}

	public long getFromTimeMillis() {
		return fromTimeMillis;
	}

	public void setFromTimeMillis(long fromTimeMillis) {
		this.fromTimeMillis = fromTimeMillis;
	}

	public long getToTimeMillis() {
		return toTimeMillis;
	}

	public void setToTimeMillis(long toTimeMillis) {
		this.toTimeMillis = toTimeMillis;
	}

	public int getRoomCapacity() {
		return roomCapacity;
	}

	public void setRoomCapacity(int roomCapacity) {
		this.roomCapacity = roomCapacity;
	}
}
