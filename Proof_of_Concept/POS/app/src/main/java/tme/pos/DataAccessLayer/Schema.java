package tme.pos.DataAccessLayer;

/**
 * Created by vanlanchoy on 10/15/2014.
 */
public class Schema {
    public static final String UPDATED_BY_COLUMN="UpdatedBy";
    public static final String UPDATED_BY_COLUMN_TYPE="text";
    public static final String UPDATED_DATE_COLUMN="UpdatedDate";
    public static final String UPDATED_DATE_COLUMN_TYPE="integer";
    public static final String VERSION_COLUMN="Version";
    public static final String VERSION_COLUMN_TYPE="integer";
    public static final String LOCK_TIME_STAMP_COLUMN="LockDate";
    public static final String LOCK_TIME_STAMP_COLUMN_TYPE="integer";
    public static final String LOCK_BY_COLUMN="LockBy";
    public static final String LOCK_BY_COLUMN_TYPE="text";
    public static final String ID_COLUMN="Id";
    public static final String ID_COLUMN_TYPE = "integer";
    //not using auto increment = primary key here because for data copy from one device to another in the future
    public static class DataTable_Orders
    {
        public static final String TABLE_NAME="Orders";
        public static final String DATE_COLUMN="Date";
        public static final String DATE_COLUMN_TYPE="integer";
        public static final String GSON_CONTENT_COLUMN="Content";
        public static final String GSON_CONTENT_COLUMN_TYPE="text";
        public static final String RECEIPT_INDEX_COLUMN="ReceiptIndex";
        public static final String RECEIPT_INDEX_COLUMN_TYPE="integer";
        public static final String CUSTOMER_TABLE_ID_COLUMN="CustomerTableId";
        public static final String CUSTOMER_TABLE_ID_COLUMN_TYPE="text";
        public static final String[] GetColumnNames()
        {
            String[] columns ={LOCK_TIME_STAMP_COLUMN
                    ,VERSION_COLUMN
                    ,LOCK_BY_COLUMN
                    ,DATE_COLUMN
                    ,UPDATED_BY_COLUMN
                    ,UPDATED_DATE_COLUMN
                    ,GSON_CONTENT_COLUMN
                    ,RECEIPT_INDEX_COLUMN
                    ,CUSTOMER_TABLE_ID_COLUMN};
            return columns;
        }
    }
    public static class DataTable_FloorPlanData {
        public static final String TABLE_NAME="FloorPlanData";

        public static final String FLOOR_PLAN_COLUMN="ObjectString";
        public static final String FLOOR_PLAN_COLUMN_TYPE="text";

        public static String[] GetColumnNames() {
            String[] columns = {FLOOR_PLAN_COLUMN
                    ,LOCK_TIME_STAMP_COLUMN
                    ,VERSION_COLUMN
                    ,LOCK_BY_COLUMN
                    ,UPDATED_BY_COLUMN
                    ,UPDATED_DATE_COLUMN};
            return columns;
        }
    }
    public static class DataTable_ReceiptCountData {
        public static final String TABLE_NAME="ReceiptCountData";
        public static final String CURRENT_RECEIPT_INDEX_COLUMN="ReceiptIndex";
        public static final String CURRENT_RECEIPT_INDEX_COLUMN_TYPE="integer";


