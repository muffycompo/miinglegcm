package com.maomuffy.miinglegcm;

import static com.maomuffy.miingleutilities.GCMPoCUtilities.DISPLAY_MESSAGE_ACTION;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.SHAREDPREF_LOCATION;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.TAG;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.EXTRA_MESSAGE_BODY;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.EXTRA_MESSAGE_CATEGORY;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.EXTRA_MESSAGE_MEMBER;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.EXTRA_MESSAGE_CREATED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.maomuffy.miingleutilities.DBCategoryAdapter;
import com.maomuffy.miingleutilities.DBDiscussionAdapter;
import com.maomuffy.miingleutilities.MUtils;
import com.maomuffy.miingleutilities.NetworkConnectionDetector;
import com.maomuffy.miingleutilities.WakeLockerUtility;



public class DiscussionsActivity extends Activity implements OnClickListener, OnKeyListener {

//	Cursor c;
	DBDiscussionAdapter miingleDb;
	Integer member_id, isLoggedIn, category_id;
	TextView tvQuestion;
	ListView lvDiscussions;
	Button btSend;
	EditText etMessageDiscussion;
	ProgressDialog pDialog;
	GoogleCloudMessaging gcm;
	MUtils ists;
	Context ct;
	SimpleCursorAdapter dAdapter;
	// flag for Internet connection status
    Boolean isInternetAvailable = false;
	NetworkConnectionDetector con;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussion_activity);
		
		// creating connection detector class instance
        con = new NetworkConnectionDetector(getApplicationContext());
        ct = getApplicationContext();
        
		SharedPreferences sharedPref = getSharedPreferences(SHAREDPREF_LOCATION, MODE_PRIVATE);
        isLoggedIn = sharedPref.getInt("isLoggedIn", 0);
		member_id = sharedPref.getInt("member_id", 0);
		Bundle cId = getIntent().getExtras();
		category_id = Integer.parseInt(cId.getString("category_id"));
		
		miingleDb = new DBDiscussionAdapter(getApplicationContext());
		miingleDb.open();
		
		tvQuestion = (TextView)findViewById(R.id.tvQuestion);
		lvDiscussions = (ListView)findViewById(R.id.lvDiscussions);
		etMessageDiscussion = (EditText)findViewById(R.id.etMessageDiscussion);
		btSend = (Button)findViewById(R.id.btSend);
		
		displayDiscussions();
		
		// Attach onclick handler
		isInternetAvailable = con.isNetworkEnabled();
		if( isInternetAvailable ){
			btSend.setOnClickListener(this);
		} else {
			// Ignore lack of network connection for now
		}
		etMessageDiscussion.setOnKeyListener(this);
		
	}


	private void displayDiscussions() {
		lvDiscussions = (ListView) findViewById(R.id.lvDiscussions);
		Cursor cDiscussion = miingleDb.getAllRows();
		startManagingCursor(cDiscussion); // Replace with CursorLoader implementation
		
		String[] from = { DBDiscussionAdapter.KEY_DISCUSSION_BODY, DBDiscussionAdapter.KEY_DISCUSSION_MEMBER_ID };
		int[] to = { R.id.tvMessageBody, R.id.tvMemberId };
		

		dAdapter = new SimpleCursorAdapter(ct,
				R.layout.discussion_list_view, cDiscussion, from, to, 0);
		
		lvDiscussions.setAdapter(dAdapter);
		
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btSend:
			String message = etMessageDiscussion.getText().toString();
			new discussionAPICall().execute(message, category_id.toString(), member_id.toString());
			break;

		default:
			break;
		}
	}
	
	private class discussionAPICall extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// Detach operation from UI thread
			
			// GCM Stuff
			GCMRegistrar.checkDevice(DiscussionsActivity.this);
			GCMRegistrar.checkManifest(DiscussionsActivity.this);
			registerReceiver(receiveGCMMessage, new IntentFilter(
					DISPLAY_MESSAGE_ACTION));

			try {

				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				/* API URL */
				HttpPost httppost = new HttpPost(
						"http://10.0.2.2/gcm_poc/public/discussions");

				List<NameValuePair> nvp = new ArrayList<NameValuePair>();
				nvp.add(new BasicNameValuePair("member_id", params[2]));
				nvp.add(new BasicNameValuePair("body", params[0]));
				nvp.add(new BasicNameValuePair("category_id", params[1]));
				httppost.setEntity(new UrlEncodedFormEntity(nvp));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				MUtils ists = new MUtils();
				String str = ists.inputStreamToString(
						response.getEntity().getContent()).toString();
				// Return result as string
				Log.i(TAG, "Got: " + str);
				return str;

			} catch (ClientProtocolException e) {
				Toast.makeText(getApplicationContext(), "Network error...",
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), "Network error...",
						Toast.LENGTH_SHORT).show();
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// pDialog = new ProgressDialog(DiscussionsActivity.this);
			// pDialog.setMessage("Creating Account...");
			// pDialog.setCancelable(true);
			// pDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... v) {
			super.onProgressUpdate(v);
			// pDialog.setProgress(v[0]);
		}

		@Override
		protected void onPostExecute(String r) {
			// Attach result back to UI thread
			super.onPostExecute(r);

			Context ct = getApplicationContext();

			// Parse JSON from API Response
			try {
				JSONObject json = new JSONObject(r);

				if (json.getString("status").toLowerCase(Locale.getDefault())
						.equalsIgnoreCase("success")) {
					// Get Data object
					JSONObject jdata = new JSONObject(json.getString("data"));
					String body = etMessageDiscussion.getText().toString();
					String created_at = jdata.getString("created_at");
					
					
					// Insert into DB
					miingleDb.insertRow(body, category_id, member_id, created_at);
					
					// Get new cursor
					Cursor ic = miingleDb.getAllRows();
					
					Log.i(TAG, "Timestamp Received: " + created_at);
					Log.i(TAG, "Data: " + jdata.toString());

					// Update UI (ListView)
					dAdapter.changeCursor(ic);
					dAdapter.notifyDataSetChanged();
					
					
					etMessageDiscussion.setText("");
					
				} else {

					// pDialog.dismiss();

					// Notify User of Failure
					Toast toast = Toast.makeText(ct, "An error occured!",
							Toast.LENGTH_SHORT);
					toast.show();
				}
			} catch (JSONException e) {
				Toast.makeText(ct, "Network error...", Toast.LENGTH_SHORT)
						.show();
				// pDialog.dismiss();
			}
		}
	}

	private final BroadcastReceiver receiveGCMMessage = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newBody = intent.getExtras().getString(EXTRA_MESSAGE_BODY);
			int newCategoryId = intent.getExtras().getInt(EXTRA_MESSAGE_CATEGORY);
			int newMemberId = intent.getExtras().getInt(EXTRA_MESSAGE_MEMBER);
			String newCreatedAt = intent.getExtras().getString(EXTRA_MESSAGE_CREATED);
			WakeLockerUtility.acquire(getApplicationContext());
			
//			Log.i(TAG, intent.getExtras().keySet().toString());
			Log.i(TAG, newBody);
			Log.i(TAG, "Testing");
			
			// Insert into DB
			miingleDb.insertRow(newBody, newCategoryId, newMemberId, newCreatedAt);
			
			// Get new cursor
			Cursor nc = miingleDb.getAllRows();
			
			// Update UI
			dAdapter.changeCursor(nc);
			dAdapter.notifyDataSetChanged();
			
			WakeLockerUtility.release();
		}
		
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(receiveGCMMessage);
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			// Ignore
		}
	}

	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {

		if ((event.getAction() == KeyEvent.ACTION_DOWN)
				&& (keyCode == event.KEYCODE_ENTER)) {
			return true;
		}

		return false;
	}

	

	
}
