package printer;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;


//import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;
import com.StarMicronics.StarIOSDK.RasterDocument;
import com.StarMicronics.StarIOSDK.StarBitmap;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.StarMicronics.StarIOSDK.RasterDocument.RasPageEndMode;
import com.StarMicronics.StarIOSDK.RasterDocument.RasSpeed;
import com.StarMicronics.StarIOSDK.RasterDocument.RasTopMargin;
import com.starmicronics.stario.StarPrinterStatus;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.ModifierObject;
import tme.pos.BusinessLayer.Receipt;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 3/1/2015.
 */
public class StarMicronics_TSP650II_BTI_Thermal_Printer implements IPrinter {
    final static int printableArea = 576; // for raster data
    final static int pricePrintableArea = (printableArea/3);
    final static int unitAndNamePrintableArea = ((printableArea/3)*2)-10;
    final static float spaceWidth = 6.754f;
    final static int fontSize = 13;


    final static int titleFontSize = 16;



    final static int maxCharacterPerLine = 40;//max 41
    final static int titleMaxCharacterPerLine = 25;
    final static int itemNameSize = 25;
    final static int modifierNameSize = 20;
    final static String strModifierSpaces = "           ";
    final static String strDrawLine ="-----------------------------------------------------------------------";
    final static int intTimeout=3000;
    static Context context;
    //String strPrinterName = "BT:Star Micronics";
    String strSettingsSSP="";
    int intPaperWidth=80;
    String strPrinterStatusMsg="";
    String strPrinterModel="unknown";
    String strFirmwareVersion="unknown";
    StarIOPort port;
    Receipt receipt;
    BluetoothPrinterListener BluetoothPrinterStatusHandler ;
    BluetoothPrintDriver printerDriver;
    public StarMicronics_TSP650II_BTI_Thermal_Printer(Context c, String strPrinterName,int intPaperWidth,BluetoothPrinterListener listener,BluetoothDevice device)
    {
      /*  context = c;
        this.intPaperWidth = intPaperWidth;
        BluetoothPrinterStatusHandler = listener;
        printerDriver = new BluetoothPrintDriver(c,BluetoothPrinterStatusHandler);
        printerDriver.connect(device);
        context = c;*/
        //Initialize(strPrinterName);


    }
    public StarMicronics_TSP650II_BTI_Thermal_Printer(BluetoothPrinterListener listener,BluetoothDevice device,Context c)
    {
        BluetoothPrinterStatusHandler = listener;
        printerDriver = new BluetoothPrintDriver(c,BluetoothPrinterStatusHandler);
        printerDriver.connect(device);
        context = c;
    }
    @Override
    public void Connect() {
        printerDriver.start();
    }
    @Override
    public String GetDeviceName() {
        return port.getPortName();
    }
    private void Initialize(String strPrinterName)
    {

        try
        {
           port = StarIOPort.getPort(strPrinterName,strSettingsSSP,intTimeout);
           strPrinterStatusMsg = "Connected";
            Map<String, String> firmware = port.getFirmwareInformation();

            strPrinterModel = firmware.get("ModelName");
            strFirmwareVersion = firmware.get("FirmwareVersion");
        }
        catch(StarIOPortException ex)
        {
            strPrinterStatusMsg = ex.getMessage();

        }
        catch(Exception ex)
        {
            strPrinterStatusMsg = ex.getMessage();

        }
    }
    public String GetStatus()
    {
        return strPrinterStatusMsg;
    }
    public String GetPrinterModel()
    {
        return strPrinterModel;
    }
    public String GetFirmwareVersion()
    {
        return strFirmwareVersion;
    }
    public void TestPrint() {

        PrintReceipt_80mm(context, port.getPortName(), port.getPortSettings(), Enum.PrinterCommandType.raster,context.getResources(),receipt);
        //PrintCreditCardSignatureReceipt(context,port.getPortName(),port.getPortSettings(), Enum.PrinterCommandType.raster,context.getResources(),receipt,"Customer Copy");
        //PrintCreditCardSignatureReceipt(context,port.getPortName(),port.getPortSettings(), Enum.PrinterCommandType.raster,context.getResources(),receipt,"Merchant Copy");

    }
    @Override
    public void Disconnect() {
        printerDriver.stop();
    }
   /* public void Disconnect()
    {
        try
        {
            if(port!=null)
            {
                StarIOPort.releasePort(port);
                port=null;
                ShowMessage("Printer","Disconnected",context);
            }
        }
        catch (StarIOPortException ex)
        {
            ShowMessage("Disconnect",ex.getMessage(),context);
        }
    }*/
    protected  void ShowMessage(String strTitle,String strMessage,Context context)
    {
        new AlertDialog.Builder(context)
                .setTitle(strTitle)
                .setMessage(strMessage)

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
    private  byte[] convertFromListByteArrayTobyteArray(List<byte[]> ByteArray) {
        int dataLength = 0;
        for (int i = 0; i < ByteArray.size(); i++) {
            dataLength += ByteArray.get(i).length;
        }

        int distPosition = 0;
        byte[] byteArray = new byte[dataLength];
        for (int i = 0; i < ByteArray.size(); i++) {
            System.arraycopy(ByteArray.get(i), 0, byteArray, distPosition, ByteArray.get(i).length);
            distPosition += ByteArray.get(i).length;
        }

        return byteArray;
    }
    private  void sendCommand(Context context, String portName, String portSettings, ArrayList<byte[]> byteList) {
        //StarIOPort port = null;
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            //port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */
            /*try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }*/

			/*
			 * Using Begin / End Checked Block method When sending large amounts of raster data,
			 * adjust the value in the timeout in the "StarIOPort.getPort" in order to prevent
			 * "timeout" of the "endCheckedBlock method" while a printing.
			 *
			 * If receipt print is success but timeout error occurs(Show message which is "There
			 * was no response of the printer within the timeout period." ), need to change value
			 * of timeout more longer in "StarIOPort.getPort" method.
			 * (e.g.) 10000 -> 30000
			 */
            StarPrinterStatus status = port.beginCheckedBlock();

            if (true == status.offline) {
                throw new StarIOPortException("A printer is offline");
            }

            byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
            port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

            port.setEndCheckedBlockTimeoutMillis(30000);// Change the timeout time of endCheckedBlock method.
            status = port.endCheckedBlock();

            if (status.coverOpen == true) {
                throw new StarIOPortException("Printer cover is open");
            } else if (status.receiptPaperEmpty == true) {
                throw new StarIOPortException("Receipt paper is empty");
            } else if (status.offline == true) {
                throw new StarIOPortException("Printer is offline");
            }
        } catch (StarIOPortException e) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("OK", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage(e.getMessage());
            alert.setCancelable(false);
            alert.show();
        } finally {
            //do not close the connection!!!!
            /*if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }*/
        }
    }
    @Override
    public boolean IsConnected() {
        return !printerDriver.IsNoConnection();
    }
   /* @Override
    public boolean IsConnected() {
        try {
            StarPrinterStatus status = port.beginCheckedBlock();

            if (false == status.offline) {
                return true;
            }
        }
        catch (StarIOPortException e) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("OK", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage(e.getMessage());
            alert.setCancelable(false);
            alert.show();
        }
        return false;
    }*/

