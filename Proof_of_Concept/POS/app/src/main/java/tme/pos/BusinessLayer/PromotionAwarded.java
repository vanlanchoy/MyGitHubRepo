package tme.pos.BusinessLayer;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kchoy on 5/10/2016.
 */
public class PromotionAwarded implements Parcelable{
    public HashMap<Long,HashMap<Integer,Integer>>collectedItems;
    public HashMap<Integer,String>cart_GUID_Associate_ReceiptID;
    ArrayList<HashMap<Long,Integer>>itemRequireMaps;
    Boolean[] filledStatus;
    //ArrayList<HashMap<Long,Integer>>referenceAssignedMaps;
    public PromotionObject promotionObject;
    public int unit=1;
    public PromotionAwarded(PromotionObject po)
    {

        promotionObject = po;

        Instantiate();
    }
    public PromotionAwarded(Parcel in)
    {
        this.itemRequireMaps =(ArrayList<HashMap<Long,Integer>>)in.readSerializable();
        Bundle bundle = in.readBundle();
        collectedItems=(HashMap<Long,HashMap<Integer,Integer>>)bundle.getSerializable("collectedItems");
        filledStatus = (Boolean[])in.readArray(Boolean.class.getClassLoader());
        promotionObject = in.readParcelable(PromotionObject.class.getClassLoader());
        unit = in.readInt();
        cart_GUID_Associate_ReceiptID =(HashMap<Integer,String>)bundle.getSerializable("cart_GUID_Associate_ReceiptID");

    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeSerializable(itemRequireMaps);

        Bundle bundle = new Bundle();
        bundle.putSerializable("collectedItems",collectedItems);
        parcel.writeBundle(bundle);

        parcel.writeArray(filledStatus);

        parcel.writeParcelable(promotionObject,i);
        parcel.writeInt(unit);
        bundle = new Bundle();
        bundle.putSerializable("cart_GUID_Associate_ReceiptID",cart_GUID_Associate_ReceiptID);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PromotionAwarded createFromParcel(Parcel in) {
            return new PromotionAwarded(in);
        }

        public PromotionAwarded[] newArray(int size) {
            return new PromotionAwarded[size];
        }
    };

