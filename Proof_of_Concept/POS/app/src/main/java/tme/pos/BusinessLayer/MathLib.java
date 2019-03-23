package tme.pos.BusinessLayer;

import android.graphics.Point;

/**
 * Created by kchoy on 12/16/2015.
 */
public class MathLib {
    public  float CalculateDistance(float[] points)
    {
        float x = (points[2]-points[0]);
        x*=x;
        float y = (points[3]-points[1]);
        y*=y;
        return (float)Math.sqrt(y+x);
    }
    public  double AngleBetweenLines(float[] line1,float[] line2)
    {
        double angle1 = Math.atan2(line1[1] - line1[3],
                line1[0] - line1[2]);
        double angle2 = Math.atan2(line2[1] - line2[3],
                line2[0] - line2[2]);
        return angle1-angle2;
    }
    public  double DegreeBetweenLines(float[] line1,float[] line2)
    {
       double result = AngleBetweenLines(line1, line2);
        if(result<0)result+=360;
        return result;
    }
    public  double CalculateSweepAngle(Point center,Point p1)
    {
        double dx = p1.x - center.x;
        // Minus to correct for coord re-mapping
        double dy = -(p1.y - center.y);

        double inRads = Math.atan2(dy, dx);

        // We need to map to coord system when 0 degree is at 3 O'clock, 270 at 12 O'clock
        if (inRads < 0)
            inRads = Math.abs(inRads);
        else
            inRads = 2*Math.PI - inRads;

        return Math.toDegrees(inRads);
    }
    public  float[] CalculateEndPoint(double radius,double degree,float[] centerPt)
    {
        double value = degree/180*Math.PI;
        return new float[]{(float)(centerPt[0] + radius*Math.sin(value)), (float)(centerPt[1] + radius*Math.cos(value))};
    }
    public float[] CalculateMidArc(double radius,float[] centerPt,float[] pt1,float[] pt2)
    {
        /**basic equation of a circle**/
        float[] midPt = new float[]{-1,-1};
       /* double r_square = radius*radius;
        if(pt1[0]==pt2[0])
        {
            float y_square =(pt1[1]+pt2[1])/2f;
            midPt[1]=y_square;//store the y coordinate

            y_square *= y_square;
            r_square-=y_square;
            double x=Math.sqrt(r_square);
            midPt[0]=(float)x;

        }
        else
        {
            float x_square = (pt1[0]+pt2[0])/2f;
            midPt[0] = x_square;//store the x coordinate

            x_square *=x_square;
            r_square-=x_square;
            double y = Math.sqrt(r_square);
            midPt[1]=(float)y;
        }*/

        midPt[0]=(pt1[0]+pt2[0])/2f;
        midPt[1]=(pt1[1]+pt2[1])/2f;
        return midPt;
    }
    public float[] ReturnCenterPoint(float[] points)
    {
        if(points.length==0)return null;

        float smallestX,largestX,smallestY,largestY;
        smallestX = points[0];
        largestX = smallestX;
        smallestY = points[1];
        largestY = smallestY;
        float[ ] centerPoint = new float[2];
        for(int i=2;i<points.length;i+=2)
        {
            if(smallestX>points[i])smallestX=points[i];

            if(largestX<points[i])largestX=points[i];

            if(smallestY>points[i])smallestY=points[i+1];

            if(largestY<points[i])largestY=points[i+1];
        }

        centerPoint[0] = (largestX+smallestX)/2f;
        centerPoint[1] = (largestY+smallestY)/2f;
        return centerPoint;
    }
}
