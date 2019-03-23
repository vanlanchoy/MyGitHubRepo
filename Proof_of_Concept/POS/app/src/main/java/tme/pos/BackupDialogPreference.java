package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.BackupManager;
import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.common;
import tme.pos.WebService.TMe_POS_WS;

/**
 * Created by vanlan on 2/16/2016.
 */
public class BackupDialogPreference extends DialogPreference implements TMe_POS_WS_Receiver.OnPOSBackupListener
        ,BackupManager.IProgressListener {//TMe_POS_WS_Receiver.OnTMePOSServerListener
    View currentView;
    boolean blnProcessing;
    TextView tvBackup;
    TextView tvExit;
    IntentFilter backupIntentFilter;
    IntentFilter lastBackupDateIntentFilter;
    IntentFilter registrationIntentFilter;
    TMe_POS_WS_Receiver backupServerMsgReceiver ;
    TMe_POS_WS_Receiver registrationServerMsgReceiver ;
    //Intent TmeServerRegisterDeviceIntent;
    Intent intent ;
    LinearLayout progressBar;
    TextView tvPercentage;
    LinearLayout llMsgContainer;
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(false);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        super.onDismiss(dialog);
    }

    public BackupDialogPreference(Context context,AttributeSet attrs)
    {
        super(context, attrs);
        setDialogLayoutResource(R.layout.layout_dialog_backup_ui);
        blnProcessing = false;
        //filter intents
        backupIntentFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_UPLOAD_BACKUP_FILE);
        backupIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        lastBackupDateIntentFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_RETRIEVE_LAST_BACKUP_DATE);
        lastBackupDateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registrationIntentFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_REGISTER_DEVICE);
        registrationIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);


        intent = new Intent(context, TMe_POS_WS.class);


    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setCancelable(false);
        builder.setNegativeButton(null, null);
        builder.setPositiveButton(null, null);
        builder.setIcon(null);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (DialogInterface.BUTTON_POSITIVE == which) {

        }
    }


    @Override
    public void Cancelled(String strMessage) {
        tvBackup.setEnabled(true);
        ResetControls();
    }

    @Override
    protected void onBindDialogView(View view) {

        currentView = view;
        super.onBindDialogView(view);

        tvExit = (TextView)view.findViewById(R.id.tvExit);
        tvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        tvBackup = (TextView)view.findViewById(R.id.tvBackup);
        tvBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TMe_POS_WS.blnStopUploadProcess = !blnProcessing;
                if (!blnProcessing) {
                    blnProcessing = true;
                    tvExit.setEnabled(false);
                    tvExit.setTextColor(getContext().getResources().getColor(R.color.divider_grey));
                    tvBackup.setText("Cancel");
                    //clear all the status msg
                    llMsgContainer.removeAllViews();
                    Backup();
                } else {
                    common.Utility.LogActivity("Canceling backup");
                    UpdateCurrentMsg("Cancelling backup please wait......");
                    blnProcessing = false;
                    //tvExit.setTextColor(getContext().getResources().getColor(R.color.green));
                    //tvExit.setEnabled(true);
                    tvBackup.setText("Backup");
                    tvBackup.setEnabled(false);
                }
            }
        });

        llMsgContainer = (LinearLayout)view.findViewById(R.id.llMsgContainer);
        tvPercentage = (TextView)view.findViewById(R.id.tvPercentage);
        progressBar = (LinearLayout)view.findViewById(R.id.progressBar);
    }
    private void UpdateCurrentMsg(String strNewMsg)
    {
        ((TextView)((LinearLayout)llMsgContainer.getChildAt(llMsgContainer.getChildCount()-1)).getChildAt(0)).setText(strNewMsg);
    }
    private void InsertNewMessage(String strMsg)
    {
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lllpContainer = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llMsgContainer.addView(ll,lllpContainer);
        TextView tvMsg = new TextView(getContext());
        tvMsg.setMaxWidth(common.Utility.DP2Pixel(400,getContext()));
        LinearLayout.LayoutParams lllp =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMsg.setText(strMsg);
        ll.addView(tvMsg,lllp);
    }
    private void ResetControls()
    {
        blnProcessing = false;
        tvExit.setTextColor(getContext().getResources().getColor(R.color.green));
        tvExit.setEnabled(true);
        tvBackup.setText("Backup");
    }
    private void DisplayProcessIcon(Enum.OperationStatus status)
    {
        final int progressBar_size = common.Utility.DP2Pixel(25,getContext());
        final int icon_size = common.Utility.DP2Pixel(12,getContext());
        LinearLayout ll =((LinearLayout)llMsgContainer.getChildAt(llMsgContainer.getChildCount()-1));
        if(ll.getChildCount()>1)
        {
            ll.removeViewAt(1);
        }

        final LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(progressBar_size,progressBar_size);
        lllp.leftMargin=6;
        if(status== Enum.OperationStatus.processing)
        {
            ll.addView(new ProgressBar(getContext()),lllp);
        }
        else if(status== Enum.OperationStatus.success)
        {
            ImageView img = new ImageView(getContext());
            img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ok));
            lllp.height=lllp.width=common.Utility.DP2Pixel(20,getContext());
            lllp.bottomMargin=3;
            ll.addView(img,lllp);

        }
        else if(status== Enum.OperationStatus.fail)
        {
            final ImageView img = new ImageView(getContext());
            img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.failed));
            lllp.height=lllp.width=icon_size;
            ll.addView(img,lllp);
            lllp.topMargin=3;
            /*new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    img.getLayoutParams().height=icon_size;
                    img.getLayoutParams().width=icon_size;
                    lllp.topMargin=3;
                }
            }, 0);*/

        }
    }
    private void Backup()
    {


         if(!common.Utility.IsConnectedToNetwork(getContext()))
        {
            common.Utility.LogActivity("no internet");
            common.Utility.ShowMessage("Internet","You must fist connect to the internet for this operation",getContext(),R.drawable.no_access);
            ResetControls();
            return;
        }
        if(!IsRegistered())
        {
            common.Utility.LogActivity("Device hasn't register");
            common.Utility.ShowMessage("Registration","You must fist register this device.",getContext(),R.drawable.no_access);
            return;
        }

        RetrieveLastBackupDate();

    }
    private void RegisterDevice()
    {

        registrationServerMsgReceiver = new TMe_POS_WS_Receiver((TMe_POS_WS_Receiver.OnTMePOSServerListener) this);
        getContext().registerReceiver(registrationServerMsgReceiver, registrationIntentFilter);

        //start service
        intent.setAction(TMe_POS_WS_Receiver.ACTION_REGISTER_DEVICE);
        getContext().startService(intent);
    }
    private boolean IsRegistered()
    {
        if(common.myAppSettings.GetDeviceRegisterId().length()>0)return true;
        return false;
    }
    private void UploadFile()
    {
        if(!blnProcessing) return;
        common.Utility.LogActivity("upload file");
        //chop the file into smaller piece if necessary
        String strFilePath = common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+"temp.zip.gzip";
        //String strFilePath = common.myAppSettings.FILE_EXPORT_LOCATION+"item.csv";
        UploadFileToServer(strFilePath);

    }
    private void UploadFileToServer(String strFilePath)
    {
        //IntentFilter intentFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_UPLOAD_BACKUP_FILE);
        backupIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        //TMe_POS_WS_Receiver serverMsgReceiver = new TMe_POS_WS_Receiver(this);
        getContext().registerReceiver(backupServerMsgReceiver, backupIntentFilter);//start listening

        //start service to check for TMe server notification
        //Intent intent = new Intent(getContext(), TMe_POS_WS.class);
        intent.setAction(TMe_POS_WS_Receiver.ACTION_UPLOAD_BACKUP_FILE);
        intent.putExtra(TMe_POS_WS.PROFILE_ID, common.myAppSettings.GetDeviceRegisterId());
        intent.putExtra(TMe_POS_WS.HASHED_PASSWORD, common.myAppSettings.GetHashedPassword());
        intent.putExtra(TMe_POS_WS.UPLOAD_FILE_PATH,strFilePath);
        getContext().startService(intent);
    }
    private void RetrieveLastBackupDate()
    {
        //IntentFilter intentFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_RETRIEVE_LAST_BACKUP_DATE);
        common.Utility.LogActivity("get last backup date");
        InsertNewMessage("Retrieving last backup date...");
        DisplayProcessIcon(Enum.OperationStatus.processing);

        lastBackupDateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        backupServerMsgReceiver = new TMe_POS_WS_Receiver(this);
        getContext().registerReceiver(backupServerMsgReceiver, lastBackupDateIntentFilter);//start listening

        //start service to check for TMe server notification
        intent.setAction(TMe_POS_WS_Receiver.ACTION_RETRIEVE_LAST_BACKUP_DATE);
        intent.putExtra(TMe_POS_WS.PROFILE_ID, common.myAppSettings.GetDeviceRegisterId());
        intent.putExtra(TMe_POS_WS.HASHED_PASSWORD, common.myAppSettings.GetHashedPassword());
        getContext().startService(intent);
    }
    public void UpdateProgressUI(float percentage)
    {
        tvPercentage.setText(percentage+"%");
        progressBar.getLayoutParams().width = Math.round((percentage/100)*common.Utility.DP2Pixel(350,getContext()));

    }
    @Override
    public void OnGetLastBackupDate (String strDate) {

        //exit if user cancelled the process
        if(!blnProcessing)return;

        common.Utility.LogActivity("Last backup date "+ strDate);
        if(strDate.compareTo("1/1/1900 12:00 AM")==0)
        {
            UpdateCurrentMsg("Initial backup");
            DisplayProcessIcon(Enum.OperationStatus.success);
        }
        else {
            String format = "MM/dd/yyyy HH:mm a";
            SimpleDateFormat sdf = new SimpleDateFormat(format);

            try {
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = sdf.parse(strDate);
                sdf.setTimeZone(Calendar.getInstance().getTimeZone());

                //d = new Date(d.getTime() + TimeZone.getDefault().getOffset(new Date(System.currentTimeMillis()).getTime()));
                UpdateCurrentMsg("Last backup date is "
                        + sdf.format(d));

                DisplayProcessIcon(Enum.OperationStatus.success);


            }
            catch(ParseException ex)
            {

                UpdateCurrentMsg("Failed to retrieve last backup date");
                DisplayProcessIcon(Enum.OperationStatus.fail);
                ResetControls();
                return;
            }

        }

        InsertNewMessage("Zipping file...");
        DisplayProcessIcon(Enum.OperationStatus.processing);
        BackupManager bm = new BackupManager(getContext(),this);
        bm.BeginBackup();

        if(!blnProcessing)return;

        UpdateCurrentMsg("Zipped all files");
        DisplayProcessIcon(Enum.OperationStatus.success);

        InsertNewMessage("Uploading please wait...");
        DisplayProcessIcon(Enum.OperationStatus.processing);
        UploadFile();

    }
    @Override
    public void OnFileUpload(String strResult)
    {
        //common.Utility.ShowMessage("File Upload",strResult,getContext(),R.drawable.message);
        if(strResult.equalsIgnoreCase("ok"))
        {
            UpdateCurrentMsg("Backup completed");
            DisplayProcessIcon(Enum.OperationStatus.success);
        }
        else
        {
            UpdateCurrentMsg("Backup failed, please try again later");
            DisplayProcessIcon(Enum.OperationStatus.fail);
        }
        ResetControls();
    }




    @Override
    public void Progress(float percentage) {
        UpdateProgressUI(percentage);
    }
}
