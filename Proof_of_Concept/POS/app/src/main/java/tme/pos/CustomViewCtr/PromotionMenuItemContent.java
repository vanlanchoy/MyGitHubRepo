package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.PromotionObject;
import tme.pos.BusinessLayer.common;
import tme.pos.Interfaces.IOptionPopupItemListener;
import tme.pos.Interfaces.IPromotionMenuContentUpdateListener;
import tme.pos.R;

/**
 * Created by vanlanchoy on 5/30/2016.
 */
public class PromotionMenuItemContent extends LinearLayout{//} implements MyScrollView.ScrollViewListener {
    Handler checkPromotionOverTimerHandler;
    Runnable checkPromotionOverRunnable;
    IPromotionMenuContentUpdateListener promotionMenuContentUpdateListener;
    IOptionPopupItemListener popupItemListener;
    long timeToTrigger=-1;
    boolean blnIsEndTime = false;
    //float last_x_position;
    //float last_y_position;
    public PromotionMenuItemContent(Context c,IOptionPopupItemListener l2)
    {
        super(c);
        Instantiate(l2);
    }
    public PromotionMenuItemContent(Context c, AttributeSet attributeSet,IOptionPopupItemListener l2)
    {
        super(c,attributeSet);
        Instantiate(l2);
    }
    public void SetListener(IPromotionMenuContentUpdateListener l)
    {
        promotionMenuContentUpdateListener =l;


    }
    public void StopRefreshTimer()
    {
        if(checkPromotionOverTimerHandler!=null){checkPromotionOverTimerHandler.removeCallbacks(checkPromotionOverRunnable);}
    }
    private void Instantiate(IOptionPopupItemListener l2)
    {
        setOrientation(VERTICAL);
        popupItemListener = l2;
        ScrollView sv = new ScrollView(getContext());
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,common.Utility.DP2Pixel(440,getContext()));

        addView(sv,lllp);


        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(VERTICAL);

        sv.addView(ll);

