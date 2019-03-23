package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.R;

/**
 * Created by vanlan on 8/9/2015.
 */
public class PieChart extends RelativeLayout {
    HashMap<String,Duple<float[],Float>>pies;
    //ArrayList< Duple<String,float[]>> pies;
    float[] centerPoints;
    ArrayList<Pair<String,Integer>> salesData;
    String[] codes={"#0B610B","#088A08","#04B404","#01DF01","#00FF00","#2EFE2E"};
    //float radiusOff
    // set=50;
    float radius=200;
    public PieChart(Context context) {
        super(context);setWillNotDraw(false);
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

    }

    public PieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);setWillNotDraw(false);
    }
    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        if(this.centerPoints==null)
        {
            this.centerPoints = new float[]{xNew/2f,yNew/2f};
        }
        else
        {
            this.centerPoints[0] = xNew/2f;
            this.centerPoints[1] = yNew/2f;
        }
        CalculateDrawingObjectPoints();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {

        //if(pies==null)return;
        float StrokeWidth=2;

        super.onDraw(canvas);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //float strokeWidth = 4f;//paint.getStrokeWidth();

        paint.setStyle(Paint.Style.FILL);

        //paint.setStrokeWidth(4f);
        paint.setStrokeWidth(common.Utility.DP2Pixel(StrokeWidth, getContext()));


        Paint textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.black));
        textPaint.setTextSize(18);

        String strValue="No Data";
        Rect bounds = new Rect();
        if(pies==null)//no data label
        {
            paint.setColor(getResources().getColor(R.color.divider_grey));
            canvas.drawCircle(centerPoints[0], centerPoints[1],radius/* centerPoints[1] - radiusOffset*/, paint);

            //label
            paint.getTextBounds(strValue, 0, strValue.length(), bounds);
            canvas.drawText(strValue, centerPoints[0]-(bounds.width()/2)-20, centerPoints[1]+(bounds.height()/2), textPaint);
        }
        else {


            if(pies.size()==1)//only containing one kind of item
            {
                paint.setColor(Color.parseColor(codes[0]));
                canvas.drawCircle(centerPoints[0], centerPoints[1], radius, paint);
                //label
                String strItemName =(String)pies.keySet().toArray()[0];
                strValue=strItemName+" 100%";
                paint.getTextBounds(strValue, 0, strValue.length(), bounds);
                textPaint.setUnderlineText(true);
                canvas.drawText(strValue, centerPoints[0] - (bounds.width() / 2) - 20, centerPoints[1] + (bounds.height() / 2), textPaint);
            }
            else
            {

                //draw pie 1st, else will over write label
                int colorIndex=0;
                for (int i = 0; i < pies.size(); i++) {
                    /**the drawing is anti-clock wise**/
                    Duple<float[], Float> pie = pies.get(pies.keySet().toArray()[i]);
                    colorIndex = i % codes.length;//(i>codes.length-1)?i-codes.length:i;
                    paint.setColor(Color.parseColor(codes[colorIndex]));
                    DrawArc(canvas, pie.GetFirst(), paint);
                }
                for(int i = 0; i < pies.size(); i++) {

                    Duple<float[], Float> pie = pies.get(pies.keySet().toArray()[i]);
                    String strItemName = (String) pies.keySet().toArray()[i];



                    //label
                    strValue = strItemName + " " + pie.GetSecond().toString() + "%";
                    float[] tempPts =new float[]{pie.GetFirst()[2],pie.GetFirst()[3]
                    ,pie.GetFirst()[4],pie.GetFirst()[5]};

                    DrawPieLabel(tempPts//tempCenterPt
                    ,new float[]{pie.GetFirst()[0],pie.GetFirst()[1]}
                            ,strValue,canvas,textPaint);
                }
            }

        }


    }
    private void DrawPieLabel(float[] point,float[] centerPt,String strLabelText,Canvas canvas,Paint textPaint)
    {
        /**the drawing is anti-clock wise**/

        int lastIndexOf=strLabelText.lastIndexOf(" ");
        String strItemName = strLabelText.substring(0, lastIndexOf);
        String strPercentage = strLabelText.substring(lastIndexOf+1);
        Rect rectName = new Rect();
        Rect rectPercentage = new Rect();
        textPaint.getTextBounds(strItemName,0,strItemName.length(),rectName);
        textPaint.getTextBounds(strPercentage,0,strPercentage.length(),rectPercentage);

        float labelX = 0;
        float labelY=1;


        float[] startPt = common.mathLib.CalculateMidArc(radius,centerPt,new float[]{point[0],point[1]},new float[]{point[2],point[3]});
        float[] endPt = new float[2];
        if(point[2]<=centerPt[0] && point[0]>=centerPt[0])
        {
            /*if(point[0]-centerPt[0]==0)
            {

                    startPt[0] = centerPt[0] + (radius/2)-30;
                    endPt[0] = startPt[0] + 100;
                    endPt[1] = startPt[1];
                    labelX = 10;

            }
            else*/
            if(point[1]<point[3])
            {
                //from 0'o clock to 6 0'clock direction
                startPt[0] = centerPt[0] + (radius/2)-30;
                endPt[0] = startPt[0] + 100;
                endPt[1] = startPt[1];
                labelX = 10;
            }
            else if(point[1]>point[3])
            {
                //from 6'o clock to 12 0'clock direction
                startPt[0] = startPt[0] - 50;
                endPt[0] = startPt[0] - 100;
                endPt[1] = startPt[1];
                if (rectName.width() > rectPercentage.width()) {
                    labelX -= rectName.width() + 10;
                } else {
                    labelX -= rectPercentage.width() + 10;
                }
            }

           /* else if((centerPt[0]-point[2])>(point[0]-centerPt[0]))
            {
                //most area reside on left hand side, so label at left
                startPt[0] = startPt[0] - 50;
                endPt[0] = startPt[0] - 100;
                endPt[1] = startPt[1];
                if (rectName.width() > rectPercentage.width()) {
                    labelX -= rectName.width() + 10;
                } else {
                    labelX -= rectPercentage.width() + 10;
                }
            }*/
            else
            {
                startPt[0] = centerPt[0] + (radius/2)-30;
                endPt[0] = startPt[0] + 100;
                endPt[1] = startPt[1];
                labelX = 10;
            }


        }
       /* else if(point[0]<=startPt[0] && point[0]>=centerPt[0])
        {
            //end point X at 6-12 o'clock direction
            //to right
            //move the start point additional 50 to the right
            startPt[0] = startPt[0] + 50;
            endPt[0] = startPt[0] + 100;
            endPt[1] = startPt[1];
            labelX = 10;
        }*/
        else {
            if (startPt[0] < centerPt[0]) {
                //to left
                //move the start point additional 50 to the left
                startPt[0] = startPt[0] - 50;
                endPt[0] = startPt[0] - 100;
                endPt[1] = startPt[1];
                if (rectName.width() > rectPercentage.width()) {
                    labelX -= rectName.width() + 10;
                } else {
                    labelX -= rectPercentage.width() + 10;
                }
            } else if (startPt[0] > centerPt[0]) {
                //to right
                //move the start point additional 50 to the right
                startPt[0] = startPt[0] + 50;
                endPt[0] = startPt[0] + 100;
                endPt[1] = startPt[1];
                labelX = 10;

            } else {
                //180%
                if (point[1] > point[3]) {
                    //to left
                    //move the start point additional 50 to the left
                    startPt[0] = startPt[0] - 50;
                    endPt[0] = startPt[0] - 100;
                    endPt[1] = startPt[1];

                    if (rectName.width() > rectPercentage.width()) {
                        labelX -= rectName.width() + 10;
                    } else {
                        labelX -= rectPercentage.width() + 10;
                    }
                } else {
                    //to right
                    //move the start point additional 50 to the right
                    startPt[0] = startPt[0] + 50;
                    endPt[0] = startPt[0] + 100;
                    endPt[1] = startPt[1];
                    labelX = 10;

                }
            }
        }
        labelX += endPt[0];
        labelY +=endPt[1];
        float diff = Math.abs(rectName.width()-rectPercentage.width());
        float percentageNewLine=labelY+rectName.height()+10;
        if(rectName.width()>rectPercentage.width())
        {
            canvas.drawText(strItemName, labelX, labelY, textPaint);
            canvas.drawText(strPercentage, labelX+(diff/2), percentageNewLine, textPaint);
        }
        else
        {
            canvas.drawText(strItemName, labelX+(diff/2), labelY, textPaint);
            canvas.drawText(strPercentage, labelX, percentageNewLine, textPaint);
        }



        textPaint.setStrokeWidth(1);
        canvas.drawLine( startPt[0], startPt[1],endPt[0],endPt[1], textPaint);

    }
    private void CalculateDrawingObjectPoints()
    {
        if(centerPoints==null || salesData==null)return;
        int totalUnitCount=0;
        for(int i=0;i<salesData.size();i++)totalUnitCount+=salesData.get(i).second;
        if(totalUnitCount==0)
        {
            //let onDraw method handle this if no data
        }
        else {

            pies = new HashMap<String, Duple<float[], Float>>();

            int constant=180;

            if(salesData.size()==1)
            {
                //a circle with 100%

                pies.put(salesData.get(0).first,new Duple<float[], Float>(new float[]{},100f));
            }
            else
            {
                //int end=(salesData.size()>6)?6:salesData.size();
                int end=salesData.size();
                float lastX=-1;
                float lastY=-1;
                float degree =0;
                double PercentRemaining=100;
                float tempPercentage=0;
                for(int i=0;i<end;i++)
                {
                    //calculate degree
                    float tempValue =(float)(salesData.get(i).second)/(float)(totalUnitCount);
                    if(i>=end-1)
                    {
                        tempPercentage = new BigDecimal(PercentRemaining).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
                        PercentRemaining = 0;
                    }
                    else {
                        tempPercentage = new BigDecimal(tempValue * 100f).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                        PercentRemaining -= tempPercentage;
                        BigDecimal bdPercentageRemaining = new BigDecimal(PercentRemaining).setScale(2,BigDecimal.ROUND_HALF_UP);
                        String strTemp = bdPercentageRemaining.toString();
                        PercentRemaining =Float.parseFloat(strTemp);//bdPercentageRemaining.floatValue();

                    }
                    degree += tempValue*360f;
                    //store previous end point, use it as current start point
                    float[] endPt = common.mathLib.CalculateEndPoint(radius,-(degree)+constant,centerPoints);
                    if(lastX==-1)
                    {
                        pies.put(salesData.get(i).first
                                ,new Duple<float[], Float>(new float[]{centerPoints[0], centerPoints[1],centerPoints[0], centerPoints[1]-radius, endPt[0], endPt[1]}
                        ,tempPercentage));

                    }
                    else
                    {
                        pies.put(salesData.get(i).first
                                , new Duple<float[], Float>(new float[]{centerPoints[0], centerPoints[1], lastX, lastY, endPt[0], endPt[1]}
                                , tempPercentage));

                    }
                    lastX=endPt[0];
                    lastY=endPt[1];
                }
            }


        }
        invalidate();
    }
    public void Draw(ArrayList<Pair<String,Integer>> data)
    {
        salesData = data;
        CalculateDrawingObjectPoints();
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

        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(oval, (float) startAngle, (float) sweepAngle, true, paint);



        Paint linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(0.5f);
        linePaint.setColor(Color.BLACK);

        //draw border arc
        canvas.drawArc(oval, (float)startAngle, (float)sweepAngle, true, linePaint);
    }


}