    private void Instantiate()
    {
        collectedItems = new HashMap<Long,HashMap<Integer,Integer>>();
        itemRequireMaps = new ArrayList<HashMap<Long,Integer>>();
        filledStatus = new Boolean[promotionObject.ruleItems.size()];
        for(int i=0;i<filledStatus.length;i++)filledStatus[i]=false;
        cart_GUID_Associate_ReceiptID = new HashMap<Integer, String>();
        CreateRequiredItemMap();

    }
    public ArrayList<Integer> GetSharedReceiptIndex()
    {
        ArrayList<Integer> receiptIndexes;

        HashMap<Integer,Boolean>tempHM = new HashMap<Integer, Boolean>();
        for(long id:collectedItems.keySet())
        {
            for(int receiptIndex:collectedItems.get(id).keySet())
                tempHM.put(receiptIndex,true);
            //max =(max<collectedItems.get(id).size())?collectedItems.get(id).size():max;
        }
        receiptIndexes = new ArrayList<Integer>(tempHM.keySet());
        java.util.Collections.sort(receiptIndexes);


        //return tempHM.size();
        return receiptIndexes;
    }
    public boolean IsInCollectedMap(long itemId)
    {
        return collectedItems.containsKey(itemId);
    }
    public void CreateRequiredItemMap()
    {

        for(int k=0;k<promotionObject.ruleItems.size();k++)
        {
           itemRequireMaps.add(new HashMap<Long,Integer>(promotionObject.ruleItems.get(k)));
        }


    }
    public boolean IsCompletelyFilled()
    {
        for(Boolean b:filledStatus)
        {
            if(!b)return b;
        }
        return true;
    }
    public boolean IsFilled(int groupIndex)
    {
        return filledStatus[groupIndex];
    }
    public int FillUnit(long currentItemId,int unitAvailable,int receiptIndex,long categoryId,String strCartGUID)
    {
        int remainder=unitAvailable;
        int value =0;
        boolean blnIsCategory = false;
        int groupIndex = StillInRequireMaps(currentItemId);
        //skip if doesn't contained the item
        if(groupIndex==-1)return remainder;


        if(itemRequireMaps.get(groupIndex).containsKey(categoryId))
        {
            blnIsCategory = true;

        }

        while(groupIndex>-1 && unitAvailable>0) {
            if ((itemRequireMaps.get(groupIndex).containsKey(currentItemId) ||blnIsCategory) && !filledStatus[groupIndex]) {
                //get to fill quantity
                int toFill = 0;
                if(blnIsCategory)
                {
                    //look for the category id storing inside the require map
                    toFill =itemRequireMaps.get(groupIndex).get(categoryId);
                }
                else
                {
                    toFill = itemRequireMaps.get(groupIndex).get(currentItemId);
                }

                if (!collectedItems.containsKey(currentItemId)) {
                    collectedItems.put(currentItemId, new HashMap<Integer, Integer>());
                }
                if (toFill > unitAvailable) {
                    //only partially enough for current combo
                    if(blnIsCategory)
                    {
                        itemRequireMaps.get(groupIndex).put(categoryId, toFill - unitAvailable);//update the still needed quantity
                    }
                    else {
                        itemRequireMaps.get(groupIndex).put(currentItemId, toFill - unitAvailable);//update the still needed quantity
                    }

                    //this will store the actual item id instead of the category id if any
                    if (collectedItems.get(currentItemId).containsKey(receiptIndex)) {
                        value = collectedItems.get(currentItemId).get(receiptIndex);
                    }
                    value += unitAvailable;
                    remainder = 0;
                } else if (unitAvailable == toFill) {
                    //exactly same quantity needed
                    if(blnIsCategory)
                    {
                        itemRequireMaps.get(groupIndex).remove(categoryId);//remove it after fulfillment
                    }
                    else {
                        itemRequireMaps.get(groupIndex).remove(currentItemId);//remove it after fulfillment
                    }

                    filledStatus[groupIndex] = true;//update status
                    //this will store the actual item id instead of the category id if any
                    if (collectedItems.get(currentItemId).containsKey(receiptIndex)) {
                        value = collectedItems.get(currentItemId).get(receiptIndex);
                    }
                    value += unitAvailable;
                    remainder = 0;
                } else {

                    //exceed the needed amount
                    remainder = unitAvailable - toFill;
                    if(blnIsCategory)
                    {
                        itemRequireMaps.get(groupIndex).remove(categoryId);//remove it after fulfillment
                    }
                    else {
                        itemRequireMaps.get(groupIndex).remove(currentItemId);//remove it after fulfillment
                    }


                    filledStatus[groupIndex] = true;//update status
                    if (collectedItems.get(currentItemId).containsKey(receiptIndex)) {
                        value = collectedItems.get(currentItemId).get(receiptIndex);
                    }
                    value += toFill;
                }

                collectedItems.get(currentItemId).put(receiptIndex, value);
                cart_GUID_Associate_ReceiptID.put(receiptIndex,strCartGUID);
            }
            unitAvailable = remainder;
            groupIndex = StillInRequireMaps(currentItemId);
            if( unitAvailable>0 && groupIndex>-1) {
                if (itemRequireMaps.get(groupIndex).containsKey(categoryId)) {
                    blnIsCategory = true;

                }
            }
        }
        return remainder;
    }
    /*public boolean ItemNeededToCollect(int receiptIndex)
    {
        for(long itemId:collectedItems.keySet())
        {
            HashMap<Integer,Integer> record = collectedItems.get(itemId);
            //if(collectedItems.get(itemId).containsKey(receiptIndex)) {
            if(record.containsKey(receiptIndex)) {
                return true;
            }
        }

        return false;
    }*/
    public int StillInRequireMaps(long itemIdToCheck)
    {
        ItemObject io = common.myMenu.GetLatestItem(itemIdToCheck);
        //check for mapping item
        for(int i=0;i<itemRequireMaps.size();i++)
        {

            if(itemRequireMaps.get(i).containsKey(itemIdToCheck)  && !filledStatus[i])
            {
                return i;
            }
        }
        //check for any
        for(int i=0;i<itemRequireMaps.size();i++)
        {
            //check for if the item id storing in itemRequireMaps is actually a category also
            if( itemRequireMaps.get(i).containsKey(io.getParentID()) && !filledStatus[i])
            {
                return i;
            }
        }

        return -1;
    }
    public int ShareByHowManyReceipts()
    {
        //int max =1;
        HashMap<Integer,Boolean>tempHM = new HashMap<Integer, Boolean>();
        for(long id:collectedItems.keySet())
        {
            for(int receiptIndex:collectedItems.get(id).keySet())
            tempHM.put(receiptIndex,true);
            //max =(max<collectedItems.get(id).size())?collectedItems.get(id).size():max;
        }

        return tempHM.size();
    }
    public BigDecimal GetTotalComboPrice()
    {
        BigDecimal bdTotal =new BigDecimal(0);
        int unitCount=0;
        //collect the total amount
        for(long id: collectedItems.keySet())
        {
            unitCount=0;
            HashMap<Integer,Integer> map = collectedItems.get(id);
            for(int receiptIndex:map.keySet())
            {
                unitCount+=map.get(receiptIndex);
            }
            bdTotal =bdTotal.add(common.myMenu.GetLatestItem(id).getPrice().multiply(new BigDecimal(unitCount)));
        }

        return bdTotal;
    }
    public BigDecimal GetItemsTotalPriceForThisComboBeforeDiscount(int receiptIndex)
    {
        BigDecimal bdTotal =new BigDecimal(0);

        //collect the total amount
        for(long id: collectedItems.keySet())
        {
            HashMap<Integer,Integer> map = collectedItems.get(id);
            //BigDecimal bdPrice = common.myMenu.GetLatestItem(id).getPrice();
            if(map.containsKey(receiptIndex)) {
                //BigDecimal bdMul = new BigDecimal(map.get(receiptIndex));
                bdTotal = bdTotal.add(common.myMenu.GetLatestItem(id).getPrice().multiply(new BigDecimal(map.get(receiptIndex))));
            }
        }

        return bdTotal;
    }
    public BigDecimal GetItemsTotalPriceForThisComboBeforeDiscount()
    {
        //BigDecimal bdTotal = GetTotalComboPrice();
        BigDecimal bdTotal =new BigDecimal(promotionObject.GetDiscountValue());

        /*if(promotionObject.GetDiscountType()== Enum.DiscountType.cash)
        {
            //discount is store as negative value
            bdTotal =new BigDecimal(promotionObject.GetDiscountValue());
        }
        else
        {
            //bdTotal = new BigDecimal(promotionObject.GetDiscountValue()*100);
            bdTotal =bdTotal.multiply(new BigDecimal(promotionObject.GetDiscountValue()));
        }*/

        return bdTotal;
    }

