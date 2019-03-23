package tme.pos.BusinessLayer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vanlanchoy on 3/28/2015.
 */
public class CreditCard implements Parcelable {
    public String Number;
    public String ExpDate;
    public String CVV;
    public Enum.CreditCardType CardType;
    public String CardHolder;

    public CreditCard(){}
    public CreditCard(Parcel in)
    {
        Number = in.readString();
        ExpDate = in.readString();
        CVV = in.readString();
        CardType = Enum.CreditCardType.values()[in.readInt()];
        CardHolder = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Number);
        parcel.writeString(ExpDate);
        parcel.writeString(CVV);
        parcel.writeInt(CardType.ordinal());
        parcel.writeString(CardHolder);
    }
    public static final Creator<CreditCard> CREATOR = new Creator<CreditCard>()
    {
        @Override
        public CreditCard createFromParcel(Parcel parcel) {
            return new CreditCard(parcel);
        }

        @Override
        public CreditCard[] newArray(int i) {
            return new CreditCard[i];
        }
    };

}

