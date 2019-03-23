package tme.pos.BusinessLayer;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import tme.pos.MainUIActivity;
import tme.pos.R;

/**
 * Created by kchoy on 5/5/2015.
 */

public class MyLocationService implements  GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,LocationListener{

    int UPDATE_INTERVAL = 15000*60; // 15 min
    int FASTEST_INTERVAL = 5000*60; // 5 min
    int DISPLACEMENT = 10; // 10 meters


    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    Context context;
    long lastUpdatedTimeStamp;
    boolean blnPermissionGranted=false;
    public MyLocationService(Context c)
    {
        context = c;
        lastUpdatedTimeStamp = 0;
        if(CheckGooglePlayService()) {
            mGoogleApiClient = new GoogleApiClient.Builder(c)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
            mGoogleApiClient.connect();

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


        }
    }
    public void SetPermission(boolean blnFlag)
    {
        blnPermissionGranted = blnFlag;
    }
    private void StartLocationUpdate()
    {
        if(mGoogleApiClient!=null && blnPermissionGranted) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }


    private boolean CheckGooglePlayService()
    {
        if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)!= ConnectionResult.SUCCESS)
        {
            return false;
        }
        return true;
    }
    public void ResumeLocationService()
    {
        StartLocationUpdate();
    }
    public void Disconnect()
    {
        //when application shut down
        if(mGoogleApiClient!=null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }
    public void StopLocationService()
    {
        //when activity pause
        if(mGoogleApiClient!=null) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        common.Utility.LogActivity("failed to connect to google client");
        //common.Utility.ShowMessage("location service connection ","failed "+result.getErrorCode()+"",context, R.drawable.exclaimation);
    }
    @Override
    public void onConnected(Bundle b)
    {
        common.Utility.LogActivity("connected to google client");
        //common.Utility.ShowMessage("location service connection ","Connected",context,R.drawable.message);
        StartLocationUpdate();
    }
    @Override
    public void onConnectionSuspended(int result)
    {
        //client disconnected, do a reconnect
        mGoogleApiClient.connect();
        common.Utility.LogActivity("connection suspended to google client");
        //common.Utility.ShowMessage("location service connection ","suspended",context,R.drawable.message);
    }
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        //update last time stamp
        lastUpdatedTimeStamp = new Date().getTime();

        //common.Utility.ShowMessage("location service connection ","on location changed "+mLastLocation.getLatitude()+", "+mLastLocation.getLongitude()+"",context);
        common.Utility.LogActivity("location changed latitude "+mLastLocation.getLatitude() + ", longitude"+ mLastLocation.getLongitude());
        //call web service to update
        ((MainUIActivity) context).UpdateGeoLocationService(mLastLocation.getLatitude() + "", mLastLocation.getLongitude() + "");
    }
    public void GetLocationPts(double[] locationPts)
    {

        //check last time stamp
        long different = new Date().getTime()-lastUpdatedTimeStamp;

        //return nothing if the last timestamp has been update more than the interval time ago
        if(different>UPDATE_INTERVAL)
        {
            locationPts[0]=0;//Double.NaN;
            locationPts[1]=0;//Double.NaN;
        }
        else {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                locationPts[0] = mLastLocation.getLatitude();
                locationPts[1] = mLastLocation.getLongitude();



            } else {

                locationPts[0] = 0;//Double.NaN;
                locationPts[1] = 0;//Double.NaN;
            }
        }
        //common.Utility.ShowMessage("Location",strMsg,context);
    }
}
