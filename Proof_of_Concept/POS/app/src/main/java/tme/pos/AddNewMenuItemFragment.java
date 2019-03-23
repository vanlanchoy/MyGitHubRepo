package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.util.TypedValue;
import android.widget.ToggleButton;


import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;

import tme.pos.CustomViewCtr.ModifierFlippableTableRow;
import tme.pos.CustomViewCtr.TapToAddTextView;

/**
 * Created by kchoy on 12/30/2014.
 */
public class AddNewMenuItemFragment extends PhotoFeatureFragment implements TapToAddTextView.ITappedListener {//Fragment {

    private final static String PARAM_PARENT_KEY="SelectedParentId";
    private static String MODIFIER_ITEM_NAME_LABEL="Name: ";
    private static String MODIFIER_ITEM_PRICE_LABEL="Price: ";
    private static int INVOICE_ITEM_NAME_MAX_CHAR=50;
    private static int MODIFIER_NAME_MAX_CHAR=30;
    private static float ADD_MENU_ITEM_TITLE_TEXT_SIZE=50;
    private static float ADD_MENU_ITEM_TEXT=30;
    private static float ADD_MODIFIER_TEXT=35;
    private static int MODIFIER_COLOR_VIEW_WIDTH_HEIGHT=40;
    //private  Resources resources;
    private long CategoryId=-1;
    static AddNewMenuItemFragment mif;
    private static MainUIActivity MainActivity;
    private static ArrayList<ModifierObject>OriginalModifiers;
    private static ItemObject myItemObject;
    private static String CATEGORY_NAME="";
    private static String HINT_MODIFIER_NAME="Maximum %1$d characters";
    private static String HINT_MODIFIER_PRICE="$0.00";
    private static int MAX_MODIFIER_ITEM=10;
    private static float TAP_TO_ADD_BUTTON_WIDTH=800f;
    private View FragmentView;
    //private static final String TAG = "MyApp";
    //private static final String KEY_APP_CRASHED = "KEY_APP_CRASHED";
    private ArrayList<ModifierObject> NotOnDisplayModifierList;
    private  boolean blnIsIndividualTab = true;
    private long GLOBAL_MODIFIER_PARENT_ID=-1;
    private long NEW_MODIFIER_PARENT_ID=-2;
    ImageView imgItemPic;
    static ImageView imgInPgViewCtr;
    boolean blnSaving = false;
    CheckBox chkDoNotTrack;
    EditText txtItemName;
    EditText txtBarcode;
    EditText txtPrice;
    //String strSavedImageUri;

    @Override
    protected void SavedPictureResult(String strSavedPicPath) {

        if(strSavedPicPath.length()==0)return;//saved failed return;
        //delete the existing physical pic if any
        if(imgItemPic.getTag()!=null) {
            DeleteExistingItemPic((String) imgItemPic.getTag());
        }
        Bitmap bitmap =DecodeBitmapFile(strSavedPicPath);
        imgItemPic.setImageBitmap(bitmap);
        imgItemPic.setBackground(null);
        imgItemPic.setTag(strSavedPicPath);
        if(imgInPgViewCtr!=null) {
            imgInPgViewCtr.setImageBitmap(bitmap);
            imgInPgViewCtr.setBackground(null);
        }

        FragmentView.findViewById(R.id.tvRemovePic).setVisibility(View.VISIBLE);
    }

    public AddNewMenuItemFragment(){}


