package tme.pos;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import tme.pos.BusinessLayer.Duple;
import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.ModifierObject;
import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 5/22/2016.
 */
public class ModifierOptionDialog extends Dialog {
    public interface IModifierChangedListener
    {
        void UpdateModifiers(ArrayList<ModifierObject>modifiers);
    }
    boolean blnIndividualModifierTabClicked;
    TextView tabModifierIndividual;
    TextView tabModifierGlobal;
    LinearLayout prtLayout;
    ArrayList<Spinner> IndividualSpinners;
    ArrayList<Spinner> GlobalSpinners;
    ArrayList<ModifierObject>WhiteGroupAvailableIndividualModifiers;
    ArrayList<ModifierObject>WhiteGroupAvailableGlobalModifiers;
    int spinnerAutoTriggerOnItemSelectedCounter=0;//0 allow method to trigger else we are updating list manually(will trigger it)
    ModifierObject SpinnerDefaultModifierObject = new ModifierObject(-1,"Select an option...",-2,"0",0,1,1);
    int MUTUAL_GROUP_COUNT=6;
    int spinnerCounter=0;
    ArrayList<ModifierObject>selected_modifiers;
    HashMap<Long,ArrayList<ModifierObject>>all_modifiers;
    IModifierChangedListener listener;
    Activity activity;
    long globalModifierId=-1;

    @Override
    public void dismiss() {
        common.Utility.LogActivity("dismiss modifier option dialog");
        super.dismiss();
    }


