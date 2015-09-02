package com.omegaraven.kitwidget;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.omegaraven.kitwidget.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class KITWidgetConfig extends Activity implements OnClickListener, OnItemSelectedListener {

	private static final int CONTACT_PICKER_RESULT = 1001;
	//DB Mod
	private static final String CURRENT_ID = "reachout.currentId";
	
	// Database Work
	private KITWidgetDataSource dataSource;
	private KITWidgetContact currentContact;
	private String contactImageUri = "";
	
	Button selectContact, submitButton;
	TextView contactNameView, contactNumberView, contactIdView;
	ArrayAdapter<String> aAdapter, dayAdapter;
	Spinner numberSpinner, daySpinner;
	AppWidgetManager awm;
	ImageView ivContactPic;
	Bitmap cPhoto;
	Context c;
	Uri result;
	int awID;
	int contactFlag = 0;
	long currentTime = 0;
	boolean firstSetup = true;
	int numContacts = 0;
	String selectedDays;
	boolean daySelected = false;
	
	Bundle extras;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.kitwidget_config_layout);
		ivContactPic = (ImageView) findViewById(R.id.ivContactPicture);
		selectContact = (Button) findViewById(R.id.bSelect);
		contactNameView = (TextView) findViewById(R.id.tvName);
		contactNumberView = (TextView) findViewById(R.id.tvNumber);
		submitButton = (Button) findViewById(R.id.bSubmit);
		numberSpinner = (Spinner) findViewById(R.id.numberSpinner);
		daySpinner = (Spinner) findViewById(R.id.spDays);
		
		aAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, 0);
		dayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, 0);
		aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		numberSpinner.setAdapter(aAdapter);
		numberSpinner.setOnItemSelectedListener(this);
		daySpinner.setAdapter(dayAdapter);
		daySpinner.setOnItemSelectedListener(this);
		
		buildDayAdapter();

		selectContact.setOnClickListener(this);
		submitButton.setOnClickListener(this);
		
		dataSource = new KITWidgetDataSource(this);
		currentContact = new KITWidgetContact();
		dataSource.open();

		c = KITWidgetConfig.this;

		// Getting information about the widget
		Intent i = getIntent();
		extras = i.getExtras();
		
		if (extras != null) {
			// ID was IDS.... which jacked up my cornflakes
			awID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
		} else {
			finish();
		}
		
		awm = AppWidgetManager.getInstance(c);
		super.onCreate(savedInstanceState);
	}

	private void buildDayAdapter() {
		for(int i=0; i <= 30; i++){
			// Quick Hack to fix the crash on 0 selection
//			if(i == 0){
//				dayAdapter.add("--");
//			} else {			
				dayAdapter.add(String.valueOf(i+1));
				dayAdapter.notifyDataSetChanged();
			
		}		
	}

	@Override
	public void onClick(View v) {
		if (extras != null) {
			awID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		} else {
			finish();
		}
		if (v == selectContact) {
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,	Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
		} else if (v == submitButton) {
			if (contactFlag == 1) {
				// Getting current time and setting up preferences for it
				currentTime = System.currentTimeMillis();
				selectedDays = daySpinner.getSelectedItem().toString();
				// Convert Data
				String selectedName = contactNameView.getText().toString();
				String selectedNumber = contactNumberView.getText().toString();
				currentContact.setReachOutContact(selectedName, selectedNumber, currentTime, contactImageUri, String.valueOf(awID), selectedDays);
				
				Long currentId = dataSource.createContact(currentContact);
								
				Intent intent = new Intent(c.getApplicationContext(),
						KITWidgetService.class);
				
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID);
				
				int[] currentAWIDS = {awID};
				
				intent.putExtra(CURRENT_ID, currentId);
				intent.putExtra("AWIDS", currentAWIDS);
				intent.putExtra("updateFlag", true);
				intent.putExtra("NewBuild", true);
								
				c.startService(intent);
				setResult(RESULT_OK, intent);

				finish();

			} else {
				Toast.makeText(c, "Please Select a Contact and Number of days.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				result = data.getData();

				ContentResolver cr = getContentResolver();
				Cursor cur = cr.query(result, null, null, null, null);

				aAdapter.clear();
				aAdapter.notifyDataSetChanged();
				if (cur.getCount() > 0) {
					while (cur.moveToNext()) {
						// read id
						String id = cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts._ID));
						/** read names **/
						String displayName = cur
								.getString(cur
										.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						/** Phone Numbers **/
						Cursor pCur = cr
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = ?", new String[] { id },
										null);
						while (pCur.moveToNext()) {
							String number = pCur
									.getString(pCur
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							pCur
									.getString(pCur
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));							
							aAdapter.add(number);
							aAdapter.notifyDataSetChanged();
							contactNameView.setText(displayName);
						}
						pCur.close();
					}					
					
				}
				cur.close();
				
				contactImageUri = result.toString();
				cPhoto = BitmapFactory.decodeStream(openPhoto(result));
				ivContactPic.setImageBitmap(cPhoto);
				contactFlag = 1;

				break;

			}
		} else {
			// Possibly log error
		}
	}

	
	
	public InputStream openPhoto(Uri contactId) {
		// Attempt at pulling a photo
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
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// onItemSelected(AdapterView<?> parent, View view,int pos, long id) 
		// Attempt to get the data from the Spinner
		switch(parent.getId()){
		case R.id.numberSpinner:
			String selectedNumber = (String) parent.getItemAtPosition(pos);
			contactNumberView.setText(selectedNumber);
		case R.id.spDays:
			selectedDays = (String) parent.getItemAtPosition(pos);
			//daySelected = true;
		}		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub		
	}
	
	@Override
	protected void onResume(){
		dataSource.open();
		super.onResume();
	}
	
	@Override
	protected void onPause(){
		dataSource.close();
		super.onPause();
	}

}
