package printer;

import android.content.Context;
import android.os.Message;
import android.widget.Toast;

//import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;

/**
 * Created by vanlanchoy on 10/25/2015.
 */
public class BluetoothPrinterListener extends android.os.Handler{
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    Context context;
    String strDeviceName="";
    boolean blnConnected = false;
    public BluetoothPrinterListener(Context c)
    {
        context=c;
    }
    public String GetConnectedDeviceName(){return strDeviceName;}
    public boolean IsConnected(){return blnConnected;}
    @Override
    public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (strDeviceName.length()==0)return;//not ready yet
                    switch (msg.arg1) {
                        case BluetoothPrintDriver.STATE_CONNECTED:

                            /*Toast.makeText(context, "connected to " + strDeviceName,
                                    Toast.LENGTH_SHORT).show();*/


                            break;
                        case BluetoothPrintDriver.STATE_CONNECTING:
                            Toast.makeText(context,"connecting to "+strDeviceName,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothPrintDriver.STATE_LISTEN:
                        case BluetoothPrintDriver.STATE_NONE:
                            /*Toast.makeText(context,"disconnected to "+strDeviceName,
                                    Toast.LENGTH_SHORT).show();*/
                            //reset device name
                            blnConnected = false;
                            strDeviceName="";
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    String ErrorMsg = null;
                    byte[] readBuf = (byte[]) msg.obj;
                    float Voltage = 0;

                    if(readBuf[2]==0)
                        ErrorMsg = "NO ERROR!         ";
                    else
                    {
                        if((readBuf[2] & 0x02) != 0)
                            ErrorMsg = "ERROR: No printer connected!";
                        if((readBuf[2] & 0x04) != 0)
                            ErrorMsg = "ERROR: No paper!  ";
                        if((readBuf[2] & 0x08) != 0)
                            ErrorMsg = "ERROR: Voltage is too low!  ";
                        if((readBuf[2] & 0x40) != 0)
                            ErrorMsg = "ERROR: Printer Over Heat!  ";
                    }
                    Voltage = (float) ((readBuf[0]*256 + readBuf[1])/10.0);
                    //if(D) Log.i(TAG, "Voltage: "+Voltage);
                    //DisplayToast(ErrorMsg+"                                        "+"Battery voltage"+Voltage+"V");
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    strDeviceName = msg.getData().getString(DEVICE_NAME);
                    blnConnected = true;
                    /*Toast.makeText(context, "Connected to "
                            + strDeviceName, Toast.LENGTH_SHORT).show();*/
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(context, msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }

    }
}
