package com.devoxx.model;

import android.net.Uri;

public class TalkSpeakerApiModel extends SpeakerBaseApiModel {
	private String name;
	private LinkApiModel link;

	public static String getUuidFromLink(LinkApiModel link) {
		return Uri.parse(link.getHref()).getLastPathSegment();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LinkApiModel getLink() {
		return link;
	}

	public void setLink(LinkApiModel link) {
		this.link = link;
	}
}