        public static String[] GetColumnNames() {
            String[] columns = {CURRENT_RECEIPT_INDEX_COLUMN
            ,LOCK_TIME_STAMP_COLUMN
                    ,VERSION_COLUMN
                    ,LOCK_BY_COLUMN
                    ,UPDATED_BY_COLUMN
                    ,UPDATED_DATE_COLUMN};
            return columns;
        }
    }
    //not using auto increment = primary key here because for data copy from one device to another in the future
    public static class DataTable_Promotion
    {
        public static final String TABLE_NAME="Promotion";
        public static final String ID_COLUMN="id";
        public static final String ID_COLUMN_TYPE="integer";
        /*public static final String VERSION_COLUMN="version";
        public static final String VERSION_COLUMN_TYPE="integer";*/
        public static final String TITLE_COLUMN="Title";
        public static final String TITLE_COLUMN_TYPE="text";
        public static final String FROM_DATE_TIME_COLUMN="FromDate";
        public static final String FROM_DATE_TIME_COLUMN_TYPE="integer";
        public static final String TO_DATE_TIME_COLUMN="ToDate";
        public static final String TO_DATE_TIME_COLUMN_TYPE="integer";
        public static final String DAY_COLUMN="Day";
        public static final String DAY_COLUMN_TYPE="integer";
        public static final String REPEAT_EVERY_COLUMN="RepeatEvery";
        public static final String REPEAT_EVERY_COLUMN_TYPE="integer";
        public static final String RULE_TYPE_COLUMN="RuleType";
        public static final String RULE_TYPE_COLUMN_TYPE="integer";
        public static final String RULE_ITEM_COLUMN="RuleItem";
        public static final String RULE_ITEM_COLUMN_TYPE="text";
        public static final String RULE_ABOVE_AMOUNT_COLUMN="RuleAboveAmount";
        public static final String RULE_ABOVE_AMOUNT_COLUMN_TYPE="text";
        public static final String RULE_TO_AMOUNT_COLUMN="RuleToAmount";
        public static final String RULE_TO_AMOUNT_COLUMN_TYPE="text";
        public static final String RULE_AMOUNT_NO_LIMIT_COLUMN="RuleAmountNoLimit";
        public static final String RULE_AMOUNT_NO_LIMIT_COLUMN_TYPE="integer";
        public static final String DISCOUNT_BY_COLUMN="DiscountBy";
        public static final String DISCOUNT_BY_COLUMN_TYPE="integer";
        public static final String DISCOUNT_VALUE_COLUMN="DiscountValue";
        public static final String DISCOUNT_VALUE_COLUMN_TYPE="text";
        public static final String COLOR_COLUMN="Color";
        public static final String COLOR_COLUMN_TYPE="integer";
        public static final String ACTIVE_FLAG_COLUMN="IsActive";
        public static final String ACTIVE_FLAG_COLUMN_TYPE="integer";
        public static final String INACTIVE_DATE_COLUMN="InactiveDate";
        public static final String INACTIVE_DATE_COLUMN_TYPE="integer";
        public static final String BY_DAY_OF_WEEK_COLUMN="ByDayOfWeek";
        public static final String BY_DAY_OF_WEEK_COLUMN_TYPE="integer";
        public static final String OCCURRENCE_CALENDAR_MONTHS_COLUMN="OccurrenceCalendarMonth";
        public static final String OCCURRENCE_CALENDAR_MONTHS_COLUMN_TYPE="integer";
        public static final String OCCURRENCE_CALENDAR_DAY_COLUMN="OccurrenceCalendarDay";
        public static final String OCCURRENCE_CALENDAR_DAY_COLUMN_TYPE="text";
        public static final String EXPIRATION_DATE_COLUMN="ExpirationDate";
        public static final String EXPIRATION_DATE_COLUMN_TYPE="integer";
        public static final String PROMOTION_CREATED_DATE_COLUMN="CreatedDate";
        public static final String PROMOTION_CREATED_DATE_COLUMN_TYPE="integer";
        /*public static final String UPDATED_BY_COLUMN="UpdatedBy";
        public static final String UPDATED_BY_COLUMN_TYPE="text";
        public static final String LOCK_TIME_STAMP_COLUMN="LockDate";
        public static final String LOCK_TIME_STAMP_COLUMN_TYPE="integer";
        public static final String LOCK_BY_COLUMN="LockBy";
        public static final String LOCK_BY_COLUMN_TYPE="text";*/
        public static final String[] GetColumnNames()
        {
            String[] columns ={ID_COLUMN,TITLE_COLUMN,FROM_DATE_TIME_COLUMN,TO_DATE_TIME_COLUMN
            ,DAY_COLUMN,REPEAT_EVERY_COLUMN,RULE_TYPE_COLUMN,RULE_ITEM_COLUMN,RULE_ABOVE_AMOUNT_COLUMN
            ,RULE_TO_AMOUNT_COLUMN,RULE_AMOUNT_NO_LIMIT_COLUMN,DISCOUNT_BY_COLUMN,DISCOUNT_VALUE_COLUMN
                    ,COLOR_COLUMN
                    ,ACTIVE_FLAG_COLUMN,INACTIVE_DATE_COLUMN
                    ,BY_DAY_OF_WEEK_COLUMN
                    ,OCCURRENCE_CALENDAR_MONTHS_COLUMN
                    ,OCCURRENCE_CALENDAR_DAY_COLUMN
                    ,EXPIRATION_DATE_COLUMN
                    ,PROMOTION_CREATED_DATE_COLUMN
                    ,VERSION_COLUMN
                    ,UPDATED_BY_COLUMN
                    ,UPDATED_DATE_COLUMN
                    ,LOCK_TIME_STAMP_COLUMN
                    ,LOCK_BY_COLUMN};
            return columns;
        }
    }
    public static class DataTable_CustomList
    {
        public static final String TABLE_NAME="CustomList";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer ";
        public static final String PAGE_TITLE_COLUMN="PageTitle";
        public static final String PAGE_TITLE_COLUMN_TYPE="text";
        public static final String PAGE_CONTENT_COLUMN="PageContent";
        public static final String PAGE_CONTENT_COLUMN_TYPE="text";
        /*public static final String UPDATED_BY_COLUMN="UpdatedBy";
        public static final String UPDATED_BY_COLUMN_TYPE="text";
        public static final String VERSION_COLUMN="Version";
        public static final String VERSION_COLUMN_TYPE="integer";
        public static final String LOCK_TIME_STAMP_COLUMN="LockDate";
        public static final String LOCK_TIME_STAMP_COLUMN_TYPE="integer";
        public static final String LOCK_BY_COLUMN="LockBy";
        public static final String LOCK_BY_COLUMN_TYPE="text";*/
        public static final String[] GetColumnNames()
        {
            String[] columns={ID_COLUMN,
                    PAGE_TITLE_COLUMN,
                    PAGE_CONTENT_COLUMN,
                    UPDATED_BY_COLUMN,
                    UPDATED_DATE_COLUMN,
                    VERSION_COLUMN,
                    LOCK_TIME_STAMP_COLUMN,
                    LOCK_BY_COLUMN};
            return columns;
        }
    }
    public static class DataTable_PaymentType
    {
        public static final String TABLE_NAME="PaymentType";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer ";
        public static final String PAYMENT_NAME_COLUMN="Type";//cash,credit card, paypal
        public static final String PAYMENT_NAME_COLUMN_TYPE="text";
        public static final String[] GetColumnNames()
        {
            String[] columns ={ID_COLUMN,PAYMENT_NAME_COLUMN};
            return columns;
        }
    }
    public static class DataTable_Server
    {
        public static final String TABLE_NAME="Server";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer ";
        public static final String SERVER_NAME_COLUMN="Name";
        public static final String SERVER_NAME_COLUMN_TYPE="text";
        public static final String ACTIVE_FLAG_COLUMN = "IsActive";
        public static final String ACTIVE_FLAG_COLUMN_TYPE = "integer";
        public static final String GENDER_FLAG_COLUMN = "Gender";
        public static final String GENDER_FLAG_COLUMN_TYPE = "integer";
        public static final String PHONE_COLUMN = "Phone";
        public static final String PHONE_COLUMN_TYPE = "text";
        public static final String INACTIVE_DATE_COLUMN="InactiveDate";
        public static final String INACTIVE_DATE_COLUMN_TYPE="integer";
        public static final String PICTURE_PATH_COLUMN = "Picture";
        public static final String PICTURE_PATH_COLUMN_TYPE = "text";
        public static final String ADDRESS_COLUMN="address";
        public static final String ADDRESS_COLUMN_TYPE="text";
        public static final String NOTE_COLUMN="note";
        public static final String NOTE_COLUMN_TYPE="text";
        public static final String EMAIL_COLUMN="email";
        public static final String EMAIL_COLUMN_TYPE="text";
        /*public static final String UPDATED_BY_COLUMN="UpdatedBy";
        public static final String UPDATED_BY_COLUMN_TYPE="text";
        public static final String VERSION_COLUMN="Version";
        public static final String VERSION_COLUMN_TYPE="integer";
        public static final String LOCK_TIME_STAMP_COLUMN="LockDate";
        public static final String LOCK_TIME_STAMP_COLUMN_TYPE="integer";
        public static final String LOCK_BY_COLUMN="LockBy";
        public static final String LOCK_BY_COLUMN_TYPE="text";*/
        public static final String[] GetColumnNames()
        {
            String[] columns ={ID_COLUMN,SERVER_NAME_COLUMN,ACTIVE_FLAG_COLUMN,GENDER_FLAG_COLUMN,PHONE_COLUMN
                    ,INACTIVE_DATE_COLUMN,PICTURE_PATH_COLUMN,ADDRESS_COLUMN,NOTE_COLUMN,EMAIL_COLUMN
                    ,UPDATED_BY_COLUMN,UPDATED_DATE_COLUMN,VERSION_COLUMN,LOCK_BY_COLUMN,LOCK_TIME_STAMP_COLUMN};
            return columns;
        }
    }

