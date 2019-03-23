package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by vanlanchoy on 1/22/2015.
 */
public class DisplayErrorDialogActivity extends MainUIActivity {

    public DisplayErrorDialogActivity()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String strStack = getIntent().getExtras().getString("ErrorStack");
        SendCrashReport(strStack);
    }
    @Override
    protected void onStart()
    {

    }
    void SendCrashReport(final String strErrorStack)
    {
        if(isConnectedToNetwork())
        {
            //only ask when internet connection is available
            new AlertDialog.Builder(this)
                    .setMessage("Application encounter fatal error and needs to be shut down, apologize for the inconveniences. Will you like to report this to the developers?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d( "Uncaught Exception",strErrorStack);
                        }
                    })
                    .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.exit(1);
                        }
                    })
                    .show();
        }
        else
        {
            //display friendly message if no internet connection
            new AlertDialog.Builder(this)
                    .setMessage("Application encounter fatal error and needs to be shut down, apologize for the inconveniences.")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.exit(1);
                        }
                    })

                    .show();
        }
    }
    boolean isConnectedToNetwork()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfos = cm.getAllNetworkInfo();

        for(NetworkInfo ni :netInfos)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    return true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    return true;
        }

        return false;
    }
}
