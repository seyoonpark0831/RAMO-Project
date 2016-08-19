package com.ramo.networkexperiment.Tab2_Traffic;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.ramo.networkexperiment.SendMailTask;
import com.ramo.networkexperiment.Tab3_Sensor.MicrophoneInput;
import com.ramo.networkexperiment.Tab3_Sensor.MicrophoneInputListener;



public class NetworkTrackService extends Service implements MicrophoneInputListener {
		
	String folder_name;
	
	// Network Track Part
	public static int TYPE_WIFI = 1;
	public static int TYPE_MOBILE = 2;
	public static int TYPE_NOT_CONNECTED = 0;
	
	static String previous_status = "INITIAL_STATE";
	static String status = null;
	
	//public static int traffic_save_interval = 120;
	//public static String userId;
	String dbName_traffic;
	
	protected static final Context MainActivity = null;
	boolean mQuit;
	
	// SQLite
	NetTrafficDBHelper netDBHelper;
	NetTrafficTraceDBHelper netTraceDBHelper;
	
	String upload_dbName;
	
	
	
	
	// Sensor Part
	int count;
    String timestamp;
    String gps_lat;
    String gps_long;
    String light;
    String proximity;
    String sound_level;
    String gravity_x;
    String gravity_y;
    String gravity_z;
    //String battery_level;
    String battery_plug;
    String accel_sensor;
    String wifi;
	
    TextView txv_Count, txv_TimeStamp, txv_Gps_Lat, txv_Gps_Long, txv_Light, txv_Proximity, txv_Sound_Level, txv_Battery, txv_Accel_Sensor, txv_Wifi;
    
    boolean gps_flag;
    boolean light_flag;
    boolean proximity_flag;
    boolean sound_flag;
    
    
    
    // 센서 매니져
    SensorManager mSm;
    
    // 
    LocationManager mLocMan;
    String mProvider;
    
    LocationManager mLocMan_gps;
    LocationManager mLocMan_network;
    
    String lat_gps;
    String lon_gps;
    String lat_net;
    String lon_net;
    
    String pos_lat;
    String pos_lon;
    
    
    ConnectivityManager cm;

    
    
    // 소음 측정
    MicrophoneInput micInput;
    
    double mOffsetdB = 10;  // Offset for bar, i.e. 0 lit LEDs at 10 dB.
    // The Google ASR input requirements state that audio input sensitivity
    // should be set such that 90 dB SPL at 1000 Hz yields RMS of 2500 for
    // 16-bit samples, i.e. 20 * log_10(2500 / mGain) = 90.
    double mGain = 2500.0 / Math.pow(10.0, 90.0 / 20.0);
    // For displaying error in calibration.
    double mDifferenceFromNominal = 0.0;
    double mRmsSmoothed;  // Temporally filtered version of RMS.
    double mAlpha = 0.9;  // Coefficient of IIR smoothing filter for RMS.
    private int mSampleRate;  // The audio sampling rate to use.
    private int mAudioSource;  // The audio source to use.
    
    // SQLite 데이터 베이스
    SensorDBHelper sDBHelper;
    
    
    //int sensor_save_interval;
    //String userId;
    String dbName_sensor;
    int file_flag;
	
	
	
	
	
	
	
	public void onCreate() {
		super.onCreate();
		
		while (true) {
			if (com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()) != null && com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()) != null) {
				break;
			}
			