        CreatePageItems();
    }
    private void ScheduleCheckPromotionOverTask()
    {
        int hr = 0;
        int minute = 0;
        //kick off timer
        if(checkPromotionOverTimerHandler==null) {
            checkPromotionOverTimerHandler = new Handler();
        }
        if(checkPromotionOverRunnable==null)
        {
            checkPromotionOverRunnable = new Runnable() {
                @Override
                public void run() {
                    CreatePageItems();
                }
            };
        }



               if(timeToTrigger<=-1)
        {

            timeToTrigger =(((24*60*60))-
                    ((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60*60)+(Calendar.getInstance().get(Calendar.MINUTE)*60)+Calendar.getInstance().get(Calendar.SECOND)))*1000;
        }
        else
        {
            //adjust the time so that it will execute exactly on time
            Calendar cNext = new GregorianCalendar();
            cNext.setTimeInMillis(timeToTrigger);
            hr = cNext.get(Calendar.HOUR_OF_DAY);
            minute = cNext.get(Calendar.MINUTE);

            timeToTrigger =(((hr*60*60)+(minute*60))-
                    ((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60*60)+(Calendar.getInstance().get(Calendar.MINUTE)*60)+Calendar.getInstance().get(Calendar.SECOND)))*1000;
            timeToTrigger+=(timeToTrigger>-1 && blnIsEndTime)?60000:0;

        }

        checkPromotionOverTimerHandler.postDelayed(
                checkPromotionOverRunnable, timeToTrigger);

       /*if(((LinearLayout)((ScrollView)getChildAt(0)).getChildAt(0)).getChildCount()>0) {
           TextView tv = (TextView) ((LinearLayout) ((ScrollView) getChildAt(0)).getChildAt(0)).getChildAt(0);
           tv.setText(tv.getText() + " (Next check is " + common.Utility.ReturnDateTimeString(Calendar.getInstance().getTimeInMillis() + timeToTrigger) + ") hr=" + hr + " min=" + minute);
       }*/
    }

    protected  void CreatePageItems()
    {
        if(checkPromotionOverTimerHandler!=null)checkPromotionOverTimerHandler.removeCallbacks(null);



        CreateListViewModePagePromotionItems();





        ScheduleCheckPromotionOverTask();
    }
    private void CreateListViewModePagePromotionItems()
    {
        ArrayList<PromotionObject> results= common.myPromotionManager.GetPromotionForToday();//.GetPromotionForCurrentMoment();

        //value only available after calling myPromotionManager.GetPromotionForCurrentMoment()
        //common.myPromotionManager.GetStartTimeForNextPromotion();
        //Date d = new Date(timeToTrigger);


        CreatePromotionListItemRow(results);


    }


    private void CreatePromotionListItemRow(ArrayList<PromotionObject>promotions)
    {

        Calendar cNextPromotionEnd = new GregorianCalendar();
        Calendar cNextPromotionStart = new GregorianCalendar();
        timeToTrigger = -1;//reset
        //Calendar cTemp = new GregorianCalendar();

        //cNextPromotionStart.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        //cNextPromotionEnd.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

        int currentTriggerHr=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentTriggerMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int currentHr = currentTriggerHr;
        int currentMinute = currentTriggerMinute;
        ((LinearLayout)((ScrollView)getChildAt(0)).getChildAt(0)).removeAllViews();
        for(int i=0;i<promotions.size();i++)
        {

            /**we are only interested in promotion trigger by item***/
            if(promotions.get(i).GetRule()== Enum.PromotionByType.total)continue;

            //get next trigger reload time
            //cTemp.setTimeInMillis(promotions.get(i).GetEndDateTime());
            cNextPromotionStart.setTimeInMillis(promotions.get(i).GetStartDateTime());
            //cNextPromotionStart.add(Calendar.MINUTE,1);
            cNextPromotionStart.set(Calendar.SECOND,0);
            cNextPromotionEnd.setTimeInMillis(promotions.get(i).GetEndDateTime());
            //cNextPromotionEnd.add(Calendar.MINUTE,1);
            cNextPromotionEnd.set(Calendar.SECOND,0);
            int startHr=cNextPromotionStart.get(Calendar.HOUR_OF_DAY);
            int startMin = cNextPromotionStart.get(Calendar.MINUTE);
            int endHr=cNextPromotionEnd.get(Calendar.HOUR_OF_DAY);
            int endMin = cNextPromotionEnd.get(Calendar.MINUTE);

            if(timeToTrigger==-1)
            {
                //need to be greater than current moment initially!!!
                if (currentTriggerHr == startHr && currentTriggerMinute < startMin ){
                    timeToTrigger = cNextPromotionStart.getTimeInMillis();
                    currentTriggerHr = startHr;
                    currentTriggerMinute = startMin;
                    blnIsEndTime=false;
                } else if (currentTriggerHr < startHr
                        ) {
                    timeToTrigger = cNextPromotionStart.getTimeInMillis();
                    currentTriggerHr = startHr;
                    currentTriggerMinute = startMin;
                    blnIsEndTime=false;
                } else if (currentTriggerHr == endHr && currentTriggerMinute < endMin
                        ) {
                    timeToTrigger = cNextPromotionEnd.getTimeInMillis();
                    currentTriggerHr = endHr;
                    currentTriggerMinute = endMin;
                    blnIsEndTime=true;
                } else if (currentTriggerHr < endHr
                        ) {
                    timeToTrigger = cNextPromotionEnd.getTimeInMillis();
                    currentTriggerHr = endHr;
                    currentTriggerMinute = endMin;
                    blnIsEndTime=true;
                }
            }
            else {
                if (currentTriggerHr == startHr && currentTriggerMinute > startMin &&
                        ((currentHr==startHr && currentMinute<startMin ) ||
                                currentHr<startHr)
                        ) {
                    timeToTrigger = cNextPromotionStart.getTimeInMillis();
                    currentTriggerHr = startHr;
                    currentTriggerMinute = startMin;
                    blnIsEndTime=false;
                } else if (currentTriggerHr >startHr &&
                        ((currentHr==startHr && currentMinute<startMin ) ||
                                currentHr<startHr)
                        ) {
                    timeToTrigger = cNextPromotionStart.getTimeInMillis();
                    currentTriggerHr = startHr;
                    currentTriggerMinute = startMin;
                    blnIsEndTime=false;
                } else if (currentTriggerHr == endHr && currentTriggerMinute > endMin &&
                        ((currentHr==endHr && currentMinute<endMin ) ||
                                currentHr<endHr)
                        ) {
                    timeToTrigger = cNextPromotionEnd.getTimeInMillis();
                    currentTriggerHr = endHr;
                    currentTriggerMinute = endMin;
                    blnIsEndTime=true;
                } else if ( currentTriggerHr > endHr &&
                        ((currentHr==endHr && currentMinute<endMin ) ||
                                currentHr<endHr)
                        ) {
                    timeToTrigger = cNextPromotionEnd.getTimeInMillis();
                    currentTriggerHr = endHr;
                    currentTriggerMinute = endMin;
                    blnIsEndTime=true;
                }
            }



        }

        //add one minute if is end time
        //Date d1 = new Date(timeToTrigger);
        //timeToTrigger+=(timeToTrigger>-1 && blnIsEndTime)?60000:0;
        //Date d2 = new Date(timeToTrigger);
        //now get the promotion is happening at the moment
        promotions = common.myPromotionManager.FilterPromotionForCurrentMoment(promotions, Enum.PromotionByType.item);//GetPromotionForCurrentMoment();
        for(int i=0;i<promotions.size();i++) {
            /**we are only interested in promotion trigger by item***/
            if(promotions.get(i).GetRule()== Enum.PromotionByType.total)continue;

            PromotionTextView tvTitle = new PromotionTextView(getContext(),promotions.get(i).GetDiscountColor().value,popupItemListener,promotions.get(i).GetId());
            tvTitle.setBackgroundColor(getResources().getColor(promotions.get(i).GetDiscountColor().value));
            tvTitle.setText(promotions.get(i).GetTitle());
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.SP_MENU_ITEM_TEXT_SIZE);
            LinearLayout.LayoutParams lllp= new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvTitle.setLayoutParams(lllp);
            lllp.setMargins(0,0,0,10);

            ((LinearLayout)((ScrollView)getChildAt(0)).getChildAt(0)).addView(tvTitle);
        }
        if(promotions.size()==0)
        {
            TextView tvTitle = new TextView(getContext());
            tvTitle.setGravity(Gravity.CENTER);
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.SP_MENU_ITEM_TEXT_SIZE);
            tvTitle.setText("No promotion combo available");
            LinearLayout.LayoutParams lllp= new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvTitle.setLayoutParams(lllp);

            ((LinearLayout)((ScrollView)getChildAt(0)).getChildAt(0)).addView(tvTitle);
        }

        if(promotionMenuContentUpdateListener!=null)promotionMenuContentUpdateListener.ContentUpdated();
    }

    /*@Override
    public void onScrollChanged(MyScrollView msv, int x, int y, int old_x, int old_y) {
        if(listener!=null)listener.ContentUpdated();
    }*/
}
