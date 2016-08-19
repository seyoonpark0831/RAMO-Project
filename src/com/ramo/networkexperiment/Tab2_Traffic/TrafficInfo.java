package com.ramo.networkexperiment.Tab2_Traffic;

import android.graphics.drawable.Drawable;

public class TrafficInfo {
	private Drawable appicon;
	private String appname;
	private String packageName;
	private int uid;
	private long app_total_up;
	private long app_total_down;
	private long app_mobile_up;
	private long app_mobile_down;
	private long app_wifi_up;
	private long app_wifi_down;
	
	
	public TrafficInfo() {
		super();
	}
	public TrafficInfo(Drawable appicon, String appname, String packageName, int uid, long app_total_up, long app_total_down, long app_mobile_up, long app_mobile_down, long app_wifi_up, long app_wifi_down) {
		super();
		this.appicon = appicon;
		this.appname = appname;
		this.packageName = packageName;
		this.uid = uid;
		this.app_total_up = app_total_up;
		this.app_total_down = app_total_down;
		this.app_mobile_up = app_mobile_up;
		this.app_mobile_down = app_mobile_down;
		this.app_wifi_up = app_wifi_up;
		this.app_wifi_down = app_wifi_down;
	}
	public Drawable getAppicon() {
		return appicon;
	}
	public void setAppicon(Drawable appicon) {
		this.appicon = appicon;
	}
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public long getTotalUp() {
		return app_total_up;
	}
	public void setTotalUp(long app_total_up) {
		this.app_total_up = app_total_up;
	}
	public long getTotalDown() {
		return app_total_down;
	}
	public void setTotalDown(long app_total_down) {
		this.app_total_down = app_total_down;
	}


	public long getMobileUp() {
		return app_mobile_up;
	}
	public void setMobileUp(long app_mobile_up) {
		this.app_mobile_up = app_mobile_up;
	}
	public long getMobileDown() {
		return app_mobile_down;
	}
	public void setMobileDown(long app_mobile_down) {
		this.app_mobile_down = app_mobile_down;
	}
	
	public long getWifiUp() {
		return app_wifi_up;
	}
	public void setWifiUp(long app_wifi_up) {
		this.app_wifi_up = app_wifi_up;
	}
	public long getWifiDown() {
		return app_wifi_down;
	}
	public void setWifiDown(long app_wifi_down) {
		this.app_wifi_down = app_wifi_down;
	}
	
	
	
	
	@Override
	public String toString() {
		return "TrafficInfo [appicon=" + appicon + ", appname=" + appname
				+ ", packageName=" + packageName + ", uid=" + uid + "]";
	}
	
	
}
