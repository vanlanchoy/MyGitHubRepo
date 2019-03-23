package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.math.BigDecimal;
import java.util.ArrayList;

import tme.pos.BusinessLayer.DailyChartModel;
import tme.pos.BusinessLayer.common;
import tme.pos.R;

/**
 * Created by vanlanchoy on 12/20/2015.
 */
public class Graph extends RelativeLayout {
    int height;
    int width;
    float max;
    //float highestValue=0f;
    //float lowestValue=0f;
    int highestValueIndex=-1;
    boolean blnShowSelectedDotEffect=false;
    final int defaultEffectCounterValue=10;
    final int defaultDelayValue=4;
    int delayValue=defaultDelayValue;
    int effectCounter=defaultEffectCounterValue;
    ArrayList<Float> valueCoordinates;
    ArrayList<Float> dayCoordinates;
    ArrayList<Pair<Float,Float>> dotObjects;

    //vertical label line
    float verticalTopOffset;
    float verticalBottomOffset;
    float verticalLeftOffset;

    //horizontal label line
    float horizontalTopOffset;
    float horizontalLeftOffset;
    float horizontalRightOffset;

    String[] codes={"#0B610B","#088A08","#04B404","#01DF01","#00FF00","#2EFE2E"};

    ArrayList<Pair<Integer,Float>> data;

    /**animation valuable**/
    int monthIndex=0;
    int subLoopIndex=0;
    int subFrame=1000;
    float animationLastX = 0;
    float animationLastY = 0;


