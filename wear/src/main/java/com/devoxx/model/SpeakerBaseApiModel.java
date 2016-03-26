package com.devoxx.model;

import java.io.Serializable;

public class SpeakerBaseApiModel implements Serializable {
	private String uuid;
	private String firstName;
	private String lastName;
	private String avatarURL;
	private String avatarImage;
	private String twitter;


	public String getName() {
		return String.format("%s %s", firstName, lastName);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAvatarURL() {
		return avatarURL;
	}

	public void setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}

	public String getAvatarImage() {
		return avatarImage;
	}

	public void setAvatarImage(String avatarImage) {
		this.avatarImage = avatarImage;
	}

	public String getTwitter() {
		return twitter;
	}

	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}
}
