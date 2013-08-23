package com.maomuffy.miingleutilities;

import android.content.Context;
import android.content.Intent;

public final class GCMPoCUtilities {
	
	public static final String DISCUSSION_SERVER_URL = "http://10.0.2.2/gcm_poc/public/discussions"; 
    public static final String SENDER_ID = "842566924491"; 
    public static final String TAG = "GCM PoC";
    public static final String DISPLAY_MESSAGE_ACTION = "com.maomuffy.miinglegcm.DISPLAY_MESSAGE";
    public static final String EXTRA_MESSAGE_BODY = "body";
    public static final String EXTRA_MESSAGE_CATEGORY = "category_id";
    public static final String EXTRA_MESSAGE_MEMBER = "member_id";
    public static final String EXTRA_MESSAGE_CREATED = "created_at";
    public static final String SHAREDPREF_LOCATION = "com.maomuffy.miinglegcm.pref";

    
    public static void showMessage(Context context, String message, int category, int member, String created) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE_BODY, message);
        intent.putExtra(EXTRA_MESSAGE_CATEGORY, category);
        intent.putExtra(EXTRA_MESSAGE_MEMBER, member);
        intent.putExtra(EXTRA_MESSAGE_CREATED, created);
        context.sendBroadcast(intent);
    }
}
