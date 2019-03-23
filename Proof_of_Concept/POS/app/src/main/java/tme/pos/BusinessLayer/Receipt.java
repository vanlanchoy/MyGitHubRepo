package tme.pos.BusinessLayer;


import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;


/**
 * Created by vanlanchoy on 3/28/2015.
 */
public class Receipt implements Parcelable, Cloneable{
    public MyCart myCart=null;
    public Server server;
    public String HeaderNote;
    public String FooterNote;
    public Enum.ReceiptNoteAlignment HeaderNoteAlignment;
    public Enum.ReceiptNoteAlignment FooterNoteAlignment;
    public CompanyProfile companyProfile;
    public float flTaxRate=0;
    float CashValueForGratuity=0;
    float PercentageValueForGratuity=0;
    float CashValueForDiscount=0;
    float PercentageValueForDiscount=0;
    public float PaidAmount=0;
    public Enum.PaymentType paymentType;
    Enum.DiscountType discountType;
    public CreditCard creditCard;
    public String receiptNumber;
    public String tableNumber;
    public Calendar receiptDateTime;
    public String transactionId;
    public double dbLongitude = 0;
    public double dbLatitude=0;
    public float flTotal=0;
    public boolean blnActive=true;
    public Enum.PrintReceiptType printReceiptType;
    final static String newLine="line.separator";
    public boolean blnHasPaid=false;
    public int Version=1;
    public long lngLastUpdateDate=0;
    public String strLinkedReceipts="";
    public Receipt(MyCart mc,float percentage,CompanyProfile cp,
                   String strHeader,String strFooter,String strTableNum,
                   String strReceiptNumber,Enum.ReceiptNoteAlignment HeaderAlignment,
                   Enum.ReceiptNoteAlignment FooterAlignment,//int subId,
                   double latitude,double longitude,Server s,boolean hasPaid
                   ,int version)
    {
        //instantiate
        myCart = mc;
        paymentType = Enum.PaymentType.cash;
        discountType = Enum.DiscountType.none;
        //CashValueForGratuity=0f;
        CashValueForDiscount=0f;
        flTaxRate = percentage;
        companyProfile = cp;
        HeaderNote = strHeader;
        FooterNote = strFooter;
        tableNumber = strTableNum;
        creditCard = new CreditCard();
        server = s;
        receiptNumber = strReceiptNumber;
        PaidAmount = 0f;
        transactionId ="";
        receiptDateTime =Calendar.getInstance();//c.setTime(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss"));//.format(new Date()));
        HeaderNoteAlignment = HeaderAlignment;
        FooterNoteAlignment = FooterAlignment;
        dbLatitude=latitude;
        dbLongitude=longitude;
        flTotal =mc.getAmount().floatValue();
        blnHasPaid = hasPaid;
        Version = version;
    }
    public Receipt(Parcel in)
    {

        myCart = in.readParcelable(MyCart.class.getClassLoader());
        server = in.readParcelable(Server.class.getClassLoader());
        HeaderNote = in.readString();
        FooterNote =in.readString();
        HeaderNoteAlignment = Enum.ReceiptNoteAlignment.values()[in.readInt()];
        FooterNoteAlignment = Enum.ReceiptNoteAlignment.values()[in.readInt()];
        companyProfile = in.readParcelable(CompanyProfile.class.getClassLoader());
        flTaxRate = in.readFloat();
        //CashValueForGratuity = in.readFloat();
        CashValueForDiscount = in.readFloat();
        PaidAmount = in.readFloat();
        paymentType = Enum.PaymentType.values()[in.readInt()];
        discountType = Enum.DiscountType.values()[in.readInt()];
        creditCard = in.readParcelable(CreditCard.class.getClassLoader());
        receiptNumber = in.readString();
        tableNumber = in.readString();
        long lnTime = in.readLong();
        //if(lnTime>0)
        receiptDateTime =new GregorianCalendar();//Calendar.getInstance();
        receiptDateTime.setTimeInMillis(lnTime);
        transactionId = in.readString();
        dbLongitude = in.readDouble();
        dbLatitude = in.readDouble();
        flTotal = in.readFloat();
        /*blnActive = in.readByte()!=0;
        blnHasPaid = in.readByte()!=0;*/
        blnActive = (in.readInt()==0?false:true);
        blnHasPaid = (in.readInt()==0?false:true);
        Version = in.readInt();
        lngLastUpdateDate = in.readLong();
        strLinkedReceipts = in.readString();
    }
    public Enum.DiscountType GetDiscountType(){return discountType;}

