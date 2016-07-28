package com.map;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zlb on 2016/7/19.
 */
public class MapUtil {

    static double EARTH_RADIUS = 6378.137;//地球半径
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }
    public static double getDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000d;
        return s;
    }
    public static double getDistance(Point a,Point b){
        return getDistance(a.getPoint_y(),a.getPoint_x(),b.getPoint_y(),b.getPoint_x());
    }
    public static List<List<Point>> adjustment(List<Point> pointList,double maxdistance){
        int size = pointList.size();
        List<List<Point>> lists = new ArrayList<List<Point>>();
        int start = 0;
        for(int i=0;i<size-1;i++){
            Point a = pointList.get(i);
            Point b = pointList.get(i+1);
            if(getDistance(a,b)>maxdistance){
                List<Point> newlist = pointList.subList(start,i+1);
                lists.add(newlist);
                start = i+1;
            }
        }
        if(start < size-1)lists.add(pointList.subList(start,size));
        return lists;
    }

    public static void writeObject(File file,Object object) {
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStream);
            objectOutputStream.writeObject(object);
            outStream.close();
            System.out.println("Write Object successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Object readObject(File file){
        FileInputStream freader;
        Object obj = null;
        try {
            freader = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(freader);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static double distance2(Point a, Point b){
        Double ax = a.getPoint_x();
        Double ay = a.getPoint_y();
        Double bx = b.getPoint_x();
        Double by = b.getPoint_y();
        return (ax-bx)*(ax-bx)+(ay-by)*(ay-by);  // 取平方
    }

    public static Point nearByPoint(List<Point> points, Point a){
        Point ret = null;
        Double distance2 = 0d;
        boolean first = true;
        for(Point p:points){
            Double dt = distance2(p,a);
            if(first){
                distance2=dt;
                ret = p;
                first =false;
            }else if(distance2 > dt){
                distance2 = dt;
                ret = p;
            }
        }
        return ret;
    }

    public static void updatePointInfo(List<Point> pointList,List<Point> wsplist){
        int size = pointList.size();
        for(int i=0;i<size;i++){
            Point a = pointList.get(i);
            Point wsp = nearByPoint(wsplist,a);
            a.setWs_id(wsp.getId());
        }
    }


    public static void main(String[] args) {
        double lng1=115; //jingdu
        double lat1=40;  //weidu

        double lng2=119;
        double lat2=40;
        List<Point> list = new ArrayList<>();
        list.add(new Point(lat1,lng1,0d,1));
        list.add(new Point(lat2,lng2,0d,2));
        list.add(new Point(41d,117d,0d,3));
        list.add(new Point(42d,118d,0d,4));
        System.out.println("before:");
        for(Point p: list){
            System.out.println(p.toString());
        }
        List<Point> wsl = new ArrayList<>();
        wsl.add(new Point(42d,118d,0d,40));
        wsl.add(new Point(41.9d,118d,0d,50));
        wsl.add(new Point(41.5d,118d,0d,60));
        updatePointInfo(list,wsl);
        System.out.println("after:");
        for(Point p: list){
            System.out.println(p.toString());
        }

    }
}
