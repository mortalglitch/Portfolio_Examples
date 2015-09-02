package com.omegaraven.kitwidget;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.omegaraven.kitwidget.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.RemoteViews;

public class KITWidgetService extends Service {
	//public static final String PREFS_NAME = "com.omegaraven.reachoutpref";
	//private static final String REMOVE_STORE = "reachout.removeStore";

	// Declarations
	private Bitmap cPhoto;
	private long createTime, currentTime, measuredTime;
	private int daysPassed;
	private String stringDaysWanted;
	private int daysWanted;
	private String selectedName, selectedNumber, contactUriString;

	//private static int[] removeArray = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		KITWidgetDataSource datasource = new KITWidgetDataSource(this);
		Context con = KITWidgetService.this;
		currentTime = System.currentTimeMillis();

		// Section receiving extra data
		Bundle extras = intent.getExtras();

		if (extras == null) {
			//Log.d("ServiceLog", "Bundle Failed");
		}

		// new
//		if (removeArray == null) {
//			removeArray = loadRemoved(REMOVE_STORE, con);
//		}

		// Begin remove
		if (extras.getBoolean("removeFlag")) {
			int removedID = extras.getInt("removedID", 0);
			removeContact(removedID, con, datasource);
		}

		if (extras.getBoolean("updateFlag")) {
			int[] currentawIDS = intent.getIntArrayExtra("AWIDS");
			int n = currentawIDS.length;
			if (n > 0) {
				for (int i = 0; i < n; i++) {
					int currentawID = currentawIDS[i];

					if (currentawID != AppWidgetManager.INVALID_APPWIDGET_ID) {
						getData(currentawID, datasource);

						AppWidgetManager appWidgetManager = AppWidgetManager
								.getInstance(this.getApplicationContext());

						Uri contactUri = Uri.parse(contactUriString);

						RemoteViews rV = new RemoteViews(this
								.getApplicationContext().getPackageName(),
								R.layout.kitwidget_widget_layout);
						buildCurrentWidget(rV, currentawID, contactUri);

						/*
						 * Calling timeCalc which determines how many days have
						 * passed
						 */
						timeCalc(rV);

						/*
						 * Handle Click to Dial This area will need to get
						 * additional work done in order to be able to reset the
						 * call timer.
						 */
						Intent in = new Intent(con.getApplicationContext(),
								KITWidgetDialer.class);
						in.putExtra("CURRENT_AWID", currentawID);
						in.putExtra("CONTACT_NUMBER", selectedNumber);
						PendingIntent pi = PendingIntent.getService(
								con.getApplicationContext(), currentawID, in,
								PendingIntent.FLAG_CANCEL_CURRENT);
						rV.setOnClickPendingIntent(R.id.bWidgetCall, pi);

						appWidgetManager.updateAppWidget(currentawID,
								rV);

						if (extras.getBoolean("NewBuild")) {
							ComponentName widgetComponent = new ComponentName(
									con, KITWidget.class);
							int[] widgetIds = appWidgetManager
									.getAppWidgetIds(widgetComponent);
							Intent update = new Intent();
							update.putExtra(
									AppWidgetManager.EXTRA_APPWIDGET_ID,
									currentawID);
							update.putExtra(
									AppWidgetManager.EXTRA_APPWIDGET_IDS,
									widgetIds);
							update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
							con.sendBroadcast(update);
						}
					}
				}
			}
		}
		stopSelf();
		return START_NOT_STICKY;

	}

	private void buildCurrentWidget(RemoteViews rV, int currentawID,
			Uri contactUri) {

		rV.setTextViewText(R.id.tvWidgetName, selectedName);
		rV.setTextViewText(R.id.tvWidgetNumber, selectedNumber);
		if (contactUriString != "defaultString") {
			cPhoto = BitmapFactory.decodeStream(openPhoto(contactUri));
			rV.setImageViewBitmap(R.id.ivWidgetContactPic, cPhoto);
		}

	}

	private void getData(int currentawID, KITWidgetDataSource dataSource) {

		dataSource.open();
		selectedName = dataSource.getContactName(String.valueOf(currentawID));
		selectedNumber = dataSource.getContactNumber(String
				.valueOf(currentawID));
		createTime = dataSource.getContactCreateTime(String
				.valueOf(currentawID));
		contactUriString = dataSource.getContactImageUri(String
				.valueOf(currentawID));
		stringDaysWanted = dataSource.getContactDesiredDays(String
				.valueOf(currentawID));
		daysWanted = Integer.parseInt(stringDaysWanted);
		dataSource.close();

	}

	private void timeCalc(RemoteViews rV) {
		daysPassed = 0;
		measuredTime = currentTime - createTime;
		// Log.w("ServiceLogTimeSystem", "measured time: " + measuredTime+ "currentTime " + currentTime + "createTime: " + createTime);
		// while (measuredTime > (long) 86399998) {
		// daysPassed++;
		// measuredTime = measuredTime - (long) 86399998;
		// }
		while (measuredTime > (long) 1000 * 86400) {
			daysPassed++;
			measuredTime = measuredTime - (long) 1000 * 86400;
		}

		if (daysPassed < daysWanted) {
			daysWanted = daysWanted - daysPassed;
			rV.setTextViewText(R.id.bWidgetCall, "" + daysWanted);
		} else {
			rV.setTextViewText(R.id.bWidgetCall, "K.I.T (Call)");
		}

	}

	private void removeContact(int removedID, Context con,
			KITWidgetDataSource datasource) {
		datasource.open();
		if (removedID != 0) {
			datasource.deleteContact(removedID);
			//removeArray = addRemoveId(removeArray, removedID);
			//storeRemoved(removeArray, REMOVE_STORE, con);
		}
		datasource.close();
	}

	public InputStream openPhoto(Uri contactId) {
		Uri displayPhotoUri = Uri.withAppendedPath(contactId,
				Contacts.Photo.DISPLAY_PHOTO);
		try {
			AssetFileDescriptor fd = getContentResolver()
					.openAssetFileDescriptor(displayPhotoUri, "r");
			return fd.createInputStream();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public int[] addRemoveId(int original[], int itemAdd) {
		int[] result = Arrays.copyOf(original, original.length + 1);
		result[original.length] = itemAdd;
		return result;
	}

}