    public void SetDiscountValue(Enum.DiscountType dt,float value)
    {
        discountType = dt;
        if(dt== Enum.DiscountType.cash)
        {
            PercentageValueForDiscount=0f;
            CashValueForDiscount=value;
        }
        else
        {
            PercentageValueForDiscount=value/100f;
            RecalculatePercentageWiseCashValueForDiscount();


        }

        UpdateReceipt();
    }
    private void RecalculatePercentageWiseCashValueForDiscount()
    {
        CashValueForDiscount = myCart.GetAmountAfterPromotionByCashDiscount().multiply(new BigDecimal(PercentageValueForDiscount)).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
        CashValueForDiscount = (new BigDecimal(CashValueForDiscount)).setScale(2, RoundingMode.HALF_UP).floatValue();
    }
    public float GetCashValueForDiscount(){
        if(discountType== Enum.DiscountType.percentage) {
            RecalculatePercentageWiseCashValueForDiscount();
        }
        return CashValueForDiscount;
    }
    public float GetPercentageValueForDiscount(){return PercentageValueForDiscount*100;}
    public float GetCashValueForGratuity(){return GetAmountAfterAmountCashPromotionDiscountPlusAdditionalDiscount().multiply(new BigDecimal(PercentageValueForGratuity)).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();}
    public float GetGratuityPercentage(){return PercentageValueForGratuity*100f;}
    public void SetGratuityPercentage(float flPercent)
    {
        PercentageValueForGratuity = flPercent/100f;
        UpdateReceipt();
    }
    public BigDecimal GetAmountAfterAmountCashPromotionDiscountPlusAdditionalDiscount()
    {
        UpdateReceipt();
        float tempPrice = myCart.GetAmountAfterPromotionByCashDiscount().floatValue();
        if(discountType== Enum.DiscountType.percentage)
        {RecalculatePercentageWiseCashValueForDiscount();}
        tempPrice -=CashValueForDiscount;
        return new BigDecimal(tempPrice).setScale(2, RoundingMode.HALF_UP);
    }
    public BigDecimal GetTaxAmountAfterAmountCashPromotionDiscount()
    {
        float tax  = GetAmountAfterAmountCashPromotionDiscountPlusAdditionalDiscount().floatValue()*flTaxRate;
        return new BigDecimal(tax).setScale(2,BigDecimal.ROUND_HALF_UP);
    }
    public String ConvertCartItemPromotionToSQLData() {
        if(myCart==null)return "";
        if(myCart.GetDisplayCartItemList().size()==0)return "";
        ArrayList<CartDisplayItem>items = myCart.GetDisplayCartItemList();
        StringBuilder sb = new StringBuilder();
        /**FORMATTED
        store item [si;unit;item id<space> version;modifier 1 <space> version,modifier 2 <space> version]
         Promotion awarded [pa;unit;promotion object id;sub version;receipt index<space>cart GUID,receipt index<space>cart GUID;item id <space> receipt id <space> unit count,item id <space> receipt id <space> unit count]
         **/
        for(int i=0;i<items.size();i++)
        {
            if(items.get(i).cit== Enum.CartItemType.PromotionAwarded)
            {
                sb.append(items.get(i).pa.ToSQLString());

            }
            else
            {
                sb.append(items.get(i).si.ToSQLString());

            }
        }
        return sb.toString();
    }
    public String ConvertCartItemsToSQLData()
    {
        if(myCart==null)return "";
        if(myCart.GetItems().size()==0)return "";
        StringBuilder sb = new StringBuilder();
        //[unit;item id;modifier 1,modifier 1][unit;item id;modifier 1,modifier 2]
        ArrayList<StoreItem>items = myCart.GetItems();
        for(int i=0;i<items.size();i++)
        {
            StoreItem si =items.get(i);
            sb.append("["+si.UnitOrder+";"+si.item.getID());
            if(si.modifiers.size()>0)sb.append(";");
            for(int j=0;j<si.modifiers.size();j++)
            {

                sb.append(si.modifiers.get(j).getID());
                if(j+1<si.modifiers.size())sb.append(",");
            }
            sb.append("]"); //[unit;item id;modifier id,modifier id]
        }


        return sb.toString();
    }

