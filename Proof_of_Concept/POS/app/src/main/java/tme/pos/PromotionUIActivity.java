package tme.pos;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Filter;


import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.CustomViewCtr.LinearLayoutWithExpansionAnimation;
import tme.pos.CustomViewCtr.MyScrollView;
import tme.pos.CustomViewCtr.PromotionSummaryPopup;


/**
 * Created by kchoy on 3/4/2016.
 */
public class PromotionUIActivity extends FragmentActivity implements DatePickerDialog.OnDateSetListener
,MyScrollView.IScrollViewListener{
    int CALENDAR_SLOT_SCROLLVIEW_HEIGHT;
    int CALENDAR_SLOT_INDEX_HEIGHT;
    int RAW_CALENDAR_SLOT_SCROLLVIEW_HEIGHT=80;
    int RAW_CALENDAR_SLOT_INDEX_HEIGHT=35;
    int MAX_DAILY_MODE_COLUMN_BAR= 8;
//    int TOTAL_DATE_SLOT_IN_CALENDAR_VIEW=42;
    //int MAX_WEEKLY_MODE_COLUMN_BAR= 7;
    //static int MINUTE_BUFFER=30*60*1000;//at least 30 mins from expiration date
    Handler handler = new Handler();
    //LinearLayout llActiveDialog;
    PromotionSummaryPopup llActiveDialog;
    LinearLayout llCurrentTimeBar;
    boolean blnDateDialogShow=false;
    boolean blnShowingPromotionDetailPopup =false;
    boolean blnShowingFilterPopup = false;
    TextView tvSelectedCalendarMode;
    TextView tvDateDisplayLabel;
    LinearLayout selectedCalendarDisplayPanel;
    boolean blnLoading;
    boolean blnInitialWeeklyPanelShow=true;
    Handler timerBarHandler;
    Runnable timerElapseRunnable;
    Runnable runPromotionSummaryPopup;
    ProgressDialog pd;
    boolean blnPromotionDetailDialogShow;

    /**daily panel**/
    TableLayout tblDayView;
    LinearLayout[]dayViewPromotionBar;//table row next to each time category
    boolean blnFingerDown=false;
    RelativeLayout rlDailyUIPanel;
    MyScrollView svDailyPanel;

    /**weekly panel**/
    RelativeLayout rlWeeklyUIPanel;
    TableLayout tblWeeklyView;
    TableLayout tblWeeklyCalendarHeader;
    MyScrollView svWeeklyPanel;
    //HashMap<Long,PromotionObject>distinctWeeklyIds= new HashMap<Long, PromotionObject>();

    /**monthly panel**/
    RelativeLayout rlMonthlyUIPanel;
    TableLayout tblMonthlyCalendarHeader;
    TableLayout tblMonthlyView;
    MyScrollView svMonthlyPanel;

    /**filter promotion list panel**/
    LinearLayout promotionListPanel;
    CheckBox chkShowAllPromotion;


    Enum.PromotionViewMode promotionViewMode = Enum.PromotionViewMode.daily;
    Enum.CallDateDialogFrom dialogFor = Enum.CallDateDialogFrom.from;

    int DAILY_TIME_RAW_PROMOTION_EVENT_WIDTH=120;
    int DAILY_BAR_RAW_PROMOTION_EVENT_WIDTH=1140;
    int DAILY_AND_WEEKLY_RAW_PROMOTION_EVENT_HEIGHT=60;
    int TABLE_HEADER_RAW_ROW_HEIGHT=33;
    int TABLE_HEADER_RAW_ROW_WIDTH=DAILY_BAR_RAW_PROMOTION_EVENT_WIDTH/7;

    float touched_X;
    float touched_Y;
    //float initial_X;
    float last_scroll_Y;
    ArrayList<LinearLayout> promotionEventUI;
    ArrayList<Long> onDisplayDailyPromotionIds;
    Handler handlerMonthlyViewPromotionUI;
    //boolean blnFingerDown = false;
    //Runnable runnableMontlhViewPromotionUI;

    public PromotionUIActivity()
    {

        //clear the cache
        common.myPromotionManager.ClearCache();
        promotionEventUI = new ArrayList<LinearLayout>();
        onDisplayDailyPromotionIds = new ArrayList<Long>();

        handlerMonthlyViewPromotionUI = new Handler();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(timerBarHandler!=null)timerBarHandler.removeCallbacks(null);//stop it
    }

    @Override
    protected void onResume() {

        super.onResume();
        ((POS_Application) getApplication()).setCurrentActivity(this);
        if(promotionViewMode== Enum.PromotionViewMode.daily)ScheduleDrawMinuteLineTask();
    }
    public void onScrollChanged(MyScrollView msv,int x, int y, int old_x, int old_y)
    {
        /*StringBuilder sb = new StringBuilder();
        int[] location = new int[2];
        for(int i=0;i<dayViewPromotionBar.length;i++)
        {

            dayViewPromotionBar[i].getLocationOnScreen(location);
            sb.append("[time: "+i+" x: "+location[0]+" y: "+location[1]+"]");

        }*/

    }
    public void ResetPromotionDialogPopupFlag()
    {
        blnPromotionDetailDialogShow = false;
    }
   /* private void ClearWeeklyPanelDrawing()
    {
        for(LinearLayout ll:promotionEventUI)
        {
            rlWeeklyUIPanel.removeView(ll);
        }
        promotionEventUI.clear();
    }*/
    private void ClearCalendarPanelDrawing()
    {
        for(LinearLayout ll:promotionEventUI)
        {
            ((RelativeLayout)ll.getParent()).removeView(ll);
            //rlDailyUIPanel.removeView(ll);
        }
        promotionEventUI.clear();

    }



    private boolean IsDayViewConflicting(PromotionObject po1,PromotionObject po2)
    {
        Calendar cPo1Start = new GregorianCalendar();
        cPo1Start.setTimeInMillis(po1.GetStartDateTime());
        cPo1Start.set(2000, 1, 1, cPo1Start.get(Calendar.HOUR_OF_DAY), cPo1Start.get(Calendar.MINUTE), 0);
        cPo1Start.set(Calendar.MILLISECOND, 0);

        Calendar cPo1End = new GregorianCalendar();
        cPo1End.setTimeInMillis(po1.GetEndDateTime());
        cPo1End.set(2000, 1, 1, cPo1End.get(Calendar.HOUR_OF_DAY), cPo1End.get(Calendar.MINUTE), 59);
        cPo1End.set(Calendar.MILLISECOND, 999);

        Calendar cPo2Start = new GregorianCalendar();
        cPo2Start.setTimeInMillis(po2.GetStartDateTime());
        cPo2Start.set(2000, 1, 1, cPo2Start.get(Calendar.HOUR_OF_DAY), cPo2Start.get(Calendar.MINUTE), 0);
        cPo2Start.set(Calendar.MILLISECOND, 0);


        Calendar cPo2End = new GregorianCalendar();
        cPo2End.setTimeInMillis(po2.GetEndDateTime());
        cPo2End.set(2000, 1, 1, cPo2End.get(Calendar.HOUR_OF_DAY), cPo2End.get(Calendar.MINUTE), 59);
        cPo2End.set(Calendar.MILLISECOND, 999);

        if(!(
                (cPo1End.getTimeInMillis()<cPo2Start.getTimeInMillis())
                || (cPo1Start.getTimeInMillis() >cPo2End.getTimeInMillis())
                    )
                )
        {
            return true;
        }

        return false;
    }
    private boolean IsWeekViewConflicting(PromotionObject po1,PromotionObject po2,HashMap<Integer,HashMap<Integer,ArrayList<Long>>> weekData)
    {

        boolean blnFoundPo1 = false;
        boolean blnFoundPo2 = false;
        for(HashMap<Integer,ArrayList<Long>> dayData:weekData.values())
        {
            //reset each day level
            blnFoundPo1 = blnFoundPo2 = false;
            for(ArrayList<Long> hourData:dayData.values())
            {
                for(long id:hourData) {
                    if (po1.GetId()==id)blnFoundPo1=true;
                    if (po2.GetId()==id)blnFoundPo2=true;
                    if(blnFoundPo1 && blnFoundPo2)break;
                }

                if(blnFoundPo1 && blnFoundPo2)
                {
                    return IsDayViewConflicting(po1,po2);

                }
            }
        }
        return false;
    }
    private ArrayList<PromotionObject>StartCombination(ArrayList<PromotionObject> list
            ,ArrayList<PromotionObject>remaining)
    {
        if(remaining.size()==0)return list;
        boolean blnConflict = false;
        Calendar c1Start = new GregorianCalendar();
        //Calendar c2Start = new GregorianCalendar();
        Calendar c1End = new GregorianCalendar();
        //Calendar c2End = new GregorianCalendar();
        ArrayList<PromotionObject>maxList=new ArrayList<PromotionObject>();

        for(int i=remaining.size()-1;i>-1;i--)
        {
            ArrayList<PromotionObject> copiedList = new ArrayList<PromotionObject>(list);
            PromotionObject currentPO = remaining.remove(i);
            //we are only interested in time component
            c1Start.setTimeInMillis(currentPO.GetStartDateTime());
            c1Start.set(2015, 1, 1);
            c1End.setTimeInMillis(currentPO.GetEndDateTime());
            c1End.set(2015, 1, 1);
            //check whether the current promotion will fit into the remaining time slots
            //check for overlap
            if(copiedList.size()==0)
            {
                copiedList.add(currentPO);
                ArrayList<PromotionObject> temp = StartCombination(copiedList, new ArrayList<PromotionObject>(remaining));
                if (temp.size() > maxList.size()) {
                    maxList = temp;
                }
            }
            else {
                //reset
                blnConflict=false;
                for (int j = 0; j < list.size(); j++) {
                    if(IsDayViewConflicting(currentPO,list.get(j)))
                    {
                        blnConflict = true;
                        break;
                    }

                }
                //didn't break early because of conflict
                if(!blnConflict)
                {
                    copiedList.add(currentPO);
                    ArrayList<PromotionObject> temp = StartCombination(copiedList, new ArrayList<PromotionObject>(remaining));
                    if (temp.size() > maxList.size()) {
                        maxList = temp;
                    }
                }
                else
                {
                    //compare with current list current list
                    if (copiedList.size() > maxList.size()) {
                        maxList = copiedList;
                    }
                }
            }
        }

        return maxList;
    }
    private Set<Long> GetDistinctWeekPromotionIds( HashMap<Integer,HashMap<Integer,ArrayList<Long>>>rawData)
    {
        HashMap<Long,Boolean> hm = new HashMap<Long, Boolean>();
        //HashMap<Integer,HashMap<Integer,ArrayList<Long>>>rawData = GetWeeklyModeAllPromotionHM();
        for(HashMap<Integer,ArrayList<Long>> hourHm:rawData.values())
        {
            for(ArrayList<Long>ids:hourHm.values())
            {
                for(Long id:ids)
                {
                    if(!hm.containsKey(hm))
                    {
                        hm.put(id,true);
                    }
                }
            }
        }
        return hm.keySet();
    }
    private HashMap<Integer,HashMap<Integer,ArrayList<Long>>> GetWeeklyModeAllPromotionHM()
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        //c.add(Calendar.DATE, c.getFirstDayOfWeek()-c.get(Calendar.DAY_OF_WEEK));
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());



        return common.myPromotionManager.GetPromotionWeekByDate_WeekDayAsKey(c.get(Calendar.DATE), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }
    private ArrayList<Long> GetDailyModeAllPromotionList()
    {

        Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        int month = c.get(Calendar.MONTH)+1;
        int year = c.get(Calendar.YEAR);
        int date = c.get(Calendar.DATE);

        //save tag
        rlDailyUIPanel.setTag(c.getTimeInMillis());

        return GetPromotionIdsByGivenDate(date, month, year);

    }
    private ArrayList<Long> GetPromotionIdsByGivenDate(int date,int not_calendar_month, int year)
    {
        return common.myPromotionManager.GetDistinctPromotionIds(date,not_calendar_month,year,(promotionViewMode== Enum.PromotionViewMode.monthly)?true:false);
        /*//get today's promotion
        HashMap<Integer,ArrayList<Long>>data= common.myPromotionManager.GetPromotionWeekByDate_WeekDayAsKey(date,not_calendar_month,year).get(date);

        //get distinct id for today's date
        ArrayList<Long>todayIds = new ArrayList<Long>();



        if(data!=null) {
            boolean blnFound = false;
            for (int hr : data.keySet()) {
                for (long id : data.get(hr)) {
                    blnFound = false;
                    for (long storeId : todayIds) {
                        if (storeId == id) {
                            blnFound = true;
                            break;
                        }
                    }
                    if (!blnFound) {
                        todayIds.add(id);
                    }
                }
            }
        }

        return todayIds;*/
    }
    private void OrderAndDrawWeeklyPromotionBar(HashMap<Integer,HashMap<Integer,ArrayList<Long>>> data)
    {
        /** #1 will going to fill up the 1st day of week (one column space only), try to
         * fill in as much possible**/
        //collect all the promotion objects with their id's
        HashMap<Long, PromotionObject>distinctWeeklyIds=new HashMap<Long, PromotionObject>();
        HashMap<Long,PromotionObject> currentDayAvailablePromotion= new HashMap<Long,PromotionObject>();
        //ArrayList<PromotionObject>displayingPromotions = new ArrayList<PromotionObject>();
        HashMap<Integer,ArrayList<PromotionObject>>drawnList = new HashMap<Integer, ArrayList<PromotionObject>>();
        //onDisplayDailyPromotionIds.clear();

        Calendar tempCal = new GregorianCalendar();
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long)tvDateDisplayLabel.getTag());
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());




        //start with 1st day of week
        //int currentDay = c.get(Calendar.DAY_OF_WEEK);
        int currentDay = c.get(Calendar.DATE);
        //int weekDay=c.get(Calendar.DAY_OF_WEEK);
        int dayCount=1;
        boolean blnConflict = false;
        boolean blnOverlapped= false;
        boolean blnFound = false;
        while(dayCount<8) {

            //clear the list from previous day
            currentDayAvailablePromotion.clear();
            //get distinct promotion object id from each hour for the particular day
            HashMap<Integer,ArrayList<Long>> TargetDay = data.get(currentDay);
            for(ArrayList<Long> ids:TargetDay.values())
            {
                for(Long id:ids) {
                    PromotionObject currentPo = common.myPromotionManager.Get(id);
                    //storing for current day only, add 1st and remove later after checking for conflict
                    currentDayAvailablePromotion.put(id,currentPo);

                    //storing for the whole week
                        if(!distinctWeeklyIds.containsKey(id))
                        {
                            //compare to each promotion object and check for conflict before adding

                            blnConflict = false;
                            for(ArrayList<PromotionObject> checkList:drawnList.values())
                            {
                                for(PromotionObject comparePo:checkList)
                                {

                                    if(IsWeekViewConflicting(comparePo,currentPo,data))
                                    {
                                        blnConflict = true;
                                        break;
                                    }

                                }

                                //stop the next loop if already found a conflict
                                if(blnConflict)break;
                            }

                            //didn't have any conflict with previous drawn promotion, so add to distinct list
                            if(!blnConflict)
                            {

                                distinctWeeklyIds.put(id,currentPo);
                            }
                            else
                            {
                                //remove it from the current day pool selection if is conflict with others
                                blnOverlapped=true;
                                currentDayAvailablePromotion.remove(id);
                            }

                        }


                }
            }


            //maximize number of promotion object to display on single column
            drawnList.put(currentDay, StartCombination(new ArrayList<PromotionObject>(),new ArrayList<PromotionObject>(currentDayAvailablePromotion.values())));

            //secondary check for conflict on current day, previously is checking for past day
            if(drawnList.get(currentDay).size()!=currentDayAvailablePromotion.size())blnOverlapped=true;

            for (PromotionObject po : drawnList.get(currentDay)) {
                //add to must include list
                //blnFound=false;
                /*for(Long id:onDisplayDailyPromotionIds) {
                    if(id==po.GetId()){blnFound=true;break;}

                }
                if(!blnFound){onDisplayDailyPromotionIds.add(po.GetId());}*/
                //tempIds = data.get(Calendar.getInstance().getFirstDayOfWeek());
                tempCal.setTimeInMillis(po.GetStartDateTime());
                int startHour = tempCal.get(Calendar.HOUR_OF_DAY);
                int startMinute = tempCal.get(Calendar.MINUTE);
                tempCal.setTimeInMillis(po.GetEndDateTime());
                int endHour = tempCal.get(Calendar.HOUR_OF_DAY);

                int endMinute = tempCal.get(Calendar.MINUTE);
                LinearLayout ll = CreatePromotionEventUI(po.GetId(), startHour, endHour, startMinute, endMinute, po.GetTitle(), po.GetDiscountColor().value, dayCount);
                promotionEventUI.add(ll);
                rlWeeklyUIPanel.addView(ll);
            }

            //increment to next day
            c.add(Calendar.DATE,1);
            //currentDay=c.get(Calendar.DAY_OF_WEEK);
            currentDay=c.get(Calendar.DATE);
            dayCount++;
        }

        if(blnOverlapped) {

            Toast.makeText(this, "Some promotions are overlapped might not visible.", Toast.LENGTH_SHORT).show();
        }

    }
    private void OrderAndDrawDailyPromotionBar(ArrayList<Long> todayIds)
    {
        //try to fill in the time slot with as many promotion as possible

        Calendar c = new GregorianCalendar();
        ArrayList<PromotionObject>list = new ArrayList<PromotionObject>();
        for(long id:todayIds)
        {
            list.add(common.myPromotionManager.Get(id));
        }
        int columnCount=1;
        ArrayList<PromotionObject>drawList;//=new ArrayList<PromotionObject>();
        while(list.size()!=0)
        {
            drawList = StartCombination(new ArrayList<PromotionObject>(),new ArrayList<PromotionObject>(list));

            for(PromotionObject po:drawList)
            {
                c.setTimeInMillis(po.GetStartDateTime());
                int startHour=c.get(Calendar.HOUR_OF_DAY);
                int startMinute = c.get(Calendar.MINUTE);
                c.setTimeInMillis(po.GetEndDateTime());
                int endHour = c.get(Calendar.HOUR_OF_DAY);

                int endMinute = c.get(Calendar.MINUTE);
                LinearLayout ll=CreatePromotionEventUI(po.GetId(),startHour,endHour,startMinute,endMinute,po.GetTitle(),po.GetDiscountColor().value,columnCount);
                promotionEventUI.add(ll);
                rlDailyUIPanel.addView(ll);
            }
            //now remove drew promotion
            for(int i=0;i<drawList.size();i++)
            {
                for(int j=list.size()-1;j>-1;j--)
                {
                    if(list.get(j).GetId()==drawList.get(i).GetId())
                        list.remove(j);
                }
            }
            columnCount++;
            if(columnCount>MAX_DAILY_MODE_COLUMN_BAR) {
                if(list.size()>0)
                {
                    Toast.makeText(this, "Some promotions are overlapped might not visible.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    private void DrawWeeklyData(HashMap<Integer,HashMap<Integer,ArrayList<Long>>> data,ArrayList<Long>showFilteredList)
    {
        //ClearWeeklyPanelDrawing();
        ClearCalendarPanelDrawing();
        onDisplayDailyPromotionIds = showFilteredList;//new ArrayList<Long>(GetDistinctWeekPromotionIds(data));
        FilterNonShowingId(data,showFilteredList);
        if(data.size()>0)OrderAndDrawWeeklyPromotionBar(data);
    }
    private void FilterNonShowingId(HashMap<Integer,HashMap<Integer,ArrayList<Long>>> data,ArrayList<Long>showFilteredList)
    {
        boolean blnFound = false;
        for(HashMap<Integer,ArrayList<Long>> tempHr:data.values())
        {

            for(ArrayList<Long> ids:tempHr.values())
            {
                for(int i=ids.size()-1;i>-1;i--)
                {
                    long currentId = ids.get(i);
                    blnFound = false;
                    for(Long mustInclude:showFilteredList)
                    {
                        if(mustInclude==currentId)
                        {
                            blnFound = true;
                            break;
                        }
                       /* long internalId = ids.get(i);
                        if(mustInclude==ids.get(i).longValue()) {

                        }*/
                    }
                    if(!blnFound)
                    {
                        ids.remove(i);
                    }
                }
            }
        }
    }
    private void DrawDailyData(ArrayList<Long> todayIds)
    {
        //show promotion on table row
        //ClearDailyPanelDrawing();
        ClearCalendarPanelDrawing();
        //save the filtered list
        onDisplayDailyPromotionIds = todayIds;

        //ArrayList<Long> todayIds =GetDailyModeAllPromotionList();
        if(todayIds.size()>0)OrderAndDrawDailyPromotionBar(todayIds);

    }
    private LinearLayout PromotionUIHitTest(float x,float y,MyScrollView msv)
    {
        //Rect r = new Rect();
        //MyScrollView msv = (MyScrollView)findViewById(R.id.svDailyPanel);
        //msv.getDrawingRect(r);
        LinearLayout ll=null;
        int[] position = new int[2];
        for(int i =0;i<promotionEventUI.size();i++)
        {
            float widthRange =promotionEventUI.get(i).getX()+promotionEventUI.get(i).getWidth();
            float heightRange =promotionEventUI.get(i).getY()+promotionEventUI.get(i).getHeight();

            if(promotionViewMode== Enum.PromotionViewMode.monthly)
            {
                /*//special case for monthly view
                promotionEventUI.get(i).getLocationOnScreen(position);
                if(position[0]<=x && x<=position[0]+promotionEventUI.get(i).getWidth()
                        && position[1]<=y && y<=position[1]+promotionEventUI.get(i).getHeight())
                {
                    return promotionEventUI.get(i);
                }*/
            }
            else
            {
                if (promotionEventUI.get(i).getX() <= x && widthRange >= x
                        && promotionEventUI.get(i).getY() <= y && heightRange >= y) {
                    return promotionEventUI.get(i);
                }
            }
        }

        return ll;
    }
    public void PreparePanel(boolean blnSamePanel)
    {
        if(blnLoading)return;

        //cleanup any runnable in the queue
        if(timerBarHandler!=null)timerBarHandler.removeCallbacksAndMessages(null);

        blnLoading = true;
        int panelId=0;
        //hide the promotion summary popup if any
        ClosePromotionActiveDialog();


        if(promotionViewMode== Enum.PromotionViewMode.daily)
        {
            panelId = R.id.llDayPanel;
            if((Long)tvDateDisplayLabel.getTag()!=(Long)rlDailyUIPanel.getTag()) {
                //draw again if the date is not same anymore
                DrawDailyData(GetDailyModeAllPromotionList());
            }

            DrawTimeLine();

        }
        else if(promotionViewMode== Enum.PromotionViewMode.weekly)
        {
            panelId = R.id.llWeeklyPanel;
            //display correct time window
            if((Long)tvDateDisplayLabel.getTag()!=(Long)rlWeeklyUIPanel.getTag()) {
                //draw again if the date is not same anymore
                HashMap<Integer,HashMap<Integer,ArrayList<Long>>> data = GetWeeklyModeAllPromotionHM();
                DrawWeeklyData(data, new ArrayList<Long>(GetDistinctWeekPromotionIds(data)));
            }
            UpdateWeeklyCalendarHeaderRow();
            DrawTimeLine();
            if(blnInitialWeeklyPanelShow) {
                blnInitialWeeklyPanelShow = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        svWeeklyPanel.smoothScrollBy(0,GetYAxisByGivenTimeInDailyView(8, 0));
                    }
                }, 1000);
            }
        }
        else
        {
            //monthly
            panelId = R.id.llMonthlyPanel;
            if((Long)tvDateDisplayLabel.getTag()!=(Long)rlMonthlyUIPanel.getTag()) {
                //draw again if the date is not same anymore
               DrawMonthlyData(GetMonthlyPromotion(),(ArrayList<Long>)GetDistinctMonthlyId());

            }


        }

        if(!blnSamePanel) {
            //show the layout at last
            ShowPanel(panelId, Enum.PromotionActivityPanel.calendar_display_type_panel);
        }
        else if(promotionViewMode== Enum.PromotionViewMode.monthly)
        {
            ShowProgressDialog();
            ScheduleResizeCalendarDaySlotScrollViewThread();
            /*//need to remove redundant scroll after showing
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ResizeCalendarDaySlotScrollView();
                }
            }, 1000);*/
        }

        blnLoading = false;
    }
    private void DrawMonthlyData(ArrayList<Long>[][] data,ArrayList<Long>showFilteredList)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        c.set(Calendar.DATE, 1);
        c=common.Utility.GetFirstDayOfWeekDate(c);

        onDisplayDailyPromotionIds = showFilteredList;
        FilterNonShowingId(data,showFilteredList);

        UpdateMonthlyDateContainer(new GregorianCalendar(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DATE)));
        //UpdateMonthlyPromotion(new GregorianCalendar(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DATE)));
        UpdateMonthlyPromotion(data);
    }
    private void FilterNonShowingId(ArrayList<Long>[][] data,List<Long>filteredList)
    {
        boolean blnFound = false;
        for(int i=0;i<6;i++)
        {
            for(int j=0;j<7;j++)
            {
                ArrayList<Long>daily_data = data[i][j];
                for(int k=daily_data.size()-1;k>-1;k--)
                {
                    blnFound = false;
                    for(int l=0;l<filteredList.size();l++)
                    {
                        if((long)daily_data.get(k)==(long)filteredList.get(l))
                        {
                            blnFound = true;
                            break;
                        }
                    }

                    if(!blnFound)
                    {
                        daily_data.remove(k);
                    }
                }

            }
        }
    }

    private List<Long> GetDistinctMonthlyId()
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        c.set(Calendar.DATE,1);
        c=common.Utility.GetFirstDayOfWeekDate(c);


        HashMap<Long,Boolean> table = new HashMap<Long, Boolean>();

        for(int i=0;i<6;i++) {

            //slot
            for (int j = 0; j < 7; j++) {

                ArrayList<Long> daily_ids =GetPromotionIdsByGivenDate(c.get(Calendar.DATE),c.get(Calendar.MONTH)+1,c.get(Calendar.YEAR));
                for(Long id : daily_ids)
                {
                    if(!table.containsKey(id))
                    {
                        table.put(id,true);
                    }
                }

                //move on to next day
                c.add(Calendar.DATE,1);
            }
        }
        return new ArrayList<Long>(table.keySet());

    }
    private void UpdateMonthlyPromotion(ArrayList<Long>[][] data)//Calendar c,HashMap<Long,Boolean> ToShowList)
    {

        ScrollView sv;
        LinearLayout promotionContainer;
        TableRow trWeek;
        TextView tvPO;

        Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        c.set(Calendar.DATE,1);
        c=common.Utility.GetFirstDayOfWeekDate(c);

        for(int i=0;i<6;i++)
        {
            trWeek = (TableRow)tblMonthlyView.getChildAt(i);
            //slot
            for(int j=0;j<7;j++)
            {
                //recreate scrollview if not present
                if(!(((LinearLayout)trWeek.getChildAt(j)).getChildAt(1) instanceof ScrollView))
                {
                    sv = new ScrollView(this);
                    //remove tablelayout at position one and add it to into scrollview
                    LinearLayout ll = (LinearLayout) ((LinearLayout)trWeek.getChildAt(j)).getChildAt(1);
                    ((LinearLayout)trWeek.getChildAt(j)).removeViewAt(1);
                    LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,CALENDAR_SLOT_SCROLLVIEW_HEIGHT);
                    ((LinearLayout)trWeek.getChildAt(j)).addView(sv,lllp);

                    ScrollView.LayoutParams svlpContainer = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,ScrollView.LayoutParams.WRAP_CONTENT);
                    sv.addView(ll,svlpContainer);

                }
                else
                {
                    sv = (ScrollView)((LinearLayout)trWeek.getChildAt(j)).getChildAt(1);
                }

                //sv = (ScrollView)((LinearLayout)trWeek.getChildAt(j)).getChildAt(1);
                sv.getLayoutParams().height = -2;
                sv.setTag(c.getTimeInMillis());//sv.setBackgroundColor(Color.RED);
                promotionContainer = (LinearLayout) sv.getChildAt(0);//promotionContainer.setBackgroundColor(Color.GREEN);
                promotionContainer.removeAllViews();

                //get distinct promotion for current day
                ArrayList<Long> ids =data[i][j];
                for(Long id : ids)
                {


                    PromotionObject po = common.myPromotionManager.Get(id);
                    tvPO = new TextView(this);

                    tvPO.setText(po.GetTitle());
                    tvPO.setTag(po.GetId());
                    LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tvPO.setPadding(2,2,2,2);
                    lllp.bottomMargin=5;
                    tvPO.setBackgroundColor(getResources().getColor(po.GetDiscountColor().value));
                    promotionContainer.addView(tvPO,lllp);


                }


                //move on to next day
                c.add(Calendar.DATE,1);


            }

        }


    }
    private void ResizeCalendarDaySlotScrollView()
    {
        TableRow trWeek;
        ScrollView sv;
        LinearLayout promotionContainer;
        common.Utility.LogActivity("Resizing calendar day slot scroll view, date "+tvDateDisplayLabel.getText());
        for(int i=0;i<6;i++) {
            trWeek = (TableRow) tblMonthlyView.getChildAt(i);
            //slot
            for (int j = 0; j < 7; j++) {
                common.Utility.LogActivity("i="+i+",j="+j+" day "+((TextView)((LinearLayout)trWeek.getChildAt(j)).getChildAt(0)).getText());
                sv = (ScrollView)((LinearLayout)trWeek.getChildAt(j)).getChildAt(1);
                promotionContainer = (LinearLayout) sv.getChildAt(0);

                //resize the scroll view as well
                //int tempHeight = promotionContainer.getHeight();
                if(promotionContainer.getHeight()>CALENDAR_SLOT_SCROLLVIEW_HEIGHT)
                {
                    sv.getLayoutParams().height=CALENDAR_SLOT_SCROLLVIEW_HEIGHT;
                    final GestureDetector myGestureDetector = new GestureDetector(this,new MyMonthlyCalendarGestureListener(sv));
                    //disable the linear layout touch listener in sv
                    sv.getChildAt(0).setOnTouchListener(null);
                    sv.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, final MotionEvent event) {

                            if (blnShowingFilterPopup)
                            {HideRightSideFilterBar();}
                            //destroy any active dialog
                            if (!blnShowingPromotionDetailPopup && llActiveDialog != null)
                            {
                                ClosePromotionActiveDialog();
                            }
                            else if(blnShowingPromotionDetailPopup && llActiveDialog != null)
                            {
                                //queueing to calculate popup location
                                return false;
                            }

                            int action = MotionEventCompat.getActionMasked(event);
                            switch (action) {
                                //gesture detector will implement on down event
                                case MotionEvent.ACTION_CANCEL:
                                    blnFingerDown = false;
                                    view.getParent().requestDisallowInterceptTouchEvent(false);
                                    break;
                                case MotionEvent.ACTION_UP:
                                    blnFingerDown = false;
                                    view.getParent().requestDisallowInterceptTouchEvent(false);
                                    break;
                            }

                            return myGestureDetector.onTouchEvent(event);


                        }
                    });
                }
                else
                {
                    //remove the scrollview since scrolling not needed
                    sv = (ScrollView)((LinearLayout)trWeek.getChildAt(j)).getChildAt(1);
                    LinearLayout ll = (LinearLayout) sv.getChildAt(0);
                    sv.removeAllViews();
                    ll.setTag(sv.getTag());
                    ((LinearLayout)trWeek.getChildAt(j)).removeView(sv);
                    ((LinearLayout)trWeek.getChildAt(j)).addView(ll);
                    final GestureDetector myGestureDetector = new GestureDetector(this,new MyMonthlyCalendarGestureListenerWithoutScrolling(ll));
                    ll.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {

                            if (blnShowingFilterPopup)
                            {HideRightSideFilterBar();}
                            //destroy any active dialog
                            if (!blnShowingPromotionDetailPopup && llActiveDialog != null)
                            {
                                ClosePromotionActiveDialog();
                            }
                            else if(blnShowingPromotionDetailPopup && llActiveDialog != null)
                            {
                                //queueing to calculate popup location
                                return false;
                            }

                            int action = MotionEventCompat.getActionMasked(event);
                            switch (action) {
                                //gesture detector will implement on down event
                                case MotionEvent.ACTION_CANCEL:
                                    blnFingerDown = false;
                                    break;
                                case MotionEvent.ACTION_UP:
                                    blnFingerDown = false;
                                    break;
                            }

                            return myGestureDetector.onTouchEvent(event);
                        }
                    });

                }
            }
        }
        MonthlyViewCalendarLoadCompleted();
    }
    private void UpdateMonthlyDateContainer(Calendar c)
    {

        TextView tvIndex;
        //row
        for(int i=0;i<6;i++)
        {
            TableRow trWeek = (TableRow)tblMonthlyView.getChildAt(i);
            //slot
            for(int j=0;j<7;j++)
            {
                if(i!=5)
                {
                    if(j!=6) {
                        trWeek.getChildAt(j).setBackground(getResources().getDrawable(R.drawable.draw_border_left_top));
                    }
                    else
                    {
                        trWeek.getChildAt(j).setBackground(getResources().getDrawable(R.drawable.draw_border_left_top_right));
                    }
                }
                else
                {
                    if(j!=6) {
                        trWeek.getChildAt(j).setBackground(getResources().getDrawable(R.drawable.draw_border_left_top_bottom));
                    }
                    else
                    {
                        trWeek.getChildAt(j).setBackground(getResources().getDrawable(R.drawable.draw_border));
                    }
                }
                tvIndex =(TextView)((LinearLayout)trWeek.getChildAt(j)).getChildAt(0);
                Calendar cCurrentLabel = new GregorianCalendar();
                cCurrentLabel.setTimeInMillis((Long)tvDateDisplayLabel.getTag());
                if(c.get(Calendar.YEAR)==Calendar.getInstance().get(Calendar.YEAR)
                        && c.get(Calendar.MONTH)==Calendar.getInstance().get(Calendar.MONTH)
                        && c.get(Calendar.DATE)==Calendar.getInstance().get(Calendar.DATE))
                {
                    tvIndex.setTextColor(getResources().getColor(R.color.lost_shine_green));
                    tvIndex.setTypeface(null,Typeface.BOLD);
                }
                else {
                    if(cCurrentLabel.get(Calendar.MONTH)==c.get(Calendar.MONTH)) {
                        tvIndex.setTextColor(Color.BLACK);
                    }
                    else
                    {
                        tvIndex.setTextColor(getResources().getColor(R.color.divider_grey));
                    }
                    tvIndex.setTypeface(null, Typeface.NORMAL);
                }
                tvIndex.setText(c.get(Calendar.DATE)+"");
                c.add(Calendar.DATE,1);
            }
        }

    }

    private void UpdateWeeklyCalendarHeaderRow()
    {
        TextView tv;
        Date date;
        TableRow tr = (TableRow)tblWeeklyCalendarHeader.getChildAt(0);
        final Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        //c.add(Calendar.DATE,c.getFirstDayOfWeek()-c.get(Calendar.DAY_OF_WEEK));
        c.set(Calendar.DAY_OF_WEEK,c.getFirstDayOfWeek());

        int currentYear =Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth=Calendar.getInstance().get(Calendar.MONTH);
        int currentDate=Calendar.getInstance().get(Calendar.DATE);
        for(int i=1;i<tr.getChildCount();i++)
        {
            tv = (TextView)tr.getChildAt(i);
            date = new Date(c.getTimeInMillis());
            String strDate=GetDayOfWeek(c.get(Calendar.DAY_OF_WEEK))+"<br>"+new SimpleDateFormat("MM/dd/yyyy").format(date);
            final long timeInMili = date.getTime();
            tv.setText(Html.fromHtml(strDate));
            tv.setTag(timeInMili);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvDateDisplayLabel.setTag((Long)view.getTag());
                    findViewById(R.id.tvDay).callOnClick();
                }
            });
            //highlight if is today's date
            if(c.get(Calendar.YEAR)==currentYear
                    && c.get(Calendar.MONTH)==currentMonth
                    && c.get(Calendar.DATE)==currentDate)
            {
                tv.setBackgroundColor(getResources().getColor(R.color.lost_shine_green));
                tv.setTextColor(Color.WHITE);
            }
            else
            {
                tv.setBackgroundColor(Color.WHITE);
                tv.setTextColor(Color.BLACK);
            }

            c.add(Calendar.DATE,1);
        }
        //UpdateCalendarHeaderRow(tblWeeklyCalendarHeader);
    }

    private String GetDayOfWeek(int i)
    {
        switch (i)
        {
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
            default:
                return "";
        }
    }
    private LinearLayout CreatePromotionEventUI(long promotionId,int startHr,int endHr,int startMinute,int endMinute,String strTitle
            ,int colorId,int count)
    {


        final LinearLayout ll = new LinearLayout(this);
        ll.setTag(promotionId);


        if(colorId==Enum.DiscountColor.discount_blue.value)
            ll.setBackground(getResources().getDrawable(R.drawable.draw_promotion_blue_boarder));
        else if(colorId==Enum.DiscountColor.discount_indigo.value)
            ll.setBackground(getResources().getDrawable(R.drawable.draw_promotion_indigo_boarder));
        else if(colorId==Enum.DiscountColor.discount_green.value)
            ll.setBackground(getResources().getDrawable(R.drawable.draw_promotion_green_boarder));
        else if(colorId==Enum.DiscountColor.discount_pink.value)
            ll.setBackground(getResources().getDrawable(R.drawable.draw_promotion_pink_boarder));
        else if(colorId==Enum.DiscountColor.discount_yellow.value)
            ll.setBackground(getResources().getDrawable(R.drawable.draw_promotion_yellow_boarder));
        else if(colorId==Enum.DiscountColor.discount_orange.value)
            ll.setBackground(getResources().getDrawable(R.drawable.draw_promotion_orange_boarder));
        else if(colorId==Enum.DiscountColor.discount_brown.value)
            ll.setBackground(getResources().getDrawable(R.drawable.draw_promotion_brown_boarder));

        int PROMOTION_EVENT_WIDTH=common.Utility.DP2Pixel(DAILY_TIME_RAW_PROMOTION_EVENT_WIDTH, this);
        int PROMOTION_EVENT_HEIGHT=common.Utility.DP2Pixel(DAILY_AND_WEEKLY_RAW_PROMOTION_EVENT_HEIGHT, this);//-2 for offsetting boarder layout in promotion bar layout
        int WEEKLY_MONTHLY_DATE_BAR_WIDTH=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_WIDTH,PromotionUIActivity.this);

        float minuteHeight = endMinute;
        minuteHeight +=(endHr-startHr)*60f;
        minuteHeight -=startMinute;
        minuteHeight /=60f;

        LinearLayout.LayoutParams lllp  = new LinearLayout.LayoutParams(PROMOTION_EVENT_WIDTH, Math.round((PROMOTION_EVENT_HEIGHT)* minuteHeight));


        ll.setLayoutParams(lllp);
        ll.setPadding(common.Utility.DP2Pixel(10, this), 0, 0, 0);

        int minuteY = Math.round(PROMOTION_EVENT_HEIGHT * (Float.parseFloat(startMinute+"")/60f));

        if(promotionViewMode== Enum.PromotionViewMode.daily)
        {ll.setX((PROMOTION_EVENT_WIDTH + 10) * count);}
        else
        {ll.setX(((WEEKLY_MONTHLY_DATE_BAR_WIDTH) * (count-1))+PROMOTION_EVENT_WIDTH+5+((WEEKLY_MONTHLY_DATE_BAR_WIDTH-PROMOTION_EVENT_WIDTH)/2));}//taking into account the timer column width is not the same
        ll.setY(startHr * PROMOTION_EVENT_HEIGHT + minuteY);
        lllp.height=lllp.height<10?common.Utility.DP2Pixel(10,this):lllp.height;
        TextView tvTitle = new TextView(this);
        tvTitle.setText(strTitle);
        ll.addView(tvTitle);




        return ll;
    }
    private void SetTouchedLocation(float x,float y)
    {
        touched_X = x;
        touched_Y = y;
    }

    private void ShowPanelAnimation(final int ShowPanelId, final Enum.PromotionActivityPanel activityPanel)
    {

        float tempWidth=600;
        if(activityPanel== Enum.PromotionActivityPanel.calendar_display_type_panel)
        {
            tempWidth =selectedCalendarDisplayPanel.getWidth();
        }
        final float flPanelWidth = tempWidth;//(activityPanel== Enum.PromotionActivityPanel.calendar_display_type_panel)?600;
        TranslateAnimation movementSlideOut = new TranslateAnimation(0.0f,common.Utility.DP2Pixel(flPanelWidth, this),  0.0f, 0.0f);//move left
        movementSlideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LinearLayout tempPanel = null;
                switch (activityPanel) {

                    case calendar_display_type_panel:
                        tempPanel = selectedCalendarDisplayPanel;
                        break;
                    default:
                }
                tempPanel.setVisibility(View.GONE);
                tempPanel = (LinearLayout) findViewById(ShowPanelId);
                switch (activityPanel) {

                    case calendar_display_type_panel:
                        selectedCalendarDisplayPanel=tempPanel;
                        break;
                    default:
                }
                tempPanel.setVisibility(View.VISIBLE);

                TranslateAnimation movementSlideIn = new TranslateAnimation(-flPanelWidth, 0.0f, 0.0f, 0.0f);//move right


                movementSlideIn.setDuration(200);
                movementSlideIn.setFillAfter(true);


                tempPanel.startAnimation(movementSlideIn);

                if(promotionViewMode== Enum.PromotionViewMode.monthly)
                {
                    ShowProgressDialog();
                    ScheduleResizeCalendarDaySlotScrollViewThread();
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movementSlideOut.setDuration(200);
        LinearLayout tempPanel=null;
        switch (activityPanel) {

            case calendar_display_type_panel:
                tempPanel = selectedCalendarDisplayPanel;
                break;
            default:
        }
        tempPanel.startAnimation(movementSlideOut);
    }
    private void ScheduleResizeCalendarDaySlotScrollViewThread()
    {
        //cancel whatever is in the queue
        if(runPromotionSummaryPopup!=null)
        {
            handler.removeCallbacks(runPromotionSummaryPopup);
            runPromotionSummaryPopup = null;
        }
        runPromotionSummaryPopup = new Runnable() {
            @Override
            public void run() {
                ResizeCalendarDaySlotScrollView();
            }
        };
        handler.postDelayed(runPromotionSummaryPopup, 1000);
    }
    private void ShowPanel(int ShowPanelId,Enum.PromotionActivityPanel panelType)
    {
        LinearLayout panel = null;


        switch(panelType)
        {
            case calendar_display_type_panel:
                panel=selectedCalendarDisplayPanel;

                break;

            default:
        }
        if(panel!=null) {

            ShowPanelAnimation(ShowPanelId,panelType);

        }
        else {
            panel = (LinearLayout) findViewById(ShowPanelId);
            panel.setVisibility(View.VISIBLE);
            switch(panelType)
            {
                case calendar_display_type_panel:
                    selectedCalendarDisplayPanel=panel;

                    break;

                default:
            }
        }

    }
    public void HideRightSideFilterBar()
    {
        if(!blnShowingFilterPopup)return;
        common.Utility.LogActivity("Hide promotion filter");
        final ScrollView filterBar=(ScrollView)findViewById(R.id.filterBar);
        TranslateAnimation movementRight = new TranslateAnimation(filterBar.getX(), filterBar.getX() + 20, 0.0f, 0.0f);
        movementRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                filterBar.setVisibility(View.GONE);
                blnShowingFilterPopup = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //movementRight.setDuration(500);
        movementRight.setDuration(50);
        filterBar.startAnimation(movementRight);
    }
    private void ShowRightSideFilterBar()
    {
        common.Utility.LogActivity("show promotion filter");
        final ScrollView filterBar=(ScrollView)findViewById(R.id.filterBar);


        final TranslateAnimation movementLeft = new TranslateAnimation(common.Utility.DP2Pixel(200, this),0,0.0f,0.0f);
        movementLeft.setDuration(500);
        movementLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                filterBar.setVisibility(View.VISIBLE);
                blnShowingFilterPopup = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



        filterBar.startAnimation(movementLeft);
    }
    //compilation error due to set generic collection type
    //private ArrayList<PromotionObject> InsertionSortPromotionByTitle(ArrayList<PromotionObject> list)
    private void InsertionSort(PromotionObject po,ArrayList<PromotionObject>sorted)
    {
        boolean blnAdded = false;

        if(sorted.size()==0)
        {
            sorted.add(po);
        }
        else
        {
            for(int i=0;i<sorted.size();i++)
            {
                if(sorted.get(i).GetTitle().compareTo(po.GetTitle())>=0)
                {
                    sorted.add(i,po);
                    blnAdded = true;
                    break;
                }
            }
            if(!blnAdded)sorted.add(po);
        }
    }
    private ArrayList<PromotionObject> InsertionSortPromotionByTitle(PromotionObject[] promotionObjects)
    {
        ArrayList<PromotionObject>sorted = new ArrayList<PromotionObject>();

        for(PromotionObject po:promotionObjects)
        {
           InsertionSort(po,sorted);
        }

        return sorted;
    }
    private ArrayList<PromotionObject> InsertionSortPromotionByTitle(ArrayList<Long> list)
    {
        ArrayList<PromotionObject>sorted = new ArrayList<PromotionObject>();

        for(Long id:list)
        {
            PromotionObject po = common.myPromotionManager.Get(id);
            InsertionSort(po, sorted);
        }

        return sorted;
    }
    private void ListAvailablePromotionInFilterPanel()
    {
        promotionListPanel.removeAllViews();
        ArrayList<PromotionObject> list = new ArrayList<PromotionObject>();
        if(promotionViewMode== Enum.PromotionViewMode.weekly)
        {
            //complete distinct Promotion object id list
            ArrayList<Long>ids =new ArrayList<Long>(GetDistinctWeekPromotionIds(GetWeeklyModeAllPromotionHM()));
            for(Long id:ids)
            {

                    list.add(common.myPromotionManager.Get(id));
            }
            list = InsertionSortPromotionByTitle(list.toArray(new PromotionObject[0]));

        }
        else if(promotionViewMode== Enum.PromotionViewMode.daily)
        {
            //ArrayList<PromotionObject> list = InsertionSortPromotionByTitle(GetDailyModeAllPromotionList());
            list = InsertionSortPromotionByTitle(GetDailyModeAllPromotionList());

        }
        else
        {
            //monthly view
            list =InsertionSortPromotionByTitle((ArrayList<Long>)GetDistinctMonthlyId());
        }
        for(PromotionObject po:list)
        {
            CheckBox chk = new CheckBox(this);
            chk.setTag(po.GetId());
            chk.setText(po.GetTitle());
            chk.setTextColor(getResources().getColor(po.GetDiscountColor().textColorValue));
            promotionListPanel.addView(chk);

            //check the checkbox if is currently in the show list
            for(int i=0;i<onDisplayDailyPromotionIds.size();i++)
            {
                if(onDisplayDailyPromotionIds.get(i)==po.GetId())
                {
                    chk.setChecked(true);
                    break;
                }
            }

            chk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox clickedChk = (CheckBox) view;
                    if (clickedChk.isChecked()) {
                        //check all other filters have been checked then check the ALL filter if happen
                        int count = 0;
                        for (int i = 0; i < promotionListPanel.getChildCount(); i++) {
                            if (((CheckBox) promotionListPanel.getChildAt(i)).isChecked()) count++;
                        }
                        chkShowAllPromotion.setChecked(promotionListPanel.getChildCount() == count);
                    } else {
                        //unchecked ALL filter
                        chkShowAllPromotion.setChecked(false);
                    }
                }
            });
        }

        //finally check for 'Show All' if all promotions will be displayed
        if(onDisplayDailyPromotionIds.size()==list.size())chkShowAllPromotion.setChecked(true);
    }
    private void ConfigureFilterPanel()
    {

        promotionListPanel = (LinearLayout)findViewById(R.id.promotionListPanel);

        //filter bar
        ScrollView filterBar = (ScrollView)findViewById(R.id.filterBar);
        filterBar.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams rllp = (RelativeLayout.LayoutParams)filterBar.getLayoutParams();
        rllp.setMargins(0,0,0,0);

        findViewById(R.id.imgFilter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListAvailablePromotionInFilterPanel();
                ShowRightSideFilterBar();
            }
        });

        findViewById(R.id.imgHideFilterBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HideRightSideFilterBar();
            }
        });

        TextView tvApplyFilter = (TextView)findViewById(R.id.tvApplyFilter);
        tvApplyFilter.setText(Html.fromHtml("<u>Apply</u>"));
        tvApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!blnShowingPromotionDetailPopup && llActiveDialog != null)
                {ClosePromotionActiveDialog();}
                ApplyFilter();
                HideRightSideFilterBar();
            }
        });

        chkShowAllPromotion = (CheckBox)findViewById(R.id.chkShowAllPromotion);
        chkShowAllPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean blnChecked = false;
                if (chkShowAllPromotion.isChecked()) {
                    blnChecked = true;
                }

                for (int i = 0; i < promotionListPanel.getChildCount(); i++) {
                    ((CheckBox) promotionListPanel.getChildAt(i)).setChecked(blnChecked);
                }
            }
        });

    }
    private void ApplyFilter()
    {

        if(promotionViewMode== Enum.PromotionViewMode.daily)
        {
            //get complete list
            DrawDailyData(GetFilterPromotionIdList());

        }
        else if(promotionViewMode== Enum.PromotionViewMode.weekly)
        {
            //HashMap<Integer,HashMap<Integer,ArrayList<Long>>> data = GetWeeklyModeAllPromotionHM();
            //DrawWeeklyData(data,new ArrayList<Long>(GetDistinctWeekPromotionIds(data)));
            DrawWeeklyData(GetWeeklyModeAllPromotionHM(),GetFilterPromotionIdList());
        }
        else
        {
            //monthly
            DrawMonthlyData(GetMonthlyPromotion(), GetFilterPromotionIdList());
        }
    }
    private ArrayList<Long>[][] GetMonthlyPromotion()
    {
        ArrayList<Long>[][] ids = new ArrayList[6][7];
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        c.set(Calendar.DATE,1);
        c=common.Utility.GetFirstDayOfWeekDate(c);

        for(int i=0;i<6;i++) {

            //slot

            for (int j = 0; j < 7; j++) {


                ids[i][j]=GetPromotionIdsByGivenDate(c.get(Calendar.DATE),c.get(Calendar.MONTH)+1,c.get(Calendar.YEAR));
                //move on to next day
                c.add(Calendar.DATE,1);
            }
        }

        return ids;
    }
    private ArrayList<Long> GetFilterPromotionIdList()
    {
        ArrayList<Long>list =new ArrayList<Long>();
        if(promotionViewMode== Enum.PromotionViewMode.daily)
        {
            list =GetDailyModeAllPromotionList();
        }
        else if(promotionViewMode== Enum.PromotionViewMode.weekly)
        {
            HashMap<Integer,HashMap<Integer,ArrayList<Long>>> data = GetWeeklyModeAllPromotionHM();
            list =new ArrayList<Long>(GetDistinctWeekPromotionIds(data));
        }
        else
        {
            //monthly
            list =(ArrayList<Long>)GetDistinctMonthlyId();
        }
        for(int i=0;i<promotionListPanel.getChildCount();i++)
        {
            if(!((CheckBox)promotionListPanel.getChildAt(i)).isChecked())
            {
                for(int j=0;j<list.size();j++)
                {
                    long l1 = list.get(j);
                    long l2 = (Long)promotionListPanel.getChildAt(i).getTag();
                    if(l1==l2)
                    {
                        list.remove(j);
                        break;
                    }
                }
            }
        }
        return list;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ((POS_Application) getApplication()).setCurrentActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_promotion_ui);

        CALENDAR_SLOT_SCROLLVIEW_HEIGHT=common.Utility.DP2Pixel(RAW_CALENDAR_SLOT_SCROLLVIEW_HEIGHT,this);
        CALENDAR_SLOT_INDEX_HEIGHT=common.Utility.DP2Pixel(RAW_CALENDAR_SLOT_INDEX_HEIGHT,this);

        tvDateDisplayLabel = (TextView)findViewById(R.id.tvDateDisplayLabel);
        SetTextLabelProperties(tvDateDisplayLabel);
        tvDateDisplayLabel.setTextColor(getResources().getColor(R.color.green));
        tvDateDisplayLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        SetCurrentDateForDateInput(tvDateDisplayLabel, Enum.CallDateDialogFrom.label);
        tvDateDisplayLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blnShowingFilterPopup) HideRightSideFilterBar();
                //set flag preventing double dialog box for date picker on screen
                if (blnDateDialogShow) return;
                blnDateDialogShow = true;
                //destroy any active dialog
                if (!blnShowingPromotionDetailPopup && llActiveDialog != null)
                { ClosePromotionActiveDialog();}

                common.Utility.LogActivity("date display label clicked");
                dialogFor = Enum.CallDateDialogFrom.label;
                ShowDatePickerDialog();
            }
        });


        TextView tv = (TextView)findViewById(R.id.tvActivityTitle);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHART_OPTIONS_TITLE_TEXT_SIZE);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);

        findViewById(R.id.imgCreatePromotion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //destroy any active dialog
                if (!blnShowingPromotionDetailPopup && llActiveDialog != null)
                { ClosePromotionActiveDialog();}
                HideRightSideFilterBar();
                ShowAddPromotionPanel();
            }
        });
        findViewById(R.id.imgNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (promotionViewMode) {
                    case daily:
                        GoToNextDay();
                        break;
                    case weekly:
                        GoToNextWeek();
                        break;
                    case monthly:
                        GoToNextMonth();
                        break;
                }
            }
        });
        findViewById(R.id.imgPrevious).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (promotionViewMode) {
                    case daily:
                        GoToPreviousDay();
                        break;
                    case weekly:
                        GoToPreviousWeek();
                        break;
                    case monthly:
                        GoToPreviousMonth();
                        break;
                }
            }
        });

        ConfigureDayPanel();
        ConfigureWeekPanel();
        ConfigureMonthPanel();
        ConfigureFilterPanel();

        /**call on daily panel mode click, need to assign other view mode before it can be triggered properly **/
        promotionViewMode = Enum.PromotionViewMode.monthly;
        findViewById(R.id.tvDay).callOnClick();


        /**show filter at 1st and call on hide on it later, this will allow proper positioning later**/
        //blnShowingFilterPopup=true;
        //HideRightSideFilterBar();
        //set as default selected panel on startup display
        selectedCalendarDisplayPanel =(LinearLayout) findViewById(R.id.llDayPanel);
        //DrawTimeLine();

    }
    private void ShowAddPromotionPanel()
    {
        if(blnPromotionDetailDialogShow)return;
        blnPromotionDetailDialogShow = true;
        AddPromotionDialog dialog = new AddPromotionDialog(this,this);
        dialog.show();
    }
    private void GoToNextWeek()
    {
        common.Utility.LogActivity("go to next week");
        Calendar c=new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        c.add(Calendar.DATE,7);
        GoToNewWeek(c);
    }
    private void GoToPreviousWeek()
    {
        common.Utility.LogActivity("go to previous week");
        Calendar c=new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        c.add(Calendar.DATE,-7);
        GoToNewWeek(c);
    }
    private void GoToNewWeek(Calendar c) {
        if(blnShowingFilterPopup)HideRightSideFilterBar();
        ShowNewWeek(c, true);
    }
    private void GoToNextMonth()
    {
        common.Utility.LogActivity("go to next month");
        Calendar c=new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        c.add(Calendar.MONTH,1);
        GoToNewMonth(c);
    }
    private void GoToPreviousMonth()
    {
        common.Utility.LogActivity("go to previous month");
        Calendar c=new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        c.add(Calendar.MONTH, -1);
        GoToNewMonth(c);
    }
    private void GoToNewMonth(Calendar c)
    {

        //TextView tv =ReturnCurrentDateTextLabel();
        tvDateDisplayLabel.setTag(c.getTimeInMillis());
        ShowNewMonth(c, true);
    }
    private void GoToNextDay()
    {
        common.Utility.LogActivity("go to next day");
        GotoNewDate(1);

    }

    private void GoToPreviousDay()
    {
        common.Utility.LogActivity("go to previous day");
        GotoNewDate(-1);
    }
    private void GotoNewDate(int dayOffset)
    {
        if(blnShowingFilterPopup)HideRightSideFilterBar();

        dialogFor = Enum.CallDateDialogFrom.label;
        Calendar cal = new GregorianCalendar();
        //TextView tv =ReturnCurrentDateTextLabel();
        cal.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        cal.add(Calendar.DATE, dayOffset);
        tvDateDisplayLabel.setTag(cal.getTimeInMillis());
        ShowNewDay(cal, true);

    }
    private void ConfigureMonthPanel()
    {
        TextView tvMonth =(TextView) this.findViewById(R.id.tvMonth);
        SetTextSizeAndTypeFace(tvMonth, common.text_and_length_settings.PROMOTION_FRAGMENT_VIEW_MODE_TITLE_TEXT_SIZE);
        CreateClickEffectForRoundedBoarder(tvMonth, Enum.PromotionViewMode.monthly);

        tblMonthlyView = (TableLayout)findViewById(R.id.tblMonthlyView);
        tblMonthlyCalendarHeader = (TableLayout)findViewById(R.id.tblMonthlyCalendarHeader);
        rlMonthlyUIPanel = (RelativeLayout)findViewById(R.id.rlMonthlyUIPanel);
        svMonthlyPanel =(MyScrollView) findViewById(R.id.svMonthlyPanel);
        svMonthlyPanel.SetProperties(this);

        final GestureDetector myGestureDetector = new GestureDetector(this,new MyMonthlyCalendarGestureListener((ScrollView)svMonthlyPanel));

        rlMonthlyUIPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //common.Utility.ShowMessage("Main scroll view","touch",PromotionUIActivity.this,R.drawable.message);
                if (blnShowingFilterPopup) HideRightSideFilterBar();
                //myGestureDetector.onTouchEvent(event);

                //destroy any active dialog
                if (!blnShowingPromotionDetailPopup && llActiveDialog != null)
                {    ClosePromotionActiveDialog();}

                SetTouchedLocation(event.getX(), event.getY());


                return true;
            }
        });

        DrawMonthlyPanelTimeTable();
    }
    private void DrawMonthlyPanelTimeTable()
    {
        tblMonthlyCalendarHeader.addView(CreateMonthlyTableHeaderRow());

        for(int i=0;i<6;i++) {
            tblMonthlyView.addView(CreateMonthlyDayRow());
        }
    }
    private TableRow CreateMonthlyTableHeaderRow()
    {
        int PROMOTION_EVENT_WIDTH=common.Utility.DP2Pixel(DAILY_TIME_RAW_PROMOTION_EVENT_WIDTH,PromotionUIActivity.this);

        int DATE_BAR_WIDTH=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_WIDTH,PromotionUIActivity.this);
        int DATE_BAR_HEIGHT=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_HEIGHT,PromotionUIActivity.this);

        TableRow tr = new TableRow(this);
        tr.setPadding(5, 0, 5, 0);
        TableLayout.LayoutParams lllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(lllp);

        /**header row**/
      /*  TextView tvDummyTime = new TextView(this);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(PROMOTION_EVENT_WIDTH, DATE_BAR_HEIGHT);
        tvDummyTime.setLayoutParams(trlp);
        tr.addView(tvDummyTime);*/

        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        c.set(Calendar.DAY_OF_WEEK,c.getFirstDayOfWeek());

        for(int i=0;i<7;i++) {
            TextView tv = new TextView(this);
            tv.setText(GetDayOfWeek(c.get(Calendar.DAY_OF_WEEK)));
            tv.setGravity(Gravity.CENTER);
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(DATE_BAR_WIDTH, DATE_BAR_HEIGHT);
            if(i==0)
            {
                trlp.leftMargin=PROMOTION_EVENT_WIDTH;
            }
            tr.addView(tv, trlp);
            c.add(Calendar.DATE,1);
        }




        return tr;
    }
    private void ConfigureWeekPanel()
    {
        TextView tvWeek =(TextView) this.findViewById(R.id.tvWeek);
        SetTextSizeAndTypeFace(tvWeek, common.text_and_length_settings.PROMOTION_FRAGMENT_VIEW_MODE_TITLE_TEXT_SIZE);
        tblWeeklyView = (TableLayout)findViewById(R.id.tblWeeklyView);
        tblWeeklyCalendarHeader = (TableLayout)findViewById(R.id.tblWeeklyCalendarHeader);
        rlWeeklyUIPanel = (RelativeLayout)findViewById(R.id.rlWeeklyUIPanel);
        CreateClickEffectForRoundedBoarder(tvWeek, Enum.PromotionViewMode.weekly);
        final GestureDetector myGestureDetector = new GestureDetector(this,new MyWeeklyCalendarGestureListener());
        rlWeeklyUIPanel.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                if (blnShowingFilterPopup) HideRightSideFilterBar();
                //myGestureDetector.onTouchEvent(event);
                Runnable runnable = null;
                //destroy any active dialog
                if (!blnShowingPromotionDetailPopup && llActiveDialog != null)
                    ClosePromotionActiveDialog();
                SetTouchedLocation(event.getX(), event.getY());
                int action = MotionEventCompat.getActionMasked(event);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        blnFingerDown = true;
                        if (runnable != null) {
                            handler.removeCallbacks(runnable);
                            runnable = null;
                        }
                        runnable = new Runnable() {
                            @Override
                            public void run() {

                                if (!blnFingerDown && !blnShowingPromotionDetailPopup) {
                                    blnShowingPromotionDetailPopup = true;
                                    ShowPromotionSummaryPopup(PromotionUIHitTest(touched_X, touched_Y, (MyScrollView) findViewById(R.id.svWeeklyPanel)));

                                }
                            }
                        };
                        handler.postDelayed(runnable, 100);
                        break;

                    case MotionEvent.ACTION_MOVE:

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        blnFingerDown = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        blnFingerDown = false;
                        break;
                }

                return true;
            }
        });


        svWeeklyPanel =(MyScrollView) findViewById(R.id.svWeeklyPanel);
        svWeeklyPanel.SetProperties(this);


        DrawWeeklyPanelTimeTable();

    }
    private void DrawWeeklyPanelTimeTable()
    {
        //create the top header row
        tblWeeklyCalendarHeader.addView(CreateWeekTableHeaderRow());
        //tblWeeklyView.addView(, 0);

        for(int i=0;i<24;i++) {
            if(i>10) {
                if(i==12) {
                    tblWeeklyView.addView(CreateWeekHourRow("12:00 PM", i));
                }
                else if(i>=12) {
                    tblWeeklyView.addView(CreateWeekHourRow(i - 12 + ":00 PM", i));
                } else {
                    tblWeeklyView.addView(CreateWeekHourRow(i + ":00 AM", i));
                }
            } else {
                tblWeeklyView.addView(CreateWeekHourRow("0" + i + ":00 AM", i));
            }
        }


    }
    private void DrawDailyPanelTimeTable()
    {


        for(int i=0;i<24;i++) {
            if(i>10) {
                if(i==12)
                {
                    tblDayView.addView(CreateDayViewHourRow("12:00 PM", i));
                }
                else if(i>=12)
                {
                    tblDayView.addView(CreateDayViewHourRow(i - 12 + ":00 PM", i));
                }
                else {
                    tblDayView.addView(CreateDayViewHourRow(i + ":00 AM", i));
                }
            }
            else {
                tblDayView.addView(CreateDayViewHourRow("0" + i + ":00 AM", i));
            }
        }
    }
    private void ShowDeleteOptionConfirmationDialog(final PromotionObject po)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Options");
        alert.setMessage("Would you like to set the promotion to expire after today or remove it from calendar?");
        alert.setPositiveButton("Mark Expire", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                common.Utility.LogActivity("set promotion " +
                        po.GetId() + " to expire after " +
                        new SimpleDateFormat("MM/dd/yyyy").format(new Date(Calendar.getInstance().getTimeInMillis())));
                common.myPromotionManager.SetExpire(po.GetId());
                Redraw(po);
            }
        });
        alert.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                common.Utility.LogActivity("delete the promotion " + po.GetId());
                if (common.myPromotionManager.RemovePromotion(po.GetId()) > 0) {
                    RemovePromotionFromUIAndCache(po.GetId());
                    Redraw(po);
                }
            }
        });
        alert.show();
    }
    public void ClearPromotionEventUI()
    {

        for(int i=promotionEventUI.size()-1;i>-1;i--) {

            ((RelativeLayout) promotionEventUI.get(i).getParent()).removeView(promotionEventUI.get(i));

        }

        promotionEventUI.clear();
        rlDailyUIPanel.setTag(null);
        rlMonthlyUIPanel.setTag(null);
        rlWeeklyUIPanel.setTag(null);
    }
    public void RemovePromotionFromUIAndCache(long id)
    {
        for(int i=promotionEventUI.size()-1;i>-1;i--)
        {
            if((Long)promotionEventUI.get(i).getTag()==id)
            {
                common.Utility.LogActivity("remove promotion view id "+id);
                ((RelativeLayout)promotionEventUI.get(i).getParent()).removeView(promotionEventUI.get(i));
                promotionEventUI.remove(i);
                //break; do not break it could be more than once in weekly and monthly
            }
        }

    }
    private void ResizeMonthlySummaryPopupDialog()
    {
        if(llActiveDialog==null)return;
        //TextView tv = (TextView) llActiveDialog.getChildAt(0);
        //tv.setText("1");
        int height =llActiveDialog.getChildAt(0).getHeight();
        ScrollView sv = (ScrollView)llActiveDialog.getChildAt(1);
        height+=sv.getChildAt(0).getHeight();
        //if(height<llActiveDialog.getHeight())
        //{
        RelativeLayout.LayoutParams rllp = (RelativeLayout.LayoutParams)llActiveDialog.getLayoutParams();
            rllp.height = height+common.Utility.DP2Pixel(10,this);
        llActiveDialog.setLayoutParams(rllp);
        llActiveDialog.invalidate();


        //}
    }
    private void CalculatePopupDialogPosition()
    {
        //get visible panel section

        MyScrollView myScrollView;
        if(promotionViewMode== Enum.PromotionViewMode.daily) {
            myScrollView = (MyScrollView) findViewById(R.id.svDailyPanel);

        }
        else if(promotionViewMode== Enum.PromotionViewMode.weekly)
        {
            myScrollView = (MyScrollView) findViewById(R.id.svWeeklyPanel);
        }
        else
        {
            myScrollView = (MyScrollView) findViewById(R.id.svMonthlyPanel);
        }
        Rect r = new Rect();
        myScrollView.getDrawingRect(r);


        touched_X = llActiveDialog.getWidth()+touched_X>r.right?r.right-llActiveDialog.getWidth():touched_X;
        touched_Y=llActiveDialog.getHeight()+touched_Y>r.bottom?r.bottom-llActiveDialog.getHeight():touched_Y;
        llActiveDialog.setX(touched_X);
        llActiveDialog.setY(touched_Y);


        //llActiveDialog.SetTouchPosition(touched_X,touched_Y);
        llActiveDialog.SetSize(llActiveDialog.getHeight(),llActiveDialog.getWidth(),50,50,false);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                llActiveDialog.setVisibility(View.VISIBLE);
                llActiveDialog.AnimationShow();

            }
        });



        blnShowingPromotionDetailPopup=false;


    }
    private void ShowProgressDialog()
    {
        new Handler().post(new Runnable() {
            @Override
            public void run() {

                if(pd==null)pd = new ProgressDialog(PromotionUIActivity.this);
                pd.setMessage("Loading...");
                pd.setCancelable(false);
                pd.isIndeterminate();
                pd.show();

            }
        });
    }
    private void MonthlyViewCalendarLoadCompleted()
    {

        if(pd !=null && pd.isShowing()){pd.dismiss();}
    }
    private void ShowPromotionDetail(PromotionObject po)
    {
        if(blnPromotionDetailDialogShow)return;
        blnPromotionDetailDialogShow=true;
        AddPromotionDialog dialog = new AddPromotionDialog(this,this,po);
        dialog.show();
    }


    public void ShowPromotionSummariesPopupInMonthlyMode(final View view)
    {
        int dialogWidth = common.Utility.DP2Pixel(300, this);
        int scrollviewHeight = common.Utility.DP2Pixel(410,this);
        LinearLayout promotionContainer;
        //construct a parent container for all the promotion details
        //LinearLayout llParent = new LinearLayout(this);
        PromotionSummaryPopup llParent = new PromotionSummaryPopup(this);
        llParent.IsPromotionPopupInMonthlyView(true);
        llParent.setBackgroundColor(Color.WHITE);
        llParent.setBackground(getResources().getDrawable(R.drawable.draw_black_line_border));
        llParent.setOrientation(LinearLayout.VERTICAL);
        llParent.setPadding(5,5,5,5);
        //get select calendar date
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        TextView tvDate = new TextView(this);
        tvDate.setText(sdf.format(new Date((Long)view.getTag())).toString());
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimensionPixelSize(R.dimen.dp_promotion_summary_popup_title_text_size));
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View local_view) {
                tvDateDisplayLabel.setTag((Long)view.getTag());
                findViewById(R.id.tvDay).callOnClick();
            }
        });
        common.Utility.LogActivity("show monthly promotions summary popup on date " + tvDate.getText());

        Calendar cSelected = new GregorianCalendar();
        cSelected.setTimeInMillis((Long)view.getTag());
        if(cSelected.get(Calendar.MONTH)== Calendar.getInstance().get(Calendar.MONTH)
                && cSelected.get(Calendar.DATE)== Calendar.getInstance().get(Calendar.DATE)
                && cSelected.get(Calendar.YEAR)== Calendar.getInstance().get(Calendar.YEAR))
        {
            tvDate.setTextColor(getResources().getColor(R.color.lost_shine_green));
        }
        llParent.addView(tvDate);

        //add a vertical scroll view
        final ScrollView sv = new ScrollView(this);
        //final GestureDetector myGestureDetector = new GestureDetector(this,new MyMonthlySummaryPopupGestureListener(sv,tvDate));
        sv.setOnTouchListener(new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    // Allow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            // Handle ListView touch events.
            v.onTouchEvent(event);
            return true;
        }
    });



        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(dialogWidth,scrollviewHeight);
        llParent.addView(sv,lllp);
        TableLayout tbl = new TableLayout(this);
        tbl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        ScrollView.LayoutParams svlp = new FrameLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        sv.addView(tbl,svlp);
        if(view instanceof  ScrollView) {
            promotionContainer = (LinearLayout) ((ScrollView) view).getChildAt(0);
        }
        else
        {
            promotionContainer =(LinearLayout) view;
        }

        for(int i=0;i<promotionContainer.getChildCount();i++)
        {
            //if(i%2==0)
            //{
            TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                tbl.addView(new TableRow(this),tllp);
            //}
            LinearLayout llDetails =CreatePromotionSummaryUI(common.myPromotionManager.Get ((Long)promotionContainer.getChildAt(i).getTag()),true);
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(dialogWidth-10, ViewGroup.LayoutParams.WRAP_CONTENT);
            trlp.bottomMargin=3;
            ((TableRow)tbl.getChildAt(tbl.getChildCount()-1)).addView(llDetails,trlp);

        }


        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        llParent.setY(touched_Y);
        llParent.setX(touched_X);

        rlMonthlyUIPanel.addView(llParent,rllp);

        llActiveDialog = llParent;
        llActiveDialog.setVisibility(View.INVISIBLE);

        //reset
        runPromotionSummaryPopup = null;


        //setup threads to
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CalculatePopupDialogPosition();
            }
        }, 0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ResizeMonthlySummaryPopupDialog();
            }
        }, 0);

    }

    private PromotionSummaryPopup CreatePromotionSummaryUI(final PromotionObject po,boolean blnShowContent)
    {
        int imageWidth = common.Utility.DP2Pixel(35,this);
        int imageHeight = common.Utility.DP2Pixel(35,this);
        int Margin_50 = common.Utility.DP2Pixel(50,this);
        int Margin_10 = common.Utility.DP2Pixel(10,this);
        int labelTextSize = getResources().getDimensionPixelSize(R.dimen.dp_promotion_summary_popup_text_size);
        int titleTextSize = getResources().getDimensionPixelSize(R.dimen.dp_promotion_summary_popup_title_text_size);

        PromotionSummaryPopup llDetail = new PromotionSummaryPopup(PromotionUIActivity.this);
        llDetail.setBackgroundColor(Color.WHITE);

        llDetail.setOrientation(LinearLayout.VERTICAL);
        llDetail.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
        //llDetail.setBackground(getResources().getDrawable(R.drawable.border_with_shadow_png));
        llDetail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        //Edit options
        RelativeLayout rlOptions = new RelativeLayout(this);
        rlOptions.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        ImageView imgEdit = new ImageView(this);
        imgEdit.setBackground(getResources().getDrawable(R.drawable.green_border_edit));
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.Utility.LogActivity("update option popup clicked");
                ShowPromotionDetail(po);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        ClosePromotionActiveDialog();
                    }
                }, 200);

            }
        });
        RelativeLayout.LayoutParams lpImg1 = new RelativeLayout.LayoutParams(imageWidth,imageHeight);
        lpImg1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        lpImg1.setMargins(0, Margin_10, Margin_50, Margin_10);
        rlOptions.addView(imgEdit, lpImg1);
        //Delete option
        ImageView imgDel = new ImageView(this);
        imgDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                common.Utility.LogActivity("delete option popup clicked");

                ShowDeleteOptionConfirmationDialog(po);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ClosePromotionActiveDialog();
                    }
                }, 200);

            }
        });
        imgDel.setBackground(getResources().getDrawable(R.drawable.green_border_delete));
        RelativeLayout.LayoutParams lpImg2 = new RelativeLayout.LayoutParams(imageWidth,imageHeight);
        lpImg2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        lpImg2.setMargins(0, Margin_10, Margin_10, Margin_10);
        rlOptions.addView(imgDel, lpImg2);

        LinearLayout.LayoutParams lllp =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llDetail.addView(rlOptions, lllp);

        //title
        TextView tv = new TextView(PromotionUIActivity.this);
        tv.setText(po.GetTitle());
        tv.setBackgroundColor(getResources().getColor(po.GetDiscountColor().value));
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, titleTextSize);
        tv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        LinearLayout.LayoutParams lpTitle = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setGravity(Gravity.CENTER);
        llDetail.setPadding(2,0,2,0);
        //llDetail.setPadding(Margin_50,0,Margin_10,0);
        llDetail.addView(tv, lpTitle);
        //TIME
        TextView tvTime = new TextView(this);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, labelTextSize);
        tvTime.setText(Html.fromHtml("Time " + po.GetStartTimeString() + " - " + po.GetEndTimeString()));
        LinearLayout.LayoutParams lpTime = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvTime.setGravity(Gravity.CENTER);
        tvTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        llDetail.addView(tvTime, lpTime);
        //date
        TextView tvDate = new TextView(this);
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, labelTextSize);
        tvDate.setText("Date: " + po.GetStartDateString() + " - " + po.GetEndDateString());
        LinearLayout.LayoutParams lpDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvDate.setGravity(Gravity.CENTER);
        tvDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        llDetail.addView(tvDate, lpDate);

        //occurrence
        TextView tvOccurrence = new TextView(this);
        tvOccurrence.setTextSize(TypedValue.COMPLEX_UNIT_DIP, labelTextSize);
        tvOccurrence.setText("Occurrence: " + po.GetDateConditionString());
        LinearLayout.LayoutParams lpOccurrence = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvOccurrence.setGravity(Gravity.CENTER);
        tvOccurrence.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        llDetail.addView(tvOccurrence, lpOccurrence);

        //expiry
        TextView tvExpiry = new TextView(this);
        tvExpiry.setTextSize(TypedValue.COMPLEX_UNIT_DIP, labelTextSize);
        tvExpiry.setText("Ends: " + po.GetExpirationDateString());
        LinearLayout.LayoutParams lpExpiry = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvExpiry.setGravity(Gravity.CENTER);
        tvExpiry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        llDetail.addView(tvExpiry, lpExpiry);

        //Rule
        TextView tvRule = new TextView(this);
        tvRule.setTextSize(TypedValue.COMPLEX_UNIT_DIP, labelTextSize);
        tvRule.setText("Rule: " + po.GetRuleString());
        LinearLayout.LayoutParams lpRule = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvRule.setGravity(Gravity.CENTER);
        tvRule.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        llDetail.addView(tvRule, lpRule);

        //discount value
        TextView tvDiscount = new TextView(this);
        tvDiscount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, labelTextSize);
        tvDiscount.setText("Discount: " + po.GetDiscountString());
        LinearLayout.LayoutParams lpDiscount = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvDiscount.setGravity(Gravity.CENTER);
        tvDiscount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        lpDiscount.setMargins(0, 0, 0, Margin_10);
        llDetail.addView(tvDiscount, lpDiscount);

        if(!blnShowContent) {
            llDetail.setBackgroundColor(Color.TRANSPARENT);
            rlOptions.setVisibility(View.INVISIBLE);
            tv.setVisibility(View.INVISIBLE);
            tv.setTag(true);
            tvTime.setVisibility(View.INVISIBLE);
            tvDate.setVisibility(View.INVISIBLE);
            tvOccurrence.setVisibility(View.INVISIBLE);
            tvExpiry.setVisibility(View.INVISIBLE);
            tvRule.setVisibility(View.INVISIBLE);
            tvDiscount.setVisibility(View.INVISIBLE);
        }
        return llDetail;
    }

    private void ShowPromotionSummaryPopup(View PromotionUI)
    {
        if(PromotionUI==null){blnShowingPromotionDetailPopup=false; return;}


        int dialogWidth = common.Utility.DP2Pixel(300, this);
        final PromotionObject po = common.myPromotionManager.Get((Long) PromotionUI.getTag());
        if(po==null)
        {
            common.Utility.LogActivity("show promotion but promotion object is null");
        }
        else {
            common.Utility.LogActivity("show promotion " + (Long) PromotionUI.getTag() + " summary popup");
        }
        PromotionSummaryPopup llDetail = CreatePromotionSummaryUI(po,false);

        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

        llDetail.setY(touched_Y);
        llDetail.setX(touched_X);

        if(promotionViewMode== Enum.PromotionViewMode.daily)
        {
            rlDailyUIPanel.addView(llDetail, rllp);
        }
        else if(promotionViewMode== Enum.PromotionViewMode.weekly)
        {
            rlWeeklyUIPanel.addView(llDetail, rllp);
        }
        else
        {
            rlMonthlyUIPanel.addView(llDetail,rllp);
        }
        llActiveDialog = llDetail;
        llActiveDialog.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CalculatePopupDialogPosition();
            }
        }, 0);


    }
    private void ClosePromotionActiveDialog()
    {
        if(llActiveDialog != null) {
            common.Utility.LogActivity("remove promotion summary popup");
            last_scroll_Y=-1;
            ((RelativeLayout)llActiveDialog.getParent()).removeView(llActiveDialog);
            //rlDailyUIPanel.removeView(llActiveDialog);
            llActiveDialog = null;
        }
    }

    private void ConfigureDayPanel() {
        TextView tvDay = (TextView) this.findViewById(R.id.tvDay);
        SetTextSizeAndTypeFace(tvDay, common.text_and_length_settings.PROMOTION_FRAGMENT_VIEW_MODE_TITLE_TEXT_SIZE);
        CreateClickEffectForRoundedBoarder(tvDay, Enum.PromotionViewMode.daily);
        tblDayView = (TableLayout) findViewById(R.id.tblDayView);
        rlDailyUIPanel = (RelativeLayout) findViewById(R.id.rlDailyUIPanel);

        //final GestureDetector myGestureDetector = new GestureDetector(this,new MyDailyCalendarGestureListener());
        rlDailyUIPanel.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                if (blnShowingFilterPopup) HideRightSideFilterBar();
                //myGestureDetector.onTouchEvent(event);
                Runnable runnable = null;
                //destroy any active dialog
                if (!blnShowingPromotionDetailPopup && llActiveDialog != null)
                    ClosePromotionActiveDialog();

                SetTouchedLocation(event.getX(), event.getY());
                int action = MotionEventCompat.getActionMasked(event);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        blnFingerDown = true;
                        if (runnable != null) {
                            handler.removeCallbacks(runnable);
                            runnable = null;
                        }
                        runnable = new Runnable() {
                            @Override
                            public void run() {

                                if (!blnFingerDown && !blnShowingPromotionDetailPopup) {
                                    blnShowingPromotionDetailPopup = true;
                                    ShowPromotionSummaryPopup(PromotionUIHitTest(touched_X, touched_Y, (MyScrollView) findViewById(R.id.svDailyPanel)));

                                }
                            }
                        };
                        handler.postDelayed(runnable, 100);
                        break;

                    case MotionEvent.ACTION_MOVE:

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        blnFingerDown = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        blnFingerDown = false;
                        break;
                }

                return true;
            }
        });

        svDailyPanel =(MyScrollView) findViewById(R.id.svDailyPanel);
        svDailyPanel.SetProperties(this);

        dayViewPromotionBar = new LinearLayout[24];
        DrawDailyPanelTimeTable();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                svDailyPanel.smoothScrollBy(0, GetYAxisByGivenTimeInDailyView(8,0));
            }
        }, 1000);



    }
   /* private int GetYAxisByGivenTimeInWeeklyView(int hr,int minute)
    {
        int DATE_BAR_HEIGHT=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_HEIGHT,PromotionUIActivity.this);
        return DATE_BAR_HEIGHT+GetYAxisByGivenTimeInDailyView(hr,minute);
    }*/
    private int GetYAxisByGivenTimeInDailyView(int hr,int minute)
    {
        //calculate Y-axis to add
        int PROMOTION_EVENT_HEIGHT=common.Utility.DP2Pixel(DAILY_AND_WEEKLY_RAW_PROMOTION_EVENT_HEIGHT, this);
        int minuteY = Math.round(PROMOTION_EVENT_HEIGHT * (Float.parseFloat(minute + "") / 60f));
        final int setY=hr * PROMOTION_EVENT_HEIGHT + minuteY;
        return setY;
    }
    private void SetTextSizeAndTypeFace(TextView tv,float textSize) {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
    }


    private boolean SubjectTitleClick(View v)
    {

        if(v==tvSelectedCalendarMode)return false;
        v.setBackground(getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));

        if (tvSelectedCalendarMode != null) {

            tvSelectedCalendarMode.setTextColor(getResources().getColor(R.color.divider_grey));
            tvSelectedCalendarMode.setBackground(null);
        }
        tvSelectedCalendarMode = (TextView) v;

        tvSelectedCalendarMode.setTextColor(getResources().getColor(R.color.green));
        return true;
    }

    private void CreateClickEffectForRoundedBoarder(View v,final Enum.PromotionViewMode pvm)
    {

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View innerView) {
                if (blnShowingFilterPopup) HideRightSideFilterBar();
                if (promotionViewMode == pvm) return;
                promotionViewMode = pvm;
                if (SubjectTitleClick(innerView)) {
                    ClosePromotionActiveDialog();
                    /**changing date label display
                     daily=day in milli
                     weekly = day in milli, and query start  day of week
                     month = day in milli, and query start day of month
                     **/
                    Calendar c = new GregorianCalendar();
                    c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
                    ClearCalendarPanelDrawing();
                    switch (pvm) {
                        case monthly:
                            common.Utility.LogActivity("month calendar clicked");
                            ShowNewMonth(c, false);
                            break;
                        case weekly:
                            common.Utility.LogActivity("week calendar clicked");
                            ShowNewWeek(c, false);
                            break;
                        case daily:
                            common.Utility.LogActivity("day calendar clicked");
                            ShowNewDay(c, false);
                            break;
                        default:


                    }
                    //PreparePanel(false);
                }
            }
        });

    }


    public void Redraw(PromotionObject po)
    {


        //check the new save is affecting current display
        Calendar cToday = new GregorianCalendar();
        cToday.setTimeInMillis((Long) tvDateDisplayLabel.getTag());//default 59 seconds when set tag
        /*int intDate =cToday.get(Calendar.DATE);
        int intMonth = cToday.get(Calendar.MONTH) + 1;
        int intYear = cToday.get(Calendar.YEAR);
        //trigger promotion manager to reload current month data
        common.myPromotionManager.ClearCache();
        if(promotionViewMode== Enum.PromotionViewMode.monthly) {

            common.myPromotionManager.GetPromotionByMonth(intMonth, intYear,TOTAL_DATE_SLOT_IN_CALENDAR_VIEW);
        }
        else
        {
            common.myPromotionManager.GetPromotionWeekByDate_WeekDayAsKey(intDate,intMonth,intYear);
        }*/

        Calendar cStart = new GregorianCalendar();
        cStart.setTimeInMillis(po.GetStartDateTime());
        cStart.set(Calendar.HOUR_OF_DAY, 0);
        cStart.set(Calendar.MINUTE, 0);
        cStart.set(Calendar.SECOND, 0);
        cStart.set(Calendar.MILLISECOND, 0);


        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(po.GetEndDateTime());
        cEnd.set(Calendar.HOUR_OF_DAY, 23);
        cEnd.set(Calendar.MINUTE,59);
        cEnd.set(Calendar.SECOND,59);
        cEnd.set(Calendar.MILLISECOND, 999);


        common.Utility.LogActivity("Redraw mode is "+promotionViewMode.name()+ " promotion object "+po.GetId());
        switch (promotionViewMode) {
            case monthly:

                DrawMonthlyData(GetMonthlyPromotion(),new ArrayList<Long>(GetDistinctMonthlyId()));

                ShowProgressDialog();
                ScheduleResizeCalendarDaySlotScrollViewThread();
                break;
            case weekly:
                Calendar cWeekStart = new GregorianCalendar();
                cToday = common.Utility.GetFirstDayOfWeekDate(cToday);
                //cToday.set(Calendar.DAY_OF_WEEK, cToday.getFirstDayOfWeek());
                cWeekStart.setTimeInMillis(cToday.getTimeInMillis());
                cWeekStart.set(Calendar.HOUR_OF_DAY, 0);
                cWeekStart.set(Calendar.MINUTE, 0);
                cWeekStart.set(Calendar.SECOND, 0);
                cWeekStart.set(Calendar.MILLISECOND, 0);
                Calendar cWeekEnd = new GregorianCalendar();
                cWeekEnd.setTimeInMillis(cWeekStart.getTimeInMillis());
                cWeekEnd.add(Calendar.DATE, 6);
                cWeekEnd.set(Calendar.HOUR_OF_DAY, 23);
                cWeekEnd.set(Calendar.MINUTE, 59);
                cWeekEnd.set(Calendar.SECOND, 59);
                cWeekEnd.set(Calendar.MILLISECOND, 999);
                if(cWeekStart.getTimeInMillis()<=cStart.getTimeInMillis() && cStart.getTimeInMillis()<=cEnd.getTimeInMillis()
                        || IsAffectedPromotionObjectOnCurrentDisplay(po.GetId()))
                {
                    HashMap<Integer,HashMap<Integer,ArrayList<Long>>> data =GetWeeklyModeAllPromotionHM();
                    DrawWeeklyData(data,new ArrayList<Long>(GetDistinctWeekPromotionIds(data)));
                }
                //ShowWeekPanel();
                break;
            case daily:
                if((cStart.getTimeInMillis()<=cToday.getTimeInMillis() && cEnd.getTimeInMillis()>=cToday.getTimeInMillis())
                        || IsAffectedPromotionObjectOnCurrentDisplay(po.GetId()))
                {
                    //redraw if the new promotion fall into current date
                    DrawDailyData(GetDailyModeAllPromotionList());

                }
                break;
            default:
                break;
        }
    }
    private boolean IsAffectedPromotionObjectOnCurrentDisplay(long id)
    {
        boolean blnFalg=false;

        for(int i=0;i<onDisplayDailyPromotionIds.size();i++)//use GUI list but not getting a new completed list, because deleted PO already removed in there
        {
            if(id==onDisplayDailyPromotionIds.get(i))
            {
                return true;
            }
        }
        return blnFalg;
    }
   /* private TextView ReturnCurrentDateTextLabel()
    {
        TextView tv=null;
        if(dialogFor== Enum.CallDateDialogFrom.label)
        {
            tv = tvDateDisplayLabel;
        }

        return tv;
    }*/
    private DatePickerDialog createDialogWithoutDateField() {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long)tvDateDisplayLabel.getTag());
        DatePickerDialog dpd = new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), 24);
        dpd.setTitle("Please select month and year");
        try {
            java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                    datePicker.setCalendarViewShown(false);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {

                        if ("mDaySpinner".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
        }
        return dpd;
    }

    protected void ShowDatePickerDialog()
    {
        blnDateDialogShow = true;
        //TextView tv=ReturnCurrentDateTextLabel();//tvFromDate;
        final Calendar c=Calendar.getInstance();


        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        if(promotionViewMode!= Enum.PromotionViewMode.monthly) {
            new DatePickerDialog(this, this, year, month, day).show();
        }
        else
        {
            createDialogWithoutDateField().show();
            //DatePickerDialog dialog = new DatePickerDialog(this,this,year,month,day);
           /* ViewGroup group = (ViewGroup)dialog.getWindow().getDecorView();
            DatePicker datePicker  = findDatePicker(group);
            if (datePicker != null) {
               datePicker.findViewById(getResources().getSystem().getIdentifier("year", "id", "android")).setVisibility(View.GONE);//.getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
            }

            dialog.show();*/
        }
    }

    private void SetCurrentDateForDateInput(TextView tvDate,Enum.CallDateDialogFrom callDateDialogFrom)
    {
        final Calendar c = Calendar.getInstance();
        Date date = new Date(c.getTimeInMillis());
        tvDate.setTag(date.getTime());
        if(callDateDialogFrom== Enum.CallDateDialogFrom.label)
        {
            tvDate.setText(Html.fromHtml("<u>"
                    +new SimpleDateFormat("EEEE, MMMM dd, yyyy").format(date).toString()
                    +"</u>"));
        }

    }
    private TableRow CreateWeekTableHeaderRow()
    {
        int PROMOTION_EVENT_WIDTH=common.Utility.DP2Pixel(DAILY_TIME_RAW_PROMOTION_EVENT_WIDTH,PromotionUIActivity.this);
        //int PROMOTION_EVENT_HEIGHT=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_HEIGHT,PromotionUIActivity.this);
        int DATE_BAR_WIDTH=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_WIDTH,PromotionUIActivity.this);
        int DATE_BAR_HEIGHT=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_HEIGHT,PromotionUIActivity.this);

        TableRow tr = new TableRow(this);
        tr.setPadding(5, 0, 5, 0);
        TableLayout.LayoutParams lllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(lllp);

        /**header row**/
        TextView tvDummyTime = new TextView(this);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(PROMOTION_EVENT_WIDTH, DATE_BAR_HEIGHT);
        tvDummyTime.setLayoutParams(trlp);
        tr.addView(tvDummyTime);


        for(int i=0;i<7;i++) {
            TextView tv = new TextView(this);
            tv.setText(i+"");
            tv.setGravity(Gravity.CENTER);
            trlp = new TableRow.LayoutParams(DATE_BAR_WIDTH, DATE_BAR_HEIGHT);

            tr.addView(tv, trlp);
            //tv.setBackground(getResources().getDrawable(R.drawable.draw_border));
            /*if(i==0)
                tv.setBackground(getResources().getDrawable(R.drawable.draw_border_left_top_right));
            else
                tv.setBackground(getResources().getDrawable(R.drawable.draw_border_top_right));*/
        }




        return tr;
    }
    private void DrawTimeLine()
    {
        //cleanup any runnable in the queue
        if(timerBarHandler!=null)timerBarHandler.removeCallbacksAndMessages(null);


        if(llCurrentTimeBar!=null) {
            if(llCurrentTimeBar.getParent()!=null) {
                ((RelativeLayout) llCurrentTimeBar.getParent()).removeView(llCurrentTimeBar);
            }
            llCurrentTimeBar = null;
        }

        //int PROMOTION_EVENT_HEIGHT=common.Utility.DP2Pixel(DAILY_AND_WEEKLY_RAW_PROMOTION_EVENT_HEIGHT, this);
        //draw if is today is on display only, and is one of the day in current week, for weekly view
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis((Long) tvDateDisplayLabel.getTag());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        if(promotionViewMode== Enum.PromotionViewMode.daily
                && (Calendar.getInstance().get(Calendar.MONTH)!=c.get(Calendar.MONTH)
                || Calendar.getInstance().get(Calendar.DATE) !=c.get(Calendar.DATE)
                || Calendar.getInstance().get(Calendar.YEAR) !=c.get(Calendar.YEAR))) return;

        //reset it to 1st day of week
        //c.add(Calendar.DATE,c.getFirstDayOfWeek()-c.get(Calendar.DAY_OF_WEEK));//currently pointing to today's date even if is weekly view
        c.set(Calendar.DAY_OF_WEEK,c.getFirstDayOfWeek());
        Date d = new Date(c.getTimeInMillis());
        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(c.getTimeInMillis());
        cEnd.add(Calendar.DATE, 6);
        cEnd.set(Calendar.HOUR_OF_DAY, 23);
        cEnd.set(Calendar.MINUTE, 59);
        cEnd.set(Calendar.SECOND,59);
        cEnd.set(Calendar.MILLISECOND,9999);

        if(promotionViewMode== Enum.PromotionViewMode.weekly
                && !(c.getTimeInMillis()<=Calendar.getInstance().getTimeInMillis()
                && Calendar.getInstance().getTimeInMillis()<=cEnd.getTimeInMillis())) return;

        //start constructing the time bar
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,3);
        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(rllp);
        ll.setBackgroundColor(getResources().getColor(R.color.lost_shine_green));



        if(promotionViewMode== Enum.PromotionViewMode.daily)
        {
            //draw
            ll.setY(GetYAxisByGivenTimeInDailyView(Calendar.getInstance().get(Calendar.HOUR_OF_DAY),Calendar.getInstance().get(Calendar.MINUTE)));
            llCurrentTimeBar = ll;
            rlDailyUIPanel.addView(llCurrentTimeBar);
        }
        else if(promotionViewMode== Enum.PromotionViewMode.weekly)
        {
            //draw
            //ll.setY(GetYAxisByGivenTimeInWeeklyView(Calendar.getInstance().get(Calendar.HOUR_OF_DAY),Calendar.getInstance().get(Calendar.MINUTE)));
            ll.setY(GetYAxisByGivenTimeInDailyView(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE)));
            llCurrentTimeBar = ll;
            rlWeeklyUIPanel.addView(llCurrentTimeBar);
        }
        else
        {
            //not doing timer for monthly view
            return;
        }


        ScheduleDrawMinuteLineTask();
    }
    private void ScheduleDrawMinuteLineTask()
    {
        if(timerBarHandler==null)timerBarHandler = new Handler();
        if(timerElapseRunnable==null)timerElapseRunnable = new Runnable() {
            @Override
            public void run() {
                DrawTimeLine();
            }
        };

        int elapse =60-Calendar.getInstance().get(Calendar.SECOND);
        elapse =(elapse==0)?60:elapse;//current execution took less than a second to complete so it will execute immediately
        timerBarHandler.postDelayed(
                timerElapseRunnable, elapse*1000);
    }
    private TableRow CreateMonthlyDayRow()
    {
        int PROMOTION_EVENT_WIDTH=common.Utility.DP2Pixel(DAILY_TIME_RAW_PROMOTION_EVENT_WIDTH,PromotionUIActivity.this);
        int PROMOTION_EVENT_HEIGHT=common.Utility.DP2Pixel(DAILY_AND_WEEKLY_RAW_PROMOTION_EVENT_HEIGHT,PromotionUIActivity.this)*2;
        int DATE_BAR_WIDTH=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_WIDTH,PromotionUIActivity.this);

        TableRow tr = new TableRow(this);
        tr.setPadding(5,0,5,0);
        TableLayout.LayoutParams lllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PROMOTION_EVENT_HEIGHT+2);
        tr.setLayoutParams(lllp);

        //time label
       /* TextView tvPlaceHolder = new TextView(this);
        tvPlaceHolder.setText("");


        TableRow.LayoutParams trlp = new TableRow.LayoutParams(PROMOTION_EVENT_WIDTH,PROMOTION_EVENT_HEIGHT );
        tvPlaceHolder.setLayoutParams(trlp);

        tr.addView(tvPlaceHolder);*/

        for(int j=0;j<7;j++) {
            LinearLayout llDate = new LinearLayout(this);
            llDate.setOrientation(LinearLayout.VERTICAL);
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(DATE_BAR_WIDTH, PROMOTION_EVENT_HEIGHT);
            if(j==0)
            {
                trlp.leftMargin=PROMOTION_EVENT_WIDTH;
            }

            tr.addView(llDate, trlp);

            //date of month
            TextView tvIndex = new TextView(this);
            tvIndex.setText("#");
            tvIndex.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            tvIndex.setPadding(5, 5, 5, 5);
            LinearLayout.LayoutParams lllpDate = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,CALENDAR_SLOT_INDEX_HEIGHT);
            llDate.addView(tvIndex,lllpDate);

            //scroll view for content
            final ScrollView sv = new ScrollView(this);
            //final GestureDetector myGestureDetector = new GestureDetector(this,new MyMonthlyCalendarGestureListener(sv));
            //sv.requestDisallowInterceptTouchEvent(true);
            /*sv.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, final MotionEvent event) {
                    //common.Utility.ShowMessage("container scroll view","touch",PromotionUIActivity.this,R.drawable.message);
                    if (blnShowingFilterPopup)
                    {HideRightSideFilterBar();}
                    //destroy any active dialog
                    if (!blnShowingPromotionDetailPopup && llActiveDialog != null)
                        ClosePromotionActiveDialog();

                    view.getParent().requestDisallowInterceptTouchEvent(true);

                    return myGestureDetector.onTouchEvent(event);

                }
            });*/
             lllpDate  = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,CALENDAR_SLOT_SCROLLVIEW_HEIGHT);

            llDate.addView(sv, lllpDate);

            //container for content
            final LinearLayout llContainer = new LinearLayout(this);
            llContainer.setOrientation(LinearLayout.VERTICAL);
            llContainer.setPadding(3,0,2,2);
            ScrollView.LayoutParams svlpContainer = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,ScrollView.LayoutParams.WRAP_CONTENT);
            sv.addView(llContainer,svlpContainer);

            //llContainer.setMinimumHeight(5000);

            //insert a test content
            TextView tvTest = new TextView(this);
            tvTest.setPadding(3,3,3,3);
            tvTest.setText("this is a test");
            llContainer.addView(tvTest);




        }

        return tr;
    }

    private TableRow CreateWeekHourRow(String strTime,int i)
    {
        int PROMOTION_EVENT_WIDTH=common.Utility.DP2Pixel(DAILY_TIME_RAW_PROMOTION_EVENT_WIDTH,PromotionUIActivity.this);
        int PROMOTION_EVENT_HEIGHT=common.Utility.DP2Pixel(DAILY_AND_WEEKLY_RAW_PROMOTION_EVENT_HEIGHT,PromotionUIActivity.this);
        int DATE_BAR_WIDTH=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_WIDTH,PromotionUIActivity.this);
        //int DATE_BAR_HEIGHT=common.Utility.DP2Pixel(TABLE_HEADER_RAW_ROW_HEIGHT,PromotionUIActivity.this);

        TableRow tr = new TableRow(this);
        tr.setPadding(5,0,5,0);
        TableLayout.LayoutParams lllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PROMOTION_EVENT_HEIGHT+2);
        tr.setLayoutParams(lllp);

        //time label
        TextView tvTime = new TextView(this);
        tvTime.setText(strTime);
        if(i==23)
        {
            tvTime.setBackground(getResources().getDrawable(R.drawable.draw_border_left_top_bottom));
        }
        else {
            tvTime.setBackground(getResources().getDrawable(R.drawable.draw_border_left_top));
        }
        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(PROMOTION_EVENT_WIDTH,PROMOTION_EVENT_HEIGHT );
        tvTime.setLayoutParams(trlp);

        tr.addView(tvTime);

        for(int j=0;j<7;j++) {
            TextView tv = new TextView(this);
            trlp = new TableRow.LayoutParams(DATE_BAR_WIDTH, PROMOTION_EVENT_HEIGHT);
            tr.addView(tv, trlp);
            if(j==0)
            {
                if(i<23)
                {
                    tv.setBackground(getResources().getDrawable(R.drawable.draw_border_left_top_right));
                }
                else
                {
                    tv.setBackground(getResources().getDrawable(R.drawable.draw_border));
                }
            }

            else {
                if(i<23)
                {
                    tv.setBackground(getResources().getDrawable(R.drawable.draw_border_top_right));
                }
                else
                {
                    tv.setBackground(getResources().getDrawable(R.drawable.draw_border_top_right_bottom));
                }

            }

        }

        return tr;
    }
    private TableRow CreateDayViewHourRow(String strTime,int i)
    {
        int PROMOTION_EVENT_WIDTH=common.Utility.DP2Pixel(DAILY_TIME_RAW_PROMOTION_EVENT_WIDTH,PromotionUIActivity.this);
        int PROMOTION_EVENT_HEIGHT=common.Utility.DP2Pixel(DAILY_AND_WEEKLY_RAW_PROMOTION_EVENT_HEIGHT,PromotionUIActivity.this);
        int promotionBarWidth = common.Utility.DP2Pixel(DAILY_BAR_RAW_PROMOTION_EVENT_WIDTH,this);

        TableRow tr = new TableRow(this);
        tr.setPadding(5,0,5,0);
        TableLayout.LayoutParams lllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PROMOTION_EVENT_HEIGHT+2);
        tr.setLayoutParams(lllp);

        //time label
        TextView tvTime = new TextView(this);
        tvTime.setText(strTime);
        if(i==23)
        {
            tvTime.setBackground(getResources().getDrawable(R.drawable.draw_border));
        }
        else {
            tvTime.setBackground(getResources().getDrawable(R.drawable.draw_border_left_top_right));
        }
        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(PROMOTION_EVENT_WIDTH, PROMOTION_EVENT_HEIGHT);
        tvTime.setLayoutParams(trlp);

        //promotion bar
        LinearLayout llPromotion = new LinearLayout(this);
        if(i==23)
        {
            llPromotion.setBackground(getResources().getDrawable(R.drawable.draw_border_without_left));
        }
        else {
            llPromotion.setBackground(getResources().getDrawable(R.drawable.draw_border_top_right));
        }

        trlp = new TableRow.LayoutParams(promotionBarWidth,PROMOTION_EVENT_HEIGHT);
        llPromotion.setLayoutParams(trlp);

        tr.addView(tvTime);
        tr.addView(llPromotion);
        dayViewPromotionBar[i]=llPromotion;
        return tr;
    }
    private String ConstructNewDateHTMLLabel(Calendar calStart)
    {
        String strDate ="<u>"
                + new SimpleDateFormat("EEEE, MMMM dd, yyyy").format(new Date(calStart.getTimeInMillis())).toString()
                + "</u>";

        return strDate;
    }
    private String ConstructNewMonthlyHTMLLabel(Calendar c)
    {
        Date dtStart = new Date(c.getTimeInMillis());
        return "<u>"+new SimpleDateFormat("MMMM yyyy").format(dtStart).toString()+"</u>";
    }
    private String ConstructNewWeekHTMLLabel(Calendar calStart)
    {
        calStart.add(Calendar.DATE,
                -(calStart.get(Calendar.DAY_OF_WEEK) - calStart.getFirstDayOfWeek())
        );
        Date dtStart = new Date(calStart.getTimeInMillis());
        calStart.add(Calendar.DATE, 6);
        Date dtEnd = new Date(calStart.getTimeInMillis());

        String strWeek ="<u>"
                +new SimpleDateFormat("MM/dd/yyyy").format(dtStart).toString()
                        +" - "
                        +new SimpleDateFormat("MM/dd/yyyy").format(dtEnd).toString()+"</u>";

      /*  String strWeek ="<u>"
                +new SimpleDateFormat("MMMM dd, yyyy").format(dtStart).toString()
                +"-"
                +new SimpleDateFormat("MMMM dd, yyyy").format(dtEnd).toString()+"</u>";*/
        return strWeek;
    }
    private void ShowNewMonth(Calendar calStart,boolean blnIsSamePanel)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(calStart.getTimeInMillis());
        c.add(Calendar.DATE,
                -(c.get(Calendar.DAY_OF_WEEK)-c.getFirstDayOfWeek())
        );
        tvDateDisplayLabel.setText(Html.fromHtml(ConstructNewMonthlyHTMLLabel(c)));
        tvDateDisplayLabel.setTag(calStart.getTimeInMillis());
        PreparePanel(blnIsSamePanel);
    }
    private void ShowNewWeek(Calendar calStart,boolean blnIsSamePanel)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(calStart.getTimeInMillis());
        c.add(Calendar.DATE,
                -(c.get(Calendar.DAY_OF_WEEK)-c.getFirstDayOfWeek())
        );
        tvDateDisplayLabel.setText(Html.fromHtml(ConstructNewWeekHTMLLabel(c)));
        tvDateDisplayLabel.setTag(calStart.getTimeInMillis());
        PreparePanel(blnIsSamePanel);
    }

    private void ShowNewDay(Calendar cal,boolean blnIsSamePanel)
    {
        tvDateDisplayLabel.setText(Html.fromHtml(ConstructNewDateHTMLLabel(cal)));

        PreparePanel(blnIsSamePanel);
        DrawTimeLine();
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {

        if(!blnDateDialogShow)return;
        if(blnDateDialogShow)blnDateDialogShow = false;
        Calendar cal = new GregorianCalendar();//Calendar.getInstance();


        cal.set(year,month,day);
        if(dialogFor== Enum.CallDateDialogFrom.label)
        {
            //TextView tv =ReturnCurrentDateTextLabel();
            long oldTime = (Long)tvDateDisplayLabel.getTag();
            tvDateDisplayLabel.setTag(cal.getTimeInMillis());
            switch(promotionViewMode)
            {
                case daily:
                    ShowNewDay(cal,false);
                    break;
                case weekly:
                    //skip if the selected day still falling in to current week
                    Calendar c = new GregorianCalendar();
                    c.setTimeInMillis(oldTime);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND,0);
                    c.set(Calendar.MILLISECOND,0);
                    oldTime = c.getTimeInMillis();
                    c.add(Calendar.DATE,7);
                    if(oldTime<=cal.getTimeInMillis() && cal.getTimeInMillis()<c.getTimeInMillis()) return;
                    ShowNewWeek(cal,false);
                    break;
                case monthly:
                    c = new GregorianCalendar();
                    c.setTimeInMillis(oldTime);
                    if(c.get(Calendar.YEAR)!=cal.get(Calendar.YEAR)
                            ||c.get(Calendar.MONTH)!=cal.get(Calendar.MONTH)
                            ) {
                        ShowNewMonth(cal, false);
                    }
                    break;
            }


        }




        //reset flag
        blnDateDialogShow=false;
    }

    private void SetTextLabelProperties(TextView tv)
    {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_PROMOTION_FIELD_LABEL_TEXT_SIZE);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
    }




    private class MyDailyCalendarGestureListener implements GestureDetector.OnGestureListener{
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            return false;
        }
        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }



        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            if(motionEvent.getX()<motionEvent2.getX())
            {

                GoToPreviousDay();
            }
            else
            {

                GoToNextDay();
            }
            return true;
        }

    }
    private class MyWeeklyCalendarGestureListener implements GestureDetector.OnGestureListener{
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            return false;
        }
        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }



        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            if(motionEvent.getX()<motionEvent2.getX())
            {

                GoToPreviousWeek();
            }
            else
            {

                GoToNextWeek();
            }
            return true;
        }

    }
    private class MyMonthlyCalendarGestureListener extends MyMonthlyCalendarGestureListenerWithoutScrolling{//implements GestureDetector.OnGestureListener{
        ScrollView sv;
        public MyMonthlyCalendarGestureListener(ScrollView sv)
        {
            super(sv);
            this.sv = sv;
        }
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {

            if(motionEvent==null)return false;
            sv.scrollBy(0,-Math.round(motionEvent2.getY()-motionEvent.getY()));
            return true;
        }
       /* @Override
        public void onShowPress(MotionEvent motionEvent) {

        }
        @Override
        public boolean onDown(MotionEvent motionEvent) {

            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            //common.Utility.ShowMessage("SCROLLING","X: "+motionEvent.getX()+", Y:"+motionEvent.getY(),PromotionUIActivity.this,R.drawable.message);
            int[] position = new int[2];
            sv.getLocationOnScreen(position);
            SetTouchedLocation(position[0],position[1]);
            ShowPromotionSummariesPopupInMonthlyMode(sv);
            return true;
        }



        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
          *//* if(Math.abs(motionEvent.getY()-motionEvent2.getY())<50 && Math.abs(motionEvent.getX()-motionEvent2.getX())>100) {

                if (motionEvent.getX() < motionEvent2.getX())
                {

                    GoToPreviousMonth();
                } else if (motionEvent.getX() > motionEvent2.getX()) {

                    GoToNextMonth();
                }
           }*//*
            return false;
        }*/

    }
    private class MyMonthlyCalendarGestureListenerWithoutScrolling implements GestureDetector.OnGestureListener{
        View ll;
        public MyMonthlyCalendarGestureListenerWithoutScrolling(View ll)
        {
            this.ll = ll;
        }
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {

            return false;
        }
        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }
        @Override
        public boolean onDown(MotionEvent motionEvent) {

            if(ll instanceof ScrollView)
            {
                ((ScrollView) ll).requestDisallowInterceptTouchEvent(true);
            }
            //set flag
            blnFingerDown = true;

            //stop the current scheduled runnable
            if(runPromotionSummaryPopup!=null)
            {
                handler.removeCallbacks(runPromotionSummaryPopup);
                runPromotionSummaryPopup=null;
            }

            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {


            runPromotionSummaryPopup = new Runnable() {
                @Override
                public void run() {

                    if (!blnFingerDown && !blnShowingPromotionDetailPopup) {
                        blnShowingPromotionDetailPopup = true;
                        int[] position = new int[2];
                        ll.getLocationOnScreen(position);
                        SetTouchedLocation(position[0],position[1]);
                        ShowPromotionSummariesPopupInMonthlyMode(ll);
                    }
                }
            };
            handler.postDelayed(runPromotionSummaryPopup,100);

            //common.Utility.ShowMessage("SCROLLING","X: "+motionEvent.getX()+", Y:"+motionEvent.getY(),PromotionUIActivity.this,R.drawable.message);
            //if(blnShowingPromotionDetailPopup)return true;


            return true;
        }



        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
           /* if(Math.abs(motionEvent.getY()-motionEvent2.getY())<50 && Math.abs(motionEvent.getX()-motionEvent2.getX())>100) {

                if (motionEvent.getX() < motionEvent2.getX())
                {

                    GoToPreviousMonth();
                } else if (motionEvent.getX() > motionEvent2.getX()) {

                    GoToNextMonth();
                }
            }*/
            return false;
        }

    }


}