    public static class DataTable_Receipt
    {
        public static final String TABLE_NAME="Receipt";
        public static final String DEVICE_NAME_COLUMN="DeviceId";
        public static final String DEVICE_NAME_COLUMN_TYPE="text";
        public static final String PAYMENT_TYPE_ID_COLUMN="PaymentTypeId";
        public static final String PAYMENT_TYPE_ID_COLUMN_TYPE="integer";
        public static final String RECEIPT_DATE_COLUMN="ReceiptDate";
        public static final String RECEIPT_DATE_COLUMN_TYPE="integer";
        public static final String CANCEL_FLAG_COLUMN="IsCancel";
        public static final String CANCEL_FLAG_COLUMN_TYPE="integer";
        public static final String CANCEL_DATE_COLUMN="CancelDate";
        public static final String CANCEL_DATE_COLUMN_TYPE="integer";
        public static final String GRATUITY_COLUMN = "Tip";
        public static final String GRATUITY_COLUMN_TYPE = "integer";
        public static final String DISCOUNT_COLUMN = "Discount";
        public static final String DISCOUNT_COLUMN_TYPE = "real";
        public static final String TAX_COLUMN = "Tax";
        public static final String TAX_COLUMN_TYPE = "real";
        public static final String TAX_AMOUNT_COLUMN="TaxAmount";
        public static final String TAX_AMOUNT_COLUMN_TYPE="real";
        public static final String SERVER_ID_COLUMN = "Server";
        public static final String SERVER_ID_COLUMN_TYPE = "integer";
        public static final String RECEIPT_NUMBER_COLUMN="ReceiptNumber";//prefix + increment counter
        public static final String RECEIPT_NUMBER_COLUMN_TYPE="text";
        public static final String AMOUNT_WITH_PROMOTION_AND_ADDITIONAL_DISCOUNT_COLUMN = "AmountWithPromotionAndAdditionalDiscount";
        public static final String AMOUNT_WITH_PROMOTION_AND_ADDITIONAL_DISCOUNT_COLUMN_Type ="real";
        public static final String TOTAL_COLUMN="Total";
        public static final String TOTAL_COLUMN_TYPE="real";
        public static final String DINING_TABLE_COLUMN="DiningTableNumber";
        public static final String DINING_TABLE_COLUMN_TYPE="text";
        public static final String CREDIT_CARD_NUMBER_COLUMN="CreditCardNumber";
        public static final String CREDIT_CARD_NUMBER_COLUMN_TYPE="text";
        public static final String CREDIT_CARD_HOLDER_COLUMN="CreditCardHolder";
        public static final String CREDIT_CARD_HOLDER_COLUMN_TYPE="text";
        public static final String CREDIT_CARD_EXP_COLUMN="CreditCardExp";
        public static final String CREDIT_CARD_EXP_COLUMN_TYPE="text";
        public static final String CREDIT_CARD_CVV_COLUMN="CreditCardCVV";
        public static final String CREDIT_CARD_CVV_COLUMN_TYPE="text";
        public static final String ACTIVE_FLAG_COLUMN = "IsActive";
        public static final String ACTIVE_FLAG_COLUMN_TYPE = "integer";
        public static final String TRANSACTION_ID_COLUMN = "TransactionID";
        public static final String TRANSACTION_ID_COLUMN_TYPE="text";
        public static final String CART_ITEM_COLUMN = "CartItem";//[item id,modifier id 1,modifier id 2....,modifier id N]
        public static final String CART_ITEM_COLUMN_TYPE="text";
        public static final String LATITUDE_COLUMN="Latitude";
        public static final String LATITUDE_COLUMN_TYPE="real";
        public static final String LONGITUDE_COLUMN="Longitude";
        public static final String LONGITUDE_COLUMN_TYPE="real";
        public static final String CART_GUID_COLUMN="CartGUID";
        public static final String CART_GUID_COLUMN_TYPE="text";
        public static final String PROMOTION_ID_COLUMN="Promotion";
        public static final String PROMOTION_ID_COLUMN_TYPE="text";
        public static final String LINKED_RECEIPT_COLUMN="LinkedReceipt";
        public static final String LINKED_RECEIPT_COLUMN_TYPE="text";
        public static final String[] GetColumnNames()
        {
        String[] columns ={DEVICE_NAME_COLUMN,PAYMENT_TYPE_ID_COLUMN,RECEIPT_DATE_COLUMN,CANCEL_FLAG_COLUMN,CANCEL_DATE_COLUMN
                ,GRATUITY_COLUMN,DISCOUNT_COLUMN,TAX_COLUMN,TAX_AMOUNT_COLUMN,SERVER_ID_COLUMN,RECEIPT_NUMBER_COLUMN
        ,TOTAL_COLUMN,DINING_TABLE_COLUMN,CREDIT_CARD_NUMBER_COLUMN,CREDIT_CARD_HOLDER_COLUMN,CREDIT_CARD_EXP_COLUMN
        ,CREDIT_CARD_CVV_COLUMN,ACTIVE_FLAG_COLUMN,TRANSACTION_ID_COLUMN,CART_ITEM_COLUMN,LATITUDE_COLUMN,LONGITUDE_COLUMN,
                CART_GUID_COLUMN,PROMOTION_ID_COLUMN,AMOUNT_WITH_PROMOTION_AND_ADDITIONAL_DISCOUNT_COLUMN,LINKED_RECEIPT_COLUMN};
        return columns;
    }
    }
    public static class DataTable_Category
    {
        public static final String TABLE_NAME="category";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer";
        public static final String CATEGORY_NAME_COLUMN="Name";
        public static final String CATEGORY_NAME_COLUMN_TYPE="text";
        public static final String ACTIVE_FLAG_COLUMN="IsActive";
        public static final String ACTIVE_FLAG_COLUMN_TYPE="integer";
        public static final String INACTIVE_DATE_COLUMN="InactiveDate";
        public static final String INACTIVE_DATE_COLUMN_TYPE="integer";
        /*public static final String UPDATED_BY_COLUMN="UpdatedBy";
        public static final String UPDATED_BY_COLUMN_TYPE="text";
        public static final String VERSION_COLUMN="Version";
        public static final String VERSION_COLUMN_TYPE="integer";
        public static final String LOCK_TIME_STAMP_COLUMN="LockDate";
        public static final String LOCK_TIME_STAMP_COLUMN_TYPE="integer";
        public static final String LOCK_BY_COLUMN="LockBy";
        public static final String LOCK_BY_COLUMN_TYPE="text";*/
        public static final String[] GetColumnNames() {
            String[] columns = {ID_COLUMN,CATEGORY_NAME_COLUMN,ACTIVE_FLAG_COLUMN,INACTIVE_DATE_COLUMN
            ,UPDATED_BY_COLUMN,UPDATED_DATE_COLUMN,VERSION_COLUMN,LOCK_TIME_STAMP_COLUMN,LOCK_BY_COLUMN};
            return columns;
        }
    }

