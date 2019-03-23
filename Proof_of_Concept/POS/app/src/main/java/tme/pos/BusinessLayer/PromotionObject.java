package tme.pos.BusinessLayer;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Created by kchoy on 3/8/2016.
 */
public class PromotionObject implements Parcelable,Cloneable,Comparable<PromotionObject> {
    long lngID;
    Enum.PromotionByType byType;
    Enum.DiscountType discountType;
    int version;
    Enum.PromotionDateOption dateOption;
    public ArrayList<HashMap<Long,Integer>> ruleItems;
    Enum.DiscountColor color;
    String strTitle;
    String strDayOfMonth;
    long lngStartDateTime;
    long lngEndDateTime;
    long lngCreatedDateTime;
    long lngUpdatedDateTime;
    int intDays;
    int intMonths;
    Enum.OccurrenceWeek Occurrence;
    boolean blnUpperLimit;
    BigDecimal bdFromAmount;
    BigDecimal bdUpperAmount;

    boolean blnIsActive;
    double dblDiscountValue;
    long lngInactiveDate;
    long lngExpirationDate;
    long lngWeekMonthPromotionStartTime;
    long lngWeekMonthPromotionEndTime;

    public PromotionObject(Parcel in)
    {
        lngID=in.readLong();
        byType.value = in.readInt();
        discountType.value = in.readInt();
        dateOption.value=in.readInt();

        //Bundle bundle = in.readBundle();
        //ruleItems =(HashMap<Long,Integer>)bundle.getSerializable("map");
        /**convert each hash map bundle back to hash map **/
        //get the array list count 1st
        int size = in.readInt();
        //now read the serializable for N-th time
        for(int j=0;j<size;j++)
        {
            Bundle bundle = in.readBundle();
            ruleItems.add((HashMap<Long,Integer>)bundle.getSerializable("map"+j));
        }

        color.value=in.readInt();
        strTitle=in.readString();
        strDayOfMonth=in.readString();
        lngStartDateTime=in.readLong();
        lngEndDateTime=in.readLong();
        intDays=in.readInt();
        intMonths=in.readInt();
        Occurrence.value=in.readInt();
        blnUpperLimit=in.readInt()==1;
        bdFromAmount=new BigDecimal(in.readString());
        bdUpperAmount=new BigDecimal(in.readString());
        blnIsActive=in.readInt()==1;
        dblDiscountValue=in.readDouble();
        lngInactiveDate=in.readLong();
        lngExpirationDate=in.readLong();
        lngWeekMonthPromotionStartTime=in.readLong();
        lngWeekMonthPromotionEndTime=in.readLong();
        version = in.readInt();
        lngCreatedDateTime = in.readLong();
        lngUpdatedDateTime = in.readLong();
    }
    public PromotionObject(PromotionObject po)
    {
        byType = po.byType;

        ruleItems =MakeRuleItemsCopy(po.ruleItems);

        strTitle=po.GetTitle();
        lngStartDateTime=po.GetStartDateTime();
        lngEndDateTime=po.GetEndDateTime();

        intDays=po.intDays;
        lngID=po.GetId();
        Occurrence =po.GetOccurrence();
        bdUpperAmount =po.GetUpperLimitAmount();
        bdFromAmount =po.GetStartingAmount();

        dblDiscountValue = po.GetDiscountValue();
        discountType = po.GetDiscountType();
        color =po.GetDiscountColor();
        blnIsActive = po.IsActive();
        blnUpperLimit = po.blnUpperLimit;
        lngInactiveDate=po.GetInactiveDate();
        strDayOfMonth=po.GetDayOfMonth();
        lngExpirationDate=po.GetExpirationDate();

        lngWeekMonthPromotionEndTime=po.GetWeekMonthPromotionEndTime();
        lngWeekMonthPromotionStartTime=po.GetWeekMonthPromotionStartTime();
        version = po.version;
        lngCreatedDateTime = po.GetCreatedDate();
        lngUpdatedDateTime = po.GetUpdatedDate();
    }
    public PromotionObject()
    {
        byType = Enum.PromotionByType.item;
        discountType = Enum.DiscountType.cash;
        ruleItems = new ArrayList<HashMap<Long, Integer>>();

        strTitle="";
        lngStartDateTime=-1;
        lngEndDateTime=-1;

        intDays=0;
        lngID=-1;
        Occurrence = Enum.OccurrenceWeek.Weekly;
        bdUpperAmount = new BigDecimal("0");
        bdFromAmount = new BigDecimal("0");

        dblDiscountValue = 0;
        color = Enum.DiscountColor.discount_blue;
        blnIsActive = false;
        lngInactiveDate=-1;
        strDayOfMonth="";
        lngExpirationDate=-1;

        lngWeekMonthPromotionEndTime=0;
        lngWeekMonthPromotionStartTime=0;

        version = 1;
        lngCreatedDateTime=-1;
        lngUpdatedDateTime=-1;
    }
    public long GetUpdatedDate(){return lngUpdatedDateTime;}
    public void SetUpdatedDate(long ticks){lngUpdatedDateTime = ticks;}
    public int GetCurrentVersionNumber()
    {
        return version;
    }
    public void SetVersion(int version)
    {
        this.version = version;
    }
    public void IncreaseCurrentVersionNumber()
    {
        version++;
    }
    private ArrayList<HashMap<Long,Integer>>MakeRuleItemsCopy(ArrayList<HashMap<Long,Integer>>target)
    {
        ArrayList<HashMap<Long,Integer>>copied = new ArrayList<HashMap<Long, Integer>>();

        for(int i=0;i<target.size();i++)
        {
            copied.add(new HashMap<Long, Integer>(target.get(i)));
        }
        return copied;
    }
    public long GetWeekMonthPromotionStartTime()
    {
        return lngWeekMonthPromotionStartTime;
    }

