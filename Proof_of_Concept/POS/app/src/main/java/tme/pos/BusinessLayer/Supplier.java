package tme.pos.BusinessLayer;

import java.util.Date;

/**
 * Created by kchoy on 6/10/2015.
 */
public class Supplier implements Comparable<Supplier>{
    public String Name;
    public long SupplierId;
    public boolean IsActive;
    public Date InactiveDate;
    public String PhoneNumber;
    public String Address;
    public String Email;
    public String Note;

    @Override
    public int compareTo(final Supplier o) {
        return this.Name.compareTo(o.Name);
    }
}
