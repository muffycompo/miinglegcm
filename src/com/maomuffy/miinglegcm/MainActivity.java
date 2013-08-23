package com.maomuffy.miinglegcm;


import static com.maomuffy.miingleutilities.GCMPoCUtilities.SHAREDPREF_LOCATION;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.TAG;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.SENDER_ID;

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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.maomuffy.miingleutilities.MUtils;
import com.maomuffy.miinglegcm.R;
import com.maomuffy.miingleutilities.NetworkConnectionDetector;


public class MainActivity extends Activity implements OnClickListener {

	Context ct;
	TextView tvSignup;
	EditText etEmailLogin,etPasswordLogin;
	Button btLogin;
	ProgressDialog pDialog;
	GoogleCloudMessaging gcm;
	String regId;
	Integer isLoggedIn;
	MUtils ists;
	// flag for Internet connection status
    Boolean isInternetAvailable = false;
	NetworkConnectionDetector con;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// creating connection detector class instance
        con = new NetworkConnectionDetector(getApplicationContext());
        ct = getApplicationContext();
		SharedPreferences sharedPref = getSharedPreferences(SHAREDPREF_LOCATION, MODE_PRIVATE);
		isLoggedIn = sharedPref.getInt("isLoggedIn", 0);
		
		tvSignup = (TextView)findViewById(R.id.tvSignup);
		etEmailLogin =  (EditText)findViewById(R.id.etEmailLogin);
		etPasswordLogin = (EditText)findViewById(R.id.etPasswordLogin);
		btLogin = (Button)findViewById(R.id.btLogin);
		
		// Attach onclick handler
		isInternetAvailable = con.isNetworkEnabled();
		if( isInternetAvailable ){
			btLogin.setOnClickListener(this);
			tvSignup.setOnClickListener(this);
		} else {
			con.showDialog(this, "Network Error", "Internet Required!", isInternetAvailable);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btLogin:
			String loginEmail = etEmailLogin.getText().toString();
			String loginPassword = etPasswordLogin.getText().toString();
			Log.i(TAG, "Sent: " + loginEmail);
			new loginAPICall().execute(loginEmail,loginPassword);
			break;
			
		case R.id.tvSignup:
			Intent signup = new Intent(ct, SignupActivity.class);
			startActivity(signup);
			break;

		default:
			break;
		}
		
	}
	
	private class loginAPICall extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			// Detach operation from UI thread
			
			// Get regID from GCM
			regId = GCMRegistrar.getRegistrationId(MainActivity.this);
			
			if (regId.equals("")) {
				gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
				try {
					regId = gcm.register(SENDER_ID);
				} catch (IOException e) {
					
					Log.i(TAG, "GCM problem");
				}
			}
			
			try {
				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				/* API URL */
				HttpPost httppost = new HttpPost("http://10.0.2.2/gcm_poc/public/authenticate");
				
				List<NameValuePair> nvp = new ArrayList<NameValuePair>();
				nvp.add(new BasicNameValuePair("email", params[0]));
				nvp.add(new BasicNameValuePair("password", params[1]));
				nvp.add(new BasicNameValuePair("gcm_reg_code", regId));
				httppost.setEntity(new UrlEncodedFormEntity(nvp));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				ists = new MUtils();
				String str = ists.inputStreamToString(response.getEntity().getContent()).toString();				
				// Return result as string
				Log.i(TAG, "Got: " + str);
				return str;

			} catch (ClientProtocolException e) {
				Toast.makeText(getApplicationContext(), "Network error...", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), "Network error...", Toast.LENGTH_SHORT).show();
			}
			
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Prepare to Miingle...");
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... v) {
			super.onProgressUpdate(v);
			pDialog.setProgress(v[0]);
		}

		@Override
		protected void onPostExecute(String r) {
			// Attach result back to UI thread
			super.onPostExecute(r);
			
			Context ct = getApplicationContext();
			
			// Parse JSON from API Response
			try {
				JSONObject json = new JSONObject(r);
				
				if( json.getString("status").toLowerCase(Locale.getDefault()).equalsIgnoreCase("success") ){
					// Get Data object
					JSONObject data = json.getJSONObject("data");
					
					// Prepare for storage
					String email = etEmailLogin.getText().toString();
					String fullname = data.getString("fullname");
					int userID = data.getInt("id");
					String gsm = data.getString("gsmno");
					
					// Store Returned User Profile Object in Shared Preferences
					
					// Create object of SharedPreferences.
					SharedPreferences sharedPref = getSharedPreferences(SHAREDPREF_LOCATION, 0);
					// now get Editor
					SharedPreferences.Editor editor = sharedPref.edit();
					// put your value
					editor.putInt("isLoggedIn", 1);
					editor.putString("email", email);
					editor.putString("fullname", fullname);
					editor.putString("gsmno", gsm);
					editor.putInt("member_id", userID);
					// commits your edits
					editor.commit();
					
					Intent i = new Intent(ct, DashboardActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					pDialog.dismiss();
					
				} else {
					
					pDialog.dismiss();
					
					// Notify User of Failure
					Toast toast = Toast.makeText(ct, "Login failed: Check your credentials!", Toast.LENGTH_SHORT);
					toast.show();
				}
			} catch (JSONException e) {
				Toast.makeText(ct, "Network error...", Toast.LENGTH_SHORT).show();
				pDialog.dismiss();
			}
		}
		
	}
	


}
