package tme.pos.BusinessLayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

/**
 * Created by kchoy on 12/19/2014.
 */
public class ItemObject extends CategoryObject implements Parcelable
{
    protected long ParentID;
    protected BigDecimal Price;



    protected boolean blnDoNotTrack;
    protected String PicturePath;
    protected long lngBarcode;
    protected  int version;
    public ItemObject(long intID,String strName,long intParentID,String strPrice
            ,String strPicturePath,boolean blnDoNotTrack,long barcode,int version)
    {
        super(intID,strName);
        ParentID = intParentID;
        Price = new BigDecimal(common.Utility.ConvertCurrencyFormatToBigDecimalString(strPrice));//.replace("$",""));
        PicturePath = strPicturePath;
        this.blnDoNotTrack = blnDoNotTrack;
        lngBarcode = barcode;
        this.version = version;
    }
    public ItemObject(Parcel in)
    {
        super(in);
        //String name = in.readString();
        this.ParentID = in.readLong();
        this.Price = new BigDecimal(in.readString());
        this.blnDoNotTrack = (in.readInt()==0)?false:true;
        this.lngBarcode = in.readLong();
        this.version = in.readInt();
        /*int flag = in.readInt();
        String temp = in.readString();
        //if(in.readByte()!=0) {
        if(flag==1){

            this.Price = new BigDecimal("0.0");
        }*/
    }
    public void setBarcode(long barcode)
    {
        lngBarcode = barcode;
    }
    public  long getBarcode(){return lngBarcode;}
    public void setPicturePath(String strPicturePath){PicturePath = strPicturePath;}
    public String getPicturePath()
    {
        if(PicturePath==null)PicturePath="";
        return PicturePath;
    }
    public void setParentID(long newID){ParentID = newID;}
    public long getParentID()
    {
        return ParentID;
    }
    public BigDecimal getPrice(){return Price;}
    public boolean getDoNotTrackFlag(){return blnDoNotTrack;}
    public void setDoNotTrackFlag(boolean flag){blnDoNotTrack = flag;}
    public void setPrice(BigDecimal newPrice){this.Price = newPrice;}
    public int GetCurrentVersionNumber()
    {
        return version;
    }
    public void SetVersion(int version)
    {
        this.version = version;
    }
    public static final Creator<ItemObject> CREATOR = new Creator<ItemObject>()
    {
        @Override
        public ItemObject createFromParcel(Parcel parcel) {
            return new ItemObject(parcel);
        }

        @Override
        public ItemObject[] newArray(int i) {
            return new ItemObject[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        //parcel.writeString(getName());
        parcel.writeLong(ParentID);
        parcel.writeString(Price.toString());
        parcel.writeInt((blnDoNotTrack)?0:1);
        parcel.writeLong(lngBarcode);
        parcel.writeInt(version);
        //parcel.writeByte((byte) (Price == null ? 0 : 1));
        /*if(Price!=null)
        {
            parcel.writeInt(1);
            String strTemp = Price.toPlainString();
            parcel.writeString(strTemp);
        }
        else
        {
            parcel.writeInt(0);
            parcel.writeString("0");
        }*/
        //if(Price!=null)parcel.writeString(Price.toPlainString());
        //if(Price!=null)parcel.writeString(Price.toString());

    }

}
