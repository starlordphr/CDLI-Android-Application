package com.assolutions.altaf.cdlitablet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by HAWONG on 09-Sep-17.
 */

 public class  ConnectivityChecker {

    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context){
        NetworkInfo info = ConnectivityChecker.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }
}
