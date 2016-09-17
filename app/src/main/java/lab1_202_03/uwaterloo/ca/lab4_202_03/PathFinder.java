package lab1_202_03.uwaterloo.ca.lab4_202_03;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jack on 7/11/16.
 */
public class PathFinder{
    ArrayList<PointF> pathPoints = new ArrayList<PointF>();

    public void findPath(PointF currentPoint, PointF destPoint){
        pathPoints.set(0, currentPoint);
        if(MainActivity.map.calculateIntersections(currentPoint, destPoint).isEmpty()) {
            pathPoints.set(1, destPoint);
            MainActivity.mv.setUserPath(pathPoints);
        } else {
//            if(currentPoint.x < destPoint.x - 0.5){
//
//            } else if(currentPoint.x > destPoint.x + 0.5){
//
//            }
        }
    }

    public void moveXDirection(){

    }

    public void moveYDirection(){

    }

    public void addPoint(PointF nextPoint){
        pathPoints.add(nextPoint);
    }
}
