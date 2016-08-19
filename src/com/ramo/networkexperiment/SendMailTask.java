package com.ramo.networkexperiment;

import android.os.AsyncTask;
import android.util.Log;

public class SendMailTask extends AsyncTask<String, Void, Void>{
	  @Override
	  protected Void doInBackground(String... params) {
		   // TODO Auto-generated method stub
		   try{
		    
		          GMailSender sender = new GMailSender ("trafficexp.ramo@gmail.com", "ramo1234");
		          
		          /*
		          sender.sendMail("의견보내기", // subject.getText().toString(),
							"TEST", // body.getText().toString(),
							"psy3061@gmail.com", // from.getText().toString(),
							"psy3061@naver.com" // to.getText().toString()
					);
					*/
		          
		          
		          //sender.sendMailWithFile(params[0], params[1],"psy3061@gmail.com","psy3061@naver.com",params[3], params[4]);
		          sender.sendMailWithFile(params[0], params[1],com.ramo.networkexperiment.MainActivity.getUserId(null),"trafficexp.ramo@gmail.com",params[2], params[3]);
		          
		          
		   }catch(Exception e){
				Log.e("Mail send failure", e.getMessage(), e);
				GMailSender sender = new GMailSender ("trafficexp.ramo@gmail.com", "ramo1234");
				sender.sendMailWithFile(params[0], params[1],com.ramo.networkexperiment.MainActivity.getUserId(null),"trafficexp.ramo@gmail.com",params[2], params[3]);
		   }
		   return null;
	  }

	private Object getApplicationContext() {
		// TODO Auto-generated method stub
		return null;
	}
}