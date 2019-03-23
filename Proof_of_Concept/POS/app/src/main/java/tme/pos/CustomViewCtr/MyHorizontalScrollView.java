package tme.pos.CustomViewCtr;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.MenuItem;
import android.view.MotionEvent;

import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;


import tme.pos.BusinessLayer.common;
import tme.pos.MainUIActivity;
import tme.pos.R;


/**
 * Created by kchoy on 10/10/2014.
 */
public class MyHorizontalScrollView extends HorizontalScrollView {
    //create this in activity onCreate method

    public interface OnScrollListener {
        public void onScrollChanged(MyHorizontalScrollView scrollView, int x, int y, int oldX, int oldY);
        public void onEndScroll(MyHorizontalScrollView scrollView);
    }

    //protected GestureDetector myGestureDetector;
    private boolean blnIsScrolling;
    private boolean blnIsTouching;
    private boolean blnExpand;
    private float flRawX;
    int intInitialScrollViewBottom =0;

    private OnScrollListener mOnScrollListener;
    private Runnable mScrollingRunnable;

    public MyHorizontalScrollView(Context context) {
        super(context);
        Configure();
    }

    public MyHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Configure();
    }

    public MyHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Configure();
    }


    private void Configure()
    {



    }


    private void ScrollingStopped()
    {
        //check each child in the top container is visible
        //else deselect the child
        MyTopMenuContainer tmc = (MyTopMenuContainer)getChildAt(0);
        tmc.ParentScrollStop();


    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        //int intCurrentYInDP = 0;
        int intCurrentY =0;

        LinearLayout MenuItemSelectionPanel=(LinearLayout)((LinearLayout)getParent()).findViewById(R.id.MenuItemSelectionPanel);
        switch(action)
        {
            case MotionEvent.ACTION_DOWN:
                //ShowMessageBox("Horizontal Scroll View Action Down",ev.getRawY()+"");
                if(!blnExpand) {
                    if (ev.getRawY() > DP2Pixel(100,getContext())) {//compare in dp
                        //blnExpand = true;
                        //flRawX = ev.getRawX();
                        //intInitialScrollViewBottom = this.getBottom();

                    } else {
                        blnExpand = false;

                    }
                }
                else
                {

                }
                break;
            case MotionEvent.ACTION_MOVE:
                blnIsTouching = true;
                blnIsScrolling = true;

                /*if(blnExpand)
                {
                    try {
                        //intCurrentYInDP = (int) Pixel2DP(ev.getY(), getContext());
                        intCurrentY = (int)ev.getY();
                        if (intCurrentY > intInitialScrollViewBottom) {
                            //hide menu item selection panel and show this panel

                            if(this.getBottom()>intInitialScrollViewBottom+Pixel2DP(50,getContext()))
                            {
                                //expand till occupied menu item selection panel
                                blnExpand = false;

                                SlideDownScrollView(MenuItemSelectionPanel.getBottom(),10);



                            }
                            else {
                                this.setBottom(intCurrentY);
                                this.setBackground(getResources().getDrawable(R.drawable.draw_gridview_category_bottom_border));
                                MenuItemSelectionPanel.setTop(intCurrentY);
                            }
                        }
                    }
                    catch(Exception ex)
                    {
                        ShowErrorMessageBox("set top",ex);
                    }
                }*/


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
                    //allow the panel to roll up if it doesn't rolled down to a certain level
                    if(blnExpand && MenuItemSelectionPanel.getTop()!=MenuItemSelectionPanel.getBottom())
                    {
                        //ShowMessageBox("action up",""+Pixel2DP(Math.abs(flRawX-ev.getX()),getContext()));
                        blnExpand=false;
                        this.setBackground(null);
                        this.setBottom(intInitialScrollViewBottom);
                        MenuItemSelectionPanel.setTop(intInitialScrollViewBottom);
                    }
                }

                blnIsTouching = false;
                break;
            case MotionEvent.ACTION_OUTSIDE:

                break;
        }


        return super.onTouchEvent(ev);
    }


    @Override
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
                            mOnScrollListener.onEndScroll(MyHorizontalScrollView.this);
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

    }
    private  void ShowMessageBox(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
        messageBox.setTitle(strTitle);
        messageBox.setMessage(strMsg);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(iconId),getResources(),36,36));
        }
        messageBox.show();
    }
    public OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    public void setOnScrollListener(OnScrollListener mOnEndScrollListener) {
        this.mOnScrollListener = mOnEndScrollListener;
    }
    @Override
    protected void onDraw(Canvas canvas)
    {

        super.onDraw(canvas);



    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {

        int action = MotionEventCompat.getActionMasked(motionEvent);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                //ShowMessageBox("Horizontal scroll intercept","");
                if(motionEvent.getRawY()>DP2Pixel(100,getContext())) {//compare in DP
                    //return true to allow on touch to process
                    //return true;
                }
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
        //return false;

        return super.onInterceptTouchEvent(motionEvent);

    }
    protected void ShowErrorMessageBox(String strMethodName,Exception ex)
    {
        String strMessage = "Exception Name: "+ex.toString()+", Message: "+ex.getMessage()+", Cause: "+ex.getCause();
        Log.d("EXCEPTION: " + strMethodName, strMessage);

        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
        messageBox.setTitle(strMethodName);
        messageBox.setMessage(strMessage);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }
    public static int DP2Pixel(float dp,Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (metrics.density * dp + 0.5f);
    }
    public static float Pixel2DP(float px,Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float dp =(float)(px/metrics.density);
        return dp;
    }

}
