package tme.pos.BusinessLayer;

/**
 * Created by kchoy on 9/27/2016.
 */

public class OrderCompareModel {
    public int Version;
    public String TableId;
    public int ReceiptIndex;
    public long LastUpdateDate;
    public OrderCompareModel(int version,String strTableId,int receiptIndex,long lastUpdatedate) {
        this.Version =version;
        this.TableId = strTableId;
        this.ReceiptIndex = receiptIndex;
        this.LastUpdateDate = lastUpdatedate;
    }
}
