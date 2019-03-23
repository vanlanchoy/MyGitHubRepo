package tme.pos.BusinessLayer;

import android.content.Context;
import android.graphics.Typeface;

import tme.pos.MainUIActivity;
import tme.pos.R;

/**
 * Created by kchoy on 4/1/2015.
 */
public class TextLengthAndSize {
    Context context;
    public Typeface TYPE_FACE_ABEL_FONT;
    public int INVOICE_ITEM_NAME_MAX_LENGTH;
    public int SUPPLIER_NAME_MAX_LENGTH;
    public int SERVER_NAME_MAX_LENGTH;
    public int SUPPLIER_ADDRESS_MAX_LENGTH;
    public int EMAIL_ADDRESS_MAX_LENGTH;
    public float CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE;
    public float SERVER_FRAGMENT_TEXT_SIZE;
    public float INVENTORY_POPUP_WINDOW_TEXT_SIZE;
    public float PROMOTION_FRAGMENT_VIEW_MODE_TITLE_TEXT_SIZE;
    public float ADD_MENU_ITEM_MODIFIER_TEXT_SIZE;
    public float ADD_MENU_ITEM_TEXT_SIZE;
    public float RESOURCE_MANAGEMENT_SELECTION_TITLE_TEXT_SIZE;
    public float CHART_OPTIONS_TITLE_TEXT_SIZE;
    public float LABEL_TEXT_SIZE;
    //public int SUB_ITEM_NAME_MAX_LENGTH;
    public int MODIFIER_TABLE_RECEIPT_ROW_NAME_TOP_MARGIN;
    public int MODIFIER_TABLE_RECEIPT_ROW_PRICE_TOP_MARGIN;
    public int MODIFIER_TABLE_RECEIPT_ROW_PADDING;
    public int MAX_SERVER;
    public int MAX_SUPPLIER;
    public int MAX_BARCODE_LENGTH;
    public int RECEIPT_DISPLAY_TABLE_ROW_PADDING;
    public int PROMOTION_CATEGORY_ID=0;
    public int TAP_TO_ADD_CATEGORY_ID=-1;
    public static float SP_MENU_ITEM_TEXT_SIZE=30;
    //public float INVOICE_ITEM_NAME_WIDTH_WEIGHT;
    //public static float DP_RECEIPT_ITEM_MENU_TEXT_SIZE;
    public static float DP_PROMOTION_FIELD_LABEL_TEXT_SIZE;
    public static int LEFT_RIGHT_PADDING_RECEIPT_ROW;
    public static int TOP_DOWN_PADDING_RECEIPT_ROW;
    public static int TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW;
    //public static float INVOICE_ITEM_UNIT_PRICE_WIDTH_WEIGHT;
    //public static float INVOICE_SUB_ITEM_UNIT_WIDTH_WEIGHT;
    //public static float INVOICE_ITEM_TOTAL_PRICE_WIDTH_WEIGHT;
    //public static float INVOICE_SUB_ITEM_PRICE_WIDTH_WEIGHT;
    //public static float INVOICE_SUB_ITEM_NAME_WIDTH_WEIGHT;
    public static int LIST_VIEW_MODE_NUMBER_ITEM_TO_LOAD_PER_BATCH;
    public final static long GLOBAL_MODIFIER_PARENT_ID = -1;
    public final static long NEW_MODIFIER_PARENT_ID = -2;
    public final static float TOTAL_PRICE_LIMIT=1000000f;
    public final static int UNIT_LIMIT=999;
    public static float TAP_TO_ADD_BUTTON_WIDTH;
    public int ITEM_PER_PAGE_TO_DISPLAY_LIST_MODE;
    public int ITEM_PER_PAGE_TO_DISPLAY_PIC_MODE;
    public int MODIFIER_PAGE_COUNT=6;
    public int PAGE_INDICATOR_CIRCLE_VIEW_HEIGHT=0;
    public int PAGE_INDICATOR_CIRCLE_VIEW_WIDTH=0;
    public int LEFT_RIGHT_PADDING=10;
    public int TOP_DOWN_PADDING=10;
    public float INVOICE_ITEM_NAME_WIDTH_WEIGHT = 0f;
    public float INVOICE_SUB_ITEM_NAME_WIDTH_WEIGHT = 0f;
    public float INVOICE_ITEM_TOTAL_PRICE_WIDTH_WEIGHT = 0f;
    public float INVOICE_SUB_ITEM_PRICE_WIDTH_WEIGHT = 0f;
    public float INVOICE_ITEM_UNIT_PRICE_WIDTH_WEIGHT = 0f;
    public float INVOICE_SUB_ITEM_UNIT_WIDTH_WEIGHT = 0f;
    public float DP_MENU_ITEM_POPUP_MODIFIER_TEXT_SIZE=0;
    public float DP_RECEIPT_ITEM_MENU_TEXT_SIZE=22;
    public float DP_RECEIPT_MODIFIER_TEXT_SIZE=20;
    public int SUB_ITEM_NAME_MAX_LENGTH = 30;
    public float DP_DISCOUNT_AMOUNT_TEXT_SIZE=22;
    public float DP_DISCOUNT_AMOUNT_LABEL_TEXT_SIZE=35;
    public float COMPANY_PROFILE_TEXT_SIZE = 40;

