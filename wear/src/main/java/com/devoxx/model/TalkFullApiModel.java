package com.devoxx.model;



import java.util.List;

public class TalkFullApiModel extends TalkBaseApiModel {
	private String lang;
	private String summary;
	private List<TalkSpeakerApiModel> speakers;

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
}
