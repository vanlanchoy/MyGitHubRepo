package tme.pos;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import tme.pos.BusinessLayer.Supplier;
import tme.pos.BusinessLayer.common;
import tme.pos.BusinessLayer.Enum;

/**
 * Created by kchoy on 6/8/2015.
 */
public class ResourceManagementFragmentActivity extends FragmentActivity implements PhotoFeatureFragment.OnFragmentInteractionListener{
    TextView[] TitleSelections;
    TextView tvSupplier;
    @Override
    protected  void onResume()
    {
        //Log.d("Resource management fragment activity Info", "on resume");
        super.onResume();
        ((POS_Application)getApplication()).setCurrentActivity(this);
    }
    @Override
    public void onFragmentInteraction(Uri uri)
    {

    }
    private void CreateClickEffectForRoundedborder(View v,final Enum.FragmentName fragmentName)
    {

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v2, MotionEvent event) {
                switch (fragmentName) {
                    case server:
                        TitleClicked(v2, Enum.FragmentName.server);
                        break;

                    default:
                        TitleClicked(v2, Enum.FragmentName.supplier);

                }
                return false;
            }
        });
    }
                //int colorFrom = getResources().getColor(R.color.transparent);
                //int colorTo = getResources().getColor(R.color.selected_row_green);
                //ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                //colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                //@Override
                // public void onAnimationUpdate(ValueAnimator animator) {
                //v2.setBackgroundColor((Integer) animator.getAnimatedValue());
                      /*  new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {


                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            v2.setBackground(getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
                                        }
                                    }, 100);



                            }*/
                //}, 100);
                //}

                // });
                //colorAnimation.setDuration(100);
                //colorAnimation.start();

                //return false;
                //}
                //});
    //}
    @Override
    public void onCreate(Bundle bundle)
    {

        ((POS_Application)getApplication()).setCurrentActivity(this);
        super.onCreate(bundle);


        //common.Utility.ShowMessage("bundle",strTemp,null);
        setContentView(R.layout.layout_resource_management_popup_window_ui);


        TitleSelections = new TextView[2];
        TextView tvServer = (TextView)findViewById(R.id.tvServer);
        tvServer.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        tvServer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.RESOURCE_MANAGEMENT_SELECTION_TITLE_TEXT_SIZE);
        CreateClickEffectForRoundedborder(tvServer, Enum.FragmentName.server);


//        TextView tvSupplier = (TextView)findViewById(R.id.tvSupplier);
        tvSupplier = (TextView)findViewById(R.id.tvSupplier);
        tvSupplier.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        tvSupplier.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.RESOURCE_MANAGEMENT_SELECTION_TITLE_TEXT_SIZE);
        CreateClickEffectForRoundedborder(tvSupplier, Enum.FragmentName.supplier);




        TitleSelections[0] = tvServer;
        TitleSelections[1] = tvSupplier;


        Bundle bb = getIntent().getExtras();
        String strTemp = "No value";
        if(bb.getString("panel")!=null)
        {
            strTemp = bb.getString("panel");
        }

        if(strTemp.equalsIgnoreCase("server")) {
            common.Utility.DispatchTouchEvent(tvServer);
            //tvServer.callOnClick();
        }
        else //if(strTemp.equalsIgnoreCase("supplier"))
        {


            if(bb.getString("show_add")!=null)
            {
                ShowAddSupplierDialog();
            }
            else
            {
                common.Utility.DispatchTouchEvent(tvSupplier);
            }
            //tvSupplier.callOnClick();
        }
        /*else {
            //trigger click
            common.Utility.DispatchTouchEvent(tvServer);
        }*/
    }
    private void ShowAddSupplierDialog()
    {

        RemoveSelectedBorder();
        tvSupplier.setBackground(getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
        tvSupplier.setTextColor(getResources().getColor(R.color.green));
        tvSupplier.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
                .hide(getSupportFragmentManager().findFragmentById(R.id.frgServer))
                .commit();

        getSupportFragmentManager().beginTransaction()
                //.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
                .show(getSupportFragmentManager().findFragmentById(R.id.frgSupplier))
                .commit();

        SupplierUIFragment frag = (SupplierUIFragment) getSupportFragmentManager().findFragmentById(R.id.frgSupplier);
        frag.ShowAddSupplierDialog();
    }
    private void TitleClicked(View tv,Enum.FragmentName fn)
    {
        RemoveSelectedBorder();
        tv.setBackground(getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
        ((TextView)tv).setTextColor(getResources().getColor(R.color.green));
        ((TextView)tv).setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD);

        Fragment frgShow,frgHide;
        if(fn== Enum.FragmentName.supplier)
        {
            frgHide=getSupportFragmentManager().findFragmentById(R.id.frgServer);
            frgShow=getSupportFragmentManager().findFragmentById(R.id.frgSupplier);


        }
        else
        {
            frgHide=getSupportFragmentManager().findFragmentById(R.id.frgSupplier);
            frgShow=getSupportFragmentManager().findFragmentById(R.id.frgServer);
        }
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
                .hide(frgHide)
                .commit();

        getSupportFragmentManager().beginTransaction()
                //.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
                .show(frgShow)
                .commit();
    }
    private void RemoveSelectedBorder()
    {
        if(TitleSelections==null)return;
        for(int i=0;i<TitleSelections.length;i++)
        {
            TitleSelections[i].setBackground(null);
            TitleSelections[i].setTextColor(Color.GRAY);// getResources().getColor(R.color.dark_grey));
            TitleSelections[i].setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        }
    }
}
