package tme.pos.CustomViewCtr;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;


import tme.pos.BusinessLayer.common;
import tme.pos.MainUIActivity;
import tme.pos.R;

/**
 * Created by vanlanchoy on 10/11/2014.
 */
public class ActivityLinearLayout extends LinearLayout {
    //protected GestureDetector myGestureDetector;

    private int CategotyHorizontalScrollbarViewBottom=0;
    //boolean blnActionBarProcess = false;
    boolean blnCollapseProcess = false;
    boolean blnUIDoneCollapsing = false;
    boolean blnGridViewExpanding = false;
    boolean blnExpandProcess = false;
    int intInitialScrollViewBottom =0;

    public ActivityLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Configure();
    }

    public ActivityLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Configure();
    }

    public ActivityLinearLayout(Context context) {
        super(context);
        Configure();
    }
    public void SlideDownCategoryGridView(final LinearLayout menuItemPanel, final int intTargetHeightBeforeVanish,final int intSpeed)
    {
        //LinearLayout MenuItemSelectionPanel =(LinearLayout)findViewById(R.id.MenuItemSelectionPanel);
        MyHorizontalScrollView mhsv = (MyHorizontalScrollView)findViewById(R.id.MyTopMenuContainerScrollbar);
        if(menuItemPanel.getTop()>=intTargetHeightBeforeVanish)//-MainUIActivity.DP2Pixel(30,getContext()))
        {

            mhsv.getLayoutParams().height=menuItemPanel.getBottom();

            mhsv.setBackground(null);

            //menuItemPanel.getLayoutParams().height=MenuItemSelectionPanel.getBottom();
            menuItemPanel.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0));
            menuItemPanel.setBackground(null);
            menuItemPanel.setVisibility(INVISIBLE);
            ((MainUIActivity)getContext()).ShowCategoryInGridView();
            findViewById(R.id.CheckoutPanel).setBackground(getResources().getDrawable(R.drawable.draw_top_border));
            findViewById(R.id.MenuItemPager).setBackground(getResources().getDrawable(R.drawable.draw_top_border));
            blnGridViewExpanding = false;
        }
        else
        {
            menuItemPanel.setTop(menuItemPanel.getTop()+intSpeed);
            Log.d("menu item top",menuItemPanel.getTop()+"");
            //MenuItemSelectionPanel.setTop(msv.getBottom());
            //menuItemPanel.setBackground(getResources().getDrawable(R.drawable.draw_gridview_category_bottom_border));
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    SlideDownCategoryGridView(menuItemPanel,intTargetHeightBeforeVanish,intSpeed+10);
                }
            });
        }
    }

    public void SlideUpCategoryGridView(final GridView view,final int intTargetHeightBeforeVanish,final int intSpeed,final String strSelectedTag )
    {
        //if(view.getBottom()<=intTargetHeightBeforeVanish)
        if(view.getBottom()<=intTargetHeightBeforeVanish)
        {
            final MainUIActivity mua = (MainUIActivity)getContext();
            LinearLayout MenuItemSelectionPanel =(LinearLayout) mua.findViewById(R.id.MenuItemSelectionPanel);
            LinearLayout CheckoutPanel = (LinearLayout)mua.findViewById(R.id.CheckoutPanel);
            MyHorizontalScrollView msv =(MyHorizontalScrollView)mua.findViewById(R.id.MyTopMenuContainerScrollbar);
            msv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));// msv.intInitialScrollViewBottom-msv.getTop()));
            final MyTopMenuContainer tmc = (MyTopMenuContainer)mua.findViewById(R.id.CategoryContainer);
            MenuItemSelectionPanel.getLayoutParams().height = CheckoutPanel.getTop()-tmc.getBottom();
            MenuItemSelectionPanel.setBackground(getResources().getDrawable(R.drawable.draw_top_border));
            view.setBackground(null);
            view.setPadding(0,20,0,0);
            view.setHorizontalSpacing(10);
            view.setVerticalSpacing(20);

            mua.HideCategoryInGridView();

            MenuItemSelectionPanel.setVisibility(VISIBLE);
            findViewById(R.id.CheckoutPanel).setBackground(null);
            findViewById(R.id.CheckoutPanel).setBackgroundColor(getResources().getColor(R.color.white_green));
            //findViewById(R.id.CheckoutPanel).setBackground(getResources().getDrawable(R.drawable.draw_checkout_panel_top_border));
            findViewById(R.id.MenuItemPager).setBackground(getResources().getDrawable(R.drawable.draw_top_border));

            //call to draw around selected item if not null, need to wait after newly create item visible
            //else the border will off
            if(strSelectedTag.length()>0)
            {
                for (int i = 0; i < tmc.getChildCount(); i++) {
                    //if (i % 2 == 0) {
                    if(tmc.getChildAt(i) instanceof MyCategoryItemView)
                    {
                        final MyCategoryItemView child = (MyCategoryItemView) tmc.getChildAt(i);
                        if ((child.getTag() + "").compareTo(strSelectedTag) == 0) {
                            //ShowMessageBox(child.getTag()+"",child.getLeft() +"");

                            //mua.LoadCategorySubMenuItem(strSelectedTag);
                            new Handler().postDelayed(
                               new Runnable() {
                                    @Override
                                    public void run() {
                                        tmc.DrawChildBorder(new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom()), child);
                                        mua.ShowPageItemLoading(strSelectedTag,null);
                                        //mua.LoadCategorySubMenuItem(strSelectedTag,null,-1);
                                    }
                            },500);
                            break;
                        }
                    }
                }
            }
        }
        else
        {
            view.setBottom(view.getBottom()-intSpeed);
            view.setBackground(getResources().getDrawable(R.drawable.draw_gridview_category_bottom_border));
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Log.d("Slide up grid view","selected tag is "+strSelectedTag);
                    SlideUpCategoryGridView(view,intTargetHeightBeforeVanish,intSpeed+10,strSelectedTag);
                }
            });
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {

        int action = MotionEventCompat.getActionMasked(motionEvent);

        MainUIActivity mua = (MainUIActivity)getContext();
        final LinearLayout MenuItemSelectionPanel =(LinearLayout) mua.findViewById(R.id.MenuItemSelectionPanel);
        final LinearLayout CheckoutPanel = (LinearLayout)mua.findViewById(R.id.CheckoutPanel);
        final MyHorizontalScrollView msv =(MyHorizontalScrollView)mua.findViewById(R.id.MyTopMenuContainerScrollbar);
        final MyTopMenuContainer tmc = (MyTopMenuContainer)mua.findViewById(R.id.CategoryContainer);
        final GridView gvCategory = (GridView) findViewById(R.id.gvCategory);
        //int intRawYInDP = (int)((MainUIActivity)getContext()).Pixel2DP(motionEvent.getRawY(),getContext());
        int intRawYInDP = (int)motionEvent.getRawY();
        float ActionBarHeight = 0f;

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                if(mua.getActionBar().isShowing())
                {

                    final android.content.res.TypedArray styledAttributes = mua.getTheme().obtainStyledAttributes(
                            new int[] { android.R.attr.actionBarSize });
                    ActionBarHeight =styledAttributes.getDimension(0, 0);
                    //ActionBarHeight = MainUIActivity.Pixel2DP(styledAttributes.getDimension(0, 0),getContext());
                    styledAttributes.recycle();



                }
                //if(motionEvent.getRawY()<MainUIActivity.DP2Pixel(20,getContext()) && blnActionBarProcess) {
                    //return true;
                //}
                //else if(blnCollapseProcess)
                if(blnCollapseProcess)
                {

                    CategotyHorizontalScrollbarViewBottom = msv.getBottom();


                    if((intRawYInDP<findViewById(R.id.CheckoutPanel).getTop()+MainUIActivity.DP2Pixel(20,getContext())+ (int)ActionBarHeight &&
                            intRawYInDP>findViewById(R.id.CheckoutPanel).getTop()-MainUIActivity.DP2Pixel(10,getContext())+(int)ActionBarHeight))
                    {
                        return true;
                    }
                }
                else if(blnExpandProcess)
                {
                    if(intRawYInDP<findViewById(R.id.MyTopMenuContainerScrollbar).getBottom()+MainUIActivity.DP2Pixel(20,getContext())+ActionBarHeight &&
                            intRawYInDP>findViewById(R.id.MyTopMenuContainerScrollbar).getBottom()-MainUIActivity.DP2Pixel(10,getContext())+ActionBarHeight)
                    {

                        return true;
                    }
                }
                break;
            case (MotionEvent.ACTION_MOVE):

/*
                if(motionEvent.getRawY()>MainUIActivity.DP2Pixel(50,getContext()) &&  blnActionBarProcess)//convert numerical dp to pixel
                {

                    if(!((MainUIActivity)getContext()).getActionBar().isShowing()) {
                        blnActionBarProcess=false;
                        ((MainUIActivity) getContext()).getActionBar().show();

                        ((MainUIActivity) getContext()).ReadjustMenuPanelComponentSizes();

                    }
                }
                else if(blnExpandProcess)
                    */
                if(blnExpandProcess && !blnGridViewExpanding)
                {
                    //Log.d("grid view expand Y value",motionEvent.getY()+"");
                    Log.d("msv bottom value",msv.getBottom()+"");
                    if(motionEvent.getY()>msv.getBottom()+MainUIActivity.DP2Pixel(100,getContext()))
                    {
                        blnGridViewExpanding = true;
                        Log.d("grid view expand","case 1");
                        //expand
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                //SlideDownCategoryGridView(msv,msv.getBottom()+10,10);

                                SlideDownCategoryGridView(MenuItemSelectionPanel,CheckoutPanel.getTop(),10);
                            }
                        };
                        new Handler().post(r);
                    }
                      //if(motionEvent.getY()<=MenuItemSelectionPanel.getTop())
                      else if(motionEvent.getY()<=intInitialScrollViewBottom)
                      {
                          Log.d("grid view expand","case 2");
                          //msv.setBottom(MenuItemSelectionPanel.getTop());
                          //MenuItemSelectionPanel.setTop((int)motionEvent.getY());
                          MenuItemSelectionPanel.setTop(intInitialScrollViewBottom);
                      }
                      else
                      {
                        Log.d("grid view expand","case 3");
                        //msv.setBottom((int)motionEvent.getY());
                        //msv.setBackground(getResources().getDrawable(R.drawable.draw_gridview_category_bottom_border));
                        MenuItemSelectionPanel.setTop((int)motionEvent.getY());
                      }
                }
                else if( blnCollapseProcess)
                {

                    if(motionEvent.getY()<CheckoutPanel.getTop()-MainUIActivity.DP2Pixel(30,getContext()))
                    {
                        Log.d("grid view collapse","case 1");
                        //collapse the expanded category grid view
                        blnCollapseProcess=false;
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                if(tmc.selectedChildItem!=null){
                                    SlideUpCategoryGridView(gvCategory,20,20,tmc.selectedChildItem.getTag().toString());
                                }
                                else
                                {
                                    SlideUpCategoryGridView(gvCategory,20,20,"");
                                }
                            }
                        };
                        new Handler().post(r);
                        /*
                        msv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));// msv.intInitialScrollViewBottom-msv.getTop()));
                        MenuItemSelectionPanel.getLayoutParams().height = CheckoutPanel.getTop()-tmc.getBottom();
                        mua.HideCategoryInGridView();
                        */
                    }
                    else if(motionEvent.getY()>=CheckoutPanel.getTop())
                    {
                        Log.d("grid view collapse","case 2");
                        //don't let the category scroll bar view extend beyond check out panel
                        gvCategory.setBottom(CheckoutPanel.getTop());
                        //msv.setBottom(CheckoutPanel.getTop());
                    }

                    else
                    {
                        Log.d("grid view collapse","case 3");
                        //category scroll bar view height follow user finger direction
                        //msv.setBottom((int)mua.Pixel2DP(motionEvent.getY(),getContext()));
                        gvCategory.setBottom((int)motionEvent.getY());

                        gvCategory.setBackground(getResources().getDrawable(R.drawable.draw_gridview_category_bottom_border));
                    }


                }
                break;
            case (MotionEvent.ACTION_UP):
                //blnActionBarProcess=false;
                if(blnCollapseProcess) {
                    blnCollapseProcess = false;
                    //msv.setBottom(CategotyHorizontalScrollbarViewBottom);
                    gvCategory.setBottom(CheckoutPanel.getTop());
                    gvCategory.setBackground(null);
                    gvCategory.setPadding(0,20,0,0);
                    gvCategory.setHorizontalSpacing(10);
                    gvCategory.setVerticalSpacing(20);
                }
                if(blnExpandProcess){
                    blnExpandProcess = false;
                    msv.setBackground(null);
                    msv.setBottom(intInitialScrollViewBottom);
                    MenuItemSelectionPanel.setTop(intInitialScrollViewBottom);
                    intInitialScrollViewBottom = 0;
                }
                break;

            case (MotionEvent.ACTION_CANCEL):

                break;
            case (MotionEvent.ACTION_OUTSIDE):

                break;

            default:
                break;
        }


        //myGestureDetector.onTouchEvent(motionEvent);

        return false;

    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent)
    {

        int action = MotionEventCompat.getActionMasked(motionEvent);

        MainUIActivity mua = (MainUIActivity)getContext();
        MyHorizontalScrollView HScrollView = (MyHorizontalScrollView) findViewById(R.id.MyTopMenuContainerScrollbar);
        //MyTopMenuContainer tmc = (MyTopMenuContainer)mua.findViewById(R.id.CategoryContainer);
        float intRawY = motionEvent.getRawY();
        float ActionBarHeight = 0f;

        switch (action) {
            case (MotionEvent.ACTION_DOWN):

                //hidden because category has expanded
                if(mua.getActionBar().isShowing())
                {
                    //screen been pushed down
                    //get action bar size
                    final android.content.res.TypedArray styledAttributes = mua.getTheme().obtainStyledAttributes(
                            new int[] { android.R.attr.actionBarSize });
                    ActionBarHeight = styledAttributes.getDimension(0, 0);

                    styledAttributes.recycle();


                }
                /*
                if(motionEvent.getRawY()<20) {
                    //user drag top of screen to show action bar
                    //return true to allow on touch to process

                    blnActionBarProcess=true;
                    return false;
                }
                else if (HScrollView.getVisibility()==View.GONE)//collapse process
                */
                if (HScrollView.getVisibility()==View.GONE)//collapse process
                {




                    if(intRawY<findViewById(R.id.CheckoutPanel).getTop()+MainUIActivity.DP2Pixel(20,getContext())+ActionBarHeight &&
                       intRawY>findViewById(R.id.CheckoutPanel).getTop()-MainUIActivity.DP2Pixel(10,getContext())+ActionBarHeight)
                    {
                        blnCollapseProcess=true;
                        return true;
                    }
                }
                else if(HScrollView.getVisibility()==View.VISIBLE)
                {

                    if(intRawY<findViewById(R.id.MyTopMenuContainerScrollbar).getBottom()+MainUIActivity.DP2Pixel(20,getContext())+ActionBarHeight &&
                            intRawY>findViewById(R.id.MyTopMenuContainerScrollbar).getBottom()-MainUIActivity.DP2Pixel(10,getContext())+ActionBarHeight)
                    {
                        blnExpandProcess = true;
                        intInitialScrollViewBottom = findViewById(R.id.MyTopMenuContainerScrollbar).getBottom();
                        return true;
                    }

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
        return false;
    }


    protected void Configure()
    {


    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);


    }


}
