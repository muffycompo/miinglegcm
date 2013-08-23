package com.maomuffy.miinglegcm;

import static com.maomuffy.miingleutilities.GCMPoCUtilities.SHAREDPREF_LOCATION;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.maomuffy.miingleutilities.NetworkConnectionDetector;


public class DashboardActivity extends Activity implements OnClickListener {

	TextView tvName;
	Button btCategories, btProfile, btLogout;
	// flag for Internet connection status
    Boolean isInternetAvailable = false;
	NetworkConnectionDetector con;
	Context ct;
	Integer isLoggedIn;
	String member_name;
	GoogleCloudMessaging gcm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_activity);
		
		// creating connection detector class instance
        con = new NetworkConnectionDetector(getApplicationContext());
        ct = getApplicationContext();
        gcm = GoogleCloudMessaging.getInstance(this);
        
        SharedPreferences sharedPref = getSharedPreferences(SHAREDPREF_LOCATION, MODE_PRIVATE);
        isLoggedIn = sharedPref.getInt("isLoggedIn", 0);
		member_name = sharedPref.getString("fullname", "");
		
		tvName = (TextView)findViewById(R.id.tvName);
		btCategories = (Button)findViewById(R.id.btCategories);
		btProfile = (Button)findViewById(R.id.btProfile);
		btLogout = (Button)findViewById(R.id.btLogout);
		
		isInternetAvailable = con.isNetworkEnabled();
		if( isInternetAvailable ){
			btCategories.setOnClickListener(this);
			btProfile.setOnClickListener(this);
		} else {
			con.showDialog(this, "Network Error", "Internet Required!", isInternetAvailable);
		}
		tvName.setText(member_name);
		btLogout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btCategories:
			Intent category = new Intent(ct, CategoriesActivity.class);
			startActivity(category);
			break;
			
		case R.id.btProfile:
//			Intent profile = new Intent(this, ProfileActivity.class);
//			startActivity(profile);
			Toast toast = Toast.makeText(ct, "Not Implemented!", Toast.LENGTH_SHORT);
			toast.show();
			break;
			
		case R.id.btLogout:
			// Unregister device
			try {
				gcm.unregister();
			} catch (IOException e) {
				
			}
			Intent logout = new Intent(ct, MainActivity.class);
			logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(logout);
			break;

		default:
			break;
		}
		
	}
	
}
