package tme.pos.Interfaces;

/**
 * Created by kchoy on 6/20/2016.
 */
public interface ICustomListActivityListener {
    void  CustomList_AddNewUnitToCart(long itemId);
    void CustomList_ShowItemOptionPopup(long itemId);
}