    public BigDecimal GetTotalDiscountAwarded(boolean blnAddExtraPenny,int receiptIndex)
    {
        int intShareBy =ShareByHowManyReceipts();
        float flTotal =0;
        float dividedTotal =0;
        if(promotionObject.GetDiscountType()== Enum.DiscountType.percentage)
        {
            if(receiptIndex!=-1) {
                flTotal = GetItemsTotalPriceForThisComboBeforeDiscount(receiptIndex).floatValue();//*unit;
                double discountedValue = promotionObject.GetDiscountValue() * flTotal;//percentage doesn't needed to divide by number of receipt just total times discount %
                //discountedValue *=unit;
                dividedTotal = new BigDecimal(discountedValue).setScale(2, RoundingMode.HALF_UP).floatValue();
            }
            else
            {
                flTotal = GetTotalComboPrice().multiply(new BigDecimal(promotionObject.GetDiscountValue())).floatValue();//*unit;
                dividedTotal = flTotal;
            }
        }
        else
        {
            flTotal = GetItemsTotalPriceForThisComboBeforeDiscount().floatValue()*unit;
            dividedTotal = flTotal;
            if(receiptIndex!=-1)
            {
                dividedTotal = flTotal/intShareBy;
            }
        }


        String strDividedTotal = dividedTotal+"";
        if(strDividedTotal.contains("E")) {
            strDividedTotal = new BigDecimal(strDividedTotal).toPlainString();
        }
        int indexDecimalPt = strDividedTotal.indexOf(".");
        if(indexDecimalPt+3+1<=strDividedTotal.length() && promotionObject.GetDiscountType()== Enum.DiscountType.cash)
        {
            //remove the third decimal point
            if(indexDecimalPt==-1) {
                //do nothing
            }
            else {
                strDividedTotal = strDividedTotal.substring(0, indexDecimalPt + 3);
            }
            dividedTotal = Float.parseFloat(strDividedTotal);
        }

        //BigDecimal dbRoundDown = new BigDecimal(dividedTotal).setScale(2,RoundingMode.DOWN);//round down to 2 decimal point
        //float flAnswer = dbRoundDown.floatValue();

        /*check by multiplying the divided value with number of receipt whether is equal to flTotal,
            else add the remaining balance to the 1st receipt*/
        if((receiptIndex<0 ||blnAddExtraPenny) && promotionObject.GetDiscountType()== Enum.DiscountType.cash)
        {
            //float flTemp = flAnswer*intShareBy;
            float flTemp = dividedTotal*intShareBy;
            float flDiff = flTotal-flTemp;
            dividedTotal +=flDiff;
            //flAnswer+=flDiff;
        }

        //round it up if the final value still is greater than 2 decimal pts
        strDividedTotal = new BigDecimal(dividedTotal).toPlainString();
        indexDecimalPt = strDividedTotal.indexOf(".");
        if(indexDecimalPt>-1 && indexDecimalPt+3+1<=strDividedTotal.length())
        {
            return new BigDecimal(dividedTotal).setScale(2,RoundingMode.HALF_UP);
        }
        else {
            return new BigDecimal(dividedTotal);
        }
        //return new BigDecimal(flAnswer);

    }
    public String ToSQLString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[pa;"+unit+";"+promotionObject.GetId()+";"+promotionObject.GetCurrentVersionNumber());
        if(cart_GUID_Associate_ReceiptID.size()>0)sb.append(";");
        Integer[] lst = cart_GUID_Associate_ReceiptID.keySet().toArray(new Integer[cart_GUID_Associate_ReceiptID.size()]);
        for(int j=0;j<lst.length;j++)
        {
            sb.append(lst[j]+" "+cart_GUID_Associate_ReceiptID.get(lst[j]));
            if(j+1<lst.length)sb.append(",");
        }
//HashMap<Long,HashMap<Integer,Integer>>
        /**format
         * Promotion awarded [pa;unit;promotion object id;sub version;receipt index<space>cart GUID,receipt index<space>cart GUID;item id <space> receipt id <space> unit count,item id <space> receipt id <space> unit count]
         * **/

        sb.append(";");
        String strTemp="";
        for(long id:collectedItems.keySet())
        {
            HashMap<Integer,Integer> tempInnerHM = collectedItems.get(id);
            for(int receiptId:tempInnerHM.keySet())
            {
                strTemp+=id+" "+receiptId+" "+tempInnerHM.get(receiptId)+",";
            }
        }
        strTemp =(strTemp.length()>0)?strTemp.substring(0,strTemp.length()-1):"";


        sb.append(strTemp+"]");

        return sb.toString();
    }
}
