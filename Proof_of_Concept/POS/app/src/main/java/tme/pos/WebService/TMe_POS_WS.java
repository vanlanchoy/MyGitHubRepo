package tme.pos.WebService;


import android.app.IntentService;
import android.content.Context;
import android.content.Entity;
import android.content.Intent;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.NetworkOnMainThreadException;
import android.util.Base64;


import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;


import tme.pos.BusinessLayer.common;
import tme.pos.TMe_POS_WS_Receiver;



/**
 * Created by vanlanchoy on 3/8/2015.
 */

public class TMe_POS_WS extends IntentService {

    public static volatile boolean blnStopUploadProcess=false;
    public static final String STRING_UPLOAD_PROCESS_CANCELLED_MESSAGE="User cancelled process";
    public static final String SERVER_MESSAGE = "TMe_POS_Server_Notification_Message";
    public static final String SERVER_MESSAGE_ID = "TMe_POS_Server_Notification_Message_Id";
    public static final String EXPIRATION_DATE = "TMe_POS_EXPIRATION_DATE";
    public static final String PROFILE_ID = "TMe_POS_PROFILE_ID";
    public static final String HASHED_PASSWORD = "TMe_POS_HASHED_PASS";
    //public static final String PROFILE_ID_2 = "TMe_POS_PROFILE_ID_2";
    public static final String LONGITUDE = "TMe_POS_LONGITUDE";
    public static final String LATITUDE = "TMe_POS_LATITUDE";
    static final String NAMESPACE = "http://TMePOS.com/";
    static final String strTargetURL = "http://192.168.1.124:1234/Service1.svc";
    //static final String strTargetURL = "http://192.168.1.124:49185/Service1.svc";
    static final String strFileUploadTargetURL = "http://192.168.1.124:1234/FileUploadService.svc";
    //static final String strFileUploadTargetURL = "http://192.168.1.124:49185/FileUploadService.svc";
    static final String SERVICE_NAME = "POS_Service";
    static final String FILE_UPLOAD_SERVICE_NAME = "POS_FileUploadService";
    public static final String ACTION_ROW_AFFECTED = "TMe_ROW_AFFECTED";
    public static final String LAST_BACKUP_DATE = "TMe_POS_LAST_BACKUP_DATE";
    public static final String ERROR_MSG = "TMe_POS_ERROR_MSG";
    public static final String UPLOAD_FILE_PATH = "TMe_POS_FILE_PATH";
    public static final String FILE_UPLOAD_RESULT = "TMe_POS_FILE_UPLOAD_RESULT";
    public static final String HASH_METHOD="TMe_POS_HASH_METHOD";
    public static final String LOGIN_EMAIL_ADDRESS="TMe_LOGIN_EMAIL";
    public static final String PASSWORD="TMe_LOGIN_PASSWORD";
    public static final String RESULT="WEB_SERVICE_RESULT";

    //web service parameter
    public static final String PROFILE_ID_SERVER_PARAMETER="strProfileId";
    public static final String HASHED_PASSWORD_SERVER_PARAMETER="strHashedPassword";
    public static final String SERIAL_SERVER_PARAMETER="strSerial";
    public static final String EMAIL_SERVER_PARAMETER="strEmail";
    public static final String MAC_ID_SERVER_PARAMETER="strMacId";
    public static final String BRAND_SERVER_PARAMETER="strBrand";
    public static final String MODEL_SERVER_PARAMETER="strModel";
    public static final String HASHED_METHOD_SERVER_PARAMETER="strHashedMethod";
    public static final String FILENAME_SERVER_PARAMETER="Filename";
    public static final String ERROR_MESSAGE_PARAMETER="strErrorMsg";

