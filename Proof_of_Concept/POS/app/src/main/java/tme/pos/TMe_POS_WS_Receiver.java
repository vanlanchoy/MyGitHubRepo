package tme.pos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tme.pos.BusinessLayer.common;
import tme.pos.WebService.TMe_POS_WS;

/**
 * Created by kchoy on 3/9/2015.
 */
public class TMe_POS_WS_Receiver extends BroadcastReceiver {
    public interface OnTMePOSServerListener {
        public void onNotificationReceive(String strMsg,String strMsgId);
        void onExpirationDateReceive(String strDate);
        void onRegisterDevice(String strResult,String strEmail,String strHashMethod);
        void onUpdateGeoLocation(String RowAffected);

    }

    public interface OnPOSBackupListener{
        void OnGetLastBackupDate(String strDate);
        void OnFileUpload(String strResult);
        void Cancelled(String strMessage);
    }
    public interface OnPOSSendErrorMsgListener
    {
        void OnSendErrorMsg(String strErrorMsg);
    }
    public static final String ACTION_GET_SERVER_NOTIFICATION="TMe_POS_GET_SERVER_NOTIFICATION";
    public static final String ACTION_GET_EXPIRATION_DATE="TMe_POS_GET_EXPIRATION_DATE";
    public static final String ACTION_REGISTER_DEVICE="TMe_POS_REGISTER_DEVICE";
    public static final String ACTION_UPDATE_GEOLOCATION="TMe_POS_UPDATE_GEOLOCATION";
    public static final String ACTION_RETRIEVE_LAST_BACKUP_DATE="TMe_POS_LAST_BACKUP_DATE";
    public static final String ACTION_SEND_ERROR_MSG="TMe_POS_SEND_ERROR_MSG";
    public static final String ACTION_UPLOAD_BACKUP_FILE="TMe_POS_UPLOAD_BACKUP_FILE";
    public static final String ACTION_SEND_PASSWORD_RESET_LINK="TMe_POS_SEND_PASSWORD_RESET_LINK";


    OnTMePOSServerListener receiveListener;
    OnPOSBackupListener backupListener;
    OnPOSSendErrorMsgListener sendErrorMsgListener;

    public TMe_POS_WS_Receiver(OnTMePOSServerListener listener)
    {
        receiveListener = listener;
    }
    public TMe_POS_WS_Receiver(OnPOSBackupListener listener)
    {
        backupListener = listener;
    }
    public TMe_POS_WS_Receiver(OnPOSSendErrorMsgListener listener)
    {
        sendErrorMsgListener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION_GET_SERVER_NOTIFICATION)) {
            String strMessage = intent.getStringExtra(TMe_POS_WS.SERVER_MESSAGE);
            String strMsgId = intent.getStringExtra(TMe_POS_WS.SERVER_MESSAGE_ID);
            if(receiveListener!=null)
            {
                receiveListener.onNotificationReceive(strMessage,strMsgId);
            }
        } else if(intent.getAction().equals(ACTION_GET_EXPIRATION_DATE)) {
                String strMessage = intent.getStringExtra(TMe_POS_WS.EXPIRATION_DATE);
            if(receiveListener!=null) {
                receiveListener.onExpirationDateReceive(strMessage);
            }
        } else if(intent.getAction().equals(ACTION_REGISTER_DEVICE)) {
            String strResult = intent.getStringExtra(TMe_POS_WS.RESULT);
            String strHashMethod = intent.getStringExtra(TMe_POS_WS.HASH_METHOD);
            String strEmail = intent.getStringExtra(TMe_POS_WS.LOGIN_EMAIL_ADDRESS);
            if(receiveListener!=null) {
                if(strResult.equalsIgnoreCase(TMe_POS_WS.STRING_UPLOAD_PROCESS_CANCELLED_MESSAGE))
                {
                    backupListener.Cancelled(strResult);
                }
                else {
                    receiveListener.onRegisterDevice(strResult,strEmail,strHashMethod);
                }
            }
        }else if(intent.getAction().equals(ACTION_UPDATE_GEOLOCATION)) {
            String strRowCount = intent.getStringExtra(TMe_POS_WS.ACTION_ROW_AFFECTED);

            if(receiveListener!=null) {
                receiveListener.onUpdateGeoLocation(strRowCount);
            }
        }
        else if(intent.getAction().equals(ACTION_RETRIEVE_LAST_BACKUP_DATE)) {
            String strDate = intent.getStringExtra(TMe_POS_WS.LAST_BACKUP_DATE);

            if(backupListener!=null) {

                if(strDate.equalsIgnoreCase(TMe_POS_WS.STRING_UPLOAD_PROCESS_CANCELLED_MESSAGE))
                {
                    backupListener.Cancelled(strDate);
                }
                else {
                    backupListener.OnGetLastBackupDate(strDate);
                }
                //backupListener.OnGetLastBackupDate("1/1/1900 12:00 AM");
            }

        }
        else if(intent.getAction().equals(ACTION_SEND_ERROR_MSG)) {

            if(sendErrorMsgListener!=null) {
                sendErrorMsgListener.OnSendErrorMsg("");
            }

        }
        else if(intent.getAction().equals(ACTION_UPLOAD_BACKUP_FILE)) {
            String strResult = intent.getStringExtra(TMe_POS_WS.FILE_UPLOAD_RESULT);

            if(backupListener!=null) {
                backupListener.OnFileUpload(strResult);
            }

            if(sendErrorMsgListener!=null) {
                sendErrorMsgListener.OnSendErrorMsg(strResult);
            }

        }



    }
}
