package tme.pos.BusinessLayer;

import android.graphics.Typeface;

/**
 * Created by vanlanchoy on 10/8/2014.
 */
public class RegularOrderItemProperties {
    public StoreItem si;
    public Typeface tf;
    public int intMaxChar;
    public float flTextSize;
    public int intLeftRightPadding;
    public int intTopDownPadding;
    public int intTblRowPadding;
    public RegularOrderItemProperties(StoreItem si, Typeface tf, int intMaxChar, float flTextSize,
                                      int intLeftRightPadding, int intTopDownPadding, int intTblRowPadding)
    {
        this.si = si;
        this.flTextSize = flTextSize;
        this.intLeftRightPadding = intLeftRightPadding;
        this.intMaxChar = intMaxChar;
        this.intTblRowPadding = intTblRowPadding;
        this.intTopDownPadding = intTopDownPadding;
        this.tf = tf;
    }
}
