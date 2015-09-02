package com.omegaraven.kitwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneBootReceiver extends BroadcastReceiver{

	public static boolean wasPhoneBooted = false;
	/*
	 * This receiver will check for the phone to boot.
	 * It will then trigger the widget to update.
	 */
	@Override
	public void onReceive(Context con, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			wasPhoneBooted = true;
			}		
	}

}
