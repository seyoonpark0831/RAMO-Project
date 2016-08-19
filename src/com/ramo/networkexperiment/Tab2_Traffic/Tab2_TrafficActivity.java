package com.ramo.networkexperiment.Tab2_Traffic;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ramo.networkexperiment.R;


public class Tab2_TrafficActivity extends Activity {

	
	static TextView trafficUpdateTimeTxt;
	TextView totalMobileTxt;
	TextView totalWifiTxt;
	ListView trafficListView;
	
	private List<TrafficInfo> trafficInfos;
 	private TrafficManagerAdapter mAdapter;
	
 	long total_mobile_up;
 	long total_mobile_down;
 	long total_wifi_up;
 	long total_wifi_down;
 	
 	// SQLite
 	NetTrafficDBHelper netDBHelper;
 	
	Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab2);
		
		
		trafficUpdateTimeTxt = (TextView)findViewById(R.id.traffic_update_time);
		totalMobileTxt = (TextView)findViewById(R.id.total_mobile);
		totalWifiTxt = (TextView)findViewById(R.id.total_wifi);
		trafficListView = (ListView)findViewById(R.id.traffic_list);
		
		
		
		
		
		trafficInfos = new ArrayList<TrafficInfo>();
		
		// 현재 연원일 시간 포맷 설정
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date currentTime = new Date();
        String dTime = formatter.format(currentTime);
		trafficUpdateTimeTxt.setText("LastUpdate : "+dTime);
		
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
		
		trafficInfos.clear();
		
		total_mobile_up = 0;
		total_mobile_down = 0;
		total_wifi_up = 0;
		total_wifi_down = 0;
		
		netDBHelper = new NetTrafficDBHelper(getBaseContext());
		SQLiteDatabase db;
		db = netDBHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT * FROM TRAFFIC_TABLE ORDER BY down_current_traffic", null);
		
		
		
		int count = 0;			
		try {
			count = cursor.getCount();
		} catch (Exception e) {
			count = 0;
		}
		
		
		if (count == 0) {
			// TRAFFIC_TABLE에 데이터 ROW 존재하지 않는 경우
			
			
	        
		} else {
			// TRAFFIC_TABLE에 데이터 ROW 존재하는 경우
			
			
			while (cursor.moveToNext() == true) {
				int db_uid = cursor.getInt(cursor.getColumnIndex("uid"));
				String db_package_name = cursor.getString(cursor.getColumnIndex("package_name"));
				String db_app_name = cursor.getString(cursor.getColumnIndex("app_name"));
				long db_up_mobile 	= cursor.getLong(cursor.getColumnIndex("up_mobile"));
				long db_down_mobile 	= cursor.getLong(cursor.getColumnIndex("down_mobile"));
				long db_up_wifi 		= cursor.getLong(cursor.getColumnIndex("up_wifi"));
				long db_down_wifi 	= cursor.getLong(cursor.getColumnIndex("down_wifi"));
				long db_up_current 	= cursor.getLong(cursor.getColumnIndex("up_current_traffic"));
				long db_down_current	= cursor.getLong(cursor.getColumnIndex("down_current_traffic"));
				
				total_mobile_up = total_mobile_up + db_up_mobile;
				total_mobile_down = total_mobile_down + db_down_mobile;
				total_wifi_up = total_wifi_up + db_up_wifi;
				total_wifi_down = total_wifi_down + db_down_wifi;
				
				
						
		        // 패키지명으로 해당 앱 이름 뽑아오기
				Drawable appicon = null;
				
				try {
					appicon = (Drawable)getPackageManager().getApplicationIcon(db_package_name);							
				} catch (NameNotFoundException error) {
					//error.printStackTrace();
				}	
		        
		        
				
				//trafficInfos.add(new TrafficInfo(appicon, db_app_name, db_package_name, db_uid, db_up_current, db_down_current, db_up_mobile, db_down_mobile, db_up_wifi, db_down_wifi));
				trafficInfos.add(new TrafficInfo(appicon, db_app_name, db_package_name, db_uid, db_up_mobile+db_up_wifi, db_down_mobile+db_down_wifi, db_up_mobile, db_down_mobile, db_up_wifi, db_down_wifi));
				
				
			}
			
			
			mAdapter = new TrafficManagerAdapter(getApplicationContext());
			trafficListView.setAdapter(mAdapter);
			
			
		}
		
		
		String mobileTotalTraffic = "↑"+TextFormat.formatByte(total_mobile_up)+"  ↓"+TextFormat.formatByte(total_mobile_down);
		String wifiTotalTraffic = "↑"+TextFormat.formatByte(total_wifi_up)+"  ↓"+TextFormat.formatByte(total_wifi_down);
		
		totalMobileTxt.setText(mobileTotalTraffic);
		totalWifiTxt.setText(wifiTotalTraffic);
		
		cursor.close();
		netDBHelper.close();
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
	
	
	static class ViewHolder{
     	ImageView ti_appicon;
     	TextView ti_appname;
     	TextView ti_apptraffic;
     	TextView ti_mobile_up_traffic;
     	TextView ti_mobile_down_traffic;
     	TextView ti_wifi_up_traffic;
     	TextView ti_wifi_down_traffic;
     }
	
	private final class TrafficManagerAdapter extends BaseAdapter{
     	
     	private LayoutInflater mInflater;
     	public TrafficManagerAdapter(Context context) {
     		mInflater = LayoutInflater.from(context);
 		}

 		public int getCount() {
 			return trafficInfos.size();
 		}

 		public Object getItem(int position) {
 			return trafficInfos.get(position);
 		}

 		public long getItemId(int position) {
 			return position;
 		}

 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = null;
 			ViewHolder holder = null;
 			if(convertView != null){
 				view = convertView;
 				holder = (ViewHolder) view.getTag();
 			}else{
 				view = mInflater.inflate(R.layout.traffic_manager_item, null);
 				holder = new ViewHolder();
 				holder.ti_appicon = (ImageView) view.findViewById(R.id.ti_appicon);
 				holder.ti_appname = (TextView) view.findViewById(R.id.ti_appname);
 				holder.ti_apptraffic = (TextView) view.findViewById(R.id.ti_apptraffic);
 				holder.ti_mobile_up_traffic = (TextView) view.findViewById(R.id.ti_mobile_up_traffic);
 				holder.ti_mobile_down_traffic = (TextView) view.findViewById(R.id.ti_mobile_down_traffic);
 				holder.ti_wifi_up_traffic = (TextView) view.findViewById(R.id.ti_wifi_up_traffic);
 				holder.ti_wifi_down_traffic = (TextView) view.findViewById(R.id.ti_wifi_down_traffic);
 				view.setTag(holder);
 			}
 			
 			TrafficInfo trafficInfo = trafficInfos.get(position);
 			
 			holder.ti_appicon.setImageDrawable(trafficInfo.getAppicon());
 			holder.ti_appname.setText(trafficInfo.getAppname());
 			//int uid = trafficInfo.getUid();
 			
 			holder.ti_apptraffic.setText("TOTAL ↑ : "+TextFormat.formatByte(trafficInfo.getTotalUp())+"    ↓ : "+TextFormat.formatByte(trafficInfo.getTotalDown()));
 			
 			holder.ti_mobile_up_traffic.setText("M↑ "+TextFormat.formatByte(trafficInfo.getMobileUp()));
 			holder.ti_mobile_down_traffic.setText("M↓ "+TextFormat.formatByte(trafficInfo.getMobileDown()));
 			
 			holder.ti_wifi_up_traffic.setText("W↑ "+TextFormat.formatByte(trafficInfo.getWifiUp()));
 			holder.ti_wifi_down_traffic.setText("W↓ "+TextFormat.formatByte(trafficInfo.getWifiDown()));
 			
 			
 			
 			return view;
 		}
     	
     }
	
	
	
	public static void updateTrafficTime(Context context) {
		
		// 현재 연원일 시간 포맷 설정
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date currentTime = new Date();
        String dTime = formatter.format(currentTime);
		
        
        
        if (trafficUpdateTimeTxt != null) {
        	trafficUpdateTimeTxt.setText("LastUpdate : "+dTime);
        }
        
	}
	
    
    /*
    public void onStop() {
    	super.onStop();
    	intent = new Intent(this, NetworkTrackService.class);
		stopService(intent);
    }
	*/
	
	
	/*
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
		int conn = MainActivity.getConnectivityStatus(context);
		String status = null;
		if (conn == MainActivity.TYPE_WIFI) {
			status = "WIFI";
		} else if (conn == MainActivity.TYPE_MOBILE) {
			status = "MOBILE";
		} else if (conn == MainActivity.TYPE_NOT_CONNECTED) {
			status = "NOT";
		}
		return status;
	}
	
	public static String getCurrentStatusString(Context context) {
		return current_status;
	}
	
	public static void setCurrentStatusString(String string) {
		current_status = string;
	}
	*/
}
