package com.ramo.networkexperiment.Tab4_Setting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.TrafficStats;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ramo.networkexperiment.GMailSender;
import com.ramo.networkexperiment.MainActivity;
import com.ramo.networkexperiment.R;
import com.ramo.networkexperiment.SendMailTask;
import com.ramo.networkexperiment.Tab2_Traffic.TextFormat;

public class Tab4_SettingActivity extends Activity {

	ProgressDialog dialog;
	GMailSender sender;

	
	int s_interval;
	int t_interval;
	
	// SQLite
	SettingDBHelper settingDBHelper;
	
	TextView userIdTxt;
	
	RadioGroup sensorGroup;
	RadioGroup trafficGroup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab4);
		
		/*
		sensorGroup = (RadioGroup)findViewById(R.id.sensor_radioGroup);
		sensorGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				
				if (group.getId() == R.id.sensor_radioGroup) {
					switch (checkedId) {
					case R.id.sensor_radio0:
						s_interval = 30;
						break;
					case R.id.sensor_radio1:
						s_interval = 60;
						break;
					case R.id.sensor_radio2:
						s_interval = 120;
						break;
					case R.id.sensor_radio3:
						s_interval = 180;
						break;
					case R.id.sensor_radio4:
						s_interval = 300;
						break;
					
					}
				}			
			}
		});
		
		*/
		
		trafficGroup = (RadioGroup)findViewById(R.id.traffic_radioGroup);
		trafficGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				
				if (group.getId() == R.id.traffic_radioGroup) {
					switch (checkedId) {
					case R.id.traffic_radio0:
						t_interval = 30;
						break;
					case R.id.traffic_radio1:
						t_interval = 60;
						break;
					case R.id.traffic_radio2:
						t_interval = 120;
						break;
					case R.id.traffic_radio3:
						t_interval = 180;
						break;
					case R.id.traffic_radio4:
						t_interval = 300;
						break;
					
					}
				}			
			}
		});
		
		userIdTxt = (TextView)findViewById(R.id.user_id_txt);
		
		
		
		settingDBHelper = new SettingDBHelper(getBaseContext());
		final SQLiteDatabase db;
		db = settingDBHelper.getReadableDatabase();
        
		Cursor cursor = db.rawQuery("SELECT * FROM SETTING_TABLE", null);
		
		
		
		int count = 0;			
		try {
			count = cursor.getCount();
		} catch (Exception e) {
			count = 0;
		}
		
		if (count == 0) {
			
		} else  {
			// SETTING_TABLE에 데이터 ROW 존재하는 경우
			String db_userid = "";
			int db_sensor_interval = 0;
			int db_traffic_interval = 0;
			
			cursor.moveToNext();
			db_userid 		= cursor.getString(cursor.getColumnIndex("user_id"));
			db_sensor_interval 	= cursor.getInt(cursor.getColumnIndex("sensor_interval"));
			db_traffic_interval = cursor.getInt(cursor.getColumnIndex("traffic_interval"));
			
			s_interval = db_sensor_interval;
			t_interval = db_traffic_interval;
			
			//Toast.makeText(getBaseContext(), "ID : "+db_userid+"\nSensor : "+db_sensor_interval+"\nTraffic : "+db_traffic_interval, 0).show();
			
			userIdTxt.setText(db_userid);
			
			/*
			switch (db_sensor_interval) {
			case 30:
				sensorGroup.check(R.id.sensor_radio0);
				break;
				
			case 60:
				sensorGroup.check(R.id.sensor_radio1);
				break;
				
			case 120:
				sensorGroup.check(R.id.sensor_radio2);
				break;
				
			case 180:
				sensorGroup.check(R.id.sensor_radio3);
				break;
				
			case 300:
				sensorGroup.check(R.id.sensor_radio4);
				break;

			}
			
			*/
			
			switch (db_traffic_interval) {
			case 30:
				trafficGroup.check(R.id.traffic_radio0);
				break;
				
			case 60:
				trafficGroup.check(R.id.traffic_radio1);
				break;
				
			case 120:
				trafficGroup.check(R.id.traffic_radio2);
				break;
				
			case 180:
				trafficGroup.check(R.id.traffic_radio3);
				break;
				
			case 300:
				trafficGroup.check(R.id.traffic_radio4);
				break;

			}
			
			
		}
			
		
		cursor.close();
		
	}

	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	}
	
	
	public void mOnClick(View context) {
    	
    	
    	switch (context.getId()) {
    	case R.id.btn_save:
    		//Toast.makeText(getBaseContext(), "Sensor : "+s_interval+"\nTraffic : "+t_interval, 0).show();
    		//Log.d("TEST","Sensor : "+s_interval+"\nTraffic : "+t_interval);
    		
    		SQLiteDatabase db;
			db = settingDBHelper.getWritableDatabase();
			
			// SQL 명령으로 삽입
			db.execSQL("UPDATE SETTING_TABLE SET sensor_interval = '"+s_interval+"', traffic_interval = '"+t_interval+"' WHERE _id = '1';");
			
			db.close();
			
	        MainActivity.setSensorInterval(s_interval);
	        MainActivity.setTrafficInterval(t_interval);
    		
	        Toast.makeText(getBaseContext(), "설정이 변경되었습니다", 0).show();
	        
    		break;
    	case R.id.btn_idmod:
    		//Toast.makeText(getBaseContext(), "Sensor : "+s_interval+"\nTraffic : "+t_interval, 0).show();
    		//Log.d("TEST","Sensor : "+s_interval+"\nTraffic : "+t_interval);
    		
    		final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.user_registration, null);
			
			new AlertDialog.Builder(this)
			.setTitle("변경할 사용자 ID를 등록하세요")
			.setIcon(R.drawable.ic_launcher)
			.setView(linear)
			.setPositiveButton("등록", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText reg_userid = (EditText)linear.findViewById(R.id.edit_userid);
					
					if (reg_userid.getText().length() != 0) {
						// 정상 등록
						Toast.makeText(getBaseContext(), "'"+reg_userid.getText()+"'으로 정상 변경되었습니다", 0).show();
						
						SQLiteDatabase db;
						db = settingDBHelper.getWritableDatabase();
						
						// SQL 명령으로 삽입
						db.execSQL("UPDATE SETTING_TABLE SET user_id = '"+reg_userid.getText()+"' WHERE _id = '1';");
						
						db.close();
						
						userIdTxt.setText(reg_userid.getText());
						
				        MainActivity.setUserId(reg_userid.getText().toString());
				        MainActivity.setSensorInterval(s_interval);
				        MainActivity.setTrafficInterval(t_interval);
				        
					} else {
						// 다시 입력
						Toast.makeText(getBaseContext(), "잘못 입력하였습니다\n다시 입력해 주세요", 0).show();
						
					}
					
				}
			})
			.setNegativeButton("취소", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(), "취소하였습니다", 0).show();
				}
			})
			.show();
    		
    		break;
    		
    	case R.id.btn_reset:
    		//Toast.makeText(getBaseContext(), "Sensor : "+s_interval+"\nTraffic : "+t_interval, 0).show();
    		//Log.d("TEST","Sensor : "+s_interval+"\nTraffic : "+t_interval);
    		
    		s_interval = 30;
    		t_interval = 120;
    		
			db = settingDBHelper.getWritableDatabase();
			
			// SQL 명령으로 삽입
			db.execSQL("UPDATE SETTING_TABLE SET sensor_interval = '"+s_interval+"', traffic_interval = '"+t_interval+"' WHERE _id = '1';");
			
			db.close();
			
	        MainActivity.setSensorInterval(s_interval);
	        MainActivity.setTrafficInterval(t_interval);
    		
	        //sensorGroup.check(R.id.sensor_radio0);
	        trafficGroup.check(R.id.traffic_radio2);
	        
	        Toast.makeText(getBaseContext(), "설정이 초기화되었습니다", 0).show();
    		
    		break;
    		
    	case R.id.btn_send:
    		
    		String database_directory = getApplicationContext().getDatabasePath("Setting.db").getParent();
    		
    		String path = "/data/data/com.ramo.networkexperiment/ZipFiles";
    		File newDir = new File(path);
    		
    		if (!newDir.exists()) {
    			newDir.mkdir();
    		}
    		
    		
    		// 현재 연원일 시간 포맷 설정
			SimpleDateFormat formatter_date = new SimpleDateFormat("yyyyMMdd");
			Date currentTime_date = new Date();
			String dTime_date = formatter_date.format(currentTime_date);
    		
    		try {
				ZipFile zipfile = new ZipFile(path+"/[Total]" + com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_" + dTime_date  + ".zip");
				ZipParameters parameters = new ZipParameters();
				parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
				parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
				zipfile.addFolder(database_directory, parameters);
			} catch (Exception e) {
				// 	TODO: handle exception
			}
			
    		
			String Mail_Title = "전체 백업 자료 : " + com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext());
    		String Mail_Body = "사용자 : " + com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext()) + "\n현재까지 모든 데이터 백업입니다";
    		String File_Name = "[Total]" + com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_" + dTime_date  + ".zip";
    		String File_Path = path+"/[Total]" + com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_" + dTime_date  + ".zip";
    		
            new SendMailTask().execute(Mail_Title,Mail_Body,File_Path,File_Name);
    		
    		
    		break;
    		/*
    	case R.id.btn_app:
    		// Get running processes
    		PackageManager pm  = this.getPackageManager();
    		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
    		Iterator<ApplicationInfo> appIter = packages.iterator();
    		while(appIter.hasNext())
    		{
    			ApplicationInfo appInfo = appIter.next();
    			
    			// Get UID of the selected process
    			int uid = appInfo.uid;
    			
    			// Get traffic data
    			long up_total = TrafficStats.getUidTxBytes(uid);
    			long down_total = TrafficStats.getUidRxBytes(uid);
    			
    			
    			// 패키지명으로 해당 앱 이름 뽑아오기
				String appname = new String();
				try {
					appname = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
				} catch (NameNotFoundException error) {
					//error.printStackTrace();
				}
				
				//Log.d("APP","Name : " + appname + "       UID : "+uid+ "       UP : " + up_total + " DOWN : " + down_total);
				
				Log.d("APP","Name : " + appname + "       UID : "+uid+ "       TOTAL : " + TextFormat.formatByte(up_total + down_total));
				
				//Log.d("data", String.valueOf(TrafficStats.(uid) + TrafficStats.getUidTxBytes(uid)));
    		}
    		
    		break;
    		
    		
    	case R.id.btn_app2:
    		// Get running processes
    		PackageManager pm2  = this.getPackageManager();
    		List<ApplicationInfo> packages2 = pm2.getInstalledApplications(PackageManager.GET_META_DATA);
    		Iterator<ApplicationInfo> appIter2 = packages2.iterator();
    		while(appIter2.hasNext())
    		{
    			ApplicationInfo appInfo = appIter2.next();
    			
    			// Get UID of the selected process
    			int uid = appInfo.uid;
    			
    			// Get traffic data
    			long total = getTotalBytesManual(uid);
    			
    			
    			// 패키지명으로 해당 앱 이름 뽑아오기
				String appname = new String();
				try {
					appname = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
				} catch (NameNotFoundException error) {
					//error.printStackTrace();
				}
				
				//Log.d("APP","Name : " + appname + "       UID : "+uid+ "       UP : " + up_total + " DOWN : " + down_total);
				
				Log.d("APP","Name : " + appname + "       UID : "+uid+ "       TOTAL : " + TextFormat.formatByte(total));
				
				//Log.d("data", String.valueOf(TrafficStats.(uid) + TrafficStats.getUidTxBytes(uid)));
    		}
    		
    		break;
    		*/
    	}
    	
    }
	
	private Long getTotalBytesManual(int localUid){

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
		        BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
		        String receivedLine;
		        String sentLine;

		        if ((receivedLine = brReceived.readLine()) != null) {
		            textReceived = receivedLine;
		        }
		        if ((sentLine = brSent.readLine()) != null) {
		            textSent = sentLine;
		        }

		    }
		    catch (IOException e) {

		    }
		 return Long.valueOf(textReceived).longValue() + Long.valueOf(textSent).longValue();

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
	
	
	
	class SettingDBHelper extends SQLiteOpenHelper {
    	public SettingDBHelper(Context context) {
    		super(context, "Setting.db", null, 1);
    	}
    	
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("CREATE TABLE SETTING_TABLE ( _id INTEGER PRIMARY KEY AUTOINCREMENT , user_id TEXT, sensor_interval TEXT, traffic_interval TEXT, last_sensor_db_for_upload TEXT, last_traffic_db_for_upload TEXT, OS TEXT, MODEL TEXT);");
    	}
    	
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		//db.execSQL("DROP TABLE IF EXISTS SENSOR_TABLE");
    		//onCreate(db);
    	}
    }
	
	
	
	
}
