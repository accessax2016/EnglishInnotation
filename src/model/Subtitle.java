package model;

import java.io.Serializable;

public class Subtitle implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String startTime;
	private String content;
	private Boolean isFavorite;
	
	public Subtitle(String startTime, String content, Boolean isFavorite) {
		super();
		this.startTime = startTime;
		this.content = content;
		this.isFavorite = isFavorite;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getIsFavorite() {
		return isFavorite;
	}

	public void setIsFavorite(Boolean isFavorite) {
		this.isFavorite = isFavorite;
	}
	
	

}
