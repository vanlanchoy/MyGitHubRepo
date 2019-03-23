package tme.pos.CustomViewCtr;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Html;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.FloorPlanUIActivity;
import tme.pos.R;

/**
 * Created by vanlanchoy on 4/19/2015.
 */
public class FloorPlanCtr extends RelativeLayout {

    public interface FloorPlanCtrTableTouchedListener {
        public void OnTableTouched(String strTableId);
    }
    FloorPlanCtrTableTouchedListener listener;
    float offset = 30.0f;
    //float tableObjectOffset=40f;
    int tableLabelXOffset=-25;
    int tableLabelYOffset=7;
    final float tableObjectRadius = 30f;
    int StrokeWidth = 3;
    final int defaultEffectCounterValue=60;//assume 60 fps
    int effectCounter=defaultEffectCounterValue;
    final float fingerRadius=40f;//according to google, human finger cover 60dp on screen
    boolean blnDraw=false;
    float[] points = new float[0];
    float[] selectedPoints= new float[0];
    float previousX,previousY;
    Enum.FloorPlanMode floorPlanMode;
    FloorPlanUIActivity parentCtr;
    ArrayList<Duple<float[],float[]>>floorPlanScribbles = new ArrayList<Duple<float[],float[]>>();
    ArrayList<float[]>floorPlanLines = new ArrayList<float[]>();
    ArrayList<float[]>floorPlanArces = new ArrayList<float[]>();
    ArrayList<Duple<float[],Duple<String,String>>>floorPlanTable = new ArrayList<Duple<float[],Duple<String,String>>>();
    float[] centerPoint = new float[0];
    Enum.SelectedFloorPlanObject selectedObject = Enum.SelectedFloorPlanObject.none;
    TextView tvMessage;
    //String strTableLabel;
    Duple<float[],Duple<String,String>> selectedTable;
    boolean blnTableNameEditing=false;
    EditText txtPopupLabel;
    boolean blnDirty=false;
    Path tempPath = new Path();
    float tempX = -1;
    float tempY = -1;
    boolean blnRemoveLastDrawnPath=false;
    //RelativeLayout selectedTableObject;
    public void SetMessageControl(TextView tv)
    {
        this.tvMessage = tv;
    }
    public void SetParentCtr(FloorPlanUIActivity parent)
    {
        parentCtr=parent;
    }
    public void SetTableTouchedListener(FloorPlanCtrTableTouchedListener l){listener=l;}
    public void SetFloorPlanMode(Enum.FloorPlanMode mode)
    {

        if(floorPlanMode== Enum.FloorPlanMode.copy)
        {
            //reinsert the selected object
            if(selectedObject== Enum.SelectedFloorPlanObject.arc)
            {
                floorPlanArces.add(selectedPoints);
            }
            else if(selectedObject== Enum.SelectedFloorPlanObject.line)
            {
                floorPlanLines.add(selectedPoints);
            }
            else if(selectedObject== Enum.SelectedFloorPlanObject.scribble)
            {
                floorPlanScribbles.add(new Duple<float[], float[]>(selectedPoints, centerPoint));
            }
           /* else if(selectedObject== Enum.SelectedFloorPlanObject.table)
            {
                //floorPlanTable.add(selectedPoints);
            }*/
        }


        /*if(selectedTableObject!=null)
        {
            ((ImageButton)selectedTableObject.getChildAt(0)).setBackground(getResources().getDrawable(R.drawable.draw_circle));
        }*/


        floorPlanMode = mode;
        points=new float[0];
        centerPoint = new float[0];
        selectedPoints=new float[0];
        UpdateMessage(0);
        invalidate();
    }
    public boolean IsDirty()
    {
        return blnDirty;
    }