    public long GetWeekMonthPromotionEndTime()
    {
        return lngWeekMonthPromotionEndTime;
    }
    public long GetCreatedDate(){if(lngCreatedDateTime==-1){lngCreatedDateTime=Calendar.getInstance().getTimeInMillis();}return lngCreatedDateTime;}
    public void SetCreatedDate(long ticks){lngCreatedDateTime = ticks;}
    public void SetExpirationDate(long date){lngExpirationDate=date;}
    public long GetExpirationDate(){return lngExpirationDate;}
    public void SetActiveFlag(boolean blnFlag){blnIsActive=blnFlag;}
    public boolean IsActive(){return blnIsActive;}
    public void SetInactiveDate(long lngDate){lngInactiveDate=lngDate;}
    public long GetInactiveDate(){return lngInactiveDate;}
    public void SetId(long id)
    {
        lngID = id;
    }
    public long GetId(){return lngID;}
    public void SetDiscountValue (double dblValue)
    {
        dblDiscountValue = dblValue;
    }
    public double GetDiscountValue(){return dblDiscountValue;}
    public void SetDiscountType(Enum.DiscountType dt)
    {
        discountType = dt;
    }
    public Enum.DiscountType GetDiscountType(){return discountType;}
    public void SetUpperLimitAmount(String strAmount)
    {
        bdUpperAmount = new BigDecimal(strAmount.replaceAll("[$,]",""));
    }
    public BigDecimal GetUpperLimitAmount(){return bdUpperAmount; }
    public void SetStartingAmount(String strAmount)
    {
        bdFromAmount = new BigDecimal(strAmount.replaceAll("[$,]",""));
    }
    public BigDecimal GetStartingAmount(){return bdFromAmount; }
    public void SetUpperLimitFlag(boolean blnFlag)
    {
        blnUpperLimit = blnFlag;
    }
    public boolean GetUpperLimitFlag(){
        return blnUpperLimit;
    }
    public void SetAlternateOccurrence(Enum.OccurrenceWeek ow)
    {
        Occurrence = ow;
    }
    public Enum.OccurrenceWeek GetOccurrence(){return Occurrence;}
    public void SetPromotionDateOption(Enum.PromotionDateOption value){dateOption=value;}
    public Enum.PromotionDateOption GetPromotionDateOption(){return dateOption;}
    public void SetDayOfMonth(String strValue){strDayOfMonth=strValue;}
    public String GetDayOfMonth(){return strDayOfMonth;}
    public void SetOccurMonth(int monthValue)
    {

        intMonths = monthValue;
    }
    public int GetOccurMonth()
    {
        return intMonths;
    }
    public boolean CheckOccurMonth(Enum.Month month){

        return ((intMonths & month.value)==month.value);
    }
    public void SetOccurDay(int dayValue)
    {

        intDays = dayValue;
    }
    public int GetOccurDay()
    {
        return intDays;
    }
    public boolean CheckOccurDay(Enum.Day day){

        return ((intDays & day.value)==day.value);
    }
    public void SetStartDateTime(long value)
    {
        lngStartDateTime = value;
    }
    public long GetStartDateTime(){return lngStartDateTime;}
    public void SetEndDateTime(long value)
    {
        lngEndDateTime = value;
    }
    public long GetEndDateTime(){return lngEndDateTime;}
    public void SetTitle(String strTitle)
    {
        this.strTitle = strTitle;
    }
    public String GetTitle(){return strTitle;}
    public void SetDiscountColor(Enum.DiscountColor dc)
    {
        color=dc;
    }
    public Enum.DiscountColor GetDiscountColor(){return color;}
    public void SetRule(Enum.PromotionByType byType)
    {
        this.byType = byType;
    }
    public Enum.PromotionByType GetRule()
    {
        return byType;
    }
    public void AddRuleItem(HashMap<Long,Integer>hashMap)
    {
        ruleItems.add(hashMap);

    }
    public void RemoveRuleItem(long id,int listIndex)
    {
        if(ruleItems.get(listIndex).containsKey(id))ruleItems.remove(id);
    }
    public int GetRuleItemCount()
    {
        return ruleItems.size();
    }
    public String GetAllRuleItemString()
    {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<ruleItems.size();i++)
            sb.append(GetRuleItemString(i));

