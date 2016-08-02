package com.map;

import com.util.MysqlConnector;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by zlb on 2016/7/19.
 */
public class MapUtil {

    static double EARTH_RADIUS = 6378.137;//地球半径
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }
    public static String SELECT_RAIN = "select mw.name, mw.road_id, mw.latitude,mw.longitude , wbt.rain_probability from motorway mw inner join weather_by_town wbt on wbt.town_id = mw.wsp_id ";
    public static String WHERE_RAIN_G = "where mw.name like 'G%' ";
    public static String[] RAIN_LEVEL = {"and 1=1","and wbt.rain_probability >= 40.0 and wbt.rain_probability < 60.0","and wbt.rain_probability >= 60.0 and wbt.rain_probability < 80.0","and wbt.rain_probability >= 80.0"};
    public static String SELECT_DATE = "";
    public static String SELECT_HOUR = "";
    public static java.text.NumberFormat nf = java.text.NumberFormat.getInstance();


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
                if(relist.size()>0)lists.add(relist);
                start = i+1;
            }
        }
        if(start < size-1){
            List<Point> newlist = pointList.subList(start,size);
            List<Point> relist = reduce(newlist);
            if(relist.size()>0)lists.add(relist);
        }
        return lists;
    }

    public static List<Point> reduce(List<Point> points){
        List<Point> newlist = new ArrayList<>();
        int size = points.size();
        int fact = 1;
        if(size>500){
            fact=500;
//        }else if(size > 300){
//            fact=300;
//        }else if(size>50){
//            fact=30;
//        }else if(size>20){
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

    public static void syncPoint_bdx_ToDB(List<Point> list){
        String prefix = "UPDATE motorway SET bd_lng = CASE id ";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        nf.setGroupingUsed(false);
        boolean ret = true;

            String sql = new String(prefix);
            int count = 0;
            String where = "where id in (";
            for(Point point: list){
                sql = sql + " WHEN " + point.getId() + " THEN " + nf.format(point.getBd_x());
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
            if(count>0) {
                sql = sql + " END\n";
                sql = sql + where.substring(0,where.length()-1)+ ")";
                ret = mysqlConnector.updateSQL(sql);
            }
    }
    public static void syncPoint_bdy_ToDB(List<Point> list){
        String prefix = "UPDATE motorway SET bd_lat = CASE id ";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        boolean ret = true;
        nf.setGroupingUsed(false);
        String sql = new String(prefix);
        int count = 0;
        String where = "where id in (";
        for(Point point: list){
            sql = sql + " WHEN " + point.getId() + " THEN " + nf.format(point.getBd_y());
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
        if(count>0) {
            sql = sql + " END\n";
            sql = sql + where.substring(0,where.length()-1)+ ")";
            ret = mysqlConnector.updateSQL(sql);
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

    //geoconv to bd geo
    public static List<Point> loadPoints(String table){
        List<Point> mapinfo = new ArrayList<>();
        String s = "select * from " + table + " where name like 'G%'";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                Point point = new Point(rs.getInt(1),rs.getDouble(5),rs.getDouble(4),0d,rs.getInt(7));
                mapinfo.add(point);
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
                if(name==null||name.trim().length()==0){
                    name = road_id.trim();
                }else {
                    int off = name.indexOf("-");
                    if(off>0)name = name.substring(0,off);
                }
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

    public static Map<String, List<Point>> getRainInfoByLevel(String table,String date,String hour,int level){
        Map<String,List<Point>> motorwayInfo = new HashMap<>();
        String s = SELECT_RAIN + WHERE_RAIN_G + RAIN_LEVEL[level]; // + " and date and hour";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String name = rs.getString(2);
                String road_id = rs.getString(3);
                if(name==null||name.trim().length()==0){
                    name = road_id.trim();
                }else {
                    int off = name.indexOf("-");
                    if(off>0)name = name.substring(0,off);
                }
                Point point = new Point(rs.getInt(1),rs.getDouble(5),rs.getDouble(4),0d,rs.getInt(7));
                List<Point> points = null;
                if(motorwayInfo.containsKey(name)){
                    points = motorwayInfo.get(name);
                    points.add(point);
                }else {
                    points = new ArrayList<>();
                    points.add(point);
                    motorwayInfo.put(name, points);
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
        return motorwayInfo;
    }

    protected static HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(20 * 1000);
        conn.setReadTimeout(40 * 1000);
        conn.setUseCaches(false);
        conn.setRequestMethod("GET");
//        Headers headers = exchange.getRequestHeaders();
//        if (headers != null) {
//            for (String header : headers.keySet()) {
//                List<String> values = headers.get(header);
//                for (int i = 0; i < values.size(); i++) {
//                    String value = values.get(i);
//                    if (i == 0) {
//                        conn.setRequestProperty(header, value);
//                    } else {
//                        conn.addRequestProperty(header, value);
//                    }
//                }
//            }
//        }
        conn.setInstanceFollowRedirects(true);
        conn.connect();
        return conn;
    }
    public static Map toMap(String jsonString) throws IOException, JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        Map result = new HashMap();
        Iterator iterator = jsonObject.keys();
        String key = null;
        Object value = null;

        while (iterator.hasNext()) {
            key = (String) iterator.next();
            value = jsonObject.get(key);
            if (value instanceof JSONObject) {
//                System.out.println("key: " + key + "  value: "+ value.toString());
                value = toMap(value.toString());
            }
            result.put(key, value);
        }
        return result;
    }
    public static void main(String[] args) {
        List<Point> motorwayPoints = loadPoints("motorway");
        System.out.println("motorway road count: " + motorwayPoints.size());
        String url = "http://api.map.baidu.com/geoconv/v1/?coords=";
        String points = "";
        String typeAndkey = "&from=1&to=6&ak=MGLOQ2LDO2W4w1ut42Y3kGPAxBk0G5N8";
        int count = 0;
        int current = 0;
        for(Point point:motorwayPoints){
             if(count>0&&count%50==0){

                  try {
                      points = points.substring(0,points.length()-1);
                      URL wurl = new URL(url + points + typeAndkey);
                      HttpURLConnection conn = null;
                     conn = openConnection(wurl);
                     int statusCode = conn.getResponseCode();
                      if(statusCode > 200)continue;
                      int len = 0;
                      byte[] temp = new byte[1024];
                      InputStream is = conn.getInputStream();
                      StringBuilder content = new StringBuilder();
                      while ((len = is.read(temp)) != -1) {
                          content.append(new String(temp, 0, len));
                      }
                      String res = content.toString();
                      JSONObject jsonObject = new JSONObject(res);
                      JSONArray list = jsonObject.getJSONArray("result");
                      for(int i=0;i<list.length();i++){
                          JSONObject jso = list.getJSONObject(i);
                          Point tmp = motorwayPoints.get(current+i);
                          tmp.setBd_x(jso.getDouble("x"));
                          tmp.setBd_y(jso.getDouble("y"));
                      }
                      current = current + 50;
                      points = "";
                  }catch (Exception e){
                      e.printStackTrace();
                      break;
                  }
             }
            points = points + "" + point.getPoint_x()+ "," + point.getPoint_y() + ";";
            count++;
        }
        syncPoint_bdx_ToDB(motorwayPoints);
        syncPoint_bdy_ToDB(motorwayPoints);
//        List<Point> wspInfo = loadWSInfo();
//        System.out.println("weather station count: " + wspInfo.size());
//
//        for(String key:motorwayInfo.keySet()){
//            List<Point> points = motorwayInfo.get(key);
//            updatePointInfo(points,wspInfo);
//        }
//        syncPointInfoToDB(motorwayInfo);
        System.out.println("Done");

    }
}
