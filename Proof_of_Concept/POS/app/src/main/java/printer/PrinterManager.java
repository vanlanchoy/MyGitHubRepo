package printer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.net.ContentHandler;
import java.util.ArrayList;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.Receipt;
import tme.pos.BusinessLayer.common;
import tme.pos.BusinessLayer.Enum;
import tme.pos.MainUIActivity;
import tme.pos.R;
import tme.pos.ReceiptQueueActivity;

/**
 * Created by vanlanchoy on 10/25/2015.
 */
public class PrinterManager extends BroadcastReceiver{
    String strBrand;
    String strModel;
    String strAddress;
    int intPaperWidth;
    Context context;
    IPrinter connectedPrinter;
    BluetoothPrinterListener printerListener;
    ArrayList<Receipt>receiptQueue = new ArrayList<Receipt>();
    public PrinterManager(Context c)
    {
        context = c;
        //receiptQueue.add(Rongta_RPP200_Printer.CreateTestReceipt(context));
        //receiptQueue.add(Rongta_RPP200_Printer.CreateTestReceipt(context));
        //((Activity)context).startActivityForResult(new Intent(context, ReceiptQueueActivity.class), common.myAppSettings.INTENT_RECEIPT_QUEUE);
    }
  /*  public void DeleteReceiptInQueue(int[] Unsorted)
    {
        if(Unsorted.length==1)
        {
            if(Unsorted[0]==-1)
            {
                receiptQueue.clear();
                return;
            }
            else
            {
                receiptQueue.remove(Unsorted[0]);
            }
        }
        else
        {
            //sort the list descending order
            int temp;
            for(int i=0;i<Unsorted.length;i++)
            {
                for(int j=i+1;j>0;j--)
                {
                    if(Unsorted[j]>Unsorted[j-1])
                    {
                        temp = Unsorted[i];
                        Unsorted[j-1] = Unsorted[j];
                        Unsorted[j-1] = temp;
                    }
                }
            }

            for(int i=0;i<Unsorted.length;i++)
            {
                Log.d("Tag","i is now "+Unsorted[i]);
                receiptQueue.remove(Unsorted[i]);
            }
        }
    }*/
    public void ConnectSelectedPrinter(int intPaperWidth, String strBrand, String strModel,BluetoothDevice device)
    {
        this.intPaperWidth = intPaperWidth;
        this.strBrand = strBrand;
        this.strModel = strModel;
        this.strAddress = device.getAddress();

        //make sure is the right device
        if(device.getAddress().equalsIgnoreCase(strAddress))
        {
            if(strBrand.equalsIgnoreCase("star micronics"))
            {
                printerListener = new BluetoothPrinterListener(context);
                if(intPaperWidth==58) {
                    connectedPrinter = new StarMicronics_TSP650II_BTI_Thermal_58mm_Printer(printerListener, device, context);
                }
                else
                {
                    Toast.makeText(context, "hasn't implemented 80mm for Star Micronics printer", Toast.LENGTH_SHORT).show();
                }
                //connectedPrinter = new StarMicronics_TSP650II_BTI_Thermal_Printer(printerListener,device,context);


            }
            else if(strBrand.equalsIgnoreCase("rongta"))
            {
                if(strModel.equalsIgnoreCase("RPP200"))
                {
                    printerListener = new BluetoothPrinterListener(context);
                    connectedPrinter = new Rongta_RPP200_Printer(printerListener,device,context);

                }
            }

            else
            {
                Toast.makeText(context, "Device "+device.getName()+" not supported", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void ResumePrintingTask()
    {
        //ExecutePrintJob();
    }
    public void RemovedSelectedReceipt(int[] deletedIndexes)
    {
        for(int i=0;i<deletedIndexes.length;i++)
        {
            if(deletedIndexes[i]==-1)//delete all
            {
                receiptQueue.clear();
                return;
            }
            receiptQueue.remove(deletedIndexes[i]);
        }
    }
    public void StartBluetoothPrinter()
    {
        //reconnect or reselect??
    }
    public void StopBluetoothPrinter()
    {
        if(connectedPrinter!=null)connectedPrinter.Disconnect();
        strAddress="";
        strModel="";
        strBrand="";
        intPaperWidth=58;

        connectedPrinter=null;
    }
    public boolean HasConnectedPrinter()
    {
        if(connectedPrinter!=null && connectedPrinter.IsConnected())return true;

        return false;
    }
    private void CheckPrintQueue()
    {
        if(receiptQueue.size()>1)
        {
            //passing string instead of receipt objects
            ArrayList<String>lstReceipts = new ArrayList<String>();
            for(int i=0;i<receiptQueue.size();i++)
            {
                Receipt receipt=receiptQueue.get(i);
                lstReceipts.add(i+"|"+receipt.ReturnDateTimeString()
                        +"|"+common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount())
                        +"|"+receipt.tableNumber);
            }
            //prompt user to take action on current queue
            Intent intent = new Intent(context, ReceiptQueueActivity.class);
            intent.putExtra(AppSettings.EXTRA_RECEIPT_QUEUE,lstReceipts);
            //intent.putExtra(AppSettings.EXTRA_RECEIPT_QUEUE,receiptQueue);
            ((Activity)context).startActivityForResult(intent, common.myAppSettings.INTENT_RECEIPT_QUEUE);
        }
        else if(receiptQueue.size()==1)
        {
            ExecutePrintJob();
        }

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //Toast.makeText(context, "found", Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            //check whether is the device selected by the user
            if(device.getAddress().equalsIgnoreCase(strAddress)) {
                // target device is now connected
                CheckPrintQueue();

            }
            Toast.makeText(context, "(Printer Manager)connected to " + device.getName(), Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            //Toast.makeText(context, "discovery finished", Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            //Device is about to disconnect, stop printing task
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            //Device has disconnected, destroy the printer object
            if(device.getAddress().equalsIgnoreCase(strAddress)) {
                StopBluetoothPrinter();



            }
            Toast.makeText(context, "(Printer Manager) disconnected " + device.getName(), Toast.LENGTH_SHORT).show();
        }
    }
    public void OpenDrawer()
    {
        if(connectedPrinter!=null && connectedPrinter.IsConnected())
        {
            connectedPrinter.OpenCashDrawer();
        }
    }
    public boolean PrintReceipt(Receipt receipt)
    {
        //synchronized (receiptQueue) {
            //QueueReceipt(receipt);

            if(connectedPrinter==null)
            {
                common.Utility.ShowMessage("Printer","No printer device selected.",context, R.drawable.exclaimation);
                return false;

            }
            if(!connectedPrinter.IsConnected())
            {
                if(connectedPrinter.GetDeviceName().length()>0) {
                    connectedPrinter.Connect();
                }
                else {


                    common.Utility.ShowMessage("Printer", "Please select printer device to connect.", context, R.drawable.exclaimation);
                    connectedPrinter = null;//prompt the user to select again later
                    return false;
                }

            }
           //ExecutePrintJob();
        //}
        if( connectedPrinter.IsConnected()) {
            //Receipt r = receiptQueue.remove(0);
            connectedPrinter.PrintReceipt(receipt);
            return true;
        }
        return false;
    }
    private  void ExecutePrintJob()
    {
        synchronized (receiptQueue) {
            while (receiptQueue.size() > 0
                    && connectedPrinter != null){
                    boolean blnConnected= connectedPrinter.IsConnected();
                if(blnConnected) {
                    Receipt r = receiptQueue.remove(0);
                    connectedPrinter.PrintReceipt(r);
                    if(r.printReceiptType== Enum.PrintReceiptType.change_balance)
                    {
                        //let checkout panel invoke the call
                        //((MainUIActivity)context).Paid(r);
                    }
                }
            }
        }
    }
   /* public void QueueReceipt(Receipt receipt)
    {
        synchronized (receiptQueue) {
            common.Utility.FillInReceiptProperties(receipt,receipt.tableNumber);
            receiptQueue.add(receipt);

        }
    }*/
}
