package com.ramo.networkexperiment.Tab3_Sensor;


import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.ramo.networkexperiment.SendMailTask;



public class SensorService extends Service implements MicrophoneInputListener {
	
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
		
		// 현재 연원일 시간 포맷 설정
		SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd_HH)");
        Date currentTime2 = new Date();
        String dTime2 = formatter2.format(currentTime2);
        
        dbName = "[Sensor]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
        
		sDBHelper = new SensorDBHelper(getBaseContext());
		
		gps_flag = false;
	    light_flag = false;
	    proximity_flag = false;
	    sound_flag = false;
		
		
		mSm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mProvider = mLocMan.getBestProvider(new Criteria(), true);
		
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
		
		
		
	}
	
	
	class SensorDBHelper extends SQLiteOpenHelper {
    	public SensorDBHelper(Context context) {
    		//super(context, "SensorData.db", null, 1);
    		super(context, dbName, null, 1);
    	}
    	
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL("CREATE TABLE SENSOR_TABLE ( _id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp TEXT, gps_lat TEXT, gps_long TEXT, light TEXT, proximity TEXT, sound TEXT, gravity_x TEXT, gravity_y TEXT, gravity_z TEXT, battery_level TEXT, battery_plug TEXT, wifi TEXT);");
    		
    		
    		try {
    			if (com.ramo.networkexperiment.MainActivity.getLastSensorDBForUpload(getBaseContext()).length() != 0 && com.ramo.networkexperiment.MainActivity.getLastSensorDBForUpload(getBaseContext()) != dbName) {
        			
        			
        			String Mail_Title = com.ramo.networkexperiment.MainActivity.getLastSensorDBForUpload(getBaseContext());
            		String Mail_Body = com.ramo.networkexperiment.MainActivity.getLastSensorDBForUpload(getBaseContext());
            		String DB_NAME = com.ramo.networkexperiment.MainActivity.getLastSensorDBForUpload(getBaseContext());
            		String dbPath = getApplicationContext().getDatabasePath(com.ramo.networkexperiment.MainActivity.getLastSensorDBForUpload(getBaseContext())).getAbsolutePath();
            		
                    new SendMailTask().execute(Mail_Title,Mail_Body,dbPath,DB_NAME);
                    
                    com.ramo.networkexperiment.MainActivity.setLastSensorDBForUpload(dbName);
                    
            		SQLiteDatabase setting_db = new SettingDBHelper(getBaseContext()).getWritableDatabase();
            		
            		// SQL 명령으로 삽입
            		setting_db.execSQL("UPDATE SETTING_TABLE SET last_sensor_db_for_upload = '"+dbName+"' WHERE _id = '1';");
            		
            		setting_db.close();
            		
        		} else if (com.ramo.networkexperiment.MainActivity.getLastSensorDBForUpload(getBaseContext()).length() == 0) {
        			com.ramo.networkexperiment.MainActivity.setLastSensorDBForUpload(dbName);
        			
        			SQLiteDatabase setting_db = new SettingDBHelper(getBaseContext()).getWritableDatabase();
            		
            		// SQL 명령으로 삽입
            		setting_db.execSQL("UPDATE SETTING_TABLE SET last_sensor_db_for_upload = '"+dbName+"' WHERE _id = '1';");
            		
            		setting_db.close();
        		}
    		} catch (Exception e) {
    			com.ramo.networkexperiment.MainActivity.setLastSensorDBForUpload(dbName);
    			
    			SQLiteDatabase setting_db = new SettingDBHelper(getBaseContext()).getWritableDatabase();
        		
        		// SQL 명령으로 삽입
        		setting_db.execSQL("UPDATE SETTING_TABLE SET last_sensor_db_for_upload = '"+dbName+"' WHERE _id = '1';");
        		
        		setting_db.close();
    		}
    		
    		
    		
    		
    		
    		
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
	
	public void onDestroy() {
		super.onDestroy();
		
		//Toast.makeText(this, "Service End", 0).show();
		/*
		mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_LIGHT));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GRAVITY));
		*/
		
		mSm.unregisterListener(mSensorListener);
		mQuit = true;
		
	}
	
	public int onStartCommand (Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		//Toast.makeText(this, "Service Start!", 0).show();
		mQuit = false;
				
		SensorThread thread = new SensorThread(this, mHandler);
		thread.start();
		return START_STICKY;
	}
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	
	class SensorThread extends Thread {
		SensorService mParent;
		Handler mHandler;
		
		int delay = SensorManager.SENSOR_DELAY_UI;
		
		
		public SensorThread(SensorService parent, Handler handler) {
			mParent = parent;
			mHandler = handler;
		}
		
		
		
		public void run() {
			
			while(mQuit == false) {
				
				//gps_flag = false;
			    //light_flag = false;
			    //proximity_flag = false;
			    //sound_flag = false;
				
				mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_LIGHT), delay);
		    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_PROXIMITY), delay);
		    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delay);
		    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), delay);
		    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GRAVITY), delay);
				
		    	
		    	
		    	
		    	
		    	Message msg = new Message();
				msg.what = 0;
				msg.obj = "";
				mHandler.sendMessage(msg);
		    	
				
			    	
				
				count++;
				try{Thread.sleep(com.ramo.networkexperiment.MainActivity.getSensorInterval(getBaseContext())*1000);}catch (Exception e) {;}
				//try{Thread.sleep(5000);}catch (Exception e) {;}
			}
			
		}
	}
	
	
	Handler mHandler = new Handler() {
		
		SQLiteDatabase db;
		
		public void handleMessage (Message msg) {
			if (msg.what == 0) {
				// 배터리 잔량 체크하는 코드
				Intent bat = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				int battery_level = bat.getIntExtra("level", -1);
				int battery_plug = bat.getIntExtra("plugged", -1);
				if (battery_plug == 0) {
					SensorService.this.battery_plug = "No Charging";
				} else {
					SensorService.this.battery_plug = "Charging";
				}
				
				//Toast.makeText(SensorService.this,"배터리 충전 : "+SensorService.this.battery_plug+"\n배터리 레벨 : "+battery_level, 0).show();
				
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n" + light + "\n" + proximity + "\n" + accel_sensor, 0).show();
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n배터리 : "+battery, 0).show();
				
				
				// GPS 좌표 갱신 (업데이트 종료는 GPS 업데이터 된 메서드에서 바로 한다)
				mLocMan.requestLocationUpdates(mProvider, 0, 0, mListener);
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
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		        Date currentTime = new Date();
		        String dTime = formatter.format(currentTime);
		        
		        
		        
		        
		        
				
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
				SimpleDateFormat formatter2 = new SimpleDateFormat("(yyyyMMdd_HH)");
		        Date currentTime2 = new Date();
		        String dTime2 = formatter2.format(currentTime2);
		        
		        dbName = "[Sensor]"+com.ramo.networkexperiment.MainActivity.getUserId(getBaseContext())+"_"+dTime2+".db";
		        
		        String DB_NAME = dbName;
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
	    		
	    		db = sDBHelper.getWritableDatabase();
	    		// SQL 명령으로 삽입
		        db.execSQL("INSERT INTO SENSOR_TABLE VALUES (null,"+"'"+dTime+"'"+","+gps_lat+","+gps_long+","+light+","+proximity+","+sound_level+","+gravity_x+","+gravity_y+","+gravity_z+","+battery_level+",'"+SensorService.this.battery_plug+"',"+"'"+wifi+"'"+");");
		        
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
	
}
