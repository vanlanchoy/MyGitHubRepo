package printer;

import tme.pos.BusinessLayer.Receipt;

/**
 * Created by vanlanchoy on 10/25/2015.
 */
public interface IPrinter {
    void PrintReceipt(Receipt receipt);
    void Disconnect();
    boolean IsConnected();
    void Connect();
    String GetDeviceName();
    void OpenCashDrawer();
}