    public TMe_POS_WS() {
        super("Get Server Notification");

    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Intent broadcastIntent = new Intent();
        // Gets data from the incoming Intent


        if (workIntent.getAction().equals(TMe_POS_WS_Receiver.ACTION_GET_SERVER_NOTIFICATION)) {

            String[] values = new String[]{"", "-1"};

            // Do work here, based on the contents of dataString
            String strResult = GetServerMessage();
            if (strResult != null) {
                values = strResult.split(";");
                values[0] = values[0].substring(values[0].indexOf("=") + 1, values[0].length());
                values[1] = values[1].substring(values[1].indexOf("=") + 1, values[1].length());
            }

            broadcastIntent.setAction(TMe_POS_WS_Receiver.ACTION_GET_SERVER_NOTIFICATION);
            //broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(SERVER_MESSAGE, values[0]);
            broadcastIntent.putExtra(SERVER_MESSAGE_ID, values[1]);
        }
        else if (workIntent.getAction().equals(TMe_POS_WS_Receiver.ACTION_GET_EXPIRATION_DATE))
        {
            String strResult = GetExpirationDate(workIntent.getStringExtra(PROFILE_ID), workIntent.getStringExtra(HASHED_PASSWORD));//pass in device identitfication
            broadcastIntent.setAction(TMe_POS_WS_Receiver.ACTION_GET_EXPIRATION_DATE);
            //broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(EXPIRATION_DATE, strResult);
        }
        else if (workIntent.getAction().equals(TMe_POS_WS_Receiver.ACTION_REGISTER_DEVICE))
        {
            String strMethodIndex = workIntent.getStringExtra(HASH_METHOD);
            String strEmail = workIntent.getStringExtra(LOGIN_EMAIL_ADDRESS);
            String strResult = RegisterDevice(workIntent.getStringExtra(PASSWORD),strMethodIndex,strEmail);
            broadcastIntent.setAction(TMe_POS_WS_Receiver.ACTION_REGISTER_DEVICE);
            if(blnStopUploadProcess)
            {
                strResult = STRING_UPLOAD_PROCESS_CANCELLED_MESSAGE;

            }

            broadcastIntent.putExtra(RESULT, strResult);
            broadcastIntent.putExtra(LOGIN_EMAIL_ADDRESS, strEmail);
            broadcastIntent.putExtra(HASH_METHOD, strMethodIndex);

        } else if (workIntent.getAction().equals(TMe_POS_WS_Receiver.ACTION_UPDATE_GEOLOCATION)) {

            String strResult = UpdateGeoLocation(
                    workIntent.getStringExtra(PROFILE_ID),
                    workIntent.getStringExtra(HASHED_PASSWORD),
                    workIntent.getStringExtra(LATITUDE),
                    workIntent.getStringExtra(LONGITUDE));
            broadcastIntent.setAction(TMe_POS_WS_Receiver.ACTION_UPDATE_GEOLOCATION);

            broadcastIntent.putExtra(ACTION_ROW_AFFECTED, strResult);
        } else if (workIntent.getAction().equals(TMe_POS_WS_Receiver.ACTION_RETRIEVE_LAST_BACKUP_DATE)) {

            String strResult = GetLastBackupDate(
                    workIntent.getStringExtra(PROFILE_ID),
                    workIntent.getStringExtra(HASHED_PASSWORD));

            broadcastIntent.setAction(TMe_POS_WS_Receiver.ACTION_RETRIEVE_LAST_BACKUP_DATE);

            broadcastIntent.putExtra(LAST_BACKUP_DATE, strResult);
        } else if (workIntent.getAction().equals(TMe_POS_WS_Receiver.ACTION_SEND_ERROR_MSG)) {

            SendErrorMsg(
                    workIntent.getStringExtra(ERROR_MSG),
                    workIntent.getStringExtra(PROFILE_ID),
                    workIntent.getStringExtra(HASHED_PASSWORD));

            broadcastIntent.setAction(TMe_POS_WS_Receiver.ACTION_SEND_ERROR_MSG);


        } else if (workIntent.getAction().equals(TMe_POS_WS_Receiver.ACTION_UPLOAD_BACKUP_FILE)) {
            String strFilePath = workIntent.getStringExtra(UPLOAD_FILE_PATH);
            //File file = new File(strFilePath);
            //FileInputStream in = null;
            //try {
            //in =new FileInputStream(file);
            String strFilename = workIntent.getStringExtra(UPLOAD_FILE_PATH);
            strFilename = strFilename.substring(strFilename.lastIndexOf("/") + 1);
            String strResult = UploadBackupFile(
                    workIntent.getStringExtra(PROFILE_ID),
                    workIntent.getStringExtra(HASHED_PASSWORD),
                    strFilePath,
                    strFilename
            );
            broadcastIntent.setAction(TMe_POS_WS_Receiver.ACTION_UPLOAD_BACKUP_FILE);

            broadcastIntent.putExtra(FILE_UPLOAD_RESULT, strResult);

        }
        //else if(workIntent.getAction().equals(TMe_POS_WS_Receiver.Ac))
        //broadcast the result
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }

