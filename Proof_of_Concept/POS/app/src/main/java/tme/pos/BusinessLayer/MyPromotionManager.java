package tme.pos.BusinessLayer;

import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.widget.Switch;

import com.google.android.gms.analytics.ecommerce.Promotion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;

/**
 * Created by kchoy on 3/11/2016.
 */
public class MyPromotionManager {
    Context context;
    PromotionObject[] allPromotionsForCurrentListingReceipt;
    HashMap<Long,PromotionObject>promotionRecords;
    HashMap<Integer,HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<Long>>>>> promotionCalendarForWeek;
    HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<Long>>>>promotionCalendarForMonth;
    long lastStartTimeForByMonthQuery;
    long lastStartTimeForWeekQuery;
    //long nextPromotionStartTime=-1;
    int TOTAL_DATE_SLOT_IN_CALENDAR_VIEW=42;
    int secondForOneDay = 24*60*60;
    public MyPromotionManager(Context c)
    {
        context = c;
        promotionRecords = new HashMap<Long,PromotionObject>();
        promotionCalendarForWeek = new HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Long>>>>>();
        promotionCalendarForMonth = new HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<Long>>>>();
        lastStartTimeForByMonthQuery =0;
        lastStartTimeForWeekQuery=0;
        allPromotionsForCurrentListingReceipt = new PromotionObject[0];
    }
    public ArrayList<PromotionObject>GetPromotionForToday() {
        ArrayList<PromotionObject> results = new ArrayList<PromotionObject>();
        HashMap<Long, Boolean> records = new HashMap<Long, Boolean>();
        int date = Calendar.getInstance().get(Calendar.DATE);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        HashMap<Integer, HashMap<Integer, ArrayList<Long>>> data = GetPromotionWeekByDate_WeekDayAsKey(date, month + 1, year);

        ArrayList<Long> promotionIds;

        if (data.containsKey(date)) {
            while (hour < 24) {
                if (data.get(date).containsKey(hour)) {
                    promotionIds = data.get(date).get(hour);
                    for (int i = 0; i < promotionIds.size(); i++) {
                        records.put(promotionIds.get(i), true);
                    }
                }
                hour++;
            }

        }
        for(Long id:records.keySet()) {
            results.add(Get(id));
        }


        return results;
    }
    public ArrayList<PromotionObject>FilterPromotionForCurrentMoment(ArrayList<PromotionObject> promotionIds,Enum.PromotionByType promotionByType)
    {
        PromotionObject po;
        ArrayList<PromotionObject>results=new ArrayList<PromotionObject>();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);

