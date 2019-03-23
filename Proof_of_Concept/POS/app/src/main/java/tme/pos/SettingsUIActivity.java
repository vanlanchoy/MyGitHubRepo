package tme.pos;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by kchoy on 10/9/2014.
 */
public class SettingsUIActivity extends Activity {
    @Override
    protected  void onResume()
    {
        Log.d("setting activity Info", "on resume");
        super.onResume();
        ((POS_Application)getApplication()).setCurrentActivity(this);
    }
    @Override
    protected void onCreate(Bundle savedIntanceState)
    {
        Log.d("setting activity Info", "on create");
        super.onCreate(savedIntanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MainUIActivity.SettingsFragment())
                .commit();

    }

}
