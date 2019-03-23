package tme.pos.BusinessLayer;

/**
 * Created by kchoy on 10/25/2016.
 */

public class ReceiptCompareModel {
    public int Version;
    public String ReceiptNum;
    public ReceiptCompareModel(int  Version,String strReceiptNum) {
        this.Version = Version;
        this.ReceiptNum = strReceiptNum;
    }
}