        Calendar cNextPromotionStart = new GregorianCalendar();
        cNextPromotionStart.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        Calendar cEnd = new GregorianCalendar();
        Calendar cStart = new GregorianCalendar();
        for(int i=0;i<promotionIds.size();i++) {


            //check for minute component make sure still valid
            po = promotionIds.get(i);

            if(promotionByType!=null && promotionByType!=po.GetRule())
            {
                continue;
            }
            cStart.setTimeInMillis(po.GetStartDateTime());
            cEnd.setTimeInMillis(po.GetEndDateTime());


            if (cStart.get(Calendar.HOUR_OF_DAY) < hour
                    || (cStart.get(Calendar.HOUR_OF_DAY) == hour && cStart.get(Calendar.MINUTE) <= minute))
            {
                if (cEnd.get(Calendar.HOUR_OF_DAY) > hour) {
                    results.add(po);
                } else if (cEnd.get(Calendar.HOUR_OF_DAY) == hour) {

                    if (cEnd.get(Calendar.MINUTE) >= minute) results.add(po);
                }

            }
        }
        return results;
    }
    public ArrayList<PromotionObject>GetPromotionForCurrentMoment()
    {
        //nextPromotionStartTime=-1;
        ArrayList<PromotionObject>results=new ArrayList<PromotionObject>();
        int date = Calendar.getInstance().get(Calendar.DATE);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);

        PromotionObject po;
        ArrayList<Long> promotionIds;
        Calendar cNextPromotionStart = new GregorianCalendar();
        cNextPromotionStart.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        Calendar cEnd = new GregorianCalendar();
        Calendar cStart = new GregorianCalendar();
        HashMap<Integer,HashMap<Integer,ArrayList<Long>>> data=GetPromotionWeekByDate_WeekDayAsKey(date,month+1,year);


        //Calendar cNextPromotionEnd = new GregorianCalendar();
        //Calendar cNextPromotionStart = new GregorianCalendar();


        //int currentTriggerHr=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        //int currentTriggerMinute = Calendar.getInstance().get(Calendar.MINUTE);
        //long timeToTrigger=-1;
        if(data.containsKey(date))
        {
            if(data.get(date).containsKey(hour))
            {
                promotionIds = data.get(date).get(hour);
                for(int i=0;i<promotionIds.size();i++)
                {
                    /*po = Get(promotionIds.get(i));
                    cNextPromotionStart.setTimeInMillis(po.GetStartDateTime());
                    //cNextPromotionStart.add(Calendar.MINUTE,1);
                    cNextPromotionStart.set(Calendar.SECOND,0);
                    cNextPromotionEnd.setTimeInMillis(po.GetEndDateTime());
                    //cNextPromotionEnd.add(Calendar.MINUTE,1);
                    cNextPromotionEnd.set(Calendar.SECOND,0);
                    int startHr=cNextPromotionStart.get(Calendar.HOUR_OF_DAY);
                    int startMin = cNextPromotionStart.get(Calendar.MINUTE);
                    int endHr=cNextPromotionEnd.get(Calendar.HOUR_OF_DAY);
                    int endMin = cNextPromotionEnd.get(Calendar.MINUTE);


                        //need to be greater than current moment initially!!!
                        if (currentTriggerHr == startHr && currentTriggerMinute < startMin ) {
                            results.add(po);
                        } else if (currentTriggerHr < startHr) {
                            results.add(po);
                        } else if (currentTriggerHr == endHr && currentTriggerMinute < endMin) {
                            results.add(po);
                        } else if (currentTriggerHr < endHr || endHr==0) {
                            results.add(po);
                        }

*/

                    //check for minute component make sure still valid
                    po = Get(promotionIds.get(i));
                    cStart.setTimeInMillis(po.GetStartDateTime());
                    cEnd.setTimeInMillis(po.GetEndDateTime());
                    Date d1 = new Date(po.GetStartDateTime());Date d2 = new Date(po.GetEndDateTime());
                    int startHr = cStart.get(Calendar.HOUR_OF_DAY);
                    int startMin = cStart.get(Calendar.MINUTE);
                    int endHr = cEnd.get(Calendar.HOUR_OF_DAY);
                    int endMin = cEnd.get(Calendar.MINUTE);

                    int startHr2 = cNextPromotionStart.get(Calendar.HOUR_OF_DAY);
                    int startMin2 = cNextPromotionStart.get(Calendar.MINUTE);


                    if(cStart.get(Calendar.HOUR_OF_DAY)<hour
                          || (cStart.get(Calendar.HOUR_OF_DAY)==hour && cStart.get(Calendar.MINUTE)<=minute))
                    {
                      if(cEnd.get(Calendar.HOUR_OF_DAY)>hour)
                      {
                          results.add(po);
                      }
                      else if(cEnd.get(Calendar.HOUR_OF_DAY)==hour)
                      {

                          if(cEnd.get(Calendar.MINUTE)>=minute)results.add(po);
                      }
                    }
                    else
                    {
                        cNextPromotionStart.set(Calendar.HOUR_OF_DAY,cStart.get(Calendar.HOUR_OF_DAY));
                        cNextPromotionStart.set(Calendar.MINUTE,cStart.get(Calendar.MINUTE));
                        cNextPromotionStart.set(Calendar.SECOND,cStart.get(Calendar.SECOND));

                       /* if(nextPromotionStartTime==-1)
                        {

                            nextPromotionStartTime = cNextPromotionStart.getTimeInMillis();
                        }
                        else
                        {
                            if(nextPromotionStartTime>cNextPromotionStart.getTimeInMillis())
                                nextPromotionStartTime = cNextPromotionStart.getTimeInMillis();
                        }*/
                    }

                }
            }
        }
        return results;
    }
    public long GetTimeForPromotionRefresh(boolean blnIncludeCashPromotion)
    {
        boolean blnIsEndTime = false;
        ArrayList<PromotionObject> promotions= GetPromotionForToday();
        Calendar cNextPromotionEnd = new GregorianCalendar();
        Calendar cNextPromotionStart = new GregorianCalendar();
        long timeToTrigger = -1;//reset

        int currentTriggerHr=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentTriggerMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int currentHr = currentTriggerHr;
        int currentMinute = currentTriggerMinute;

        for(int i=0;i<promotions.size();i++)
        {

            /**we are only interested in promotion trigger by item***/
            if(promotions.get(i).GetRule()== Enum.PromotionByType.total && !blnIncludeCashPromotion)continue;

            //get next trigger reload time

            cNextPromotionStart.setTimeInMillis(promotions.get(i).GetStartDateTime());

            cNextPromotionStart.set(Calendar.SECOND,0);
            cNextPromotionEnd.setTimeInMillis(promotions.get(i).GetEndDateTime());

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

        if(timeToTrigger<=-1)
        {

            timeToTrigger =(((secondForOneDay))-
                    ((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60*60)+(Calendar.getInstance().get(Calendar.MINUTE)*60)+Calendar.getInstance().get(Calendar.SECOND)))*1000;
        }
        else
        {
            //adjust the time so that it will execute exactly on time
            Calendar cNext = new GregorianCalendar();
            cNext.setTimeInMillis(timeToTrigger);
            int hr = cNext.get(Calendar.HOUR_OF_DAY);
            int minute = cNext.get(Calendar.MINUTE);

            timeToTrigger =(((hr*60*60)+(minute*60))-
                    ((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60*60)+(Calendar.getInstance().get(Calendar.MINUTE)*60)+Calendar.getInstance().get(Calendar.SECOND)))*1000;
            timeToTrigger+=(timeToTrigger>-1 && blnIsEndTime)?60000:0;

        }



        return timeToTrigger;//nextPromotionStartTime;
    }
    public ArrayList<Long> GetDistinctPromotionIds(int date,int regular_month,int year,boolean blnFromMonthlyCalendar)
    {
        boolean blnFound;
        ArrayList<Long> ids = new ArrayList<Long>();
        ArrayList<Long> tempLst;

        /*//base case no id loaded yet
        if(lastStartTimeForByMonthQuery==0 && lastStartTimeForWeekQuery==0)
        {
            GetPromotionWeekByDate_WeekDayAsKey(date,regular_month,year);
        }*/

        if(!blnFromMonthlyCalendar)
        {
            promotionCalendarForMonth.clear();
            lastStartTimeForByMonthQuery=0;
            GetPromotionWeekByDate_WeekDayAsKey(date,regular_month,year);

            if(promotionCalendarForWeek.containsKey(year))
                if(promotionCalendarForWeek.get(year).containsKey(regular_month))
                    if(promotionCalendarForWeek.get(year).get(regular_month).containsKey(date))
                    {
                        HashMap<Integer,ArrayList<Long>> data=promotionCalendarForWeek.get(year).get(regular_month).get(date);
                        for(int hr:data.keySet())
                        {
                            tempLst = data.get(hr);
                            for(int i=0;i<tempLst.size();i++)
                            {
                                blnFound = false;
                                for(int j=0;j<ids.size();j++)
                                {
                                    if(tempLst.get(i).longValue()==ids.get(j).longValue())
                                    {
                                        blnFound=true;
                                    }
                                }
                                if(!blnFound)
                                {
                                    ids.add(tempLst.get(i));
                                }
                            }
                        }
                    }
        }
        else
        {
            promotionCalendarForWeek.clear();
            lastStartTimeForWeekQuery=0;
            GetPromotionByMonth(regular_month,year,TOTAL_DATE_SLOT_IN_CALENDAR_VIEW);
            if(promotionCalendarForMonth.containsKey(year))
                if(promotionCalendarForMonth.get(year).containsKey(regular_month))
                    if(promotionCalendarForMonth.get(year).get(regular_month).containsKey(date))
                    {
                        return promotionCalendarForMonth.get(year).get(regular_month).get(date);
                    }
        }
        return ids;
    }
    public HashMap<Integer,HashMap<Integer,ArrayList<Long>>> GetPromotionWeekByDate_WeekDayAsKey(int date,int not_java_calendar_month,int year)
    {
        int javaCalendarMonth = not_java_calendar_month-1;
        Calendar c =new GregorianCalendar(year,javaCalendarMonth,date,0,0,0);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());

        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(c.getTimeInMillis());
        cEnd.add(Calendar.DATE,6);
        if(c.getTimeInMillis()!=lastStartTimeForWeekQuery) {
            ClearCache();//order does matter
            lastStartTimeForWeekQuery = c.getTimeInMillis();
            AddToExistingCalendar(LoadPromotions(c.getTimeInMillis(), cEnd.getTimeInMillis(),false));
        }


        //not_java_calendar_month = c.get(Calendar.MONTH)+1;
        //date = c.get(Calendar.DATE);
        HashMap<Integer,HashMap<Integer,ArrayList<Long>>> data=new HashMap<Integer, HashMap<Integer, ArrayList<Long>>>();
       /* */
        for(int i=0;i<7;i++) {
            CreateInstanceIfNotExisted(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DATE));

            //insert date entry
            data.put(c.get(Calendar.DATE), new HashMap<Integer,ArrayList<Long>>());
            //start inserting promotion id into data object
            HashMap<Integer, ArrayList<Long>> hrs = promotionCalendarForWeek.get(c.get(Calendar.YEAR)).get(c.get(Calendar.MONTH) + 1).get(c.get(Calendar.DATE));
            if(hrs.size()>0) {
                for (int hr : hrs.keySet()) {
                    data.get(c.get(Calendar.DATE)).put(hr, new ArrayList<Long>(hrs.get(hr)));

                }
            }
            else
            {
                data.get(c.get(Calendar.DATE)).put(0, new ArrayList<Long>());
            }
            c.add(Calendar.DATE,1);
        }
        return  data;
    }
    public HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<Long>>>> GetPromotionByMonth(int not_java_calendar_month,int year,int totalDayToShow)
    {


        int javaCalendarMonth = not_java_calendar_month-1;
        Calendar c =new GregorianCalendar(year,javaCalendarMonth,1,0,0,0);//Calendar.getInstance();
        c=common.Utility.GetFirstDayOfWeekDate(c);
        long start = c.getTimeInMillis();

        //return cache version
        if(lastStartTimeForByMonthQuery==start)return promotionCalendarForMonth;
        ClearCache();


        //Date date1 = new Date(c.getTimeInMillis());
        Calendar cEnd = new GregorianCalendar();
        cEnd.set(year, javaCalendarMonth, c.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

        //Date date2 = new Date(c.getTimeInMillis());



        int cDiff = Math.round(cEnd.getTimeInMillis()-start/(1000*59*59*24));
        int dayOffset = totalDayToShow-cDiff;
        cEnd.add(Calendar.DATE,dayOffset+1);
        long end = cEnd.getTimeInMillis();


        AddToMonthCalendar(LoadPromotions(start, end,false));
        lastStartTimeForByMonthQuery  = start;
        return promotionCalendarForMonth;
        //CreateInstanceIfNotExisted(year,not_java_calendar_month,1);
        //return po id and dates
        //return promotionCalendar.get(year).get(not_java_calendar_month);
    }
    private void InsertIntoTableCalendar(int currentYear,int currentMonth,int currentDate
    ,int startHr,int endHr,PromotionObject po){
        CreateInstanceIfNotExisted(currentYear, currentMonth, currentDate);
        HashMap<Integer, HashMap<Integer, ArrayList<Long>>> targetMonth = promotionCalendarForWeek.get(currentYear).get(currentMonth);
        HashMap<Integer, ArrayList<Long>> targetDate = targetMonth.get(currentDate);
        for(int i=startHr;i<=endHr;i++)
        {
            if (!targetDate.containsKey(i)) {
                targetDate.put(i, new ArrayList<Long>());
            }
            ArrayList<Long> targetHour = targetDate.get(i);
            targetHour.add(po.GetId());

        }
    }
    private void AddOncePromotionIntoMonthCalendar(PromotionObject po)
    {
        if(po.GetPromotionDateOption()!= Enum.PromotionDateOption.once)return;
        Calendar cStart = new GregorianCalendar();
        cStart.setTimeInMillis(po.GetStartDateTime());
        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(po.GetEndDateTime());


        //int startHr = cStart.get(Calendar.HOUR_OF_DAY);
        int currentYear =  cStart.get(Calendar.YEAR);
        int currentMonth = cStart.get(Calendar.MONTH)+1;
        int currentDate = cStart.get(Calendar.DAY_OF_MONTH);



        while(cStart.getTimeInMillis()<cEnd.getTimeInMillis())
        {
            CreateIfDoesNotExistsForMonthCalendar(currentYear,currentMonth,currentDate);

            promotionCalendarForMonth.get(currentYear).get(currentMonth).get(currentDate).add(po.GetId());

            cStart.add(Calendar.DATE,1);

            currentYear =  cStart.get(Calendar.YEAR);
            currentMonth = cStart.get(Calendar.MONTH)+1;
            currentDate = cStart.get(Calendar.DAY_OF_MONTH);
        }
    }
    private void AddOncePromotionIntoCalendar(PromotionObject po)
    {
        if(po.GetPromotionDateOption()!= Enum.PromotionDateOption.once)return;
        Calendar cStart = new GregorianCalendar();
        cStart.setTimeInMillis(po.GetStartDateTime());
        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(po.GetEndDateTime());


        int startHr = cStart.get(Calendar.HOUR_OF_DAY);
        int currentYear =  cStart.get(Calendar.YEAR);
        int currentMonth = cStart.get(Calendar.MONTH)+1;
        int currentDate = cStart.get(Calendar.DAY_OF_MONTH);
        cStart.set(Calendar.MINUTE,0);
        cStart.set(Calendar.SECOND,0);
        cStart.set(Calendar.MILLISECOND,0);

        int endHr = cEnd.get(Calendar.HOUR_OF_DAY);
        int endYear =  cEnd.get(Calendar.YEAR);
        int endMonth = cEnd.get(Calendar.MONTH)+1;
        int endDate = cEnd.get(Calendar.DAY_OF_MONTH);


        while(cStart.getTimeInMillis()<cEnd.getTimeInMillis())
        {
            InsertIntoTableCalendar(currentYear,currentMonth,currentDate,startHr,endHr,po);


            cStart.add(Calendar.DATE,1);

            currentYear =  cStart.get(Calendar.YEAR);
            currentMonth = cStart.get(Calendar.MONTH)+1;
            currentDate = cStart.get(Calendar.DAY_OF_MONTH);
        }
    }

    private ArrayList<Integer> GetOccurMonths(PromotionObject po)
    {
        ArrayList<Integer>months = new ArrayList<Integer>();
        if((po.GetOccurMonth()& Enum.Month.Jan.value)==Enum.Month.Jan.value)
        {
            months.add(1);
        }
        if((po.GetOccurMonth()& Enum.Month.Feb.value)==Enum.Month.Feb.value)
        {
            months.add(2);
        }
        if((po.GetOccurMonth()& Enum.Month.Mar.value)==Enum.Month.Mar.value)
        {
            months.add(3);
        }
        if((po.GetOccurMonth()& Enum.Month.Apr.value)==Enum.Month.Apr.value)
        {
            months.add(4);
        }
        if((po.GetOccurMonth()& Enum.Month.May.value)==Enum.Month.May.value)
        {
            months.add(5);
        }
        if((po.GetOccurMonth()& Enum.Month.Jun.value)==Enum.Month.Jun.value)
        {
            months.add(6);
        }
        if((po.GetOccurMonth()& Enum.Month.Jul.value)==Enum.Month.Jul.value)
        {
            months.add(7);
        }
        if((po.GetOccurMonth()& Enum.Month.Aug.value)==Enum.Month.Aug.value)
        {
            months.add(8);
        }
        if((po.GetOccurMonth()& Enum.Month.Sep.value)==Enum.Month.Sep.value)
        {
            months.add(9);
        }
        if((po.GetOccurMonth()& Enum.Month.Oct.value)==Enum.Month.Oct.value)
        {
            months.add(10);
        }
        if((po.GetOccurMonth()& Enum.Month.Nov.value)==Enum.Month.Nov.value)
        {
            months.add(11);
        }
        if((po.GetOccurMonth()& Enum.Month.Dec.value)==Enum.Month.Dec.value)
        {
            months.add(12);
        }
        return months;
    }

    private void AddMonthPromotionIntoMonthCalendar(PromotionObject po)
    {
        if(po.GetPromotionDateOption()!= Enum.PromotionDateOption.month)return;

        Calendar cStart = new GregorianCalendar();
        cStart.setTimeInMillis(po.GetStartDateTime());
        int startHr = cStart.get(Calendar.HOUR_OF_DAY);
        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(po.GetEndDateTime());
        int endHr = cEnd.get(Calendar.HOUR_OF_DAY);

        Calendar tempCal = new GregorianCalendar();
        tempCal.setTimeInMillis(po.GetStartDateTime());

        int currentYear =  tempCal.get(Calendar.YEAR);
        int currentMonth = tempCal.get(Calendar.MONTH)+1;
        int currentDate = tempCal.get(Calendar.DAY_OF_MONTH);


        ArrayList<Integer>months = GetOccurMonths(po);
        String[] days = po.GetDayOfMonth().split(",");
        while(tempCal.getTimeInMillis()<po.GetEndDateTime())
        {
            for(int i=0;i<months.size();i++)
            {

                //set calendar month
                tempCal.set(Calendar.MONTH, months.get(i) - 1);
                currentMonth = tempCal.get(Calendar.MONTH)+1;


                for(int j = 0;j<days.length;j++)
                {
                    int day = Integer.parseInt(days[j]);
                    if(tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)>=day)
                    {
                        //set calendar day
                        tempCal.set(Calendar.DATE,day);

                        //check expiration date
                        Date dEnd = new Date(po.GetEndDateTime());
                        Date dExp = new Date(po.GetExpirationDate());
                        if(tempCal.getTimeInMillis()>po.GetEndDateTime())return;

                        //start on creation date only
                        if(po.GetStartDateTime()>tempCal.getTimeInMillis())continue;

                        //insertion
                        currentDate = tempCal.get(Calendar.DAY_OF_MONTH);
                        CreateIfDoesNotExistsForMonthCalendar(currentYear,currentMonth,currentDate);
                        promotionCalendarForMonth.get(currentYear).get(currentMonth).get(currentDate).add(po.GetId());

                    }
                }
            }


            //add calendar year
            tempCal.add(Calendar.YEAR, 1);
            currentYear =  tempCal.get(Calendar.YEAR);
        }
    }
    private void AddMonthPromotionIntoCalendar(PromotionObject po)
    {
        if(po.GetPromotionDateOption()!= Enum.PromotionDateOption.month)return;

        Calendar cStart = new GregorianCalendar();
        cStart.setTimeInMillis(po.GetStartDateTime());
        int startHr = cStart.get(Calendar.HOUR_OF_DAY);
        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(po.GetEndDateTime());
        int endHr = cEnd.get(Calendar.HOUR_OF_DAY);

        Calendar tempCal = new GregorianCalendar();
        tempCal.setTimeInMillis(po.GetStartDateTime());

        int currentYear =  tempCal.get(Calendar.YEAR);
        int currentMonth = tempCal.get(Calendar.MONTH)+1;
        int currentDate = tempCal.get(Calendar.DAY_OF_MONTH);


        ArrayList<Integer>months = GetOccurMonths(po);
        String[] days = po.GetDayOfMonth().split(",");
        while(tempCal.getTimeInMillis()<po.GetEndDateTime())
        {
            for(int i=0;i<months.size();i++)
            {

                //set calendar month
                tempCal.set(Calendar.MONTH, months.get(i) - 1);
                currentMonth = tempCal.get(Calendar.MONTH)+1;


                for(int j = 0;j<days.length;j++)
                {
                    int day = Integer.parseInt(days[j]);
                    if(tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)>=day)
                    {
                        //set calendar day
                        tempCal.set(Calendar.DATE,day);

                        //check expiration date
                        Date dEnd = new Date(po.GetEndDateTime());
                        Date dExp = new Date(po.GetExpirationDate());
                        if(tempCal.getTimeInMillis()>po.GetEndDateTime())return;

                        //start on creation date only
                        if(po.GetStartDateTime()>tempCal.getTimeInMillis())continue;

                        //insertion
                        currentDate = tempCal.get(Calendar.DAY_OF_MONTH);
                        InsertIntoTableCalendar(currentYear,currentMonth,currentDate,startHr,endHr,po);
                    }
                }
            }


            //add calendar year
            tempCal.add(Calendar.YEAR, 1);
            currentYear =  tempCal.get(Calendar.YEAR);
        }


    }
    private void AddWeekDayPromotionIntoMonthCalendar(PromotionObject po)
    {
        if(po.GetPromotionDateOption()!= Enum.PromotionDateOption.day)return;
        Calendar cStart = new GregorianCalendar();
        cStart.setTimeInMillis(po.GetStartDateTime());
        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(po.GetEndDateTime());


        int startHr = cStart.get(Calendar.HOUR_OF_DAY);
        int currentYear =  cStart.get(Calendar.YEAR);
        int currentMonth = cStart.get(Calendar.MONTH)+1;
        int currentDate = cStart.get(Calendar.DAY_OF_MONTH);


        int endHr = cEnd.get(Calendar.HOUR_OF_DAY);

        Calendar tempCal = new GregorianCalendar();
        tempCal.setTimeInMillis(cStart.getTimeInMillis());
        int firstDayOfWeek = tempCal.getFirstDayOfWeek();
        boolean blnFirst = true;
        //skip any day that's occur before the start day
        while(tempCal.getTimeInMillis()<cEnd.getTimeInMillis())
        {
            Date d = new Date(tempCal.getTimeInMillis());
            //adjusting to the correct week
            if(firstDayOfWeek==tempCal.get(Calendar.DAY_OF_WEEK))
            {
                //check is the 1st loop

                if(!blnFirst)
                {
                    if(po.GetOccurrence()== Enum.OccurrenceWeek.Weekly)
                    {
                        //do nothing
                    }
                    else if(po.GetOccurrence()== Enum.OccurrenceWeek.TwoWeek)
                    {
                        tempCal.add(Calendar.DATE,7);
                    }
                    else if(po.GetOccurrence()== Enum.OccurrenceWeek.ThreeWeek)
                    {
                        tempCal.add(Calendar.DATE,14);
                    }
                    else if(po.GetOccurrence()== Enum.OccurrenceWeek.Monthly)
                    {
                        tempCal.add(Calendar.DATE,21);
                    }
                }

            }
            blnFirst = false;
            //check if the day is our target day
            if(IsWeekPromotionOccur(tempCal,po))
            {
                currentYear =  tempCal.get(Calendar.YEAR);
                currentMonth = tempCal.get(Calendar.MONTH)+1;
                currentDate = tempCal.get(Calendar.DAY_OF_MONTH);

                CreateIfDoesNotExistsForMonthCalendar(currentYear,currentMonth,currentDate);
                promotionCalendarForMonth.get(currentYear).get(currentMonth).get(currentDate).add(po.GetId());




            }
            tempCal.add(Calendar.DATE,1);
        }

    }
    private void CreateIfDoesNotExistsForMonthCalendar(int currentYear,int currentMonth,int currentDate)
    {
        if(!promotionCalendarForMonth.containsKey(currentYear))
        {
            promotionCalendarForMonth.put(currentYear,new HashMap<Integer, HashMap<Integer, ArrayList<Long>>>());
        }

        if(!promotionCalendarForMonth.get(currentYear).containsKey(currentMonth))
        {
            promotionCalendarForMonth.get(currentYear).put(currentMonth,new HashMap<Integer, ArrayList<Long>>());
        }

        if(!promotionCalendarForMonth.get(currentYear).get(currentMonth).containsKey(currentDate))
        {
            promotionCalendarForMonth.get(currentYear).get(currentMonth).put(currentDate,new ArrayList<Long>());
        }
    }
    private void AddWeekDayPromotionIntoCalendar(PromotionObject po)
    {
        if(po.GetPromotionDateOption()!= Enum.PromotionDateOption.day)return;
        Calendar cStart = new GregorianCalendar();
        cStart.setTimeInMillis(po.GetStartDateTime());
        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(po.GetEndDateTime());


        int startHr = cStart.get(Calendar.HOUR_OF_DAY);
        int currentYear =  cStart.get(Calendar.YEAR);
        int currentMonth = cStart.get(Calendar.MONTH)+1;
        int currentDate = cStart.get(Calendar.DAY_OF_MONTH);


        int endHr = cEnd.get(Calendar.HOUR_OF_DAY);

        Calendar tempCal = new GregorianCalendar();
        tempCal.setTimeInMillis(cStart.getTimeInMillis());
        int firstDayOfWeek = tempCal.getFirstDayOfWeek();
        boolean blnFirst = true;
        //skip any day that's occur before the start day
        while(tempCal.getTimeInMillis()<cEnd.getTimeInMillis())
        {
            Date d = new Date(tempCal.getTimeInMillis());
            //adjusting to the correct week
            if(firstDayOfWeek==tempCal.get(Calendar.DAY_OF_WEEK))
            {
                //check is the 1st loop

                if(!blnFirst)
                {
                    if(po.GetOccurrence()== Enum.OccurrenceWeek.Weekly)
                    {
                        //do nothing
                    }
                    else if(po.GetOccurrence()== Enum.OccurrenceWeek.TwoWeek)
                    {
                        tempCal.add(Calendar.DATE,7);
                    }
                    else if(po.GetOccurrence()== Enum.OccurrenceWeek.ThreeWeek)
                    {
                        tempCal.add(Calendar.DATE,14);
                    }
                    else if(po.GetOccurrence()== Enum.OccurrenceWeek.Monthly)
                    {
                        tempCal.add(Calendar.DATE,21);
                    }
                }

            }
            blnFirst = false;
            //check if the day is our target day
            if(IsWeekPromotionOccur(tempCal,po))
            {
                currentYear =  tempCal.get(Calendar.YEAR);
                currentMonth = tempCal.get(Calendar.MONTH)+1;
                currentDate = tempCal.get(Calendar.DAY_OF_MONTH);

                InsertIntoTableCalendar(currentYear,currentMonth,currentDate,startHr,endHr,po);



            }
            tempCal.add(Calendar.DATE,1);
        }


    }


    private boolean IsWeekPromotionOccur(Calendar c, PromotionObject po)
    {
        Calendar startTime = new GregorianCalendar();
        startTime.setTimeInMillis(po.GetWeekMonthPromotionStartTime());
        Calendar endTime = new GregorianCalendar();
        endTime.setTimeInMillis(po.GetWeekMonthPromotionEndTime());

        boolean blnFlag = false;
        if(c.get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY
                && (po.GetOccurDay() & Enum.Day.Monday.value)== Enum.Day.Monday.value)
        {
            blnFlag = true;
        }
        else if(c.get(Calendar.DAY_OF_WEEK)==Calendar.TUESDAY
                && (po.GetOccurDay() & Enum.Day.Tuesday.value)== Enum.Day.Tuesday.value)
        {
            blnFlag = true;
        }
        else if(c.get(Calendar.DAY_OF_WEEK)==Calendar.WEDNESDAY
                && (po.GetOccurDay() & Enum.Day.Wednesday.value)== Enum.Day.Wednesday.value)
        {
            blnFlag = true;
        }
        else if(c.get(Calendar.DAY_OF_WEEK)==Calendar.THURSDAY
                && (po.GetOccurDay() & Enum.Day.Thursday.value)== Enum.Day.Thursday.value)
        {
            blnFlag = true;
        }
        else if(c.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY
                && (po.GetOccurDay() & Enum.Day.Friday.value)== Enum.Day.Friday.value)
        {
            blnFlag = true;
        }
        else if(c.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY
                && (po.GetOccurDay() & Enum.Day.Saturday.value)== Enum.Day.Saturday.value)
        {
            blnFlag = true;
        }
        else if(c.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY
                && (po.GetOccurDay() & Enum.Day.Sunday.value)== Enum.Day.Sunday.value)
        {
            blnFlag = true;
        }



        return blnFlag;
    }
    private void RemoveFromMonthCalendar(long promotionId)
    {
        HashMap<Integer,HashMap<Integer,ArrayList<Long>>> months;
        HashMap<Integer,ArrayList<Long>> days;
        ArrayList<Long> promotionIds;
        for(int year:promotionCalendarForMonth.keySet())
        {
            months = promotionCalendarForMonth.get(year);
            for(int month:months.keySet())
            {
                days = months.get(month);

                for(int day:days.keySet())
                {
                    promotionIds = days.get(day);

                    for(int i=promotionIds.size()-1;i>-1;i--)
                    {

                        if(promotionIds.get(i)==promotionId)
                        {
                            promotionIds.remove(i);
                        }
                    }
                }

            }
        }
    }
    private void AddToMonthCalendar(PromotionObject[] items)
    {


            for (int i=0;i<items.length;i++)
            {
                if(items[i].GetPromotionDateOption()== Enum.PromotionDateOption.once)
                {
                    AddOncePromotionIntoMonthCalendar(items[i]);
                }
                else if(items[i].GetPromotionDateOption() == Enum.PromotionDateOption.day)
                {
                    AddWeekDayPromotionIntoMonthCalendar(items[i]);
                }
                else if(items[i].GetPromotionDateOption() == Enum.PromotionDateOption.month)
                {
                    AddMonthPromotionIntoMonthCalendar(items[i]);
                }
            }

    }
    private void AddToExistingCalendar(PromotionObject[] items)
    {

            for (int i = 0; i < items.length; i++) {
                if (items[i].GetPromotionDateOption() == Enum.PromotionDateOption.once) {
                    AddOncePromotionIntoCalendar(items[i]);
                } else if (items[i].GetPromotionDateOption() == Enum.PromotionDateOption.day) {
                    AddWeekDayPromotionIntoCalendar(items[i]);
                } else if (items[i].GetPromotionDateOption() == Enum.PromotionDateOption.month) {
                    AddMonthPromotionIntoCalendar(items[i]);
                }
            }


    }
    public int RemovePromotion(long id)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String[] columns ={Schema.DataTable_Promotion.ACTIVE_FLAG_COLUMN
        ,Schema.DataTable_Promotion.INACTIVE_DATE_COLUMN};
        String[] values={"0",Calendar.getInstance().getTimeInMillis()+""};
        String[] args = {id+"","1"};
        String strWhereClause = Schema.DataTable_Promotion.ID_COLUMN+"=? and "
                +Schema.DataTable_Promotion.ACTIVE_FLAG_COLUMN+"=?";
        int rowCount =helper.Update(Schema.DataTable_Promotion.TABLE_NAME,
                columns,values
                ,strWhereClause,args);

        if(rowCount>0)
        {RemovePromotionFromCalendar(id);RemovePromotionObjectFromCache(id);}
        return rowCount;

    }
    public int SetExpire(long id)
    {
        Calendar cExp = new GregorianCalendar();
        cExp.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis( promotionRecords.get(id).GetEndDateTime());
        c.set(cExp.get(Calendar.YEAR), cExp.get(Calendar.MONTH), cExp.get(Calendar.DATE));
        DatabaseHelper helper = new DatabaseHelper(context);
        String[] columns ={Schema.DataTable_Promotion.EXPIRATION_DATE_COLUMN,Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN};
        //Calendar c = new GregorianCalendar();
        //c.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        cExp.set(Calendar.HOUR_OF_DAY, 23);
        cExp.set(Calendar.MINUTE, 59);
        cExp.set(Calendar.SECOND, 59);
        String[] values={cExp.getTimeInMillis()+"",c.getTimeInMillis()+""};
        String[] args = {id+"","1"};
        String strWhereClause = Schema.DataTable_Promotion.ID_COLUMN+"=? and "
                +Schema.DataTable_Promotion.ACTIVE_FLAG_COLUMN+"=?";

        int rowCount =helper.Update(Schema.DataTable_Promotion.TABLE_NAME,
                columns, values
                , strWhereClause, args);

        if(rowCount>0)
        {
            SetExpirationDateAndFlag(cExp.getTimeInMillis(),c.getTimeInMillis(), id);
            RemovePromotionFromCalendar(id);
            AddToExistingCalendar(new PromotionObject[]{promotionRecords.get(id)});
        }
        return rowCount;
    }
    private void SetExpirationDateAndFlag(long expDate,long endOnDate,long id)
    {

        promotionRecords.get(id).SetExpirationDate(expDate);//this one will have the date and time user click on the expire button
        promotionRecords.get(id).SetEndDateTime(endOnDate);//preserving the original time component

    }
    private void RemovePromotionObjectFromCache(long id)
    {
        promotionRecords.remove(id);
    }
    private void RemovePromotionFromCalendar(long id)
    {
        for(int year:promotionCalendarForWeek.keySet())
        {
            for(int month:promotionCalendarForWeek.get(year).keySet())
            {
               for(int date:promotionCalendarForWeek.get(year).get(month).keySet())
               {
                   for(int hr:promotionCalendarForWeek.get(year).get(month).get(date).keySet())
                   {
                       ArrayList<Long> ids = promotionCalendarForWeek.get(year).get(month).get(date).get(hr);
                       for(int i=ids.size()-1;i>=0;i--)
                       {
                           //only one target id in an hour time frame
                           if(ids.get(i)==id) {
                               ids.remove(i);
                               common.Utility.LogActivity("removed promotion id "+id + " from "+month+"/"+date+"/"+year+" hr:"+hr);
                               break;
                           }
                       }
                       promotionCalendarForWeek.get(year).get(month).get(date).put(hr,ids);//reinsert
                   }
               }
            }
        }
    }
    private void CreateInstanceIfNotExisted(int year,int month,int date)
    {
        if(!promotionCalendarForWeek.containsKey(year))
        {
            promotionCalendarForWeek.put(year, new HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Long>>>>());
        }
        //month doesn't existed do a query
        if(!promotionCalendarForWeek.get(year).containsKey(month))
        {
            promotionCalendarForWeek.get(year).put(month, new HashMap<Integer, HashMap<Integer, ArrayList<Long>>>());
        }

        HashMap<Integer, HashMap<Integer, ArrayList<Long>>>targetMonth = promotionCalendarForWeek.get(year).get(month);

        if(!targetMonth.containsKey(date))
        {
            targetMonth.put(date,new HashMap<Integer, ArrayList<Long>>());
        }
    }

    public boolean Update(PromotionObject po)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        if(helper.UpdatePromotionObjectWithTransaction(po))
        {
            //clear all data and reload
            ClearCache();

            /*PromotionObject target = po;//update the old with the newly saved
            PromotionObject[] items =new PromotionObject[]{target};
            RemovePromotionFromCalendar(po.GetId());
            RemoveFromMonthCalendar(po.GetId());
            promotionRecords.put(po.GetId(),po);
            if(lastStartTimeForByMonthQuery==0)
            {
                AddToExistingCalendar(items);
            }
            else
            {
                AddToMonthCalendar(items);
            }*/

            return true;
        }
        return false;

    }
    public void ReloadPromotion(long id)
    {
        common.Utility.LogActivity("query promotion id "+id);



        DatabaseHelper helper = new DatabaseHelper(context);
        String strSortOrder=Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN+ " asc";


        String strWhereClause=Schema.DataTable_Promotion.ID_COLUMN+" =? ";
        String[] args = {id+""};

        Cursor c = helper.query(Schema.DataTable_Promotion.TABLE_NAME, Schema.DataTable_Promotion.GetColumnNames(), strWhereClause, args, strSortOrder);
        ArrayList<PromotionObject> results= ReadPromotionObjects(c);
        helper.close();

        //update the existing one
        if(results.size()>0) {
            if (promotionRecords.containsKey(results))
            {
                promotionRecords.put(results.get(0).GetId(),results.get(0));
            }
        }


    }
    public long Save(PromotionObject po)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        po.SetId(helper.GenerateNextPromotionId());

        String[] values = new String[]{
                po.GetId()+""
                ,po.GetTitle().toString()
                ,po.GetStartDateTime()+""
                ,po.GetEndDateTime()+""
        ,po.GetOccurDay()+""
                ,po.GetOccurrence().value+""
                ,po.GetRule().value+""
                ,po.GetAllRuleItemString()
                ,po.GetStartingAmount().toPlainString()
        ,po.GetUpperLimitAmount().toPlainString()
                ,(po.GetUpperLimitFlag()?"1":"0")
                ,po.GetDiscountType().value+""
                ,po.GetDiscountValue()+""
        ,po.GetDiscountColor().value+""
        ,"1"//active flag
                ,""//Inactive date
        ,po.GetPromotionDateOption().value+""
        ,po.GetOccurMonth()+""
        ,po.GetDayOfMonth()
        ,po.GetExpirationDate()+""
                ,po.GetCurrentVersionNumber()+""
                ,po.GetCreatedDate()+""
                ,"1"
                ,common.myAppSettings.DEVICE_UNIQUE_ID
                ,"0"
                ,""
       };


        long id =helper.Insert(Schema.DataTable_Promotion.TABLE_NAME
                , Schema.DataTable_Promotion.GetColumnNames()
                , values);
        if(id>-1)
        {
            ClearCache();
           /* id = po.GetId();
            //set flag
            po.SetActiveFlag(true);
            //add to existing
            promotionRecords.put(po.GetId(),po);
            AddToExistingCalendar(new PromotionObject[]{po});
            common.Utility.LogActivity("created new promotion "+po.GetId());*/
        }
        else
        {
            common.Utility.LogActivity("failed to create promotion");
        }
        return id;
    }

    public void ClearCache()
    {
        promotionCalendarForMonth.clear();
        promotionCalendarForWeek.clear();
        promotionRecords.clear();
        lastStartTimeForWeekQuery=0;
        lastStartTimeForByMonthQuery=0;
    }
    private HashMap<Long,ArrayList<PromotionObject>> ReadHistoricalPromotionObjects(Cursor c)
    {
        HashMap<Long,ArrayList<PromotionObject>>newItems= new HashMap<Long,ArrayList<PromotionObject>>();

        int columnTitleIndex=c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.TITLE_COLUMN);
        int columnIdIndex=c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.ID_COLUMN);
        int columnFromDateIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.FROM_DATE_TIME_COLUMN);
        int columnToDateIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.TO_DATE_TIME_COLUMN);
        int columnDayIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.DAY_COLUMN);
        int columnRepeatEveryIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.REPEAT_EVERY_COLUMN);
        int columnRuleByIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.RULE_TYPE_COLUMN);
        int columnRuleItemIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.RULE_ITEM_COLUMN);
        int columnRuleAboveAmountIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.RULE_ABOVE_AMOUNT_COLUMN);
        int columnRuleToAmountIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.RULE_TO_AMOUNT_COLUMN);
        int columnRuleAmountNoLimitIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.RULE_AMOUNT_NO_LIMIT_COLUMN);
        int columnDiscountByAmountIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.DISCOUNT_BY_COLUMN);
        int columnDiscountValueIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.DISCOUNT_VALUE_COLUMN);

        int columnByDateOfWeekIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.BY_DAY_OF_WEEK_COLUMN);
        int columnOccurrenceCalendarMonthsIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.OCCURRENCE_CALENDAR_MONTHS_COLUMN);

        int columnVersionIndex = c.getColumnIndex(Schema.VERSION_COLUMN);
        int columnUpdatedDate = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.DATE_COLUMN);

        int columnOccurrenceCalendarDayIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.OCCURRENCE_CALENDAR_DAY_COLUMN);
        int columnExpirationDateIndex = c.getColumnIndex(Schema.DataTable_PromotionUpdateLog.EXPIRATION_DATE_COLUMN);

        c.moveToFirst();
        while(!c.isAfterLast())
        {

            PromotionObject po = new PromotionObject();
            po.SetTitle(c.getString(columnTitleIndex));
            po.SetId(c.getLong(columnIdIndex));
            po.SetStartDateTime(c.getLong(columnFromDateIndex));
            po.SetEndDateTime(c.getLong(columnToDateIndex));
            po.SetOccurDay(c.getInt(columnDayIndex));
            po.SetAlternateOccurrence(Enum.OccurrenceWeek.values()[c.getInt(columnRepeatEveryIndex)]);
            po.SetRule(Enum.PromotionByType.values()[c.getInt(columnRuleByIndex)]);
            po.SetRuleItemString(c.getString(columnRuleItemIndex));
            po.SetStartingAmount(c.getString(columnRuleAboveAmountIndex));
            po.SetUpperLimitAmount(c.getString(columnRuleToAmountIndex));
            po.SetUpperLimitFlag(c.getInt(columnRuleAmountNoLimitIndex) == 1);
            po.SetDiscountType(Enum.DiscountType.values()[c.getInt(columnDiscountByAmountIndex)]);
            po.SetDiscountValue(c.getDouble(columnDiscountValueIndex));



            po.SetPromotionDateOption(Enum.PromotionDateOption.values()[c.getInt(columnByDateOfWeekIndex)]);
            po.SetOccurMonth(c.getInt(columnOccurrenceCalendarMonthsIndex));
            po.SetDayOfMonth(c.getString(columnOccurrenceCalendarDayIndex));

            po.SetVersion(c.getInt(columnVersionIndex));
            po.SetExpirationDate(c.getLong(columnExpirationDateIndex));
            po.SetUpdatedDate(c.getLong(columnUpdatedDate));
            if(!newItems.containsKey(po.GetId()))
            {
                newItems.put(po.GetId(),new ArrayList<PromotionObject>());
            }
            newItems.get(po.GetId()).add(po);



            c.moveToNext();
        }
        c.close();

        return newItems;
    }
    private ArrayList<PromotionObject> ReadPromotionObjects(Cursor c)
    {
        ArrayList<PromotionObject>newItems= new ArrayList<PromotionObject>();
        int columnTitleIndex=c.getColumnIndex(Schema.DataTable_Promotion.TITLE_COLUMN);
        int columnIdIndex=c.getColumnIndex(Schema.DataTable_Promotion.ID_COLUMN);
        int columnFromDateIndex = c.getColumnIndex(Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN);
        int columnToDateIndex = c.getColumnIndex(Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN);
        int columnDayIndex = c.getColumnIndex(Schema.DataTable_Promotion.DAY_COLUMN);
        int columnRepeatEveryIndex = c.getColumnIndex(Schema.DataTable_Promotion.REPEAT_EVERY_COLUMN);
        int columnRuleByIndex = c.getColumnIndex(Schema.DataTable_Promotion.RULE_TYPE_COLUMN);
        int columnRuleItemIndex = c.getColumnIndex(Schema.DataTable_Promotion.RULE_ITEM_COLUMN);
        int columnRuleAboveAmountIndex = c.getColumnIndex(Schema.DataTable_Promotion.RULE_ABOVE_AMOUNT_COLUMN);
        int columnRuleToAmountIndex = c.getColumnIndex(Schema.DataTable_Promotion.RULE_TO_AMOUNT_COLUMN);
        int columnRuleAmountNoLimitIndex = c.getColumnIndex(Schema.DataTable_Promotion.RULE_AMOUNT_NO_LIMIT_COLUMN);
        int columnDiscountByAmountIndex = c.getColumnIndex(Schema.DataTable_Promotion.DISCOUNT_BY_COLUMN);
        int columnDiscountValueIndex = c.getColumnIndex(Schema.DataTable_Promotion.DISCOUNT_VALUE_COLUMN);
        int columnColorIndex = c.getColumnIndex(Schema.DataTable_Promotion.COLOR_COLUMN);
        int columnActiveFlagIndex = c.getColumnIndex(Schema.DataTable_Promotion.ACTIVE_FLAG_COLUMN);
        int columnInactiveDateIndex = c.getColumnIndex(Schema.DataTable_Promotion.INACTIVE_DATE_COLUMN);
        int columnByDateOfWeekIndex = c.getColumnIndex(Schema.DataTable_Promotion.BY_DAY_OF_WEEK_COLUMN);
        int columnOccurrenceCalendarMonthsIndex = c.getColumnIndex(Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_MONTHS_COLUMN);
        int columnOccurrenceCalendarDayIndex = c.getColumnIndex(Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_DAY_COLUMN);
        int columnExpirationDateIndex = c.getColumnIndex(Schema.DataTable_Promotion.EXPIRATION_DATE_COLUMN);
        int columnVersionIndex = c.getColumnIndex(Schema.VERSION_COLUMN);
        int columnCreatedDate = c.getColumnIndex(Schema.DataTable_Promotion.PROMOTION_CREATED_DATE_COLUMN);
        while(!c.isAfterLast())
        {

                PromotionObject po = new PromotionObject();
                po.SetTitle(c.getString(columnTitleIndex));
                po.SetId(c.getLong(columnIdIndex));
                po.SetStartDateTime(c.getLong(columnFromDateIndex));
                po.SetEndDateTime(c.getLong(columnToDateIndex));
                po.SetOccurDay(c.getInt(columnDayIndex));
                po.SetAlternateOccurrence(Enum.OccurrenceWeek.values()[c.getInt(columnRepeatEveryIndex)]);
                po.SetRule(Enum.PromotionByType.values()[c.getInt(columnRuleByIndex)]);
                po.SetRuleItemString(c.getString(columnRuleItemIndex));
                po.SetStartingAmount(c.getString(columnRuleAboveAmountIndex));
                po.SetUpperLimitAmount(c.getString(columnRuleToAmountIndex));
                po.SetUpperLimitFlag(c.getInt(columnRuleAmountNoLimitIndex) == 1);
                po.SetDiscountType(Enum.DiscountType.values()[c.getInt(columnDiscountByAmountIndex)]);
                po.SetDiscountValue(c.getDouble(columnDiscountValueIndex));
                //iterate through the color to find the correct enum value
                int savedColorValue = c.getInt(columnColorIndex);
                for(Enum.DiscountColor dc : Enum.DiscountColor.values())
                {
                    if(dc.value==savedColorValue)
                    {
                        po.SetDiscountColor(dc);
                        break;
                    }
                }

                po.SetActiveFlag(c.getInt(columnActiveFlagIndex) == 1);
                po.SetInactiveDate(c.getLong(columnInactiveDateIndex));
                po.SetPromotionDateOption(Enum.PromotionDateOption.values()[c.getInt(columnByDateOfWeekIndex)]);
                po.SetOccurMonth(c.getInt(columnOccurrenceCalendarMonthsIndex));
                po.SetDayOfMonth(c.getString(columnOccurrenceCalendarDayIndex));

                po.SetVersion(c.getInt(columnVersionIndex));
                po.SetExpirationDate(c.getLong(columnExpirationDateIndex));
                po.SetCreatedDate(c.getLong(columnCreatedDate));
                newItems.add(po);



            c.moveToNext();
        }
        c.close();

        return newItems;
    }
    public ArrayList<PromotionObject> GetCurrentPromotionByTotal()
    {

        ArrayList<PromotionObject>results = new ArrayList<PromotionObject>();
        //PromotionObject[] promotions = LoadPromotions(startTime,startTime,false);
        ArrayList<Long>resultIds = GetDistinctPromotionIds(Calendar.getInstance().get(Calendar.DATE),Calendar.getInstance().get(Calendar.MONTH)+1,Calendar.getInstance().get(Calendar.YEAR),false);
        for(long id:resultIds)
        {
            results.add(Get(id));
        }
        for(int i=results.size()-1;i>=0;i--)
        {
            if(results.get(i).byType== Enum.PromotionByType.item) {
                results.remove(i);
            }
            else
            {
                //check time component to see whether still valid
                Calendar cEnd = new GregorianCalendar();
                cEnd.setTimeInMillis(results.get(i).GetEndDateTime());
                Calendar cStart = new GregorianCalendar();
                cStart.setTimeInMillis(results.get(i).GetStartDateTime());
                int startHr =cStart.get(Calendar.HOUR_OF_DAY);
                int startMin =cStart.get(Calendar.MINUTE);
                int endHr =cEnd.get(Calendar.HOUR_OF_DAY);
                int endMin = cEnd.get(Calendar.MINUTE);
                int currentHr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int currentMin = Calendar.getInstance().get(Calendar.MINUTE);
                if(endHr==currentHr && currentMin>endMin)
                {
                    results.remove(i);
                }
                else if(endHr<currentHr)
                {
                    results.remove(i);
                }
                else if(startHr>currentHr)
                {
                    results.remove(i);
                }
                else if(startHr==currentHr && startMin>currentHr)
                {
                    results.remove(i);
                }

               /* if(!((endHr==currentHr && currentMin<=endMin) ||
                        //((endHr>currentHr )||(endHr==currentHr&&endMin>=currentMin) && startHr>currentHr) ||
                        (endHr>currentHr && startHr<currentHr) ||
                        (startHr==currentHr && startMin<=currentMin))
                        ) {
                    results.remove(i);
                }*/
            }
        }


        return results;
    }
    private HashMap<Long,ArrayList<PromotionObject>> LoadPreviousVersionOfPromotions(long startTime,long endTime)
    {
       //query all the promotions update after end time and before start time
        common.Utility.LogActivity("query promotion in change log start time "+startTime + " and end time "+endTime);


        DatabaseHelper helper = new DatabaseHelper(context);
        String strSortOrder=Schema.DataTable_PromotionUpdateLog.ID_COLUMN+","+Schema.DataTable_PromotionUpdateLog.DATE_COLUMN+ " asc";

        String strWhereClause="("+
                "("+Schema.DataTable_PromotionUpdateLog.FROM_DATE_TIME_COLUMN+" <=? and "
                +Schema.DataTable_PromotionUpdateLog.TO_DATE_TIME_COLUMN+" >=?) or "
                +"("+Schema.DataTable_PromotionUpdateLog.FROM_DATE_TIME_COLUMN+"<=? and "
                +Schema.DataTable_PromotionUpdateLog.TO_DATE_TIME_COLUMN+"<=?) or "
                +"("+Schema.DataTable_PromotionUpdateLog.FROM_DATE_TIME_COLUMN+">=? and "
                +Schema.DataTable_PromotionUpdateLog.TO_DATE_TIME_COLUMN+"<=?) or "
                +"("+Schema.DataTable_PromotionUpdateLog.FROM_DATE_TIME_COLUMN+">=? and "
                +Schema.DataTable_PromotionUpdateLog.TO_DATE_TIME_COLUMN+">=?)"
                +")";
        String[] args=new String[8];
        int index=0;
        for(int i=0;i<4;i++) {
            args[index++] = startTime + "";
            args[index++] = endTime + "";
        }

        Cursor c = helper.query(Schema.DataTable_PromotionUpdateLog.TABLE_NAME, Schema.DataTable_PromotionUpdateLog.GetColumnNames(), strWhereClause, args, strSortOrder);
        HashMap<Long,ArrayList<PromotionObject>> results = ReadHistoricalPromotionObjects(c);
        helper.close();
        return results;
    }
    private PromotionObject[] LoadPromotions(long startDate,long endDate,boolean blnIncludeInactive)
    {
        ClearCache();
        common.Utility.LogActivity("query promotion between "+startDate+" and "+endDate);


        ArrayList<PromotionObject> newItems = new ArrayList<PromotionObject>();
        DatabaseHelper helper = new DatabaseHelper(context);
        String strSortOrder=Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN+ " asc";


        String strWhereClause="("+
                "("+Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN+" <=? and "
                +Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN+" >=?) or "
                /*+"("+Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN+"<=? and "
                +Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN+"<=?) or "*/
                +"("+Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN+">=? and "
                +Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN+"<=?) or "
                +"("+Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN+">=? and "
                +Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN+">=?)"
                +")"
                +((!blnIncludeInactive)?" and "+Schema.DataTable_Promotion.ACTIVE_FLAG_COLUMN+"=?":"");
               /* +((!blnIncludeInactive)?" and "+Schema.DataTable_Promotion.ACTIVE_FLAG_COLUMN+"=? and "
                +Schema.DataTable_Promotion.PROMOTION_CREATED_DATE_COLUMN+"<=?":"");*/


        //String[] args=new String[(!blnIncludeInactive)?9:8];
        String[] args=new String[(!blnIncludeInactive)?7:6];
        int index=0;
        for(int i=0;i<3;i++) {
            args[index++] = startDate + "";
            args[index++] = endDate + "";
        }
        if(!blnIncludeInactive) {
            args[6] = "1";
            //args[8] = "1";

        }




        Cursor c = helper.query(Schema.DataTable_Promotion.TABLE_NAME, Schema.DataTable_Promotion.GetColumnNames(), strWhereClause, args, strSortOrder);
        ArrayList<PromotionObject>results = ReadPromotionObjects(c);
        for(int i=0;i<results.size();i++)
        {
            //check existing record to see already existed
            if(!promotionRecords.containsKey(results.get(i).GetId()))
            {
                promotionRecords.put(results.get(i).GetId(), results.get(i));

            }

            newItems.add(results.get(i));
        }


        helper.close();

        PromotionObject[] arryResults =newItems.toArray(new PromotionObject[0]);
        return arryResults;
    }
    public PromotionObject Get(long Id)
    {
        return promotionRecords.get(Id);
    }
    public PromotionObject Get(long id,int version)
    {
        common.Utility.LogActivity("query promotion id "+id+" version "+version);


        PromotionObject po=null;
        DatabaseHelper helper = new DatabaseHelper(context);
        String strSortOrder="";


        String strWhereClause=Schema.DataTable_Promotion.ID_COLUMN+"=? and "+Schema.VERSION_COLUMN+"=?";


        String[] args=new String[]{id+"",version+""};





        Cursor c = helper.query(Schema.DataTable_Promotion.TABLE_NAME, Schema.DataTable_Promotion.GetColumnNames(), strWhereClause, args, strSortOrder);
        ArrayList<PromotionObject>results = ReadPromotionObjects(c);
        c.close();
        if(results.size()==0)
        {
            //couldn't find in current promotion table, now search log table
            strWhereClause=Schema.DataTable_PromotionUpdateLog.ID_COLUMN+"=? and "+Schema.VERSION_COLUMN+"=?";
            c = helper.query(Schema.DataTable_PromotionUpdateLog.TABLE_NAME,Schema.DataTable_PromotionUpdateLog.GetColumnNames(),strWhereClause,args,strSortOrder);
            HashMap<Long,ArrayList<PromotionObject>> hmResult =ReadHistoricalPromotionObjects(c);
            if(hmResult.containsKey(id))
            {
                po = hmResult.get(id).get(0);
            }
            c.close();
        }
        else {
            po = results.get(0);
        }

        helper.close();


        return po;
    }
    public PromotionObject[] GetCompletedPromotionsForCurrentReceipts(ArrayList<Receipt> receipts)
    {
        long now =Calendar.getInstance().getTimeInMillis();
        //update order time for item so that it will synchronize all the same type item in order to apply promotion next time
        //for example, a burger has been ordered at 12pm and 1 hour later 1Pm the same person order another burger
        //and the there is promotion for buying two burgers with half price starting at 12.30pm
        //this is for keeping promotion item after the receipt is partially paid.
        for(Receipt r:receipts)
        {
            for(StoreItem si:r.myCart.GetItems())
            {
                si.orderTime = now;
            }
        }
        return LoadPromotions(now,now,false);
    }
    public ArrayList<PromotionAwarded> GetAllAvailableByItemPromotionAwarededForReceipts(ArrayList<Receipt> receipts)
    {
        return CheckDiscountByItemPromotionsAtCurrentMoment(receipts);
    }
    public ArrayList<PromotionAwarded> CheckDiscountByItemPromotions(ArrayList<Receipt> receipts, int receiptIndex,MyCart myCart)
    {
        boolean blnFound;
        PromotionAwarded promotionAwarded;

        common.Utility.LogActivity("get a list of promotion from a series given receipts");
        if(receiptIndex==-1) {
            receipts.clear();
            receipts.add(common.Utility.CreateNewReceiptObject(""));
            receipts.get(0).myCart = myCart;
        }

        //check not current promotion if partial receipt has been processed

        ArrayList<PromotionAwarded> promotionAwardeds =CheckDiscountByItemPromotionsAtCurrentMoment(receipts);

        receiptIndex=(receiptIndex==-1)?0:receiptIndex;//update receipt index to zero after processed with copied list during CheckPromotion()

        /**insert promotion awarded for all receipts**/
        for(int a =0;a<receipts.size();a++)
        {
            for(int i = promotionAwardeds.size()-1; i>-1; i--)
            {
                promotionAwarded = promotionAwardeds.get(i);
                blnFound = false;

                for(long key: promotionAwarded.collectedItems.keySet())
                {
                    if(promotionAwarded.collectedItems.get(key).containsKey(a))
                    {
                        blnFound = true;
                        break;
                    }

                }

                if(!blnFound)
                {
                    promotionAwardeds.remove(i);
                }
            }
        }





        return promotionAwardeds;
    }
    private boolean HavingSameReceiptIndexes(PromotionAwarded pa1,PromotionAwarded pa2)
    {
        if(pa1.ShareByHowManyReceipts()!=pa2.ShareByHowManyReceipts())return false;

        for(long id:pa1.collectedItems.keySet())
        {
            HashMap<Integer,Integer>lst1 = pa1.collectedItems.get(id);
            HashMap<Integer,Integer>lst2 = pa2.collectedItems.get(id);
            if(lst1.size()!=lst2.size())return false;

            for(int receiptIndex:lst1.keySet())
            {
                if(!lst2.containsKey(receiptIndex))return false;
            }
        }

        return true;
    }
    private void MergePromotionAwarded(ArrayList<PromotionAwarded>promotionAwardeds)
    {
        boolean blnMerged = true;
        int unit=0;
        while(blnMerged)
        {
            blnMerged = false;
            for(int i=0;i<promotionAwardeds.size()-1;i++)
            {
                /**Merge if both have the same promotion id and same receipt index only**/
                if(promotionAwardeds.get(i).promotionObject.GetId()==promotionAwardeds.get(i+1).promotionObject.GetId()
                        && HavingSameReceiptIndexes(promotionAwardeds.get(i),promotionAwardeds.get(i+1)))
                {
                    //append the item details over to another
                    for(long key:promotionAwardeds.get(i+1).collectedItems.keySet())
                    {
                        for(int receiptIndex:promotionAwardeds.get(i+1).collectedItems.get(key).keySet()) {
                            HashMap<Integer,Integer> tbl = promotionAwardeds.get(i).collectedItems.get(key);
                            unit = promotionAwardeds.get(i+1).collectedItems.get(key).get(receiptIndex);
                            if(tbl.containsKey(receiptIndex))
                            {

                                tbl.put(receiptIndex,tbl.get(receiptIndex)+unit);
                            }
                            else
                            {
                                tbl.put(receiptIndex,unit);
                            }
                        }
                    }
                    blnMerged = true;

                    promotionAwardeds.get(i).unit+=promotionAwardeds.get(i+1).unit;
                    promotionAwardeds.remove(i+1);
                    break;
                }
            }
        }
    }
    public boolean IsPromotionChanged() {
        return false;
    }
    public boolean IsItemHasPromotionAtTheMoment(ArrayList<StoreItem> storeItems) {
        PromotionObject[] pos = LoadPromotions(Calendar.getInstance().getTimeInMillis(),Calendar.getInstance().getTimeInMillis(),false);

        //filter out promotion by item only
        ArrayList<PromotionObject> promotionsByItem = FilterOutCashValueCombo(pos);
        for(int i=0;i<storeItems.size();i++)
        for(PromotionObject po:promotionsByItem) {
            if(po.ContaintItemId(storeItems.get(i).item.getID()))return true;
        }

        return false;
    }
    public boolean IsItemHasPromotionAtTheMoment(long lngItemId) {
        PromotionObject[] pos = LoadPromotions(Calendar.getInstance().getTimeInMillis(),Calendar.getInstance().getTimeInMillis(),false);

        //filter out promotion by item only
        ArrayList<PromotionObject> promotionsByItem = FilterOutCashValueCombo(pos);

        for(PromotionObject po:promotionsByItem) {
            if(po.ContaintItemId(lngItemId))return true;
        }

        return false;
    }
    public boolean IsPromotionValid(PromotionObject po,long lngTimeToCheck)
    {
        Calendar cCompare = new GregorianCalendar();
        cCompare.setTimeInMillis(lngTimeToCheck);
        int currentHr = cCompare.get(Calendar.HOUR_OF_DAY);
        int currentMinute = cCompare.get(Calendar.MINUTE);
        boolean blnTake =false;
        Calendar cNextPromotionEnd = new GregorianCalendar();
        Calendar cNextPromotionStart = new GregorianCalendar();
        cNextPromotionStart.setTimeInMillis(po.GetStartDateTime());

        cNextPromotionStart.set(Calendar.SECOND,0);
        cNextPromotionEnd.setTimeInMillis(po.GetEndDateTime());

        cNextPromotionEnd.set(Calendar.SECOND,0);
        int startHr=cNextPromotionStart.get(Calendar.HOUR_OF_DAY);
        int startMin = cNextPromotionStart.get(Calendar.MINUTE);
        int endHr=cNextPromotionEnd.get(Calendar.HOUR_OF_DAY);
        int endMin = cNextPromotionEnd.get(Calendar.MINUTE);
        if(endHr==23 && endMin==59)
        {
            endHr=24;
            endMin =0;
        }
        if (currentHr > startHr &&  (currentHr<endHr || (currentHr==endHr && currentMinute<=endMin) ))
        {
            blnTake = true;
        }
        else if(currentHr==startHr && currentMinute>=startMin &&
                (currentHr<endHr || (currentHr==endHr && currentMinute<=endMin))
                )
        {
            blnTake = true;
        }


        return blnTake;
    }
