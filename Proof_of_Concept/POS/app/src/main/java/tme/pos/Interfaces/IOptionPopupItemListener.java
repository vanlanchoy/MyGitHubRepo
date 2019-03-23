package tme.pos.Interfaces;

/**
 * Created by kchoy on 5/31/2016.
 */
public interface IOptionPopupItemListener {
    void PromotionItemSingleTap(long promotionId);
    void PromotionItemDoubleTap(long promotionId);
    void ComboItemSingleTap(long comboId);
    void ComboItemDoubleTap(long comboId);
}