    public TextLengthAndSize(Context c)
    {
        context =c;
        LoadApplicationData();
    }
    private void LoadApplicationData()
    {
        //application font name
        TYPE_FACE_ABEL_FONT = Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family));

        //for displaying on receipt table
        INVOICE_ITEM_NAME_MAX_LENGTH = Integer.parseInt(context.getResources().getString(R.string.invoice_item_name_max_char));
        ADD_MENU_ITEM_MODIFIER_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_add_menu_item_modifier_text);
        ADD_MENU_ITEM_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_add_menu_item_text);
        CHART_OPTIONS_TITLE_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_chart_options_title_text_size);
        DP_PROMOTION_FIELD_LABEL_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_promotion_field_label_text_size);
        SUB_ITEM_NAME_MAX_LENGTH = Integer.parseInt(context.getResources().getString(R.string.sub_item_name_max_char));
        MODIFIER_TABLE_RECEIPT_ROW_NAME_TOP_MARGIN =((MainUIActivity)context).DP2Pixel(Float.parseFloat(context.getString(R.string.dp_top_margin_for_sub_item_name)), context);
        MODIFIER_TABLE_RECEIPT_ROW_PRICE_TOP_MARGIN = ((MainUIActivity)context).DP2Pixel(Float.parseFloat(context.getString(R.string.dp_top_margin_for_sub_item_price)), context);
        MODIFIER_TABLE_RECEIPT_ROW_PADDING = ((MainUIActivity) context).DP2Pixel(Float.parseFloat(context.getString(R.string.dp_padding_size_for_row)), context);

        //for checkout panel
        CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE=context.getResources().getDimension(R.dimen.dp_checkout_panel_fragment_text_size);
        INVENTORY_POPUP_WINDOW_TEXT_SIZE=context.getResources().getDimension(R.dimen.dp_inventory_popup_window_text_size);
        RESOURCE_MANAGEMENT_SELECTION_TITLE_TEXT_SIZE =context.getResources().getDimension(R.dimen.dp_resource_management_selection_title_text_size);
        LABEL_TEXT_SIZE = context.getResources().getDimensionPixelSize(R.dimen.dp_label_text_size);
        //server fragment
        SERVER_FRAGMENT_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_server_fragment_text_size);

        //promotion fragment
        PROMOTION_FRAGMENT_VIEW_MODE_TITLE_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_promotion_fragment_view_mode_title_text_size);
        DP_DISCOUNT_AMOUNT_TEXT_SIZE= context.getResources().getDimension(R.dimen.dp_discount_amount_text_size);
        DP_DISCOUNT_AMOUNT_LABEL_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_discount_amount_label_text_size);
        //active server size
        MAX_SERVER = Integer.parseInt(context.getResources().getString(R.string.max_server));
        MAX_SUPPLIER = Integer.parseInt(context.getResources().getString(R.string.max_supplier));

        SUPPLIER_NAME_MAX_LENGTH = Integer.parseInt(context.getResources().getString(R.string.supplier_name_max_char));
        SERVER_NAME_MAX_LENGTH = Integer.parseInt(context.getResources().getString(R.string.server_name_max_char));
        EMAIL_ADDRESS_MAX_LENGTH = Integer.parseInt(context.getResources().getString(R.string.email_address_max_char));
        SUPPLIER_ADDRESS_MAX_LENGTH = Integer.parseInt(context.getResources().getString(R.string.supplier_address_max_char));

        INVOICE_ITEM_NAME_WIDTH_WEIGHT = Float.parseFloat(context.getResources().getString(R.string.invoice_item_name_width_weight));
        DP_RECEIPT_ITEM_MENU_TEXT_SIZE = Float.parseFloat(context.getResources().getString(R.string.sp_text_size_for_row_item));
        LEFT_RIGHT_PADDING_RECEIPT_ROW = common.Utility.DP2Pixel(Float.parseFloat(context.getResources().getString(R.string.dp_left_right_padding_size_for_row_item)), context);
        TOP_DOWN_PADDING_RECEIPT_ROW=common.Utility.DP2Pixel(Float.parseFloat(context.getResources().getString(R.string.dp_top_bottom_padding_size_for_row_item)), context);
        TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW = common.Utility.DP2Pixel(Float.parseFloat(context.getString(R.string.dp_top_bottom_padding_size_for_row_sub_item)), context);

        INVOICE_ITEM_UNIT_PRICE_WIDTH_WEIGHT = Float.parseFloat(context.getResources().getString(R.string.invoice_item_unit_price_width_weight));
        INVOICE_SUB_ITEM_PRICE_WIDTH_WEIGHT = Float.parseFloat(context.getResources().getString(R.string.invoice_sub_item_price_width_weight));
        INVOICE_ITEM_TOTAL_PRICE_WIDTH_WEIGHT = Float.parseFloat(context.getResources().getString(R.string.invoice_item_total_price_width_weight));
        INVOICE_SUB_ITEM_UNIT_WIDTH_WEIGHT = Float.parseFloat(context.getResources().getString(R.string.invoice_sub_item_unit_width_weight));

        INVOICE_SUB_ITEM_NAME_WIDTH_WEIGHT = Float.parseFloat(context.getResources().getString(R.string.invoice_sub_item_name_width_weight));

        MAX_BARCODE_LENGTH = Integer.parseInt(context.getResources().getString(R.string.max_barcode_length));
        LIST_VIEW_MODE_NUMBER_ITEM_TO_LOAD_PER_BATCH = Integer.parseInt(context.getResources().getString(R.string.list_view_mode_number_item_to_load_per_batch));
        //max item per page
        ITEM_PER_PAGE_TO_DISPLAY_LIST_MODE = Integer.parseInt(context.getResources().getString(R.string.item_per_page_to_display_list_mode));
        ITEM_PER_PAGE_TO_DISPLAY_PIC_MODE = Integer.parseInt(context.getResources().getString(R.string.item_per_page_to_display_pic_mode));

        RECEIPT_DISPLAY_TABLE_ROW_PADDING=common.Utility.DP2Pixel(Float.parseFloat(context.getString(R.string.dp_padding_size_for_row)),context);

        TAP_TO_ADD_BUTTON_WIDTH =common.Utility.DP2Pixel(Float.parseFloat(context.getString(R.string.dp_tap_to_add_button_width)),context);

        //item menu options popup
        MODIFIER_PAGE_COUNT = Integer.parseInt(context.getResources().getString(R.string.modifier_group_page_count));
        PAGE_INDICATOR_CIRCLE_VIEW_HEIGHT = Integer.parseInt(context.getResources().getString(R.string.page_indicator_circle_view_height));
        PAGE_INDICATOR_CIRCLE_VIEW_WIDTH = Integer.parseInt(context.getResources().getString(R.string.page_indicator_circle_view_width));
        LEFT_RIGHT_PADDING = MainUIActivity.DP2Pixel(Float.parseFloat(context.getString(R.string.dp_left_right_padding_size_for_row_item)), context);
        TOP_DOWN_PADDING=MainUIActivity.DP2Pixel(Float.parseFloat(context.getString(R.string.dp_top_bottom_padding_size_for_row_item)), context);
        DP_MENU_ITEM_POPUP_MODIFIER_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_menu_item_popup_modifier_text_size);
        DP_RECEIPT_MODIFIER_TEXT_SIZE=Float.parseFloat(context.getResources().getString(R.string.sp_text_size_for_row_sub_item));

        COMPANY_PROFILE_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_settings_company_profile_text_size);

    }
}
