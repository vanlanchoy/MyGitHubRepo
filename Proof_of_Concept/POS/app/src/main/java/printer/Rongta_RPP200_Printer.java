package printer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

//import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import tme.pos.BusinessLayer.CartDisplayItem;
import tme.pos.BusinessLayer.CompanyProfile;
import tme.pos.BusinessLayer.ConvertUtil;
import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.ModifierObject;
import tme.pos.BusinessLayer.MyCart;
import tme.pos.BusinessLayer.PromotionAwarded;
import tme.pos.BusinessLayer.Receipt;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.CreditCard;
import tme.pos.BusinessLayer.Server;
import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.common;
import tme.pos.R;

/**
 * Created by vanlanchoy on 10/25/2015.
 */
public class Rongta_RPP200_Printer implements IPrinter {
    BluetoothPrinterListener BluetoothPrinterStatusHandler ;
    BluetoothPrintDriver printerDriver;

    Context context;
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
    final static int EnlargedFontMaxCharPerLine=21;
    final static int RegularFontMaxCharPerLine=39;
    final static int RegularDashMaxCharPerLine=42;
    final static int RegularSummaryLineOffset=2;

    final static byte ALIGNMENT_LEFT=(byte)0;
    final static byte ALIGNMENT_CENTER=(byte)1;
    final static byte ALIGNMENT_RIGHT=(byte)2;


    final static byte FONT_REGULAR=(byte)0x00;
    final static byte FONT_ENLARGE_WIDTH_AND_HEIGHT=(byte)0x11;
    final static byte FONT_ENLARGE_WIDTH=(byte)0x10;
    final static byte FONT_ENLARGE_HEIGHT=(byte)0x01;

    final static byte NO_UNDERLINE=(byte)0x00;
    final static byte UNDERLINE=(byte)0x02;

    final static byte UNBOLD=(byte)0x00;
    final static byte BOLD=(byte)0x01;

    final static byte CHARACTER_FONT_NORMAL=(byte)0x00;
    final static byte CHARACTER_FONT_MINI=(byte)0x01;

    final static byte NORMAL_PRINT=(byte)0x00;
    final static byte REVERSE_PRINT=(byte)0x01;

    byte[] readyToPrintBitmap=null;
    int existingBitmapLength=0;
    @Override
    public void Disconnect() {
        printerDriver.stop();
    }

    public Rongta_RPP200_Printer(BluetoothPrinterListener listener,BluetoothDevice device,Context c)
    {
        BluetoothPrinterStatusHandler = listener;
        printerDriver = new BluetoothPrintDriver(c,BluetoothPrinterStatusHandler);
        printerDriver.connect(device);
        context = c;
    }
    public String GetDeviceName()
    {
        return BluetoothPrinterStatusHandler.GetConnectedDeviceName();
    }



