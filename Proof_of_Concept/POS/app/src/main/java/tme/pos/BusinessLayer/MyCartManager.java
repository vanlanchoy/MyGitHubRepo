package tme.pos.BusinessLayer;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import javax.mail.Store;

import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.R;

/**
 * Created by kchoy on 5/1/2015.
 */
public class MyCartManager {

    public HashMap<String,ArrayList<Receipt>> Receipts;
    public MyCartManager()
    {
        Receipts = new HashMap<String, ArrayList<Receipt>>();

    }
    private void RemoveCompletedReceipt()
    {
        boolean blnCompleted = true;
        ArrayList<String>removeKeys = new ArrayList<String>();
        for(String strKey:Receipts.keySet())
        {
            blnCompleted =true;
            ArrayList<Receipt>receipts = Receipts.get(strKey);
            for(int i=0;i<receipts.size();i++)
            {
                if(!receipts.get(i).blnHasPaid)
                {
                    blnCompleted = false;
                    break;
                }
            }
            if(blnCompleted)
            {
                removeKeys.add(strKey);
            }
        }

        for(String strKey:removeKeys)
        {
            Receipts.remove(strKey);
        }
    }
    public MyCart GetCart(String strTableId,int intCartIndex)
    {
        if(!Receipts.containsKey(strTableId) || (Receipts.get(strTableId).size()==0 && intCartIndex==0))
        {
            //create a cart object with this key if it doesn't existed
            //ArrayList<MyCart>carts = new ArrayList<MyCart>();
            MyCart cart = new MyCart(common.myAppSettings.GetTaxPercentage(),strTableId,0) ;
            //carts.add(cart);
            //Carts.put(strTableId,carts);
            String strTableLabel="";
            Duple[] tableLabels = common.floorPlan.GetTableLabels();
            for(int i=0;i<tableLabels.length;i++)
            {
                if(tableLabels[i].GetFirst()==strTableId)
                {
                    strTableLabel=(String)tableLabels[i].GetSecond();
                    break;
                }
            }
            //create a new receipt for this cart
            double[] location = new double[2];

            if(common.myLocationService!=null)
                common.myLocationService.GetLocationPts(location);


                     Receipt receipt = new Receipt(cart,
                    common.myAppSettings.GetTaxPercentage(),
                    common.companyProfile,
                    "",
                    "",
                    strTableLabel,
                    "",//Calendar.getInstance().getTimeInMillis()+"",
                    Enum.ReceiptNoteAlignment.left,
                    Enum.ReceiptNoteAlignment.left,
                    location[0],
                    location[1],
                     new Server(),false,1);
            ArrayList<Receipt> receipts = new ArrayList<Receipt>();
            receipts.add(receipt);
            Receipts.put(strTableId,receipts);
        }

        if(Receipts.get(strTableId).size()>intCartIndex)
            return Receipts.get(strTableId).get(intCartIndex).myCart;
        else
            return null;//return null page if doesn't have the target page index (maybe no split receipt)
    }
    public void RemoveEmptySplitReceipt()
    {
        //clean up empty receipt before saving it during crash
        for(String key:Receipts.keySet())
        {
            ArrayList<Receipt> lstReceipt = Receipts.get(key);

            for (int i = lstReceipt.size()-1;i>0; i--) {
                Receipt r = lstReceipt.get(i);
                if (r.myCart.GetItems().isEmpty())
                {
                    lstReceipt.remove(i);
                }

            }

            UpdateReceiptIndex(lstReceipt);
        }

        RemoveCompletedReceipt();
    }
    public void UpdateReceiptIndex(ArrayList<Receipt> receipts) {
        for(int i=0;i<receipts.size();i++)
        {
            receipts.get(i).myCart.UpdateReceiptIndex(i);
        }
    }
    public void RemoveReceipt(Receipt receipt,String strTableId)
    {
        if(Receipts.containsKey(strTableId))
        {
            ArrayList<Receipt> targetReceiptList =Receipts.get(strTableId);
            //do not remove if there is only one receipt in the list, need to keep it as default
            if(targetReceiptList.size()<2)return;
            for(int i=0;i<targetReceiptList.size();i++)
            {
                if(targetReceiptList.get(i)==receipt)
                {
                    targetReceiptList.remove(i);
                    UpdateReceiptIndex(targetReceiptList);
                    break;
                }
            }
        }
    }