    public ModifierOptionDialog(Context context, ArrayList<ModifierObject> selected_modifiers,
                                HashMap<Long,ArrayList<ModifierObject>>allModifiers, IModifierChangedListener l)
    {
        super(context);
        this.selected_modifiers = selected_modifiers;
        this.all_modifiers = allModifiers;
        this.listener = l;
        activity  = common.Utility.FindActivity(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_modifier_option_ui_popup_window);


        IndividualSpinners = new ArrayList<Spinner>();
        GlobalSpinners = new ArrayList<Spinner>();
        MUTUAL_GROUP_COUNT =Integer.parseInt(getContext().getResources().getString(R.string.modifier_group_page_count));
        //individual tab
        tabModifierIndividual = (TextView)findViewById(R.id.tabModifierIndividual);
        tabModifierIndividual.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.ADD_MENU_ITEM_MODIFIER_TEXT_SIZE);
        tabModifierIndividual.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        tabModifierIndividual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabClicked(view);
            }
        });
        //global tab
        tabModifierGlobal = (TextView)findViewById(R.id.tabModifierGlobal);
        tabModifierGlobal.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.ADD_MENU_ITEM_MODIFIER_TEXT_SIZE);
        tabModifierGlobal.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        tabModifierGlobal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabClicked(view);
            }
        });


        prtLayout = (LinearLayout)findViewById(R.id.ModifierPage);

        findViewById(R.id.imgCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        findViewById(R.id.imgOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                common.Utility.LogActivity("user click ok on selected modifiers");
                if(listener!=null)listener.UpdateModifiers(ConstructSelectedModifierList());
                dismiss();
            }
        });
        tabModifierIndividual.callOnClick();

    }
    private ArrayList<ModifierObject> ConstructSelectedModifierList()
    {
        ArrayList<ModifierObject>lst = ConstructSelectedModifierList(GlobalSpinners);
        lst.addAll(ConstructSelectedModifierList(IndividualSpinners));
        return lst;

    }
    private ArrayList<ModifierObject> ConstructSelectedModifierList(ArrayList<Spinner>spinners)
    {
        ArrayList<ModifierObject>lst = new ArrayList<ModifierObject>();
        for(Spinner s:spinners)
        {
            if(s.getSelectedItemPosition()==0)continue; //default, no item selected

            lst.add((ModifierObject)s.getSelectedView().getTag());
        }
        return lst;
    }
    private ArrayList<ModifierObject>ReturnAvailableGlobalWhiteGroupModifierSelection()
    {
        return ReturnAvailableWhiteGroupModifierSelection(WhiteGroupAvailableGlobalModifiers,GlobalSpinners);
    }
    private ArrayList<ModifierObject>ReturnAvailableIndividualWhiteGroupModifierSelection()
    {
        return ReturnAvailableWhiteGroupModifierSelection(WhiteGroupAvailableIndividualModifiers,IndividualSpinners);
    }
    private ArrayList<ModifierObject>ReturnAvailableWhiteGroupModifierSelection(ArrayList<ModifierObject>CompleteModifierList,ArrayList<Spinner> spinners)
    {
        ArrayList<ModifierObject> AvailableList = new ArrayList<ModifierObject>(CompleteModifierList);
        for(Spinner s:spinners)
        {
            if(s.getSelectedItemPosition()==0)continue; //default, no item selected

            ModifierObject mo=(ModifierObject)s.getSelectedView().getTag();
            for(int j=AvailableList.size()-1;j>=0;j--)
            {



                if(mo.getID()==AvailableList.get(j).getID())
                {
                    AvailableList.remove(j);
                    break;
                }
            }
        }
        return AvailableList;
    }
    private void UpdateExistingWhiteGroupSpinners()
    {
        //assigning available modifier options + currently selected modifier to each spinner UI on screen
        //so that the user can see the available options when they click on it
        ArrayList<ModifierObject>AvailableList;
        ArrayList<Spinner> Spinners = IndividualSpinners;
        if(blnIndividualModifierTabClicked)
        {
            AvailableList = ReturnAvailableIndividualWhiteGroupModifierSelection();
        }
        else
        {
            AvailableList = ReturnAvailableGlobalWhiteGroupModifierSelection();
            Spinners = GlobalSpinners;
        }
        for(Spinner s:Spinners)
        {
            //only interested in white group spinner
            if(((ModifierObject)s.getSelectedView().getTag()).getMutualGroup()!= Enum.MutualGroupColor.white.group)continue;

            spinnerAutoTriggerOnItemSelectedCounter++;

            int index =s.getSelectedItemPosition();
            ModifierObject moSelected = (ModifierObject)s.getSelectedView().getTag();

            //construct a temp list with current selected modifier object in it
            ArrayList<ModifierObject>list = PermutativeInsertWhiteGroupModifierIntoAvailableList(new ArrayList<ModifierObject>(AvailableList), moSelected);
            list.add(0,SpinnerDefaultModifierObject);

            MyModifierArrayAdapter sAdapter = new MyModifierArrayAdapter(activity,R.layout.layout_modifier_spinner_ui,
                    new ArrayList<ModifierObject>(list));
            sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s.setAdapter(sAdapter);



            //reselect the selected modifier object after list update
            if(index==0)continue;

            for(int i=1;i<((MyModifierArrayAdapter)s.getAdapter()).myList.size();i++)
            {
                if(moSelected.getID()==((ModifierObject)((MyModifierArrayAdapter)s.getAdapter()).myList.get(i)).getID())
                {

                    s.setSelection(i);
                    s.setTag(i);

                    break;
                }
            }
        }
    }
    private ArrayList<ModifierObject> PermutativeInsertWhiteGroupModifierIntoAvailableList(ArrayList<ModifierObject>list, ModifierObject mo)
    {
        if(mo.getID()==globalModifierId)return list;//do nothing if the user selected option label, just return the list immediately

        ArrayList<ModifierObject>InitialModifiers = WhiteGroupAvailableIndividualModifiers;
        InitialModifiers = (blnIndividualModifierTabClicked)?InitialModifiers:WhiteGroupAvailableGlobalModifiers;

        //get the index for this new modifier obj in the initial list
        int selectedModifierIndex =-1;
        int nextAvailableModifierIndex =-1;
        for(int i=0;i<InitialModifiers.size();i++)
        {
            if(InitialModifiers.get(i).getID()==mo.getID())
            {
                selectedModifierIndex = i;
                break;
            }
        }

        //and get the index of the 1st element in available list
        if(list.size()>0)
        {
            for(int j=0;j<InitialModifiers.size();j++)
            {
                if(InitialModifiers.get(j).getID()==list.get(0).getID())
                {
                    nextAvailableModifierIndex = j;
                    break;
                }
            }

        }

        //add directly into the list if is 1st or last element in the original list
        if(selectedModifierIndex==0 || selectedModifierIndex<nextAvailableModifierIndex)
        {
            list.add(0, mo);
        }
        else if(selectedModifierIndex==InitialModifiers.size()-1)
        {
            list.add(mo);
        }
        else if(list.size()==0)
        {
            list.add(mo);
        }
        else
        {
            //insert in between
            //check all the possible element before the target index in the initial list, see we can find a match
            //in the available list
            int PreviousElementIndex=-1;
            for(int i=selectedModifierIndex-1;i>=0;i--)
            {
                for(int j=0;j< list.size();j++)
                {
                    if(list.get(j).getID()==InitialModifiers.get(i).getID())
                    {
                        PreviousElementIndex=j;
                        list.add(PreviousElementIndex+1,mo);
                        break;
                    }
                }

                if(PreviousElementIndex>-1)break;
            }
        }

        return list;
    }
    private void RemoveSelectedWhiteGroupSpinnerFromUI(AdapterView av)
    {
        ArrayList<Spinner>spinners = IndividualSpinners;
        if(!blnIndividualModifierTabClicked) {
            spinners = GlobalSpinners;
        }


        if(spinners.size()>1)
        {
            //remove a spinner object from UI
            //remove a spinner object from UI
            ((LinearLayout)av.getParent()).removeView(av);

            //remove from the array list
            for(int i=0;i<spinners.size();i++)
            {
                if(spinners.get(i).getId()==av.getId()) {
                    spinners.remove(i);
                    break;
                }
            }
        }
        UpdateExistingWhiteGroupSpinners();


    }
    private void AddNewWhiteGroupSpinner(LinearLayout prtLayout,int InsertPosition)
    {
        ArrayList<ModifierObject>AvailableModifier =ReturnAvailableIndividualWhiteGroupModifierSelection();// new ArrayList<ModifierObject>(WhiteGroupAvailableIndividualModifiers);
        AvailableModifier =(!blnIndividualModifierTabClicked)?ReturnAvailableGlobalWhiteGroupModifierSelection():AvailableModifier;



        //update each spinner available options + currently selected when user open the drop down
        UpdateExistingWhiteGroupSpinners();

        if(AvailableModifier.size()==0)return;//no more option to be selected
        //or there is still un-selected white group spinner
        for(int i=0;i<prtLayout.getChildCount();i++)
        {
            Spinner s = (Spinner)prtLayout.getChildAt(i);
            if(s.getSelectedItemPosition()==0 && ((ModifierObject)s.getAdapter().getItem(s.getSelectedItemPosition())).getMutualGroup()==0)return;

        }

        //add a new spinner obj into the UI
        Spinner s = new Spinner(getContext());
        s.setId(spinnerCounter++);
        spinnerAutoTriggerOnItemSelectedCounter++;

        s.setTag(0);
        ArrayList<ModifierObject> spinnerModifierList=new ArrayList<ModifierObject>();
        spinnerModifierList.add(SpinnerDefaultModifierObject);
        spinnerModifierList.addAll(AvailableModifier);
        MyModifierArrayAdapter sAdapter = new MyModifierArrayAdapter(activity,R.layout.layout_modifier_spinner_ui,
                new ArrayList<ModifierObject>(spinnerModifierList));
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(sAdapter);
        s.setSelection(0);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                SpinnerOnItemSelected(adapterView, view, i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        prtLayout.addView(s,InsertPosition);
        if(blnIndividualModifierTabClicked)
        {
            IndividualSpinners.add(InsertPosition,s);
        }
        else
        {
            GlobalSpinners.add(InsertPosition,s);
        }



    }
    protected void SpinnerOnItemSelected(AdapterView av,View v,int position)
    {

        LinearLayout prtLayout = (LinearLayout)av.getParent();

        //we are updating list item for each spinner, this will trigger the method and we trying to avoid infinite loop
        if(spinnerAutoTriggerOnItemSelectedCounter>0)
        {
            spinnerAutoTriggerOnItemSelectedCounter--;

            return;
        }

        //this method will trigger by while instantiating spinner object
        ModifierObject RemoveMo=null;



        if(position==0)
        {
            common.Utility.LogActivity("user deselected modifier");
            //during initiate startup, skip it
            if(RemoveMo!=null) {
                //cancel, when user select the 1st item, remove the spinner from UI if there is another un-selected white group spinner
                if (RemoveMo.getMutualGroup() == Enum.MutualGroupColor.white.group) {

                    //check if there is another unselected spinner sitting around
                    int blnMoreThanOne = 0;
                    for (int i = 0; i < prtLayout.getChildCount(); i++) {

                        if (((ModifierObject) ((Spinner) av).getSelectedView().getTag()).getMutualGroup() == 0) {
                            int selectIndex = ((Spinner)prtLayout.getChildAt(i)).getSelectedItemPosition();

                            ModifierObject mo = ((ModifierObject)((Spinner)prtLayout.getChildAt(i)).getAdapter().getItem(selectIndex));

                            if(((Spinner)prtLayout.getChildAt(i)).getSelectedItemPosition()==0 && mo.getMutualGroup()==Enum.MutualGroupColor.white.group)
                            {
                                blnMoreThanOne++;

                            }
                        }

                    }
                    if(blnMoreThanOne>1)//reuse the current spinner if there is only one option left
                    {
                        for (int i = 0; i < prtLayout.getChildCount(); i++) {

                            if (((ModifierObject) ((Spinner) av).getSelectedView().getTag()).getMutualGroup() == 0) {
                                if (((Spinner) av).getSelectedItemPosition() == 0) {
                                    RemoveSelectedWhiteGroupSpinnerFromUI(av);
                                    break;
                                }
                            }


                        }
                    }
                    else{UpdateExistingWhiteGroupSpinners();}

                }
            }

        }
        else
        {


            //add, if not 1st item
            Duple<Integer,ModifierObject> bundle = new Duple<Integer, ModifierObject>(av.getId(),(ModifierObject)v.getTag());
            //new Item to add
            common.Utility.LogActivity("user selected modifier id "+bundle.GetSecond().getID());

            if(bundle.GetSecond().getMutualGroup()== Enum.MutualGroupColor.white.group){
                //get the next insertion position for this new spinner,next after the current white group spinner
                int nextNewSpinnerPosition = 0;

                for(int i=0;i<prtLayout.getChildCount();i++)
                {
                    if(prtLayout.getChildAt(i)==av)
                    {
                        nextNewSpinnerPosition = i+1;
                        break;
                    }
                }
                AddNewWhiteGroupSpinner((LinearLayout)av.getParent(),nextNewSpinnerPosition);
            }

        }

    }


    private void ShowModifiers()
    {

        //remove all and recreate later
        prtLayout.removeAllViews();
       /* if(GlobalSpinners.size()==0 && IndividualSpinners.size()==0) {
            CreateModifierSpinners(true);
            CreateModifierSpinners(false);
        }*/

        if(GlobalSpinners.size()==0)CreateModifierSpinners(false);
        if(IndividualSpinners.size()==0)CreateModifierSpinners(true);

        ArrayList<Spinner>spinners = (blnIndividualModifierTabClicked)?IndividualSpinners:GlobalSpinners;
        for(Spinner s: spinners)
        {

            prtLayout.addView(s);

        }

        if(prtLayout.getChildCount()==0)
        {
            int paddingSize = common.Utility.DP2Pixel(20,getContext());
            TextView tv = new TextView(getContext());
            tv.setText("No modifier is available");
            tv.setTextColor(getContext().getResources().getColor(R.color.add_new_category_item_text_grey));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
            tv.setPadding(paddingSize,paddingSize,0,0);
            prtLayout.addView(tv);
        }

    }
    private ArrayList<ModifierObject>[] SortModifiersAccordingToGroup(ArrayList<ModifierObject> list)
    {
        ArrayList<ModifierObject>[] groups = new ArrayList[MUTUAL_GROUP_COUNT];

        for(int i = 0;i<MUTUAL_GROUP_COUNT;i++)
            groups[i] = new ArrayList<ModifierObject>();

        for(ModifierObject mo:list)
            groups[mo.getMutualGroup()].add(mo);

        return groups;
    }
    private ArrayList RetrieveAllModifiersInMenuUnderCurrentParentItem(boolean blnIndividual)
    {
        ArrayList<ModifierObject>filteredModifiers = new ArrayList<ModifierObject>();

        if(blnIndividual)
        {
            //individual
            for(long id:all_modifiers.keySet())
            {
                if(id!=globalModifierId)
                {
                    filteredModifiers.addAll(all_modifiers.get(id));
                    break;
                }
            }

/*
            for (ModifierObject mo : all_modifiers) {
                if (mo.getParentID() != common.text_and_length_settings.GLOBAL_MODIFIER_PARENT_ID)
                    filteredModifiers.add(mo);

            }*/


        }
        else
        {
            //global
            if(all_modifiers.containsKey(globalModifierId)) {
                filteredModifiers.addAll(all_modifiers.get(globalModifierId));
            }
            /*for(ModifierObject mo:all_modifiers)
            {
                if(mo.getParentID()==common.text_and_length_settings.GLOBAL_MODIFIER_PARENT_ID)
                    filteredModifiers.add(mo);
            }*/

        }
        return filteredModifiers;
    }
    private ArrayList<ModifierObject>GetReceiptModifiers(boolean blnIndividual,int MutualGroupId)
    {
        ArrayList<ModifierObject>filteredList = new ArrayList<ModifierObject>();

        for(ModifierObject mo:selected_modifiers)
        {
            if(mo.getParentID()==common.text_and_length_settings.GLOBAL_MODIFIER_PARENT_ID && mo.getMutualGroup()==MutualGroupId && !blnIndividual )
            {
                filteredList.add(mo);
            }
            else if(mo.getParentID()!=common.text_and_length_settings.GLOBAL_MODIFIER_PARENT_ID && mo.getMutualGroup()==MutualGroupId && blnIndividual)
            {
                filteredList.add(mo);
            }
        }
        return filteredList;
    }
    private ArrayList<ModifierObject>CreateSpinnerAdapterList(ArrayList<ModifierObject>InitialModifierList,ArrayList<ModifierObject>receiptModifierList, boolean blnLeaveCurrentSelectedInTheList,long lnSelectedId)
    {


        ArrayList<ModifierObject> spinnerAdapterList = new ArrayList<ModifierObject>(InitialModifierList);
        //remove other selected modifier from the adapter list except the current selected one
        for (int j = spinnerAdapterList.size() - 1; j >= 0; j--)
        {
            for(int i=0;i<receiptModifierList.size();i++)
            {
                if(receiptModifierList.get(i).getID()==spinnerAdapterList.get(j).getID())
                {
                    //do another layer of checking if is the selected modifier
                    if(spinnerAdapterList.get(j).getID()==lnSelectedId)
                    {
                        if(!blnLeaveCurrentSelectedInTheList)
                        {
                            spinnerAdapterList.remove(j);
                            break;
                        }
                    }
                    else
                    {
                        spinnerAdapterList.remove(j);
                        break;
                    }


                }
            }

        }


        return spinnerAdapterList;
    }
    private Spinner CreateSpinner(ArrayList<ModifierObject> spinnerAdapterList,long modifierID)//,ArrayList<ModifierObject>receiptModifierList)
    {

        int selectedPosition = -1;


        Spinner s = new Spinner(getContext());

        s.setId(spinnerCounter++);

        spinnerAdapterList.add(0,new ModifierObject(-1, "Select an option...", -2, "0", spinnerAdapterList.get(0).getMutualGroup(), 1,1));//add default


        MyModifierArrayAdapter sAdapter = new MyModifierArrayAdapter(activity
                , R.layout.layout_modifier_spinner_ui, new ArrayList<ModifierObject>(spinnerAdapterList));
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(sAdapter);

        //get selected item position in the adapter list
        for (int j = 0; j < spinnerAdapterList.size(); j++) {
            if (spinnerAdapterList.get(j).getID() == modifierID) {
                selectedPosition = j;
                break;
            }
        }


        //select the 1st item if not match was found
        selectedPosition=(selectedPosition==-1)?0:selectedPosition;
        s.setTag(selectedPosition);
        s.setSelection(selectedPosition);
        //bind listener at last just to avoid trigger the on item selected method
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                SpinnerOnItemSelected(adapterView, view, i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView){}

        });


        return s;

    }
    private void CreateModifierSpinners(boolean blnIndividual)
    {
        ArrayList[] ModifierGroups = SortModifiersAccordingToGroup(RetrieveAllModifiersInMenuUnderCurrentParentItem(blnIndividual));
        ArrayList<Spinner> spinners =null;

        ArrayList<ModifierObject>WhiteGroupModifiersOnMenu=null;
        if(blnIndividual) {
            WhiteGroupAvailableIndividualModifiers = new ArrayList<ModifierObject>(ModifierGroups[0]);
            WhiteGroupModifiersOnMenu = WhiteGroupAvailableIndividualModifiers;
            spinners = IndividualSpinners;
        }
        else{
            WhiteGroupAvailableGlobalModifiers = new ArrayList<ModifierObject>(ModifierGroups[0]);
            WhiteGroupModifiersOnMenu = WhiteGroupAvailableGlobalModifiers;
            spinners = GlobalSpinners;
        }


        for(int i=0;i<ModifierGroups.length;i++)
        {
            if(ModifierGroups[i].size()>0)
            {
                if(i==0)
                {
                    //handle white group differently
                    //further filter the list to retrieve the white group modifiers only(individual/global)
                    ArrayList<ModifierObject> ReceiptWhiteGroupModifierList = GetReceiptModifiers( blnIndividual, Enum.MutualGroupColor.white.group);

                    for(int j=0;j<ReceiptWhiteGroupModifierList.size();j++)
                    {
                        ArrayList<ModifierObject>spinnerAdapterList =CreateSpinnerAdapterList(WhiteGroupModifiersOnMenu,
                                ReceiptWhiteGroupModifierList,true,ReceiptWhiteGroupModifierList.get(j).getID());
                        spinners.add(CreateSpinner(spinnerAdapterList,ReceiptWhiteGroupModifierList.get(j).getID()));//ReceiptWhiteGroupModifierList));

                    }
                    //skip if there is one white group modifier selected in the cart item, then
                    //the algo will detect onItemSelected and trigger to create a new spinner

                    if(WhiteGroupModifiersOnMenu.size()>0 && WhiteGroupModifiersOnMenu.size()>ReceiptWhiteGroupModifierList.size())
                    {
                        //create a new white group spinner for next selection, if there is modifier item selection left on the list
                        if (WhiteGroupModifiersOnMenu.size() > ReceiptWhiteGroupModifierList.size()) {
                            ArrayList<ModifierObject> spinnerAdapterList = CreateSpinnerAdapterList(WhiteGroupModifiersOnMenu,
                                    ReceiptWhiteGroupModifierList, false, -1);
                            spinners.add(CreateSpinner(spinnerAdapterList,-1));// ReceiptWhiteGroupModifierList));

                        }
                    }



                }
                else
                {
                    //for all other group
                    //recreate the spinner for the order

                    ArrayList<ModifierObject> ReceiptGroupModifierList = GetReceiptModifiers( blnIndividual,i);
                    long selectedId=(ReceiptGroupModifierList.size()>0)?ReceiptGroupModifierList.get(0).getID():-1;
                    ArrayList<ModifierObject>spinnerAdapterList =CreateSpinnerAdapterList(new ArrayList<ModifierObject>(ModifierGroups[i]),
                            ReceiptGroupModifierList,true,selectedId);

                    spinners.add(CreateSpinner(spinnerAdapterList,selectedId));//ReceiptGroupModifierList));
                }
            }
        }

    }
    public void TabClicked(View v)
    {

        if(v.getId()==R.id.tabModifierGlobal)
        {
            tabModifierIndividual.setBackground(null);
            blnIndividualModifierTabClicked=false;
            common.Utility.LogActivity("global tab clicked");

        }
        else
        {
            tabModifierGlobal.setBackground(null);
            blnIndividualModifierTabClicked=true;
            common.Utility.LogActivity("individual tab clicked");
        }

        //v.setBackground(getContext().getResources().getDrawable(R.drawable.abc_ab_transparent_light_holo));
        v.setBackground(getContext().getResources().getDrawable(R.color.half_transparent_dark_grey));

        ShowModifiers();

    }
}
