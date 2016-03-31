package com.devoxx.model;



import java.util.ArrayList;
import java.util.List;

public class TalkFullApiModel extends TalkBaseApiModel {
	private boolean favorite;
	private String lang;
	private String summary;
	private List<TalkSpeakerApiModel> speakers;
	private String roomName;
	private Long fromTimeMillis;
	private Long toTimeMillis;


	public boolean getFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public List<TalkSpeakerApiModel> getSpeakers() {
		return speakers;
	}

	public void setSpeakers(List<TalkSpeakerApiModel> speakers) {
		this.speakers = speakers;
	}

	public void addSpeaker(TalkSpeakerApiModel speaker) {

		if (speaker == null) {
			return;
		}

		if (speakers == null) {
			speakers = new ArrayList<>();
		}

		speakers.add(speaker);
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public Long getFromTimeMillis() {
		return fromTimeMillis;
	}

	public void setFromTimeMillis(Long fromTimeMillis) {
		this.fromTimeMillis = fromTimeMillis;
	}

	public Long getToTimeMillis() {
		return toTimeMillis;
	}

	public void setToTimeMillis(Long toTimeMillis) {
		this.toTimeMillis = toTimeMillis;
	}
}
