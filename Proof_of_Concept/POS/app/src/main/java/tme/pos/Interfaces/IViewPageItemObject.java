package tme.pos.Interfaces;

import java.math.BigDecimal;

/**
 * Created by kchoy on 5/17/2016.
 */
public interface IViewPageItemObject {
    long getId();
    String getName();
    boolean getDoNotTrackFlag();
    String getPicturePath();
    long getBarcode();
    long getParentID();
    BigDecimal getPrice();
}
