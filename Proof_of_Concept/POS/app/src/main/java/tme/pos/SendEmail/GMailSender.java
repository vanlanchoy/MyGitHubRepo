package tme.pos.SendEmail;


import android.content.Context;
import android.os.AsyncTask;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

import java.io.InputStream;

import javax.activation.DataHandler;
        import javax.activation.DataSource;
        import javax.mail.Message;

        import javax.mail.Session;

import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
        import javax.mail.internet.MimeMessage;
        import java.io.ByteArrayInputStream;
        import java.io.IOException;

        import java.io.OutputStream;

        import java.util.Properties;

import tme.pos.BusinessLayer.common;
import tme.pos.R;


public class GMailSender extends AsyncTask<Void, Void, Boolean> {//} extends javax.mail.Authenticator {

    private Session session;
    String strSubject;
    String strTo;
    String strFrom;
    String strBody;
    String strToken;
    Context context;


   @Override
   protected Boolean doInBackground(Void... params) {

       try {

           SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com",
                   587,
                   strFrom,
                   strToken,
                   true);

           MimeMessage message = new MimeMessage(session);
           //DataHandler handler = new DataHandler(new ByteArrayDataSource(strBody.getBytes(), "text/plain"));
           message.setSender(new InternetAddress(strFrom));
           message.setSubject(strSubject);
           message.setContent(strBody, "text/html; charset=utf-8");
           //message.setDataHandler(handler);
           if (strTo.indexOf(',') > 0)
               message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(strTo));
           else
               message.setRecipient(Message.RecipientType.TO, new InternetAddress(strTo));
           smtpTransport.sendMessage(message, message.getAllRecipients());


       } catch (Exception e) {
           common.Utility.ShowMessage("Send Email",e.getMessage(),context, R.drawable.exclaimation);
           return false;
       }



       return true;
   }
    public SMTPTransport connectToSmtp(String host, int port, String userEmail,
                                       String oauthToken, boolean debug) throws Exception {


        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.sasl.enable", "false");
        session = Session.getInstance(props);
        session.setDebug(debug);


        final URLName unusedUrlName = null;
        SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
        // If the password is non-null, SMTP tries to do AUTH LOGIN.
        final String emptyPassword = null;
        transport.connect(host, port, userEmail, emptyPassword);

        byte[] response = String.format("user=%s\1auth=Bearer %s\1\1", userEmail,
                oauthToken).getBytes();
        response = BASE64EncoderStream.encode(response);

        transport.issueCommand("AUTH XOAUTH2 " + new String(response),
                235);

        return transport;
    }

    public GMailSender(String user,String body,String subject,String recipients,String token,Context context) {
        strFrom = user;
        strBody=body;
        strSubject=subject;
        strTo = recipients;
        strToken = token;
        this.context = context;
        //AccountManager am = AccountManager.get(context);
        //Account me = am.getAccounts()[0];



    }


    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}