    public static class DataTable_Item
    {
        public static final String TABLE_NAME="item";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer";
        public static final String PARENT_ID_COLUMN = "ParentID";
        public static final String PARENT_ID_COLUMN_TYPE="integer";
        public static final String ITEM_NAME_COLUMN="Name";
        public static final String ITEM_NAME_COLUMN_TYPE="text";
        public static final String ACTIVE_FLAG_COLUMN="IsActive";
        public static final String ACTIVE_FLAG_COLUMN_TYPE="integer";
        public static final String ITEM_PRICE_COLUMN="Price";
        public static final String ITEM_PRICE_COLUMN_TYPE="text";
        public static final String INACTIVE_DATE_COLUMN="InactiveDate";
        public static final String INACTIVE_DATE_COLUMN_TYPE="integer";
        public static final String PICTURE_PATH_COLUMN = "Picture";
        public static final String PICTURE_PATH_COLUMN_TYPE = "text";
        public static final String DO_NOT_TRACK_COLUMN = "DoNotTrack";
        public static final String DO_NOT_TRACK_COLUMN_TYPE = "integer";
        public static final String BARCODE_COLUMN="barcode";
        public static final String BARCODE_COLUMN_TYPE="long";
       /* public static final String UPDATED_BY_COLUMN="UpdatedBy";
        public static final String UPDATED_BY_COLUMN_TYPE="text";
        public static final String VERSION_COLUMN="Version";
        public static final String VERSION_COLUMN_TYPE="integer";
        public static final String LOCK_TIME_STAMP_COLUMN="LockDate";
        public static final String LOCK_TIME_STAMP_COLUMN_TYPE="integer";
        public static final String LOCK_BY_COLUMN="LockBy";
        public static final String LOCK_BY_COLUMN_TYPE="text";*/

