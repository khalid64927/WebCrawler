package com.khalid.crawler.queue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.khalid.crawler.interfaces.IPoolReportable;

import android.util.Log;


public class URLPool {

	private static URLPool mURLPool = null;
	public static final int POOL_LIMIT = 1000; 
	private IPoolReportable listener;

	private AtomicBoolean isPoolSizeReached = new AtomicBoolean();
	
	private Set<String> _URLPool = new HashSet<String>();
	private LinkedList<String> urlPoolList = new LinkedList<String>();
	private LinkedList<String> urlProcessedPoolList = new LinkedList<String>();
	
		
	public static URLPool getInstance(){
		if(mURLPool == null){
			mURLPool =  new URLPool();
			return mURLPool;
		}
		return mURLPool;
	}
	
	
	public boolean hasPoolSizeReached(){
		return isPoolSizeReached.get();
	}
	

	public LinkedList<String> getUrlPoolList() {
		return urlPoolList;
	}
	public LinkedList<String> getUrlProcessedPoolSet() {
		return urlProcessedPoolList;
	}
	

	public synchronized int getUrlPoolSize(){
		return _URLPool.size();
	}
	
	public synchronized int getUrlProcessedPoolSize(){
		return urlProcessedPoolList.size();
	}
	
	
	/**
	 * Clear all of the workloads.
	 */
	public void clear() {
		getUrlPoolList().clear();
		_URLPool.clear();
		getUrlProcessedPoolSet().clear();
	}
	
	

	public synchronized boolean push(String url){
		boolean isSucess = false;
		if(!isPoolSizeReached.get()){
		if(_URLPool.add(url)){
			urlPoolList.addLast(url);
			if(_URLPool.size() == POOL_LIMIT){
				listener.onPoolSizeReached();
				Log.e("limit reached ------"," ===== "+_URLPool.size());
				isPoolSizeReached.set(true);
			}
			isSucess = true;
		}
		
	}
		return isSucess;
	}
	
	public synchronized String pop(){
		String url = null;
		if(urlPoolList.size()!=0){
			if(!isPoolSizeReached.get()){
				url = urlPoolList.removeFirst();
				urlProcessedPoolList.add(url);
			}
		}
		return url;
	}
	

	public void setPoolListener(IPoolReportable lisetener)
	{
		this.listener = lisetener;
	}
	

}
