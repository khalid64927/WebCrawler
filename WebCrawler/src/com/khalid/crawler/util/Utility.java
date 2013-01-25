package com.khalid.crawler.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.widget.Toast;

public class Utility {

	private Context mContext;
	public Utility(Context context) {
		this.mContext = context;
	}
	
	/**
	 * Added to Check if any Internet Connection (Wifi / Network Data
	 * Connection) is available or not.
	 * 
	 * @param context
	 * @return true if connnection is available
	 */
	public final static boolean isIntenetAvailable(Context context) {
		if ((!isNetworkAvailable(context))
				&& (!isWifiAvailable(context))) {
			Toast.makeText(context, "Internet Unavailable", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * Check Internet is available or not.
	 * 
	 * @param context
	 * @return
	 */
	private static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connec = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connec.getActiveNetworkInfo();
		if (ni != null && ni.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if Wi-Fi available or not. If not then doesn't show a pop-up.
	 * 
	 * @param context
	 * @return
	 */
	private static boolean isWifiAvailable(Context context) {
		WifiManager connec = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		ConnectivityManager connec1 = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = connec1.getNetworkInfo(1).getState();
		if (connec.isWifiEnabled()
				&& wifi.toString().equalsIgnoreCase("CONNECTED")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * validates text field
	 * 
	 * @param String url
	 * @param Context 
	 * @return
	 */
	public static boolean validatURL(String url, Context context) {
		boolean isValid = true;

		if (TextUtils.isEmpty(url)) {
			Toast.makeText(context, "Enter Seed URL", Toast.LENGTH_SHORT).show();
			isValid = false;
		}else if(!checkLink(url)){
			Toast.makeText(context, "Improper URL Please enter valid URL example \n http://www.wikipedia.org \n https://www.google.com ", Toast.LENGTH_LONG).show();
			isValid = false;
		}
		return isValid;
	}
	
	/**
	 * function which validate links 
	 * @param1 String: URL
	 * @return
	 */
	private static boolean checkLink(String link) {
		if (link.startsWith("http://"))
			return true;
		if (link.startsWith("https://"))
			return true;
		return false;
	}
	

}