        public static final String[] GetColumnNames() {
            String[] columns = {ID_COLUMN,PARENT_ID_COLUMN,ITEM_NAME_COLUMN,ACTIVE_FLAG_COLUMN
                    ,ITEM_PRICE_COLUMN,INACTIVE_DATE_COLUMN,PICTURE_PATH_COLUMN,DO_NOT_TRACK_COLUMN
                    ,BARCODE_COLUMN,VERSION_COLUMN,
            UPDATED_BY_COLUMN,UPDATED_DATE_COLUMN,LOCK_BY_COLUMN,LOCK_TIME_STAMP_COLUMN};
            return columns;
        }
    }
    public static class DataTable_Modifier
    {
        public static final String TABLE_NAME="modifier";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer";
        public static final String PARENT_ID_COLUMN = "ParentItemID";
        public static final String PARENT_ID_COLUMN_TYPE="integer";
        public static final String ITEM_NAME_COLUMN="Name";
        public static final String ITEM_NAME_COLUMN_TYPE="text";
        public static final String ACTIVE_FLAG_COLUMN="IsActive";
        public static final String ACTIVE_FLAG_COLUMN_TYPE="integer";
        public static final String ITEM_PRICE_COLUMN="Price";
        public static final String ITEM_PRICE_COLUMN_TYPE="text";
        public static final String MUTUAL_GROUP_COLUMN="GroupId";
        public static final String MUTUAL_GROUP_COLUMN_TYPE="integer";
        public static final String INACTIVE_DATE_COLUMN="InactiveDate";
        public static final String INACTIVE_DATE_COLUMN_TYPE="integer";
        /*public static final String UPDATED_BY_COLUMN="UpdatedBy";
        public static final String UPDATED_BY_COLUMN_TYPE="text";
        public static final String VERSION_COLUMN="Version";
        public static final String VERSION_COLUMN_TYPE="integer";
        public static final String LOCK_TIME_STAMP_COLUMN="LockDate";
        public static final String LOCK_TIME_STAMP_COLUMN_TYPE="integer";
        public static final String LOCK_BY_COLUMN="LockBy";
        public static final String LOCK_BY_COLUMN_TYPE="text";*/
        public static final String[] GetColumnNames() {
            String[] columns = {ID_COLUMN,PARENT_ID_COLUMN,ITEM_NAME_COLUMN,ACTIVE_FLAG_COLUMN
                    ,ITEM_PRICE_COLUMN,MUTUAL_GROUP_COLUMN,INACTIVE_DATE_COLUMN,VERSION_COLUMN
            ,UPDATED_BY_COLUMN,UPDATED_DATE_COLUMN,LOCK_BY_COLUMN,LOCK_TIME_STAMP_COLUMN};
            return columns;
        }
    }
    public static class DataTable_Inventory
    {
        public static final String TABLE_NAME="inventory";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer";
        public static final String ITEM_COLUMN="item";
        public static final String ITEM_COLUMN_TYPE="integer";
        public static final String SUPPLIER_COLUMN="Supplier";
        public static final String SUPPLIER_COLUMN_TYPE="integer";
        public static final String RECORD_DATE_COLUMN="RecordDate";
        public static final String RECORD_DATE_COLUMN_TYPE="integer";
        public static final String UNIT_COLUMN="Unit";
        public static final String UNIT_COLUMN_TYPE="integer";
        public static final String COST_PRICE_COLUMN="CostPrice";
        public static final String COST_PRICE_COLUMN_TYPE="text";
        public static final String ACTIVE_FLAG_COLUMN="IsActive";
        public static final String ACTIVE_FLAG_COLUMN_TYPE="integer";
        public static final String INACTIVE_DATE_COLUMN="InactiveDate";
        public static final String INACTIVE_DATE_COLUMN_TYPE="integer";
        public static final String[] GetColumnNames() {
            String[] columns = {ID_COLUMN,ITEM_COLUMN,SUPPLIER_COLUMN,RECORD_DATE_COLUMN
                    ,UNIT_COLUMN,COST_PRICE_COLUMN,ACTIVE_FLAG_COLUMN,INACTIVE_DATE_COLUMN};
            return columns;
        }
    }
    public static class DataTable_Supplier
    {
        public static final String TABLE_NAME="supplier";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer";
        public static final String NAME_COLUMN="Name";
        public static final String NAME_COLUMN_TYPE="text";
        public static final String ADDRESS_COLUMN="address";
        public static final String ADDRESS_COLUMN_TYPE="text";
        public static final String PHONE_COLUMN="Phone";
        public static final String PHONE_COLUMN_TYPE="text";
        public static final String EMAIL_COLUMN="Email";
        public static final String EMAIL_COLUMN_TYPE="text";
        public static final String ACTIVE_FLAG_COLUMN="IsActive";
        public static final String ACTIVE_FLAG_COLUMN_TYPE="integer";
        public static final String INACTIVE_DATE_COLUMN="InactiveDate";
        public static final String INACTIVE_DATE_COLUMN_TYPE="integer";
        public static final String NOTE_COLUMN="note";
        public static final String NOTE_COLUMN_TYPE="text";
       /* public static final String UPDATED_BY_COLUMN="UpdatedBy";
        public static final String UPDATED_BY_COLUMN_TYPE="text";
        public static final String VERSION_COLUMN="Version";
        public static final String VERSION_COLUMN_TYPE="integer";
        public static final String LOCK_TIME_STAMP_COLUMN="LockDate";
        public static final String LOCK_TIME_STAMP_COLUMN_TYPE="integer";
        public static final String LOCK_BY_COLUMN="LockBy";
        public static final String LOCK_BY_COLUMN_TYPE="text";*/
        public static final String[] GetColumnNames() {
            String[] columns = {ID_COLUMN,NAME_COLUMN,ADDRESS_COLUMN,PHONE_COLUMN
                    ,EMAIL_COLUMN,ACTIVE_FLAG_COLUMN,INACTIVE_DATE_COLUMN,NOTE_COLUMN
            ,VERSION_COLUMN,LOCK_BY_COLUMN,LOCK_TIME_STAMP_COLUMN,UPDATED_BY_COLUMN,UPDATED_DATE_COLUMN};
            return columns;
        }
    }
    public static class DataTable_PromotionUpdateLog
    {
        public static final String TABLE_NAME="PromotionUpdateLog";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer";
        /*public static final String VERSION_COLUMN="Version";
        public static final String VERSION_COLUMN_TYPE="integer";*/
        public static final String TITLE_COLUMN="Title";
        public static final String TITLE_COLUMN_TYPE="text";
        public static final String FROM_DATE_TIME_COLUMN="FromDate";
        public static final String FROM_DATE_TIME_COLUMN_TYPE="integer";
        public static final String TO_DATE_TIME_COLUMN="ToDate";
        public static final String TO_DATE_TIME_COLUMN_TYPE="integer";
        public static final String DAY_COLUMN="Day";
        public static final String DAY_COLUMN_TYPE="integer";
        public static final String REPEAT_EVERY_COLUMN="RepeatEvery";
        public static final String REPEAT_EVERY_COLUMN_TYPE="integer";
        public static final String RULE_TYPE_COLUMN="RuleType";
        public static final String RULE_TYPE_COLUMN_TYPE="integer";
        public static final String RULE_ITEM_COLUMN="RuleItem";
        public static final String RULE_ITEM_COLUMN_TYPE="text";
        public static final String RULE_ABOVE_AMOUNT_COLUMN="RuleAboveAmount";
        public static final String RULE_ABOVE_AMOUNT_COLUMN_TYPE="text";
        public static final String RULE_TO_AMOUNT_COLUMN="RuleToAmount";
        public static final String RULE_TO_AMOUNT_COLUMN_TYPE="text";
        public static final String RULE_AMOUNT_NO_LIMIT_COLUMN="RuleAmountNoLimit";
        public static final String RULE_AMOUNT_NO_LIMIT_COLUMN_TYPE="integer";
        public static final String DISCOUNT_BY_COLUMN="DiscountBy";
        public static final String DISCOUNT_BY_COLUMN_TYPE="integer";
        public static final String DISCOUNT_VALUE_COLUMN="DiscountValue";
        public static final String DISCOUNT_VALUE_COLUMN_TYPE="text";
        public static final String BY_DAY_OF_WEEK_COLUMN="ByDayOfWeek";
        public static final String BY_DAY_OF_WEEK_COLUMN_TYPE="integer";
        public static final String OCCURRENCE_CALENDAR_MONTHS_COLUMN="OccurrenceCalendarMonth";
        public static final String OCCURRENCE_CALENDAR_MONTHS_COLUMN_TYPE="integer";
        public static final String OCCURRENCE_CALENDAR_DAY_COLUMN="OccurrenceCalendarDay";
        public static final String OCCURRENCE_CALENDAR_DAY_COLUMN_TYPE="text";
        public static final String EXPIRATION_DATE_COLUMN="ExpirationDate";
        public static final String EXPIRATION_DATE_COLUMN_TYPE="integer";
        public static final String DATE_COLUMN="CreatedDate";
        public static final String DATE_COLUMN_TYPE="integer";