    public String UpdateGeoLocation(String strId,String strHashedPassword, String strLatitude, String strLongitude) {

        String strMethod = "GeoLocation";
        //HashMap<String,String>KeyValuePairs = new HashMap<String, String>();
        HashMap<String, Object> KeyValuePairs = new HashMap<String, Object>();
        /*KeyValuePairs.put("strSerial",Build.SERIAL);
        KeyValuePairs.put("strId2", strId2);
        KeyValuePairs.put("strLatitude2", strLatitude);
        KeyValuePairs.put("strLongitude",strLongitude);*/
        KeyValuePairs.put("strValues", Build.SERIAL + "|" + strId + "|" + strLatitude + "|" + strLongitude+"|"+strHashedPassword);

        return SendRegularSoapMessage(strMethod, KeyValuePairs);
    }

    public String RegisterDevice(String strPass,String strHashMethod,String strEmail) {
        common.Utility.LogActivity("Register device");
        WifiManager wifiMan = (WifiManager) this.getSystemService(
                Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();

        String strMethod = "Register";
        //HashMap<String,String>KeyValuePairs = new HashMap<String, String>();
        HashMap<String, Object> KeyValuePairs = new HashMap<String, Object>();
        KeyValuePairs.put(HASHED_PASSWORD_SERVER_PARAMETER, strPass);
        KeyValuePairs.put(HASHED_METHOD_SERVER_PARAMETER,strHashMethod);
        KeyValuePairs.put(EMAIL_SERVER_PARAMETER, strEmail);
        KeyValuePairs.put(SERIAL_SERVER_PARAMETER, Build.SERIAL);
        KeyValuePairs.put(MAC_ID_SERVER_PARAMETER, wifiInf.getMacAddress());
        KeyValuePairs.put(BRAND_SERVER_PARAMETER, Build.BRAND);
        KeyValuePairs.put(MODEL_SERVER_PARAMETER, android.os.Build.MODEL);


        return SendRegularSoapMessage(strMethod, KeyValuePairs);
    }

    public String GetExpirationDate(String strId, String strHashedPassword) {
        common.Utility.LogActivity("get expiration date");
        String strMethod = "Expiry";
        //HashMap<String,String>KeyValuePairs = new HashMap<String, String>();
        HashMap<String, Object> KeyValuePairs = new HashMap<String, Object>();
        KeyValuePairs.put(PROFILE_ID_SERVER_PARAMETER, strId);
        KeyValuePairs.put(HASHED_PASSWORD_SERVER_PARAMETER, strHashedPassword);

        return SendRegularSoapMessage(strMethod, KeyValuePairs);
    }

    /* private Element[] BuildSoapEnvelopeHeader(HashMap<String,Object>KeyValuePairs)
     {
         ArrayList<Element> elements = new ArrayList<Element>();
         for(String strKey:KeyValuePairs.keySet())
         {
             Element element = new Element().createElement(NAMESPACE,strKey);
             element.addChild(Node.TEXT,KeyValuePairs.get(strKey));
             elements.add(element);
         }
         return elements.toArray(new Element[0]);
     }*/
    private SoapSerializationEnvelope CreateSoapEnvelope(String strMethodName, HashMap<String, Object> KeyValuePairs) {
        SoapObject request = new SoapObject(NAMESPACE, strMethodName);
        for (String strKey : KeyValuePairs.keySet()) {
            request.addProperty(strKey, KeyValuePairs.get(strKey));
        }
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        return envelope;
    }


    private String CancelProcess(String strMsg)
    {
        return "Message="+strMsg+"; MessageCode=-1;";
    }
    private String SendUploadFile(String strMethodName,HashMap<String,Object>KeyValuePairs,String strFilePath)
    {

        String strUTF8 ="utf-8";

        //check cancellation
        if(blnStopUploadProcess)
        {
            return CancelProcess(STRING_UPLOAD_PROCESS_CANCELLED_MESSAGE);
        }


        try {
            File file =new File(strFilePath);
            URL url = new URL(strFileUploadTargetURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            /**web header**/
            //conn.setRequestProperty("Connection","Keep-Alive");
            conn.setRequestProperty("Content-type", "text/xml;charset="+strUTF8);
            //conn.setRequestProperty("Content-type", "multipart/related; type=\"application/xop+xml");
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            //conn.setRequestProperty("Expect","100-continue");
            //conn.setRequestProperty("VsDebuggerCausalityData","uIDPo+4PV8VPOuBHnFeUlUHX9g8AAAAAqyItjcS6bUGnegrS5A5HzmOHz4nDt8hMsqYnV+aJQJ0ACQAA");
            conn.setRequestProperty("SOAPAction", NAMESPACE + FILE_UPLOAD_SERVICE_NAME + "/" + strMethodName);
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            //conn.setFixedLengthStreamingMode(file.length());
            /**envelope**/
            OutputStream os =conn.getOutputStream();


            String strSoapRequest="<?xml version=\"1.0\" encoding=\"utf-8\"?>";
            strSoapRequest+="<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">";
            strSoapRequest+="<s:Header>\n";
            for(String strKey:KeyValuePairs.keySet()) {
                strSoapRequest+="<h:"+strKey+" xmlns:h=\"http://TMePOS.com/\">" + KeyValuePairs.get(strKey) + "</h:"+strKey+">";
            }
            //strSoapRequest+="<To s:mustUnderstand=\"1\" xmlns=\"http://schemas.microsoft.com/ws/2005/05/addressing/none\">http://192.168.1.124:1234/FileUploadService.svc</To>\n" +
                    //"<Action s:mustUnderstand=\"1\" xmlns=\"http://schemas.microsoft.com/ws/2005/05/addressing/none\">http://TMePOS.com/POS_FileUploadService/UploadFile</Action>\n";
            strSoapRequest+="</s:Header>\n";
            strSoapRequest+="<s:Body>";
            strSoapRequest+="<FileDetails xmlns=\"http://TMePOS.com/\">";
            strSoapRequest+="<FileStream>";
            //strSoapRequest+="<Id2>1</Id2>";
            //strSoapRequest+="<Filename>abc.gzip</Filename>";

            //byte[] temp1 = strSoapRequest.getBytes();
            //os.write(Base64.encode(temp1,0,temp1.length,Base64.DEFAULT));
            os.write(strSoapRequest.getBytes());

            //now include zip file
            FileInputStream fis = new FileInputStream(file);
            //StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[4096];
            int byteRead;

            while((byteRead= fis.read(buffer))!=-1)
            {

                //check cancellation
                if(blnStopUploadProcess)
                {
                    os.flush();
                    os.close();
                    fis.close();
                    conn.disconnect();
                    return CancelProcess(STRING_UPLOAD_PROCESS_CANCELLED_MESSAGE);
                }

                byte[] temp1 = Base64.encode(buffer, 0, byteRead, Base64.DEFAULT);
                os.write(temp1,0,temp1.length);
            }

            strSoapRequest="</FileStream>";
            strSoapRequest+="</FileDetails>";
            strSoapRequest+="</s:Body>\n";
            strSoapRequest+="</s:Envelope>\n";
            //temp1 = strSoapRequest.getBytes();
            //os.write(Base64.encode(temp1,0,temp1.length,Base64.DEFAULT));
            os.write(strSoapRequest.getBytes());
            os.flush();
            int code =conn.getResponseCode();
            String strMsg=conn.getResponseMessage();

            fis.close();
            if(code==200)//ok
            return strMsg;
            try {

                BufferedReader r = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), strUTF8));
                String content = "";
                String line;
                while ((line = r.readLine()) != null) {
                    content += line + "\n";
                }

                conn.disconnect();


                return content;
            }
            catch (FileNotFoundException ex)
            {
                if(strMsg.length()>0)return strMsg;
                return "Message= never hit web server; MessageCode=-1;";
            }
            catch (IOException ex) {


                return "Message="+ex.getMessage()+"; MessageCode=-1;";
            }

            //return strMsg;
            //return new JSONObject(EntityUtils.toString(entity)).toString();
        }

