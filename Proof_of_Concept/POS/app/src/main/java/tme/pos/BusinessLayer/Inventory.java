package tme.pos.BusinessLayer;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by kchoy on 6/18/2015.
 */
public class Inventory {
    public long lngInventoryId;
    public long lngItemId;
    public long lngSupplierId;
    public int UnitCount;
    public BigDecimal CostPrice;
    public Date RecordDate;
}