        public static final String[] GetColumnNames() {
            String[] columns = {ID_COLUMN,TITLE_COLUMN,FROM_DATE_TIME_COLUMN,TO_DATE_TIME_COLUMN,DAY_COLUMN
            ,REPEAT_EVERY_COLUMN,RULE_TYPE_COLUMN,RULE_ITEM_COLUMN,RULE_ABOVE_AMOUNT_COLUMN,RULE_TO_AMOUNT_COLUMN
            ,RULE_AMOUNT_NO_LIMIT_COLUMN,DISCOUNT_BY_COLUMN,DISCOUNT_VALUE_COLUMN,BY_DAY_OF_WEEK_COLUMN
            ,OCCURRENCE_CALENDAR_MONTHS_COLUMN,OCCURRENCE_CALENDAR_DAY_COLUMN,EXPIRATION_DATE_COLUMN
                    ,VERSION_COLUMN,DATE_COLUMN
            };
            return columns;
        }

    }
    public static class DataTable_ItemAndModifierUpdateLog
    {
        public static final String TABLE_NAME="ItemUpdateLog";
        public static final String ID_COLUMN="_id";
        public static final String ID_COLUMN_TYPE="integer";
        public static final String NAME_COLUMN="Name";
        public static final String NAME_COLUMN_TYPE="text";
        public static final String PRICE_COLUMN="Price";
        public static final String PRICE_COLUMN_TYPE="text";
        public static final String DATE_COLUMN="LogDate";
        public static final String DATE_COLUMN_TYPE="integer";
        /*public static final String VERSION_COLUMN="version";
        public static final String VERSION_COLUMN_TYPE="integer";*/

