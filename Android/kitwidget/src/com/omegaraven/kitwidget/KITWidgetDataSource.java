package com.omegaraven.kitwidget;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class KITWidgetDataSource {
	// Test
	private static boolean contactCreated;
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_NUMBER, MySQLiteHelper.COLUMN_CREATETIME, MySQLiteHelper.COLUMN_IMAGE, MySQLiteHelper.COLUMN_WIDGETID, MySQLiteHelper.COLUMN_DAYS};
	
	public KITWidgetDataSource(Context context){
		dbHelper = new MySQLiteHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();	
	}
	
	// This needs to be cleaned up to create the entire Database
	public long createContact(KITWidgetContact contact){
		ContentValues values = new ContentValues();
	    values.put(MySQLiteHelper.COLUMN_NAME, contact.getName());
	    values.put(MySQLiteHelper.COLUMN_NUMBER, contact.getNumber());
	    values.put(MySQLiteHelper.COLUMN_CREATETIME, contact.getCreateTime());
	    values.put(MySQLiteHelper.COLUMN_IMAGE, contact.getImage());
	    values.put(MySQLiteHelper.COLUMN_WIDGETID, contact.getAppWidId());
	    values.put(MySQLiteHelper.COLUMN_DAYS, contact.getDays());
	    // Need to understand these below
	    long insertId = database.insert(MySQLiteHelper.TABLE_CONTACTS, null,
	        values);
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
	        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    
	    cursor.close();
	    contactCreated = true;
	    return insertId;
	    
	}
	
	public void deleteContact(int contactawID) {
	    int id = contactawID;
	    database.delete(MySQLiteHelper.TABLE_CONTACTS, MySQLiteHelper.COLUMN_WIDGETID
	        + " = " + id, null);
	  }

	  private KITWidgetContact cursorToContact(Cursor cursor) {
	    KITWidgetContact contact = new KITWidgetContact();
	    contact.setId(cursor.getLong(0));
	    contact.setReachOutContact(cursor.getString(1), null, 0, null, null,null);
	    return contact;
	  }
	  
	  /*
	   * Added mod below to put the data into a ReachOutContact Object
	   */
	  public KITWidgetContact getContact(String awID){
		  		  		  
		  KITWidgetContact currentContact = new KITWidgetContact();
		  if(contactCreated){
		  String[] projection = {
				  MySQLiteHelper.COLUMN_ID,
				  MySQLiteHelper.COLUMN_NAME,
				  MySQLiteHelper.COLUMN_NUMBER,
				  MySQLiteHelper.COLUMN_CREATETIME,
				  MySQLiteHelper.COLUMN_IMAGE,
				  MySQLiteHelper.COLUMN_WIDGETID,
				  MySQLiteHelper.COLUMN_DAYS};
		  String[] searchCriteria = {String.valueOf(awID)};
		  
		  Cursor cur = database.query(dbHelper.TABLE_CONTACTS, projection, dbHelper.COLUMN_WIDGETID, searchCriteria, null, null, null);
		  
		  cur.moveToFirst();
		  long itemId = cur.getLong(cur.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_ID));
		  String conName = cur.getString(cur.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_NAME));
		  String conNumber = cur.getString(cur.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_NUMBER));
		  long conCreateTime = cur.getLong(cur.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_CREATETIME));
		  String conImage = cur.getString(cur.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_IMAGE));
		  String conWidget = cur.getString(cur.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_WIDGETID));
		  String conDays = cur.getString(cur.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_DAYS));
		  
		  currentContact.setReachOutContact(conName, conNumber, conCreateTime, conImage, conWidget, conDays);
		  currentContact.setId(itemId);
		  cur.close();
		  
		 
		  } else {
			  currentContact.setReachOutContact("fail", "fail", 0, "fail", "fail", "fail");
			  currentContact.setId(0);
		  }
		  
		  return currentContact;
	  }
	  
	  /*
	   * The direct contact return method is failing I will try individuals similar to getContactName below.
	   */
	  public String getContactName(String awID){
		  String contactName = "";
		  		  
		  String contactQuery = "SELECT name FROM reachouthelp WHERE widgetid =?";
		  Cursor cur = database.rawQuery(contactQuery, new String[] {awID});
		  
		  if(cur != null){
			  cur.moveToFirst();
			  contactName = cur.getString(0);
			  cur.close();
			  contactCreated = false;
			  return contactName;
		  }
	
		  return contactName;
	  }
	  
	  public String getContactNumber(String awID){
		  String contactNumber = "";
		  		  
		  String contactQuery = "SELECT number FROM reachouthelp WHERE widgetid =?";
		  Cursor cur = database.rawQuery(contactQuery, new String[] {awID});
		  
		  if(cur != null){
			  cur.moveToFirst();
			  contactNumber = cur.getString(0);
			  cur.close();
			  contactCreated = false;
			  return contactNumber;
		  }			  
		  return contactNumber;
	  }
	  
	  public String getContactImageUri(String awID){
		  String contactImageUri = "";
		  		  
		  String contactQuery = "SELECT image FROM reachouthelp WHERE widgetid =?";
		  Cursor cur = database.rawQuery(contactQuery, new String[] {awID});
		  
		  if(cur != null){
			  cur.moveToFirst();
			  contactImageUri = cur.getString(0);
			  cur.close();
			  contactCreated = false;
			  return contactImageUri;
		  }			  
		  return contactImageUri;
	  }
	  
	  public String getContactDesiredDays(String awID){
		  String contactDesiredDays = "";
		  		  
		  String contactQuery = "SELECT days FROM reachouthelp WHERE widgetid =?";
		  Cursor cur = database.rawQuery(contactQuery, new String[] {awID});
		  if(cur != null){
			  cur.moveToFirst();
			  contactDesiredDays = cur.getString(0);
			  cur.close();
			  contactCreated = false;
			  return contactDesiredDays;
		  }			  
		  return contactDesiredDays;
	  }
	  
	  public long getContactCreateTime(String awID){
		  long contactCreateTime = 0;
		  		  
		  String contactQuery = "SELECT createtime FROM reachouthelp WHERE widgetid =?";
		  Cursor cur = database.rawQuery(contactQuery, new String[] {awID});		  
		  if(cur != null){
			  cur.moveToFirst();
			  contactCreateTime = cur.getLong(0);
			  cur.close();
			  contactCreated = false;
			  return contactCreateTime;
		  }
		  return contactCreateTime;			  
	  }
	  
	  public void resetTimer(String awID){
		  
		  long newTime = System.currentTimeMillis();
		  ContentValues values = new ContentValues();
		  values.put(MySQLiteHelper.COLUMN_CREATETIME, newTime);
		  
		  String selection = MySQLiteHelper.COLUMN_WIDGETID + " LIKE ?";
		  String[] selectionArgs = {awID};
		  
		  int count = database.update(MySQLiteHelper.TABLE_CONTACTS, values, selection, selectionArgs);
		  	  
	  }
	
}
