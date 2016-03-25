package com.devoxx.model;

import java.io.Serializable;

public class LinkApiModel implements Serializable {
	private String href;
	private String rel;
	private String title;

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
