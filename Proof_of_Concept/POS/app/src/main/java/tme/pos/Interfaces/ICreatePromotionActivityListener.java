package tme.pos.Interfaces;

import java.util.HashMap;

/**
 * Created by kchoy on 6/2/2016.
 */
public interface ICreatePromotionActivityListener {
    void UpdateRuleItemGroup(HashMap<Long,Integer>hashMap,int listIndex);
    void AddRuleItemGroup(HashMap<Long,Integer>hashMap);
}