    public void UpdateReceipt()
    {
        /**order does matter**/
        float flOriginalAmount = myCart.getAmount().floatValue();
        //calculate discount
        if(discountType== Enum.DiscountType.cash)
        {

        }
        else if(discountType== Enum.DiscountType.none)
        {
            CashValueForDiscount=0;
            PercentageValueForDiscount=0;
        }
        else
        {
            CashValueForDiscount = PercentageValueForDiscount * myCart.getAmount().floatValue();
            CashValueForDiscount = (new BigDecimal(CashValueForDiscount)).setScale(2, RoundingMode.HALF_UP).floatValue();


        }

    }
    public BigDecimal ReturnReceiptTotalAmountWithoutGratuityAndAmountPromotionDiscount()
    {
        UpdateReceipt();
        return myCart.getAmount().add(myCart.getTaxAmount());

    }
    public BigDecimal ReturnReceiptFinalTotalAmount()
    {
        UpdateReceipt();
        BigDecimal gratuity = new BigDecimal(GetCashValueForGratuity()).setScale(2,RoundingMode.HALF_UP);

        return GetAmountAfterAmountCashPromotionDiscountPlusAdditionalDiscount().add(gratuity).add(GetTaxAmountAfterAmountCashPromotionDiscount());


    }

    public String ReturnDateTimeString()
    {


        return new SimpleDateFormat("MM-dd-yyyy HH:mm:ss a").format(receiptDateTime.getTime());
    }

