package tme.pos.Interfaces;

import tme.pos.CustomViewCtr.MenuItemFlippableTableRow;
import tme.pos.ItemInventoryOptionDialog;

/**
 * Created by kchoy on 5/27/2016.
 */
public interface IPageActivityListener
{
    int AddNewUnitToCart(long itemId,int unit,int initUnitCount);
    void ShowItemOptionPopup(long itemId, IToBeUpdatedInventoryView view, int initUnitCount);
    void AddNewItem();
    int ShowItemInventoryOption(long itemId, IItemViewUpdateUnit callback);
    boolean IsReceiptPanelBusy();
}