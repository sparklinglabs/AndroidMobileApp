package com.devoxx.model;

import java.io.Serializable;

public class BreakApiModel implements Serializable {

	private String id;
	private String nameEN;
	private String nameFR;
	private RoomApiModel room;

	@Override
	public String toString() {
		return "BreakApiModel{" +
				"id='" + id + '\'' +
				", nameEN='" + nameEN + '\'' +
				", nameFR='" + nameFR + '\'' +
				", room=" + room +
				'}';
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNameEN() {
		return nameEN;
	}

	public void setNameEN(String nameEN) {
		this.nameEN = nameEN;
	}

	public String getNameFR() {
		return nameFR;
	}

	public void setNameFR(String nameFR) {
		this.nameFR = nameFR;
	}

	public RoomApiModel getRoom() {
		return room;
	}

	public void setRoom(RoomApiModel room) {
		this.room = room;
	}
}