    public static AddNewMenuItemFragment GetInstance(){return mif;}
    public static AddNewMenuItemFragment newInstance(long CategoryId,int Max_Char_Item_Name,
                                                     float Title_Text_Size,float Text_Size,String strCategory,
                                                     float Modifier_Label_Size,
                                                     MainUIActivity mua,
                                                     String strModifierItemLabel,
                                                     int maxModifierItem,
                                                     int maxSubItemNameChar,
                                                     String strModifierHint,
                                                     String strModifierPrice,
                                                     String strModifierPriceLabel,
                                                     int intColorViewSize,
                                                     ArrayList<ModifierObject>modifiers,
                                                     ItemObject itemObject,
                                                     ImageView pgViewImageCtr)
    {
        mif = new AddNewMenuItemFragment();
        Bundle args = new Bundle();
        args.putLong(PARAM_PARENT_KEY,CategoryId);
        mif.setArguments(args);
        INVOICE_ITEM_NAME_MAX_CHAR = Max_Char_Item_Name;
        ADD_MENU_ITEM_TITLE_TEXT_SIZE = Title_Text_Size;
        ADD_MENU_ITEM_TEXT = Text_Size;
        CATEGORY_NAME = strCategory;
        ADD_MODIFIER_TEXT = Modifier_Label_Size;
        MainActivity = mua;
        MODIFIER_ITEM_NAME_LABEL = strModifierItemLabel;
        MAX_MODIFIER_ITEM = maxModifierItem;
        MODIFIER_NAME_MAX_CHAR=maxSubItemNameChar;
        HINT_MODIFIER_NAME=strModifierHint;
        HINT_MODIFIER_PRICE = strModifierPrice;
        MODIFIER_ITEM_PRICE_LABEL = strModifierPriceLabel;
        MODIFIER_COLOR_VIEW_WIDTH_HEIGHT = intColorViewSize;
        OriginalModifiers = modifiers;//consisting individual and global modifiers
        myItemObject = itemObject;
        TAP_TO_ADD_BUTTON_WIDTH = Float.parseFloat(mua.getResources().getString(R.string.dp_tap_to_add_button_width));
        imgInPgViewCtr = pgViewImageCtr;
        return mif;
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }
    @Override
    public void onPause()
    {
        super.onPause();

    }
    @Override
    public void onStart()
    {
        //UI visible to the user
        super.onStart();
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        //GoogleAnalytics.getInstance(MainActivity).reportActivityStart(MainActivity);
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //retrieve argument
        CategoryId = getArguments().getLong(PARAM_PARENT_KEY);
        //((POS_Application) MainActivity.getApplication()).getTracker(POS_Application.TrackerName.APP_TRACKER);
    }
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedIntanceState)
    {

        final View v =  inflater.inflate(R.layout.layout_add_menu_item_popup_window_ui,container,false);

        FragmentView = v;


        //delete
        final ImageButton imgDelete = (ImageButton)v.findViewById(R.id.imgDelete);
        common.control_events.SetOnTouchImageButtonEffect(imgDelete,R.drawable.green_border_outer_glow_delete,R.drawable.green_border_delete);
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("Confirm");
                b.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.question),getResources(),36,36));
                b.setMessage("Delete current Item?");
                b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                b.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        MainActivity.DeleteItem(myItemObject.getID());
                        dialogInterface.dismiss();

                    }
                });
                b.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        InputMethodManager inputManager = (InputMethodManager) MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(imgDelete.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                });
                b.show();
            }
        });
        //save button
        final ImageButton imgSave = (ImageButton)v.findViewById(R.id.imgSaveAddNew);
        common.control_events.SetOnTouchImageButtonEffect(imgSave,R.drawable.green_border_outer_glow_save,R.drawable.green_border_save);
        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(blnSaving)return;
                blnSaving=true;
                btnSaveClick(v);
                common.control_events.HideSoftKeyboard(imgSave);
                //InputMethodManager inputManager = (InputMethodManager) MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                //inputManager.hideSoftInputFromWindow(imgSave.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        //cancel button
        final ImageButton imgCancel = (ImageButton)v.findViewById(R.id.imgCancelAddNew);
        common.control_events.SetOnTouchImageButtonEffect(imgCancel, R.drawable.green_border_outer_glow_cancel, R.drawable.green_border_cancel);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCancelClick(view);
                InputMethodManager inputManager = (InputMethodManager) MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(imgCancel.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });


        //itemp profile pic
        imgItemPic = (ImageView)v.findViewById(R.id.imgItemPic);
        imgItemPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPhoto();
            }
        });
        //item pic if any
        if(myItemObject!=null && myItemObject.getPicturePath().length()>0) {
            imgItemPic.setImageBitmap(DecodeBitmapFile(myItemObject.getPicturePath()));
            imgItemPic.setTag(myItemObject.getPicturePath());
        }
        //add modifier row
        //final ExpandableAddNewItemTextView tvAddModifier = new ExpandableAddNewItemTextView(MainActivity,-1, common.Utility.DP2Pixel(TAP_TO_ADD_BUTTON_WIDTH,MainActivity));
        TapToAddTextView tvAddModifier = new TapToAddTextView(getActivity());
        ((TableRow)FragmentView.findViewById(R.id.trAddNewModifier)).addView(tvAddModifier);
        tvAddModifier.SetListener(this);
        // /ExpandableAddNewItemTextView tvAddModifier =CreateTapToAddButton();

        TableRow.LayoutParams trlp = new TableRow.LayoutParams(600, ViewGroup.LayoutParams.WRAP_CONTENT);
        trlp.setMargins(10,0,0,20);
        tvAddModifier.setLayoutParams(trlp);



        //title text size
        TextView tvTitle = (TextView)v.findViewById(R.id.lblNewItemWindowTitle);
        //tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,ADD_MENU_ITEM_TITLE_TEXT_SIZE);
        String strSubjectTitle =(myItemObject==null)?tvTitle.getText()+"":"Edit Item";
        tvTitle.setText(CATEGORY_NAME +" - "+strSubjectTitle);
        tvTitle.setTextColor(getResources().getColor(R.color.divider_grey));
        tvTitle.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)), Typeface.BOLD);

        //item name label
        TextView tvItem = (TextView)v.findViewById(R.id.lblItemName);
        tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT);
        tvItem.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));

        TextView tvName = (TextView)v.findViewById(R.id.lblItemPrice);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT);
        tvName.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));

        TextView tvBarcode = (TextView)v.findViewById(R.id.lblBarcode);
        tvBarcode.setTextSize(TypedValue.COMPLEX_UNIT_DIP,ADD_MENU_ITEM_TEXT);
        tvBarcode.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));

        chkDoNotTrack = (CheckBox)v.findViewById(R.id.chkDoNotTrack);
        chkDoNotTrack.setChecked(true);
       /* //set click listener for toggle button
        ToggleButton tglBtn = (ToggleButton)v.findViewById(R.id.toggleValuePrefix);
        tglBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onToggleClicked(view);
            }
        });*/

        //set para for edit text section
        txtItemName = (EditText)v.findViewById(R.id.txtItemName);
        txtItemName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT);
        String strHint=txtItemName.getHint()+"";
        txtItemName.setHint(strHint.replace("%1$d", INVOICE_ITEM_NAME_MAX_CHAR + ""));
        txtItemName.setFilters(CreateMaxLengthFilter(INVOICE_ITEM_NAME_MAX_CHAR));
        txtItemName.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));


        //textview to delete item pic
        v.findViewById(R.id.tvRemovePic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteItemPicDialog(myItemObject);
            }
        });

        //set para for price edit text
        txtPrice = (EditText)v.findViewById(R.id.txtItemPrice);
        txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT);
        txtPrice.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));
        txtPrice.addTextChangedListener(new TextWatcher() {
            boolean isEditing;
            String strPrevious = "";

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;
                strPrevious = CheckTextChanged(s, strPrevious);

                isEditing = false;
            }
        });

        //set para for price edit text
        txtBarcode = (EditText)v.findViewById(R.id.txtBarcode);
        txtBarcode.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT);
        txtBarcode.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));
        txtBarcode.setFilters(common.Utility.CreateMaxLengthFilter(common.text_and_length_settings.MAX_BARCODE_LENGTH));

        //modifier
        //((TextView)v.findViewById(R.id.lblModifier)).setTextSize(ADD_MODIFIER_TEXT);
        ((TextView)v.findViewById(R.id.lblModifier)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        //((TextView)v.findViewById(R.id.lblModifier)).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ((TextView)v.findViewById(R.id.lblModifier)).setGravity(Gravity.CENTER);

        //tabs
        final TextView tabModifier = ((TextView)v.findViewById(R.id.tabModifierGlobal));
        tabModifier.setTextSize(ADD_MODIFIER_TEXT);
        tabModifier.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));

        tabModifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blnIsIndividualTab) {//if currently is individual only, if clicking the same object will not have effect
                    TabClicked(view);
                }
            }
        });

        final TextView tabIndividual = ((TextView)v.findViewById(R.id.tabModifierIndividual));
        tabIndividual.setTextSize(ADD_MODIFIER_TEXT);
        tabIndividual.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));

        tabIndividual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!blnIsIndividualTab) {//if currently is global only, if clicking the same object will not have effect
                    TabClicked(view);
                }
            }
        });

        NotOnDisplayModifierList = GetOriginalGlobalModifier();


        if(myItemObject!=null)
        {

            txtItemName.setText(myItemObject.getName());
            chkDoNotTrack.setEnabled(false);
            chkDoNotTrack.setChecked(myItemObject.getDoNotTrackFlag());
            /*if(myItemObject.getPrice().doubleValue()<0)
            {
                tglBtn.setChecked(true);
                onToggleClicked(tglBtn);
            }*/
            txtPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(myItemObject.getPrice()).replace("-", ""));
            txtBarcode.setText(myItemObject.getBarcode()==0?"":myItemObject.getBarcode()+"");
            if(myItemObject.getPicturePath().length()>0)v.findViewById(R.id.tvRemovePic).setVisibility(View.VISIBLE);
            CreateModifierUI(GetOriginalIndividualModifier());
        }
        else
        {
            //new create item window hide delete button
            RelativeLayout.LayoutParams lpDelete = (RelativeLayout.LayoutParams)imgDelete.getLayoutParams();
            //move imgSave to imgDelete position
            RelativeLayout.LayoutParams lpSave = (RelativeLayout.LayoutParams)imgSave.getLayoutParams();
            lpSave.setMargins(lpDelete.leftMargin,lpDelete.topMargin,lpDelete.rightMargin,lpDelete.bottomMargin);

            //hide delete button
            imgDelete.setVisibility(View.GONE);
        }
        return v;
    }
    private void DeleteItemPicDialog(final ItemObject item)
    {
        if(imgItemPic.getTag()==null)return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Option");
        builder.setIcon(common.Utility.ReturnMessageBoxSizeIcon(R.drawable.question));
        builder.setMessage("Delete item picture?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FragmentView.findViewById(R.id.tvRemovePic).setVisibility(View.INVISIBLE);
                DeleteExistingItemPic((String) imgItemPic.getTag());
                imgItemPic.setBackground(getResources().getDrawable(R.drawable.photo_not_available));
                imgItemPic.setImageBitmap(null);
                imgItemPic.setTag(null);//reset
                if (imgInPgViewCtr != null) {
                    imgInPgViewCtr.setImageBitmap(null);
                    imgInPgViewCtr.setBackground(getResources().getDrawable(R.drawable.photo_not_available));
                }
                dialogInterface.dismiss();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });
        builder.show();

    }
   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (resultCode == getActivity().RESULT_OK)
        {
            String strUniquePicName = System.currentTimeMillis() + ".jpg";
            Bitmap bitmap=null;
            //delete the existing physical pic if any
            if(imgItemPic.getTag()!=null) {
                DeleteExistingItemPic((String) imgItemPic.getTag());
            }

            if (requestCode == tme.pos.BusinessLayer.Enum.ChoosePhotoFrom.gallery.value)
            {
                Uri selectedImageUri = data.getData();



                //compress file
                bitmap = DecodeBitmapFile(getPath(selectedImageUri));






            }
            else if(requestCode == Enum.ChoosePhotoFrom.camera.value)
            {

                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals(TemporaryCameraPictureFileName)) {
                        f = temp;
                        break;
                    }
                }



                bitmap = DecodeBitmapFile(f.getAbsolutePath());



            }

            if(bitmap!=null)
            {
                strSavedImageUri = SaveCompressedPic(strUniquePicName,bitmap);
                if(strSavedImageUri.length()<1){return;}

                imgItemPic.setImageBitmap(bitmap);
                imgItemPic.setBackground(null);
                imgItemPic.setTag(strSavedImageUri);
                if(imgInPgViewCtr!=null) {
                    imgInPgViewCtr.setImageBitmap(bitmap);
                    imgInPgViewCtr.setBackground(null);
                }

            }
        }

    }*/

   /* public ExpandableAddNewItemTextView CreateTapToAddButton()
    {
        final ExpandableAddNewItemTextView tvAddModifier = new ExpandableAddNewItemTextView(MainActivity);
        tvAddModifier.setTextSize(TypedValue.COMPLEX_UNIT_DIP,ADD_MENU_ITEM_TEXT);//ADD_MENU_ITEM_TITLE_TEXT_SIZE);
        tvAddModifier.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        tvAddModifier.setGravity(Gravity.CENTER);
        tvAddModifier.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
        tvAddModifier.setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
        tvAddModifier.setText("TAP TO ADD");
        ((TableRow)FragmentView.findViewById(R.id.trAddNewModifier)).addView(tvAddModifier);
        tvAddModifier.setOnTouchListener(new View.OnTouchListener() {
            boolean blnTap = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = MotionEventCompat.getActionMasked(motionEvent);
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        Log.d("add new", "action down");
                        blnTap = true;
                        tvAddModifier.setTextColor(getResources().getColor(R.color.light_green));


                        break;
                    case (MotionEvent.ACTION_MOVE):
                        Log.d("add new", "action move");
                        //blnTap = false;
                        break;
                    case (MotionEvent.ACTION_UP):
                        Log.d("add new", "action up");
                        tvAddModifier.setTextColor(getResources().getColor(R.color.add_new_category_item_text_grey));
                        if (blnTap) {
                            android.os.Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    CreateModifierRow(FragmentView.findViewById(R.id.tblModifier));
                                }
                            }, 150);
                        }
                        blnTap = false;
                        break;
                    case (MotionEvent.ACTION_CANCEL):
                        Log.d("add new", "action cancel");
                        tvAddModifier.setTextColor(getResources().getColor(R.color.add_new_category_item_text_grey));
                        blnTap = false;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        return tvAddModifier;
    }*/
    public String CheckTextChanged(Editable s,String strPrevious)
    {
        boolean isNegative=false;
        if(s.toString().indexOf("-")>-1)isNegative = true;
        if(!s.toString().equals(strPrevious)) {
            String str = s.toString().replaceAll("[$,.]", "");

            if (str.length() == 0) {
                str = "0";
            } else if (str.equals("-")) {
                str = "0";
            }
            double s1 = Double.parseDouble(str);

            String strFormatted = NumberFormat.getCurrencyInstance().format((s1 / 100));
            double s2= ((s1 / 100.0));
            if(Math.abs(s2)>1000000)
            {
                ShowMessage("Price","Please keep the value under <b><i>million</i></b>.",R.drawable.no_access);
                strFormatted = NumberFormat.getCurrencyInstance().format((0 / 100));
            }

            if(isNegative && s1>0){strFormatted="-"+strFormatted;}
            s.replace(0, s.length(), strFormatted);

            strPrevious = strFormatted;
        }
        return strPrevious;
    }
    private void CreateModifierRow(final View ParentLayout)
    {

            //table parent
            final TableLayout tl = (TableLayout) ParentLayout.findViewById(R.id.tblModifier);

            if (tl.getChildCount() >= (MAX_MODIFIER_ITEM + 1)) {
                ShowMessage("Add Modifier","You have reached maximum of <b><i>"+MAX_MODIFIER_ITEM+"</i></b> modifiers.",R.drawable.no_access);

               return;
            }


            tl.addView(CreateModifierRowComponents(tl.getChildCount(), "", false, "", 0), tl.getChildCount() - 1);
           // tl.addView(tr, tl.getChildCount() - 1);


            TableRow trAddModifier = (TableRow)ParentLayout.findViewById(R.id.trAddNewModifier);

            ((TableRow.LayoutParams)trAddModifier.getChildAt(0).getLayoutParams()).span=6;
    }



    private void ReIndexModifier()
    {
        TableRow trAddModifier = (TableRow)FragmentView.findViewById(R.id.trAddNewModifier);
        TableLayout tl = (TableLayout)trAddModifier.getParent();
        int childCount = ((TableLayout)trAddModifier.getParent()).getChildCount();
        if(childCount<(MAX_MODIFIER_ITEM+1))
        {

            //ParentLayout.findViewById(R.id.tvAddNewModifier).setVisibility(View.VISIBLE);
            trAddModifier.getChildAt(0).setVisibility(View.VISIBLE);
        }
        if(childCount==1)
        {
            TableLayout tbl = (TableLayout)getActivity().findViewById(R.id.tblModifier);


            ((TableRow.LayoutParams)((TableRow)tbl.getChildAt(0)).getChildAt(0).getLayoutParams()).span=1;
            View v =((TableRow)tbl.getChildAt(0)).getChildAt(0);//
            ((TableRow)tbl.getChildAt(0)).removeView(v);
            ((TableRow)tbl.getChildAt(0)).addView(v);


        }
        else
        {
            //re-indexing
            for(int i=0;i<childCount-1;i++)
            {
                TableRow tr = (TableRow)tl.getChildAt(i);
                if(tr.getChildAt(0) instanceof TextView)
                {
                    ((TextView)tr.getChildAt(0)).setText(i+1+". " + MODIFIER_ITEM_NAME_LABEL);

                }
            }
        }
    }
    private void PromptSaveQuestion()
    {
        //ask if user intend to create/save global modifier only
        //else prompt for item name if creating a new item
        final AlertDialog.Builder messageBox = new AlertDialog.Builder(getActivity());
        messageBox.setTitle("Save");
        messageBox.setMessage(Html.fromHtml("Save global modifier only?"));
        messageBox.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.question),getResources(),36,36));
        messageBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SaveGlobalModifier(true);
                btnCancelClick(null);//dismiss the item after that
            }
        });
        messageBox.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                common.Utility.ShowMessage("Add Item", "Please provide a name for this new item.",getActivity(),R.drawable.exclaimation);
                txtItemName.requestFocus();
                return;
            }
        });

        messageBox.show();
    }

    public void btnSaveClick(View v)
    {
        //EditText txtName =(EditText) v.findViewById(R.id.txtItemName);
        if(txtItemName.getText().length()==0)
        {
            PromptSaveQuestion();
            blnSaving=false;
            return;
        }


        //check if the barcode has existed
        //EditText txtBarcode = (EditText)v.findViewById(R.id.txtBarcode);
        String strBarcode = (txtBarcode.getText()+"").trim();
        if(strBarcode.length()>0 &&
                ((myItemObject!=null && MainActivity.IsBarCodeExists(strBarcode,myItemObject.getID()))//check it is oneself barcode
                       || (myItemObject==null && MainActivity.SearchItemByBarcode(strBarcode,null)))//check the barcode belongs to other
                )
        {
            common.Utility.ShowMessage("Save","Barcode already existed",getActivity(),R.drawable.no_access);
            blnSaving=false;
            txtBarcode.requestFocus();
            return;
        }
        if(strBarcode.length()>0 && Long.parseLong(strBarcode)<1)
        {
            common.Utility.ShowMessage("Save","Barcode needs to be greater than zero",getActivity(),R.drawable.no_access);
            blnSaving=false;
            txtBarcode.requestFocus();
            return;
        }
        Long lngBarcode = strBarcode.length()>0?Long.parseLong(strBarcode):0l;
        //separate between individual and global modifier
        //always save/create global modifier with this method, doesn't need item id
        //SaveGlobalModifier();



        //EditText txtName =(EditText) v.findViewById(R.id.txtItemName);
        //EditText txtPrice = (EditText)v.findViewById(R.id.txtItemPrice);
        //ToggleButton tgSign = (ToggleButton)v.findViewById(R.id.toggleValuePrefix);

        String strPrice = txtPrice.getText()+"";
        if(strPrice.length()==0)strPrice="0.00";
        BigDecimal bdPrice = new BigDecimal(common.Utility.ConvertCurrencyFormatToBigDecimalString(strPrice));//.replace("$","").replace(",",""));
       /* if(tgSign.isChecked())//is on then negative
        {
            bdPrice = bdPrice.negate();
        }*/

        //save new
        if(myItemObject==null) {
            SaveGlobalModifier(true);
            CheckBox chkDoNotTrack =(CheckBox)v.findViewById(R.id.chkDoNotTrack);
            ItemObject io = new ItemObject(-1,txtItemName.getText() + "",
                    CategoryId,
                    bdPrice.toPlainString(),
                    (imgItemPic.getTag()!=null)?(String)imgItemPic.getTag():""
            ,chkDoNotTrack.isChecked()
                    ,lngBarcode,1);
            MainActivity.SaveNewItemAndModifiers(io,ConstructNewIndividualModifierList());
            //MainActivity.SaveNewItemModifiers(txtName.getText() + "", bdPrice,(imgItemPic.getTag()!=null)?(String)imgItemPic.getTag():"", CategoryId,ConstructNewIndividualModifierList());
        }
        else
        {
            //update
            ArrayList<ModifierObject>deleteList =new ArrayList<ModifierObject>();
            ArrayList<ModifierObject>updateList =new ArrayList<ModifierObject>();

            ConstructModifierLists(deleteList,updateList,true,myItemObject.getID());

            String strPhotoPath = (imgItemPic.getTag() != null) ? (String) imgItemPic.getTag():"";

            SaveGlobalModifier((deleteList.size()>0 || updateList.size()>0)?false:true);//allow individual modifier to trigger reload

            if(myItemObject.getPrice().doubleValue()==bdPrice.doubleValue()
                    && myItemObject.getName().trim().equals(txtItemName.getText().toString().trim())
                    && myItemObject.getPicturePath().equalsIgnoreCase(strPhotoPath)
                    && myItemObject.getBarcode()==lngBarcode)
            {

                //ShowMessage("update",txtName.getText()+"");
                //update the modifiers only
                SaveIndividualModifier(null,myItemObject.getID(),myItemObject.getParentID(),deleteList,updateList);//"",bdPrice,myItemObject.getParentID(), myItemObject.getID(),myItemObject.getPicturePath());
            }
            else
            {

                //update item and modifiers
                myItemObject.setPrice(bdPrice);
                myItemObject.setName(txtItemName.getText().toString().trim());
                myItemObject.setPicturePath(strPhotoPath);
                myItemObject.setBarcode(lngBarcode);
                SaveIndividualModifier(myItemObject, myItemObject.getID(), myItemObject.getParentID(), deleteList, updateList);//txtName.getText().toString(),bdPrice,myItemObject.getParentID(), myItemObject.getID(),myItemObject.getPicturePath());
            }
        }
    }
    private void SaveIndividualModifier(ItemObject io,long lngItemId,long lngCategoryId
                                        ,ArrayList<ModifierObject>deleteList, ArrayList<ModifierObject>updateList)//String strName,BigDecimal Price, long lngParentId,long lngId,String strImageFilePath)
    {
        //get individual modifier list
        //ArrayList<ModifierObject>deleteList =new ArrayList<ModifierObject>();
        //ArrayList<ModifierObject>updateList =new ArrayList<ModifierObject>();

        //ConstructModifierLists(deleteList,updateList,true,lngItemId);



        //io might be not available if only updating modifier objects
       MainActivity.UpdateItemAndModifiers(io,lngItemId,lngCategoryId, deleteList,updateList);
    }
    private void ConstructModifierLists(ArrayList<ModifierObject>deleteList,ArrayList<ModifierObject>updatedList,boolean blnCollectIndividual,long lngItemId)
    {
        boolean blnFound = false;
        ArrayList<ModifierObject>needToCheckList;
        ArrayList<ModifierObject>localDeleteList;
        if(blnCollectIndividual)
        {
            localDeleteList = GetOriginalIndividualModifier();
            if(blnIsIndividualTab)
            {
                needToCheckList = ConstructCurrentDisplayingModifiers(lngItemId);
            }
            else
            {
                needToCheckList = NotOnDisplayModifierList;
            }
        }else
        {
            localDeleteList = GetOriginalGlobalModifier();
            if(!blnIsIndividualTab)
            {
                needToCheckList = ConstructCurrentDisplayingModifiers(GLOBAL_MODIFIER_PARENT_ID);
            }
            else
            {
                needToCheckList = NotOnDisplayModifierList;
            }
        }

        for(int i=needToCheckList.size()-1;i>-1;i--)
        {
            blnFound = false;
            ModifierObject moUpdate = needToCheckList.get(i);
            for(int j=localDeleteList.size()-1;j>-1;j--)
            {
                ModifierObject moOriginal = localDeleteList.get(j);

                //same as original, exclude from list
                if(moOriginal.getParentID()==moUpdate.getParentID() &&
                        moOriginal.getPrice().doubleValue()==moUpdate.getPrice().doubleValue() &&
                        moOriginal.getName().trim().equalsIgnoreCase(moUpdate.getName().trim()) &&
                        moOriginal.getID()==moUpdate.getID() &&
                        moOriginal.getMutualGroup()==moUpdate.getMutualGroup() )
                {
                    //remove it from both list
                    blnFound = true;


                }
                else if(moOriginal.getParentID()==moUpdate.getParentID() && moOriginal.getID()==moUpdate.getID())
                {
                    //insert to update list to perform update later
                    updatedList.add(moUpdate);
                    blnFound = true;

                }


                if(blnFound){
                    //remove it from both list if found, else will be marked as deleted
                    needToCheckList.remove(i);
                    localDeleteList.remove(j);
                    break;
                }

            }

            //add new if not found yet after checking with original list
            if(!blnFound)updatedList.add(moUpdate);

        }
        for(int i=0;i<localDeleteList.size();i++)
        deleteList.add(localDeleteList.get(i));

    }
    private void SaveGlobalModifier(boolean blnReloadReceipt)
    {


        ArrayList<ModifierObject>deleteGlobalModifiers = new ArrayList<ModifierObject>();
        ArrayList<ModifierObject>updateModifierList = new ArrayList<ModifierObject>();

        ConstructModifierLists(deleteGlobalModifiers,updateModifierList,false,GLOBAL_MODIFIER_PARENT_ID);

        //update for any changes and log it into activity table
        MainActivity.SaveGlobalModifiers(deleteGlobalModifiers,updateModifierList,blnReloadReceipt);


    }
    private ArrayList<ModifierObject>GetOriginalModifier(boolean blnGetIndividual)
    {
        ArrayList<ModifierObject>list1 = new ArrayList<ModifierObject>();
        for(int i=0;i<OriginalModifiers.size();i++)
        {
            if(blnGetIndividual) {
                if (OriginalModifiers.get(i).getParentID() != GLOBAL_MODIFIER_PARENT_ID) {
                    ModifierObject mo = OriginalModifiers.get(i);
                    list1.add(
                            new ModifierObject(mo.getID(),
                                    mo.getName(),
                                    mo.getParentID(),
                                    mo.getPrice().toPlainString(),
                                    mo.getMutualGroup(),
                                    mo.getIsActive(),
                                    mo.GetCurrentVersionNumber()
                            )
                    );
                }
            }
            else
            {
                if (OriginalModifiers.get(i).getParentID() == GLOBAL_MODIFIER_PARENT_ID) {
                    ModifierObject mo = OriginalModifiers.get(i);
                    list1.add(
                            new ModifierObject(mo.getID(),
                                    mo.getName(),
                                    mo.getParentID(),
                                    mo.getPrice().toPlainString(),
                                    mo.getMutualGroup(),
                                    mo.getIsActive(),
                                    mo.GetCurrentVersionNumber()
                            )
                    );
                }
            }
        }
        return list1;
    }
    private ArrayList<ModifierObject>GetOriginalIndividualModifier()
    {
       return GetOriginalModifier(true);
    }
    private ArrayList<ModifierObject>GetOriginalGlobalModifier()
    {
        return GetOriginalModifier(false);

    }
    private ModifierFlippableTableRow CreateModifierRowComponents(int rowIndex,String strModifierName,
           boolean blnNegate, String strPrice,int intGroup)
    {
        ModifierFlippableTableRow tr = new ModifierFlippableTableRow(getActivity(),this);


        TableRow.LayoutParams trParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        trParams.setMargins(10,0,0,0);

        trParams.column = 0;

        TextView tvName = new TextView(getActivity());
        tvName.setText(rowIndex  + ". " + MODIFIER_ITEM_NAME_LABEL);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT);
        tvName.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));
        tvName.setLayoutParams(trParams);

        //edit text for modifier name
        trParams = new TableRow.LayoutParams();
        trParams.column = 1;
        EditText txtName = new EditText(getActivity());
        txtName.setSingleLine(true);
        if(strModifierName.length()>0)txtName.setText(strModifierName);
        txtName.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));
        txtName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT);
        txtName.setHint(HINT_MODIFIER_NAME.replace("%1$d", MODIFIER_NAME_MAX_CHAR + ""));
        txtName.setFilters(CreateMaxLengthFilter(MODIFIER_NAME_MAX_CHAR));
        txtName.setLayoutParams(trParams);
        txtName.requestFocus();

        //price label
        trParams = new TableRow.LayoutParams();
        trParams.leftMargin = 20;
        trParams.column = 2;
        TextView tvPrice = new TextView(getActivity());
        tvPrice.setText(MODIFIER_ITEM_PRICE_LABEL);
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT);
        tvPrice.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));
        tvPrice.setLayoutParams(trParams);

        //toggle button for price value
        trParams = new TableRow.LayoutParams();
        trParams.column = 3;

        //order does matter
       /* ToggleButton tb = new ToggleButton(getActivity());
        tb.setTextOff("+");
        tb.setTextOn("-");
        tb.setTextColor((blnNegate) ? getResources().getColor(R.color.red):getResources().getColor(R.color.green));
        tb.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT + 15);
        tb.setLayoutParams(trParams);
        tb.setChecked(false);
        tb.setBackground(getResources().getDrawable(R.drawable.abc_ab_transparent_light_holo));
        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onToggleClicked(view);
            }
        });
        tb.setChecked(blnNegate);*/

        //edit text for modifier price
        trParams = new TableRow.LayoutParams();
        trParams.column = 4;
        EditText txtModifierPrice = new EditText(getActivity());
        if(strPrice.length()>0)txtModifierPrice.setText("$"+strPrice);
        txtModifierPrice.setHint(HINT_MODIFIER_PRICE);
        txtModifierPrice.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        txtModifierPrice.setKeyListener(DigitsKeyListener.getInstance("0123456789.,$"));
        txtModifierPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT);
        txtModifierPrice.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), getResources().getString(R.string.app_font_family)));
        txtModifierPrice.setLayoutParams(trParams);
        txtModifierPrice.addTextChangedListener(new TextWatcher() {
            boolean isEditing;
            String strPrevious = "";

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;
                strPrevious = CheckTextChanged(s, strPrevious);

                isEditing = false;
            }
        });

        //mutual color
        trParams = new TableRow.LayoutParams();
        trParams.column = 5;
        trParams.leftMargin = 20;
        tr.setGravity(Gravity.BOTTOM);
        Spinner spinnerColor = new Spinner(getActivity());
        spinnerColor.setGravity(Gravity.BOTTOM);


        ArrayList<String> aryColors = new ArrayList<String>();
        aryColors.add("0");
        aryColors.add("1");
        aryColors.add("2");
        aryColors.add("3");
        aryColors.add("4");
        aryColors.add("5");
        ModifierMutualColorSpinnerAdapter adapter = new ModifierMutualColorSpinnerAdapter(getActivity(), R.layout.layout_modifier_mutual_color_ui, aryColors);
        spinnerColor.setAdapter(adapter);
        spinnerColor.setLayoutParams(trParams);
        spinnerColor.setSelection(intGroup);

        tr.addView(tvName,0);
        tr.addView(txtName);
        tr.addView(tvPrice);
        //tr.addView(tb);
        tr.addView(txtModifierPrice);
        tr.addView(spinnerColor);

        return tr;
    }
    private ArrayList<ModifierObject> ConstructNewIndividualModifierList()
    {

        //get the individual list only
        ArrayList<ModifierObject>list1 = (blnIsIndividualTab)?ConstructCurrentDisplayingModifiers(NEW_MODIFIER_PARENT_ID): NotOnDisplayModifierList;

        return list1;


    }
    public void btnCancelClick(View v)
    {
        MainActivity.DismissAddNewItemFragment();
    }
   /* public void onToggleClicked(View view) {
        // Is the toggle on?
        ToggleButton btn = (ToggleButton)view;

        if (!btn.isChecked()) {
           btn.setTextColor(getActivity().getResources().getColor(R.color.green));
        } else {
            btn.setTextColor(Color.RED);
        }
    }*/
    public void TabClicked(View v)
    {
        //save current displaying item with this parent id
        long LocalParentId;
        if(v.getId()==R.id.tabModifierGlobal)
        {
            blnIsIndividualTab = false;
            //reset flag
            TextView tabIndividual = (TextView)getActivity().findViewById(R.id.tabModifierIndividual);
            tabIndividual.setBackground(null);


            //no item id yet if is new
            if(myItemObject!=null)
            {
                LocalParentId =this.myItemObject.getID();
            }
            else
            {
                LocalParentId=NEW_MODIFIER_PARENT_ID;//assign some other negative value other than global(-1)
            }



        }
        else
        {
            //reset flag
            TextView tabGlobal = (TextView)getActivity().findViewById(R.id.tabModifierGlobal);
            tabGlobal.setBackground(null);


            blnIsIndividualTab = true;

            //getActivity().findViewById(R.id.tabModifierGlobal).setBackground(null);
            LocalParentId = GLOBAL_MODIFIER_PARENT_ID;

        }

        ArrayList<ModifierObject> tempList = ConstructCurrentDisplayingModifiers(LocalParentId);
        DisplayLastSaveModifiers(tempList);


        //v.setBackground(getActivity().getResources().getDrawable(R.drawable.abc_ab_transparent_light_holo));
        v.setBackground(getActivity().getResources().getDrawable(R.color.half_transparent_dark_grey));
    }
    private ArrayList<ModifierObject> SortModifierList(ArrayList<ModifierObject>list)
    {
        HashMap<Integer,ArrayList<ModifierObject>> sortedModifier = new HashMap<Integer, ArrayList<ModifierObject>>();
        for(int i=0;i< list.size();i++)
        {
            if(!sortedModifier.containsKey(list.get(i).getMutualGroup()))
            {
                sortedModifier.put(list.get(i).getMutualGroup(),new ArrayList<ModifierObject>());
            }
            ArrayList<ModifierObject>tempList = sortedModifier.get(list.get(i).getMutualGroup());
            tempList.add(list.get(i));
            sortedModifier.put(list.get(i).getMutualGroup(),tempList);
        }

        list.clear();
        //sort by alphabetic order within group
        for(int i:sortedModifier.keySet())
        {
            ArrayList<ModifierObject>tempList = sortedModifier.get(i);
            SortModifierArrayList(tempList);

            for(int j=0;j<tempList.size();j++)list.add(tempList.get(j));
        }
        return list;
    }
    private void SortModifierArrayList(ArrayList<ModifierObject>list)
    {
        for(int i=0;i<list.size();i++)
        {
            for(int j=i+1;j<list.size();j++)
            {
                if(list.get(i).getName().compareTo(list.get(j).getName())>=0)
                {
                    ModifierObject mo =list.remove(i);
                    list.add(j,mo);
                }
            }
        }

    }
    private void CreateModifierUI(ArrayList<ModifierObject>list)
    {
        final TableLayout tbl = (TableLayout) FragmentView.findViewById(R.id.tblModifier);


        //tbl.removeViewsInLayout(0, tbl.getChildCount() - 1);//tablelayout still retain, can see gap
        tbl.removeViews(0, tbl.getChildCount() - 1);//will remove all layout and leaving the last view couldn't be displayed

        //sort the list before displaying, order by color then name
        SortModifierList(list);

        for(int i=0;i<list.size();i++)
        {
            ModifierObject mo = list.get(i);

            ModifierFlippableTableRow tr = CreateModifierRowComponents(tbl.getChildCount(), mo.getName(),
                    (mo.getPrice().compareTo(BigDecimal.ZERO) >=0) ? false : true, mo.getPrice().toPlainString().replace("-", ""), mo.getMutualGroup());
            tr.setTag(mo);
            tbl.addView(tr,tbl.getChildCount() - 1);
            //tbl.getChildAt(tbl.getChildCount()-1).setTag(mo);
        }


        if(tbl.getChildCount()==1)
        {
            ((TableRow.LayoutParams)((TableRow)tbl.getChildAt(0)).getChildAt(0).getLayoutParams()).span=1;


            View v =((TableRow)tbl.getChildAt(0)).getChildAt(0);//
            ((TableRow)tbl.getChildAt(0)).removeView(v);
            ((TableRow)tbl.getChildAt(0)).addView(v);// .getChildAt(0).setLeft(0);

        }
        else
        {
            ((TableRow.LayoutParams)((TableRow)tbl.getChildAt(tbl.getChildCount()-1)).getChildAt(0).getLayoutParams()).span=6;
        }
        Animation slide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_fast);
        tbl.setAnimation(slide);

    }
    private void DisplayLastSaveModifiers(ArrayList<ModifierObject> tempList)
    {
        if(NotOnDisplayModifierList ==null) NotOnDisplayModifierList = new ArrayList<ModifierObject>();

        CreateModifierUI(NotOnDisplayModifierList);


        NotOnDisplayModifierList = tempList;



    }
    private ArrayList<ModifierObject> ConstructCurrentDisplayingModifiers(long localParentId)
    {


        //table parent
        TableLayout tl = (TableLayout) FragmentView.findViewById(R.id.tblModifier);
        //store the current list before clearing it
        ArrayList<ModifierObject> tempList = new ArrayList<ModifierObject>();
        //new modifier default id
        long lngTempModifierId=-1;
        long lngModifierID;
        ModifierObject mo;
        for(int i = 0;i<tl.getChildCount()-1;i++)
        {

            TableRow tr = (TableRow)tl.getChildAt(i);
            mo=(tr.getTag()!=null)?(ModifierObject) tr.getTag():null;


            //skip this row if no name has been given
            if(((EditText)tr.getChildAt(1)).getText().length()==0)continue;

            String strPrice = ((EditText)tr.getChildAt(3)).getText()+"";
            if(strPrice.length()==0)strPrice="0.00";
            BigDecimal bdPrice = new BigDecimal(common.Utility.ConvertCurrencyFormatToBigDecimalString(strPrice));//.replace("$","").replace(",",""));
          /*  ToggleButton tglBtn = (ToggleButton)tr.getChildAt(3);
            if(tglBtn.isChecked())
            {
                bdPrice =bdPrice.negate();
            }*/
            //new row
            //even though user delete the existing modifier and re-add it back with same name and price also considered new modifier
            String strNewName =((EditText)tr.getChildAt(1)).getText()+"";
            if(mo==null)
            {
                lngModifierID = lngTempModifierId;
                //ShowMessage("construct","mo doesn't existed in tag!!");
               /* mo = new ModifierObject(lngTempModifierId,
                        strNewName,
                        localParentId,
                        bdPrice.toPlainString(),
                        ((Spinner)tr.getChildAt(5)).getSelectedItemPosition(),
                        1
                );*/
            }
            else
            {
                lngModifierID = mo.getID();

            }
            mo = new ModifierObject(lngModifierID,
                    strNewName,
                    localParentId,
                    bdPrice.toPlainString(),
                    ((Spinner)tr.getChildAt(4)).getSelectedItemPosition(),
                    1,
                    1
            );
            //ShowMessage("construct","mo id from tag:"+mo.getID());
            tempList.add(mo);
        }

        return tempList;
    }
    private InputFilter[] CreateMaxLengthFilter(int length)
    {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(length);
        return filters;
    }
    public void ShowMessage(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getActivity());
        messageBox.setTitle(strTitle);
        messageBox.setMessage(Html.fromHtml(strMsg));
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(iconId),getResources(),36,36));
        }
        messageBox.show();
    }


    public void RemoveAllModifierRows()
    {
        TableLayout tbl = (TableLayout)getActivity().findViewById(R.id.tblModifier);
        //tbl.removeViewsInLayout(0,tbl.getChildCount()-1);
        int count = tbl.getChildCount()-1;
        tbl.removeViews(0,tbl.getChildCount()-1);

        ((TableRow.LayoutParams)((TableRow)tbl.getChildAt(0)).getChildAt(0).getLayoutParams()).span=1;
        View v =((TableRow)tbl.getChildAt(0)).getChildAt(0);//
        ((TableRow)tbl.getChildAt(0)).removeView(v);
        ((TableRow)tbl.getChildAt(0)).addView(v);


    }
    public void RemoveModifierRow(TableRow tr)
    {
        ((TableLayout)tr.getParent()).removeView(tr);
        ReIndexModifier();
    }

    @Override
    public void Tapped() {
        CreateModifierRow(FragmentView.findViewById(R.id.tblModifier));
    }
}
