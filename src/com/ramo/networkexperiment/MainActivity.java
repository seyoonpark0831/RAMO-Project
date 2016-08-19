package com.ramo.networkexperiment;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.ramo.networkexperiment.Tab1_Map.MapService;
import com.ramo.networkexperiment.Tab1_Map.Tab1_MapActivity;
import com.ramo.networkexperiment.Tab2_Traffic.NetworkTrackService;
import com.ramo.networkexperiment.Tab2_Traffic.Tab2_TrafficActivity;
import com.ramo.networkexperiment.Tab3_Sensor.Tab3_SensorActivity;
import com.ramo.networkexperiment.Tab4_Setting.Tab4_SettingActivity;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {

	TabHost mTab;
	
	// SQLite
	static SettingDBHelper settingDBHelper;
	
	public static String userId;
	static int sensor_interval;
	public static int traffic_interval;
	public static String last_sensor_db_for_upload;
	public static String last_traffic_db_for_upload;
	
	AlertDialog registrationDialog;
	
	Intent intent_map;
	Intent intent_sensor;
	Intent intent_traffic;
	
	int start = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tab_test);
        
        String versionSDK = Build.VERSION.SDK;
        //Toast.makeText(getBaseContext(), "OS Ver : "+versionSDK+"\nModel : "+Build.PRODUCT, 0).show();


        
        
        settingDBHelper = new SettingDBHelper(getBaseContext());
		final SQLiteDatabase db;
		db = settingDBHelper.getWritableDatabase();
        
		Cursor cursor = db.rawQuery("SELECT * FROM SETTING_TABLE", null);
		
		
		
		int count = 0;			
		try {
			count = cursor.getCount();
		} catch (Exception e) {
			count = 0;
		}
		
		
		if (count == 0) {
			// SETTING_TABLE에 데이터 ROW 존재하지 않는 경우
			
			final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.user_registration, null);
			
			registrationDialog = new AlertDialog.Builder(this)
			.setTitle("실험에 사용할 사용자 ID를 등록하세요")
			.setIcon(R.drawable.ic_launcher)
			.setView(linear)
			.setCancelable(false)
			.setPositiveButton("등록", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText reg_userid = (EditText)linear.findViewById(R.id.edit_userid);
					
					if (reg_userid.getText().length() != 0) {
						// 정상 등록
						Toast.makeText(getBaseContext(), "'"+reg_userid.getText()+"'으로 정상 등록되었습니다", 0).show();
						// SQL 명령으로 삽입
				        db.execSQL("INSERT INTO SETTING_TABLE VALUES ("+null+","+"'"+reg_userid.getText()+"'"+",'30','30', '', '','"+ Build.VERSION.SDK_INT +"','"+ Build.MODEL +"');");
				        
				        setUserId(reg_userid.getText().toString());
				        setSensorInterval(30);
				        setTrafficInterval(30);
				        
				        IntentStart();
				        
					} else {
						// 다시 입력
						Toast.makeText(getBaseContext(), "잘못 입력하였습니다\n다시 입력해 주세요", 0).show();
						registrationDialog.show();
					}
					
				}
			})
			.setNegativeButton("취소", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(), "프로그램을 종료합니다", 0).show();
					moveTaskToBack(true);
					finish();
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			})
			.show();
			
			
			
	        
		} else {
			// SETTING_TABLE에 데이터 ROW 존재하는 경우
			String db_userid = null;
			int db_sensor_interval = 30;
			int db_traffic_interval = 120;
			//cursor.moveToNext();
			
			
			cursor.moveToNext();
			db_userid 		= cursor.getString(cursor.getColumnIndex("user_id"));
			db_sensor_interval 	= cursor.getInt(cursor.getColumnIndex("sensor_interval"));
			db_traffic_interval = cursor.getInt(cursor.getColumnIndex("traffic_interval"));
			last_sensor_db_for_upload = cursor.getString(cursor.getColumnIndex("last_sensor_db_for_upload"));
			last_traffic_db_for_upload = cursor.getString(cursor.getColumnIndex("last_traffic_db_for_upload"));
			
			
			setUserId(db_userid);
	        setSensorInterval(db_sensor_interval);
	        setTrafficInterval(db_traffic_interval);
			
	        IntentStart();
	        
	        /*
	        intent_map = new Intent(this, MapService.class);
			startService(intent_map);
			
			intent_sensor = new Intent(this, SensorService.class);
			startService(intent_sensor);
			
			intent_traffic = new Intent(this, NetworkTrackService.class);
			startService(intent_traffic);
			*/
			
		}
		
        TabHost mTab = getTabHost();
        
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.activity_main, mTab.getTabContentView(), true);
        
        mTab.addTab(mTab.newTabSpec("tag")
				.setIndicator("Map")
				.setContent(new Intent(this, Tab1_MapActivity.class)));
		
		mTab.addTab(mTab.newTabSpec("tag")
				.setIndicator("Network Traffic")
				.setContent(new Intent(this, Tab2_TrafficActivity.class)));
		
		mTab.addTab(mTab.newTabSpec("tag")
				.setIndicator("Sensor Data")
				.setContent(new Intent(this, Tab3_SensorActivity.class)));
        
		mTab.addTab(mTab.newTabSpec("tag")
				.setIndicator("Setting")
				.setContent(new Intent(this, Tab4_SettingActivity.class)));
		
    }
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void IntentStart() {
		if (start == 0) {
			
			intent_map = new Intent(this, MapService.class);
			startService(intent_map);
			
			//intent_sensor = new Intent(this, SensorService.class);
			//startService(intent_sensor);
			
			intent_traffic = new Intent(this, NetworkTrackService.class);
			startService(intent_traffic);
			
			start = 1;
		}
		
	}
	
	public void onStart() {
		super.onStart();
		/*
		intent_map = new Intent(this, MapService.class);
		startService(intent_map);
		
		intent_sensor = new Intent(this, SensorService.class);
		startService(intent_sensor);
		
		intent_traffic = new Intent(this, NetworkTrackService.class);
		startService(intent_traffic);
		*/
		
	}
	
	public void onDestroy() {
		super.onDestroy();
		
		intent_map = new Intent(this, MapService.class);
		stopService(intent_map);
		
		//intent_sensor = new Intent(this, SensorService.class);
		//stopService(intent_sensor);
		
		intent_traffic = new Intent(this, NetworkTrackService.class);
		stopService(intent_traffic);
		
	}
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	}
	
	class SettingDBHelper extends SQLiteOpenHelper {
    	public SettingDBHelper(Context context) {
    		super(context, "Setting.db", null, 1);
    		Log.d("DB Make", "DB CREATE");
    	}
    	
    	
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("CREATE TABLE SETTING_TABLE ( _id INTEGER PRIMARY KEY AUTOINCREMENT , user_id TEXT, sensor_interval TEXT, traffic_interval TEXT, last_sensor_db_for_upload TEXT, last_traffic_db_for_upload TEXT, OS TEXT, MODEL TEXT);");
    		Log.d("DB Make", "TABLE CREATE");
    	}
    	
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		//db.execSQL("DROP TABLE IF EXISTS SENSOR_TABLE");
    		//onCreate(db);
    	}
    }
	
	
	
	
	
	
	
	
	public static String getUserId(Context context) {
		return userId;
	}
	
	public static void setUserId(String id) {
		userId = id;
	}
	
	public static int getTrafficInterval(Context context) {
		return traffic_interval;
	}
	
	public static void setTrafficInterval(int t_interval) {
		traffic_interval = t_interval;
	}
	
	public static int getSensorInterval(Context context) {
		return sensor_interval;
	}
	
	public static void setSensorInterval(int s_interval) {
		sensor_interval = s_interval;
	}
	
	public static String getLastSensorDBForUpload(Context context) {
		return last_sensor_db_for_upload;
	}
	
	public static void setLastSensorDBForUpload(String dbName) {
		
		last_sensor_db_for_upload = dbName;
		/*
		settingDBHelper = new SettingDBHelper(getBaseContext());
		SQLiteDatabase db = settingDBHelper.getWritableDatabase();
		
		// SQL 명령으로 삽입
		db.execSQL("UPDATE SETTING_TABLE SET last_sensor_db_for_upload = '"+dbName+"' WHERE _id = '1';");
		
		db.close();
		*/
	}
	
	public static String getLastTrafficDBForUpload(Context context) {
		return last_traffic_db_for_upload;
	}
	
	public static void setLastTrafficDBForUpload(String dbName) {
		
		last_traffic_db_for_upload = dbName;
		
		/*
		SQLiteDatabase db = settingDBHelper.getWritableDatabase();
		
		// SQL 명령으로 삽입
		db.execSQL("UPDATE SETTING_TABLE SET last_traffic_db_for_upload = '"+dbName+"' WHERE _id = '1';");
		
		db.close();
		
		last_traffic_db_for_upload = dbName;
		*/
	}
	
	
}
