package tme.pos.BusinessLayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by vanlanchoy on 11/19/2016.
 */

public class JsonClass {
    public String ConvertReceiptToJsonString(Receipt receipt)
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();
        Gson gson = gsonBuilder.setPrettyPrinting().create();
        return gson.toJson(receipt,Receipt.class);

    }
}
