package tme.pos;

/**
 * Created by vanlanchoy on 1/25/2015.
 */
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;


public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {
    private Context context;
    private static Context context1;

    public MyUncaughtExceptionHandler(Context ctx) {
        context = ctx;
        context1 = ctx;
    }

    private StatFs getStatFs() {
        File path = Environment.getDataDirectory();
        return new StatFs(path.getPath());
    }

    private long getAvailableInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    private long getTotalInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
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
        StatFs stat = getStatFs();
        message.append("Total Internal memory: ")
                .append(getTotalInternalMemorySize(stat)).append('\n');
        message.append("Available Internal memory: ")
                .append(getAvailableInternalMemorySize(stat)).append('\n');
    }
    private void ReportErrorMessageToPosCenter(final  Throwable e)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context1);
        new Thread()
        {
            @Override
            public void run() {
                Looper.prepare();
                builder.setTitle("We deeply sorry!");
                builder.setPositiveButton("Report",null);

                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                System.exit(0);
                            }
                        });

             /*   builder.setPositiveButton("Report",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                //send log info to server
                                if(!common.Utility.IsConnectedToNetwork(context)) {
                                    common.Utility.ShowMessage("Internet","You must fist connect to the internet for this operation",
                                            context,R.drawable.no_access);
                                }
                                common.PosCenter.SendError(e);
                                //System.exit(0);
                            }
                        });*/
                builder.setMessage("Something has caused application to crash");
                final AlertDialog d =builder.create();
                d.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if(!common.Utility.IsConnectedToNetwork(context)) {
                                    common.Utility.ShowMessage("Internet","You must fist connect to the internet for this operation",
                                            context,R.drawable.no_access);
                                    return;
                                }
                                common.PosCenter.SendError(e);
                            }
                        });
                    }
                });
                //builder.show();
                d.show();
                Looper.loop();
            }
        }.start();

    }
    public void uncaughtException(Thread t, Throwable e) {
        try {
            //set crash flag
            common.myAppSettings.SetApplicationCrashFlag(true);
            //save
            //common.Utility.SaveReceiptsObjectIntoJson(context);

            ReportErrorMessageToPosCenter(e);

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
            Log.e(MyUncaughtExceptionHandler.class.getName(),
                    "Error while sendErrorMail" + report);
            sendErrorMail(report);
        } catch (Throwable ignore) {
            Log.e(MyUncaughtExceptionHandler.class.getName(),
                    "Error while sending error e-mail", ignore);
        }
    }

    /**
     * This method for call alert dialog when application crashed!
     */
    public void sendErrorMail(final StringBuilder errorContent) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        new Thread()
        {
            @Override
            public void run() {
                Looper.prepare();
                builder.setTitle("Sorry...!");
                builder.create();
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                System.exit(0);
                            }
                        });
                builder.setPositiveButton("Report",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent sendIntent = new Intent(
                                        Intent.ACTION_SEND);
                                String subject = "TMe POS Crash Report";
                                StringBuilder body = new StringBuilder("Content:");
                                body.append('\n').append('\n');
                                body.append(errorContent).append('\n')
                                        .append('\n');
                                // sendIntent.setType("text/plain");
                                sendIntent.setType("message/rfc822");
                                sendIntent.putExtra(Intent.EXTRA_EMAIL,
                                        new String[] { "vanlanchoy81@gmail.com" });
                                sendIntent.putExtra(Intent.EXTRA_TEXT,
                                        body.toString());
                                sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                                        subject);
                                sendIntent.setType("message/rfc822");
                                context1.startActivity(sendIntent);
                                System.exit(0);
                            }
                        });
                builder.setMessage("Oops,Your application has crashed");
                builder.show();
                Looper.loop();
            }
        }.start();
    }
}
