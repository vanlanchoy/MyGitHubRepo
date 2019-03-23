package tme.pos.BusinessLayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Modifier;

/**
 * Created by kchoy on 2/2/2015.
 */
public class ModifierObject extends ItemObject implements Parcelable{
    int MutualGroup=-1;
    protected  int IsActive=-1;
    public ModifierObject(long intID,String strName,long intParentID,String strPrice,int intGroup,int ActiveFlag,int version)
    {
        super(intID,strName,intParentID, strPrice,"",false,0,version);
        MutualGroup = intGroup;
        IsActive = ActiveFlag;
    }
    public ModifierObject(Parcel in)
    {
        super(in);
        this.MutualGroup = in.readInt();
        this.IsActive = in.readInt();

    }
    public int getMutualGroup(){return MutualGroup;}
    public int getIsActive(){return IsActive;}

    public void setActiveFlag(int newFlag)
    {
        IsActive = newFlag;
    }

    public static final Creator<ModifierObject> CREATOR = new Creator<ModifierObject>()
    {
        @Override
        public ModifierObject createFromParcel(Parcel parcel) {
            return new ModifierObject(parcel);
        }

        @Override
        public ModifierObject[] newArray(int i) {
            return new ModifierObject[i];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel,i);
        parcel.writeInt(MutualGroup);
        parcel.writeInt(IsActive);
    }
}
