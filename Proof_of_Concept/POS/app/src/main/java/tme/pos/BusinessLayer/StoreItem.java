package tme.pos.BusinessLayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by kchoy on 2/12/2015.
 */
public class StoreItem implements Cloneable,Parcelable{
    public ArrayList<ModifierObject> modifiers;
    public ItemObject item;
    public int UnitOrder=0;
    //public int initialUnitOrder=0;
    public float Discount=0.0f;
    public long orderTime=0;
    public long promotionId = -1;
    //public long boundId=-1;
    public StoreItem(ItemObject io)
    {

        Instantiate(io.getID(),io.getName(),io.getParentID(),io.getPrice().toPlainString()
                ,io.PicturePath,io.getDoNotTrackFlag(),io.getBarcode(),io.version);
    }

    public StoreItem(long ID, String strName, long parentID, String strPrice, String strPicturePath
            , boolean blnDoNotTrack, long barcode,int version)
    {
        Instantiate(ID,strName,parentID,strPrice,strPicturePath,blnDoNotTrack,barcode,version);

    }
    private void Instantiate(long ID, String strName, long parentID, String strPrice, String strPicturePath
            , boolean blnDoNotTrack, long barcode,int version)
    {
        modifiers = new ArrayList<ModifierObject>();
        item = new ItemObject(ID,strName,parentID,strPrice,strPicturePath,blnDoNotTrack,barcode,version);
        orderTime = Calendar.getInstance().getTimeInMillis()
;    }
    public StoreItem(Parcel in)
    {
        Parcelable[] parcelables = in.readParcelableArray(ModifierObject.class.getClassLoader());
        this.modifiers = new ArrayList<ModifierObject>();
        if(parcelables!=null)
        {
          for(int i=0;i<parcelables.length;i++)
              modifiers.add((ModifierObject)parcelables[i]);
        }


        this.item = in.readParcelable(ItemObject.class.getClassLoader());
        this.UnitOrder = in.readInt();
        this.Discount = in.readFloat();
        this.orderTime = in.readLong();
        this.promotionId = in.readLong();
        //this.boundId = in.readLong();
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        ModifierObject[] tempArry = new ModifierObject[modifiers.size()];
        modifiers.toArray(tempArry);
        parcel.writeParcelableArray(tempArry,i);
        parcel.writeParcelable(item,i);
        parcel.writeInt(UnitOrder);
        parcel.writeFloat(Discount);
        parcel.writeLong(orderTime);
        parcel.writeLong(promotionId);
        //parcel.writeLong(boundId);
    }

    @Override
    public Object clone(){
        StoreItem si = new StoreItem(item);
        for(int i = 0;i<modifiers.size();i++)
        {
            si.modifiers.add(modifiers.get(i));
        }
        si.UnitOrder = UnitOrder;
        si.Discount = Discount;
        si.orderTime = orderTime;
        //si.promotionId = promotionId;
        //si.boundId = boundId;
        return si;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StoreItem> CREATOR = new Creator<StoreItem>()
    {
        @Override
        public StoreItem createFromParcel(Parcel parcel) {
            return new StoreItem(parcel);
        }

        @Override
        public StoreItem[] newArray(int i) {
            return new StoreItem[i];
        }
    };
    public boolean IsSameOrderedItemExcludeUnitCount(StoreItem si)
    {
        boolean blnFound = false;

        if(si.item.getID()==this.item.getID())
        {
            if(si.modifiers.size()==0 && modifiers.size()==0)
            {
                return true;
            }
            else
            {
                for(int i=0;i<modifiers.size();i++)
                {
                    for(int j=0;j<si.modifiers.size();j++)
                    {
                        blnFound=false;
                        if(modifiers.get(i).getID()==si.modifiers.get(j).getID())
                        {
                            blnFound=true;
                            break;
                        }

                    }
                    if(!blnFound) return false;
                }
            }
        }

        return blnFound;
    }
    public static String ReturnVersionNumber(String strItemString)
    {
        String[] itemDetails = strItemString.split(";");
        String[] item_and_version = itemDetails[2].split(" ");
        return item_and_version[1];
    }
    public static String ReturnItemId(String strItemString)
    {
        String[] itemDetails = strItemString.split(";");
        String[] item_and_version = itemDetails[2].split(" ");
        return item_and_version[0];

    }
    public static String ReturnUnitCount(String strItemString)
    {
        String[] itemDetails = strItemString.split(";");
        return itemDetails[1];
    }
    public String ToSQLString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[si;"+UnitOrder+";"+item.getID()+ " "+item.version);
        if(modifiers.size()>0)sb.append(";");
        for(int j=0;j<modifiers.size();j++)
        {

            sb.append(modifiers.get(j).getID()+ " "+modifiers.get(j).version);
            if(j+1<modifiers.size())sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
