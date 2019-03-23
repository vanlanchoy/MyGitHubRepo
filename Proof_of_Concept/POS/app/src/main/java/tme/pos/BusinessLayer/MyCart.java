package tme.pos.BusinessLayer;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.internal.LinkedTreeMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

import javax.mail.Store;

/**
 * Created by kchoy on 10/9/2014.
 */
public class MyCart implements Parcelable,Cloneable{

    private ArrayList<CartDisplayItem>sortedAndCombinedItemsForDisplay;
    private ArrayList<StoreItem> items;//raw, with immutable order
    //private ArrayList<PromotionAwarded>promotionsAwarded;
    public float percentage=0;
    public String tableId;
    public String GUID="";
    public int receiptIndex=0;
    public PromotionObject promotionObject;//for promotion by price
    public PromotionAwarded promotionAwarded;
    public boolean blnIsLock=false;
    public MyCart(float flPercentage,String tableId,int receiptIndex)
    {
        items=new ArrayList<StoreItem>();
        //promotionsAwarded = new ArrayList<PromotionAwarded>();
        sortedAndCombinedItemsForDisplay = new ArrayList<CartDisplayItem>();
        this.percentage = flPercentage;
        this.tableId = tableId;
        this.receiptIndex = receiptIndex;
        this.promotionAwarded = null;
        this.GUID = java.util.UUID.randomUUID().toString();

    }
    public ArrayList<CartDisplayItem> GetDisplayCartItemList(){return sortedAndCombinedItemsForDisplay;}

