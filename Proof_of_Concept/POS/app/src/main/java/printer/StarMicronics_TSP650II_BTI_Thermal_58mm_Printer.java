package printer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * Created by vanlanchoy on 9/5/2016.
 */
public class StarMicronics_TSP650II_BTI_Thermal_58mm_Printer extends Generic_58mm_Printer {
    public StarMicronics_TSP650II_BTI_Thermal_58mm_Printer(BluetoothPrinterListener listener, BluetoothDevice device, Context c)
    {
        super(listener,device,c);
    }
}