public boolean IsPromotionValidAtTheMoment(PromotionObject po)
{

    //int currentHr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    //int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

    return IsPromotionValid(po,Calendar.getInstance().getTimeInMillis());
    /*boolean blnTake =false;
    Calendar cNextPromotionEnd = new GregorianCalendar();
    Calendar cNextPromotionStart = new GregorianCalendar();
    cNextPromotionStart.setTimeInMillis(po.GetStartDateTime());

    cNextPromotionStart.set(Calendar.SECOND,0);
    cNextPromotionEnd.setTimeInMillis(po.GetEndDateTime());

    cNextPromotionEnd.set(Calendar.SECOND,0);
    int startHr=cNextPromotionStart.get(Calendar.HOUR_OF_DAY);
    int startMin = cNextPromotionStart.get(Calendar.MINUTE);
    int endHr=cNextPromotionEnd.get(Calendar.HOUR_OF_DAY);
    int endMin = cNextPromotionEnd.get(Calendar.MINUTE);
    if(endHr==23 && endMin==59)
    {
        endHr=24;
        endMin =0;
    }
    if (currentHr > startHr &&  (currentHr<endHr || (currentHr==endHr && currentMinute<=endMin) ))
    {
        blnTake = true;
    }
    else if(currentHr==startHr && currentMinute>=startMin &&
            (currentHr<endHr || (currentHr==endHr && currentMinute<=endMin))
            )
    {
        blnTake = true;
    }


    return blnTake;*/
}
    public void CopiedReceiptList(ArrayList<Receipt>copiedLst,ArrayList<Receipt>originalLst)
    {
        for(int i=0;i<originalLst.size();i++)
        {
            copiedLst.add((Receipt) originalLst.get(i).clone());
        }
    }
    private ArrayList<PromotionAwarded> CheckDiscountByItemPromotions(ArrayList<Receipt> receipts,long startTime,long endTime)
    {

        ArrayList<Receipt>copiedReceipts = new ArrayList<Receipt>();


        //make a copied
        CopiedReceiptList(copiedReceipts,receipts);

        //clear before query
        ClearCache();
        //filter out discount for total amount type, only interested in item combo
        //for reloading the partial paid receipts, you have to include inactive promotion just in case user delete it recently
        allPromotionsForCurrentListingReceipt = LoadPromotions(startTime,endTime,false);//keeping a class level copied
        PromotionObject[] queriedPromotions = allPromotionsForCurrentListingReceipt.clone();
        ArrayList<PromotionObject> promotions = FilterOutCashValueCombo(queriedPromotions);


        //sort by highest discount value
        SortHighestDiscountFirstForItemPromotion(promotions);



        ArrayList<PromotionAwarded> promotionAwardeds =  CollectReceiptPromotions(promotions,copiedReceipts);



        ClearCache();
        return promotionAwardeds;
    }
    private HashMap<Long,Long> GetPromotionCreatedDate(Long[] ids)
    {
        HashMap<Long,Long>dates = new HashMap<Long, Long>();

        String strIds ="";
        for(int i=0;i<ids.length;i++)
            strIds+=ids[i].toString()+",";

        common.Utility.LogActivity("query promotion created date for id's "+strIds);

        if(ids.length==0)return dates;


        strIds = strIds.substring(0,strIds.length()-1);


        DatabaseHelper helper = new DatabaseHelper(context);
        String strSortOrder=Schema.DataTable_Promotion.ID_COLUMN+ " asc";


        String strWhereClause=Schema.DataTable_Promotion.ID_COLUMN+" in(?)";

        String[] args=new String[]{strIds};





        Cursor c = helper.query(Schema.DataTable_Promotion.TABLE_NAME, Schema.DataTable_Promotion.GetColumnNames(), strWhereClause, args, strSortOrder);
        int intIdColumn = c.getColumnIndex(Schema.DataTable_Promotion.ID_COLUMN);
        int intCreatedDateColumn = c.getColumnIndex(Schema.DataTable_Promotion.PROMOTION_CREATED_DATE_COLUMN);
        c.moveToFirst();
        while(!c.isAfterLast())
        {
            dates.put(c.getLong(intIdColumn),c.getLong(intCreatedDateColumn));
            c.moveToNext();
        }

        c.close();

        helper.close();
        return dates;
    }
    private ArrayList<PromotionObject> SwapOutChangesPromotions(HashMap<Long,ArrayList<PromotionObject>> oldPromotions,ArrayList<PromotionObject>promotions
    ,ArrayList<Receipt> receipts,HashMap<Long,Long>createdDates)
    {
        ArrayList<PromotionObject> finalVersionPromotions = new ArrayList<PromotionObject>();
        HashMap<Long,ArrayList<PromotionObject>>collectedPOs = new HashMap<Long, ArrayList<PromotionObject>>();
        PromotionObject currentPo;
        MyCart myCart;
        boolean blnTake=false;
        long orderDateTime =receipts.get(0).myCart.GetItems().get(0).orderTime;
        //collect old promotion
        for(int i=0;i<receipts.size();i++)
        {
            myCart = receipts.get(i).myCart;

            //check previous promotion and see whether we have match
            for(long poId:oldPromotions.keySet())
            {
                blnTake = false;
                ArrayList<PromotionObject> lst = oldPromotions.get(poId);
                for(int j = 0;j<lst.size();j++)
                {
                    //stop immediately if the record created date is greater than item order date
                    //we only interest the changes greater than order date, so check the immediate greater updated time and smaller created date
                    if(lst.get(j).GetUpdatedDate()>myCart.GetItems().get(0).orderTime &&
                           createdDates.get(lst.get(j).GetId())<myCart.GetItems().get(0).orderTime)
                    {
                        currentPo = lst.get(j);
                        if(IsPromotionValid(currentPo,orderDateTime))
                        {
                            collectedPOs.put(currentPo.GetId(),new ArrayList<PromotionObject>());
                            collectedPOs.get(currentPo.GetId()).add(currentPo);
                            blnTake = true;
                        }

                    }

                    if(blnTake)break;
                }


            }


        }

        //now collect any current promotion if it doesn't collected before in old
        for(int i=0;i<receipts.size();i++) {

            for(PromotionObject po:promotions)
            {
                //proceed if no same promotion collected before
                if(!collectedPOs.containsKey(po.GetId()))
                {
                    //here is the 'real' promotion created time
                    if(IsPromotionValid(po,orderDateTime) && po.GetCreatedDate()<receipts.get(i).myCart.GetItems().get(0).orderTime)
                    {
                        collectedPOs.put(po.GetId(),new ArrayList<PromotionObject>());
                        collectedPOs.get(po.GetId()).add(po);
                    }
                }
            }
        }

        //now we only need one version of each of promotion
        for(long id:collectedPOs.keySet())
        {
            finalVersionPromotions.add(collectedPOs.get(id).get(0));
        }

        return finalVersionPromotions;
    }
    public ArrayList<PromotionAwarded> CollectReceiptPromotions(ArrayList<PromotionObject> promotions,
                                                ArrayList<Receipt>copiedReceipts )
    {
        PromotionObject po;
        int currentCopiedReceiptIndex;
        StoreItem si2;
        ArrayList<PromotionAwarded> promotionAwardeds = new ArrayList<PromotionAwarded>();
        ArrayList<PromotionAwarded> collectedPromotionAwarded = new ArrayList<PromotionAwarded>();
        for(int i=0;i<promotions.size();i++)
        {
            //skip if is inactive
            if(!promotions.get(i).blnIsActive)continue;

            po = promotions.get(i);

            collectedPromotionAwarded.clear();
            for (int j = 0; j < copiedReceipts.size(); j++)
            {
                currentCopiedReceiptIndex = j;
                ArrayList<StoreItem> siLst = copiedReceipts.get(j).myCart.GetItems();


                for (int k = 0; k < siLst.size(); k++) {
                    si2 = siLst.get(k);

                    //only item ordered timestamp in range will be proceed
                    if (IsPromotionValidAtTheMoment(po))
                    {
                        FillExistingAndCreateMultipleComboObject(collectedPromotionAwarded,si2,po,
                                currentCopiedReceiptIndex,copiedReceipts.get(j).myCart.GUID);
                    }

                }

            }
            //now collect all the completed combo from the processed list and release those incomplete back
            //to it belongs
            CollectCompletedCombo(collectedPromotionAwarded, promotionAwardeds,copiedReceipts);

        }

        return promotionAwardeds;
    }
    private ArrayList<PromotionAwarded> CheckDiscountByItemPromotionsAtCurrentMoment(ArrayList<Receipt> receipts)
    {
        long now =Calendar.getInstance().getTimeInMillis();
        //update order time for item so that it will synchronize all the same type item in order to apply promotion next time
        //for example, a burger has been ordered at 12pm and 1 hour later 1Pm the same person order another burger
        //and the there is promotion for buying two burgers with half price starting at 12.30pm
        //this is for keeping promotion item after the receipt is partially paid.
        for(Receipt r:receipts)
        {
            for(StoreItem si:r.myCart.GetItems())
            {
                si.orderTime = now;
            }
        }
        return CheckDiscountByItemPromotions(receipts,now,now);
    }
    private ArrayList<PromotionAwarded> CheckDiscountByItemPromotionsWithItemOrderedTimeStamp(ArrayList<Receipt> receipts)
    {

        //collect date range where the item orders have been placed
        long start = -1;
        long end=-1;
        for(Receipt r:receipts)
        {
            for (StoreItem si :r.myCart.GetItems()) {
                if (start == -1) {
                    start = si.orderTime;
                }
                if (end == -1) {
                    end = si.orderTime;
                }
                if (start > si.orderTime) {
                    start = si.orderTime;
                }
                if (end < si.orderTime) {
                    end = si.orderTime;
                }
            }
        }

        return CheckDiscountByItemPromotions(receipts,start,end);

    }

    private void SortByStartingPriceForPricePromotion(ArrayList<PromotionObject> lst)
    {
        boolean blnSwap=true;
        while(blnSwap)
        {
            blnSwap = false;
            for(int i=0;i<lst.size()-1;i++)
            {
                if(lst.get(i).bdFromAmount.floatValue()>lst.get(i+1).bdFromAmount.floatValue())
                {
                    PromotionObject po = lst.remove(i);
                    lst.add(i+1,po);
                    blnSwap = true;
                }
            }
        }
    }

    private void CollectCompletedCombo(ArrayList<PromotionAwarded> lst, ArrayList<PromotionAwarded> lstPromotionAwarded, ArrayList<Receipt> copiedReceipts)
    {
        PromotionAwarded promotionAwarded;
        long key;
        int key2;
        for(int i=lst.size()-1;i>-1;i--)
        {
            promotionAwarded = lst.get(i);
            //if(promotionAwarded.itemRequire.size()>0)
            if(!promotionAwarded.IsCompletelyFilled())
            {
                //incomplete,release the resources
                while(promotionAwarded.collectedItems.size()>0)
                {
                    key = promotionAwarded.collectedItems.keySet().iterator().next();

                    HashMap<Integer,Integer> resource = promotionAwarded.collectedItems.get(key);

                    while(resource.size()>0)
                    {
                        key2 = resource.keySet().iterator().next();
                        ArrayList<StoreItem>receiptSILst =copiedReceipts.get(key2).myCart.GetItems();
                        for(int j=0;j<receiptSILst.size();j++)
                        {
                            if(receiptSILst.get(j).item.getID()==key)
                            {
                                receiptSILst.get(j).UnitOrder+=resource.get(key2);
                                resource.put(key2,0);
                            }
                        }
                        resource.remove(key2);
                    }

                    promotionAwarded.collectedItems.remove(key);
                }

            }
            else
            {
                //complete
                lstPromotionAwarded.add(promotionAwarded);
                //remove it from the list after added into promotion awarded
                lst.remove(i);
            }
        }
    }
    /**
     * the combo list will contain the same pass in promotion object only for each call
     * and will release any incomplete combo back to the store item list if couldn't
     * form a complete combo, thus allow the new promotion to use
     * **/
    private void FillExistingAndCreateMultipleComboObject(ArrayList<PromotionAwarded>lst, StoreItem si, PromotionObject po, int receiptIndex,String strGUID)
    {

        //int unitAvailable = si.UnitOrder;
        long currentItemId = si.item.getID();
        long categoryId = si.item.getParentID();
        //check the combo list whether is there any spot to fill with this item
        for(PromotionAwarded promotionAwarded :lst)
        {
            //if(!promotionAwarded.itemRequire.isEmpty() && si.UnitOrder>0)//skip fulfilled
            if(!promotionAwarded.IsCompletelyFilled() && si.UnitOrder>0)//skip fulfilled
            si.UnitOrder = promotionAwarded.FillUnit(currentItemId,si.UnitOrder,receiptIndex,si.item.ParentID,strGUID);
        }

        //use the remaining unit to create  combo object until the unit available become zero
        //promotion id=-1 is not bond to a particular promotion(input via combo selection)
        for(int i=0;i<po.ruleItems.size();i++) {
           if (po.ruleItems.get(i).containsKey(currentItemId) //&& (si.promotionId == -1 || si.promotionId == po.GetId())
                   || po.ruleItems.get(i).containsKey(categoryId)) {

               while (si.UnitOrder > 0) {
                   //si.boundId = boundId;
                   PromotionAwarded promotionAwarded = new PromotionAwarded(po);
                   si.UnitOrder = promotionAwarded.FillUnit(currentItemId, si.UnitOrder, receiptIndex,categoryId,strGUID);
                   lst.add(promotionAwarded);
               }

           }

        }


    }

    private boolean IsInPromotionTime(PromotionObject po,StoreItem si)
    {
        common.Utility.LogActivity("check item is in promotion "+ po.GetId());
        Calendar cItem = new GregorianCalendar();
        cItem.setTimeInMillis(si.orderTime);

        if(po.dateOption== Enum.PromotionDateOption.once )
        {
            return IsInOncePromotionTime(po,cItem);
        }
        else if(po.dateOption== Enum.PromotionDateOption.day)
        {
            return IsInDayPromotionTime(po,cItem);
        }
        else if(po.dateOption== Enum.PromotionDateOption.month)
        {
            return IsInMonthPromotionTime(po,cItem);
        }

        return false;
    }
    private boolean IsInMonthPromotionTime(PromotionObject po,Calendar cItem)
    {

        Enum.Month month;
        //get ordered item month
        switch (cItem.get(Calendar.MONTH))
        {
            case Calendar.JANUARY:
                month = Enum.Month.Jan;
                break;
            case Calendar.FEBRUARY:
                month = Enum.Month.Feb;
                break;
            case Calendar.MARCH:
                month = Enum.Month.Mar;
                break;
            case Calendar.APRIL:
                month = Enum.Month.Apr;
                break;
            case Calendar.MAY:
                month = Enum.Month.May;
                break;
            case Calendar.JUNE:
                month = Enum.Month.Jun;
                break;
            case Calendar.JULY:
                month = Enum.Month.Jul;
                break;
            case Calendar.AUGUST:
                month = Enum.Month.Aug;
                break;
            case Calendar.SEPTEMBER:
                month = Enum.Month.Sep;
                break;
            case Calendar.OCTOBER:
                month = Enum.Month.Oct;
                break;
            case Calendar.NOVEMBER:
                month = Enum.Month.Nov;
                break;
            default:
                //december
                month = Enum.Month.Dec;
            break;
        }
        if(po.CheckOccurMonth(month))
        {
            String[] days=po.GetDayOfMonth().split(",");
            int itemDate =cItem.get(Calendar.DATE);
            for(int i=0;i<days.length;i++)
            {
                if(itemDate==Integer.parseInt(days[i]))
                {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean IsInDayPromotionTime(PromotionObject po,Calendar cItem)
    {
        Calendar cStart = new GregorianCalendar();
        cStart.setTimeInMillis(po.GetStartDateTime());
        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(po.GetEndDateTime());
        Calendar tempCal = new GregorianCalendar();
        tempCal.setTimeInMillis(cStart.getTimeInMillis());
        int firstDayOfWeek = tempCal.getFirstDayOfWeek();//reset to 1st day of week(Saturday in US)
        boolean blnFirst = true;
        while(tempCal.getTimeInMillis()<cEnd.getTimeInMillis()) {
            Date d = new Date(tempCal.getTimeInMillis());
            //adjusting to the correct week


            if (firstDayOfWeek == tempCal.get(Calendar.DAY_OF_WEEK)) {
                //check is the 1st loop

                if (!blnFirst) {
                    if (po.GetOccurrence() == Enum.OccurrenceWeek.Weekly) {
                        //do nothing
                    } else if (po.GetOccurrence() == Enum.OccurrenceWeek.TwoWeek) {
                        tempCal.add(Calendar.DATE, 7);
                    } else if (po.GetOccurrence() == Enum.OccurrenceWeek.ThreeWeek) {
                        tempCal.add(Calendar.DATE, 14);
                    } else if (po.GetOccurrence() == Enum.OccurrenceWeek.Monthly) {
                        tempCal.add(Calendar.DATE, 21);
                    }
                }

            }
            blnFirst = false;
            if(tempCal.get(Calendar.DATE)==cItem.get(Calendar.DATE)
                    && tempCal.get(Calendar.MONTH)==cItem.get(Calendar.MONTH)
                    && tempCal.get(Calendar.YEAR)==cItem.get(Calendar.YEAR))
            {
                //check if promotion will happen today
                if(IsWeekPromotionOccur(tempCal,po))
                {
                    //will happen on today(target weekday)
                    //now check the time
                    cStart.set(cItem.get(Calendar.YEAR),cItem.get(Calendar.MONTH),cItem.get(Calendar.DATE),cStart.get(Calendar.HOUR_OF_DAY),cStart.get(Calendar.MINUTE),0);
                    cStart.set(Calendar.MILLISECOND,0);
                    cEnd.set(cItem.get(Calendar.YEAR),cItem.get(Calendar.MONTH),cItem.get(Calendar.DATE),cEnd.get(Calendar.HOUR_OF_DAY),cEnd.get(Calendar.MINUTE),59);
                    cEnd.set(Calendar.MILLISECOND,999);
                    if(cItem.getTimeInMillis()>=cStart.getTimeInMillis() && cItem.getTimeInMillis()<=cEnd.getTimeInMillis())
                    {
                        return true;
                    }
                }
            }
            else
            {
                if(tempCal.getTimeInMillis()>cItem.getTimeInMillis())return false;
            }

            tempCal.add(Calendar.DATE, 1);

        }




            return false;
    }
    private boolean IsInOncePromotionTime(PromotionObject po,Calendar cItem)
    {
        Calendar cStart = new GregorianCalendar();
        cStart.setTimeInMillis(po.GetStartDateTime());
        Calendar cEnd = new GregorianCalendar();
        cEnd.setTimeInMillis(po.GetEndDateTime());
        Date dStart = new Date(cStart.getTimeInMillis());
        Date dEnd = new Date(cEnd.getTimeInMillis());
        Date ditem = new Date(cItem.getTimeInMillis());
        long start = cStart.getTimeInMillis();
        long end = cEnd.getTimeInMillis();
        long item = cItem.getTimeInMillis();
        if(cItem.getTimeInMillis()>=cStart.getTimeInMillis()
                && cItem.getTimeInMillis()<=cEnd.getTimeInMillis())
        {
            return true;
        }
        return false;
    }
    private void InsertIntoCashItemPromotionSortedArrayList(ArrayList<PromotionObject>lst, PromotionObject po)
    {
        if(lst.size()==0) {
            lst.add(po);
            return;
        }
        for(int j=0;j<lst.size();j++)
        {
            if(lst.get(j).dblDiscountValue>po.dblDiscountValue)//is in negative value
            {
                lst.add(j,po);
                return;
            }
        }
        lst.add(po);
    }
    private void InsertIntoPercentageItemPromotionSortedArrayList(ArrayList<Duple<BigDecimal,PromotionObject>>lst,PromotionObject po)
    {
        BigDecimal tempBD=GetHighestMoneyValue(po);
        if(lst.size()==0) {
            lst.add(new Duple<BigDecimal, PromotionObject>(tempBD,po));
            return;
        }
        for(int j=0;j<lst.size();j++)
        {
            if(lst.get(j).GetFirst().compareTo(tempBD)==1)//is in negative value, so need to be smaller
            {
                lst.add(j,new Duple<BigDecimal, PromotionObject>(tempBD,po));
                return;
            }
        }
        lst.add(new Duple<BigDecimal, PromotionObject>(tempBD,po));
    }
    private BigDecimal GetHighestMoneyValue(PromotionObject po)
    {
        BigDecimal highest;
        BigDecimal total=null;
        for(int i=0;i<po.ruleItems.size();i++)
        {
            highest=null;
            HashMap<Long,Integer>map =po.ruleItems.get(i);
            for(Long key:map.keySet())
            {
                ItemObject io = common.myMenu.GetLatestItem(key);
                if(io==null)
                {
                    io = GetTheMostExpensiveItemInTheGroup(key);
                    if(io==null)
                    {
                        //user hasn't defined any item for this category
                        io =new ItemObject(-1,"dummy",key,"0","",true,0,1);
                    }
                }
                if(highest==null) {
                    highest = io.getPrice().multiply(new BigDecimal(map.get(key)));
                }
                else
                {
                    BigDecimal tempBD = io.getPrice().multiply(new BigDecimal(map.get(key)));
                    if(tempBD.compareTo(highest)==1)
                    {
                        highest = tempBD;
                    }
                }

            }
            total=(total==null)?highest:highest.add(total);

        }

        return total.multiply(new BigDecimal(po.GetDiscountValue()));
    }
    private ItemObject GetTheMostExpensiveItemInTheGroup(long parentId)
    {
        ArrayList<ItemObject> items =common.myMenu.GetCategoryItems(parentId,true);
        ItemObject mostExpensiveItem=null;
        for(ItemObject io:items)
        {
            if(mostExpensiveItem==null)
            {
                mostExpensiveItem = io;
            }
            else
            {
               if(mostExpensiveItem.getPrice().longValue()<io.getPrice().longValue())
               {
                   mostExpensiveItem = io;
               }
            }
        }

        return mostExpensiveItem;
    }
    public void SortHighestDiscountFirstForItemPromotion(ArrayList<PromotionObject> lst)
    {
        ArrayList<PromotionObject>cashPromotion = new ArrayList<PromotionObject>();
        ArrayList<Duple<BigDecimal,PromotionObject>>percentagePromotion = new ArrayList<Duple<BigDecimal,PromotionObject>>();
        for(int i=0;i<lst.size();i++)
        {
            if(lst.get(i).discountType== Enum.DiscountType.percentage)
            {
                InsertIntoPercentageItemPromotionSortedArrayList(percentagePromotion,lst.get(i));
            }
            else
            {
                InsertIntoCashItemPromotionSortedArrayList(cashPromotion,lst.get(i));
            }
        }

        lst.clear();
        while(cashPromotion.size()>0 || percentagePromotion.size()>0)
        {
            if(cashPromotion.size()>0 || percentagePromotion.size()==0)
            {
                lst.add(cashPromotion.remove(0));
            }
            else if(cashPromotion.size()==0 || percentagePromotion.size()>0)
            {
                lst.add(percentagePromotion.remove(0).GetSecond());
            }
            else
            {
                if(cashPromotion.get(0).dblDiscountValue<percentagePromotion.get(0).GetFirst().doubleValue())//negative value comparison
                {
                    lst.add(cashPromotion.remove(0));
                }
                else
                {
                    lst.add(percentagePromotion.remove(0).GetSecond());
                }
            }
        }

    }
    public ArrayList<PromotionObject> FilterOutCashValueCombo(PromotionObject[] promotions)
    {
        common.Utility.LogActivity("filter out cash value combo");
        return FilterPromotion(promotions,Enum.PromotionByType.item);
       /* ArrayList<PromotionObject>newLst = new ArrayList<PromotionObject>();
        for(int i=0;i<promotions.length;i++)
        {
            if(promotions[i].GetRule()== Enum.PromotionByType.item)
            {
                newLst.add(promotions[i]);
            }
        }
        return newLst;*/
    }
    private ArrayList<PromotionObject>FilterOutPromotionByItemCombo(PromotionObject[] promotions)
    {
        common.Utility.LogActivity("filter out item combination combo");
        return FilterPromotion(promotions,Enum.PromotionByType.total);
    }
    private ArrayList<PromotionObject>FilterPromotion(PromotionObject[] promotions,Enum.PromotionByType promotionType)
    {

        ArrayList<PromotionObject>newLst = new ArrayList<PromotionObject>();
        for(int i=0;i<promotions.length;i++)
        {
            if(promotions[i].GetRule()== promotionType)
            {
                newLst.add(promotions[i]);
            }
        }
        return newLst;
    }

}
