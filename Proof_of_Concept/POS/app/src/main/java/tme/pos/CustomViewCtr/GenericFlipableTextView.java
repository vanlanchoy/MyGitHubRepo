package tme.pos.CustomViewCtr;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import tme.pos.MyDoubleTapGestureListener;
import tme.pos.R;

/**
 * Created by vanlanchoy on 5/31/2016.
 */
public abstract class GenericFlipableTextView extends TextView {
    protected GestureDetector myGestureDetector;
    int background_color = R.color.white;
    public GenericFlipableTextView(Context c)
    {
        super(c);
        Configure();
    }
    public GenericFlipableTextView(Context c,int colorId)
    {
        super(c);
        background_color = colorId;
        Configure();
    }
    protected void Configure()
    {
        //assigning id
        this.setId(generateViewId());
        //final int Id = this.getId();


        myGestureDetector = new GestureDetector(getContext(), new MyDoubleTapGestureListener(GenericFlipableTextView.this));

        //add on touch listener
        setOnTouchListener(new OnTouchListener() {


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
                        setBackgroundColor(getResources().getColor(background_color));

                        break;
                    case (MotionEvent.ACTION_CANCEL):
                        setBackgroundColor(getResources().getColor(background_color));

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
                //setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                ShowConfirmation();

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
