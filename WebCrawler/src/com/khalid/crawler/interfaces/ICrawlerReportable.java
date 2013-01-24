package com.khalid.crawler.interfaces;

/**
 * Interface for reporting crawl 
 * information
*/
public interface ICrawlerReportable {

	/**
	 * function returns recent crawled URL
	*/
	public void spiderFoundURL(String url);
	
	/**
	 * function returns processed URLs count
	*/
	public void spiderURLProcessed(int processedURLCount);
	
	/**
	 * function returns total gathered URLs count
	*/
	public void spiderLinkCounts(int linkCount);
	
	/**
	 * function to notify when threads finish their task
	*/
	public void finished();
	

	
	
	
}
