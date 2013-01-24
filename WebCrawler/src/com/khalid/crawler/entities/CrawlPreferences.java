package com.khalid.crawler.entities;

public class CrawlPreferences {
	
	public CrawlPreferences(String seedURL, String searchString, boolean isDomainSpecific, int deapthLevel, String[] fileTypes,int filesize) {
	this.mSeedURL = seedURL;
	this.mSearchString = searchString;
	this.mIsDomainSpecific = isDomainSpecific;
	this.mDeapthLevel = deapthLevel;
	this.mMediaTypes = fileTypes;
	this.mFileSize = filesize * 1048576;
	}
	
	private String mSeedURL;
	private String mSearchString;
	private boolean mIsDomainSpecific;
	private int mDeapthLevel;
	private String[] mMediaTypes;
	private long mFileSize;
	
	public String getmSeedURL() {
		return mSeedURL;
	}
	public void setmSeedURL(String mSeedURL) {
		this.mSeedURL = mSeedURL;
	}
	public String getmSearchString() {
		return mSearchString;
	}
	public void setmSearchString(String mSearchString) {
		this.mSearchString = mSearchString;
	}
	public boolean ismIsDomainSpecific() {
		return mIsDomainSpecific;
	}
	public void setmIsDomainSpecific(boolean mIsDomainSpecific) {
		this.mIsDomainSpecific = mIsDomainSpecific;
	}
	public int getmDeapthLevel() {
		return mDeapthLevel;
	}
	public void setmDeapthLevel(int mDeapthLevel) {
		this.mDeapthLevel = mDeapthLevel;
	}
	public String[] getmMediaTypes() {
		return mMediaTypes;
	}
	public void setmMediaTypes(String[] mMediaTypes) {
		this.mMediaTypes = mMediaTypes;
	}
	
	
	
	

}
