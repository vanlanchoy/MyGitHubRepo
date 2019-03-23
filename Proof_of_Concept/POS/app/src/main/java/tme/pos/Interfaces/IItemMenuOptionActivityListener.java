package tme.pos.Interfaces;

import java.util.ArrayList;

import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.StoreItem;

/**
 * Created by kchoy on 5/27/2016.
 */
public interface IItemMenuOptionActivityListener
{
    void UpdateCurrentCart(ArrayList<StoreItem> lst,ArrayList<Integer>originalOrderLstIndexes);
    void InsertNewItemIntoCurrentCart(ArrayList<StoreItem>lst,IToBeUpdatedInventoryView callbackView);
    void DialogDismissed();
    void EditItem(ItemObject io);
    void SetDialogPopupFlag(boolean blnIsShow);
    boolean IsReceiptPanelBusy();
}