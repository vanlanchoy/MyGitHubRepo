package tme.pos.BusinessLayer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Locale;

import tme.pos.R;
import tme.pos.TMe_POS_WS_Receiver;
import tme.pos.WebService.TMe_POS_WS;

/**
 * Created by vanlanchoy on 4/9/2016.
 */
public class ServerCommunicator implements TMe_POS_WS_Receiver.OnPOSSendErrorMsgListener{
    Context context;
    TMe_POS_WS_Receiver serverMsgReceiver;
    public ServerCommunicator(Context c)
    {
        context = c;
        serverMsgReceiver = new TMe_POS_WS_Receiver(this);
        RegisterService();
    }
    public void UnRegisterService()
    {
        if(serverMsgReceiver!=null)
        context.unregisterReceiver(serverMsgReceiver);
    }
    private void RegisterService()
    {
        IntentFilter serverMsgListenerFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_SEND_ERROR_MSG);
        serverMsgListenerFilter.addCategory(Intent.CATEGORY_DEFAULT);
        //TMe_POS_WS_Receiver serverMsgReceiver = new TMe_POS_WS_Receiver(this);
        context.registerReceiver(serverMsgReceiver, serverMsgListenerFilter);//start listening
    }
    private void addInformation(StringBuilder message) {
        message.append("Locale: ").append(Locale.getDefault()).append('\n');
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            message.append("Version: ").append(pi.versionName).append('\n');
            message.append("Package: ").append(pi.packageName).append('\n');
        } catch (Exception e) {
            Log.e("CustomExceptionHandler", "Error", e);
            message.append("Could not get Version information for ").append(
                    context.getPackageName());
        }
        message.append("Phone Model: ").append(android.os.Build.MODEL)
                .append('\n');
        message.append("Phone Serial: ").append(Build.SERIAL).append('\n');
        message.append("Android Version: ")
                .append(android.os.Build.VERSION.RELEASE).append('\n');
        message.append("Board: ").append(android.os.Build.BOARD).append('\n');
        message.append("Brand: ").append(android.os.Build.BRAND).append('\n');
        message.append("Device: ").append(android.os.Build.DEVICE).append('\n');
        message.append("Host: ").append(android.os.Build.HOST).append('\n');
        message.append("ID: ").append(android.os.Build.ID).append('\n');
        message.append("Model: ").append(android.os.Build.MODEL).append('\n');
        message.append("Product: ").append(android.os.Build.PRODUCT)
                .append('\n');
        message.append("Type: ").append(android.os.Build.TYPE).append('\n');

    }
    public void SendError(Throwable e)
    {
        try {
            //prepare crash log
            StringBuilder report = new StringBuilder();
            Date curDate = new Date();
            report.append("Error Report collected on : ")
                    .append(curDate.toString()).append('\n').append('\n');
            report.append("Information:").append('\n');
            addInformation(report);
            report.append('\n').append('\n');
            report.append("Stack:\n");


            if(e.getMessage()!=null)report.append(e.getMessage()+"\n");

            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            report.append(result.toString());
            printWriter.close();
            report.append('\n');
            report.append(common.Utility.ReturnActivityLogString());
            report.append("**** End of current Report ***");

            //report error  to server error log table


            //start service to check for TMe server notification
            Intent intent = new Intent(context, TMe_POS_WS.class);
            intent.setAction(TMe_POS_WS_Receiver.ACTION_SEND_ERROR_MSG);
            intent.putExtra(TMe_POS_WS.ERROR_MSG, report.toString());
            intent.putExtra(TMe_POS_WS.PROFILE_ID, common.myAppSettings.GetDeviceRegisterId());
            intent.putExtra(TMe_POS_WS.HASHED_PASSWORD, common.myAppSettings.GetHashedPassword());
            context.startService(intent);
        }
        catch (Exception ex)
        {
            common.Utility.ShowMessage("Send Error Report","Failed to send error report",context, R.drawable.exclaimation);
        }
    }

    @Override
    public void OnSendErrorMsg(String strErrorMsg) {

    }
}
