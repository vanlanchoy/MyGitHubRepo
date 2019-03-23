package tme.pos.CustomViewCtr;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableRow;

import tme.pos.AddNewMenuItemFragment;
import tme.pos.BusinessLayer.common;
import tme.pos.MainUIActivity;
import tme.pos.R;

/**
 * Created by vanlanchoy on 1/28/2015.
 */
public abstract class GenericVerticalFlipableTableRow extends TableRow
{

    protected GestureDetector myGestureDetector;
    //Fragment parentFragment;
    public GenericVerticalFlipableTableRow(Context context)
    {
        super(context);
        //parentFragment = fragment;
        Configure();
    }
    public GenericVerticalFlipableTableRow(Context context, AttributeSet attrs)
    {
        super(context,attrs);
       // parentFragment = fragment;
        Configure();
    }

    protected void Configure()
    {
        //assigning id
        this.setId(generateViewId());
        final int Id = this.getId();
        final TableRow tr = this;

        myGestureDetector = new GestureDetector(getContext(), new MyDoubleTapGestureListener(this));

        //add on touch listener
        setOnTouchListener(new OnTouchListener() {
            float flInitialX=0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                myGestureDetector.onTouchEvent(motionEvent);

                int action = MotionEventCompat.getActionMasked(motionEvent);

                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        setBackgroundColor(getResources().getColor(R.color.selected_row_green));

                        break;
                    case (MotionEvent.ACTION_MOVE):



                        break;
                    case (MotionEvent.ACTION_UP):
                        setBackgroundColor(getResources().getColor(R.color.white));

                        break;
                    case (MotionEvent.ACTION_CANCEL):
                        setBackgroundColor(getResources().getColor(R.color.white));

                        break;
                    default:
                        break;
                }






                return true;
            }
        });
    }


    protected abstract void SingleTapped();
    protected abstract void ShowConfirmation();



    private class MyDoubleTapGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
        View currentObject;
        public MyDoubleTapGestureListener(View v)
        {
            currentObject = v;
        }
        @Override
        public boolean onDoubleTapEvent(MotionEvent event)
        {
            if(event.getAction()==1)
            {

                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),
                        R.anim.flip_vertical);
                anim.setTarget(currentObject);
                anim.setDuration(300);
                anim.start();
                setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                ShowConfirmation();
                //ShowDeleteConfirmation();
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            SingleTapped();
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent event)
        {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            return false;
        }
    }
}