    @Override
    public void Connect() {

        try {
            printerDriver.start();
        }
        catch(NoClassDefFoundError ex)
        {
            common.Utility.ShowMessage("Printer","Cannot connect to printer, make sure you have selected the correct name.",context, R.drawable.no_access);
        }
    }
    private void PrintReceiptHeaderNote(Receipt receipt,int maxCharacterPerLine)
    {
        PrintLine(receipt.HeaderNote, maxCharacterPerLine);
        printerDriver.LF();
    }
    private void PrintReceiptFooterNote(Receipt receipt,int maxCharacterPerLine)
    {
        PrintLine(receipt.FooterNote, maxCharacterPerLine);
        printerDriver.LF();
    }
    private void PrintReceiptDateAndTable(Receipt receipt, int maxCharacterPerLine)
    {
        if(receipt.printReceiptType!= Enum.PrintReceiptType.total)
        {
            PrintLine(receipt.ReturnDateTimeString() + "    " + receipt.tableNumber, maxCharacterPerLine);
        }
        else
        {
            PrintLine("                   " + "    " + receipt.tableNumber, maxCharacterPerLine);
        }

        printerDriver.LF();
    }
    void PrintCode93(String strContent)
    {
        int store_len = strContent.length() + 3;
        byte store_pL = (byte) (store_len % 256);
        byte store_pH = (byte) (store_len / 256);
        byte[] bytes=new byte[]{
                29,72,50
                //,29,76,store_pL,store_pH
                //,29,76,0,0
                ,29,107,72,(byte)strContent.length()};

        byte[] contentBytes=strContent.getBytes();
        byte[] cmdPrint = new byte[bytes.length+contentBytes.length];
        int index=0;
        for(int i=0;i<bytes.length;i++)
        {
            cmdPrint[index++]=bytes[i];
        }


        for (int i =0;i<contentBytes.length;i++)
        {
            cmdPrint[index++]=contentBytes[i];
        }

        printerDriver.BT_Write(cmdPrint);
    }
    private void PrintReceiptNumberAndServer(Receipt receipt,int maxCharacterPerLine)
    {
        if(common.myAppSettings.GetPrintBarcodeFlag()) {

            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.CR();
            if(receipt.receiptNumber.length()>0 ){//&& receipt.printReceiptType== Enum.PrintReceiptType.total) {
                //BluetoothPrintDriver.SetAlignMode(ALIGNMENT_CENTER);
                BluetoothPrintDriver.SetAlignMode(ALIGNMENT_LEFT);
                PrintCode93(receipt.receiptNumber);
                //BluetoothPrintDriver.LF();
            }
            //BluetoothPrintDriver.SetAlignMode(ALIGNMENT_LEFT);
            if(receipt.server !=null && receipt.server.Name.length()>0) {
                PrintLine(" Server: "+receipt.server.Name,maxCharacterPerLine);
            }
        }
        else {
            if(receipt.receiptNumber.length()>0)// && receipt.printReceiptType!= Enum.PrintReceiptType.total)
            PrintLine("Receipt#: "+receipt.receiptNumber , maxCharacterPerLine);

            if(receipt.server !=null && receipt.server.Name.length()>0)
            PrintLine(" Server: "+receipt.server.Name,maxCharacterPerLine);
            printerDriver.LF();
        }
        //printerDriver.LF();
    }