    public void SaveTableLabel()
    {


        String strNewLabel = txtPopupLabel.getText().toString().trim();

        if(blnTableNameEditing)
        {


            //check duplicate
            for(int i=0;i<floorPlanTable.size();i++) {
                if(strNewLabel.equalsIgnoreCase(floorPlanTable.get(i).GetSecond().GetSecond()))
                {
                    common.Utility.ShowMessage("Label","Table label name <b>"+floorPlanTable.get(i).GetSecond().GetSecond()+"</b> already existed.",getContext(),R.drawable.no_access);
                    return;
                }
            }


        }



        if(strNewLabel.length()>0 && selectedPoints.length>0) {
            /*if(selectedTable!=null)
            {
                //don't assigned a new id to this table if is edit mode
                selectedTable.GetSecond().SetFirst(strNewLabel);
                floorPlanTable.add(selectedTable);
            }
            else {*/
                //Duple<String, String> d = new Duple<String, String>(Calendar.getInstance().getTimeInMillis() + "", strNewLabel);
            //ShowMessage("table id ",selectedTable.GetSecond().GetFirst()+"");
            Duple<String, String> d = new Duple<String, String>(selectedTable.GetSecond().GetFirst(), strNewLabel);
                floorPlanTable.add(new Duple<float[],
                        Duple<String, String>>(selectedPoints, d));
            //}
        }


        ResetAddTableModeVariables();
        invalidate();
    }
    public boolean IsTableInEditingMode()
    {
        return blnTableNameEditing;
    }
    private void DeleteObject()
    {
        if(selectedObject == Enum.SelectedFloorPlanObject.none)return;

        //set to null
        selectedPoints = new float[0];

        //call re draw
        invalidate();
    }
    private void MoveObject(float x,float y)
    {
        if(selectedObject== Enum.SelectedFloorPlanObject.none)return;
        if(selectedPoints.length==0)return;
        //tvMessage.setText("previousX["+ previousX + "]x["+x+"]y["+y+"]previousY["+previousY+"]x1["+selectedPoints[0]+"]y1["+selectedPoints[1]+"]x2["+""+selectedPoints[2]+"]y2["+selectedPoints[3]);
        float differX = previousX-x;
        float differY = previousY-y;

        previousX = x;
        previousY = y;
        if(selectedObject== Enum.SelectedFloorPlanObject.scribble ||
                selectedObject== Enum.SelectedFloorPlanObject.line ||
                selectedObject== Enum.SelectedFloorPlanObject.arc ||
                selectedObject== Enum.SelectedFloorPlanObject.table) {
            for (int i = 0; i < selectedPoints.length; i += 2) {
                selectedPoints[i] -= differX;
                selectedPoints[i + 1] -= differY;


            }
            if(selectedObject== Enum.SelectedFloorPlanObject.scribble && centerPoint.length>0)
            {
                centerPoint[0] -=differX;
                centerPoint[1]-=differY;
            }

        }


    }
    private void EditTableLabelName(float x,float y)
    {

        if(blnTableNameEditing)return;//still in edit mode



        //set flag
        blnTableNameEditing=true;



        //add edit view at touched point
        txtPopupLabel = new EditText(getContext());
        txtPopupLabel.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtPopupLabel.setText(selectedTable.GetSecond().GetSecond());
       /* txtPopupLabel.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE)
                {
                    SaveTableLabel();
                    return true;
                }
                return false;
            }
        });*/
        txtPopupLabel.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
                        || keyCode == KeyEvent.KEYCODE_BACK) {
                    SaveTableLabel();
                    return true;

                }
                return false;
            }
        });
        txtPopupLabel.setTextSize(15);
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(4);
        txtPopupLabel.setFilters(filterArray);
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rllp.setMargins(Math.round(x)+tableLabelXOffset,Math.round(y)-(2*tableLabelYOffset),0,0);
        this.addView(txtPopupLabel,rllp);

        //show soft keyboard
        txtPopupLabel.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(txtPopupLabel, InputMethodManager.SHOW_IMPLICIT);
    }

    private void SelectTouchedObject(float x, float y)
    {
       /* if(selectedObject== Enum.SelectedFloorPlanObject.scribble)
        {
            //floorPlanObjects.add(selectedPoints);


        }*/

        selectedPoints= new float[0];
        selectedObject = Enum.SelectedFloorPlanObject.none;
        centerPoint = new float[0];

        //get which object has been selected, if hasn't
        HitTestScribbleObjects(x,y);
        if(selectedObject== Enum.SelectedFloorPlanObject.none)HitTestLineObject(x,y);
        if(selectedObject== Enum.SelectedFloorPlanObject.none)HitTestArcObjects(x, y);
        //table object does not support rotation
        if(selectedObject== Enum.SelectedFloorPlanObject.none &&
                floorPlanMode!= Enum.FloorPlanMode.rotate &&
                floorPlanMode!= Enum.FloorPlanMode.copy)HitTestTableObjects(x, y);

        if(selectedObject!= Enum.SelectedFloorPlanObject.none )
        {
            if(selectedObject== Enum.SelectedFloorPlanObject.line)
                ReturnCenterPoint(selectedPoints);
            else if(selectedObject== Enum.SelectedFloorPlanObject.scribble)
                ReturnScribbleObjectCenterPoint(selectedPoints);
            else if(selectedObject== Enum.SelectedFloorPlanObject.arc)
                ReturnArcObjectCenterPoint(selectedPoints);
        }



    }
    private double AngleBetweenLines(float[] line1,float[] line2)
    {
        double angle1 = Math.atan2(line1[1] - line1[3],
                line1[0] - line1[2]);
        double angle2 = Math.atan2(line2[1] - line2[3],
                line2[0] - line2[2]);
        return angle1-angle2;
    }
    private void ReturnArcObjectCenterPoint(float[] points)
    {
        if(points.length==0)return;
        centerPoint = new float[2];
        centerPoint[0] = points[0];
        centerPoint[1] = points[1];
    }
    private void ReturnScribbleObjectCenterPoint(float[] points)
    {
        if(points.length==0)return;

        //reuse previously stored
        if(centerPoint.length>0)return;


        float EPSILON = 10f;
        float smallestX,largestX,smallestY,largestY;
        smallestX = points[0];
        largestX = smallestX;
        smallestY = points[1];
        largestY = smallestY;
        centerPoint = new float[2];
        for(int i=2;i<points.length;i+=2)
        {
            if(smallestX>points[i])smallestX=points[i];

            if(largestX<points[i])largestX=points[i];

            if(smallestY>points[i])smallestY=points[i+1];

            if(largestY<points[i])largestY=points[i+1];
        }

        //centerPoint[0] = (largestX+smallestX)/2f;
        //centerPoint[1] = (largestY+smallestY)/2f;
        //special case for scribble object, the center point will be changed after rotation
        if(largestX-smallestX>largestY-smallestY)
        {
            float targetValue = (largestX+smallestX)/2f;
            //look for center point in x-axis
            for(int i=0;i<points.length;i+=2)
            {
                if(points[i]>=targetValue && points[i]<=targetValue+EPSILON)
                {
                    centerPoint[0]=points[i];
                    centerPoint[1]=points[i+1];
                    return;
                }
            }
        }
        else
        {
            //look for center point in y-axis
            float targetValue = (largestY+smallestY)/2f;
            for(int i=1;i<points.length;i+=2)
            {
                if(points[i]>=targetValue && points[i]<=targetValue+EPSILON)
                {
                    centerPoint[1]=points[i];
                    centerPoint[0]=points[i-1];
                    return;
                }
            }
        }

    }
    private void ReturnCenterPoint(float[] points)
    {
        if(points.length==0)return;

        float smallestX,largestX,smallestY,largestY;
        smallestX = points[0];
        largestX = smallestX;
        smallestY = points[1];
        largestY = smallestY;
        centerPoint = new float[2];
        for(int i=2;i<points.length;i+=2)
        {
            if(smallestX>points[i])smallestX=points[i];

            if(largestX<points[i])largestX=points[i];

            if(smallestY>points[i])smallestY=points[i+1];

            if(largestY<points[i])largestY=points[i+1];
        }

        centerPoint[0] = (largestX+smallestX)/2f;
        centerPoint[1] = (largestY+smallestY)/2f;
    }
    private int[] ReturnCenterPointAsResult(float[] points)
    {
        if(points.length==0)return new int[0];
        int[] cp = new int[2];
        int smallestX,largestX,smallestY,largestY;
        smallestX = Math.round(points[0]);
        largestX = Math.round(smallestX);
        smallestY = Math.round(points[1]);
        largestY = smallestY;

        for(int i=2;i<points.length;i+=2)
        {
            if(smallestX>points[i])smallestX=Math.round(points[i]);

            if(largestX<points[i])largestX=Math.round(points[i]);

            if(smallestY>points[i])smallestY=Math.round(points[i+1]);

            if(largestY<points[i])largestY=Math.round(points[i+1]);
        }

        cp[0] = (largestX+smallestX)/2;
        cp[1] = (largestY+smallestY)/2;
        return cp;
    }
    private void RotateObject(float x,float y)
    {
        if(selectedObject== Enum.SelectedFloorPlanObject.none ||
                selectedObject== Enum.SelectedFloorPlanObject.table)return;



        float[] line1=new float[]{centerPoint[0],centerPoint[1],previousX,previousY};
        float[] line2=new float[]{centerPoint[0],centerPoint[1],x,y};

        double angle = AngleBetweenLines(line1,line2);

        //angle = AngleBetweenPoints(new float[]{previousX,previousY,x,y});
        double degree =-1*Math.toDegrees(angle);

        Matrix m = new Matrix();

        m.setRotate(Float.parseFloat(degree+""),centerPoint[0],centerPoint[1]);

        m.mapPoints(selectedPoints);

        previousX = x;
        previousY = y;


    }
    private void DeselectMoveObject()
    {
        //reinsert the object back into the group before selecting another
        if(selectedObject== Enum.SelectedFloorPlanObject.scribble)
        {
            if(floorPlanMode== Enum.FloorPlanMode.move)
            {
                //don't store the center point since the object has been moved
                //floorPlanObjects.add(new Duple<float[], float[]>(selectedPoints,new float[0]));
                floorPlanScribbles.add(new Duple<float[], float[]>(selectedPoints, centerPoint));
            }
            else
            {
                //need to reuse the center point from current rotation mode
                floorPlanScribbles.add(new Duple<float[], float[]>(selectedPoints, centerPoint));
            }

            //floorPlanObjects.add(new Duple<float[], float[]>(selectedPoints,centerPoint));
        }
        else if(selectedObject== Enum.SelectedFloorPlanObject.line)
        {
            floorPlanLines.add(selectedPoints);
        }
        else if(selectedObject== Enum.SelectedFloorPlanObject.arc)
        {
            floorPlanArces.add(selectedPoints);
        }
        else if(selectedObject== Enum.SelectedFloorPlanObject.table)
        {
            floorPlanTable.add(selectedTable);//new Duple(selectedPoints,strTableLabel));
        }
        selectedPoints = new float[0];
        centerPoint = new float[0];
        selectedTable=null;
        //strTableLabel="";
        selectedObject= Enum.SelectedFloorPlanObject.none;
    }

    private void HitTestTableObjects(float x,float y)
    {

        selectedPoints=new float[0];
        for(int i=0;i<floorPlanTable.size();i++)
        {
            if( floorPlanTable.get(i)==null)continue;

            Duple<float[],Duple<String,String>> duple = floorPlanTable.get(i);
            if(duple.GetFirst().length==0)continue;
            int cX=Math.round(duple.GetFirst()[0]);
            int cY=Math.round(duple.GetFirst()[1]);
            //tvMessage.setText((Math.pow((cX-x),2)+Math.pow((cY-y),2))+"="+Math.pow(tableObjectRadius,2));
            if(Math.pow((cX - x), 2)+Math.pow((cY-y),2)<=Math.pow(tableObjectRadius,2))
            {
                selectedTable = floorPlanTable.remove(i);
                selectedPoints = selectedTable.GetFirst();//selected table object
                //ShowMessage("hit test result","x="+selectedPoints[0]+", y="+selectedPoints[1]);
                //strTableLabel = duple.GetSecond().GetSecond();//keeping track of selected table object label
                selectedObject = Enum.SelectedFloorPlanObject.table;
                return;
            }

        }
        //ShowMessage("hit test result","NONE!");
    }
    private void HitTestArcObjects(float x,float y)
    {
        //calculate the distance between center and touch point and compare with existing
        float storedDistance;
        float touchPointDistance;
        for(int i =0;i< floorPlanArces.size();i++) {
            storedDistance = common.mathLib.CalculateDistance(floorPlanArces.get(i));
            touchPointDistance = common.mathLib.CalculateDistance(new float[]{floorPlanArces.get(i)[0],
                    floorPlanArces.get(i)[1],
                    x, y});

            float x1 = floorPlanArces.get(i)[2];
            float y1 = floorPlanArces.get(i)[3];
            float x2 = floorPlanArces.get(i)[4];
            float y2 = floorPlanArces.get(i)[5];
            Point center = new Point(Math.round(floorPlanArces.get(i)[0]),Math.round(floorPlanArces.get(i)[1]));
            Point p1 =  new Point(Math.round(x1),Math.round(y1));
            Point p2 =  new Point(Math.round(x2),Math.round(y2));

            double startAngleStart = common.mathLib.CalculateSweepAngle(center, p1);
            double sweepAngleStart = common.mathLib.CalculateSweepAngle(center, p2);
            double sweepAngleTouch = common.mathLib.CalculateSweepAngle(center, new Point(Math.round(x), Math.round(y)));

            if(startAngleStart>sweepAngleStart)
            {

                sweepAngleStart = startAngleStart-sweepAngleStart;
                sweepAngleStart =Math.abs(360-sweepAngleStart);
            }
            else {
                sweepAngleStart -= startAngleStart;
            }
            if(startAngleStart>sweepAngleTouch)
            {

                sweepAngleTouch = startAngleStart-sweepAngleTouch;
                sweepAngleTouch =Math.abs(360-sweepAngleTouch);
            }
            else {
                sweepAngleTouch -= startAngleStart;
            }

            if(Math.abs(storedDistance-touchPointDistance)<=offset &&
                  sweepAngleStart>=sweepAngleTouch)
            {
                selectedObject = Enum.SelectedFloorPlanObject.arc;
                selectedPoints = floorPlanArces.remove(i);
                return;
            }
        }
    }
    private void HitTestScribbleObjects(float x,float y)
    {
        //test scribble objects
        for(int i=0;i<floorPlanScribbles.size();i++)
        {
            for(int j=0;j<floorPlanScribbles.get(i).GetFirst().length;j+=2)
            {


                //found a match
                if( Math.abs(floorPlanScribbles.get(i).GetFirst()[j] - x)<offset &&
                        Math.abs(floorPlanScribbles.get(i).GetFirst()[j+1]-y)<offset)
                {
                    //set flag
                    selectedObject = Enum.SelectedFloorPlanObject.scribble;
                    //separate it from others
                    Duple<float[],float[]>duple = floorPlanScribbles.remove(i);
                    selectedPoints = duple.GetFirst();
                    centerPoint = duple.GetSecond();
                    return;
                }
            }
        }



    }
    private void HitTestLineObject(float x,float y)
    {
        final float EPSILON = 45.f;
        final int EPSILON2 = 10;
        //test line objects
        for(int i=0;i<floorPlanLines.size();i++)
        {

            if(floorPlanLines.get(i).length==0)continue;

            int x1 = Math.round(floorPlanLines.get(i)[0]);
            int y1 = Math.round(floorPlanLines.get(i)[1]);
            int x2 = Math.round(floorPlanLines.get(i)[2]);
            int y2 = Math.round(floorPlanLines.get(i)[3]);
            int intX = Math.round(x);
            int intY = Math.round(y);

            //ShowMessage("hit test","y is ["+y+"] , x is ["+x+"] ,y1 is ["+y1+"], x1 is ["+x1 +"] ,y2 is ["+y2+"], x2 is ["+x2+"]");
            if (Math.abs(x1 - x2) < EPSILON) {
                // We've a vertical line, thus check only the x-value of the point.
                if(Math.abs(x - x1)<EPSILON && ((y1>y2 && y2<=y && y<=y1) ||(y2>=y1 && y1<=y && y<=y2)))
                {
                    //tvMessage.setText("vertical line, x1="+x1+", y1="+y1+", x2="+x2+", y2="+y2+", x="+x+", y="+y);
                }
                else{continue;}
            } else {
                int m = Math.round((y2 - y1) / (x2 - x1));
                int[] cp=ReturnCenterPointAsResult(new float[]{x1,y1,x2,y2});
                int b = cp[1] - m * cp[0];//crossed point center
                int answer = m * intX + b;//Math.abs(y - (m * x + b));
//tvMessage.setText("y="+answer+", x1="+x1+", y1="+y1+", x2="+x2+", y2="+y2+", x="+intX+", y="+intY);
                //check the touched point is on the same slope
                if(Math.abs(intY - (m * intX + b)) < EPSILON)
                {
                    //check the touched point is in the range
                   if(x1>x2 && (x2-EPSILON2)<=intX && intX<=(x1+EPSILON2)){}
                       else if(x2>=x1 && (x1-EPSILON2)<=intX && intX<=(x2+EPSILON2)){}
                    else continue;

                    if(y1>y2 && (y2-EPSILON2)<=intY && intY<=(y1+EPSILON2)){}
                    else if(y2>=y1 && (y1-EPSILON2)<=intY && intY<=(y2+EPSILON2)){}
                    else continue;
                }
                else
                {
                    continue;
                }
            }


            //tvMessage.setText("hit test y is ["+y+"] , x is ["+x+"] ,y1 is ["+floorPlanLines.get(i)[1]+"], x1 is ["+floorPlanLines.get(i)[0] +"] ,y2 is ["+floorPlanLines.get(i)[3]+"], x2 is ["+floorPlanLines.get(i)[2]+"]");

                    //set flag
                    selectedObject = Enum.SelectedFloorPlanObject.line;
                    //separate it from others
                    selectedPoints = floorPlanLines.remove(i);
                    return;


        }
    }
    private void CopyMode(float x, float y)
    {
        if(selectedPoints.length==0) {
            //get the object need to be copied for 1st touch
            SelectTouchedObject(x, y);

            if(selectedPoints.length>0)UpdateMessage(1);
        }
        else {
            //start duplicating the selected object from before
            //calculate new center point
            float differX = centerPoint[0]-x;
            float differY = centerPoint[1]-y;

            //create new object at new position
            if (selectedObject == Enum.SelectedFloorPlanObject.line) {
                floorPlanLines.add(new float[]{selectedPoints[0]-differX,//start point x
                        selectedPoints[1]-differY, //starting point y
                        selectedPoints[2]-differX, //ending point x
                        selectedPoints[3]-differY});//ending point y
            } else if (selectedObject == Enum.SelectedFloorPlanObject.scribble) {
                float[] newScribbleObject = new float[selectedPoints.length];
                for(int i=0;i<selectedPoints.length;i+=2)
                {
                    newScribbleObject[i]=selectedPoints[i]-differX;
                    newScribbleObject[i+1]=selectedPoints[i+1]-differY;
                }
                floorPlanScribbles.add(new Duple<float[], float[]>(newScribbleObject,//line point
                        new float[]{x, y}));//center point


            } else if (selectedObject == Enum.SelectedFloorPlanObject.arc) {

                floorPlanArces.add(new float[]{x, //center point x
                        y, //center point x
                        selectedPoints[2]-differX, //start angle point x
                        selectedPoints[3]-differY, //start angle point y
                        selectedPoints[4]-differX, //sweep angle point x
                        selectedPoints[5]-differY});//sweep angle point y


            }
        }


    }
    private void ArcMode(float x,float y)
    {
        if(points.length==0) {
            //recording the center point
            points = new float[]{x, y};

            //reset counter
            effectCounter=defaultEffectCounterValue;

            //update message
            UpdateMessage(1);
        }
        else if(points.length==2)
        {
            //now radius point
            float[] temp = points;
            points = new float[4];
            points[0] = temp[0];
            points[1] = temp[1];
            points[2] = x;
            points[3] = y;

            UpdateMessage(2);
        }
        else
        {
            //last point to connect center and 1st points
            floorPlanArces.add(new float[]{points[0],points[1],points[2],points[3],x,y});
            points = new float[0];
        }
    }
    private void TableMode(float x,float y,String strLabel)
    {

        //need to wait until user finished adding label before allowing to add new
        if(blnTableNameEditing)return;
        parentCtr.SetFloorPlanMode(Enum.FloorPlanMode.none);//reset flag, only one table to be added per touch
        selectedPoints = new float[]{x,y};
        if(selectedTable!=null){
            //don't create new table object if is edit mode, else id will be updated
        }
        else {
            selectedTable = new Duple<float[], Duple<String, String>>(selectedPoints,
                    new Duple<String, String>((new Date()).getTime() + "", strLabel));
        }
        //strTableLabel=strLabel;
        selectedObject = Enum.SelectedFloorPlanObject.table;
        floorPlanMode = Enum.FloorPlanMode.none;
        EditTableLabelName(x,y);

        invalidate();

    }
    private void LineMode(float x,float y)
    {
        if(points.length==0) {
            //recording the 1st point
            points = new float[]{x, y};

            //reset counter
            effectCounter=defaultEffectCounterValue;

            //update message
            UpdateMessage(1);
        }
        else
        {
            //we have two points, can draw line now
            floorPlanLines.add(new float[]{points[0],points[1],x,y});
            points = new float[0];
        }
    }
    private  void UpdateMessage(int WaitingForPointIndex)
    {
        String strMsg ="Mode: "+floorPlanMode.name().toUpperCase()+". ";
        switch(floorPlanMode) {
            case line:
                if(WaitingForPointIndex==0)
                    strMsg+="Please touch on the screen to set the <font color=\"red\">START</font> point.";
                else
                    strMsg+="Now touch on the screen to set the <font color=\"red\">END</font> point.";
                break;
            case scribble:

                strMsg+="Begin with touching the screen in order to draw.";
                break;
            case eraser:

                strMsg+="Touch any object on the screen to remove.";
                break;
            case move:

                strMsg+="Touch and drag any object on the screen to its new position.";
                break;
            case rotate:

                strMsg+="Touch and move your finger clockwise or anti-clockwise on object to change degree.";
                break;
            case arc:
                if(WaitingForPointIndex==0)
                    strMsg+="Please touch on the screen to set the <font color=\"red\">CENTER</font> point.";
                else if(WaitingForPointIndex==1)
                    strMsg+="Now touch on the screen to set the <font color=\"red\">RADIUS</font> point.";
                else
                    strMsg+="Now please CHOOSE the area to draw.";
                break;
            case table:
                strMsg+="Touch on the screen to add table or touch on existing table to edit label.";
                break;
            case copy:
                if(WaitingForPointIndex==0)
                    strMsg+="Touch on the object you wish to make a copy.";
                else
                    strMsg+="Touch any place on the screen to paste";
                break;
            case none:
                break;
            default:
                break;
        }
        if(tvMessage!=null)
        tvMessage.setText(Html.fromHtml(strMsg));

    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();


        switch(action)
        {
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_DOWN:
                if(blnDraw)return false;
                blnDraw = true;
                previousX = ev.getX();
                previousY = ev.getY();
                if(floorPlanMode== Enum.FloorPlanMode.line)
                {
                    blnDirty = true;
                    LineMode(ev.getX(),ev.getY());
                }
                else if (floorPlanMode== Enum.FloorPlanMode.arc)
                {
                    blnDirty = true;
                    ArcMode(ev.getX(),ev.getY());
                }
                else if(floorPlanMode== Enum.FloorPlanMode.copy)
                {
                    blnDirty = true;
                    CopyMode(ev.getX(),ev.getY());
                }
                else if(floorPlanMode== Enum.FloorPlanMode.move ||
                        floorPlanMode== Enum.FloorPlanMode.rotate
                        )
                {
                    if(selectedPoints.length==0)
                    SelectTouchedObject(ev.getX(), ev.getY());

                    blnDirty = true;
                }
                else if(floorPlanMode== Enum.FloorPlanMode.eraser)
                {
                    blnDirty = true;
                    SelectTouchedObject(ev.getX(),ev.getY());
                    DeleteObject();
                }
                else if(floorPlanMode== Enum.FloorPlanMode.table)
                {
                    blnDirty = true;
                    TableMode(ev.getX(),ev.getY(),"");
                }

                else if(floorPlanMode== Enum.FloorPlanMode.none)
                {
                    //remove edit table label from previous incomplete editing if
                    //user pressed the system hide keyboard key where couldn't detected
                    if(blnTableNameEditing && selectedPoints.length>0 && selectedTable.GetSecond().GetSecond().length()>0) {
                        //add it back to the list if is edit
                        SaveTableLabel();

                    }
                    else
                    {
                        ResetAddTableModeVariables();
                    }

                    //allow edit table label name
                    HitTestTableObjects(ev.getX(), ev.getY());
                    if(selectedObject== Enum.SelectedFloorPlanObject.table && selectedPoints.length>0)
                    {
                        blnDirty = true;//ShowMessage("Edit table id",""+selectedTable.GetSecond().GetFirst());
                        TableMode(selectedPoints[0],selectedPoints[1],selectedTable.GetSecond().GetSecond());
                    }
                    //EditTableLabelName(ev.getX(),ev.getY());
                }
                else if(floorPlanMode== Enum.FloorPlanMode.select)
                {

                    HitTestTableObjects(ev.getX(), ev.getY());
                    if(selectedObject== Enum.SelectedFloorPlanObject.table && selectedPoints.length>0)
                    {

                        String strTableId =selectedTable.GetSecond().GetFirst();
                        //ShowMessage("selected id",strTableId);
                        if(strTableId.length()>0 && listener!=null)listener.OnTableTouched(strTableId);
                    }
                }
                else if(floorPlanMode== Enum.FloorPlanMode.scribble)
                {
                    ScribbleLine(ev.getX(), ev.getY(), false);
                    tempPath = new Path();
                    tempPath.moveTo(ev.getX(),ev.getY());
                    tempX = ev.getX();
                    tempY = ev.getY();
                    blnRemoveLastDrawnPath=false;//will be reset in action up
                }
                return true;
                //break;
            case MotionEvent.ACTION_MOVE:
                if(blnDraw)
                {
                    if(floorPlanMode== Enum.FloorPlanMode.scribble)
                    {
                        ScribbleLine(ev.getX(), ev.getY(), false);
                        tempPath.lineTo(ev.getX(), ev.getY());
                        //tempPath.quadTo(ev.getX(),ev.getY(),(tempX+ev.getX())/2,(tempY+ev.getY())/2);//not smooth enough
                        tempX = ev.getX();
                        tempY = ev.getY();
                    }
                    else if(floorPlanMode== Enum.FloorPlanMode.move)
                    {
                        MoveObject(ev.getX(), ev.getY());
                    }
                    else if(floorPlanMode== Enum.FloorPlanMode.rotate)
                    {
                        RotateObject(ev.getX(), ev.getY());
                    }
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_HOVER_EXIT:
            case MotionEvent.ACTION_UP:
                blnDraw=false;

                if(floorPlanMode== Enum.FloorPlanMode.scribble) {
                    tempPath.lineTo(ev.getX(),ev.getY());
                    ScribbleLine(ev.getX(), ev.getY(), true);
                    //floorPlanScribbles.add(new Duple<float[], float[]>(points, new float[0]));

                    blnRemoveLastDrawnPath = true;
                    //points = new float[0];
                }

                else if(floorPlanMode== Enum.FloorPlanMode.move ||
                        floorPlanMode== Enum.FloorPlanMode.rotate)
                {
                    //deselect the current selected object
                    DeselectMoveObject();
                }
                invalidate();
                break;
            default :

                break;
        }


        return super.onTouchEvent(ev);
    }
    public void SlideOut(boolean flgSwipeLeftToDelete)
    {
        final FloorPlanCtr fpCtr = this;
        TranslateAnimation movement = new TranslateAnimation(0.0f, 5000.0f, 0.0f, 0.0f);//move right
        if(flgSwipeLeftToDelete) {
            movement = new TranslateAnimation(0.0f, -5000.0f, 0.0f, 0.0f);//move left
        }
        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ((LinearLayout)getParent()).removeView(fpCtr);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(400);
        movement.setFillAfter(true);



        startAnimation(movement);

    }
    public void SlideIn(boolean blnSlideRight)
    {
        //currentView = this;
        //final TableLayout parentTbl = (TableLayout)this.getParent();
        TranslateAnimation movement = new TranslateAnimation(-5000.0f, 0.0f, 0.0f, 0.0f);//move right
        if(!blnSlideRight)
        {
            //slide in to right
            movement = new TranslateAnimation(5000.0f, 0.0f, 0.0f, 0.0f);//move left
        }





        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(VISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(400);
        movement.setFillAfter(true);



        startAnimation(movement);

    }

    private void ResetAddTableModeVariables()
    {
        //hide soft keyboard
        if(txtPopupLabel!=null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(txtPopupLabel.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        selectedObject = Enum.SelectedFloorPlanObject.none;
        selectedPoints=new float[0];
        txtPopupLabel = null;//ShowMessage("ResetAddTableModeVariables","txtPopupLabel = null");
        selectedTable = null;
        //strTableLabel="";
        blnTableNameEditing=false;
        removeAllViews();
    }
    public FloorPlanCtr(Context context) {
        super(context);//LoadSaved();
    }

    public FloorPlanCtr(Context context, AttributeSet attrs) {
        super(context, attrs);//LoadSaved();
    }

    public FloorPlanCtr(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);//LoadSaved();
    }

    public void Clear()
    {
        blnDirty=true;
        points = new float[0];
        selectedPoints = new float[0];
        floorPlanScribbles.clear();
        floorPlanLines.clear();
        floorPlanArces.clear();
        floorPlanTable.clear();
        //floorPlanMode = Enum.FloorPlanMode.none;
        removeAllViews();

        invalidate();
    }
    private void ScribbleLine(float x, float y, boolean blnLast)
    {
        if(points.length==0)
        {
            points = new float[2];
            points[0] = x;
            points[1] = y;
            return;
        }

        float[] tempPoints = new float[points.length+2];

        //float incrementRate = 1.0f;

        //float differX =Math.abs(x- points[points.length-2]);
        //float differY =Math.abs(y- points[points.length-1]);
        //if(Math.abs(differX)<.01 && Math.abs(differY)<.01)return;


        for(int i=0;i<tempPoints.length;i++)
        {
            if(i<points.length) {
                tempPoints[i] = points[i];
            }
            else
            {
                break;


            }
        }
        tempPoints[tempPoints.length-2] = x;
        tempPoints[tempPoints.length-1] = y;
        points = tempPoints;

        if(blnLast){
            float[] extractedPts = new float[2];
            PathMeasure pm = new PathMeasure(tempPath,false);
            int length = (int)pm.getLength();
            ArrayList<Float> lstPts = new ArrayList<Float>();
            for(int i=1;i<length;i++)
            {
                pm.getPosTan(i,extractedPts,null);
                lstPts.add(extractedPts[0]);
                lstPts.add(extractedPts[1]);
            }

            float[] extractedPathPts = new float[lstPts.size()];
            int i=0;
            for(Float f:lstPts)
            {
                extractedPathPts[i++] = f;
            }

            floorPlanScribbles.add(new Duple<float[], float[]>(extractedPathPts, new float[0]));
            //floorPlanScribbles.add(new Duple<float[], float[]>(points, new float[0]));
            points = new float[0];//reset
        }
        /*float differY = points[points.length-1]-y;
        float differX = points[points.length-2]-x;
        //don't add more point if is less than 1 pixel for x or y
        if(Math.abs(differX)<.01 && Math.abs(differY)<.01)return;
        float flOriDifferX = differX,flOriDifferY=differY;
        ArrayList<Float> ptList = new ArrayList<Float>();
        //insert last position for the following calculation
        ptList.add(points[points.length-2]);
        ptList.add(points[points.length-1]);


        //connecting each point
        while(differX!=0f || differY!=0f)
        {
            if(differX<0)
            {
                //minus
                ptList.add(
                        ptList.get(ptList.size()-2)+incrementRate
                );
                differX +=incrementRate;
            }
            else if(differX>0)
            {
                //add
                ptList.add(
                        ptList.get(ptList.size()-2)-incrementRate
                );
                differX -=incrementRate;
            }
            else
            {
                //maintaining current value
                ptList.add(
                        ptList.get(ptList.size()-2)
                );
            }



            //set to zero if less than increment rate
            if(Math.abs(differX)<incrementRate)differX=0;


            //-2 because new X has been added previously
            if(differY<0)
            {
                //minus

                ptList.add(
                        ptList.get(ptList.size()-2)+incrementRate
                );
                differY +=incrementRate;
            }
            else if(differY>0)
            {
                //add
                ptList.add(
                        ptList.get(ptList.size()-2)-incrementRate
                );
                differY -=incrementRate;
            }
            else
            {
                //maintaining current value
                ptList.add(
                        ptList.get(ptList.size()-2)
                );
            }
            //set to zero if less than increment rate
            if(Math.abs(differY)<incrementRate)differY=0;


            //don't add more dot point if the differ is less than 2
            //if(Math.abs(flOriDifferX)>=2 || Math.abs(flOriDifferY)>=2)
            if(true)
            {

                float flReadyX = ptList.get(ptList.size() - 2), flReadyY = ptList.get(ptList.size() - 1);
                //remove the last two float pts, else later the end point will be changed to new inserted point
                //and start at wrong previous point, will add it back at the end later
                ptList.remove(ptList.size() - 1);
                ptList.remove(ptList.size() - 1);
                float tempX, tempY;
                //now we add additional dots, but have to exclude the next point to prevent duplicate
                *//**previous column**//*
                if (!MatchNextPoint(flReadyX - 1, flReadyY + 1, ptList)) {
                    tempX = flReadyX - 1;
                    tempY = flReadyY + 1;
                    Log.d("smooth line", "x-1=" + tempX + " , Y+1=" + tempY);
                    ptList.add(flReadyX - 1);
                    ptList.add(flReadyY + 1);
                }
                if (!MatchNextPoint(flReadyX - 1, flReadyY, ptList)) {
                    tempX = flReadyX - 1;
                    tempY = flReadyY;
                    Log.d("smooth line", "x-1=" + tempX + " , Y=" + tempY);
                    ptList.add(flReadyX - 1);
                    ptList.add(flReadyY);
                }
                if (!MatchNextPoint(flReadyX - 1, flReadyY - 1, ptList)) {
                    tempX = flReadyX - 1;
                    tempY = flReadyY - 1;
                    Log.d("smooth line", "x-1=" + tempX + " , Y-1=" + tempY);
                    ptList.add(flReadyX - 1);
                    ptList.add(flReadyY - 1);
                }
                *//**middle column**//*
                if (!MatchNextPoint(flReadyX, flReadyY + 1, ptList)) {
                    tempX = flReadyX;
                    tempY = flReadyY + 1;
                    Log.d("smooth line", "x=" + tempX + " , Y+1=" + tempY);
                    ptList.add(flReadyX);
                    ptList.add(flReadyY + 1);
                }
                if (!MatchNextPoint(flReadyX, flReadyY - 1, ptList)) {
                    tempX = flReadyX;
                    tempY = flReadyY - 1;
                    Log.d("smooth line", "x=" + tempX + " , Y-1=" + tempY);
                    ptList.add(flReadyX);
                    ptList.add(flReadyY - 1);
                }

                *//**next column**//*
                if (!MatchNextPoint(flReadyX + 1, flReadyY + 1, ptList)) {
                    tempX = flReadyX + 1;
                    tempY = flReadyY + 1;
                    Log.d("smooth line", "x+1=" + tempX + " , Y+1=" + tempY);
                    ptList.add(flReadyX + 1);
                    ptList.add(flReadyY + 1);
                }
                if (!MatchNextPoint(flReadyX + 1, flReadyY, ptList)) {
                    tempX = flReadyX + 1;
                    tempY = flReadyY;
                    Log.d("smooth line", "x+1=" + tempX + " , Y=" + tempY);
                    ptList.add(flReadyX + 1);
                    ptList.add(flReadyY);
                }
                if (!MatchNextPoint(flReadyX + 1, flReadyY - 1, ptList)) {
                    tempX = flReadyX + 1;
                    tempY = flReadyY - 1;
                    Log.d("smooth line", "x+1=" + tempX + " , Y-1=" + tempY);
                    ptList.add(flReadyX + 1);
                    ptList.add(flReadyY - 1);
                }
                ptList.add(flReadyX);
                ptList.add(flReadyY);
            }





        }

        //remove the 1st two duplicates
        ptList.remove(0);
        ptList.remove(0);

        //copy to new array

       float[] tempPoints = new float[ptList.size()+points.length];

        for(int i=0;i<tempPoints.length;i++)
        {
            if(i<points.length) {
                tempPoints[i] = points[i];
            }
            else
            {
                tempPoints[i] = ptList.get(0);
                ptList.remove(0);//remove after add
            }
        }

        points = tempPoints;*/
    }
    private boolean MatchNextPoint(float x,float y,ArrayList<Float>lst)
    {
        /*if(lst.size()>1)
        {
            //still not the end of list yet
            if(x==lst.get(0)&&y==lst.get(1))return true;
        }*/
        return false;
    }
    @Override
    protected void onDraw(Canvas canvas)
    {

        super.onDraw(canvas);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //float strokeWidth = 4f;//paint.getStrokeWidth();

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(6);
        //paint.setStrokeWidth(common.Utility.DP2Pixel(StrokeWidth, getContext()));
        paint.setColor(getResources().getColor(R.color.black));


        //draw grid line on screen, if is in design mode
        if(floorPlanMode!= Enum.FloorPlanMode.select) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int w = metrics.widthPixels;
            int h = metrics.heightPixels;

            Paint mGridPaint = new Paint();
            mGridPaint.setStrokeWidth(0.5f);
            int gridSize = 40;
            for (int i = 0; i < w; i += gridSize) {

                mGridPaint.setColor(getResources().getColor(R.color.transparent_selected_row_green));

                mGridPaint.setStrokeWidth(StrokeWidth);
                mGridPaint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(i, 0, i, h, mGridPaint);
            }
            for (int i = 0; i < h; i += gridSize) {

                mGridPaint.setColor(getResources().getColor(R.color.transparent_selected_row_green));

                mGridPaint.setStrokeWidth(StrokeWidth);
                mGridPaint.setStyle(Paint.Style.STROKE);
                //mGridPaint.setPathEffect(new DashPathEffect(new float[]{20,20,20,20}, 0));
                canvas.drawLine(0, i, w, i, mGridPaint);
            }
        }
        //draw scribble

        for(int i=0;i<floorPlanScribbles.size();i++) {
            float[] pts = floorPlanScribbles.get(i).GetFirst();
            float x=-1,y =-1;
            Path scribblesPath = new Path();
            scribblesPath.moveTo(pts[0], pts[1]);
            //starting of this scribble object
            //scribblesPath.moveTo(pts[0], pts[1]);

            for(int j=2;j<pts.length;j++)
            {
                if((j & 1)==1)
                {
                    //odd Y axis
                    y = pts[j];
                }
                else
                {
                    //even X axis
                    x = pts[j];
                }
                if(x>-1 && y>-1)
                {

                    //scribblesPath.quadTo(pts[j - 2], pts[j - 1], (pts[j - 2] + x) / 2, (pts[j - 1] + y) / 2);
                    scribblesPath.lineTo(x,y);

                    //scribblesPath.reset();
                    x= -1;
                    y= -1;
                }
            }
            //scribblesPath.lineTo(pts[pts.length-2],pts[pts.length-1]);
            canvas.drawPath(scribblesPath,paint);


        }
        if(tempPath!=null && !blnRemoveLastDrawnPath)
        {
            canvas.drawPath(tempPath, paint);//draw what ever the user is drawing on screen now, else will be showing dot at 1st
        }
        else if(tempPath!=null && blnRemoveLastDrawnPath) {
            //use clear paint to remove drawn path
            Paint delPaint = new Paint();
            delPaint.setColor(0x00000000);
            delPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            delPaint.setAlpha(0x00);
            delPaint.setAntiAlias(true);
            delPaint.setDither(true);
            delPaint.setStyle(Paint.Style.STROKE);
            delPaint.setStrokeJoin(Paint.Join.ROUND);
            delPaint.setStrokeCap(Paint.Cap.ROUND);
            delPaint.setStrokeWidth(paint.getStrokeWidth());
            canvas.drawPath(tempPath, delPaint);
            blnRemoveLastDrawnPath = false;
            tempPath.reset();
            tempPath = null;

        }
        /*for(int i=0;i<floorPlanScribbles.size();i++) {

            canvas.drawPoints(floorPlanScribbles.get(i).GetFirst(), paint);
        }*/



        //draw line
        for(int i=0;i<floorPlanLines.size();i++)
        {
            canvas.drawLines(floorPlanLines.get(i), paint);

        }

        //draw arc
        for(int i=0;i<floorPlanArces.size();i++)
        {
            DrawArc(canvas,floorPlanArces.get(i),paint);
            /*float d = CalculateDistance(new float[]{floorPlanArces.get(i)[0],floorPlanArces.get(i)[1],
                    floorPlanArces.get(i)[2],
                    floorPlanArces.get(i)[3]});
            final RectF oval = new RectF(floorPlanArces.get(i)[0]-d,//differX,
                                         floorPlanArces.get(i)[1]-d,//differY,
                                         floorPlanArces.get(i)[0]+d,//differX,
                                         floorPlanArces.get(i)[1]+d);//differY);





            Point center = new Point(Math.round(floorPlanArces.get(i)[0]),Math.round(floorPlanArces.get(i)[1]));
            Point p1 = new Point(Math.round(floorPlanArces.get(i)[2]),Math.round(floorPlanArces.get(i)[3]));
            Point p2 = new Point(Math.round(floorPlanArces.get(i)[4]),Math.round(floorPlanArces.get(i)[5]));

            double startAngle = CalculateSweepAngle(center,p1);
            double sweepAngle = CalculateSweepAngle(center,p2);
            if(startAngle>sweepAngle)
            {

                sweepAngle = startAngle-sweepAngle;
                sweepAngle =Math.abs(360-sweepAngle);
            }
            else {
                sweepAngle -= startAngle;
            }
            //tvMessage.setText("start angle=["+startAngle+"], sweep angle=["+sweepAngle+"]"+" raw sweep angle=["+(sweepAngle+startAngle)+"]");
            Path arcPath = new Path();
            arcPath.arcTo(oval, (float)startAngle, (float)sweepAngle, true);
            canvas.drawPath(arcPath,paint);*/

            //canvas.drawArc(oval,(float)startAngle,(float)Math.abs(sweepAngle),true,paint);



        }

        //draw table object on screen
        for(int i=0;i<floorPlanTable.size();i++)
        {

            if(floorPlanTable.get(i).GetFirst().length==0)continue;
            Duple<float[],Duple<String,String>> dupleTableObj = floorPlanTable.get(i);
            paint.setStrokeWidth(StrokeWidth);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.orange));
            canvas.drawCircle(dupleTableObj.GetFirst()[0], dupleTableObj.GetFirst()[1], tableObjectRadius, paint);

            paint.setColor(getResources().getColor(R.color.white));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(dupleTableObj.GetFirst()[0], dupleTableObj.GetFirst()[1], 28f, paint);

            paint.setStrokeWidth(1);
            if(floorPlanMode== Enum.FloorPlanMode.select)
            {
                Duple<String,Boolean>[] tableOccupiedStatus = common.myCartManager.GetTableStatuses();
                //change text color if current table has been occupied
                for(int j=0;j<tableOccupiedStatus.length;j++)
                {
                    if (dupleTableObj.GetSecond().GetFirst().equalsIgnoreCase(
                            tableOccupiedStatus[j].GetFirst()))
                    {
                        if(tableOccupiedStatus[j].GetSecond())
                        {
                            paint.setColor(getResources().getColor(R.color.black));
                        }
                        else
                        {
                            paint.setColor(getResources().getColor(R.color.divider_grey));
                        }
                    }
                }

            }
            else
            {paint.setColor(getResources().getColor(R.color.black));}
            paint.setTextSize(15);
            paint.setLinearText(true);
            Rect bounds = new Rect();
            paint.getTextBounds(floorPlanTable.get(i).GetSecond().GetSecond(),0,floorPlanTable.get(i).GetSecond().GetSecond().length(),bounds);
            //tvMessage.setText("text bound width is "+bounds.width()+" draw start at: "+floorPlanTable.get(i).GetFirst()[0]+tableLabelXOffset);
            float startIndex = ((50-bounds.width())/2)+floorPlanTable.get(i).GetFirst()[0]+tableLabelXOffset;
            canvas.drawText(floorPlanTable.get(i).GetSecond().GetSecond(),startIndex,floorPlanTable.get(i).GetFirst()[1]+tableLabelYOffset,paint);
            //canvas.drawText(floorPlanTable.get(i).GetSecond(),floorPlanTable.get(i).GetFirst()[0]+tableLabelXOffset,floorPlanTable.get(i).GetFirst()[1]+tableLabelYOffset,paint);
            //canvas.restore();

        }
        paint.setStrokeWidth(StrokeWidth);
        //canvas.restore();
        paint.setColor(getResources().getColor(R.color.black));
        //draw selected object if any
        if(selectedObject== Enum.SelectedFloorPlanObject.scribble && selectedPoints.length>0)
        {
            paint.setColor(getResources().getColor(R.color.red));
            canvas.drawPoints(selectedPoints,paint);
        }
        else if(selectedObject== Enum.SelectedFloorPlanObject.line && selectedPoints.length>0)
        {
            paint.setColor(getResources().getColor(R.color.red));//ShowMessage("selected point to draw","x1 "+selectedPoints[0]+", x2 "+selectedPoints[2]+" ,y1 "+selectedPoints[1]+" y2 "+selectedPoints[3]);
            canvas.drawLines(selectedPoints, paint);
        }
        else if(selectedObject== Enum.SelectedFloorPlanObject.arc && selectedPoints.length>0)
        {
            paint.setColor(getResources().getColor(R.color.red));
            DrawArc(canvas, selectedPoints, paint);
        }
        else if(selectedObject== Enum.SelectedFloorPlanObject.table && selectedPoints.length>0)
        {

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.red));
            canvas.drawCircle(selectedPoints[0], selectedPoints[1], tableObjectRadius, paint);
        }
        //rotate mode + touching object
        if(floorPlanMode== Enum.FloorPlanMode.rotate && centerPoint.length>0)
        {
            //draw center point

            //ReturnCenterPoint(selectedPoints);
            paint.setStrokeWidth(common.Utility.DP2Pixel(StrokeWidth,getContext()));
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(getResources().getColor(R.color.black));
            canvas.drawCircle(centerPoint[0],centerPoint[1],2,paint);
            //canvas.drawPoints(centerPoint,paint);
        }

        //create effect if is in line mode while waiting user to draw the end point
        paint.setStyle(Paint.Style.FILL);
        if(floorPlanMode== Enum.FloorPlanMode.line && points.length>0)
        {
            paint.setColor(GetEffectColorId(effectCounter));
            canvas.drawCircle(points[0],points[1],GetEffectCircleRadius(effectCounter--),paint);
            if(effectCounter==0)
            {
                effectCounter=defaultEffectCounterValue;

            }

            invalidate();
        }
        else if(floorPlanMode== Enum.FloorPlanMode.arc && points.length>0)
        {
            paint.setColor(GetEffectColorId(effectCounter));

            //center point
            canvas.drawCircle(points[0],points[1],GetEffectCircleRadius(effectCounter),paint);

            //radius point if any
            if(points.length>3)
            {
                canvas.drawCircle(points[2],points[3],GetEffectCircleRadius(effectCounter),paint);

                //draw guide line
                Paint mPaint = new Paint();
                mPaint.setColor(getResources().getColor(R.color.red));

                mPaint.setStrokeWidth(StrokeWidth);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setPathEffect(new DashPathEffect(new float[]{20,20,20,20}, 0));
                canvas.drawCircle(points[0],points[1],common.mathLib.CalculateDistance(points),mPaint);
            }


            effectCounter--;
            if(effectCounter==0)
            {
                effectCounter=defaultEffectCounterValue;
            }

            invalidate();
        }
        else {
            canvas.drawPoints(points, paint);
        }


    }
    private void DrawArc(Canvas canvas,float[] drawPoints,Paint paint)
    {
        float d = common.mathLib.CalculateDistance(new float[]{drawPoints[0],
                drawPoints[1],
                drawPoints[2],
                drawPoints[3]});
        final RectF oval = new RectF(drawPoints[0]-d,
                drawPoints[1]-d,
                drawPoints[0]+d,
                drawPoints[1]+d);

        Point center = new Point(Math.round(drawPoints[0]),Math.round(drawPoints[1]));
        Point p1 = new Point(Math.round(drawPoints[2]),Math.round(drawPoints[3]));
        Point p2 = new Point(Math.round(drawPoints[4]),Math.round(drawPoints[5]));

        double startAngle = common.mathLib.CalculateSweepAngle(center, p1);
        double sweepAngle = common.mathLib.CalculateSweepAngle(center, p2);
        if(startAngle>sweepAngle)
        {

            sweepAngle = startAngle-sweepAngle;
            sweepAngle =Math.abs(360-sweepAngle);
        }
        else {
            sweepAngle -= startAngle;
        }
        //tvMessage.setText("start angle=["+startAngle+"], sweep angle=["+sweepAngle+"]"+" raw sweep angle=["+(sweepAngle+startAngle)+"]");
        Path arcPath = new Path();
        arcPath.arcTo(oval, (float)startAngle, (float)sweepAngle, true);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(arcPath,paint);
    }
    /*private float CalculateDistance(float[] points)
    {
        float x = (points[2]-points[0]);
        x*=x;
        float y = (points[3]-points[1]);
        y*=y;
        return (float)Math.sqrt(y+x);
    }*/
    /*private double CalculateSweepAngle(Point center,Point p1)
    {
        double dx = p1.x - center.x;
        // Minus to correct for coord re-mapping
        double dy = -(p1.y - center.y);

        double inRads = Math.atan2(dy,dx);

        // We need to map to coord system when 0 degree is at 3 O'clock, 270 at 12 O'clock
        if (inRads < 0)
            inRads = Math.abs(inRads);
        else
            inRads = 2*Math.PI - inRads;

        return Math.toDegrees(inRads);
    }*/
    private float GetEffectCircleRadius(int effect_counter)
    {

            return Float.parseFloat((fingerRadius-((fingerRadius/defaultEffectCounterValue)*(defaultEffectCounterValue-effect_counter)))+"");

    }
    private int GetEffectColorId(int effect_counter)
    {
        //begin with transparent
        if(effect_counter<=60 && effect_counter>=55)
        {
            return getResources().getColor(R.color.transparent_red_90_percent);
        }
        else if(effect_counter<=54 && effect_counter>=49)
        {
            return getResources().getColor(R.color.transparent_red_80_percent);
        }
        else if(effect_counter<=48 && effect_counter>=43)
        {
            return getResources().getColor(R.color.transparent_red_70_percent);
        }
        else if(effect_counter<=42 && effect_counter>=37)
        {
            return getResources().getColor(R.color.transparent_red_60_percent);
        }
        else if(effect_counter<=36 && effect_counter>=31)
        {
            return getResources().getColor(R.color.transparent_red_50_percent);
        }
        else if(effect_counter<=30 && effect_counter>=35)
        {
            return getResources().getColor(R.color.transparent_red_40_percent);
        }
        else if(effect_counter<=34 && effect_counter>=29)
        {
            return getResources().getColor(R.color.transparent_red_30_percent);
        }
        else if(effect_counter<=28 && effect_counter>=23)
        {
            return getResources().getColor(R.color.transparent_red_20_percent);
        }
        else if(effect_counter<=22 && effect_counter>=17)
        {
            return getResources().getColor(R.color.transparent_red_10_percent);
        }
        else
        {
            return getResources().getColor(R.color.red);
        }
    }
    public void LoadSaved()
    {
        //FloorPlan fp = new FloorPlan(getContext());

        boolean blnFlag = common.floorPlan.LoadFloorPlan();
        if(blnFlag) {
            floorPlanArces = common.floorPlan.GetArcObjects();
            floorPlanLines = common.floorPlan.GetLineObjects();
            floorPlanScribbles = common.floorPlan.GetScribbleObjects();
            floorPlanTable = common.floorPlan.GetTableObjects();
            invalidate();
        }
        else
        {
            common.Utility.ShowMessage("Load Floor Plan Error",common.floorPlan.GetErrorMessage(),getContext(),R.drawable.exclaimation);
        }
    }
    public boolean Save()
    {
        if(blnTableNameEditing)
        {
            common.Utility.ShowMessage("Save","You have unsaved table, please provide a table label before saving.",getContext(),R.drawable.no_access);
            return false;
        }
        blnDirty = false;
        /*FloorPlan fp = new FloorPlan(getContext());
        fp.SetArcObjects(floorPlanArces);
        fp.SetLineObjects(floorPlanLines);
        fp.SetScribbleObjects(floorPlanScribbles);
        fp.SetTableObjects(floorPlanTable);*/
        common.floorPlan.SetArcObjects(floorPlanArces);
        common.floorPlan.SetLineObjects(floorPlanLines);
        common.floorPlan.SetScribbleObjects(floorPlanScribbles);
        common.floorPlan.SetTableObjects(floorPlanTable);
        boolean blnFlag =  common.floorPlan.Save();
        if(!blnFlag){common.Utility.ShowMessage("Save Error", common.floorPlan.GetErrorMessage(),getContext(),R.drawable.exclaimation);}
        else
        {
            Toast.makeText(getContext(), "Saved.", Toast.LENGTH_SHORT).show();
        }
        return blnFlag;
    }


    public void ShowMessage(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
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
}
