package com.omegaraven.kitwidget;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class KITWidget extends AppWidgetProvider {
	//public static final String PREFS_NAME = "com.omegaraven.reachoutpref";
	private PendingIntent service = null;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final Calendar TIME = Calendar.getInstance();
		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		final AlarmManager m = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		TIME.set(Calendar.MINUTE, 0);
		TIME.set(Calendar.SECOND, 0);
		TIME.set(Calendar.MILLISECOND, 0);

		final Intent in = new Intent(context, KITWidgetService.class);

		in.putExtra("AWIDS", appWidgetIds);
		in.putExtra("updateFlag", true);
		if (service == null) {
			service = PendingIntent.getService(context, 0, in,
					PendingIntent.FLAG_CANCEL_CURRENT);
		}

		m.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(),
				1000 * 10800, service); // Check every 3 hours
	}

	@Override
	public void onDisabled(Context context) {
		final AlarmManager m = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		m.cancel(service);
		super.onDisabled(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {

		for (int i = 0; i < appWidgetIds.length; i++) {
			int awID = appWidgetIds[i];

			Intent intent = new Intent(context.getApplicationContext(),
					KITWidgetService.class);
			intent.putExtra("removedID", awID);
			intent.putExtra("removeFlag", true);
			Toast.makeText(context,
					"KIT widget removed id: " + String.valueOf(awID),
					Toast.LENGTH_SHORT).show();
			context.startService(intent);
			continue;
		}
		
		AppWidgetManager awm = AppWidgetManager.getInstance(context);
		int[] ids;
		ComponentName thisWidget = new ComponentName(context,
				KITWidget.class);
		ids = awm.getAppWidgetIds(thisWidget);		
		onUpdate(context, awm, ids);

		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int appWidgetId = intent.getIntExtra(
				AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
			if (PhoneBootReceiver.wasPhoneBooted) {
				AppWidgetManager awm = AppWidgetManager.getInstance(context);
				int[] appWidgetIds;
				ComponentName thisWidget = new ComponentName(context,
						KITWidget.class);
				appWidgetIds = awm.getAppWidgetIds(thisWidget);
				PhoneBootReceiver.wasPhoneBooted = false;
				onUpdate(context, awm, appWidgetIds);
			}
			super.onReceive(context, intent);
		} else {
			if (PhoneBootReceiver.wasPhoneBooted) {
				AppWidgetManager awm = AppWidgetManager.getInstance(context);
				int[] appWidgetIds;
				ComponentName thisWidget = new ComponentName(context,
						KITWidget.class);
				appWidgetIds = awm.getAppWidgetIds(thisWidget);
				PhoneBootReceiver.wasPhoneBooted = false;
				onUpdate(context, awm, appWidgetIds);
				super.onReceive(context, intent);
			}
		}

	}

}
