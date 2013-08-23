package com.maomuffy.miinglegcm;

import static com.maomuffy.miingleutilities.GCMPoCUtilities.SENDER_ID;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.SHAREDPREF_LOCATION;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.TAG;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.maomuffy.miingleutilities.MUtils;
import com.maomuffy.miingleutilities.NetworkConnectionDetector;

public class SignupActivity extends Activity implements OnClickListener {

	EditText etNameSignup, etEmailSignup, etPasswordSignup, etGsmSignup;
	Button btSignup;
	ProgressDialog pDialog;
	GoogleCloudMessaging gcm;
	// flag for Internet connection status
	Boolean isInternetAvailable = false;
	NetworkConnectionDetector con;
	String regId, fullname, email, gsmno, password;
	Integer member_id, isLoggedIn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup_activity);
		
		con = new NetworkConnectionDetector(getApplicationContext());

		SharedPreferences sharedPref = getSharedPreferences(SHAREDPREF_LOCATION, MODE_PRIVATE);
		isLoggedIn = sharedPref.getInt("isLoggedIn", 0);
		
		etEmailSignup = (EditText)findViewById(R.id.etEmailSignup);
		etNameSignup = (EditText)findViewById(R.id.etNameSignup);
		etPasswordSignup = (EditText)findViewById(R.id.etPasswordSignup);
		etGsmSignup = (EditText)findViewById(R.id.etGsmSignup);
		btSignup = (Button)findViewById(R.id.btSignup);
		
		isInternetAvailable = con.isNetworkEnabled();
		if( isInternetAvailable ){
			btSignup.setOnClickListener(this);
		} else {
			con.showDialog(this, "Network Error", "Internet Required!", isInternetAvailable);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btSignup:
			// GCM Stuff
			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);
			
			fullname = etNameSignup.getText().toString();
			email = etEmailSignup.getText().toString();
			gsmno = etGsmSignup.getText().toString();
			password = etPasswordSignup.getText().toString();
			Log.i(TAG, "Sent: " + etEmailSignup);
			new signupAPICall().execute(fullname, email, gsmno, password);
			
			break;

		default:
			break;
		}
	}
	
	private class signupAPICall extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// Detach operation from UI thread

			try {
				
				// Get regID from GCM
				regId = GCMRegistrar.getRegistrationId(SignupActivity.this);
				
				if (regId.equals("")) {
					gcm = GoogleCloudMessaging.getInstance(SignupActivity.this);
//					GCMRegistrar.register(SignUpActivity.this, SENDER_ID);
					regId = gcm.register(SENDER_ID);
				}
				Log.i(TAG, "Registered to GCM: " + regId);
				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				/* API URL */
				HttpPost httppost = new HttpPost(
						"http://10.0.2.2/gcm_poc/public/members");

				List<NameValuePair> nvp = new ArrayList<NameValuePair>();
				nvp.add(new BasicNameValuePair("fullname", params[0]));
				nvp.add(new BasicNameValuePair("email", params[1]));
				nvp.add(new BasicNameValuePair("gsmno", params[2]));
				nvp.add(new BasicNameValuePair("password", params[3]));
				nvp.add(new BasicNameValuePair("gcm_reg_code", regId));
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
			pDialog = new ProgressDialog(SignupActivity.this);
			pDialog.setMessage("Joining Miingle...");
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

				if (json.getString("status").toLowerCase(Locale.getDefault())
						.equalsIgnoreCase("success")) {
					// // Get Data object
					member_id = json.getInt("data");
					Log.i(TAG, "Member ID: " + member_id);

					// Store Returned User Profile Object in Shared Preferences

					// Create object of SharedPreferences.
					 SharedPreferences sharedPref =
					 getSharedPreferences(SHAREDPREF_LOCATION, MODE_PRIVATE);
					// now get Editor
					 SharedPreferences.Editor editor = sharedPref.edit();
					// put your value
					 editor.putInt("isLoggedIn", 1);
					 editor.putString("email", email);
					 editor.putString("fullname", fullname);
					 editor.putString("gsmno", gsmno);
					 editor.putInt("member_id", member_id);
					// commits your edits
					 editor.commit();

					 Intent iDashboard = new Intent(ct, DashboardActivity.class);
					 startActivity(iDashboard);

					pDialog.dismiss();
				} else {

					pDialog.dismiss();

					// Notify User of Failure
					Toast toast = Toast.makeText(ct, "An error occured!",
							Toast.LENGTH_SHORT);
					toast.show();
				}
			} catch (JSONException e) {
				Toast.makeText(ct, "Network error...", Toast.LENGTH_SHORT)
						.show();
				pDialog.dismiss();
			}
		}

	}
	
	
}