    public Graph(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public Graph(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

    }

    public Graph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
    }
    public void Draw(ArrayList<Pair<Integer,Float>> model)
    {
        data = model;
        max=0;//reset
        monthIndex =0;
        subLoopIndex = 0;
        CalculateDrawingObjectPoints();

    }
    private void HitTest(float x,float y)
    {
        int offset=20;
        if(dotObjects==null)return;
        for(int i=0;i<dotObjects.size();i++)
        {
            if(x>=dotObjects.get(i).first-offset && x<=dotObjects.get(i).first+3)
            {
                if(y>=dotObjects.get(i).second-offset && y<=dotObjects.get(i).second+3)
                {
                    highestValueIndex = i;
                    blnShowSelectedDotEffect = true;
                    break;
                }
            }
        }
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
                HitTest(ev.getX(),ev.getY());
                break;
        }
        return super.onTouchEvent(ev);
    }
    public void CalculateDrawingObjectPoints()
    {
        if(data==null)return;

        dotObjects = new ArrayList<Pair<Float, Float>>();

        //vertical label line
        verticalTopOffset=common.Utility.DP2Pixel(50,getContext());
        verticalBottomOffset=height-common.Utility.DP2Pixel(20,getContext());
        verticalLeftOffset =common.Utility.DP2Pixel(100,getContext());

        //horizontal label line
        horizontalTopOffset =height-common.Utility.DP2Pixel(50,getContext());
        horizontalLeftOffset =common.Utility.DP2Pixel(50,getContext());
        horizontalRightOffset =width-common.Utility.DP2Pixel(50,getContext());

        float drawableHeight = horizontalTopOffset-verticalTopOffset;

        for(int i=0;i<data.size();i++)
        //for(int i=0;i<monthIndex+1;i++)
        {
            if(data.get(i).second>max)
            {
                max = data.get(i).second;
            }

        }

        String strMax = new BigDecimal(max).setScale(2,BigDecimal.ROUND_HALF_UP)+"";
        if(strMax.length()==4)
        {
            max+=1;
        }
        else
        {
            int length = (strMax.length()-4);//take out the floating point and
            String strZeros = "1";
            while(length-1>=strZeros.length()){strZeros+="0";}

            max+=Integer.parseInt(strZeros);
            strMax = new BigDecimal(max).setScale(2,BigDecimal.ROUND_HALF_UP)+"";
            int tempLength = strMax.length();
            strZeros+=".00";
            strMax = strMax.substring(0,strMax.length()-strZeros.length()+1);
            while(tempLength-3>strMax.length())strMax+="0";
            //strMax+=".00";
            max = Float.parseFloat(strMax);
        }



        //value per dip
        float dipPerValue = (horizontalTopOffset-verticalTopOffset)/max;
        valueCoordinates = new ArrayList<Float>();

        //date label coordinates
        float dipPerDay = (horizontalRightOffset-verticalLeftOffset)/data.size();
        dayCoordinates = new ArrayList<Float>();

        for(int i=0;i<data.size();i++)
        //for(int i=0;i<monthIndex+1;i++)
        {
            float temp = (data.get(i).second*dipPerValue);
            if(temp==0) {
                valueCoordinates.add(-1f);//assign negative value in order to skip
                //valueCoordinates.add(verticalTopOffset+drawableHeight);//start from verticalTopOffset but not zero

            }
            else {
                valueCoordinates.add(verticalTopOffset+(drawableHeight-temp) );
            }
            float tempResult = (i * dipPerDay) + verticalLeftOffset + (dipPerDay-common.Utility.DP2Pixel(11,getContext()));
            //float tempResult = (i * dipPerDay) + horizontalLeftOffset + (dipPerDay-common.Utility.DP2Pixel(11,getContext()));
            dayCoordinates.add(tempResult);


        }

        invalidate();

       /* monthIndex++;
        if(monthIndex != data.size()) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CalculateDrawingObjectPoints();
                }
            }, 300);

        }*/
    }
    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        height=yNew;
        width = xNew;

        CalculateDrawingObjectPoints();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DrawLines(canvas);
        if(monthIndex<12) {
            //draw with animation
            DrawDataAnimated(canvas);
        }
        else {
            //draw complete graph
            DrawData(canvas);
        }
    }
    private void DrawLines(Canvas canvas)
    {
        float StrokeWidth=1;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(common.Utility.DP2Pixel(StrokeWidth, getContext()));
        paint.setColor(getResources().getColor(R.color.black));

        canvas.drawLine(verticalLeftOffset, verticalTopOffset, verticalLeftOffset, verticalBottomOffset, paint);//vertical line

        canvas.drawLine(verticalLeftOffset, horizontalTopOffset, horizontalRightOffset, horizontalTopOffset, paint);//horizontal line
    }
    private int GetEffectColorId(int effect_counter)
    {
        //begin with transparent
        if(effect_counter==10)
        {
            return getResources().getColor(R.color.transparent_red_90_percent);
        }
        else if(effect_counter==9)
        {
            return getResources().getColor(R.color.transparent_red_80_percent);
        }
        else if(effect_counter==8)
        {
            return getResources().getColor(R.color.transparent_red_70_percent);
        }
        else if(effect_counter==7)
        {
            return getResources().getColor(R.color.transparent_red_60_percent);
        }
        else if(effect_counter==6)
        {
            return getResources().getColor(R.color.transparent_red_50_percent);
        }
        else if(effect_counter==5)
        {
            return getResources().getColor(R.color.transparent_red_40_percent);
        }
        else if(effect_counter==4)
        {
            return getResources().getColor(R.color.transparent_red_30_percent);
        }
        else if(effect_counter==3)
        {
            return getResources().getColor(R.color.transparent_red_20_percent);
        }
        else if(effect_counter==2)
        {
            return getResources().getColor(R.color.transparent_red_10_percent);
        }
        else
        {
            return getResources().getColor(R.color.red);
        }
    }
    private float GetEffectCircleRadius()
    {
        if(delayValue--==0) {
            delayValue = defaultDelayValue;
            effectCounter--;
        }
        return effectCounter;


    }
    private void DrawSelectedEffect(Canvas canvas,int dotIndex)
    {
        if(dotIndex==-1)return;
        Paint paint = new Paint();
        paint.setColor(GetEffectColorId(effectCounter));
        canvas.drawCircle(dotObjects.get(dotIndex).first
                ,dotObjects.get(dotIndex).second
                ,GetEffectCircleRadius(),paint);
        if(effectCounter==0)
        {
            effectCounter=defaultEffectCounterValue;

        }

        //draw price label
        paint.setColor(Color.BLACK);
        Rect rect = new Rect();
        String strTemp = common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(data.get(dotIndex).second));
        paint.getTextBounds(strTemp,0,strTemp.length(),rect);
        canvas.drawText(strTemp,dotObjects.get(dotIndex).first-(rect.width()/2)
        ,dotObjects.get(dotIndex).second-rect.height(),paint);

        invalidate();
    }
    private void DrawSalesConnectingLine(final Canvas canvas, float lastX, float lastY,  int monthIndex,final Paint paint)
    {
        float previousMonthX = 0;
        float previousMonthY = 0;
        float targetX =0 ;
        float targetY =0 ;
        String strDay = (monthIndex + 1) + "";
        float textWidth=paint.measureText(strDay,0,strDay.length());
        targetX = dayCoordinates.get(monthIndex)+ (textWidth / 2);
       /* float temp=valueCoordinates.get(monthIndex);
        if(temp>-1f)
        {
            targetY = valueCoordinates.get(monthIndex);
        }*/
        targetY = valueCoordinates.get(monthIndex)<0?0:valueCoordinates.get(monthIndex);
        if(monthIndex>0)
        {
            previousMonthX = dayCoordinates.get(monthIndex-1)+ (textWidth / 2);
            previousMonthY = valueCoordinates.get(monthIndex-1);
        }
        float fractionX = (targetX-previousMonthX)/10;
        float fractionY = (targetY-previousMonthY)/10;
        float newX = fractionX+lastX;
        float newY = fractionY+lastY;

        if(targetX<=newX && targetY<=newY)
        {

            //get the new drawing target coordinate
            newX = targetX;
            newY = targetY;
            monthIndex++;

            if(monthIndex>11)return;
        }

        //draw
        canvas.drawLine(lastX,lastY,newX,newY,paint);
        final int newMonthIndex = monthIndex;
        final float innerLastX = newX;
        final float innerLastY = newY;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DrawSalesConnectingLine(canvas,innerLastX,innerLastY,newMonthIndex,paint);
            }
        }, 100);
        invalidate();
    }
    private void DrawDataAnimated(Canvas canvas)
    {
        if(monthIndex>11)return;
        float StrokeWidth=2;

        float highestValue=-1;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(common.Utility.DP2Pixel(StrokeWidth, getContext()));
        paint.setColor(getResources().getColor(R.color.black));

        if(dayCoordinates==null) {
            canvas.drawText("No data",width/2,height/2,paint);
        }
        else {
            dotObjects.clear();
            String strTemp="";//common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(max));
            Rect rect = new Rect();
            //float lastX=-1;
            //float lastY=-1;
            float textWidth=0;
            //for (int i = 0; i < dayCoordinates.size(); i++)
            //if(i!=dayCoordinates.size())
            for (int i = 0; i <=monthIndex; i++)
            {

                String strDay = (i + 1) + "";
                paint.setColor(Color.BLACK);
                //day label
//                if(blnFirstTime)
                canvas.drawText(strDay, dayCoordinates.get(i), horizontalTopOffset + common.Utility.DP2Pixel(11, getContext()), paint);
                if(dayCoordinates.get(i)>0)// && valueCoordinates.get(i)>-1)
                {
                    textWidth=paint.measureText(strDay,0,strDay.length());

                    paint.setColor(Color.parseColor(codes[1]));


                    //zero value/no sales
                    if(valueCoordinates.get(i)==-1)
                    {
                        dotObjects.add(new Pair<Float, Float>(dayCoordinates.get(i) + (textWidth / 2), horizontalTopOffset));
                        canvas.drawCircle(dayCoordinates.get(i) + (textWidth / 2), horizontalTopOffset, 4, paint);


                        //lastX = dayCoordinates.get(i) + (textWidth / 2);
                        //lastY=horizontalTopOffset;
                    }
                    else {
                        if(!blnShowSelectedDotEffect) {
                            if (highestValue == -1) {
                                highestValueIndex = i;
                                highestValue = valueCoordinates.get(i);
                            } else {
                                if (highestValue > valueCoordinates.get(i))//largest value plotted on top(smaller Y)
                                {
                                    highestValue = valueCoordinates.get(i);
                                    highestValueIndex = i;
                                }
                            }
                        }
                        dotObjects.add(new Pair<Float, Float>(dayCoordinates.get(i) + (textWidth / 2), valueCoordinates.get(i)));
                        canvas.drawCircle(dayCoordinates.get(i) + (textWidth / 2), valueCoordinates.get(i), 4, paint);


                        //lastX = dayCoordinates.get(i) + (textWidth / 2);
                        //lastY=valueCoordinates.get(i);
                    }

                    //draw connecting line
                    //draw complete if for previous month else just preforming sub drawing
                    int index =(monthIndex==i)?subLoopIndex: subFrame;
                    float offSetX = (i==0)?0:(dayCoordinates.get(i-1)+ (textWidth / 2));
                    float newX = ((dayCoordinates.get(i)+ (textWidth / 2))-offSetX)/subFrame;
                    float value = ( i==0||valueCoordinates.get(i-1)==-1 )?0:valueCoordinates.get(i-1);
                    float offSetY = (i==0)?0:value;
                    value = valueCoordinates.get(i)==-1?0:valueCoordinates.get(i);
                    float newY=(value-offSetY)/subFrame;
                    paint.setColor(Color.parseColor(codes[1]));
                    for(int j=0;j<index;j++)
                    {

                        newX = animationLastX+newX;

                        newY = animationLastY+newY;


                        canvas.drawLine(animationLastX,animationLastY,newX,newY,paint);
                        //canvas.drawLine(lastX,lastY,dayCoordinates.get(i) + (textWidth / 2),valueCoordinates.get(i),paint);
                        animationLastX = newX;
                        animationLastY = newY;
                    }


                }



            }


            //draw value label
            paint.setColor(Color.BLACK);

            //peak value label
            strTemp=common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(max));
            paint.getTextBounds(strTemp, 0, strTemp.length(), rect);
            canvas.drawText(strTemp, verticalLeftOffset - rect.width(), verticalTopOffset + (rect.height() / 2), paint);
            //half value label
            strTemp =common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(max/2f));
            paint.getTextBounds(strTemp, 0, strTemp.length(), rect);
            canvas.drawText(strTemp, verticalLeftOffset - rect.width(), (verticalTopOffset + horizontalTopOffset) / 2 + (rect.height() / 2), paint);



            //zero value label
            strTemp =common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(0));
            paint.getTextBounds(strTemp, 0, strTemp.length(), rect);
            canvas.drawText(strTemp, verticalLeftOffset - rect.width(), horizontalTopOffset + (rect.height() / 2), paint);


            //half value horizontal line
            paint.setStrokeWidth(common.Utility.DP2Pixel(1, getContext()));
            paint.setColor(getResources().getColor(R.color.transparent_grey_50_percent));
            canvas.drawLine(verticalLeftOffset, (verticalTopOffset + horizontalTopOffset) / 2, horizontalRightOffset, (verticalTopOffset + horizontalTopOffset) / 2, paint);

            //peak value horizontal line
            canvas.drawLine(verticalLeftOffset, verticalTopOffset,horizontalRightOffset, verticalTopOffset,paint);

            if(animationLastX>=(dayCoordinates.get(monthIndex)+ (textWidth / 2)) && animationLastY>=valueCoordinates.get(monthIndex))
            {
                subLoopIndex=0;
                monthIndex++;
            }
            else
            {
                subLoopIndex++;
            }

            //draw effect
            if(monthIndex>11)
            DrawSelectedEffect(canvas,highestValueIndex);

            invalidate();
