package tme.pos.BusinessLayer;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vanlanchoy on 3/28/2015.
 */
public class CompanyProfile implements Parcelable
{
    public String CompanyName;
    public String Phone;
    public String Email;
    public String Street;
    public String City;
    public String Zipcode;
    public String State;
    public byte[] Logo;

    public CompanyProfile(){}
    public CompanyProfile(Parcel in)
    {
        CompanyName = in.readString();
        Phone = in.readString();
        Email = in.readString();
        Street = in.readString();
        City = in.readString();
        Zipcode = in.readString();
        State = in.readString();
        byte b = in.readByte();
        if(b==(byte)1)
        in.readByteArray(Logo);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(CompanyName);
        parcel.writeString(Phone);
        parcel.writeString(Email);
        parcel.writeString(Street);
        parcel.writeString(City);
        parcel.writeString(Zipcode);
        parcel.writeString(State);
        if(Logo==null)
        {
            parcel.writeByte((byte)0);
        }
        else
        {
            parcel.writeByte((byte)1);
            parcel.writeByteArray(Logo);
        }

    }

    public static final Creator<CompanyProfile> CREATOR = new Creator<CompanyProfile>()
    {
        @Override
        public CompanyProfile createFromParcel(Parcel parcel) {
            return new CompanyProfile(parcel);
        }

        @Override
        public CompanyProfile[] newArray(int i) {
            return new CompanyProfile[i];
        }
    };

}
