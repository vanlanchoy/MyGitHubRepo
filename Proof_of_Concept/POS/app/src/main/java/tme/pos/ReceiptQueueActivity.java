package tme.pos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.Receipt;
import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 10/29/2015.
 */
public class ReceiptQueueActivity extends Activity {

    private ArrayAdapter<String> adpReceipts;
    //ArrayList<Receipt> receiptQueue;
    ArrayList<String>  lstReceipt;
    ListView lvReceipt;
    TextView tvTitle;
    ArrayList<Integer> deletedReceiptIndexes;
    ArrayList<Integer> intentResult = new ArrayList<Integer>();
    int intInitialReceiptQueueSize=0;

    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override
    public void finish() {
        if(intentResult.size()==0 && lstReceipt.size()==intInitialReceiptQueueSize)
        {
            //nothing selected
            setResult(RESULT_CANCELED);

        }
        else {
            //converted the selected removed receipt into descending order array list
            if (intentResult.size() == 0 && lstReceipt.size() < intInitialReceiptQueueSize) {
                CreateIntentResult();

            }
            if(intentResult.size()>0)
            {
                int[] aryIndexes = new int[intentResult.size()];
                for(int i=intentResult.size()-1;i>=0;i--) {
                    aryIndexes[i] =intentResult.get(i);
                    intentResult.remove(i);
                }

                setResult(RESULT_OK,getIntent().putExtra(AppSettings.EXTRA_DELETED_RECEIPT_INDEX, aryIndexes));
            }else{setResult(RESULT_CANCELED);}


           /* if (intentResult.size() == 0 && lstReceipt.size() < intInitialReceiptQueueSize) {
                CreateIntentResult();
                setResult(RESULT_OK,
                        getIntent().putIntegerArrayListExtra(AppSettings.EXTRA_DELETED_RECEIPT_INDEX, intentResult)
                );

            }
            //else if(intentResult.size()!=deletedReceiptIndexes.size() && deletedReceiptIndexes.size()>0)
            else if (deletedReceiptIndexes.size() > 0) {
                setResult(RESULT_OK,
                        getIntent().putIntegerArrayListExtra(AppSettings.EXTRA_DELETED_RECEIPT_INDEX, intentResult)
                );

            } else if (intentResult.size() > 0 && lstReceipt.size() == 0) {
                setResult(RESULT_OK,
                        getIntent().putIntegerArrayListExtra(AppSettings.EXTRA_DELETED_RECEIPT_INDEX, intentResult)
                );

            }*/
        }

        super.finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //too late to set result in here


    }

    @Override
    protected void onStop() {
        super.onStop();
        //too late to set result in here
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_receipt_queue_ui);


        //setResult(Activity.RESULT_OK);
        setResult(Activity.RESULT_CANCELED);

        deletedReceiptIndexes = new ArrayList<Integer>();

        //load the receipt queue
        lstReceipt = getIntent().getStringArrayListExtra(AppSettings.EXTRA_RECEIPT_QUEUE);
        //receiptQueue = getIntent().getParcelableArrayListExtra(AppSettings.EXTRA_RECEIPT_QUEUE);

        tvTitle = (TextView)findViewById(R.id.tvTitle);
        TextView tvRemoveAll = (TextView)findViewById(R.id.tvRemoveAll);
        tvRemoveAll.setText(Html.fromHtml("<u>remove all</u>"));
        tvRemoveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lstReceipt.clear();
                CreateIntentResult();
                finish();
            }
        });

        intInitialReceiptQueueSize = lstReceipt.size();
        adpReceipts = new MyStringBaseAdapter(this,R.layout.layout_device_name_ui,lstReceipt);
        UpdateReceiptCount(lstReceipt.size());
        lvReceipt = (ListView)findViewById(R.id.lvReceipt);
        lvReceipt.setAdapter(adpReceipts);

    }
    public void RemoveSelectedItem(int position,String receiptIndex)
    {
        deletedReceiptIndexes.add(Integer.parseInt(receiptIndex));
        UpdateReceiptCount(lstReceipt.size());
        //close the activity if the removed item is the one and only item on the current list
        if(lstReceipt.size()==0)
        {
            CreateIntentResult();
            finish();
        }
    }
    private void CreateIntentResult()
    {

        //Intent intent = new Intent();

        if(lstReceipt.size()==0) {
            //indicating delete all
            intentResult.add(-1);
        }
        else if(deletedReceiptIndexes.size()==1)
        {
            intentResult.add(deletedReceiptIndexes.get(0));
        }
        else
        {
            //sort index descending order
            int index;
            while(deletedReceiptIndexes.size()>0)
            {
                index = 0;
                for(int i=1;i<deletedReceiptIndexes.size();i++)
                {
                    if(deletedReceiptIndexes.get(index)<deletedReceiptIndexes.get(i))
                    {
                        index = i;
                    }
                }
                //add the largest index into the list
                intentResult.add(deletedReceiptIndexes.get(index));
                deletedReceiptIndexes.remove(index);//remove from the source after added
            }
        }
       //intent.putIntegerArrayListExtra(AppSettings.EXTRA_DELETED_RECEIPT_INDEX,intentResult);

        //Integer[] aryIndexes = new Integer[intentResult.size()];
        //intentResult.toArray(aryIndexes);

       /* //convert the array list into array
        int[] aryIndexes = new int[intentResult.size()];
        for(int i=0;i<intentResult.size();i++)
            aryIndexes[i]=intentResult.get(i);
*/
        //intent.putExtra(AppSettings.EXTRA_DELETED_RECEIPT_INDEX, aryIndexes);
        //setResult(RESULT_OK);
    }
    private void UpdateReceiptCount(int count)
    {
        tvTitle.setText(Html.fromHtml("Receipt queue <b>" + count + "</b> item(s)"));
    }

}
