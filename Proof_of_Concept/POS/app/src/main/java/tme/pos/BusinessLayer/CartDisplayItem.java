package tme.pos.BusinessLayer;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.wallet.Cart;

import javax.mail.Store;

/**
 * Created by vanlanchoy on 7/9/2016.
 */
public class CartDisplayItem implements Parcelable {
    public PromotionAwarded pa;
    public StoreItem si;
    public Enum.CartItemType cit;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CartDisplayItem createFromParcel(Parcel in) {
            return new CartDisplayItem(in);
        }

        public CartDisplayItem[] newArray(int size) {
            return new CartDisplayItem[size];
        }
    };
    public CartDisplayItem(PromotionAwarded pa, StoreItem si, Enum.CartItemType cit)
    {
        this.cit = cit;
        this.si = si;
        this.pa = pa;

    }
    public CartDisplayItem()
    {
        pa = null;
        si = null;
        cit = Enum.CartItemType.PromotionAwarded;
    }
    public CartDisplayItem(Parcel in)
    {
        this.pa = in.readParcelable(PromotionAwarded.class.getClassLoader());
        this.si = in.readParcelable(StoreItem.class.getClassLoader());
        this.cit = Enum.CartItemType.valueOf(in.readString());
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeParcelable(pa,i);
        dest.writeParcelable(si,i);
        dest.writeString(cit.name());
    }
}