        public static final String[] GetColumnNames() {
            String[] columns = {ID_COLUMN,NAME_COLUMN,PRICE_COLUMN,DATE_COLUMN,VERSION_COLUMN};
            return columns;
        }

    }

    public static String[] GetTableColumns(String strTableName)
    {
        if(strTableName.compareToIgnoreCase(DataTable_Item.TABLE_NAME)==0)
        {
            return DataTable_Item.GetColumnNames();
        }
        if(strTableName.compareToIgnoreCase(DataTable_Server.TABLE_NAME)==0)
        {
            return DataTable_Server.GetColumnNames();
        }
        if(strTableName.compareToIgnoreCase(DataTable_Receipt.TABLE_NAME)==0)
        {
            return DataTable_Receipt.GetColumnNames();
        }
        if(strTableName.compareToIgnoreCase(DataTable_Category.TABLE_NAME)==0)
        {
            return DataTable_Category.GetColumnNames();
        }
        if(strTableName.compareToIgnoreCase(DataTable_Item.TABLE_NAME)==0)
        {
            return DataTable_Item.GetColumnNames();
        }
        if(strTableName.compareToIgnoreCase(DataTable_Modifier.TABLE_NAME)==0)
        {
            return DataTable_Modifier.GetColumnNames();
        }
        if(strTableName.compareToIgnoreCase(DataTable_Inventory.TABLE_NAME)==0)
        {
            return DataTable_Inventory.GetColumnNames();
        }
        if(strTableName.compareToIgnoreCase(DataTable_Supplier.TABLE_NAME)==0)
        {
            return DataTable_Supplier.GetColumnNames();
        }
        if(strTableName.compareToIgnoreCase(DataTable_ItemAndModifierUpdateLog.TABLE_NAME)==0)
        {
            return DataTable_ItemAndModifierUpdateLog.GetColumnNames();
        }

        return new String[0];
    }
}
