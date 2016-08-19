package com.ramo.networkexperiment.Tab2_Traffic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

	// SQLite
	NetTrafficDBHelper netDBHelper;
		
	@Override
	public void onReceive(final Context context, final Intent intent) {

		String status = NetworkTrackService.getConnectivityStatusString(context);
		String previous_status =  NetworkTrackService.getPreviousStatusString(context);
		
		if (status != previous_status) {
			//Toast.makeText(context,"NetReceiver1\nPrevious : " + previous_status +"\nNew : "+status, Toast.LENGTH_SHORT).show();
			Log.d("STATUS","NetReceiver1   Previous : " + previous_status +"   New : "+status);
			
			netDBHelper = new NetTrafficDBHelper(context);
			SQLiteDatabase db;
			db = netDBHelper.getWritableDatabase();
			
			
			
			// Get running processes
			PackageManager pm  = context.getPackageManager();
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
							appname = (String)context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
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
							appname = (String)context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(appInfo.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
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
			
			/*
			// Get running processes
			ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
					
			List<RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();
			
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
			
			
			
			for(RunningAppProcessInfo process : runningProcesses) {
				// Get UID of the selected process
				int uid = process.uid;
				
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
				up_total = TrafficStats.getUidTxBytes(uid);
				down_total = TrafficStats.getUidRxBytes(uid);
				
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
							appname = (String)context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(process.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
						} catch (NameNotFoundException error) {
							//error.printStackTrace();
						}				
						
						Log.d("DB INSERT",appname);
						
						// SQL 명령으로 삽입
				        db.execSQL("INSERT INTO TRAFFIC_TABLE VALUES ('"+uid+"',"+"'"+process.processName+"'"+",'"+appname+"',"+"0"+","+"0"+","+"0"+","+"0"+","+up_total+","+down_total+");");
				        
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
							appname = (String)context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(process.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
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
				
			}
				*/
			
			netDBHelper.close();	
			
			NetworkTrackService.setPreviousStatusString(status);
			
			previous_status =  NetworkTrackService.getPreviousStatusString(context);
			
			//Toast.makeText(context,"NetReceiver2\nPrevious : " + previous_status +"\nNew : "+status, Toast.LENGTH_SHORT).show();
			Log.d("STATUS","NetReceiver2   Previous : " + previous_status +"   New : "+status);
		}
		
		
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
