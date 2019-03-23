package tme.pos.Interfaces;

import tme.pos.CustomViewCtr.PromotionAddRuleItemSelectionRow;

/**
 * Created by kchoy on 6/1/2016.
 */
public interface IAddPromotionRuleItemRowActivityListener {
    void RemoveRow(PromotionAddRuleItemSelectionRow row);
    void AddRow();
}
