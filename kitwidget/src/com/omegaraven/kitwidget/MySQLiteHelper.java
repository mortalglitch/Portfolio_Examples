package com.omegaraven.kitwidget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * This project is based on the works found on
 * http://www.vogella.com/articles/AndroidSQLite/article.html
 * And the SQLite section of developer.android.com
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_CONTACTS = "reachouthelp";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_NUMBER = "number";
	public static final String COLUMN_CREATETIME = "createtime";
	public static final String COLUMN_IMAGE = "image";
	public static final String COLUMN_WIDGETID = "widgetid";
	public static final String COLUMN_DAYS = "days";
	
	private static boolean databaseExist;
	private static final String DATABASE_NAME = "reachout.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation SQL Statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_CONTACTS + "(" 
			+ COLUMN_ID	+ " integer primary key autoincrement, " 
			+ COLUMN_NAME + " text not null,"
			+ COLUMN_NUMBER + " text not null,"
			+ COLUMN_CREATETIME + " text not null,"
			+ COLUMN_IMAGE + " text not null,"
			+ COLUMN_WIDGETID + " text not null,"
			+ COLUMN_DAYS + " text not null);";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		databaseExist = true;
	}
	
	public boolean getdbExist(){
		return databaseExist;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {		
		db.execSQL("DROP TABLE IF EXIST " + TABLE_CONTACTS);
		onCreate(db);
	}

}
