package tme.pos.CustomViewCtr;

import android.content.Context;

import tme.pos.Interfaces.IOptionPopupItemListener;

/**
 * Created by kchoy on 5/31/2016.
 */
public class PromotionTextView extends GenericFlipableTextView {
    IOptionPopupItemListener popupItemListener;
    long promotionId;
    public PromotionTextView(Context c, int colorId, IOptionPopupItemListener popupItemListener,long promotionId) {
        super(c,colorId);
        this.popupItemListener = popupItemListener;
        this.promotionId = promotionId;
    }

    @Override
    protected void SingleTapped() {
        if(popupItemListener!=null)popupItemListener.PromotionItemSingleTap(promotionId);
    }

    @Override
    protected void ShowConfirmation() {
        if(popupItemListener!=null)popupItemListener.PromotionItemDoubleTap(promotionId);
    }
}
