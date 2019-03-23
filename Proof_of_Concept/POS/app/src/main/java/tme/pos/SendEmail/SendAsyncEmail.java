package tme.pos.SendEmail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import tme.pos.BusinessLayer.common;
import tme.pos.R;

/**
 * Created by vanlanchoy on 9/5/2015.
 */
public class SendAsyncEmail extends AsyncTask<Void, Void, Boolean> {

    String strSubject;
    String strTo;
    String strFrom;
    String strBody;
    String strToken;
    Context context;
    Activity activity;

    public class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result){
            try{
                Bundle bundle = result.getResult();
                strToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                new GMailSender(strFrom,strBody,strSubject,strTo,strToken,context).execute();
            } catch (Exception e){

                String strMsg=(e.getMessage()!=null)?e.getMessage():"Failed to send email please check your email address and internet connection.";

                common.Utility.ShowMessage("Send Email Receipt",strMsg, context, R.drawable.exclaimation);
            }
        }
    }
    public SendAsyncEmail(String From,String Subject,String Body,String To,Context c,Activity a) {

        strTo = To;
        strSubject = Subject;
        strFrom = From;
        strBody = Body;
        context =c;
        activity = a;

    }

    @Override
    protected Boolean doInBackground(Void... params) {

        //get oauth token
        try {
            AccountManager am = AccountManager.get(context);
            Account me=null;
            for(int i=0;i<am.getAccounts().length;i++)
            {
                if(am.getAccounts()[i].name.equalsIgnoreCase(strFrom))
                {
                    me = am.getAccounts()[i];
                }
            }
            if(me==null)return false;
            am.getAuthToken(me, "oauth2:https://mail.google.com/", null, activity, new OnTokenAcquired(), null);


            //strToken = GoogleAuthUtil.getToken(context, strFrom, common.myAppSettings.Scope);
        }
        catch(Exception ex)
        {
            Log.d("SendAsyncEmail",ex.getMessage());
        }



           return true;
    }
}