package tme.pos.BusinessLayer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kchoy on 10/15/2014.
 */
public class CategoryObject implements Parcelable{
    protected long Id;
    protected String Name;

    public CategoryObject(long intID,String strName)
    {
            Id = intID;
            Name=strName;
    }
    public  CategoryObject(Parcel in)
    {

        this.Name = in.readString();
        this.Id = in.readLong();
    }
    public long getID(){return Id;}
    public String getName(){return Name;}
    public void setName(String strNewName){this.Name=strNewName;}
    public void setId(long newId){this.Id=newId;}
    static final Creator CREATOR = new Creator()
    {
        @Override
        public CategoryObject createFromParcel(Parcel parcel) {
            return new CategoryObject(parcel);
        }

        @Override
        public CategoryObject[] newArray(int i) {
            return new CategoryObject[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Name);
        parcel.writeLong(Id);
    }
}
