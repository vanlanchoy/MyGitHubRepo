package tme.pos.CustomViewCtr;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import tme.pos.BusinessLayer.common;
import tme.pos.MainUIActivity;
import tme.pos.R;

/**
 * Created by vanlanchoy on 1/17/2015.
 */
public class MainLinearLayout extends LinearLayout {
    boolean blnActionBarProcess = false;
    public MainLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MainLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public MainLinearLayout(Context context) {
        super(context);

    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent)
    {

        int action = MotionEventCompat.getActionMasked(motionEvent);

            switch (action) {
            case (MotionEvent.ACTION_DOWN):

                //swipe top->bottom to show option bar
                /*if(motionEvent.getRawY()<20) {
                    //user drag top of screen to show action bar
                    //return true to allow on touch to process
                    blnActionBarProcess=true;
                    return true;
                }*/

                //swipe right->left to show option bar
                /*if(motionEvent.getRawX()>MainLinearLayout.this.getWidth()-common.Utility.DP2Pixel(20,getContext()))
                {
                    blnActionBarProcess=true;
                    return true;
                }*/
                break;
            case (MotionEvent.ACTION_MOVE):

                break;
            case (MotionEvent.ACTION_UP):
                break;

            case (MotionEvent.ACTION_CANCEL):
                break;

            default:
                break;
        }
        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        int action = MotionEventCompat.getActionMasked(motionEvent);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                //ShowMessageBox("Action down","main linear layout");

                /*if(motionEvent.getRawY()<MainUIActivity.DP2Pixel(20,getContext()) && blnActionBarProcess) {
                    return true;
                }*/
               /* if(motionEvent.getRawX()>MainLinearLayout.this.getWidth()-common.Utility.DP2Pixel(20,getContext())
                        && blnActionBarProcess)
                {
                    return true;
                }*/
                break;
            case (MotionEvent.ACTION_MOVE):
                /*if(motionEvent.getRawY()>MainUIActivity.DP2Pixel(50,getContext()) &&  blnActionBarProcess)//convert numerical dp to pixel
                {

                    if(!((MainUIActivity)getContext()).getActionBar().isShowing()) {
                        blnActionBarProcess=false;
                        ((MainUIActivity) getContext()).getActionBar().show();

                        ((MainUIActivity) getContext()).ReadjustAllPanelHeight();//ReadjustMenuPanelComponentSizes();

                    }
                }*/
               /* if(motionEvent.getRawX()<MainLinearLayout.this.getWidth()-common.Utility.DP2Pixel(50,getContext())
                        && blnActionBarProcess)
                {

                    ((MainUIActivity) getContext()).ShowRightSideOptionBar();
                }*/

                break;
            case (MotionEvent.ACTION_UP):
                blnActionBarProcess=false;
                break;

            case (MotionEvent.ACTION_CANCEL):

                break;
            case (MotionEvent.ACTION_OUTSIDE):

                break;

            default:
                break;
        }




        return false;

    }

}
