package tme.pos.BusinessLayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by vanlanchoy on 3/28/2015.
 */
public class Server implements Comparable<Server>, Parcelable {
    public String Name="";
    public long EmployeeId=-1;
    public Enum.ServerGender gender= Enum.ServerGender.male;
    public boolean IsActive=false;
    public Date InactiveDate=null;
    public String PhoneNumber="";
    public String Address="";
    public String PicturePath="";
    public String Email="";
    public String Note="";

    public Server(){}
    public Server(Parcel in)
    {
        Name =in.readString();
        EmployeeId = in.readLong();
        gender = Enum.ServerGender.values()[in.readInt()];
        IsActive = in.readByte()!=0;
        if(in.readByte()!=0)InactiveDate = new Date(in.readLong());//write back into the variable if there is value
        PhoneNumber = in.readString();
        Address=in.readString();
        PicturePath = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Name);
        parcel.writeLong(EmployeeId);
        parcel.writeInt(gender.ordinal());
        parcel.writeByte((byte) (IsActive ? 1 : 0));
        parcel.writeByte((byte) ((InactiveDate != null) ? 1 : 0));//flag whether Inactive date is null
        if(InactiveDate!=null)parcel.writeLong(InactiveDate.getTime());
        parcel.writeString(PhoneNumber);
        parcel.writeString(Address);
        parcel.writeString(PicturePath);
    }

    @Override
    public int compareTo(final Server o) {
        return this.Name.compareTo(o.Name);
    }

    public static final Creator<Server> CREATOR = new Creator<Server>()
    {
        @Override
        public Server createFromParcel(Parcel parcel) {
            return new Server(parcel);
        }

        @Override
        public Server[] newArray(int i) {
            return new Server[i];
        }
    };
}