    private void PrintContact(Receipt receipt, int maxCharacterPerLine)
    {
        //PrintLine(receipt.companyProfile.Phone + "    " + receipt.companyProfile.Email, maxCharacterPerLine);
        PrintLine(common.companyProfile.Phone + "    " + common.companyProfile.Email, maxCharacterPerLine);
        printerDriver.LF();
        printerDriver.BT_Write("");
    }
    private void PrintCompanyName(Receipt receipt,int maxCharacterPerLine)
    {
        //PrintLine(receipt.companyProfile.CompanyName, maxCharacterPerLine);
        PrintLine(common.companyProfile.CompanyName, maxCharacterPerLine);
        printerDriver.LF();

    }
    private void PrintCompanyAddress(Receipt receipt,int maxCharacterPerLine){

        /*PrintLine(receipt.companyProfile.Street,maxCharacterPerLine);
        String strLine =( (receipt.companyProfile.City.length()>0)?" "+receipt.companyProfile.City+", ":"")
                +( (receipt.companyProfile.City.length()>0)?receipt.companyProfile.State+" ":"")
                +( (receipt.companyProfile.Zipcode.length()>0)?receipt.companyProfile.Zipcode:"");*/
        PrintLine(common.companyProfile.Street,maxCharacterPerLine);
        String strLine =( (common.companyProfile.City.length()>0)?" "+common.companyProfile.City+", ":"")
                +( (common.companyProfile.City.length()>0)?common.companyProfile.State+" ":"")
                +( (common.companyProfile.Zipcode.length()>0)?common.companyProfile.Zipcode:"");
        PrintLine(strLine, maxCharacterPerLine);
        printerDriver.LF();
    }
    private void PrintReceiptItems(Receipt receipt,int maxCharacterPerLine)
    {
        //no promotion item, just print regular item list
        if(receipt.myCart.GetDisplayCartItemList().size()==0)
        {
            for(int i=0;i<receipt.myCart.GetItems().size();i++)
                PrintStoreItem(receipt.myCart.GetItems().get(i),maxCharacterPerLine);
            return;
        }

        ArrayList<CartDisplayItem> cartItems = receipt.myCart.GetDisplayCartItemList();
        for(int i=0;i<cartItems.size();i++)
        {
            if(cartItems.get(i).cit== Enum.CartItemType.StoreItem)
            {
                PrintStoreItem(cartItems.get(i).si,maxCharacterPerLine);
            }
            else
            {
                PrintPromotionAwarded(cartItems.get(i).pa,maxCharacterPerLine,receipt.myCart.receiptIndex);
            }
        }
        printerDriver.LF();
    }
    private void PrintPromotionAwarded(PromotionAwarded pa, int maxCharacterPerLine, int receiptIndex)
    {
        int intUnitCountWidth=3;
        int intPriceWidth=14;
        maxCharacterPerLine-=3;
        int intNameWidth;//=maxCharacterPerLine-intUnitCountWidth-intPriceWidth-2;//minus two for white spaces

        String strUnit="";
        int intShareBy =pa.ShareByHowManyReceipts();
        if(intShareBy==1)
        {

            strUnit = pa.unit+"";//+"x ";
        }
        else
        {

            strUnit="("+pa.unit+"/"+intShareBy+")";//x ";
        }


        /************************************************************/

        /***first four chars is reserved for unit count + space***/
        String strUnitCount = strUnit;// si.UnitOrder+"";
        while(strUnitCount.length()<intUnitCountWidth+1)strUnitCount = " "+strUnitCount;

        //update combo name width again
        intNameWidth=maxCharacterPerLine-strUnit.length()-intPriceWidth-1;//minus one for white spaces after item name

        /**third part will be 14 chars long for price tag**/
        BigDecimal bdPrice = pa.GetTotalDiscountAwarded((receiptIndex==pa.GetSharedReceiptIndex().get(0))?true:false,receiptIndex);
        String strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(bdPrice);
        while(strPrice.length()<intPriceWidth+1)strPrice=" "+strPrice;

        /***second part will be 24 chars consisting item name + space***/
        String strItemName = pa.promotionObject.GetTitle();
        int intEndIndex=-1;
        boolean blnFirstLine=true;
        printerDriver.LF();
        if(strItemName.length()>intNameWidth)
        {
            while(strItemName.length()>0) {
                intEndIndex = common.Utility.GetEndIndex(strItemName, intNameWidth);
                String strLine = strItemName.substring(0, intEndIndex);
                while(strLine.length()<intNameWidth+1)strLine+=" ";

                if (blnFirstLine) {
                    //1st line with unit and price info
                    blnFirstLine = false;
                    printerDriver.BT_Write(strUnitCount+" "+strLine+" "+strPrice);
                    printerDriver.LF();
                    //reset the unit and price string for next line use
                    strUnitCount="";
                    while(strUnitCount.length()<intUnitCountWidth+1)strUnitCount+=" ";
                    strPrice="";
                    while(strPrice.length()<intPriceWidth+1)strPrice+=" ";

                } else {
                    //remaining of item name
                    printerDriver.BT_Write(strUnitCount+" "+strLine+" "+strPrice);
                    printerDriver.LF();
                }


                if (intEndIndex != strItemName.length())
                    strItemName = strItemName.substring(intEndIndex).trim();
                else break;
            }
        }
        else
        {
            while(strItemName.length()<intNameWidth+1)strItemName+=" ";

            printerDriver.BT_Write(strUnitCount+" "+strItemName+" "+strPrice);
        }
        printerDriver.LF();




    }
    public void OpenCashDrawer()
    {
        /**Does not supported**/
    }
    private void PrintStoreItem(StoreItem si, int maxCharacterPerLine)
    {
        int intUnitCountWidth=3;
        int intPriceWidth=14;
        int intNameWidth=maxCharacterPerLine-intUnitCountWidth-intPriceWidth-2;//minus two for white spaces

        int intModifierUnitPlaceHolder=6;
        int intModifierNameWidth=maxCharacterPerLine-intModifierUnitPlaceHolder-intPriceWidth-2;//minus two for white spaces in between price section and unit section
        //ArrayList<String>lines = new ArrayList<String>();

        /***first four chars is reserved for unit count + space***/
        String strUnitCount = si.UnitOrder+"";
        while(strUnitCount.length()<intUnitCountWidth+1)strUnitCount = " "+strUnitCount;

        /**third part will be 14 chars long for price tag**/
        BigDecimal bdPrice = new BigDecimal(si.item.getPrice().floatValue()*si.UnitOrder);
        String strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(bdPrice);
        while(strPrice.length()<intPriceWidth+1)strPrice=" "+strPrice;

        /***second part will be 24 chars consisting item name + space***/
        String strItemName = si.item.getName();
        int intEndIndex=-1;
        boolean blnFirstLine=true;
        printerDriver.LF();
        if(strItemName.length()>intNameWidth)
        {
            while(strItemName.length()>0) {
                intEndIndex = common.Utility.GetEndIndex(strItemName, intNameWidth);
                String strLine = strItemName.substring(0, intEndIndex);
                while(strLine.length()<intNameWidth+1)strLine+=" ";

                if (blnFirstLine) {
                    //1st line with unit and price info
                    blnFirstLine = false;
                    printerDriver.BT_Write(strUnitCount+" "+strLine+" "+strPrice);
                    printerDriver.LF();
                    //reset the unit and price string for next line use
                    strUnitCount="";
                    while(strUnitCount.length()<intUnitCountWidth+1)strUnitCount+=" ";
                    strPrice="";
                    while(strPrice.length()<intPriceWidth+1)strPrice+=" ";

                } else {
                    //remaining of item name
                    printerDriver.BT_Write(strUnitCount+" "+strLine+" "+strPrice);
                    printerDriver.LF();
                }


                if (intEndIndex != strItemName.length())
                    strItemName = strItemName.substring(intEndIndex).trim();
                else break;
            }
        }
        else
        {
            while(strItemName.length()<intNameWidth+1)strItemName+=" ";

            printerDriver.BT_Write(strUnitCount+" "+strItemName+" "+strPrice);
        }
        printerDriver.LF();

        /**Printing for modifiers**/

        /**modifier does not have unit count so the space will be filled with six spaces**/
        String strPlaceHolder="";
        while(strPlaceHolder.length()<intModifierUnitPlaceHolder+1)strPlaceHolder+=" ";


        for(int i=0;i<si.modifiers.size();i++)
        {
            String strMo = si.modifiers.get(i).getName();


            /**third part will be 14 chars long for price tag**/
            bdPrice = new BigDecimal(si.modifiers.get(i).getPrice().floatValue()*si.UnitOrder);
            strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(bdPrice);
            while(strPrice.length()<intPriceWidth+1)strPrice=" "+strPrice;

            /**reset flag **/
            intEndIndex=-1;
            blnFirstLine=true;
            if(strMo.length()>intModifierNameWidth)
            {
                while(strMo.length()>0) {
                    intEndIndex = common.Utility.GetEndIndex(strMo, intModifierNameWidth);
                    String strLine = strMo.substring(0, intEndIndex);
                    while (strLine.length() < intModifierNameWidth + 1) strLine += " ";

                    if (blnFirstLine)
                    {

                        //1st line with unit and price info
                        blnFirstLine = false;
                        printerDriver.BT_Write(strPlaceHolder+" "+strLine+" "+strPrice);
                        printerDriver.LF();
                        //reset the unit and price string for next line use
                        strUnitCount="";
                        while(strUnitCount.length()<intUnitCountWidth+1)strUnitCount+=" ";
                        strPrice="";
                        while(strPrice.length()<intPriceWidth+1)strPrice+=" ";

                    }
                    else
                    {
                        while(strLine.length()<intModifierNameWidth+1)strLine+=" ";
                        printerDriver.BT_Write(strUnitCount+" "+strLine+" "+strPrice);
                    }

                    if (intEndIndex != strMo.length())
                        strMo = strMo.substring(intEndIndex).trim();
                    else break;
                }
            }
            else
            {
                while(strMo.length()<intModifierNameWidth+1)strMo+=" ";

                printerDriver.BT_Write(strPlaceHolder+" "+strMo+" "+strPrice);
            }
            printerDriver.LF();
        }
    }
    private void PrintReceiptSummary(Receipt receipt,int maxCharacterPerLine)
    {
        /**Amount**/
        String strSubTotalLine ="Amount";
        String strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.myCart.getAmount());
        while(strSubTotalLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strSubTotalLine+=" ";
        strSubTotalLine+=strPrice;
        printerDriver.BT_Write(strSubTotalLine);
        printerDriver.LF();

        /**promotion discount for total amount**/
        if(receipt.myCart.promotionObject!=null)
        {
            String strPromotion=receipt.myCart.promotionObject.GetTitle();
            String strPromotionDiscount = common.Utility.ConvertBigDecimalToCurrencyFormat((receipt.myCart.GetPromotionByCashAmount()));
            String strLine="";
            //int index=0;
            //boolean blnFirstLine=true;
            int remaining=0;
            //int space=RegularSummaryLineOffset;
                //if(blnFirstLine)
                //{
            remaining = maxCharacterPerLine-strPromotionDiscount.length()-RegularSummaryLineOffset;
            int lastIndex= common.Utility.GetEndIndex(strPromotion,remaining);
            strLine=strPromotion.substring(0,lastIndex);//remaining);
            while(strLine.length()+strPromotionDiscount.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strLine+=" ";
            strLine+=strPromotionDiscount;


            printerDriver.BT_Write(strLine);
            printerDriver.LF();

            if(lastIndex<strPromotion.length()) {

                while(strPromotion.substring(lastIndex,lastIndex+1)==" ")lastIndex++;

                printerDriver.BT_Write(strPromotion.substring(lastIndex).trim());//remaining));
                printerDriver.LF();
            }


        }
        /**discount**/
        if(receipt.GetCashValueForDiscount()>0.009)
        {
            String strDiscountLine="Discount";
            strPrice = "-"+common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForDiscount()));
            while(strDiscountLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strDiscountLine+=" ";
            strDiscountLine+=strPrice;
            printerDriver.BT_Write(strDiscountLine);
            printerDriver.LF();
        }

        /**Gratuity**/
        if(receipt.GetCashValueForGratuity()>0.009)
        {
            String strGratuityLine="Gratuity";
            strPrice =common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForGratuity()));
            // common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForGratuity()));
            while(strGratuityLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strGratuityLine+=" ";
            strGratuityLine+=strPrice;
            printerDriver.BT_Write(strGratuityLine);
            printerDriver.LF();
        }

