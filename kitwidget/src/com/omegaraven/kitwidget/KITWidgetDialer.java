package com.omegaraven.kitwidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class KITWidgetDialer extends Service{
	private final String CURRENT_AWID = "current_awid";
	private final String CONTACT_NUMBER = "contact_number";

	private long currentTime;
	private int currentawID;
	private String contactNumber;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * Setting up data
		 */
		
		KITWidgetDataSource datasource = new KITWidgetDataSource(this);	
		Context con = KITWidgetDialer.this;
		/*
		 * Getting data bundles with the intent
		 */
		Bundle extras = intent.getExtras();
		if(extras == null){
			//Log.w("DialerService", "Bundles null try again");
		}
		
		currentawID = extras.getInt("CURRENT_AWID");
		contactNumber = extras.getString("CONTACT_NUMBER");
		
		if(contactNumber.length() > 0){
			Intent in2 = new Intent(Intent.ACTION_CALL);
			in2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			in2.setData(Uri.parse("tel:" + contactNumber));
			getBaseContext().startActivity(in2);
		}
		
		if(currentawID != 0){
			datasource.open();
			datasource.resetTimer(String.valueOf(currentawID));
			datasource.close();
			int[] appWidgetIDS = {currentawID};
			Intent intentServ = new Intent(con,	KITWidgetService.class);
			intentServ.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, currentawID);
			intentServ.putExtra("AWIDS", appWidgetIDS);
			intentServ.putExtra("updateFlag", true);
									
			con.startService(intentServ);
		
		}
		
		stopSelf();
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