/*
            //draw animated line
            paint.setStrokeWidth(common.Utility.DP2Pixel(StrokeWidth, getContext()));
            paint.setColor(Color.parseColor(codes[1]));
            DrawSalesConnectingLine(canvas,0,0,0,paint);*/
        }
    }
    private void DrawData(Canvas canvas)
    {
        float StrokeWidth=2;

        float highestValue=-1;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(common.Utility.DP2Pixel(StrokeWidth, getContext()));
        paint.setColor(getResources().getColor(R.color.black));

        if(dayCoordinates==null) {
            canvas.drawText("No data",width/2,height/2,paint);
        }
        else {
            dotObjects.clear();
            String strTemp="";//common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(max));
            Rect rect = new Rect();
            float lastX=-1;
            float lastY=-1;

            for (int i = 0; i < dayCoordinates.size(); i++)
            {

                String strDay = (i + 1) + "";
                paint.setColor(Color.BLACK);
                //day label
                canvas.drawText(strDay, dayCoordinates.get(i), horizontalTopOffset + common.Utility.DP2Pixel(11, getContext()), paint);
                if(dayCoordinates.get(i)>0)// && valueCoordinates.get(i)>-1)
                {
                    float textWidth=paint.measureText(strDay,0,strDay.length());

                    paint.setColor(Color.parseColor(codes[1]));


                    //zero value/no sales
                    if(valueCoordinates.get(i)==-1)
                    {
                        dotObjects.add(new Pair<Float, Float>(dayCoordinates.get(i) + (textWidth / 2), horizontalTopOffset));
                        canvas.drawCircle(dayCoordinates.get(i) + (textWidth / 2), horizontalTopOffset, 4, paint);
                        //draw connecting line
                        if(lastX>-1)
                        {
                            paint.setColor(Color.parseColor(codes[1]));
                            canvas.drawLine(lastX,lastY,dayCoordinates.get(i) + (textWidth / 2),horizontalTopOffset,paint);
                        }

                        lastX = dayCoordinates.get(i) + (textWidth / 2);
                        lastY=horizontalTopOffset;
                    }
                    else {
                        if(!blnShowSelectedDotEffect) {
                            if (highestValue == -1) {
                                highestValueIndex = i;
                                highestValue = valueCoordinates.get(i);
                            } else {
                                if (highestValue > valueCoordinates.get(i))//largest value plotted on top(smaller Y)
                                {
                                    highestValue = valueCoordinates.get(i);
                                    highestValueIndex = i;
                                }
                            }
                        }
                        dotObjects.add(new Pair<Float, Float>(dayCoordinates.get(i) + (textWidth / 2), valueCoordinates.get(i)));
                        canvas.drawCircle(dayCoordinates.get(i) + (textWidth / 2), valueCoordinates.get(i), 4, paint);

                        //draw connecting line
                        if(lastX>-1)
                        {
                            paint.setColor(Color.parseColor(codes[1]));
                            canvas.drawLine(lastX,lastY,dayCoordinates.get(i) + (textWidth / 2),valueCoordinates.get(i),paint);
                        }
                        lastX = dayCoordinates.get(i) + (textWidth / 2);
                        lastY=valueCoordinates.get(i);
                    }


                }



            }


            //draw value label
            paint.setColor(Color.BLACK);

            //peak value label
            strTemp=common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(max));
            paint.getTextBounds(strTemp, 0, strTemp.length(), rect);
            canvas.drawText(strTemp, verticalLeftOffset - rect.width(), verticalTopOffset + (rect.height() / 2), paint);
            //half value label
            strTemp =common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(max/2f));
            paint.getTextBounds(strTemp, 0, strTemp.length(), rect);
            canvas.drawText(strTemp, verticalLeftOffset - rect.width(), (verticalTopOffset + horizontalTopOffset) / 2 + (rect.height() / 2), paint);



            //zero value label
            strTemp =common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(0));
            paint.getTextBounds(strTemp, 0, strTemp.length(), rect);
            canvas.drawText(strTemp, verticalLeftOffset - rect.width(), horizontalTopOffset + (rect.height() / 2), paint);

           /* paint.setPathEffect(new DashPathEffect(new float[]{6, 20}, 0));
            paint.setColor(Color.LTGRAY);
            paint.setStyle(Paint.Style.STROKE);
            Path dashPath = new Path();
            dashPath.moveTo(verticalLeftOffset, verticalTopOffset);
            dashPath.lineTo(horizontalRightOffset, verticalTopOffset);
            //canvas.drawPath(dashPath, paint);

            dashPath = new Path();
            dashPath.moveTo(verticalLeftOffset, highestValue);
            dashPath.lineTo(horizontalRightOffset, highestValue);
            paint.setColor(Color.parseColor(codes[0]));
            //canvas.drawPath(dashPath, paint);*/

            //half value horizontal line
            paint.setStrokeWidth(common.Utility.DP2Pixel(1, getContext()));
            paint.setColor(getResources().getColor(R.color.transparent_grey_50_percent));
            canvas.drawLine(verticalLeftOffset, (verticalTopOffset + horizontalTopOffset) / 2, horizontalRightOffset, (verticalTopOffset + horizontalTopOffset) / 2, paint);

            //peak value horizontal line
            canvas.drawLine(verticalLeftOffset, verticalTopOffset,horizontalRightOffset, verticalTopOffset,paint);

            //draw effect
            DrawSelectedEffect(canvas,highestValueIndex);
        }

        //canvas.drawLine();
    }
}