        /**Tax**/
        String strTaxLine="Tax";
        strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.GetTaxAmountAfterAmountCashPromotionDiscount());
        //common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.GetTaxAmountAfterAmountCashPromotionDiscount());
        while(strTaxLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strTaxLine+=" ";
        strTaxLine+=strPrice;
        printerDriver.BT_Write(strTaxLine);
    }
    private void PrintReceiptTotalSection(Receipt receipt,int maxCharacterPerLine,Enum.PrintReceiptType printReceiptType)
    {
        String strTotalLine ="Total";
        String strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount());//common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount());
        String strTotal=strPrice;
        while(strTotalLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strTotalLine+=" ";
        strTotalLine+=strPrice;
        printerDriver.BT_Write(strTotalLine);
        printerDriver.LF();
        if(printReceiptType== Enum.PrintReceiptType.change_balance)
        {
            /**Paid amount line**/
            String strPaidLine ="Paid";
            strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.PaidAmount));
            while(strPaidLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strPaidLine+=" ";
            strPaidLine+=strPrice;
            printerDriver.BT_Write(strPaidLine);
            printerDriver.LF();
            /** changed line**/
            String strChanged ="Changed";
            BigDecimal bdTemp = new BigDecimal( Float.parseFloat(strPrice.replaceAll("[$,-]",""))-receipt.ReturnReceiptFinalTotalAmount().floatValue()).setScale(2, RoundingMode.HALF_UP);
            strPrice=common.Utility.ConvertBigDecimalToCurrencyFormat(bdTemp);
            //strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.PaidAmount).subtract(receipt.ReturnReceiptFinalTotalAmount()));
            while(strChanged.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strChanged+=" ";
            strChanged+=strPrice;
            printerDriver.BT_Write(strChanged);
        }
        else if(printReceiptType== Enum.PrintReceiptType.credit_card_balance)
        {
            /**credit card info**/
            if(receipt.creditCard==null)return;
            String strCard ="Cart type: "+receipt.creditCard.CardType.name().toUpperCase();
            while(strCard.length()+strTotal.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strCard+=" ";//reusing previously saved total value
            strCard+=strTotal;
            printerDriver.BT_Write(strCard);
            printerDriver.LF();
            /** card # line**/
            String strCardNumber ="Card #: ************"+receipt.creditCard.Number.substring(12);
            while(strCardNumber.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strCardNumber+=" ";
            printerDriver.BT_Write(strCardNumber);
        }
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

    private void PrintQRCode(String strUrl)
    {
        int store_len = strUrl.length() + 3;
        byte store_pL = (byte) (store_len % 256);
        byte store_pH = (byte) (store_len / 256);

        byte[] content = strUrl.getBytes();

        byte[] cmd1 = new byte[]{
                0x1b,0x40
                ,0x1d,0x28,0x6b,0x04,0x00,49,65,50,0
        ,0x1d,0x28,0x6b,0x03,0x00,0x31,0x43,0x06
        ,0x1d,0x28,0x6b,0x03,0x00,0x31,0x45,0x33
                ,0x1d,0x28,0x6b,store_pL,store_pH,0x31,0x50,0x30};
        //,0x1d,0x28,0x6b,0x06,0x00,0x31,0x50,0x30};

        byte[] cmd2 = new byte[]{
                0x1b,0x61,0x01
                ,0x1d,0x28,0x6b,0x03,0x00,0x31,0x52,0x30
                ,0x1d,0x28,0x6b,0x03,0x00,0x31,0x51,0x30};
        byte[] cmdAll = new byte[content.length+ cmd1.length+cmd2.length];
        int index=0;
        for(int i=0;i<cmd1.length;i++)
        {
            cmdAll[index++] = cmd1[i];
        }
        for(int i=0;i<content.length;i++)
        {
            cmdAll[index++]=content[i];
        }
        for(int i=0;i<cmd2.length;i++)
        {
            cmdAll[index++] = cmd2[i];
        }

        printerDriver.BT_Write(cmdAll);

        printerDriver.LF();
    }
    private void PrintLogo(byte[] source)
    {


        //line spacing only available for text and QR code printing
        //printerDriver.BT_Write(new byte[]{(byte)27, (byte)51, (byte)100});//line spacing
        //printerDriver.BT_Write(new byte[]{(byte)27, (byte)66, (byte)5});//line spacing
        //printerDriver.BT_Write("test string alignment");
        byte[] content=null;
        if(readyToPrintBitmap!=null)
        {
            content = readyToPrintBitmap;
        }
        else {
            content = decodeBitmap(BitmapFactory.decodeByteArray(source, 0, source.length));
            readyToPrintBitmap = content;
        }
        if(content==null)
        {
            Toast.makeText(context,"Failed to print logo.",Toast.LENGTH_SHORT);
            return;
        }
        byte[] newContent=new byte[3+content.length];
        newContent[0]=0x1b;
        newContent[1]=0x33;
        newContent[2]=1;
        for(int i=0;i<content.length;i++)
        {
            newContent[i+3]=content[i];
        }
        //printerDriver.BT_Write(new byte[]{0x1B, 0x33, 24});line spacing 24 not working
        printerDriver.BT_Write(newContent);
        printerDriver.LF();

        //reset line spacing
        printerDriver.BT_Write(new byte[]{0x1B, 0x32});
    }

    public static byte[] decodeBitmap(Bitmap bmp){
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        List<String> list = new ArrayList<String>(); //binaryString list
        StringBuffer sb;

        // 每行字节数(除以8，不足补0)
        int bitLen = bmpWidth / 8;
        int zeroCount = bmpWidth % 8;
        // 每行需要补充的0
        String zeroStr = "";
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1;
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr = zeroStr + "0";
            }
        }
        // 逐个读取像素颜色，将非白色改为黑色
        for (int i = 0; i < bmpHeight; i++) {
            sb = new StringBuffer();
            for (int j = 0; j < bmpWidth; j++) {
                int color = bmp.getPixel(j, i); // 获得Bitmap 图片中每一个点的color颜色值
                //颜色值的R G B
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;

                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160)
                    sb.append("0");
                else
                    sb.append("1");
            }
            // 每一行结束时，补充剩余的0
            if (zeroCount > 0) {
                sb.append(zeroStr);
            }
            list.add(sb.toString());
        }
        // binaryStr每8位调用一次转换方法，再拼合
        List<String> bmpHexList = ConvertUtil.binaryListToHexStringList(list);
        String commandHexString = "1D763000";
        // 宽度指令
        String widthHexString = Integer
                .toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8
                        : (bmpWidth / 8 + 1));
        if (widthHexString.length() > 2) {
            Log.e("decodeBitmap error", "宽度超出 width is too large");
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString = widthHexString + "00";

        // 高度指令
        String heightHexString = Integer.toHexString(bmpHeight);
        if (heightHexString.length() > 2) {
            Log.e("decodeBitmap error", "高度超出 height is too large");
            return null;
        } else if (heightHexString.length() == 1) {
            heightHexString = "0" + heightHexString;
        }
        heightHexString = heightHexString + "00";

        List<String> commandList = new ArrayList<String>();
        commandList.add(commandHexString+widthHexString+heightHexString);
        commandList.addAll(bmpHexList);

        return ConvertUtil.hexList2Byte(commandList);
    }

    public void PrintReceipt(Receipt receipt)
    {
        if(printerDriver.IsNoConnection())
        {
            Toast.makeText(context, "Device not connected", Toast.LENGTH_SHORT).show();
           return;
        }

        /**Total Type
         * 1. no header note
         * 2. no footer note
         * 3. one total line at the end
         * **/

        String strDashLine="";
        while(strDashLine.length()<RegularDashMaxCharPerLine)strDashLine+="-";

        //receipt = CreateTestReceipt(context);
        printerDriver.Begin();
        printerDriver.LF();
        //printerDriver.SetChineseCharacterMode((byte)1);

        /*********************  LOGO   *****************************/
        //if(receipt.companyProfile.Logo!=null) {
        if(common.companyProfile.Logo!=null) {

            /*if(existingBitmapLength!=receipt.companyProfile.Logo.length){
                readyToPrintBitmap=null;
                existingBitmapLength = receipt.companyProfile.Logo.length;
            }*/
            if(existingBitmapLength!=common.companyProfile.Logo.length){
                readyToPrintBitmap=null;
                existingBitmapLength = common.companyProfile.Logo.length;
            }
            PrintLogo(common.companyProfile.Logo);

        }


        /********************  Company profile   ****************************/
        //set large font size
        //max 21 characters
        printerDriver.SetAlignMode(ALIGNMENT_CENTER);
        printerDriver.SetFontEnlarge(FONT_ENLARGE_WIDTH_AND_HEIGHT);
        PrintCompanyName(receipt, EnlargedFontMaxCharPerLine);
        printerDriver.SetFontEnlarge((byte) 0x00);//reset font size
        PrintCompanyAddress(receipt, RegularFontMaxCharPerLine);

        PrintContact(receipt, RegularFontMaxCharPerLine);

        PrintReceiptNumberAndServer(receipt, RegularFontMaxCharPerLine);

        PrintReceiptDateAndTable(receipt, RegularFontMaxCharPerLine);

        /*********************   set header note alignment before printing   *******************/
        if(receipt.printReceiptType== Enum.PrintReceiptType.change_balance
                || receipt.printReceiptType== Enum.PrintReceiptType.credit_card_balance) {
            if (receipt.HeaderNoteAlignment == Enum.ReceiptNoteAlignment.left) {
                printerDriver.SetAlignMode(ALIGNMENT_LEFT);
            }
            PrintReceiptHeaderNote(receipt, RegularFontMaxCharPerLine);



        }
        /*****************************  print order items *******************/
        printerDriver.BT_Write(strDashLine);
        printerDriver.LF();
        printerDriver.SetAlignMode(ALIGNMENT_LEFT);
        PrintReceiptItems(receipt, RegularFontMaxCharPerLine);
        printerDriver.LF();
        printerDriver.BT_Write(strDashLine);
        PrintReceiptSummary(receipt, RegularFontMaxCharPerLine);
        printerDriver.LF();
        printerDriver.BT_Write(strDashLine);
        printerDriver.LF();
        PrintReceiptTotalSection(receipt, RegularFontMaxCharPerLine,receipt.printReceiptType);
        printerDriver.LF();
        /********************   Footer note  ************************/
        if(receipt.printReceiptType== Enum.PrintReceiptType.change_balance
                || receipt.printReceiptType== Enum.PrintReceiptType.credit_card_balance)
        {
            if (receipt.HeaderNoteAlignment == Enum.ReceiptNoteAlignment.center) {
                printerDriver.SetAlignMode(ALIGNMENT_CENTER);
            }
            if(receipt.FooterNote.length()>0)printerDriver.BT_Write(strDashLine);
            PrintReceiptFooterNote(receipt,RegularFontMaxCharPerLine);
        }

        /********************   QR Code  ************************/
        if(common.myAppSettings.GetReceiptQRCodeUrlText().trim().length()>0)
        {

            PrintQRCode(common.myAppSettings.GetReceiptQRCodeUrlText().trim());
        }

    }
    @Override
    public boolean IsConnected() {
        return !printerDriver.IsNoConnection();
    }
}