        catch(IOException ex)
        {
            return "Message="+ex.getMessage()+"; MessageCode=-1;";
        }
        catch(Exception ex)
        {
            return "Message="+ex.getMessage()+"; MessageCode=-1;";
        }
        /*catch (JSONException ex)
        {
            return "Message="+ex.getMessage()+"; MessageCode=-1;";
        }*/


    }


    private String SendRegularSoapMessage(String strMethodName,HashMap<String,Object>KeyValuePairs)
    {
        SoapSerializationEnvelope envelope = CreateSoapEnvelope(strMethodName,KeyValuePairs);
        return SendSoapMessage(envelope,strMethodName);
    }
    //private String SendSoapMessage(String strMethodName,HashMap<String,String>KeyValuePairs)
    private String SendSoapMessage(SoapSerializationEnvelope envelope,String strMethodName)
    {
        String SOAP_ACTION = NAMESPACE+SERVICE_NAME+"/"+strMethodName;
        String result="";
        try {

            HttpTransportSE ht = new HttpTransportSE(strTargetURL,30000);
            ht.call(SOAP_ACTION,envelope);

            if(envelope.getResponse()!=null)
            {
                Object response = envelope.getResponse();
                result = response.toString();
                result=result.replace("anyType{","");
                result=result.replace("}","");
            }


            return result;

        }
        catch(NetworkOnMainThreadException ex)
        {
            return "Message="+ex.getMessage()+"; MessageCode=-1;";
        }
        catch(SocketTimeoutException ex)
        {
            return "Message="+ex.getMessage()+"; MessageCode=-1";
        }
        catch(SoapFault ex)
        {
            return "Error Message:"+ex.faultstring+"; MessageCode=-1";
        }
        catch (Exception ex) {

            return "Error Message:"+ex.getMessage()+"; MessageCode=-1";

        }

    }
    public String GetServerMessage(){


        String strMethod = "Notification";
        //return SendSoapMessage(strMethod,new HashMap<String, String>());
        return SendRegularSoapMessage(strMethod, new HashMap<String, Object>());

    }
    public String SendResetPasswordLink(String strLoginEmailAddress)
    {
        String strMethod="SendResetPasswordLink";

        HashMap<String,Object>KeyValuePairs = new HashMap<String, Object>();
        KeyValuePairs.put(EMAIL_SERVER_PARAMETER,strLoginEmailAddress);


        return SendRegularSoapMessage(strMethod, KeyValuePairs);
    }
    public String GetLastBackupDate(String strId,String strHashedPassword)
    {
        String strMethod="GetLastBackupDate";
        //HashMap<String,String>KeyValuePairs = new HashMap<String, String>();
        HashMap<String,Object>KeyValuePairs = new HashMap<String, Object>();
        KeyValuePairs.put(PROFILE_ID_SERVER_PARAMETER,strId);
        KeyValuePairs.put(HASHED_PASSWORD_SERVER_PARAMETER,strHashedPassword);

        return SendRegularSoapMessage(strMethod, KeyValuePairs);
    }
    public String UploadBackupFile(String strId,String strHashedPassword ,String strFilePath,String strFilename)
    {
        String strMethod="UploadFile";
        HashMap<String,Object>KeyValuePairs = new HashMap<String, Object>();
        KeyValuePairs.put(PROFILE_ID_SERVER_PARAMETER,strId);
        KeyValuePairs.put(HASHED_PASSWORD_SERVER_PARAMETER,strHashedPassword);
        KeyValuePairs.put(FILENAME_SERVER_PARAMETER,strFilename);


        //return SendUploadFileMultipart(strMethod,KeyValuePairs,strFilePath);
        return SendUploadFile(strMethod,KeyValuePairs,strFilePath);
    }
    public String SendErrorMsg(String strId,String strErrorMsg,String strHashedPassword)
    {
        String strMethod="LogErrorMsg";
        //HashMap<String,String>KeyValuePairs = new HashMap<String, String>();
        HashMap<String,Object>KeyValuePairs = new HashMap<String, Object>();
        KeyValuePairs.put(ERROR_MESSAGE_PARAMETER,strErrorMsg);
        KeyValuePairs.put(PROFILE_ID_SERVER_PARAMETER,strId);
        KeyValuePairs.put(HASHED_PASSWORD_SERVER_PARAMETER,strHashedPassword);

        return SendRegularSoapMessage(strMethod, KeyValuePairs);
    }

}
