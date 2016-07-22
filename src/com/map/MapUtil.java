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
    public static void main(String[] args) {
        double lng1=116.829; //jingdu
        double lat1=40.595;  //weidu

        double lng2=116.830;
        double lat2=40.599;
        System.out.println("distance: " + getDistance(lat1,lng1,lat2,lng2));
    }
}
