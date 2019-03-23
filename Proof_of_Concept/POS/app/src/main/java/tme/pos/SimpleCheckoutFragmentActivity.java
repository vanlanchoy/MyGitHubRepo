package tme.pos;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by kchoy on 6/24/2015.
 */
public class SimpleCheckoutFragmentActivity extends FragmentActivity {
    @Override
    protected  void onResume()
    {
        Log.d("Simple Checkout fragment activity Info", "on resume");
        super.onResume();
        ((POS_Application)getApplication()).setCurrentActivity(this);
    }
    @Override
    public void onCreate(Bundle bundle)
    {
        ((POS_Application)getApplication()).setCurrentActivity(this);
        super.onCreate(bundle);



        setContentView(R.layout.layout_checkout_option_popup_window_ui);

    }
}
