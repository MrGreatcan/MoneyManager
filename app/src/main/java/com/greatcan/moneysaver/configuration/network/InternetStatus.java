package com.greatcan.moneysaver.configuration.network;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Check if phone connected to the Internet
 */
public class InternetStatus {

    private static final String TAG = "InternetStatus";

    /**
     * Network health check
     *
     * @return
     */
    public static Boolean getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager)           context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d(TAG, "getConnectivityStatusString: WI-FI enabled");
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.d(TAG, "getConnectivityStatusString: Mobile data enabled");
                return true;
            }
        } else {
            Log.d(TAG, "getConnectivityStatusString: No internet is available");
            return false;
        }
        return false;
    }
}
