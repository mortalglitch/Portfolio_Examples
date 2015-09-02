package com.omegaraven.kitwidget;

public class KITWidgetContact {
	private long id;
	private String contactName;
	private String contactNumber;
	private long contactCreateTime;
	private String contactImage;
	private String appWidID;
	private String days;
	
	public long getId(){
		return id;
	}
	
	public String getDays(){
		return days;
	}
	
	public void setDays(String newdays){
		this.days = newdays;
	}
	
	public String getAppWidId(){
		return appWidID;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public void setAppWidID(String newAppWidID){
		this.appWidID = newAppWidID;
	}
	
	// This needs to be fleshed out to return the entire class
	public String getName(){
		return contactName;
	}
	
	public String getNumber(){
		return contactNumber;
	}
	
	public long getCreateTime(){
		return contactCreateTime;
	}
	
	public String getImage(){
		return contactImage;
	}
	
	//Need to add many more values here to read the information
	public void setReachOutContact(String newContactName , String newContactNumber, long newContactCreateTime, String newContactImage, String newawID, String newDays){
		this.contactName = newContactName;
		this.contactNumber = newContactNumber;
		this.contactCreateTime = newContactCreateTime;
		this.contactImage = newContactImage;
		this.appWidID = newawID;
		this.days = newDays;
	}
	
}
