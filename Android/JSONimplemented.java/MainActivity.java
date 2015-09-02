package com.omegaraven.example;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.someplace.helper.SimpleListAdapter;
import com.someplace.helper.XMLParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private LocationManager locationManager;
	private String provider, resultJSON;

	// JSON node keys
	static final String KEY_ITEM = "theList"; // parent node
	static final String KEY_NAME = "theName";
	static final String KEY_ADDRESS = "addr1";
	static final String KEY_CITY = "city";
	static final String KEY_STATE = "state";
	static final String KEY_ZIP = "zip";
	static final String KEY_DISTANCE = "distance";
	static final String KEY_URL = "theUrl";

	int lat;
	int lng;
	Location location;
	XMLParser parser;

	private ArrayList<HashMap<String, String>> theItems = new ArrayList<HashMap<String, String>>();
	private JSONArray jsonArray;
	SimpleListAdapter slAdapter;
	ListAdapter adapter;
	LocationClient mLocationClient;
	ListView listView;

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.lv_search_list);
		// LocationProcess();
		// Testing Google Play Connection
		if (ConnectionResult.SUCCESS == GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this)) {
			mLocationClient = new LocationClient(this, this, this);

		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		mLocationClient.connect();

	}

	@Override
	protected void onStop() {
		// Disconnect from location Client
		mLocationClient.disconnect();
		theItems.clear();
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void LocationCatch(Location location) {
		if (location != null) {
			lat = (int) (location.getLatitude());
			lng = (int) (location.getLongitude());
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			builder.setTitle("No Location Found");
			builder.setMessage("Couldn't fetch the Location Data")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// do things
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	public void searchAction(View view) {
		// Cutting the following section to set up proper design.
		Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private class NetworkHandle extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(
					"https://examplewebsite.exa/something");

			try {
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("longitude", Integer
						.toString(lng)));
				nameValuePairs.add(new BasicNameValuePair("latitude", Integer
						.toString(lat)));
				nameValuePairs.add(new BasicNameValuePair("resellerId",
						getString(R.string.reseller_id)));

				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Http Post Execution
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity resEntity = response.getEntity();

				resultJSON = EntityUtils.toString(resEntity);

				// barrayXml = EntityUtils.toByteArray(resEntity);
				System.out.println(resultJSON);

			} catch (ClientProtocolException e) {

			} catch (IOException e) {

			}

			if (resultJSON != null) {
				try {
					// JSONObject jsonObj = new JSONObject(resultJSON);
					// JSONObject jsonData = jsonObj.getJSONObject("");
					// Getting JSON Array node
					jsonArray = new JSONArray(resultJSON);
					for (int i = 0; i < jsonArray.length(); i++) {

						JSONObject c = jsonArray.getJSONObject(i);

						String theName = c.getString(KEY_NAME);
						String theDistance = c.getString(KEY_DISTANCE);
						String theAddress = c.getString(KEY_ADDRESS);
						String theCity = c.getString(KEY_CITY);
						String theState = c.getString(KEY_STATE);
						String theZip = c.getString(KEY_ZIP);
						String theURL = c.getString(KEY_URL);

						// tmp hashmap for single contact
						HashMap<String, String> message = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						message.put("theName", theName);
						message.put("theDistance", theDistance);
						message.put("theAddress", theAddress);
						message.put("theCity", theCity);
						message.put("theState", theState);
						message.put("theZip", theZip);
						message.put("theURL", theURL);

						// adding contact to contact list
						theItems.add(message);
					}

					if (theItems.isEmpty()) {
						HashMap<String, String> message = new HashMap<String, String>();
						message.put("theName", "Unable to find information.");
						message.put("theCity",
								"Please try your search again");
						message.put("theState", "use the button below.");

						theItems.add(message);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("ServiceHandler", "Couldn't get any data from the url");
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				builder.setTitle("No Result");
				builder.setMessage("No results were found during search.")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// TO-DO implement resolver
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			slAdapter = new SimpleListAdapter(MainActivity.this, theItems);
			listView.setAdapter(slAdapter);

		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		Location mCurrentLocation;
		mCurrentLocation = mLocationClient.getLastLocation();
		LocationCatch(mCurrentLocation);
		if (isNetworkAvailable()) {
			if (mCurrentLocation != null) {
				new NetworkHandle().execute();
			}
		} else {
			Log.e("ServiceHandler", "Couldn't connect to network.");
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			builder.setTitle("No Data");
			builder.setMessage("Could not connect to network.")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// TO-DO implement additional network test.
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HashMap<String, String> selection = (HashMap<String, String>) theItems
						.get(position);
				// startActivity(new Intent(Intent.ACTION_VIEW,
				// Uri.parse((String) video.get("videourl"))));
				Intent theSelectIntent = new Intent(MainActivity.this,
						theDisplay.class);
				theSelectIntent.putExtra("theName",
						selection.get("theName"));
				theSelectIntent.putExtra("theAddress",
						selection.get("theAddress"));
				theSelectIntent.putExtra("theCity",
						selection.get("theCity"));
				theSelectIntent.putExtra("theState",
						selection.get("theState"));
				theSelectIntent.putExtra("theZip",
						selection.get("theZip"));
				theSelectIntent.putExtra("theURL",
						selection.get("theURL"));
				startActivity(theSelectIntent);
			}
		});

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

}
