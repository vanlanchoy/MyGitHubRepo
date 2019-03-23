package printer;

/**
 * Created by vanlanchoy on 3/1/2015.
 */
public class Command {
    public static byte[] AutoCut()
    {
        return new byte[]{0x1B, 0x64, 0x02};
    }
    public static byte[] NullChar()
    {
        return new byte[]{0x00};
    }
    public static byte[] OpenCashDraw(){return new byte[] { 0x07 };}
}
