package com.ramo.networkexperiment.Tab1_Map;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ramo.networkexperiment.R;


public class Tab1_MapActivity extends FragmentActivity {

	Intent intent;
	
	SQLiteDatabase map_db;
	String dbName;
	
	//String userId;
	
	// SQLite 데이터 베이스
    MapDBHelper mDBHelper;
	
	GoogleMap mGoogleMap;
	 //LatLng loc = new LatLng(37.58,126.979187); 
	 //CameraPosition cp = new CameraPosition.Builder().target((loc)).zoom(16).build();
	 //MarkerOptions marker = new MarkerOptions().position(loc); 
	   
	 /** Called when the activity is first created. */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
	     setContentView(R.layout.tab1);
	      
	     mGoogleMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap(); 
	     //mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp)); 
	     //mGoogleMap.addMarker(marker); 
	                
	     //userId = "psy3061";
	     
	     // TODO Auto-generated method stub
	 }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onStart() {
		super.onStart();
		/*
		intent = new Intent(this, MapService.class);
		startService(intent);
		*/
	}
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	}
	
	public void onResume() {
		super.onResume();
		
		// AsynchTask를 상속하는 DoComplecatedJob 클래스를 생성하고
    	// execute(...) 명령으로 background작업을 시작함.
    	// (예제에 구현된 AsynchTask는 String 형의 인자를 받음) 
    	new MappingMarkers().execute("1", "2", "3");  
		
		
		/*
		if (com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()) != null) {
			//Toast.makeText(getBaseContext(), com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()), 0).show();
			
			String database_directory = getApplicationContext().getDatabasePath("Setting.db").getParent()+"/MapData/";
			
			// 현재 연원일 시간 포맷 설정
			SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd)");
	        Date currentTime2 = new Date();
	        String dTime2 = formatter2.format(currentTime2);
	        
	        dbName = database_directory+"[Map]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
	        
	        String DB_NAME = dbName;
			String dbPath = getApplicationContext().getDatabasePath(DB_NAME).getParent();
			String file_path = dbPath+"/"+DB_NAME;
			
			
			//Toast.makeText(getBaseContext(),"파일 Exist\n"+file_path, 0).show();
			mDBHelper = new MapDBHelper(getBaseContext());
			map_db = mDBHelper.getReadableDatabase();
			
			try {
				
				Cursor cr = map_db.rawQuery("SELECT * FROM MAP_TABLE", null);
				cr.moveToFirst();
				
				int dbcount = 0;			
				try {
					dbcount = cr.getCount();
				} catch (Exception e) {
					dbcount = 0;
				}
				
				if (dbcount != 0) {

					//Toast.makeText(this,"Count : "+cr.getCount()+"\nLat : "+cr.getDouble(2)+"\nLon : "+cr.getDouble(3), 0).show();
					
					double pre_lat = 0;
					double pre_lon = 0;
					
					int count = cr.getCount() - 10;
					
					for (int i = 0 ; i < cr.getCount() ; i++) {
						//Toast.makeText(this,"Count : "+cr.getPosition()+"\nLat : "+cr.getDouble(2)+"\nLon : "+cr.getDouble(3), 0).show();
						
						if (cr.getDouble(2) != 0 && cr.getDouble(3) != 0) {
							LatLng loc = new LatLng(cr.getDouble(2),cr.getDouble(3));
							
							MarkerOptions marker;
							
							if (i < count) {
								marker = new MarkerOptions().position(loc)
										.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
							} else {
								marker = new MarkerOptions().position(loc)
										.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
							}
							
							
							
							mGoogleMap.addMarker(marker);
							
							if (i == 0) {
								//pre_lat = cr.getDouble(2);
								//pre_lon = cr.getDouble(3);
								CameraPosition cp = new CameraPosition.Builder().target((loc)).zoom(16).build();
								mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
							} else {
								// Polylines are useful for marking paths and routes on the map.
								mGoogleMap.addPolyline(new PolylineOptions().geodesic(true)
						                .add(new LatLng(pre_lat, pre_lon))  				// Previous Position
						                .add(new LatLng(cr.getDouble(2), cr.getDouble(3)))  // Current Position
						                .color(Color.BLUE)
						                .width(5)
						                );
							}
							
							if (cr.isLast()) {
								CameraPosition cp = new CameraPosition.Builder().target((loc)).zoom(19).build();
								mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
							}
							
							pre_lat = cr.getDouble(2);
							pre_lon = cr.getDouble(3);
							
							cr.moveToNext();
						}
						
						
					}
				}
				
				cr.close();
				
			} catch (Exception e) {
				// 	TODO: handle exception
				
				
			}
			
			mDBHelper.close();
			
			
		}
		*/
		
	}
	
	public void onStop() {
		super.onStop();
		/*
		intent = new Intent(this, MapService.class);
		stopService(intent);
		*/
	}
	
	public void onDestroy() {
		super.onDestroy();
		/*
		intent = new Intent(this, MapService.class);
		stopService(intent);
		*/
	}
	
	
	class MapDBHelper extends SQLiteOpenHelper {
    	public MapDBHelper(Context context) {
    		//super(context, "SensorData.db", null, 1);
    		super(context, dbName, null, 1);
    	}
    	
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("CREATE TABLE MAP_TABLE ( _id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp TEXT, gps_lat TEXT, gps_long TEXT);");
    	}
    	
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		//db.execSQL("DROP TABLE IF EXISTS SENSOR_TABLE");
    		//onCreate(db);
    	}
    }

	
	
    // AsyncTask클래스는 항상 Subclassing 해서 사용 해야 함.
    // 사용 자료형은
    // background 작업에 사용할 data의 자료형: String 형
    // background 작업 진행 표시를 위해 사용할 인자: Integer형
    // background 작업의 결과를 표현할 자료형: Long
    private class MappingMarkers extends AsyncTask<String, Integer, Long> {    	
   
    
    	// 이곳에 포함된 code는 AsyncTask가 execute 되자 마자 UI 스레드에서 실행됨.
    	// 작업 시작을 UI에 표현하거나
    	// background 작업을 위한 ProgressBar를 보여 주는 등의 코드를 작성.
		@Override
		protected void onPreExecute() {	
			super.onPreExecute();
			
			if (com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()) != null) {
				
				
				String database_directory = getApplicationContext().getDatabasePath("Setting.db").getParent()+"/MapData/";
				
				// 현재 연원일 시간 포맷 설정
				SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd)");
		        Date currentTime2 = new Date();
		        String dTime2 = formatter2.format(currentTime2);
		        
		        dbName = database_directory+"[Map]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
		        
		        String DB_NAME = dbName;
				String dbPath = getApplicationContext().getDatabasePath(DB_NAME).getParent();
				String file_path = dbPath+"/"+DB_NAME;
				
				
				//Toast.makeText(getBaseContext(),"파일 Exist\n"+file_path, 0).show();
				mDBHelper = new MapDBHelper(getBaseContext());
				map_db = mDBHelper.getReadableDatabase();
			}
			
		}

		// UI 스레드에서 AsynchTask객체.execute(...) 명령으로 실행되는 callback 
		@Override
    	protected Long doInBackground(String... strData) {
			
			
			
			
			
    		long totalTimeSpent = 0;
    		
    		
    		
    		return totalTimeSpent;
    		
    		
    	}
    	
    	// onInBackground(...)에서 publishProgress(...)를 사용하면
    	// 자동 호출되는 callback으로
    	// 이곳에서 ProgressBar를 증가 시키고, text 정보를 update하는 등의
    	// background 작업 진행 상황을 UI에 표현함.
    	// (예제에서는 UI스레드의 ProgressBar를 update 함) 
    	@Override
    	protected void onProgressUpdate(Integer... progress) {
    		//progressBar.setProgress(progress[0]);
    	}
    	
    	// onInBackground(...)가 완료되면 자동으로 실행되는 callback
    	// 이곳에서 onInBackground가 리턴한 정보를 UI위젯에 표시 하는 등의 작업을 수행함.
    	// (예제에서는 작업에 걸린 총 시간을 UI위젯 중 TextView에 표시함)
    	@Override
    	protected void onPostExecute(Long result) {
    		if (com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()) != null) {
    			//Toast.makeText(getBaseContext(), com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()), 0).show();
    			try {
    				
    				Cursor cr = map_db.rawQuery("SELECT * FROM MAP_TABLE", null);
    				cr.moveToFirst();
    				
    				int dbcount = 0;			
    				try {
    					dbcount = cr.getCount();
    				} catch (Exception e) {
    					dbcount = 0;
    				}
    				
    				if (dbcount != 0) {

    					//Toast.makeText(this,"Count : "+cr.getCount()+"\nLat : "+cr.getDouble(2)+"\nLon : "+cr.getDouble(3), 0).show();
    					
    					double pre_lat = 0;
    					double pre_lon = 0;
    					
    					int count = cr.getCount() - 10;
    					
    					for (int i = 0 ; i < cr.getCount() ; i++) {
    						//Toast.makeText(this,"Count : "+cr.getPosition()+"\nLat : "+cr.getDouble(2)+"\nLon : "+cr.getDouble(3), 0).show();
    						
    						if (cr.getDouble(2) != 0 && cr.getDouble(3) != 0) {
    							LatLng loc = new LatLng(cr.getDouble(2),cr.getDouble(3));
    							
    							MarkerOptions marker;
    							
    							if (i < count) {
    								marker = new MarkerOptions().position(loc)
    										.alpha(0.3f)
    										.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
    							} else if (i == cr.getCount() -1) {
    								marker = new MarkerOptions().position(loc)
    										.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
    							} else {
    								marker = new MarkerOptions().position(loc)
    										.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    							}
    							
    							
    							
    							mGoogleMap.addMarker(marker);
    							
    							if (i == 0) {
    								//pre_lat = cr.getDouble(2);
    								//pre_lon = cr.getDouble(3);
    								CameraPosition cp = new CameraPosition.Builder().target((loc)).zoom(16).build();
    								mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    							} else {
    								// Polylines are useful for marking paths and routes on the map.
    								mGoogleMap.addPolyline(new PolylineOptions().geodesic(true)
    						                .add(new LatLng(pre_lat, pre_lon))  				// Previous Position
    						                .add(new LatLng(cr.getDouble(2), cr.getDouble(3)))  // Current Position
    						                .color(Color.BLUE)
    						                .width(5)
    						                );
    							}
    							
    							if (cr.isLast()) {
    								CameraPosition cp = new CameraPosition.Builder().target((loc)).zoom(19).build();
    								mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    							}
    							
    							pre_lat = cr.getDouble(2);
    							pre_lon = cr.getDouble(3);
    							
    							cr.moveToNext();
    						}
    						
    						
    					}
    				}
    				
    				cr.close();
    				
    			} catch (Exception e) {
    				// 	TODO: handle exception
    				
    				
    			}
    			
    			mDBHelper.close();
    			
    			
    		}
    	}
    	
    	// AsyncTask.cancel(boolean) 메소드가 true 인자로 
    	// 실행되면 호출되는 콜백.
    	// background 작업이 취소될때 꼭 해야될 작업은  여기에 구현.
    	@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			mDBHelper.close();
		}    	
    }	
	
}
