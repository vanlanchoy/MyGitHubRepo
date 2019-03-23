package tme.pos;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/**
 * Created by kchoy on 3/17/2015.
 */
public class NOT_IN_USE_POSPrinterFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.receipt_settings);


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getActivity(), "code is " + requestCode, Toast.LENGTH_SHORT).show();
    }
}
