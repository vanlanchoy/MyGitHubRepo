package tme.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.util.Log;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by kchoy on 1/15/2015.
 */
public class POS_Application extends Application {
    private static final String PROPERTY_ID="UA-58618111-1";
    private static final String TAG = "MyApp";
    private static final String KEY_APP_CRASHED = "KEY_APP_CRASHED";
    Activity currentActivity;
    private static POS_Application singleton;
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();



    public void setCurrentActivity(Activity activity)
    {
        currentActivity = activity;
        // setup handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(currentActivity));
    }
    public POS_Application()
    {

    }
     @Override
    public void onCreate()
     {
         super.onCreate();
         singleton = this;



     }
    public static POS_Application getInstance(){return singleton;}

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            //Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : null;//analytics.newTracker(R.xml.ecommerce_tracker);

            //setup uncaught exception
            //ExceptionReporter reporter = new ExceptionReporter(t,Thread.getDefaultUncaughtExceptionHandler(),this);
            //Thread.setDefaultUncaughtExceptionHandler(reporter);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    boolean isConnectedToNetwork()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info==null || !info.isConnected()) return false;

        if((info.getType() == ConnectivityManager.TYPE_WIFI)||
            (info.getType() == ConnectivityManager.TYPE_MOBILE)){
            return true;
        }
        return false;
    }

}
