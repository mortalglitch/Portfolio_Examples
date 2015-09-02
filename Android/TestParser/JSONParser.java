package com.omegaraven.jsonparser;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.util.ServiceHandler;

public class JSONParser extends ListActivity {

	private ProgressDialog pDialog;

    // URL to get contacts JSON
    private static String url = "http://randomurltoscan";

    // JSON Node names
    private static final String TAG_RESULTS = "results";
    private static final String TAG_ID = "id";
    private static final String TAG_USER = "user";
    private static final String TAG_NAME = "screen_name";
    private static final String TAG_MESSAGE = "text";

    // contacts JSONArray
    JSONArray results = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactList = new ArrayList<HashMap<String, String>>();

        ListView lv = getListView();

        // Calling async to get json data
        new GetContacts().execute();
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    results = jsonObj.getJSONArray(TAG_RESULTS);

                    // looping through All results
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject c = results.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String text = c.getString(TAG_MESSAGE);

                        // Phone node is JSON Object
                        JSONObject user = c.getJSONObject(TAG_USER);
                        String name = user.getString(TAG_NAME);

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put(TAG_ID, id);
                        contact.put(TAG_NAME, name);
                        contact.put(TAG_MESSAGE, text);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, contactList,
                    R.layout.list_item, new String[] { TAG_NAME, TAG_MESSAGE }, new int[] { R.id.name,
                            R.id.message });

            setListAdapter(adapter);
        }

    }
}
