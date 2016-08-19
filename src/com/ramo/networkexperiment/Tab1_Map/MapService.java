package com.ramo.networkexperiment.Tab1_Map;


import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;



public class MapService extends Service {
	
    String timestamp;
    String gps_lat = "";
    String gps_long = "";
    
    String lat_net = "";
    String lon_net = "";
    
    String lat_gps = "";
    String lon_gps = "";
    
    String pos_lat = "";
    String pos_lon = "";
    
	
    String pre_lat = "";
    String pre_long = "";
    
    
    // 
    LocationManager mLocMan_gps;
    LocationManager mLocMan_network;
    
    //String mProvider;
    

    
    
    // SQLite 데이터 베이스
    MapDBHelper mDBHelper;
    
    //String userId;
    String dbName;
    int file_flag;
    
	protected static final Context MainActivity = null;
	boolean mQuit;
	
	public void onCreate() {
		super.onCreate();
		
		while (true) {
			if (com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()) != null) {
				break;
			}
			
			try{Thread.sleep(1000);}catch (Exception e) {;}
		}
		//mProvider = mLocMan.getBestProvider(new Criteria(), true);
		
		//mLocMan.requestLocationUpdates(mProvider, 10000, 10, mListener);	// 10ms, 10m 단위로 변경 업데이트
		mLocMan_gps = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mLocMan_network = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		mLocMan_gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, mListener_gps);	// 10ms, 10m 단위로 변경 업데이트
		mLocMan_network.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, mListener_network);	// 10ms, 10m 단위로 변경 업데이트
	    
	    
		//userId = com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext());
	    
		
		
		
	}
	
	
	class MapDBHelper extends SQLiteOpenHelper {
    	public MapDBHelper(Context context) {
    		//super(context, "SensorData.db", null, 1);
    		super(context, dbName, null, 1);
    		//Toast.makeText(getBaseContext(), "MAP_DB : "+dbName, 0).show();
    	}
    	
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("CREATE TABLE MAP_TABLE ( _id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp TEXT, gps_lat TEXT, gps_long TEXT);");
    	}
    	
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		//db.execSQL("DROP TABLE IF EXISTS SENSOR_TABLE");
    		//onCreate(db);
    	}
    }
	
	
	public void onDestroy() {
		super.onDestroy();
		
		mLocMan_gps.removeUpdates(mListener_gps);
		mLocMan_network.removeUpdates(mListener_network);
		
		//Toast.makeText(this, "Service End", 0).show();
		mQuit = true;
		
	}
	
	public int onStartCommand (Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		//Toast.makeText(this, "Service Start!", 0).show();
		mQuit = false;
				
		MapThread thread = new MapThread(this, mHandler);
		thread.start();
		return START_STICKY;
	}
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	
	class MapThread extends Thread {
		MapService mParent;
		Handler mHandler;
		
		
		
		public MapThread(MapService parent, Handler handler) {
			mParent = parent;
			mHandler = handler;
		}
		
		
		
		public void run() {
			
			while(mQuit == false) {
				
		    	
		    	Message msg = new Message();
				msg.what = 0;
				msg.obj = "";
				mHandler.sendMessage(msg);
		    	
				
				
				try{Thread.sleep(10000);}catch (Exception e) {;}
			}
			
		}
	}
	
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		
		SQLiteDatabase db;
		
		public void handleMessage (Message msg) {
			if (msg.what == 0) {
				
				
				
				// GPS 좌표 갱신 (업데이트 종료는 GPS 업데이터 된 메서드에서 바로 한다)
				//mLocMan.requestLocationUpdates(mProvider, 100, 500, mListener);	// 10ms, 10m 단위로 변경 업데이트
				//mLocMan.requestLocationUpdates(mProvider, 0, 0, mListener);	// 10ms, 10m 단위로 변경 업데이트
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n위도 : "+gps_lat+"\n경도 : "+gps_long, 0).show();
				
				
				String database_directory = getApplicationContext().getDatabasePath("Setting.db").getParent()+"/MapData/";
				
				if (pos_lat.length() != 0 && pos_lon.length() != 0) {
					if (pre_lat.equals(pos_lat) == false && pre_long.equals(pos_lon) == false) {
		        		// 현재 연원일 시간 포맷 설정
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
				        Date currentTime = new Date();
				        String dTime = formatter.format(currentTime);
				        
				        //Toast.makeText(MapService.this,"Latitude : "+gps_lat+"\nLongitude : "+gps_long, 0).show();
				        
				        
				        
			    		// 현재 연원일 시간 포맷 설정
						SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd)");
				        Date currentTime2 = new Date();
				        String dTime2 = formatter2.format(currentTime2);
				        
				        dbName = database_directory+"[Map]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
				        
			    		
			    		mDBHelper = new MapDBHelper(getBaseContext());
			    		
			    		db = mDBHelper.getWritableDatabase();
			    		
			    		
			    		// SQL 명령으로 삽입
				        db.execSQL("INSERT INTO MAP_TABLE VALUES (null,"+"'"+dTime+"'"+","+pos_lat+","+pos_lon+");");
				        
			    		mDBHelper.close();
			    		//Toast.makeText(MapService.this,"DB Input : "+"'"+dTime+"'"+","+pos_lat+","+pos_lon+");" , 0).show();
		        	}
		        	
		        	pre_lat = pos_lat;
		        	pre_long = pos_lon;
				}
				
		        
				
				/*
		        if (gps_lat.length() != 0 && gps_long.length() != 0) {
		        	
		        	if (pre_lat.equals(gps_lat) == false && pre_long.equals(gps_long) == false) {
		        		// 현재 연원일 시간 포맷 설정
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
				        Date currentTime = new Date();
				        String dTime = formatter.format(currentTime);
				        
				        //Toast.makeText(MapService.this,"Latitude : "+gps_lat+"\nLongitude : "+gps_long, 0).show();
				        
				        
				        
			    		// 현재 연원일 시간 포맷 설정
						SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd)");
				        Date currentTime2 = new Date();
				        String dTime2 = formatter2.format(currentTime2);
				        
				        dbName = database_directory+"[Map]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
				        
			    		
			    		mDBHelper = new MapDBHelper(getBaseContext());
			    		
			    		db = mDBHelper.getWritableDatabase();
			    		
			    		
			    		// SQL 명령으로 삽입
				        db.execSQL("INSERT INTO MAP_TABLE VALUES (null,"+"'"+dTime+"'"+","+gps_lat+","+gps_long+");");
				        
			    		mDBHelper.close();
			    		//Toast.makeText(MapService.this,"DB Input : "+"'"+dTime+"'"+","+gps_lat+","+gps_long+");" , 0).show();
		        	}
		        	
		        	pre_lat = gps_lat;
		        	pre_long = gps_long;
		        	
		        }
	    		
		        */
		        
			}
		}
		
	};
	
	
	
	
	
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
			//Toast.makeText(MapService.this,"Latitude : "+location.getLatitude()+"\nLongitude : "+location.getLongitude(), 0).show();
			//mLocMan.removeUpdates(mListener);
		}
	};

	
	
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
	
	
}