    public void MergeSameItem()
    {
        for(String key:Receipts.keySet())
        {
            ArrayList<Receipt>lstReceipt = Receipts.get(key);
            for(int i=0;i<lstReceipt.size();i++)
            {
                Receipt r = lstReceipt.get(i);
                ArrayList<StoreItem>items=r.myCart.GetItems();
                for(int j=items.size()-1;j>-1;j--)
                {
                    for(int k=j-1;k>-1;k--)
                    {
                        if(items.get(j).IsSameOrderedItemExcludeUnitCount(items.get(k)))
                        {
                            //merge with current j, so that it won't loop through again
                            r.myCart.UpdateUnitOrder(j,items.get(k).UnitOrder);
                            r.myCart.RemoveStoreItem(k);
                            break;
                        }
                    }
                }
            }
        }

        //remove empty receipt
        String[] keys =new String[Receipts.keySet().size()];
        keys = Receipts.keySet().toArray(keys);
        //for(String key:Receipts.keySet())
        for(int j=keys.length-1;j>-1;j--)
        {
            String key = keys[j];
            ArrayList<Receipt>lstReceipt = Receipts.get(key);
            for(int i=lstReceipt.size()-1;i>-1;i--)
            {

                Receipt r = lstReceipt.get(i);
                if(r.myCart.GetItems().isEmpty())
                {
                    lstReceipt.remove(i);
                }
            }

            UpdateReceiptIndex(lstReceipt);
            //remove the entry in hash table so that the table label color will change to white
            if(Receipts.get(key).size()==0)Receipts.remove(key);
        }

        //trigger recreate for default receipt
        GetReceipt("",0);
    }
    public boolean GetTableStatus(String strId)
    {
        //for restaurant UI use, return true if there is item in this cart
        if(Receipts.containsKey(strId))
        {
            if(Receipts.get(strId).size()==1) {
                return (!Receipts.get(strId).get(0).myCart.GetItems().isEmpty()) ? true : false;
            }
            //definitely there is at least one item because it has split receipt
            return true;
        }
        return false;
    }
    public Duple<String,Boolean>[] GetTableStatuses()
    {
        ArrayList<Duple<String,Boolean>>list = new ArrayList<Duple<String,Boolean>>();
        Iterator<String> iter = Receipts.keySet().iterator();
        while(iter.hasNext())
        {
           String strKey = iter.next();
           list.add(new Duple<String, Boolean>(strKey,GetTableStatus(strKey)));
        }
        Duple<String,Boolean>[]result = new Duple[list.size()];
        return list.toArray(result);
    }

    public Receipt GetReceipt(String strTableId,int intReceiptIndex)
    {
        //trigger a create if key not found
        if(!Receipts.containsKey(strTableId))
        {
           GetCart(strTableId, 0);
        }

        if(Receipts.containsKey(strTableId))
        {
            if(Receipts.get(strTableId).size()>intReceiptIndex)
            {
                return Receipts.get(strTableId).get(intReceiptIndex);
            }
        }
        return null;
    }

    public void RemoveReceipt(String strTableId, int intReceiptIndex)
    {
        if(Receipts.containsKey(strTableId))
        {

            Receipts.get(strTableId).remove(intReceiptIndex);
            UpdateReceiptIndex(Receipts.get(strTableId));

        }

    }
    public void RemoveReceipts(String strTableId)
    {
        if(Receipts.containsKey(strTableId))
        {
            for(int i=Receipts.get(strTableId).size()-1;i>=0;i--) {
                Receipts.get(strTableId).get(i).blnHasPaid=false;
                //remove every receipt except the 1st one
                if(i!=0) {
                    Receipts.get(strTableId).remove(i);
                }
            }

            UpdateReceiptIndex(Receipts.get(strTableId));
            //clear the 1st receipt but don't remove it
            Receipts.get(strTableId).remove(0);

            Receipts.get(strTableId).add(common.Utility.CreateNewReceiptObject(strTableId));
            //Receipts.get(strTableId).get(0).myCart.RemoveAllStoreItems();//removing display cart list and store item list

            //Receipts.get(strTableId).get(0).myCart.blnIsLock=false;
            //Receipts.get(strTableId).get(0).blnHasPaid=false;

        }

    }



    public void AddNewReceipt(Receipt newReceipt,String strTableId)
    {
        Receipts.get(strTableId).add(newReceipt);

    }
    public ArrayList<Receipt>GetAllReceipts()
    {
        common.Utility.LogActivity("get all receipts");
        ArrayList<Receipt> rs = new ArrayList<Receipt>();
        for(String key:Receipts.keySet())
        {
            for(Receipt r:Receipts.get(key))
            {
                rs.add(r);
            }
        }

        return rs;
    }
    public ArrayList<Receipt> GetReceipts(String strTableId)
    {
        //trigger a create if key not found
        //or receipt count is zero, no default receipt
        common.Utility.LogActivity("get receipt by table id ["+strTableId+"]");
        if(!Receipts.containsKey(strTableId))
        {
            GetCart(strTableId, 0);
        }
        if(Receipts.get(strTableId)==null)
        {
            Receipts.put(strTableId,new ArrayList<Receipt>());
        }
        if(Receipts.get(strTableId).size()==0)
        {
            //remove and create new
            Receipts.remove(strTableId);
            GetCart(strTableId, 0);
        }
        return Receipts.get(strTableId);
    }
}