			try{Thread.sleep(1000);}catch (Exception e) {;}
		}
		
		//userId = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext());
		//traffic_save_interval = com.ramo.networkexperiment.MainActivity.getTrafficInterval(getBaseContext());
		
		
		//Toast.makeText(this,"NetService1\nPrevious : " + previous_status +"\nNew : "+status, Toast.LENGTH_SHORT).show();
		Log.d("STATUS","NetService1   Previous : " + previous_status +"   New : "+status);
		
		netDBHelper = new NetTrafficDBHelper(getBaseContext());
		SQLiteDatabase db;
		db = netDBHelper.getWritableDatabase();
		
		
		// Get running processes
		PackageManager pm  = this.getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		    
		
		
		
		
		//ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
				
		//List<RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();
		
		long up_total 		= 0;
		long down_total 	= 0;
		
		long up_mobile 		= 0;
		long down_mobile 	= 0;
		
		long up_wifi 		= 0;
		long down_wifi 		= 0;
		
		long db_up_mobile 	= 0;
		long db_down_mobile = 0;
		long db_up_wifi 	= 0;
		long db_down_wifi 	= 0;
		long db_up_current 	= 0;
		long db_down_current= 0;
		
		
		Iterator<ApplicationInfo> appIter = packages.iterator();

		while(appIter.hasNext())
		{
			ApplicationInfo appInfo = appIter.next();
			
			// Get UID of the selected process
			int uid = appInfo.uid;
			
			up_total 		= 0;
			down_total 		= 0;
			up_mobile 		= 0;
			down_mobile	 	= 0;
			up_wifi 		= 0;
			down_wifi	 	= 0;

			db_up_mobile 	= 0;
			db_down_mobile 	= 0;
			db_up_wifi 		= 0;
			db_down_wifi 	= 0;
			db_up_current 	= 0;
			db_down_current	= 0;
			
			
			// Get traffic data
			if (Build.VERSION.SDK_INT != 18) {
				// OS Version이 4.3이 아닌 경우 기존 메서드 사용
				up_total = TrafficStats.getUidTxBytes(uid);
				down_total = TrafficStats.getUidRxBytes(uid);
			} else {
				// OS Version이 4.3인 경우 우회 방법 사용
				up_total = getTxBytesManual(uid);
				down_total = getRxBytesManual(uid);
			}
			
			
			
			
			if (up_total == -1) {
				up_total = 0;
			}
			if (down_total == -1) {
				down_total = 0;
			}
			
			
			Cursor cursor = db.rawQuery("SELECT * FROM TRAFFIC_TABLE WHERE uid = '"+uid+"'", null);
			
			int count = 0;			
			try {
				cursor.moveToNext();
				count = cursor.getCount();
			} catch (Exception e) {
				count = 0;
			}
			
			//Log.d(null, "Count : "+count);
			
			if (up_total == 0 && down_total == 0) {
				
			} else {
				
				if (count == 0) {
					// TRAFFIC_TABLE에 데이터 ROW 존재하지 않는 경우
					
					// 패키지명으로 해당 앱 이름 뽑아오기
					String appname = new String();
					try {
						appname = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
					} catch (NameNotFoundException error) {
						//error.printStackTrace();
					}				
					
					//Log.d("DB INSERT",appname);
			                                                                                                                                                                                                                                                                          		
					// SQL 명령으로 삽입
			        db.execSQL("INSERT INTO TRAFFIC_TABLE VALUES ('"+uid+"',"+"'"+appInfo.processName+"'"+",'"+appname+"',"+"0"+","+"0"+","+"0"+","+"0"+","+up_total+","+down_total+");");
			        
				} else {
					// TRAFFIC_TABLE에 데이터 ROW 존재하는 경우
					
					
					while (cursor.isLast()) {
						db_up_mobile 	= cursor.getLong(cursor.getColumnIndex("up_mobile"));
						db_down_mobile 	= cursor.getLong(cursor.getColumnIndex("down_mobile"));
						db_up_wifi 		= cursor.getLong(cursor.getColumnIndex("up_wifi"));
						db_down_wifi 	= cursor.getLong(cursor.getColumnIndex("down_wifi"));
						db_up_current 	= cursor.getLong(cursor.getColumnIndex("up_current_traffic"));
						db_down_current	= cursor.getLong(cursor.getColumnIndex("down_current_traffic"));
						cursor.moveToNext();
					} 
					
					// 패키지명으로 해당 앱 이름 뽑아오기
					String appname = new String();
					try {
						appname = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
					} catch (NameNotFoundException error) {
						//error.printStackTrace();
					}	
					
					//Log.d("DB EXIST","Name : "+appname+", UP : "+db_up_current);
					
					
					
					if (previous_status == "INITIAL_STATE") {
						
						// 초기 상태 -> 다른 연결 상태 : 현재 트래픽 양만 업데이트
						db.execSQL("UPDATE TRAFFIC_TABLE SET up_current_traffic = '"+up_total+"', down_current_traffic = '"+down_total+"' WHERE uid = '"+uid+"';");
						
					} 
					
				}
				
			}
			
			cursor.close();
			
		}
		
		
		
		netDBHelper.close();	
		
		
		NetworkTrackService.setPreviousStatusString(getConnectivityStatusString(getBaseContext()));
		
		
		// 현재 연원일 시간 포맷 설정
		SimpleDateFormat formatter_hour = new SimpleDateFormat("HH");
        Date currentTime_hour = new Date();
        String dTime_hour = formatter_hour.format(currentTime_hour);
		
        int hour = Integer.parseInt(dTime_hour);
        hour = hour / 6;
        
        String data_directory = "";
        
        // 현재 연원일 시간 포맷 설정
		SimpleDateFormat formatter_date = new SimpleDateFormat("yyyyMMdd");
		Date currentTime_date = new Date();
		String dTime_date = formatter_date.format(currentTime_date);
        
		String database_directory = getApplicationContext().getDatabasePath("Setting.db").getParent();
        
        switch (hour) {
		case 0:
			data_directory = database_directory+"/"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_00";
			folder_name = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_00";
			break;
			
		case 1:
			data_directory = database_directory+"/"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_06";
			folder_name = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_06";
			break;
			
		case 2:
			data_directory = database_directory+"/"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_12";
			folder_name = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_12";
			break;
			
		case 3:
			data_directory = database_directory+"/"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_18";
			folder_name = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_18";
			break;

		default:
			break;
		}
		
		
        // 현재 연원일 시간 포맷 설정
 		SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd_HH)");
 		Date currentTime2 = new Date();
 		String dTime2 = formatter2.format(currentTime2);
		
        
        //dbName = "[Traffic]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
        dbName_traffic = data_directory+"/"+"[Traffic]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
		
        netTraceDBHelper = new NetTrafficTraceDBHelper(getBaseContext());
		
		//Toast.makeText(this,"NetService2\nPrevious : " + previous_status +"\nNew : "+status, Toast.LENGTH_SHORT).show();
		Log.d("STATUS","NetService2   Previous : " + previous_status +"   New : "+status);
		
		
		
		
		
		
		dbName_sensor = data_directory+"/"+"[Sensor]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
        
		sDBHelper = new SensorDBHelper(getBaseContext());
		
		gps_flag = false;
	    light_flag = false;
	    proximity_flag = false;
	    sound_flag = false;
		
		
		mSm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		//mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		//mProvider = mLocMan.getBestProvider(new Criteria(), true);
		
		mLocMan_gps = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mLocMan_network = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		
		// Here the micInput object is created for audio capture.
	    // It is set up to call this object to handle real time audio frames of
	    // PCM samples. The incoming frames will be handled by the
	    // processAudioFrame method below.
		
	    micInput = new MicrophoneInput(this);
		
	    cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    
	    
	    
	    //userId = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext());
		//sensor_save_interval = com.ramo.networkexperiment.MainActivity.getSensorInterval(getBaseContext());
	    
	    /*
	    sDBHelper = new SensorDBHelper(this);
	    
	    // 현재 연원일 시간 포맷 설정
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy.MM.dd_HH:mm");
        Date currentTime2 = new Date();
        String dTime2 = formatter2.format(currentTime2);
	    
	    dbName = "[Sensor]"+userId+"_"+dTime2+".db";
	    */
	    
		count = 0;
		
		
		lat_gps = "";
    	lon_gps = "";
    	lat_net = "";
    	lon_net = "";
    	pos_lat = "";
    	pos_lon = "";
		
    	mLocMan_gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, mListener_gps);
		mLocMan_network.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, mListener_network);
		
		
		
	}
	
	
	
	
	
	public void onDestroy() {
		super.onDestroy();
		
		String status = NetworkTrackService.getConnectivityStatusString(getBaseContext());
		String previous_status =  NetworkTrackService.getPreviousStatusString(getBaseContext());
		
		if (status != previous_status) {
			//Toast.makeText(context,"NetReceiver1\nPrevious : " + previous_status +"\nNew : "+status, Toast.LENGTH_SHORT).show();
			//Log.d("STATUS","NetReceiver1   Previous : " + previous_status +"   New : "+status);
			
			netDBHelper = new NetTrafficDBHelper(getBaseContext());
			SQLiteDatabase db;
			db = netDBHelper.getWritableDatabase();
			
			
			// Get running processes
			PackageManager pm  = this.getPackageManager();
			List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
			
			
			//ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
					
			//List<RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();
			
			long up_total 		= 0;
			long down_total 	= 0;
			
			long up_mobile 		= 0;
			long down_mobile 	= 0;
			
			long up_wifi 		= 0;
			long down_wifi 		= 0;
			
			long db_up_mobile 	= 0;
			long db_down_mobile = 0;
			long db_up_wifi 	= 0;
			long db_down_wifi 	= 0;
			long db_up_current 	= 0;
			long db_down_current= 0;
			
			
			Iterator<ApplicationInfo> appIter = packages.iterator();
			
			
			while(appIter.hasNext())
			{
				ApplicationInfo appInfo = appIter.next();
				
				// Get UID of the selected process
				int uid = appInfo.uid;
				
				up_total 		= 0;
				down_total 		= 0;
				up_mobile 		= 0;
				down_mobile	 	= 0;
				up_wifi 		= 0;
				down_wifi	 	= 0;

				db_up_mobile 	= 0;
				db_down_mobile 	= 0;
				db_up_wifi 		= 0;
				db_down_wifi 	= 0;
				db_up_current 	= 0;
				db_down_current	= 0;
				
				
				// Get traffic data
				if (Build.VERSION.SDK_INT != 18) {
					// OS Version이 4.3이 아닌 경우 기존 메서드 사용
					up_total = TrafficStats.getUidTxBytes(uid);
					down_total = TrafficStats.getUidRxBytes(uid);
				} else {
					// OS Version이 4.3인 경우 우회 방법 사용
					up_total = getTxBytesManual(uid);
					down_total = getRxBytesManual(uid);
				}
				
				if (up_total == -1) {
					up_total = 0;
				}
				if (down_total == -1) {
					down_total = 0;
				}
				
				
				Cursor cursor = db.rawQuery("SELECT * FROM TRAFFIC_TABLE WHERE uid = '"+uid+"'", null);
				
				int count = 0;			
				try {
					cursor.moveToNext();
					count = cursor.getCount();
				} catch (Exception e) {
					count = 0;
				}
				
				//Log.d(null, "Count : "+count);
				
				if (up_total == 0 && down_total == 0) {
					
				} else {
					
					if (count == 0) {
						// TRAFFIC_TABLE에 데이터 ROW 존재하지 않는 경우
						
						// 패키지명으로 해당 앱 이름 뽑아오기
						String appname = new String();
						try {
							appname = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
						} catch (NameNotFoundException error) {
							//error.printStackTrace();
						}				
						
						Log.d("DB INSERT",appname);
						
						// SQL 명령으로 삽입
				        db.execSQL("INSERT INTO TRAFFIC_TABLE VALUES ('"+uid+"',"+"'"+appInfo.processName+"'"+",'"+appname+"',"+"0"+","+"0"+","+"0"+","+"0"+","+up_total+","+down_total+");");
				        
					} else {
						// TRAFFIC_TABLE에 데이터 ROW 존재하는 경우
						
						
						while (cursor.isLast()) {
							db_up_mobile 	= cursor.getLong(cursor.getColumnIndex("up_mobile"));
							db_down_mobile 	= cursor.getLong(cursor.getColumnIndex("down_mobile"));
							db_up_wifi 		= cursor.getLong(cursor.getColumnIndex("up_wifi"));
							db_down_wifi 	= cursor.getLong(cursor.getColumnIndex("down_wifi"));
							db_up_current 	= cursor.getLong(cursor.getColumnIndex("up_current_traffic"));
							db_down_current	= cursor.getLong(cursor.getColumnIndex("down_current_traffic"));
							cursor.moveToNext();
						}
						
						// 패키지명으로 해당 앱 이름 뽑아오기
						String appname = new String();
						try {
							appname = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
						} catch (NameNotFoundException error) {
							//error.printStackTrace();
						}	
						
						//Log.d("DB EXIST","Name : "+appname+", UP : "+db_up_current);
						
						
						
						
						if (previous_status == "MOBILE") {
						
							// 이전 상태가 MOBILE 이었을 경우 : 지금까지 트래픽 양을 MOBILE로 합
							up_mobile = up_total - db_up_current + db_up_mobile; 
							down_mobile = down_total - db_down_current + db_down_mobile;
							
							Log.d("DB EXIST","Name : "+appname+", UP_M : "+up_mobile+"  DOWN_M : "+down_mobile);
							
							db.execSQL("UPDATE TRAFFIC_TABLE SET up_mobile = '"+up_mobile+"', down_mobile = '"+down_mobile+"', up_current_traffic = '"+up_total+"', down_current_traffic = '"+down_total+"' WHERE uid = '"+uid+"';");
							
						} else if (previous_status == "WIFI") {
							
							// 이전 상태가 WIFI 이었을 경우 : 지금까지 트래픽 양을 WIFI로 합
							up_wifi = up_total - db_up_current + db_up_wifi; 
							down_wifi = down_total - db_down_current + db_down_wifi;
							
							Log.d("DB EXIST","Name : "+appname+", UP_W : "+up_wifi+"  DOWN_W : "+down_wifi);
							
							db.execSQL("UPDATE TRAFFIC_TABLE SET up_wifi = '"+up_wifi+"', down_wifi = '"+down_wifi+"', up_current_traffic = '"+up_total+"', down_current_traffic = '"+down_total+"' WHERE uid = '"+uid+"';");
							
						}
						
					}
					
				}
				
				cursor.close();
				
			}
			
			
			netDBHelper.close();	
			
			NetworkTrackService.setPreviousStatusString(status);
			
			previous_status =  NetworkTrackService.getPreviousStatusString(getBaseContext());
			
			//Toast.makeText(context,"NetReceiver2\nPrevious : " + previous_status +"\nNew : "+status, Toast.LENGTH_SHORT).show();
			//Log.d("STATUS","NetReceiver2   Previous : " + previous_status +"   New : "+status);
		}
		
		
		//Toast.makeText(this, "Service End", 0).show();
		
		mSm.unregisterListener(mSensorListener);
		
		
		mLocMan_gps.removeUpdates(mListener_gps);
		mLocMan_network.removeUpdates(mListener_network);
		
		mQuit = true;
		
	}
	
	public int onStartCommand (Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		//Toast.makeText(this, "Service Start!", 0).show();
		mQuit = false;
				
		NetworkThread thread = new NetworkThread(this, mHandler);
		thread.start();
		return START_STICKY;
	}
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	

	public static int getConnectivityStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (null != activeNetwork) {
			if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
				return TYPE_WIFI;
			
			if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
				return TYPE_MOBILE;
		} 
		return TYPE_NOT_CONNECTED;
	}
	
	public static String getConnectivityStatusString(Context context) {
		int conn = NetworkTrackService.getConnectivityStatus(context);
		//String status = null;
		if (conn == NetworkTrackService.TYPE_WIFI) {
			status = "WIFI";
		} else if (conn == NetworkTrackService.TYPE_MOBILE) {
			status = "MOBILE";
		} else if (conn == NetworkTrackService.TYPE_NOT_CONNECTED) {
			status = "NOT";
		}
		return status;
	}
	
	public static String getPreviousStatusString(Context context) {
		return previous_status;
	}
	
	public static void setPreviousStatusString(String string) {
		previous_status = string;
	}
	
	
	class NetTrafficDBHelper extends SQLiteOpenHelper {
    	public NetTrafficDBHelper(Context context) {
    		super(context, "NetTraffic.db", null, 1);
    	}
    	
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("CREATE TABLE TRAFFIC_TABLE ( uid TEXT PRIMARY KEY , package_name TEXT, app_name TEXT, up_mobile TEXT, down_mobile TEXT, up_wifi TEXT, down_wifi TEXT, up_current_traffic TEXT, down_current_traffic TEXT);");
    	}
    	
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		//db.execSQL("DROP TABLE IF EXISTS SENSOR_TABLE");
    		//onCreate(db);
    	}
    }
	
	
	class NetTrafficTraceDBHelper extends SQLiteOpenHelper {
    	public NetTrafficTraceDBHelper(Context context) {
    		super(context, dbName_traffic, null, 1);
    		//Toast.makeText(getBaseContext(), "S_DB : "+dbName, 0).show();
    	}
    	
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("CREATE TABLE TRAFFIC_TRACE_TABLE (  _id INTEGER PRIMARY KEY AUTOINCREMENT, uid TEXT , package_name TEXT, app_name TEXT, mode TEXT);");
    		db.execSQL("INSERT INTO TRAFFIC_TRACE_TABLE (_id, uid, package_name, app_name, mode) VALUES ("+null+",'0',"+"'SystemTotal'"+",'SystemTotal'"+",'UPLOAD'"+");");
    		db.execSQL("INSERT INTO TRAFFIC_TRACE_TABLE (_id, uid, package_name, app_name, mode) VALUES ("+null+",'0',"+"'SystemTotal'"+",'SystemTotal'"+",'DOWNLOAD'"+");");
    		
    	}
    	
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		//db.execSQL("DROP TABLE IF EXISTS SENSOR_TABLE");
    		//onCreate(db);
    	}
    }
	
	
	class SensorDBHelper extends SQLiteOpenHelper {
    	public SensorDBHelper(Context context) {
    		//super(context, "SensorData.db", null, 1);
    		super(context, dbName_sensor, null, 1);
    	}
    	
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("CREATE TABLE SENSOR_TABLE ( _id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp TEXT, gps_lat TEXT, gps_long TEXT, light TEXT, proximity TEXT, sound TEXT, gravity_x TEXT, gravity_y TEXT, gravity_z TEXT, battery_level TEXT, battery_plug TEXT, wifi TEXT);");
    		
    	}
    	
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		//db.execSQL("DROP TABLE IF EXISTS SENSOR_TABLE");
    		//onCreate(db);
    	}
    }
	
	
	
	class SettingDBHelper extends SQLiteOpenHelper {
    	public SettingDBHelper(Context context) {
    		super(context, "Setting.db", null, 1);
    		Log.d("DB Make", "DB CREATE");
    	}
    	
    	
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("CREATE TABLE SETTING_TABLE ( _id INTEGER PRIMARY KEY AUTOINCREMENT , user_id TEXT, sensor_interval TEXT, traffic_interval TEXT, last_sensor_db_for_upload TEXT, last_traffic_db_for_upload TEXT);");
    		Log.d("DB Make", "TABLE CREATE");
    	}
    	
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		//db.execSQL("DROP TABLE IF EXISTS SENSOR_TABLE");
    		//onCreate(db);
    	}
    }
	
	
	
	class NetworkThread extends Thread {
		NetworkTrackService mParent;
		Handler mHandler;
		
		int delay = SensorManager.SENSOR_DELAY_UI;
		
		public NetworkThread(NetworkTrackService parent, Handler handler) {
			mParent = parent;
			mHandler = handler;
		}
		
		
		
		public void run() {
			
			while(mQuit == false) {
				
				
				mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_LIGHT), delay);
		    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_PROXIMITY), delay);
		    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delay);
		    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), delay);
		    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GRAVITY), delay);
				
		    	/*
		    	lat_gps = "";
		    	lon_gps = "";
		    	lat_net = "";
		    	lon_net = "";
		    	pos_lat = "";
		    	pos_lon = "";
		    	*/
		    	
		    	
		    	Message msg = new Message();
				msg.what = 0;
				msg.obj = "";
				mHandler.sendMessage(msg);
		    	
				
				try{Thread.sleep(com.ramo.networkexperiment.MainActivity.getTrafficInterval(getBaseContext()) * 1000);}catch (Exception e) {;}
				//try{Thread.sleep(5000);}catch (Exception e) {;}
				
				count++;
			}
			
		}
	}
	
	
	Handler mHandler = new Handler() {
		
		SQLiteDatabase db_net;
		SQLiteDatabase db_nettrace;
		
		public void handleMessage (Message msg) {
			if (msg.what == 0) {
				
				// 현재 연원일 시간 포맷 설정
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		        Date currentTime = new Date();
		        String dTime = formatter.format(currentTime);
				
				if (status != "INITIAL_STATE") {
					//Toast.makeText(getBaseContext(),"Net Traffic DB : " + previous_status, Toast.LENGTH_SHORT).show();
					Log.d("STATUS","NetTrackService1   Previous : " + previous_status +"   New : "+status);
					
					netDBHelper = new NetTrafficDBHelper(getBaseContext());
					SQLiteDatabase db;
					db = netDBHelper.getWritableDatabase();
					
					
					// Get running processes
					PackageManager pm  = getPackageManager();
					List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
					
					
					//ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
							
					//List<RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();
					
					long up_total 		= 0;
					long down_total 	= 0;
					
					long up_mobile 		= 0;
					long down_mobile 	= 0;
					
					long up_wifi 		= 0;
					long down_wifi 		= 0;
					
					long db_up_mobile 	= 0;
					long db_down_mobile = 0;
					long db_up_wifi 	= 0;
					long db_down_wifi 	= 0;
					long db_up_current 	= 0;
					long db_down_current= 0;
					
					
					Iterator<ApplicationInfo> appIter = packages.iterator();
					
					
					while(appIter.hasNext())
					{
						ApplicationInfo appInfo = appIter.next();
						
						// Get UID of the selected process
						int uid = appInfo.uid;
						
						up_total 		= 0;
						down_total 		= 0;
						up_mobile 		= 0;
						down_mobile	 	= 0;
						up_wifi 		= 0;
						down_wifi	 	= 0;

						db_up_mobile 	= 0;
						db_down_mobile 	= 0;
						db_up_wifi 		= 0;
						db_down_wifi 	= 0;
						db_up_current 	= 0;
						db_down_current	= 0;
						
						
						// Get traffic data
						if (Build.VERSION.SDK_INT != 18) {
							// OS Version이 4.3이 아닌 경우 기존 메서드 사용
							up_total = TrafficStats.getUidTxBytes(uid);
							down_total = TrafficStats.getUidRxBytes(uid);
						} else {
							// OS Version이 4.3인 경우 우회 방법 사용
							up_total = getTxBytesManual(uid);
							down_total = getRxBytesManual(uid);
						}
						
						if (up_total == -1) {
							up_total = 0;
						}
						if (down_total == -1) {
							down_total = 0;
						}
						
						
						Cursor cursor = db.rawQuery("SELECT * FROM TRAFFIC_TABLE WHERE uid = '"+uid+"'", null);
						
						int count = 0;			
						try {
							cursor.moveToNext();
							count = cursor.getCount();
						} catch (Exception e) {
							count = 0;
						}
						
						//Log.d(null, "Count : "+count);
						
						if (up_total == 0 && down_total == 0) {
							
						} else {
							
							if (count == 0) {
								// TRAFFIC_TABLE에 데이터 ROW 존재하지 않는 경우
								
								// 패키지명으로 해당 앱 이름 뽑아오기
								String appname = new String();
								try {
									appname = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
								} catch (NameNotFoundException error) {
									//error.printStackTrace();
								}				
								
								Log.d("DB INSERT",appname);
								
								// SQL 명령으로 삽입
						        db.execSQL("INSERT INTO TRAFFIC_TABLE VALUES ('"+uid+"',"+"'"+appInfo.processName+"'"+",'"+appname+"',"+"0"+","+"0"+","+"0"+","+"0"+","+up_total+","+down_total+");");
						        
							} else {
								// TRAFFIC_TABLE에 데이터 ROW 존재하는 경우
								
								
								while (cursor.isLast()) {
									db_up_mobile 	= cursor.getLong(cursor.getColumnIndex("up_mobile"));
									db_down_mobile 	= cursor.getLong(cursor.getColumnIndex("down_mobile"));
									db_up_wifi 		= cursor.getLong(cursor.getColumnIndex("up_wifi"));
									db_down_wifi 	= cursor.getLong(cursor.getColumnIndex("down_wifi"));
									db_up_current 	= cursor.getLong(cursor.getColumnIndex("up_current_traffic"));
									db_down_current	= cursor.getLong(cursor.getColumnIndex("down_current_traffic"));
									cursor.moveToNext();
								}
								
								// 패키지명으로 해당 앱 이름 뽑아오기
								String appname = new String();
								try {
									appname = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
								} catch (NameNotFoundException error) {
									//error.printStackTrace();
								}	
								
								//Log.d("DB EXIST","Name : "+appname+", UP : "+db_up_current);
								
								
								
								
								if (previous_status == "MOBILE") {
								
									// 이전 상태가 MOBILE 이었을 경우 : 지금까지 트래픽 양을 MOBILE로 합
									up_mobile = up_total - db_up_current + db_up_mobile; 
									down_mobile = down_total - db_down_current + db_down_mobile;
									
									Log.d("DB EXIST","Name : "+appname+", UP_M : "+up_mobile+"  DOWN_M : "+down_mobile);
									
									db.execSQL("UPDATE TRAFFIC_TABLE SET up_mobile = '"+up_mobile+"', down_mobile = '"+down_mobile+"', up_current_traffic = '"+up_total+"', down_current_traffic = '"+down_total+"' WHERE uid = '"+uid+"';");
									
								} else if (previous_status == "WIFI") {
									
									// 이전 상태가 WIFI 이었을 경우 : 지금까지 트래픽 양을 WIFI로 합
									up_wifi = up_total - db_up_current + db_up_wifi; 
									down_wifi = down_total - db_down_current + db_down_wifi;
									
									Log.d("DB EXIST","Name : "+appname+", UP_W : "+up_wifi+"  DOWN_W : "+down_wifi);
									
									db.execSQL("UPDATE TRAFFIC_TABLE SET up_wifi = '"+up_wifi+"', down_wifi = '"+down_wifi+"', up_current_traffic = '"+up_total+"', down_current_traffic = '"+down_total+"' WHERE uid = '"+uid+"';");
									
								}
								
							}
							
						}
						
						cursor.close();
						
					}
					
					
					
						
					
					netDBHelper.close();	
					
					NetworkTrackService.setPreviousStatusString(status);
					
					
					//Toast.makeText(context,"NetReceiver2\nPrevious : " + previous_status +"\nNew : "+status, Toast.LENGTH_SHORT).show();
					Log.d("STATUS","NetTrackService2   Previous : " + previous_status +"   New : "+status);
				}
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				// 현재 연원일 시간 포맷 설정
				SimpleDateFormat formatter_hour = new SimpleDateFormat("HH");
		        Date currentTime_hour = new Date();
		        String dTime_hour = formatter_hour.format(currentTime_hour);
				
		        int hour = Integer.parseInt(dTime_hour);
		        hour = hour / 6;
		        
		        String data_directory = "";
		        
		        // 현재 연원일 시간 포맷 설정
				SimpleDateFormat formatter_date = new SimpleDateFormat("yyyyMMdd");
				Date currentTime_date = new Date();
				String dTime_date = formatter_date.format(currentTime_date);
		        
				String database_directory = getApplicationContext().getDatabasePath("Setting.db").getParent();
		        
				switch (hour) {
				case 0:
					data_directory = database_directory+"/"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_00";
					folder_name = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_00";
					break;
					
				case 1:
					data_directory = database_directory+"/"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_06";
					folder_name = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_06";
					break;
					
				case 2:
					data_directory = database_directory+"/"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_12";
					folder_name = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_12";
					break;
					
				case 3:
					data_directory = database_directory+"/"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_18";
					folder_name = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime_date+"_18";
					break;

				default:
					break;
				}
				
				
				
				
				
		        // 현재 연원일 시간 포맷 설정
				SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd_HH)");
		        Date currentTime2 = new Date();
		        String dTime2 = formatter2.format(currentTime2);
		        
		        //dbName = "[Traffic]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
		        dbName_traffic = data_directory+"/"+"[Traffic]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
				
		        netTraceDBHelper = new NetTrafficTraceDBHelper(getBaseContext());
		        
		        db_nettrace = netTraceDBHelper.getWritableDatabase();
		        
		        
		        
		        
		        
		        netDBHelper = new NetTrafficDBHelper(getBaseContext());
		        
		        db_net = netDBHelper.getReadableDatabase();
		        
		        
		        // NetTraffic.db 에서 새로 추가된 Application 있는지 확인, 있으면 ROW 추가
		        Cursor cursor_net = db_net.rawQuery("SELECT uid, package_name, app_name FROM TRAFFIC_TABLE", null);
				
				int count_net = 0;
				try {
					count_net = cursor_net.getCount();
				} catch (Exception e) {
					count_net = 0;
				}
				
				
				if (count_net == 0) {
					// TRAFFIC_TABLE에 데이터 ROW 존재하지 않는 경우
					
				} else {
					// TRAFFIC_TABLE에 데이터 ROW 존재하는 경우
					
					
					while (cursor_net.moveToNext() == true) {
						int db_uid = cursor_net.getInt(cursor_net.getColumnIndex("uid"));
						String db_package_name = cursor_net.getString(cursor_net.getColumnIndex("package_name"));
						String db_app_name = cursor_net.getString(cursor_net.getColumnIndex("app_name"));
						
						Cursor cursor_nettrace = db_nettrace.rawQuery("SELECT uid FROM TRAFFIC_TRACE_TABLE WHERE uid = '"+db_uid+"'", null);
						
						int count_nettrace = 0;			
						try {
							count_nettrace = cursor_nettrace.getCount();
						} catch (Exception e) {
							count_nettrace = 0;
						}
						
						
						if (count_nettrace == 0) {
							// TRAFFIC_TRACE_TABLE에 데이터 ROW 존재하지 않는 경우
							// SQL 명령으로 삽입
					        db_nettrace.execSQL("INSERT INTO TRAFFIC_TRACE_TABLE (_id, uid, package_name, app_name, mode) VALUES ("+null+",'"+db_uid+"',"+"'"+db_package_name+"'"+",'"+db_app_name+"'"+",'m_up'"+");");
					        db_nettrace.execSQL("INSERT INTO TRAFFIC_TRACE_TABLE (_id, uid, package_name, app_name, mode) VALUES ("+null+",'"+db_uid+"',"+"'"+db_package_name+"'"+",'"+db_app_name+"'"+",'m_down'"+");");
					        db_nettrace.execSQL("INSERT INTO TRAFFIC_TRACE_TABLE (_id, uid, package_name, app_name, mode) VALUES ("+null+",'"+db_uid+"',"+"'"+db_package_name+"'"+",'"+db_app_name+"'"+",'w_up'"+");");
					        db_nettrace.execSQL("INSERT INTO TRAFFIC_TRACE_TABLE (_id, uid, package_name, app_name, mode) VALUES ("+null+",'"+db_uid+"',"+"'"+db_package_name+"'"+",'"+db_app_name+"'"+",'w_down'"+");");
						}
					}
				}
				
				cursor_net.close();
				
				// 시간별로 트래픽 양 기록 Traffic DB 업데이트
				
				/*
				// 현재 연원일 시간 포맷 설정
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		        Date currentTime = new Date();
		        String dTime = formatter.format(currentTime);
				*/
				
				
				
				db_nettrace.execSQL("ALTER TABLE TRAFFIC_TRACE_TABLE ADD COLUMN '"+dTime+"' TEXT"); // 새로운 시간 열(Column) 추가
				
				cursor_net = db_net.rawQuery("SELECT * FROM TRAFFIC_TABLE", null);
				
				count_net = 0;
				try {
					count_net = cursor_net.getCount();
				} catch (Exception e) {
					count_net = 0;
				}
				
				
				if (count_net == 0) {
					// TRAFFIC_TABLE에 데이터 ROW 존재하지 않는 경우
					
				} else {
					// TRAFFIC_TABLE에 데이터 ROW 존재하는 경우
					
					long sum_upload = 0;
					long sum_download = 0;
					
					while (cursor_net.moveToNext() == true) {
						int db_uid = cursor_net.getInt(cursor_net.getColumnIndex("uid"));
						long db_up_mobile 	= cursor_net.getLong(cursor_net.getColumnIndex("up_mobile"));
						long db_down_mobile 	= cursor_net.getLong(cursor_net.getColumnIndex("down_mobile"));
						long db_up_wifi 		= cursor_net.getLong(cursor_net.getColumnIndex("up_wifi"));
						long db_down_wifi 	= cursor_net.getLong(cursor_net.getColumnIndex("down_wifi"));
						
						sum_upload = sum_upload + db_up_mobile + db_up_wifi;
						sum_download = sum_download + db_down_mobile + db_down_wifi;
						
						db_nettrace.execSQL("UPDATE TRAFFIC_TRACE_TABLE SET '"+dTime+"' = '"+db_up_mobile+"' WHERE uid = '"+db_uid+"' AND mode = 'm_up'");
						db_nettrace.execSQL("UPDATE TRAFFIC_TRACE_TABLE SET '"+dTime+"' = '"+db_down_mobile+"' WHERE uid = '"+db_uid+"' AND mode = 'm_down'");
						db_nettrace.execSQL("UPDATE TRAFFIC_TRACE_TABLE SET '"+dTime+"' = '"+db_up_wifi+"' WHERE uid = '"+db_uid+"' AND mode = 'w_up'");
						db_nettrace.execSQL("UPDATE TRAFFIC_TRACE_TABLE SET '"+dTime+"' = '"+db_down_wifi+"' WHERE uid = '"+db_uid+"' AND mode = 'w_down'");
						
					}
					
					db_nettrace.execSQL("UPDATE TRAFFIC_TRACE_TABLE SET '"+dTime+"' = '"+sum_upload+"' WHERE _id = '1'");
					db_nettrace.execSQL("UPDATE TRAFFIC_TRACE_TABLE SET '"+dTime+"' = '"+sum_download+"' WHERE _id = '2'");
					
				}
				
				cursor_net.close();
				
				db_nettrace.close();				
				db_net.close();
				
				
				
				Tab2_TrafficActivity.updateTrafficTime(getBaseContext());
				
				
				
				
				// 배터리 잔량 체크하는 코드
				Intent bat = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				int battery_level = bat.getIntExtra("level", -1);
				int battery_plug = bat.getIntExtra("plugged", -1);
				if (battery_plug == 0) {
					NetworkTrackService.this.battery_plug = "No Charging";
				} else {
					NetworkTrackService.this.battery_plug = "Charging";
				}
				
				//Toast.makeText(SensorService.this,"배터리 충전 : "+SensorService.this.battery_plug+"\n배터리 레벨 : "+battery_level, 0).show();
				
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n" + light + "\n" + proximity + "\n" + accel_sensor, 0).show();
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n배터리 : "+battery, 0).show();
				
				
				// GPS 좌표 갱신 (업데이트 종료는 GPS 업데이터 된 메서드에서 바로 한다)
				//mLocMan.requestLocationUpdates(mProvider, 0, 0, mListener);
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n위도 : "+gps_lat+"\n경도 : "+gps_long, 0).show();
				
				
				
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n중력 : "+gravity, 0).show();
				
				while(true) {
					readPreferences();
			        micInput.setSampleRate(mSampleRate);
			        micInput.setAudioSource(mAudioSource);
			        micInput.start();
			        try{Thread.sleep(2000);}catch (Exception e) {;}
			        break;
				}
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n소음 : "+sound_level+"dB", 0).show();
				micInput.stop();
				
				
				boolean wifi_flag = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
				
				if (wifi_flag) {
					wifi = "ON";
				} else {
					wifi = "OFF";
				}
				
				
				
		        
		        
	    		// 현재 연원일 시간 포맷 설정
				//SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		        //Date currentTime = new Date();
		        //String dTime = formatter.format(currentTime);
		        
		        
		        
		        
		        
				
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n"+wifi, 0).show();
		        
		        //Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n"+dTime, 0).show();
		        
		        //Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n"+gravity, 0).show();
		        
		        /*
		        while(gps_flag == false || sound_flag == false || light_flag == false || proximity_flag == false) {
		        	if (gps_flag && sound_flag && light_flag && proximity_flag) {
		        		break;
		        	}
		        }
		        */
		        
		        
		        /*
		        if (gps_flag && sound_flag && light_flag && proximity_flag) {
		        	db = sDBHelper.getWritableDatabase();
		    		// SQL 명령으로 삽입
		    		db.execSQL("INSERT INTO SENSOR_TABLE VALUES (null,"+dTime+","+gps_lat+","+gps_long+","+light+","+proximity+","+sound_level+","+battery+","+wifi+");");
		    		sDBHelper.close();
		    		Toast.makeText(SensorService.this,"DB Input : "+dTime+","+gps_lat+","+gps_long+","+light+","+proximity+","+sound_level+","+battery+","+wifi+");" , 0).show();
		        } else {
		        	//Toast.makeText(SensorService.this,"DB Input Fail"+"\ngps : "+gps_flag+"\nsound : "+sound_flag+"\nlight : "+light_flag+"\nproximity : "+proximity_flag+"\n\n"+dTime+","+gps_lat+","+gps_long+","+light+","+proximity+","+sound_level+","+battery+","+wifi , 0).show();
		        	//Toast.makeText(SensorService.this,"DB Input Fail"+"\ngps : "+gps_flag+"\nsound : "+sound_flag+"\nlight : "+light_flag+"\nproximity : "+proximity_flag , 0).show();
		        }
		        */
		        
	    		/*
	    		String DB_NAME = "SensorData.db";
	    		String dbPath = getApplicationContext().getDatabasePath(DB_NAME).getParent();
	    		String file_path = dbPath+"/"+DB_NAME;
	    		File f = new File(file_path);
	    		f.delete();
	    		if(f.canRead()) {
	    			Toast.makeText(SensorService.this,"파일 Exist\n"+f.getAbsolutePath(), 0).show();
	    		} else {
	    			Toast.makeText(SensorService.this,"파일 Not Exist", 0).show();
	    		}
	    		*/
	    		
		        
		        
		        
	    		// 현재 연원일 시간 포맷 설정
				//SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd_HH)");
		        //Date currentTime2 = new Date();
		        //String dTime2 = formatter2.format(currentTime2);
		        
				dbName_sensor = data_directory+"/"+"[Sensor]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
		        
		        String DB_NAME = dbName_sensor;
	    		String dbPath = getApplicationContext().getDatabasePath(DB_NAME).getParent();
	    		String file_path = dbPath+"/"+DB_NAME;
	    		File f = new File(file_path);
	    		//f.delete();
	    		
	    		if(f.canRead()) {
	    			//Toast.makeText(SensorService.this,"파일 Exist\n"+f.getAbsolutePath(), 0).show();
	    		} else {
	    			//Toast.makeText(SensorService.this,"파일 Not Exist", 0).show();
	    			
	    		}
	    		
	    		sDBHelper = new SensorDBHelper(getBaseContext());
	    		
	    		SQLiteDatabase db;
	    		
	    		db = sDBHelper.getWritableDatabase();
	    		
	    		
	    		
	    		//Toast.makeText(getBaseContext(), "LAT : "+pos_lat+"\nLON : "+pos_lon, 0).show();
	    		
	    		
	    		// SQL 명령으로 삽입
		        db.execSQL("INSERT INTO SENSOR_TABLE VALUES (null,"+"'"+dTime+"'"+",'"+pos_lat+"','"+pos_lon+"',"+light+","+proximity+","+sound_level+","+gravity_x+","+gravity_y+","+gravity_z+","+battery_level+",'"+NetworkTrackService.this.battery_plug+"',"+"'"+wifi+"'"+");");
		        
	    		sDBHelper.close();
	    		//Toast.makeText(SensorService.this,"DB Input : "+"'"+dTime+"'"+","+gps_lat+","+gps_long+","+light+","+proximity+","+sound_level+","+gravity+","+battery_level+",'"+SensorService.this.battery_plug+"',"+"'"+wifi+"'"+");" , 0).show();
		        
	    		
	    		
	    		mSm.unregisterListener(mSensorListener);
	    		/*
	    		mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_LIGHT));
	        	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_PROXIMITY));
	        	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
	        	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
	        	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GRAVITY));
		        */
				
				//mLocMan_gps.removeUpdates(mListener_gps);
				//mLocMan_network.removeUpdates(mListener_network);
				
				
	    		
	    		try {
	    			
	    			if (com.ramo.networkexperiment.MainActivity.getLastTrafficDBForUpload(getBaseContext()).length() == 0) {
		    			// Setting DB에 업데이트할 DB 이름이 없는 경우 -> 현재 생성되어 있는 폴더를 넣어 놓는다
		    			com.ramo.networkexperiment.MainActivity.setLastTrafficDBForUpload(folder_name);
		    			
		    			SQLiteDatabase setting_db = new SettingDBHelper(getBaseContext()).getWritableDatabase();
		        		
		        		// SQL 명령으로 삽입
		                setting_db.execSQL("UPDATE SETTING_TABLE SET last_traffic_db_for_upload = '"+folder_name+"' WHERE _id = '1';");
		        		
		                setting_db.close();
		    		} else if (!com.ramo.networkexperiment.MainActivity.getLastTrafficDBForUpload(getBaseContext()).equals(folder_name)) {
		    			// Setting DB에 업데이트할 DB 이름이 있으나 현재 DB 저장 폴더와 이름이 다를 경우 -> 이전 자료 압축해서 메일로 전송
		    			
		    			
		    			//Toast.makeText(getBaseContext(), "LastDB : "+com.ramo.networkexperiment.MainActivity.getLastTrafficDBForUpload(getBaseContext())+"\nfolder : "+folder_name, 0).show();
		    			
		    			String path = "/data/data/com.ramo.networkexperiment/ZipFiles";
		        		File newDir = new File(path);
		        		
		        		if (!newDir.exists()) {
		        			newDir.mkdir();
		        		}
		    			
		    			
						try {
							ZipFile zipfile = new ZipFile(path+"/" + com.ramo.networkexperiment.MainActivity.getLastTrafficDBForUpload(getBaseContext()) + ".zip");
							ZipParameters parameters = new ZipParameters();
							parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
							parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
							zipfile.addFolder(database_directory+"/" + com.ramo.networkexperiment.MainActivity.getLastTrafficDBForUpload(getBaseContext()), parameters);
						} catch (Exception e) {
							// 	TODO: handle exception
						}
		    			
						
						String Mail_Title = com.ramo.networkexperiment.MainActivity.getLastTrafficDBForUpload(getBaseContext());
	            		String Mail_Body = com.ramo.networkexperiment.MainActivity.getLastTrafficDBForUpload(getBaseContext());
	            		String File_Name = com.ramo.networkexperiment.MainActivity.getLastTrafficDBForUpload(getBaseContext()) + ".zip";
	            		String File_Path = path+"/" + com.ramo.networkexperiment.MainActivity.getLastTrafficDBForUpload(getBaseContext()) + ".zip";
	            		
	                    new SendMailTask().execute(Mail_Title,Mail_Body,File_Path,File_Name);
	                    
	                    com.ramo.networkexperiment.MainActivity.setLastTrafficDBForUpload(folder_name);
		    			
		    			SQLiteDatabase setting_db = new SettingDBHelper(getBaseContext()).getWritableDatabase();
		        		
		        		// SQL 명령으로 삽입
		                setting_db.execSQL("UPDATE SETTING_TABLE SET last_traffic_db_for_upload = '"+folder_name+"' WHERE _id = '1';");
		        		
		                setting_db.close();
		    		}
	    			
	    			
	    		} catch (Exception e) {
					// TODO: handle exception
	    			
	    			com.ramo.networkexperiment.MainActivity.setLastTrafficDBForUpload(folder_name);
	    			
	    			SQLiteDatabase setting_db = new SettingDBHelper(getBaseContext()).getWritableDatabase();
	        		
	        		// SQL 명령으로 삽입
	                setting_db.execSQL("UPDATE SETTING_TABLE SET last_traffic_db_for_upload = '"+folder_name+"' WHERE _id = '1';");
	        		
	                setting_db.close();
	    			
				}
	    		
	    		
	    		
	    		
	    		
	    		
	    		
				
				
				
			}
				
			
		}
		
	};
	
	SensorEventListener mSensorListener = new SensorEventListener() {
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    		// 특별한 처리 없음 
    	}
    	
    	@SuppressWarnings("deprecation")
    	public void onSensorChanged(SensorEvent event) {
    		// 신뢰성 없는 값은 무시 
    		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
    			// return;
    		}
    		
    		float[] v = event.values;
    		switch (event.sensor.getType()) {
    		case Sensor.TYPE_LIGHT:
    			//mTxtLight.setText("조도 = " + ++mLightCount + "회 : " + v[0]);
    			light = "" + v[0];
    			light_flag = true;    			
    			break;
    		case Sensor.TYPE_PROXIMITY:
    			//mTxtProxi.setText("근접 = " + ++mProxiCount + "회 : " + v[0]);
    			proximity = "" + v[0];
    			proximity_flag = true;
    			break;
    		case Sensor.TYPE_ACCELEROMETER:
    			//mTxtAccel.setText("가속 = " + ++mAccelCount + "회 : \n X : " + v[0] + "\n Y : "+ v[1] + "\n Z : " + v[2]);
    			accel_sensor = "x : "+v[0] +"\ny : "+v[1]+"\nz : "+v[2];
    			break;
    		case Sensor.TYPE_GRAVITY:
    			gravity_x = ""+v[0];
    			gravity_y = ""+v[1];
    			gravity_z = ""+v[2];
    			break;
    		
    		
    		}
    		
    		
    	}
    	
    };
	
	/*
    LocationListener mListener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			gps_lat = ""+location.getLatitude();
			gps_long = ""+location.getLongitude();
			gps_flag = true;			
			//mLocMan.removeUpdates(mListener);
		}
	};
	*/
	
    LocationListener mListener_gps = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			
			lat_gps = ""+location.getLatitude();
			lon_gps = ""+location.getLongitude();
			
			pos_lat = lat_gps;
			pos_lon = lon_gps;
			
			//mLocMan.removeUpdates(mListener);
		}
	};
    
	
	LocationListener mListener_network = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			
			if (lat_gps.length() == 0 || lon_gps.length() == 0) {
				lat_net = ""+location.getLatitude();
				lon_net = ""+location.getLongitude();
				
				pos_lat = lat_net;
				pos_lon = lon_net;
			}
			
				
			
			
			//mLocMan.removeUpdates(mListener);
		}
	};
	
	
	
	
	
	/**
	   * Method to read the sample rate and audio source preferences.
	   */
	  private void readPreferences() {
	    SharedPreferences preferences = getSharedPreferences("LevelMeter", MODE_PRIVATE);
	    mSampleRate = preferences.getInt("SampleRate", 8000);
	    mAudioSource = preferences.getInt("AudioSource", MediaRecorder.AudioSource.VOICE_RECOGNITION);
	  }
	  
	  /**
	   *  This method gets called by the micInput object owned by this activity.
	   *  It first computes the RMS value and then it sets up a bit of
	   *  code/closure that runs on the UI thread that does the actual drawing.
	   */	  
	  public void processAudioFrame(short[] audioFrame) {
		// Compute the RMS value. (Note that this does not remove DC).
	      double rms = 0;
	      for (int i = 0; i < audioFrame.length; i++) {
	        rms += audioFrame[i]*audioFrame[i];
	      }
	      rms = Math.sqrt(rms/audioFrame.length);

	      // Compute a smoothed version for less flickering of the display.
	      mRmsSmoothed = mRmsSmoothed * mAlpha + (1 - mAlpha) * rms;
	      final double rmsdB = 20.0 * Math.log10(mGain * mRmsSmoothed);
	      
	      DecimalFormat df = new DecimalFormat("##");
          //mdBTextView.setText(df.format(20 + rmsdB));	  
		  sound_level = ""+df.format(20 + rmsdB);
		  sound_flag = true;
	  }
	
	  
	  
	  
	  private Long getTxBytesManual(int localUid){
			
			File dir = new File("/proc/uid_stat/");
			String[] children = dir.list();
			if(!Arrays.asList(children).contains(String.valueOf(localUid))){
			    return 0L;
			}
			File uidFileDir = new File("/proc/uid_stat/"+String.valueOf(localUid));
			File uidActualFileSent = new File(uidFileDir,"tcp_snd");
			
			 String textSent = "0";
			
			 try {
			        BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
			        String sentLine;
			
			        if ((sentLine = brSent.readLine()) != null) {
			            textSent = sentLine;
			        }
			
			    }
			    catch (IOException e) {
			
			    }
			 return Long.valueOf(textSent).longValue();
		
		}

		private Long getRxBytesManual(int localUid){
		
			File dir = new File("/proc/uid_stat/");
			String[] children = dir.list();
			if(!Arrays.asList(children).contains(String.valueOf(localUid))){
			    return 0L;
			}
			File uidFileDir = new File("/proc/uid_stat/"+String.valueOf(localUid));
			File uidActualFileReceived = new File(uidFileDir,"tcp_rcv");
			File uidActualFileSent = new File(uidFileDir,"tcp_snd");
			
			 String textReceived = "0";
			 String textSent = "0";
			
			 try {
			        BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
			        String receivedLine;
			
			        if ((receivedLine = brReceived.readLine()) != null) {
			            textReceived = receivedLine;
			        }
			    }
			    catch (IOException e) {
			
			    }
			 return Long.valueOf(textReceived).longValue();
		
		}
	  
	  
}