        return sb.toString();
    }
    public String GetRuleItemString(int listIndex)
    {
        StringBuilder sb = new StringBuilder();
        HashMap<Long,Integer> tempHM=ruleItems.get(listIndex);
        for(Long key:tempHM.keySet())
        {
            sb.append(key+"|"+tempHM.get(key)+",");
        }
        String strTemp = sb.toString();
        if(strTemp.length()>0)strTemp = "("+strTemp.substring(0,strTemp.length()-1)+")";//remove the last comma
        return strTemp;
    }
    public boolean ContaintItemId(long lngItemId) {
        for(HashMap<Long,Integer> hm:ruleItems) {
            if(hm.containsKey(lngItemId))return true;
        }

        return false;
    }
    public void SetRuleItemString(String strValues)
    {
        ruleItems.clear();
        if(strValues.length()==0)return;
        strValues = strValues.substring(1,strValues.length()-1);
        String[] groups = strValues.split("\\)\\(");
        for(int j=0;j<groups.length;j++)
        {
            HashMap<Long,Integer>HM = new HashMap<Long, Integer>();
            ruleItems.add(HM);
            String[]values = groups[j].split(",");
            for(int i=0;i<values.length;i++)
            {
                String[] details = values[i].split("\\|");
                HM.put(Long.parseLong(details[0]),Integer.parseInt(details[1]));
            }
        }


    }
    public String GetStartTimeString()
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(lngStartDateTime);
        return ReturnTimeString(c);
    }
    public String GetEndTimeString()
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(lngEndDateTime);
        return ReturnTimeString(c);
    }
    private String ReturnTimeString(Calendar c)
    {
        String strMinute = (c.get(Calendar.MINUTE)>9?"":"0")+c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR)==0?c.get(Calendar.HOUR_OF_DAY):c.get(Calendar.HOUR);
        return hour+":"+strMinute+" "+((c.get(Calendar.AM_PM)==Calendar.AM)?"AM":"PM");
    }
    public String GetStartDateString()
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(lngStartDateTime);
        return ReturnDateString(c);
    }
    public String GetEndDateString()
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(lngEndDateTime);
        return ReturnDateString(c);
    }
    private String ReturnDateString(Calendar c)
    {
        Date date = new Date(c.getTimeInMillis());
        return new SimpleDateFormat("MM/dd/yyyy").format(date);
    }
    public String GetDateConditionString()
    {
        StringBuilder sb = new StringBuilder();
        if(dateOption== Enum.PromotionDateOption.day)
        {
            if((intDays & Enum.Day.Monday.value)== Enum.Day.Monday.value)
            {
                sb.append("Mon, ");
            }
            if((intDays & Enum.Day.Tuesday.value)== Enum.Day.Tuesday.value)
            {
                sb.append("Tue, ");
            }
            if((intDays & Enum.Day.Wednesday.value)== Enum.Day.Wednesday.value)
            {
                sb.append("Wed, ");
            }
            if((intDays & Enum.Day.Thursday.value)== Enum.Day.Thursday.value)
            {
                sb.append("Thu, ");
            }
            if((intDays & Enum.Day.Friday.value)== Enum.Day.Friday.value)
            {
                sb.append("Fri, ");
            }
            if((intDays & Enum.Day.Saturday.value)== Enum.Day.Saturday.value)
            {
                sb.append("Sat, ");
            }
            if((intDays & Enum.Day.Sunday.value)== Enum.Day.Sunday.value)
            {
                sb.append("Sun, ");
            }

            sb.replace(sb.length()-2,sb.length(),"");
            sb.append(" (every ");
            if(Occurrence==Enum.OccurrenceWeek.Weekly)
            {
                sb.append("Week)");
            }
            else if(Occurrence==Enum.OccurrenceWeek.TwoWeek)
            {
                sb.append(" 2-Week)");
            }
            else if(Occurrence==Enum.OccurrenceWeek.ThreeWeek)
            {
                sb.append("3-Week)");
            }
            else
            {
                sb.append("Month)");
            }
        }
        else if(dateOption== Enum.PromotionDateOption.once)
        {
            sb.append("Once");
        }
        else if(dateOption==Enum.PromotionDateOption.month)
        {
            String strMonths="";
            if((intMonths & Enum.Month.Jan.value)==Enum.Month.Jan.value)
            {
                strMonths ="Jan,";
            }
            if((intMonths & Enum.Month.Feb.value)==Enum.Month.Feb.value)
            {
                strMonths +="Feb,";
            }
            if((intMonths & Enum.Month.Mar.value)==Enum.Month.Mar.value)
            {
                strMonths +="Mar,";
            }
            if((intMonths & Enum.Month.Apr.value)==Enum.Month.Apr.value)
            {
                strMonths +="Apr,";
            }
            if((intMonths & Enum.Month.May.value)==Enum.Month.May.value)
            {
                strMonths +="May,";
            }
            if((intMonths & Enum.Month.Jun.value)==Enum.Month.Jun.value)
            {
                strMonths +="Jun,";
            }
            if((intMonths & Enum.Month.Jul.value)==Enum.Month.Jul.value)
            {
                strMonths +="Jul,";
            }
            if((intMonths & Enum.Month.Aug.value)==Enum.Month.Aug.value)
            {
                strMonths +="Aug,";
            }
            if((intMonths & Enum.Month.Sep.value)==Enum.Month.Sep.value)
            {
                strMonths +="Sep,";
            }
            if((intMonths & Enum.Month.Oct.value)==Enum.Month.Oct.value)
            {
                strMonths +="Oct,";
            }
            if((intMonths & Enum.Month.Nov.value)==Enum.Month.Nov.value)
            {
                strMonths +="Nov,";
            }
            if((intMonths & Enum.Month.Dec.value)==Enum.Month.Dec.value)
            {
                strMonths +="Dec,";
            }

            sb.append(strMonths.length()>0?strMonths.substring(0,strMonths.length()-1):"");//remove the last comma

            sb.append(System.getProperty("line.separator"));
            sb.append(GetDayOfMonth());

        }
        else
        {
                sb.append("n/a");
        }
        return sb.toString();
    }
    public String GetExpirationDateString()
    {
        /*if(blnWillExpire)
        {*/
            Calendar c = new GregorianCalendar();
            c.setTimeInMillis(lngExpirationDate);
            return ReturnDateString(c);
        /*}
        else
        {
            return "Never";
        }*/
    }
    public String GetRuleString()
    {
        HashMap<Long,Integer>hm=null;
        //int startIndex=0;
        StringBuilder sb = new StringBuilder();
        if(byType== Enum.PromotionByType.item)
        {
            for(int i=0;i<ruleItems.size();i++)
            {
                hm = ruleItems.get(i);
                sb.append("(");
                for(Long itemId:hm.keySet())
                {
                    ItemObject io =common.myMenu.GetLatestItem(itemId);
                    if(io!=null)
                    {
                        sb.append(hm.get(itemId)+"X "+io.getName()+" / ");
                    }
                    else
                    {
                        //check category
                        CategoryObject co = common.myMenu.GetCategory(itemId);
                        if(co!=null)
                        {
                            sb.append(hm.get(itemId)+"X "+co.getName()+" / ");
                            //sb.append(hm.get(itemId)+"X Any "+co.getName()+" / ");
                            //sb.append(hm.get(itemId)+"X "+co.getName()+" / ");
                        }
                        else {
                            sb.append("<DELETED> / ");
                        }
                    }
                }
                sb.append(")+");
            }

            if(sb.length()>1)
            {
                sb.replace(sb.length()-1, sb.length(), "");
            }


        }
        else
        {

            if(blnUpperLimit)
            {
                sb.append(common.Utility.ConvertBigDecimalToCurrencyFormat(GetStartingAmount()));
                sb.append(" to "+common.Utility.ConvertBigDecimalToCurrencyFormat(GetUpperLimitAmount()));
            }
            else
            {
                sb.append("from "+common.Utility.ConvertBigDecimalToCurrencyFormat(GetStartingAmount())+ " and above");
            }
        }
        return sb.toString().replace(" / )",")");
    }
    public String GetDiscountString()
    {
        if(discountType== Enum.DiscountType.cash)
        {
            return common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(dblDiscountValue));
        }
        else
        {
            //String p = (dblDiscountValue*100)+"";
            double temp = dblDiscountValue*100;
            //check the string already in two decimal pts else run the below check
            String p = temp+"";
            int indexDecimalPt = p.indexOf(".");
            if(indexDecimalPt>0 && (p.length()-indexDecimalPt>3))
            {
                BigDecimal bd = new BigDecimal(temp);
                //bd = bd.setScale(3,BigDecimal.ROUND_DOWN);
                bd = bd.setScale(3,BigDecimal.ROUND_HALF_UP);
                p = bd.floatValue()+"";
            }


            //remove the last trailing zero if is having 3 decimal points
            if(p.length()-p.indexOf(".")==4 && p.lastIndexOf("0")==p.length()-1)
            {
                p = p.substring(0,p.length()-1);
            }
            //return p;
            return p+"%";
        }
    }

    @Override
    public Object clone(){
        PromotionObject po = new PromotionObject();
        po.lngID=lngID;
        po.byType.value=byType.value;
        po.discountType.value=discountType.value;
        po.dateOption.value=dateOption.value;


        po.ruleItems= new ArrayList<HashMap<Long,Integer>>();
        //now loop through the list to copy it one by one
        for(int j=0;j<ruleItems.size();j++)
        {

            po.ruleItems.add((HashMap<Long, Integer>) ruleItems.get(j).clone());
        }

        po.color.value=color.value;
        po.strTitle=strTitle;
        po.strDayOfMonth=strDayOfMonth;
        po.lngStartDateTime=lngStartDateTime;
        po.lngEndDateTime=lngEndDateTime;
        po.intDays=intDays;
        po.intMonths=intMonths;
        po.Occurrence.value=Occurrence.value;
        po.blnUpperLimit=blnUpperLimit;
        po.bdFromAmount=bdFromAmount;
        po.bdUpperAmount=bdUpperAmount;
        po.blnIsActive=blnIsActive;
        po.dblDiscountValue=dblDiscountValue;
        po.lngInactiveDate=lngInactiveDate;
        po.lngExpirationDate=lngExpirationDate;
        po.lngWeekMonthPromotionStartTime=lngWeekMonthPromotionStartTime;
        po.lngWeekMonthPromotionEndTime=lngWeekMonthPromotionEndTime;
        po.version=version;
        po.lngCreatedDateTime=lngCreatedDateTime;
        po.lngUpdatedDateTime=lngUpdatedDateTime;
        return po;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeLong(lngID);
        parcel.writeInt(byType.value);
        parcel.writeInt(discountType.value);
        parcel.writeInt(dateOption.value);
        //Bundle bundle = new Bundle();
        //bundle.putSerializable("map", ruleItems);
        //parcel.writeBundle(bundle);
        /**convert each hash map object into bundle from the array list**/
        //get the array list count 1st
        parcel.writeInt(ruleItems.size());
        //now loop through the list to convert it one by one
        for(int j=0;j<ruleItems.size();j++)
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable("map"+j, ruleItems.get(j));
            parcel.writeBundle(bundle);
        }

        parcel.writeInt(color.value);
        parcel.writeString(strTitle);
        parcel.writeString(strDayOfMonth);
        parcel.writeLong(lngStartDateTime);
        parcel.writeLong(lngEndDateTime);
        parcel.writeInt(intDays);
        parcel.writeInt(intMonths);
        parcel.writeInt(Occurrence.value);
        parcel.writeInt(blnUpperLimit?1:0);
        parcel.writeString(bdFromAmount.toPlainString());
        parcel.writeString(bdUpperAmount.toPlainString());
        parcel.writeInt(blnIsActive?1:0);
        parcel.writeDouble(dblDiscountValue);
        parcel.writeLong(lngInactiveDate);
        parcel.writeLong(lngExpirationDate);
        parcel.writeLong(lngWeekMonthPromotionStartTime);
        parcel.writeLong(lngWeekMonthPromotionEndTime);
        parcel.writeInt(version);
        parcel.writeLong(lngCreatedDateTime);
        parcel.writeLong(lngUpdatedDateTime);
    }
    public static final Creator<PromotionObject> CREATOR = new Creator<PromotionObject>()
    {
        @Override
        public PromotionObject createFromParcel(Parcel parcel) {
            return new PromotionObject(parcel);
        }

        @Override
        public PromotionObject[] newArray(int i) {
            return new PromotionObject[i];
        }
    };

    @Override
    public int compareTo(PromotionObject po) {
        if(po.lngID!=lngID) return -1;
        if(po.byType.value!=byType.value)return -1;
        if(po.discountType.value!=discountType.value)return -1;
        if(po.dateOption.value!=dateOption.value)return -1;


        if(po.ruleItems.size()!=ruleItems.size())return -1;

        //now loop through the list to copy it one by one

        for(int j=0;j<ruleItems.size();j++)
        {

           HashMap<Long, Integer> HM = ruleItems.get(j);
           for(int i =0;i<po.ruleItems.size();i++) {
              HashMap<Long,Integer> compareHM = po.ruleItems.get(i);
              for(Long itemId:compareHM.keySet()) {
                if(!HM.containsKey(itemId))return -1;
                if(HM.get(itemId)!=compareHM.get(itemId))return -1;
              }
           }

        }

        if(po.color.value!=color.value)return -1;
        if(po.strTitle.compareTo(strTitle)!=0)return -1;
        if(po.strDayOfMonth.compareTo(strDayOfMonth)!=0)return -1;
        if(po.lngStartDateTime!=lngStartDateTime)return -1;
        if(po.lngEndDateTime!=lngEndDateTime)return -1;
        if(po.intDays!=intDays)return -1;
        if(po.intMonths!=intMonths)return -1;
        if(po.Occurrence.value!=Occurrence.value)return -1;
        if(po.blnUpperLimit!=blnUpperLimit)return -1;
        if(po.bdFromAmount!=bdFromAmount)return -1;
        if(po.bdUpperAmount!=bdUpperAmount)return -1;
        if(po.blnIsActive!=blnIsActive)return -1;
        if(po.dblDiscountValue!=dblDiscountValue)return -1;
        if(po.lngInactiveDate!=lngInactiveDate)return -1;
        if(po.lngExpirationDate!=lngExpirationDate)return -1;
        if(po.lngWeekMonthPromotionStartTime!=lngWeekMonthPromotionStartTime)return -1;
        if(po.lngWeekMonthPromotionEndTime!=lngWeekMonthPromotionEndTime)return -1;
        if(po.version!=version)return -1;
        if(po.lngCreatedDateTime!=lngCreatedDateTime)return -1;
        if(po.lngUpdatedDateTime!=lngUpdatedDateTime)return -1;
        return 0;
    }
}
