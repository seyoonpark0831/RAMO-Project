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
	
	// SQLite ������ ���̽�
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
		
		// AsynchTask�� ����ϴ� DoComplecatedJob Ŭ������ �����ϰ�
    	// execute(...) ������� background�۾��� ������.
    	// (������ ������ AsynchTask�� String ���� ���ڸ� ����) 
    	new MappingMarkers().execute("1", "2", "3");  
		
		
		/*
		if (com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()) != null) {
			//Toast.makeText(getBaseContext(), com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()), 0).show();
			
			String database_directory = getApplicationContext().getDatabasePath("Setting.db").getParent()+"/MapData/";
			
			// ���� ������ �ð� ���� ����
			SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd)");
	        Date currentTime2 = new Date();
	        String dTime2 = formatter2.format(currentTime2);
	        
	        dbName = database_directory+"[Map]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
	        
	        String DB_NAME = dbName;
			String dbPath = getApplicationContext().getDatabasePath(DB_NAME).getParent();
			String file_path = dbPath+"/"+DB_NAME;
			
			
			//Toast.makeText(getBaseContext(),"���� Exist\n"+file_path, 0).show();
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

	
	
    // AsyncTaskŬ������ �׻� Subclassing �ؼ� ��� �ؾ� ��.
    // ��� �ڷ�����
    // background �۾��� ����� data�� �ڷ���: String ��
    // background �۾� ���� ǥ�ø� ���� ����� ����: Integer��
    // background �۾��� ����� ǥ���� �ڷ���: Long
    private class MappingMarkers extends AsyncTask<String, Integer, Long> {    	
   
    
    	// �̰��� ���Ե� code�� AsyncTask�� execute ���� ���� UI �����忡�� �����.
    	// �۾� ������ UI�� ǥ���ϰų�
    	// background �۾��� ���� ProgressBar�� ���� �ִ� ���� �ڵ带 �ۼ�.
		@Override
		protected void onPreExecute() {	
			super.onPreExecute();
			
			if (com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()) != null) {
				
				
				String database_directory = getApplicationContext().getDatabasePath("Setting.db").getParent()+"/MapData/";
				
				// ���� ������ �ð� ���� ����
				SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd)");
		        Date currentTime2 = new Date();
		        String dTime2 = formatter2.format(currentTime2);
		        
		        dbName = database_directory+"[Map]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
		        
		        String DB_NAME = dbName;
				String dbPath = getApplicationContext().getDatabasePath(DB_NAME).getParent();
				String file_path = dbPath+"/"+DB_NAME;
				
				
				//Toast.makeText(getBaseContext(),"���� Exist\n"+file_path, 0).show();
				mDBHelper = new MapDBHelper(getBaseContext());
				map_db = mDBHelper.getReadableDatabase();
			}
			
		}

		// UI �����忡�� AsynchTask��ü.execute(...) ������� ����Ǵ� callback 
		@Override
    	protected Long doInBackground(String... strData) {
			
			
			
			
			
    		long totalTimeSpent = 0;
    		
    		
    		
    		return totalTimeSpent;
    		
    		
    	}
    	
    	// onInBackground(...)���� publishProgress(...)�� ����ϸ�
    	// �ڵ� ȣ��Ǵ� callback����
    	// �̰����� ProgressBar�� ���� ��Ű��, text ������ update�ϴ� ����
    	// background �۾� ���� ��Ȳ�� UI�� ǥ����.
    	// (���������� UI�������� ProgressBar�� update ��) 
    	@Override
    	protected void onProgressUpdate(Integer... progress) {
    		//progressBar.setProgress(progress[0]);
    	}
    	
    	// onInBackground(...)�� �Ϸ�Ǹ� �ڵ����� ����Ǵ� callback
    	// �̰����� onInBackground�� ������ ������ UI������ ǥ�� �ϴ� ���� �۾��� ������.
    	// (���������� �۾��� �ɸ� �� �ð��� UI���� �� TextView�� ǥ����)
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
    	
    	// AsyncTask.cancel(boolean) �޼ҵ尡 true ���ڷ� 
    	// ����Ǹ� ȣ��Ǵ� �ݹ�.
    	// background �۾��� ��ҵɶ� �� �ؾߵ� �۾���  ���⿡ ����.
    	@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			mDBHelper.close();
		}    	
    }	
	
}
