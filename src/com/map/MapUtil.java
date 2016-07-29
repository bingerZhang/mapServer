package com.map;

import com.util.MysqlConnector;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                List<Point> relist = reduce(newlist);
                if(relist.size()>0)lists.add(newlist);
                start = i+1;
            }
        }
        if(start < size-1){
            List<Point> newlist = pointList.subList(start,size);
            List<Point> relist = reduce(newlist);
            if(relist.size()>0)lists.add(newlist);
        }
        return lists;
    }

    public static List<Point> reduce(List<Point> points){
        List<Point> newlist = new ArrayList<>();
        int size = points.size();
        int fact = 1;
        if(size>200){
            fact=50;
        }else if(size > 50){
            fact=30;
        }else if(size>20){
            fact=20;
//        }else if(size>8){
//            fact=5;
        }else {
            fact=1;
        }
        if(size>1) {
            if (fact == 1) {
                newlist.add(points.get(0));
                newlist.add(points.get(size - 1));
            }else {
                for(int i = 0;i<size;i++){
                    if(i%fact==0)newlist.add(points.get(i));
                }
                if((size-1)%fact>0)newlist.add(points.get(size-1));
            }
        }
        return newlist;
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

    //粗略计算
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

    public static void syncPointInfoToDB(Map<String,List<Point>> roadInfo){
        String prefix = "UPDATE motorway SET wsp_id = CASE id ";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        boolean ret = true;
        for(String key: roadInfo.keySet()){
            List<Point> list = roadInfo.get(key);
            String sql = new String(prefix);
            int count = 0;
            String where = "where id in (";
            for(Point point: list){
                sql = sql + " WHEN " + point.getId() + " THEN " + point.getWs_id();
                count++;
                where = where + point.getId() + ",";
                if(count>500){
                    sql = sql + " END\n" ;
                    sql = sql + where.substring(0,where.length()-1)+ ")";
                    ret = mysqlConnector.updateSQL(sql);
                    sql= new String(prefix);
                    count=0;
                    where = "where id in (";
                    if(!ret)break;
                }

            }
            if (!ret) break;
            if(count>0) {
                sql = sql + " END\n";
                sql = sql + where.substring(0,where.length()-1)+ ")";
                ret = mysqlConnector.updateSQL(sql);
                if (!ret) break;
            }
            if (!ret) break;
        }
    }

    public static Map<String,List<Point>> loadRoadInfo(String table){
        Map<String,List<Point>> mapinfo = new HashMap<>();
        String s = "select * from " + table + " where name like 'G%'";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String name = rs.getString(2);
                String road_id = rs.getString(3);
                if(name==null||name.trim().length()==0)name = road_id.trim();
                Point point = new Point(rs.getInt(1),rs.getDouble(5),rs.getDouble(4),0d,rs.getInt(7));
                List<Point> points = null;
                if(mapinfo.containsKey(name)){
                    points = mapinfo.get(name);
                    points.add(point);
                }else {
                    points = new ArrayList<>();
                    points.add(point);
                    mapinfo.put(name, points);
                }
            }
            mysqlConnector.disconnSQL();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if(mysqlConnector !=null){
                mysqlConnector.close_query();
                mysqlConnector.disconnSQL();
            }
        }
        return mapinfo;
    }
    public static List<Point> loadWSInfo(){
        List<Point> wspinfo = new ArrayList<>();
        String s = "select * from town_location";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String name = rs.getString(2);
                String road_id = rs.getString(3);
                if(name==null||name.trim().length()==0)name = road_id.trim();
                Point point = new Point(rs.getInt(1),rs.getDouble(4),rs.getDouble(3),0d,rs.getInt(1));
                wspinfo.add(point);
            }
            mysqlConnector.disconnSQL();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if(mysqlConnector !=null)
            {
                mysqlConnector.close_query();
                mysqlConnector.disconnSQL();
            }
        }
        return wspinfo;
    }

    public static void main(String[] args) {
        Map<String,List<Point>> motorwayInfo = loadRoadInfo("motorway");
        System.out.println("motorway road count: " + motorwayInfo.size());

        List<Point> wspInfo = loadWSInfo();
        System.out.println("weather station count: " + wspInfo.size());

        for(String key:motorwayInfo.keySet()){
            List<Point> points = motorwayInfo.get(key);
            updatePointInfo(points,wspInfo);
        }
        syncPointInfoToDB(motorwayInfo);
        System.out.println("Done");

    }
}
