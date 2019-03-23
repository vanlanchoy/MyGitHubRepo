package tme.pos.BusinessLayer;

import tme.pos.R;


/**
 * Created by kchoy on 2/10/2015.
 */
public  class Enum {
    public enum CompareObjectVersionType {
        Receipt,Order
    }
    public enum RemoveEmptyReceiptResult
    {
        VersionOutOfDate,HasRemovedEmpty, NoUpdateNeeded,WriteToDBFailed
    }
    public enum GetLockResult
    {
        VersionOutOfDate,Granted,TryLater,RecordCountMismatch,NoExistingRecord
    }
    public enum DBOperationResult {
        VersionOutOfDate,Success,Failed,TryLater,Existed
    }
    public enum HashMethod
    {
        MD5(1),SHA1(2),SHA256(3),SHA384(4),SHA512(5);
        public int value;
        HashMethod(int value)
        {
            this.value = value;
        }
    }
    public enum CartItemStatus
    {
        lock,paid,free
    }
    public enum CartItemType
    {
        PromotionAwarded,StoreItem
    }
    public enum Month
    {
        Jan(1),Feb(2),Mar(4),Apr(8),May(16),Jun(32),Jul(64),Aug(128),Sep(256),Oct(512),Nov(1024),Dec(2048);
        public int value;
        Month(int value)
        {
            this.value = value;
        }
    }
    public enum Day
    {
        Monday(1),Tuesday(2),Wednesday(4),Thursday(8),Friday(16),Saturday(32),Sunday(64);
        public int value;
        Day(int value)
        {
            this.value = value;
        }
    }
    public enum OccurrenceWeek
    {
        Weekly(0),TwoWeek(1),ThreeWeek(2),Monthly(3);

        public int value;

        OccurrenceWeek(int value){
            this.value=value;

        }
    }

    public enum PromotionDateOption
    {
        once(0),day(1),month(2);
        public int value;
        private PromotionDateOption(int v)
        {
            value = v;
        }
    }
    public enum PromotionViewMode
    {
        daily,weekly,monthly
    }

    public enum PromotionByType
    {
        item(0),total(1);
        public int value;
        private PromotionByType(int value){
            this.value=value;

        }

        }
    public enum ChartType
    {
        yearly,monthly,daily,receipt

    }
    public enum OperationStatus
    {
        processing,success,fail
    }
    public enum DiscountColor
    {
        discount_blue(R.color.discount_blue,R.color.discount_text_blue),
        discount_green(R.color.discount_green,R.color.discount_text_green),
        discount_indigo(R.color.discount_indigo,R.color.discount_text_indigo),
        discount_pink(R.color.discount_pink,R.color.discount_text_pink),
        discount_yellow(R.color.discount_yellow,R.color.discount_text_yellow),
        discount_orange(R.color.discount_orange,R.color.discount_text_orange),
        discount_brown(R.color.discount_brown,R.color.discount_text_brown);
        public int value;
        public int textColorValue;
        private DiscountColor(int value,int textColor){
            this.value=value;
            this.textColorValue = textColor;
        }

    }
    public enum CallDateDialogFrom
    {
        from,to,label,endOn,day_month_expiration_start_time,day_month_expiration_end_time
    }
    public enum MutualGroupColor
    {
        white(R.color.white,0),
        mutual_dark_brown(R.color.mutual_dark_brown,1),
        mutual_dark_orange(R.color.mutual_dark_orange,2),
        mutual_dark_indigo(R.color.mutual_dark_indigo,3),
        mutual_dark_red(R.color.mutual_dark_red,4),
        mutual_dark_navy(R.color.mutual_dark_navy,5);



        public int value;
        public int group;
        private MutualGroupColor(int value,int group){
            this.value=value;
            this.group = group;
        }




    }
    public enum ViewMode
    {
        list,
        pic
    }
    public enum DiscountType
    {
        cash(0),
        percentage(1),
        none(2);
        public int value;
        private DiscountType(int v)
        {
            value = v;
        }
    }
    public enum PromotionActivityPanel
    {
        calendar_display_type_panel,
        input_promotion_by_panel,
        input_discount_type_panel,
        input_promotion_date_range_panel

    }
    public enum ChoosePhotoFrom
    {
        camera(0),
        gallery(1);

        public int value;
        private ChoosePhotoFrom(int value)
        {
            this.value = value;
        }
    }
    public enum PaymentType
    {
        cash(0),
        paypal(1),
        credit(2);
        public int value;

        private PaymentType(int value){
            this.value=value;

        }
    }
    public enum ModifierType
    {
        individual,
        global,
        individual_and_global;
    }
    public enum PrinterCommandType
    {
        line,
        raster
    }
    public enum CreditCardType
    {
        visa(0),
        master(1),
        american_express(2),
        capital_one(3),
        discovery(4);
        public int value;

        private CreditCardType(int value){
            this.value=value;

        }
    }
    public enum ReceiptNoteAlignment
    {
        left,center;
    }
    public enum ServerGender
    {
        male(0),female(1);
        public int value;

        private ServerGender(int value){
            this.value=value;

        }


    }
    public enum SelectedFloorPlanObject
    {
        scribble(0),arc(1),circle(2),line(3),none(4),table(5);
        public int value;

        private SelectedFloorPlanObject(int value){
            this.value=value;

        }
    }
    public enum FloorPlanMode
    {
        scribble(0),arc(1),eraser(2),move(3),rotate(4),line(5),table(6),none(7),copy(8),select(9),photo(10);
        public int value;

        private FloorPlanMode(int value){
            this.value=value;

        }
    }
    public enum FragmentName
    {
        server(0),supplier(1);
        public int value;
        private FragmentName(int value){this.value=value;}
    }
    public enum ExpandStatus
    {
        expanded,collapsed
    }
    public enum PrintReceiptType
    {
        total,change_balance,credit_card_signature,credit_card_balance
    }
    public enum VisibilityMode{
        invisible,half,visible
    }
}
