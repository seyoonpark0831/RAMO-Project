package com.ramo.networkexperiment.Tab3_Sensor;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.os.Message;
import android.view.Menu;
import android.widget.TextView;

import com.ramo.networkexperiment.MainActivity;
import com.ramo.networkexperiment.R;

public class Tab3_SensorActivity extends Activity implements MicrophoneInputListener {

	
	// 센서 매니져
    SensorManager mSm;
    ConnectivityManager cm;
    
    // GPS
    LocationManager mLocMan;
    LocationManager mLocMan_gps;
    LocationManager mLocMan_network;
    
    String mProvider;
    
    
	String light;
    String proximity;
    String sound_level;
    String gravity;
    //String battery_level;
    String battery_plug;
    String accel_sensor;
    String wifi;
	
    String lat_gps;
    String lon_gps;
    String lat_net;
    String lon_net;
    
    String pos_gps;
    String pos_net;
    
    boolean gps_flag;
    boolean net_flag;
    
    
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
    
    
    
    boolean mQuit;
    boolean sound_flag;
    
    TextView lightTxt;
    TextView proxiTxt;
    TextView soundTxt;
    TextView batteryTxt;
    TextView gpsTxt;
    TextView accelTxt;
    
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab3);
		
		lightTxt = (TextView)findViewById(R.id.light_txt);
		proxiTxt = (TextView)findViewById(R.id.proxi_txt);
		soundTxt = (TextView)findViewById(R.id.sound_txt);
		batteryTxt = (TextView)findViewById(R.id.battery_txt);
		gpsTxt = (TextView)findViewById(R.id.gps_txt);
		accelTxt = (TextView)findViewById(R.id.accel_txt);
		
		
		mSm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mLocMan_gps = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mLocMan_network = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		mProvider = mLocMan.getBestProvider(new Criteria(), true);
		
		// Here the micInput object is created for audio capture.
	    // It is set up to call this object to handle real time audio frames of
	    // PCM samples. The incoming frames will be handled by the
	    // processAudioFrame method below.
		
	    micInput = new MicrophoneInput(this);
		
	    sound_flag = true;
		mQuit = false;
		
		int delay = SensorManager.SENSOR_DELAY_UI;
		mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_LIGHT), delay);
    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_PROXIMITY), delay);
    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delay);
    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), delay);
    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GRAVITY), delay);
		
    	lat_gps = "";
    	lon_gps = "";
    	lat_net = "";
    	lon_net = "";
    	pos_gps = "";
    	pos_net = "";
    	
    	
    	
    	
    	
		SensorThread thread = new SensorThread(null, mHandler);
		thread.start();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	}
	
	
	public void onResume() {
		super.onResume();
		
		int delay = SensorManager.SENSOR_DELAY_UI;
		mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_LIGHT), delay);
    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_PROXIMITY), delay);
    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delay);
    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), delay);
    	mSm.registerListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GRAVITY), delay);
		
    	mLocMan_gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, mListener_gps);
		mLocMan_network.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, mListener_network);
    	
		sound_flag = true;
		mQuit = false;
	}
	
	public void onPause() {
		super.onPause();
		mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_LIGHT));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GRAVITY));
    	
    	mLocMan_gps.removeUpdates(mListener_gps);
		mLocMan_network.removeUpdates(mListener_network);
    	
		mQuit = true;
	}
	
	public void onDestroy() {
		super.onDestroy();
		mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_LIGHT));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    	mSm.unregisterListener(mSensorListener, mSm.getDefaultSensor(Sensor.TYPE_GRAVITY));
    	
    	mLocMan_gps.removeUpdates(mListener_gps);
		mLocMan_network.removeUpdates(mListener_network);
    	
		mQuit = true;
	}

	class SensorThread extends Thread {
		MainActivity mParent;
		Handler mHandler;
		
		
		
		public SensorThread(MainActivity parent, Handler handler) {
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
	
	
	Handler mHandler = new Handler() {
		
		
		public void handleMessage (Message msg) {
			if (msg.what == 0) {
				// 배터리 잔량 체크하는 코드
				Intent bat = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				int battery_level = bat.getIntExtra("level", -1);
				int battery_plug = bat.getIntExtra("plugged", -1);
				if (battery_plug == 0) {
					Tab3_SensorActivity.this.battery_plug = "No Charging";
				} else {
					Tab3_SensorActivity.this.battery_plug = "Charging";
				}
				
				batteryTxt.setText(Tab3_SensorActivity.this.battery_plug+"\n"+battery_level+"%");
								
				
				// GPS 좌표 갱신 (업데이트 종료는 GPS 업데이터 된 메서드에서 바로 한다)
				//mLocMan.requestLocationUpdates(mProvider, 10, 0, mListener);
				
				//lat_gps = "";
		    	//lon_gps = "";
		    	//lat_net = "";
		    	//lon_net = "";
		    	//pos_lat = "";
		    	//pos_lon = "";
				
		    	gps_flag = false;
		    	net_flag = false;
		    	
				
				
				
				
				
				//Toast.makeText(SensorService.this,"센서 출력 : "+ count + "\n위도 : "+gps_lat+"\n경도 : "+gps_long, 0).show();
				
				
				
				
				while(true) {
					readPreferences();
			        micInput.setSampleRate(mSampleRate);
			        micInput.setAudioSource(mAudioSource);
			        micInput.start();
			        try{Thread.sleep(2000);}catch (Exception e) {;}
			        break;
				}
				
				micInput.stop();
				
				//Toast.makeText(MainActivity.this,"소음 측정 : "+sound_level, 0).show();
				
				soundTxt.setText(sound_level);
				
				/*
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
		        */
		        /*
				while (true) {
					try{Thread.sleep(1000);}catch (Exception e) {;}
					if (lat_gps.length() == 0  || lon_gps.length() == 0) {
			        	// GPS로 좌표가 반환되지 않는 경우 -> NETWORK로 좌표를 반환하여 사용한
			        	pos_lat = lat_net;
			        	pos_lon = lon_net;
			        	break;
			        } else if (lat_gps.length() != 0  && lon_gps.length() != 0) {
			        	pos_lat = lat_gps;
			        	pos_lon = lon_gps;
			        	break;
			        }					
				}
				
				while (gps_flag == false) {
					if (net_flag == true) {
						break;
					}
				}
				*/
				
				
		        
		        //gpsTxt.setText("Latitude : "+pos_lat+"\nLongitude : "+pos_lon);
				//gpsTxt.setText("GPS : "+lat_gps+" , "+lon_gps+"\nNET : "+lat_net+" , "+lon_net);
				
				
				
				
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
    			light = "" + v[0];
    			lightTxt.setText(light);
    			break;
    		case Sensor.TYPE_PROXIMITY:
    			proximity = "" + v[0];
    			proxiTxt.setText(proximity);
    			break;
    		case Sensor.TYPE_PRESSURE:
    			
    			break;
    		case Sensor.TYPE_ORIENTATION:
    			//mTxtOrient.setText("방향 = " + ++mOrientCount + "회 : \n azimuth : " + v[0] + "\n pitch : "+ v[1] + "\n roll : " + v[2]);
    			
    			break;
    		case Sensor.TYPE_ACCELEROMETER:
    			//mTxtAccel.setText("가속 = " + ++mAccelCount + "회 : \n X : " + v[0] + "\n Y : "+ v[1] + "\n Z : " + v[2]);
    			accel_sensor = "x : "+v[0] +"\ny : "+v[1]+"\nz : "+v[2];
    			accelTxt.setText(accel_sensor);
    			break;
    		case Sensor.TYPE_MAGNETIC_FIELD:
    			//mTxtMagnetic.setText("자기 = " + ++mMagneticCount + "회 : \n X : " + v[0] + "\n Y : "+ v[1] + "\n Z : " + v[2]);
    			//accel_sensor = "x : "+v[0] +"\ny : "+v[1]+"\nz : "+v[2];
    			break;
    		case Sensor.TYPE_GRAVITY:
    			gravity = "x : "+v[0] +"\ny : "+v[1]+"\nz : "+v[2];
    			break;
    		
    		
    		}
    		
    		
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
			// TODO Auto-generated method stub
			gps_flag = true;
			
			lat_gps = ""+location.getLatitude();
			lon_gps = ""+location.getLongitude();
			
			pos_gps = lat_gps + " , " + lon_gps;
			
			gpsTxt.setText("GPS : "+pos_gps+"\nNET : "+pos_net);
			
			//Log.d("GPS", "LAT : "+lat_gps+"    LON : "+lon_gps);
			
			//gpsTxt.setText("Latitude : "+location.getLatitude()+"\nLongitude : "+location.getLongitude());
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
			// TODO Auto-generated method stub
			net_flag = true;
			
			lat_net = ""+location.getLatitude();
			lon_net = ""+location.getLongitude();	
			//gpsTxt.setText("Latitude : "+location.getLatitude()+"\nLongitude : "+location.getLongitude());
			
			pos_net = lat_net + " , " + lon_net;
			
			gpsTxt.setText("GPS : "+pos_gps+"\nNET : "+pos_net);
			
			//Log.d("NETWORK", "LAT : "+lat_net+"    LON : "+lon_net);
			
			
			
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
		  sound_level = ""+df.format(20 + rmsdB);
		  //soundTxt.setText(sound_level);
		  //Toast.makeText(this,"소음 : "+ sound_level, 0).show();
	  }
    
}

	