    private String ReturnDashLine()
    {
        int dashChar=42;
        String strDashLine="";
        while(dashChar>0) {
            strDashLine += "-";
            dashChar--;
        }
        return strDashLine+System.getProperty(newLine);
    }
    public String ReturnHTMLDashLine()
    {
        int length=72;
        String strLine="";
        while(length>0){strLine+="-";length--;}

        return strLine;
    }
    public String ReturnHTMLContentForEmailReceipt()
    {
        String strHtmlSpace="&nbsp;";
        int count=5;
        String strModifierNameSpaces ="";
        while(count>0)
        {strModifierNameSpaces+= strHtmlSpace;count--;}

        //create table structure
        StringBuilder sb = new StringBuilder("<Table>");
        //company name
        sb.append("<tr align=\"center\"><td>"+companyProfile.CompanyName+"</td></tr>");
        //company address
        sb.append("<tr align=\"center\"><td>"+companyProfile.Street+"</td></tr>");
        //city, state and zipcode
        sb.append("<tr align=\"center\"><td>"+companyProfile.City+((companyProfile.City.length()>0)?",":"")
                +strHtmlSpace+companyProfile.State+strHtmlSpace+companyProfile.Zipcode+"</td></tr>");
        /**create sub table for contacts, receipt number, servant name and date**/
        sb.append("<tr><td>");
        sb.append("<table width=\"100%\">");
        //contact info
        sb.append("<tr >");
        if(companyProfile.Phone.length()>0)
        sb.append("<td align=\"left\">Phone#"+strHtmlSpace+companyProfile.Phone+"</td>");
        if(companyProfile.Email.length()>0)
        sb.append("<td align=\"right\"> Email:"+strHtmlSpace+companyProfile.Email+"</td>");
        sb.append("</tr>");
        //receipt number
        if(tableNumber.length()==0) {
            sb.append("<tr align=\"left\"><td colspan=\"2\">Receipt#" + strHtmlSpace + receiptNumber + "</td></tr>");
        }
        else {
            sb.append("<tr ><td align=\"left\">Receipt#" + strHtmlSpace + receiptNumber + "</td>");
            sb.append("<td align=\"right\">" + tableNumber + "</td>");
            sb.append("</tr>");
        }
        //date and servant
        sb.append("<tr >");
        sb.append("<td align=\"left\">"+ReturnDateTimeString()+"</td>");
        if(server!=null && server.Name.length()>0)
        sb.append("<td align=\"right\"> Server:"+strHtmlSpace+ server.Name+"</td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</td></tr>");
        //header note
        sb.append("<tr align=\"center\"><td>"+HeaderNote+"</td></tr>");
        //dash line
        //sb.append("<tr align=\"left\"><td>"+ReturnHTMLDashLine()+"</td></tr>");
        sb.append("<tr ><td></td></tr>");
        sb.append("<tr ><td></td></tr>");
        /**create sub table for cart items and its modifiers**/
        sb.append("<tr align=\"center\"><td>");
        sb.append("<table width=\"100%\" border=\"0\">");
        ArrayList<CartDisplayItem> cartItems = myCart.GetDisplayCartItemList();
        for(CartDisplayItem cdi:cartItems)
        {
            sb.append("<tr>");
            if(cdi.cit== Enum.CartItemType.StoreItem)
            {



                    sb.append("<td align=\"center\" width=\"60px\">"+cdi.si.UnitOrder+"x"+strHtmlSpace+"</td>");
                    sb.append("<td align=\"left\">"+cdi.si.item.Name+"</td>");
                    sb.append("<td align=\"right\">"+strHtmlSpace+strHtmlSpace+common.Utility.ConvertBigDecimalToCurrencyFormat(cdi.si.item.getPrice())+"</td>");
                    sb.append("<td align=\"right\">"+strHtmlSpace+strHtmlSpace+common.Utility.ConvertBigDecimalToCurrencyFormat(cdi.si.item.getPrice().multiply(new BigDecimal(cdi.si.UnitOrder)))+"</td>");
                    sb.append("</tr>");

                    for(ModifierObject mo :cdi.si.modifiers)
                    {
                        sb.append("<tr>");
                        sb.append("<td></td>");
                        sb.append("<td align=\"left\">"+strModifierNameSpaces+mo.Name+"</td>");
                        sb.append("<td align=\"right\">"+strHtmlSpace+strHtmlSpace+common.Utility.ConvertBigDecimalToCurrencyFormat(mo.getPrice())+"</td>");
                        sb.append("<td align=\"right\">"+strHtmlSpace+strHtmlSpace+common.Utility.ConvertBigDecimalToCurrencyFormat(mo.getPrice().multiply(new BigDecimal(cdi.si.UnitOrder)))+"</td>");
                        sb.append("</tr>");
                    }

            }
            else
            {
                String strUnit="";
                String strBasePromotionPrice="";
                int intShareBy =cdi.pa.ShareByHowManyReceipts();
                if(intShareBy==1)
                {

                    strUnit = cdi.pa.unit+"x ";
                }
                else
                {

                    strUnit="("+cdi.pa.unit+"/"+intShareBy+")x ";
                }
                strUnit+=cdi.pa.promotionObject.GetTitle();

                if(cdi.pa.promotionObject.GetDiscountType()== Enum.DiscountType.cash) {
                    strBasePromotionPrice=common.Utility.ConvertBigDecimalToCurrencyFormat(cdi.pa.GetItemsTotalPriceForThisComboBeforeDiscount());
                }
                else
                {
                    strBasePromotionPrice=String.format("%.2f",cdi.pa.promotionObject.GetDiscountValue()*100f)+"%";
                }

                BigDecimal bdDiscount = cdi.pa.GetTotalDiscountAwarded((myCart.receiptIndex==cdi.pa.GetSharedReceiptIndex().get(0))?true:false,myCart.receiptIndex);
                sb.append("<td align=\"left\" colspan=\"2\">"+strUnit+"</td>");
                sb.append("<td align=\"right\">"+strHtmlSpace+strHtmlSpace+strBasePromotionPrice+"</td>");
                sb.append("<td align=\"right\">"+strHtmlSpace+strHtmlSpace+common.Utility.ConvertBigDecimalToCurrencyFormat(bdDiscount)+"</td>");
                sb.append("</tr>");
            }
        }



        sb.append("</table>");
        sb.append("</td></tr>");
        //dash line
        //sb.append("<tr align=\"left\"><td>"+ReturnHTMLDashLine()+"</td></tr>");

        /**create sub table for summary label**/
        sb.append("<tr><td></td></tr>");
        sb.append("<tr><td></td></tr>");
        sb.append("<tr><td>");
        sb.append("<table width=\"100%\">");
        //amount
        sb.append("<tr>");
        sb.append("<td align=\"left\" width=\"90px\">Amount</td>");
        sb.append("<td align=\"right\" >"+common.Utility.ConvertBigDecimalToCurrencyFormat(myCart.getAmount())+"</td>");
        sb.append("</tr>");
        //promotion object for total cash discount
        String strPromotion=myCart.promotionObject.GetTitle();
        String strPromotionDiscount = common.Utility.ConvertBigDecimalToCurrencyFormat((myCart.GetPromotionByCashAmount()));
        sb.append("<tr>");
        sb.append("<td align=\"left\" width=\"90px\">"+strPromotion+"</td>");
        sb.append("<td align=\"right\" >"+strPromotionDiscount+"</td>");
        sb.append("</tr>");
        //additional discount
        sb.append("<tr>");
        sb.append("<td align=\"left\" width=\"90px\">Discount</td>");
        sb.append("<td align=\"right\" >-"+common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(GetCashValueForDiscount()))+"</td>");
        sb.append("</tr>");
        //gratuity
        sb.append("<tr>");
        sb.append("<td align=\"left\" width=\"90px\">Gratuity</td>");
        sb.append("<td align=\"right\" >"+common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(GetCashValueForGratuity()))+"</td>");
        sb.append("</tr>");
        //tax
        sb.append("<tr>");
        sb.append("<td align=\"left\" width=\"90px\">Tax</td>");
        sb.append("<td align=\"right\" >"+common.Utility.ConvertBigDecimalToCurrencyFormat(GetTaxAmountAfterAmountCashPromotionDiscount())+"</td>");
        sb.append("</tr>");
        //dashline
        //sb.append("<tr><td colspan=\"2\">"+ReturnHTMLDashLine()+"</td></tr>");
        sb.append("<tr><td></td></tr>");
        sb.append("<tr><td></td></tr>");
        //total
        sb.append("<tr>");
        sb.append("<td align=\"left\" width=\"90px\">Total</td>");
        sb.append("<td align=\"right\" >"+common.Utility.ConvertBigDecimalToCurrencyFormat(ReturnReceiptFinalTotalAmount())+"</td>");
        sb.append("</tr>");
        //Paid
        String strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(PaidAmount));
        sb.append("<tr>");
        sb.append("<td align=\"left\" width=\"90px\">Cash</td>");
        sb.append("<td align=\"right\" >"+strPrice+"</td>");
        sb.append("</tr>");
        //balance
        BigDecimal bdTemp = new BigDecimal( Float.parseFloat(strPrice.replaceAll("[$,-]",""))-ReturnReceiptFinalTotalAmount().floatValue()).setScale(2, RoundingMode.HALF_UP);
        strPrice=common.Utility.ConvertBigDecimalToCurrencyFormat(bdTemp);
        sb.append("<tr>");
        sb.append("<td align=\"left\" width=\"90px\">Changed</td>");
        sb.append("<td align=\"right\" >"+strPrice+"</td>");
        sb.append("</tr>");

        sb.append("</table>");
        sb.append("</td></tr>");

        //act like new line before printing footer note
        sb.append("<tr><td></td></tr>");
        sb.append("<tr><td></td></tr>");
        sb.append("<tr><td></td></tr>");

        //footer note
        sb.append("<tr align=\"center\"><td>"+FooterNote+"</td></tr>");


        sb.append("</table>");

        return sb.toString();
    }
    public String ReturnContentString()
    {
        int maxChar=39;

        StringBuilder sb = new StringBuilder();
        //company name
        sb.append(ConstructLine(companyProfile.CompanyName,maxChar));
        //address
        sb.append(ConstructLine(companyProfile.Street,maxChar));
        String strLine =( (companyProfile.City.length()>0)?" "+companyProfile.City+", ":"")
                +( (companyProfile.City.length()>0)?companyProfile.State+" ":"")
                +( (companyProfile.Zipcode.length()>0)?companyProfile.Zipcode:"");
        sb.append(ConstructLine(strLine,maxChar));
        //contact
        sb.append(ConstructLine(companyProfile.Phone + "    " +companyProfile.Email, maxChar));
        //receipt #
        sb.append(ConstructLine("Receipt#: "+receiptNumber , maxChar));
        //servant
        if(server !=null && server.Name.length()>0)
            sb.append(ConstructLine(" Server: " + server.Name, maxChar));

        //header note
        sb.append(ConstructLine(HeaderNote, maxChar));

        //dash
        sb.append(ReturnDashLine());

        //items
        ArrayList<StoreItem> items = myCart.GetItems();
        for(int i=0;i<items.size();i++)
        {
            sb.append(ConstructItemLine(items.get(i),maxChar));
        }
        //dash
        sb.append(ReturnDashLine());

        //receipt summary
        sb.append(ConstructReceiptSummary(maxChar));

        //dash
        sb.append(ReturnDashLine());

        sb.append(ConstructReceiptTotalSection(maxChar));

        //dash
        sb.append(ReturnDashLine());

        //footer note
        sb.append(ConstructLine(common.myAppSettings.GetReceiptFooterText(), maxChar));

        return sb.toString();

    }
    private String ConstructReceiptTotalSection(int maxCharacterPerLine)
    {
        String strLine="";
        int RegularSummaryLineOffset=2;
        String strTotalLine ="Total";
        String strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(ReturnReceiptFinalTotalAmount());
        String strTotal=strPrice;
        while(strTotalLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strTotalLine+=" ";
        strTotalLine+=strPrice;
        strLine=strTotalLine+System.getProperty(newLine);

        if(printReceiptType== Enum.PrintReceiptType.change_balance)
        {
            /**Paid amount line**/
            String strPaidLine ="Paid";
            strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(PaidAmount));
            while(strPaidLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strPaidLine+=" ";
            strPaidLine+=strPrice;
            strLine+=strPaidLine+System.getProperty(newLine);

            /** changed line**/
            String strChanged ="Changed";
            strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(PaidAmount).subtract(ReturnReceiptFinalTotalAmount()));
            while(strChanged.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strChanged+=" ";
            strChanged+=strPrice;
            strLine+=strChanged+System.getProperty(newLine);
        }
        else if(printReceiptType== Enum.PrintReceiptType.credit_card_balance)
        {
            /**credit card info**/
            if(creditCard!=null) {
                String strCard = "Cart type: " + creditCard.CardType.name().toUpperCase();
                while (strCard.length() + strTotal.length() < maxCharacterPerLine + 1 + RegularSummaryLineOffset)
                    strCard += " ";//reusing previously saved total value
                strCard += strTotal;
                strLine+=strCard+System.getProperty(newLine);

                /** card # line**/
                String strCardNumber = "Card #: ************" + creditCard.Number.substring(12);
                while (strCardNumber.length() < maxCharacterPerLine + 1 + RegularSummaryLineOffset)
                    strCardNumber += " ";
                strLine+=strCardNumber+System.getProperty(newLine);
            }
        }

        return strLine;
    }
    private String ConstructReceiptSummary(int maxCharacterPerLine)
    {
        int RegularSummaryLineOffset=2;
        String strLine="";
        /**Amount**/
        String strSubTotalLine ="Amount";
        String strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(myCart.getAmount());
        while(strSubTotalLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strSubTotalLine+=" ";
        strSubTotalLine+=strPrice;
        strLine=strSubTotalLine+System.getProperty(newLine);


        /**discount**/
        if(GetCashValueForDiscount()>0.009)
        {
            String strDiscountLine="Discount";
            strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(GetCashValueForDiscount()));
            while(strDiscountLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strDiscountLine+=" ";
            strDiscountLine+=strPrice;
            strLine+=strDiscountLine+System.getProperty(newLine);

        }

        /**Gratuity**/
        if(GetCashValueForGratuity()>0.009)
        {
            String strGratuityLine="Gratuity";
            strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(GetCashValueForGratuity()));
            while(strGratuityLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strGratuityLine+=" ";
            strGratuityLine+=strPrice;
            strLine+=strGratuityLine+System.getProperty(newLine);

        }

        /**Tax**/
        String strTaxLine="Tax";
        strPrice = common.Utility.ConvertBigDecimalToCurrencyFormat(GetTaxAmountAfterAmountCashPromotionDiscount());
        while(strTaxLine.length()+strPrice.length()<maxCharacterPerLine+1+RegularSummaryLineOffset)strTaxLine+=" ";
        strTaxLine+=strPrice;
        strLine+=strTaxLine+System.getProperty(newLine);


        return strLine;
    }
    private String ConstructItemLine(StoreItem si, int maxChar)
    {
        int intUnitCountWidth=3;
        int intPriceWidth=14;
        int intNameWidth=maxChar-intUnitCountWidth-intPriceWidth-2;//minus two for white spaces
        int intModifierUnitPlaceHolder=6;
        int intModifierNameWidth=maxChar-intModifierUnitPlaceHolder-intPriceWidth-2;//minus two for white spaces in between price section and unit section
        String strItemLine="";
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

        if(strItemName.length()>intNameWidth)
        {
            while(strItemName.length()>0) {
                intEndIndex = common.Utility.GetEndIndex(strItemName, intNameWidth);
                String strLine = strItemName.substring(0, intEndIndex);
                while(strLine.length()<intNameWidth+1)strLine+=" ";

                if (blnFirstLine) {
                    //1st line with unit and price info
                    blnFirstLine = false;
                    strItemLine=strUnitCount+" "+strLine+" "+strPrice+System.getProperty(newLine);

                    //reset the unit and price string for next line use
                    strUnitCount="";
                    while(strUnitCount.length()<intUnitCountWidth+1)strUnitCount+=" ";
                    strPrice="";
                    while(strPrice.length()<intPriceWidth+1)strPrice+=" ";

                } else {
                    //remaining of item name
                    strItemLine+=strUnitCount+" "+strLine+" "+strPrice+System.getProperty(newLine);
                }


                if (intEndIndex != strItemName.length())
                    strItemName += strItemName.substring(intEndIndex).trim();
                else break;
            }
        }
        else
        {
            while(strItemName.length()<intNameWidth+1)strItemName+=" ";

            strItemLine+=strUnitCount+" "+strItemName+" "+strPrice+System.getProperty(newLine);
        }

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
                        strItemLine+=strPlaceHolder+" "+strLine+" "+strPrice+System.getProperty(newLine);

                        //reset the unit and price string for next line use
                        strUnitCount="";
                        while(strUnitCount.length()<intUnitCountWidth+1)strUnitCount+=" ";
                        strPrice="";
                        while(strPrice.length()<intPriceWidth+1)strPrice+=" ";

                    }
                    else
                    {
                        while(strLine.length()<intModifierNameWidth+1)strLine+=" ";
                        strItemLine+=strUnitCount+" "+strLine+" "+strPrice+System.getProperty(newLine);
                    }

                    if (intEndIndex != strMo.length())
                        strMo = strMo.substring(intEndIndex).trim();
                    else break;
                }
            }
            else
            {
                while(strMo.length()<intModifierNameWidth+1)strMo+=" ";

                strItemLine+=strPlaceHolder+" "+strMo+" "+strPrice+System.getProperty(newLine);
            }

        }

        return strItemLine;
    }
    private String ConstructLine(String strLine,int maxChar)
    {
        String strContentLine="";
        if(strLine.length()==0)return strLine;


        int totalLength = strLine.length();
        if(totalLength<=maxChar)
        {
            strContentLine=strLine+System.getProperty(newLine);
        }
        else
        {

            int intEndIndex =-1;
            while(strLine.length()>0)
            {

                intEndIndex = common.Utility.GetEndIndex(strLine,maxChar);

                strContentLine+=(strLine.substring(0,intEndIndex));
                strContentLine+=System.getProperty(newLine);

                if(intEndIndex!=strLine.length())
                    strLine = strLine.substring(intEndIndex);
                else break;
            }
        }
        return strContentLine;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeParcelable(myCart,i);
        parcel.writeParcelable(server, i);
        parcel.writeString(HeaderNote);
        parcel.writeString(FooterNote);
        parcel.writeInt(HeaderNoteAlignment.ordinal());
        parcel.writeInt(FooterNoteAlignment.ordinal());
        parcel.writeParcelable(companyProfile, i);
        parcel.writeFloat(flTaxRate);
        //parcel.writeFloat(CashValueForGratuity);
        parcel.writeFloat(CashValueForDiscount);
        parcel.writeFloat(PaidAmount);
        parcel.writeInt(paymentType.ordinal());
        parcel.writeInt(discountType.ordinal());
        parcel.writeParcelable(creditCard, i);
        parcel.writeString(receiptNumber);
        parcel.writeString(tableNumber);
        //long lnTime = receiptDateTime.getTimeInMillis();
        parcel.writeLong(receiptDateTime.getTimeInMillis());
        parcel.writeString(transactionId);
        parcel.writeDouble(dbLongitude);
        parcel.writeDouble(dbLatitude);
        parcel.writeFloat(flTotal);
     /*   parcel.writeByte((byte)(blnActive?1:0));
        parcel.writeByte((byte)(blnHasPaid?1:0));*/
        parcel.writeInt((blnActive?1:0));
        parcel.writeInt((blnHasPaid?1:0));
        parcel.writeInt(Version);
        parcel.writeLong(lngLastUpdateDate);
        parcel.writeString(strLinkedReceipts);
    }
    public static final Creator<Receipt> CREATOR = new Creator<Receipt>()
    {
        @Override
        public Receipt createFromParcel(Parcel parcel) {
            return new Receipt(parcel);
        }

        @Override
        public Receipt[] newArray(int i) {
            return new Receipt[i];
        }
    };

    @Override
    public Object clone() {

        Receipt tempReceipt = new Receipt((MyCart) myCart.clone(),flTaxRate,companyProfile,HeaderNote
                ,FooterNote,tableNumber,receiptNumber,HeaderNoteAlignment,FooterNoteAlignment
        ,dbLatitude,dbLongitude,server,blnHasPaid,Version);
        tempReceipt.PaidAmount =PaidAmount;
        tempReceipt.myCart.promotionObject = myCart.promotionObject;
        tempReceipt.SetGratuityPercentage(GetGratuityPercentage());
        tempReceipt.CashValueForDiscount = CashValueForDiscount;
        tempReceipt.SetDiscountValue(discountType,0);
        tempReceipt.PercentageValueForDiscount=PercentageValueForDiscount;
        tempReceipt.CashValueForDiscount = CashValueForDiscount;
        tempReceipt.lngLastUpdateDate = lngLastUpdateDate;
        tempReceipt.Version = Version;
        tempReceipt.blnHasPaid = blnHasPaid;
        tempReceipt.strLinkedReceipts = strLinkedReceipts;
        return tempReceipt;
    }
}
