package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import tme.pos.BusinessLayer.common;
import tme.pos.R;

/**
 * Created by vanlanchoy on 9/7/2015.
 */
public class ReceiptScroller extends HorizontalScrollView {
    public interface OnReceiptScrollListener {
        public void onScrollChanged(HorizontalScrollView scrollView, int x, int y, int oldX, int oldY);
        public void onEndScroll(HorizontalScrollView scrollView);
        public void onFling(boolean blnFlingLeft);
    }
    private OnReceiptScrollListener mOnScrollListener;
    private Runnable mScrollingRunnable;
    private boolean blnIsScrolling;
    private boolean blnIsTouching;
    protected GestureDetector myGestureDetector;
    protected void ConfigureGestureDetector()
    {
        myGestureDetector = new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener() {
            private float flingMin=0;//because the row will follow the finger movement so we can only get very small offset for X (0.xxx)
            private float velocityMin=50;


            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
            {





                return false;
            }

            @Override
            public boolean onDown(MotionEvent e1)
            {


                return true;
            }

            @Override
            public boolean onFling(final MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
            {
                boolean blnFlingLeft = true;
                //get fling direction
                if(e1.getX()>e2.getX())
                {
                    //fling left
                    smoothScrollBy(140,0);

                }
                else
                {
                    //fling right
                    blnFlingLeft = false;
                    smoothScrollBy(-140,0);
                }

                if (mOnScrollListener != null) {
                    mOnScrollListener.onFling(blnFlingLeft);
                }
                return true;
            }
        });
    }

    public ReceiptScroller(Context context) {
        super(context);
        Configure();
    }

    public ReceiptScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        Configure();
    }

    public ReceiptScroller(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Configure();
    }
    protected void Configure()
    {
        setBackgroundColor(Color.RED);
        setOverScrollMode(OVER_SCROLL_NEVER);
       ConfigureGestureDetector();
    }



    private void ScrollingStopped()
    {



    }

   @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //int action = ev.getAction();



       /* switch(action)
        {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:
                blnIsTouching = true;
                blnIsScrolling = true;


                break;
            case MotionEvent.ACTION_UP:
                if (blnIsTouching && !blnIsScrolling) {
                    ScrollingStopped();
                    if (mOnScrollListener != null) {
                        mOnScrollListener.onEndScroll(this);

                    }
                }
                else
                {

                }

                blnIsTouching = false;
                break;
            case MotionEvent.ACTION_OUTSIDE:

                break;
        }*/

        myGestureDetector.onTouchEvent(ev);
        return true;//super.onTouchEvent(ev);
    }


    /*@Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);

        if (Math.abs(oldX - x) > 0) {
            if (mScrollingRunnable != null) {
                removeCallbacks(mScrollingRunnable);
            }

            mScrollingRunnable = new Runnable() {
                public void run() {
                    if (blnIsScrolling && !blnIsTouching) {
                        ScrollingStopped();
                        if (mOnScrollListener != null) {
                            mOnScrollListener.onEndScroll(ReceiptScroller.this);
                        }
                    }

                    blnIsScrolling = false;
                    mScrollingRunnable = null;
                }
            };

            postDelayed(mScrollingRunnable, 200);
        }
        else if(oldY>y && blnIsTouching)
        {
            //ShowMessageBox("Category item menu","Show More");
        }
        else if(oldY<y && blnIsTouching)
        {
            //ShowMessageBox("Category item menu","Show less");
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollChanged(this, x, y, oldX, oldY);
        }

    }*/
    /*@Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {

        int action = MotionEventCompat.getActionMasked(motionEvent);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):

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

        return super.onInterceptTouchEvent(motionEvent);

    }*/
    public void setOnScrollListener(OnReceiptScrollListener mOnEndScrollListener) {
        this.mOnScrollListener = mOnEndScrollListener;
    }
}
