package tme.pos.Interfaces;

import tme.pos.CustomViewCtr.MenuItemFlipablePicView;
import tme.pos.CustomViewCtr.MenuItemFlippableTableRow;
import tme.pos.ItemInventoryOptionDialog;

/**
 * Created by kchoy on 5/27/2016.
 */
public interface IMenuItemClickedListener
{
    //int ListItemSingleTapped(long itemId,int initInventoryCount);
    void  ListItemDoubleTapped(long itemId, IToBeUpdatedInventoryView row, int initInventoryCount);
    int ListItemSingleTapped(long itemId, IToBeUpdatedInventoryView row, int initInventoryCount);
    void InventoryPopupClicked(long itemId, IItemViewUpdateUnit callback);
}