    public ArrayList<StoreItem> GetItems()
    {
        return items;
    }
    public void SetPromotionByPrice(PromotionObject po){promotionObject = po;}
    public PromotionObject GetPromotionByTotalPrice(){return promotionObject;}
    public void SetItems(ArrayList<StoreItem> lst){items=lst;}
    public int GetTotalUnitCount()
    {
        int count=0;
        for(StoreItem si:items)
        {
            count+=si.UnitOrder;

        }

        return count;
    }
    public BigDecimal GetPromotionByCashAmount()
    {
        if(promotionObject==null) return new BigDecimal(0);

        BigDecimal bd=new BigDecimal((promotionObject.GetDiscountType()== Enum.DiscountType.cash)?promotionObject.GetDiscountValue():100*promotionObject.GetDiscountValue());
        return bd;
    }
    public BigDecimal GetAmountAfterPromotionByCashDiscount()
    {
        return getAmount().add(GetPromotionByCashAmount());
    }
    public BigDecimal getAmount()
    {
        BigDecimal amount = new BigDecimal(0);

        for(StoreItem si:items)
        {
            BigDecimal bdUnit = new BigDecimal(si.UnitOrder);
            amount = amount.add(si.item.getPrice().multiply(bdUnit));
            for(ModifierObject mo:si.modifiers)
            {
                amount=amount.add(mo.getPrice().multiply(bdUnit));
            }
        }


        //for(PromotionAwarded pa:promotionsAwarded)
        for(CartDisplayItem d:sortedAndCombinedItemsForDisplay)
        {
            if(d.cit== Enum.CartItemType.PromotionAwarded) {
                PromotionAwarded pa =d.pa;
                //PromotionAwarded pa = (PromotionAwarded) d.GetSecond();
                amount = amount.add(pa.GetTotalDiscountAwarded((receiptIndex == pa.GetSharedReceiptIndex().get(0)) ? true : false,receiptIndex));
            }
        }


        return amount;
    }
    public void UpdateReceiptIndex(int receiptIndex){this.receiptIndex = receiptIndex;}
    public void UpdateUnitOrder(int itemIndex,int additionalUnitToAdd)
    {
        if(items.size()-1>=itemIndex) {
            items.get(itemIndex).UnitOrder += additionalUnitToAdd;
            //CheckPromotions();
        }
    }
    public String ReturnPromotionToSQLString()
    {
        if(promotionObject==null)
        {
            return "";
        }
        //promotion Id;version #
        return promotionObject.GetId()+";"+promotionObject.GetCurrentVersionNumber();
    }
    public void RemoveAllStoreItems()
    {
        items.clear();
        sortedAndCombinedItemsForDisplay.clear();
        //CheckPromotions();
    }
    public void RemoveStoreItem(int index)
    {
        if(items.size()-1>=index)
        {
            items.remove(index);
            //CheckPromotions();
        }
    }
    public void RemoveStoreItem(StoreItem si)
    {
        items.remove(si);
        //CheckPromotions();
    }
    public void AddStoreItem(StoreItem si)
    {
        items.add(si);
        //CheckPromotions();
    }
    /*private void CheckPromotions()
    {
        common.myCartManager.UpdateOfferedComboList(tableId);
    }*/
    public BigDecimal getTaxAmount()
    {
        BigDecimal bdPercentage = new BigDecimal(percentage);
        BigDecimal bdAmount = getAmount();
        BigDecimal bdZero = new BigDecimal(0f);
        if(bdAmount.compareTo(new BigDecimal(0f))==-1)return bdZero;
        return bdPercentage.multiply(getAmount()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal()
    {
        return getAmount().add(getTaxAmount());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        StoreItem[] tempArry = new StoreItem[items.size()];
        items.toArray(tempArry);
        parcel.writeParcelableArray(tempArry, i);
        parcel.writeFloat(percentage);
        parcel.writeString(tableId);
        parcel.writeInt(receiptIndex);
        parcel.writeParcelable(promotionAwarded,i);
        parcel.writeString(GUID);
        parcel.writeInt(blnIsLock?1:0);
        /**allowed real time to check promotions**/
        /*CartDisplayItem[] cartDisplayItemArry = new CartDisplayItem[sortedAndCombinedItemsForDisplay.size()];
        sortedAndCombinedItemsForDisplay.toArray(cartDisplayItemArry);
        parcel.writeParcelableArray(cartDisplayItemArry,i);*/

    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MyCart createFromParcel(Parcel in) {
            return new MyCart(in);
        }

        public MyCart[] newArray(int size) {
            return new MyCart[size];
        }
    };
    public MyCart(Parcel in)
    {
        this.items = new ArrayList<StoreItem>();
        Parcelable[] parcelableArray = in.readParcelableArray(StoreItem.class.getClassLoader());
        if(parcelableArray!=null)
        {
            for(int i=0;i<parcelableArray.length;i++)
                items.add((StoreItem)parcelableArray[i]);
        }

        this.percentage = in.readFloat();
        this.tableId = in.readString();


        this.receiptIndex = in.readInt();
        this.promotionAwarded = in.readParcelable(PromotionAwarded.class.getClassLoader());
        this.GUID = in.readString();
        this.blnIsLock = (in.readInt()==0?false:true);
        this.sortedAndCombinedItemsForDisplay = new ArrayList<CartDisplayItem>();
       /**allowed real time to check promotions**/
       /* Parcelable[] cartDisplayItemArray = in.readParcelableArray(CartDisplayItem.class.getClassLoader());
        if(cartDisplayItemArray!=null)
        {
            for(int i=0;i<cartDisplayItemArray.length;i++)
                sortedAndCombinedItemsForDisplay.add((CartDisplayItem)cartDisplayItemArray[i]);
        }*/

    }

    @Override
    public Object clone() {
        MyCart mc = new MyCart(percentage,tableId,receiptIndex);
        ArrayList<StoreItem> tempSI = new ArrayList<StoreItem>();
        for(int i=0;i<items.size();i++)
        {
            tempSI.add((StoreItem) items.get(i).clone());
        }
        mc.items = tempSI;
        mc.tableId = tableId;

        mc.sortedAndCombinedItemsForDisplay = new ArrayList<CartDisplayItem>(sortedAndCombinedItemsForDisplay);
        mc.receiptIndex = receiptIndex;
        mc.promotionAwarded = promotionAwarded;
        mc.GUID = GUID;
        mc.blnIsLock = blnIsLock;
        return mc;
    }
}