    private static byte[] createRasterCommand(String printText, int textSize, int bold,Layout.Alignment alignment) {

        //ShowMessage("text to print",printText,context);
        byte[] command;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        Typeface typeface;

        try {
            typeface = Typeface.create(Typeface.SERIF, bold);
        } catch (Exception e) {
            typeface = Typeface.create(Typeface.DEFAULT, bold);
        }

        paint.setTypeface(typeface);
        paint.setTextSize(textSize * 2);
        paint.setLinearText(true);

        TextPaint textpaint = new TextPaint(paint);
        textpaint.setLinearText(true);
        android.text.StaticLayout staticLayout = new StaticLayout(printText, textpaint, printableArea, alignment, 1, 0, false);
        int height = staticLayout.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(staticLayout.getWidth(), height, Bitmap.Config.RGB_565);

        Canvas c = new Canvas(bitmap);
        c.drawColor(Color.WHITE);
        c.translate(0, 0);
        staticLayout.draw(c);

        StarBitmap starbitmap = new StarBitmap(bitmap, false, printableArea);

        command = starbitmap.getImageRasterDataForPrinting(true);

        return command;
    }


   private static void InsertCentralizeText(ArrayList<byte[]> list,String strText,int bold,int textSize,int charPerLine)
    {
        //perform wrap text
        String strLine="";
        String[] strings = strText.split("[ ]");
        for(int i=0;i<strings.length;i++)
        {
            if(strLine.length()==0){strLine = strings[i];}
            else{strLine += " "+strings[i];}

            //get whether appending the next string will exceed max char per line
            if(i+1<strings.length)
            {
                if((strLine.length()+1+strings[i+1].length())>charPerLine)
                {
                    //add to list
                    list.add(createRasterCommand(strLine.trim(), textSize, bold, Layout.Alignment.ALIGN_CENTER));
                    strLine="";
                }
            }
            else
            {
                //add to list
                list.add(createRasterCommand(strLine.trim(),textSize,bold,Layout.Alignment.ALIGN_CENTER));
                strLine="";
            }


        }
    }
    public static String ConvertBigDecimalToCurrencyFormat(BigDecimal price)
    {
        String strTemp= NumberFormat.getCurrencyInstance(java.util.Locale.US).format(price);
        if(strTemp.contains("("))
        {
            //negative value
            strTemp = strTemp.replace("(","");
            strTemp = strTemp.replace(")","");
            strTemp ="-"+strTemp;
        }


        return strTemp;
    }
    private static float ReturnTotalTextWidth(String strText,int textSize)
    {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        Typeface typeface;

        try {
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
        } catch (Exception e) {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
        }

        paint.setTypeface(typeface);
        paint.setTextSize(textSize * 2);
        paint.setLinearText(true);

        TextPaint textpaint = new TextPaint(paint);
        textpaint.setLinearText(true);

        float[] widths = new float[strText.length()];
        textpaint.getTextWidths(strText,widths);
        float total = 0f;
        for(float f:widths)total+=f;
        return total;
    }
    private static String FormattedString(String strText,int WidthNeeded,boolean blnAppendEnd)
    {

        float actualWidth = ReturnTotalTextWidth(strText, fontSize);
        float differ = WidthNeeded-actualWidth;
        if(differ<=0) {
            //ShowMessage("return","differ is "+differ,context);
            return strText;

        }

        float currentWidth=actualWidth;
        while(currentWidth<WidthNeeded && (currentWidth+spaceWidth)<WidthNeeded)
        {
            if(blnAppendEnd)
            {
                strText+=" ";
            }
            else
            {
                strText=" "+strText;
            }
            currentWidth +=spaceWidth;
        }
        //ShowMessage("text is ","["+strText+"]\n\r"+"currentWidth "+currentWidth+"\n\rwidthNeeded "+WidthNeeded,context);
        return strText;

    }
    private   void InsertModifier(ArrayList<byte[]>list,ModifierObject mo,int UnitOrder)
    {
        String[] strings = mo.getName().split(" ");
        boolean blnFirstLine = true;
        String textToPrint,strName="";
        BigDecimal bdPrice = new BigDecimal(mo.getPrice().floatValue() * UnitOrder);
        for(int i=0;i<strings.length;i++)
        {
            if(i==0)
            {
                strName = strings[i];
            }
            else {
                strName += " "+strings[i];
            }

            if((i+1)<strings.length)
            {
                if(strings[i+1].length()+strName.length()>modifierNameSize) {
                    if (blnFirstLine) {
                        textToPrint = FormattedString(strModifierSpaces+ strName, unitAndNamePrintableArea, true) +
                                FormattedString(ConvertBigDecimalToCurrencyFormat(bdPrice), pricePrintableArea, false);


                        blnFirstLine = false;
                    } else {
                        textToPrint = strModifierSpaces + strName;


                    }
                    list.add(createRasterCommand(textToPrint, fontSize, Typeface.NORMAL, Layout.Alignment.ALIGN_NORMAL));
                    strName = "";
                }
            }
            else
            {
                if(blnFirstLine) {
                    textToPrint  = FormattedString(strModifierSpaces+strName,unitAndNamePrintableArea,true)+
                            FormattedString(ConvertBigDecimalToCurrencyFormat(bdPrice),pricePrintableArea,false);


                    blnFirstLine = false;
                }
                else
                {
                    textToPrint  = strModifierSpaces +strName;


                }
                list.add(createRasterCommand(textToPrint,fontSize,Typeface.NORMAL, Layout.Alignment.ALIGN_NORMAL));
                strName="";
            }
        }
    }
    private  void InsertItem(ArrayList<byte[]> list,ItemObject io,int UnitOrder)
    {
        String textToPrint="";
        int UnitOrderSpacing = 3;
        int UnitOrderCompleteBlankSpace = 5;
        String strFormattedUnitOrderBlank = String.format("%"+UnitOrderCompleteBlankSpace+"s", " ");
        BigDecimal bdPrice = new BigDecimal(io.getPrice().floatValue() * UnitOrder);





        String[] strings = io.getName().split("[ ]");
        String strName = "";
        boolean blnFirstLine = true;
        for(int i=0;i<strings.length;i++)
        {
            if(i==0)
            {
                strName = strings[i];
            }
            else {
                strName += " "+strings[i];
            }


            if(UnitOrder<10)
            {
                UnitOrderSpacing = 5;
            }
            else if(UnitOrder>9 && UnitOrder<100)
            {
                UnitOrderSpacing = 4;
            }
            else
            {
                UnitOrderSpacing=0;
            }

            String strFormattedUnitOrder = UnitOrder+"";
            if(UnitOrderSpacing>0)
            {
                strFormattedUnitOrder = String.format("%"+UnitOrderSpacing+"s", UnitOrder);
            }

            if((i+1)<strings.length)
            {
                if(strings[i+1].length()+strName.length()>itemNameSize)
                {
                    //include price info if is 1st line
                    if(blnFirstLine) {
                        String string1 =FormattedString(strFormattedUnitOrder + " " +strName,unitAndNamePrintableArea,true);
                        String string2 =FormattedString(ConvertBigDecimalToCurrencyFormat(bdPrice),pricePrintableArea,false);
                        float width1 =ReturnTotalTextWidth(string1,fontSize);
                        float width2 =ReturnTotalTextWidth(string2,fontSize);
                        if(printableArea-(width1+width2)>6.5)string1+=" ";
                        textToPrint  =string1+string2;
                        //FormattedString(strFormattedUnitOrder + " " +strName,unitAndNamePrintableArea,true)+
                                //FormattedString(ConvertBigDecimalToCurrencyFormat(bdPrice),pricePrintableArea,false);


                        blnFirstLine = false;
                    }
                    else
                    {
                        textToPrint  = strFormattedUnitOrderBlank + " " +strName;


                    }
                    list.add(createRasterCommand(textToPrint,fontSize,Typeface.NORMAL, Layout.Alignment.ALIGN_NORMAL));
                    strName="";
                }
            }
            else
            {
                //include price info if is 1st line
                if(blnFirstLine) {
                    String string1 =FormattedString(strFormattedUnitOrder + " " +strName,unitAndNamePrintableArea,true);
                    String string2 =FormattedString(ConvertBigDecimalToCurrencyFormat(bdPrice),pricePrintableArea,false);
                    float width1 =ReturnTotalTextWidth(string1,fontSize);
                    float width2 =ReturnTotalTextWidth(string2,fontSize);
                    if(printableArea-(width1+width2)>6.5)string1+=" ";
                    textToPrint  =string1+string2;
                    //textToPrint  = FormattedString(strFormattedUnitOrder + " " +strName,unitAndNamePrintableArea,true)+
                            //FormattedString(ConvertBigDecimalToCurrencyFormat(bdPrice),pricePrintableArea,false);


                    blnFirstLine = false;
                }
                else
                {
                    textToPrint  = strFormattedUnitOrderBlank + " " +strName;


                }
                list.add(createRasterCommand(textToPrint,fontSize,Typeface.NORMAL, Layout.Alignment.ALIGN_NORMAL));
                strName="";
            }
        }
    }
    private  void PrintCreditCardSignatureReceipt(Context context, String portName, String portSettings, Enum.PrinterCommandType commandType, Resources res,Receipt receipt,String strVersion)
    {
        if (commandType == Enum.PrinterCommandType.raster) {
            //3inch (80mm) printing

            ArrayList<byte[]> list = new ArrayList<byte[]>();




            RasterDocument rasterDoc = new RasterDocument(RasSpeed.Medium, RasPageEndMode.FeedAndFullCut, RasPageEndMode.FeedAndFullCut, RasTopMargin.Default, 0, 0, 0);

            list.add(rasterDoc.BeginDocumentCommandData());

            String textToPrint = "";

            //text align center + wrap text
            InsertCentralizeText(list, receipt.companyProfile.CompanyName, Typeface.BOLD, titleFontSize, titleMaxCharacterPerLine);
            InsertCentralizeText(list, receipt.companyProfile.Street, Typeface.NORMAL, fontSize, maxCharacterPerLine);
            InsertCentralizeText(list, receipt.companyProfile.City.toString() + ", " + receipt.companyProfile.State + " " + receipt.companyProfile.Zipcode, Typeface.NORMAL, fontSize, maxCharacterPerLine);
            InsertCentralizeText(list, receipt.companyProfile.Phone + "    " + receipt.companyProfile.Email, Typeface.NORMAL, fontSize, maxCharacterPerLine);
            InsertCentralizeText(list, receipt.receiptNumber + "                " + receipt.tableNumber, Typeface.NORMAL, fontSize, maxCharacterPerLine);
            InsertCentralizeText(list, receipt.ReturnDateTimeString() + "    " + ((receipt.server.Name.length() > 0) ? "Server: " + receipt.server.Name : ""), Typeface.NORMAL, fontSize, maxCharacterPerLine);

            textToPrint =FormattedString(receipt.creditCard.CardType.name().toUpperCase() ,unitAndNamePrintableArea,true)+
                    FormattedString(ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount()),pricePrintableArea,false);
            list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));

            textToPrint="Card Number: ************"+receipt.creditCard.Number.substring(12);
            list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            textToPrint="Card Holder: "+receipt.creditCard.CardHolder;
            list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            textToPrint="Transaction: "+receipt.transactionId;
            list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));

            //signature section
            list.add(createRasterCommand("\n\r",2, 0,Layout.Alignment.ALIGN_NORMAL));
            textToPrint  = String.format("%30s", "Amount: ")+ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount());
            //textToPrint  = FormattedString(String.format("%20s", "Amount: "),unitAndNamePrintableArea,true)+ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount());
                    //FormattedString(ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount()),pricePrintableArea,false);
            list.add(createRasterCommand(textToPrint, 15, 0,Layout.Alignment.ALIGN_NORMAL));
            //InsertCentralizeText(list,String.format("%10s", "Amount: ")+ ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount()) ,Typeface.NORMAL,15,titleMaxCharacterPerLine);
            list.add(createRasterCommand("\n\r",2, 0,Layout.Alignment.ALIGN_NORMAL));
            textToPrint  =String.format("%30s", "Tip: ")+"_________________";
            //textToPrint  = FormattedString(String.format("%20s", "Tip: "),unitAndNamePrintableArea,true)+"_________________";
                    //FormattedString("_________________",pricePrintableArea,false);
            list.add(createRasterCommand(textToPrint, 15, 0,Layout.Alignment.ALIGN_NORMAL));
            //InsertCentralizeText(list,String.format("%10s", "Tip: ")+" _________________",Typeface.NORMAL,15,titleMaxCharacterPerLine);
            list.add(createRasterCommand("\n\r",2, 0,Layout.Alignment.ALIGN_NORMAL));
            textToPrint  = String.format("%30s", "Total: ")+"_________________";
            //textToPrint  = FormattedString(String.format("%20s", "Total: "),unitAndNamePrintableArea,true)+"_________________";
                    //FormattedString("_________________",pricePrintableArea,false);
            list.add(createRasterCommand(textToPrint, 15, 0,Layout.Alignment.ALIGN_NORMAL));
            //InsertCentralizeText(list,String.format("%10s", "Total: ")+"_________________",Typeface.NORMAL,15,titleMaxCharacterPerLine);
            list.add(createRasterCommand("\n\r",2, 0,Layout.Alignment.ALIGN_NORMAL));
            list.add(createRasterCommand("\n\r",2, 0,Layout.Alignment.ALIGN_NORMAL));
            InsertCentralizeText(list,String.format("%5s", "x")+"________________________",Typeface.NORMAL,15,titleMaxCharacterPerLine);
            list.add(createRasterCommand("\n\r",2, 0,Layout.Alignment.ALIGN_NORMAL));
            InsertCentralizeText(list, strVersion, Typeface.NORMAL, fontSize, titleMaxCharacterPerLine);

            list.add(rasterDoc.EndDocumentCommandData());

            list.add(Command.OpenCashDraw()); // Kick cash drawer

            sendCommand(context, portName, portSettings, list);
        }
    }
    public void OpenCashDrawer()
    {

        ArrayList<byte[]> lst = new ArrayList<byte[]>();
        lst.add(Command.OpenCashDraw());
        sendCommand(context, port.getPortName(), port.getPortSettings(),lst);
    }
    private void PrintReceipt_80mm(Context context, String portName, String portSettings, Enum.PrinterCommandType commandType, Resources res,Receipt receipt) {

        if (commandType == Enum.PrinterCommandType.raster)
        {
            //3inch (80mm) printing
            ArrayList<byte[]> list = new ArrayList<byte[]>();

            //printableArea = 576; // Printable area in paper is 832(dot)

            RasterDocument rasterDoc = new RasterDocument(RasSpeed.Medium, RasPageEndMode.FeedAndFullCut, RasPageEndMode.FeedAndFullCut, RasTopMargin.Default, 0, 0, 0);
            //RasterDocument rasterDoc = new RasterDocument(RasSpeed.Medium, RasPageEndMode.FeedAndFullCut, RasPageEndMode.FeedAndFullCut, -1, 0, 0, 0);
            list.add(rasterDoc.BeginDocumentCommandData());

            String textToPrint="";

            //text align center + wrap text
            InsertCentralizeText(list,receipt.companyProfile.CompanyName,Typeface.BOLD,titleFontSize,titleMaxCharacterPerLine);
            InsertCentralizeText(list, receipt.companyProfile.Street, Typeface.NORMAL, fontSize, maxCharacterPerLine);
            InsertCentralizeText(list,receipt.companyProfile.City.toString()+", "+receipt.companyProfile.State+" "+receipt.companyProfile.Zipcode,Typeface.NORMAL,fontSize,maxCharacterPerLine);
            InsertCentralizeText(list,receipt.companyProfile.Phone+"    "+receipt.companyProfile.Email,Typeface.NORMAL,fontSize,maxCharacterPerLine);
            InsertCentralizeText(list, receipt.receiptNumber + "                " + receipt.tableNumber, Typeface.NORMAL, fontSize, maxCharacterPerLine);
            InsertCentralizeText(list,receipt.receiptDateTime+"    "+((receipt.server.Name.length()>0)?"Server: "+receipt.server.Name:""),Typeface.NORMAL,fontSize,maxCharacterPerLine);

            if(receipt.HeaderNote.length()>0)
            {

                InsertCentralizeText(list,receipt.HeaderNote,Typeface.NORMAL,fontSize,maxCharacterPerLine);
            }
            list.add(createRasterCommand(strDrawLine, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));


            ArrayList<StoreItem> items = receipt.myCart.GetItems();
            for(StoreItem si:items)
            {

                InsertItem(list,si.item,si.UnitOrder);

                for(ModifierObject mo:si.modifiers)
                {
                    InsertModifier(list,mo,si.UnitOrder);
                }
                list.add(createRasterCommand("\n\r",2, 0,Layout.Alignment.ALIGN_NORMAL));
            }
            list.add(createRasterCommand(strDrawLine, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            //sub total
            textToPrint  = FormattedString("Amount" ,unitAndNamePrintableArea,true)+
                    FormattedString(ConvertBigDecimalToCurrencyFormat(receipt.myCart.getAmount()),pricePrintableArea,false);
            list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            //discount
            textToPrint  = ((receipt.GetCashValueForDiscount()>0)?FormattedString("Discount",unitAndNamePrintableArea,true)+FormattedString(ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForDiscount())),pricePrintableArea,false):"");
            if(textToPrint.length()>0)
            {
                list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            }
            //gratuity
            textToPrint  = ((receipt.GetCashValueForGratuity()>0)?FormattedString("Gratuity",unitAndNamePrintableArea,true)+FormattedString(ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForGratuity())),pricePrintableArea,false):"");
            if(textToPrint.length()>0)
            {
                list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            }
            //tax
            textToPrint  = FormattedString("Tax",unitAndNamePrintableArea,true)+FormattedString(ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.flTaxRate)),pricePrintableArea,false);
            if(textToPrint.length()>0)
            {
                list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            }
            list.add(createRasterCommand(strDrawLine, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            //total
            textToPrint =FormattedString("Total" ,unitAndNamePrintableArea,true)+
                    FormattedString(ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount()),pricePrintableArea,false);
            list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));

            //if(receipt.paymentType== Enum.PaymentType.cash)
            //{
            textToPrint =FormattedString("Paid" ,unitAndNamePrintableArea,true)+
                    FormattedString(ConvertBigDecimalToCurrencyFormat( new BigDecimal(receipt.PaidAmount)),pricePrintableArea,false);
            list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            textToPrint =FormattedString("Changed" ,unitAndNamePrintableArea,true)+
                    FormattedString(ConvertBigDecimalToCurrencyFormat( new BigDecimal(receipt.PaidAmount).subtract(receipt.ReturnReceiptFinalTotalAmount())),pricePrintableArea,false);
            list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            //}
            list.add(createRasterCommand(strDrawLine, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
            //if(receipt.paymentType== Enum.PaymentType.credit)
            //{
            textToPrint =FormattedString(receipt.creditCard.CardType.name().toUpperCase() ,unitAndNamePrintableArea,true)+
                    FormattedString(ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount()),pricePrintableArea,false);
            list.add(createRasterCommand(textToPrint, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));

            textToPrint="Card Number: ************"+receipt.creditCard.Number.substring(12);
            list.add(createRasterCommand(textToPrint, fontSize, 0, Layout.Alignment.ALIGN_NORMAL));
            textToPrint="Transaction: "+receipt.transactionId;
            list.add(createRasterCommand(textToPrint, fontSize, 0, Layout.Alignment.ALIGN_NORMAL));
            if(receipt.FooterNote.length()>0)
            {
                list.add(createRasterCommand(strDrawLine, fontSize, 0,Layout.Alignment.ALIGN_NORMAL));
                InsertCentralizeText(list,receipt.FooterNote,Typeface.NORMAL,fontSize,maxCharacterPerLine);
            }



            list.add(rasterDoc.EndDocumentCommandData());


            //list.add(Command.OpenCashDraw()); // Kick cash drawer

            sendCommand(context, portName, portSettings, list);
            OpenCashDrawer();
       }

    }
    public void PrintReceipt(Receipt receipt)
    {
        //PrintReceipt_80mm(context, port.getPortName(), port.getPortSettings(), Enum.PrinterCommandType.raster,context.getResources(),receipt);
        PrintReceiptHeaderNote(receipt,maxCharacterPerLine);
    }
    private void PrintReceiptHeaderNote(Receipt receipt,int maxCharacterPerLine)
    {
        printerDriver.Begin();
        printerDriver.LF();
        PrintLine(receipt.HeaderNote, maxCharacterPerLine);
        printerDriver.LF();
    }
    private void PrintLine(String strLine,int maxChar)
    {
        if(strLine.length()==0)return;

        int totalLength = strLine.length();
        if(totalLength<=maxChar)//21)
        {
            printerDriver.BT_Write(strLine);
        }
        else
        {

            int intEndIndex =-1;
            while(strLine.length()>0)
            {

                intEndIndex = common.Utility.GetEndIndex(strLine,maxChar);

                printerDriver.BT_Write(strLine.substring(0,intEndIndex));
                printerDriver.LF();
                if(intEndIndex!=strLine.length())
                    strLine = strLine.substring(intEndIndex);
                else break;
            }
        }
    }

}
