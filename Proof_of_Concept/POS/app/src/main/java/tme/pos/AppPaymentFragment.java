package tme.pos;

import android.os.Bundle;
import android.preference.PreferenceFragment;



/**
 * Created by kchoy on 3/20/2015.
 */
public class AppPaymentFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_payment_settings);


    }
}
