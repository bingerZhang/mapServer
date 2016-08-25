package com.map;

import com.util.MysqlConnector;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
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
    public static String SELECT_RAIN = "select * from motorway_rain w ";
    public static String WHERE_RAIN_G = "where 1=1 ";
    public static String[] RAIN_LEVEL = {"","and w.rain_probability >= 40.0 and w.rain_probability < 60.0","and w.rain_probability >= 60.0 and w.rain_probability < 80.0","and w.rain_probability >= 80.0"};
    public static String SELECT_DATE = "";
    public static String SELECT_HOUR = "";
    public static java.text.NumberFormat nf = java.text.NumberFormat.getInstance();


//    create view motorway_rain as
//    select m.id,m.name,m.road_id,m.bd_lng,m.bd_lat,w.rain_probability
//    from motorway m inner join weather_by_town w on w.town_id = m.wsp_id where m.name like 'G%';

//    create view map_rain as
//    select h.id,h.name,h.PID,h.lng,h.lat,tr.rain_p0,tr.rain_p1,tr.rain_p2,tr.rain_p3,tr.rain_p4,tr.rain_p5,tr.rain_p6,tr.rain_p7,tr.rain_p8,tr.rain_p9,tr.rain_p10,tr.rain_p11
//    from highway h inner join town_rain tr on tr.wsp_id = h.wsp_id ;

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
        if(maxdistance==0d){
            Collections.sort(pointList,new PointComparator());
            lists.add(pointList);
            return lists;
        }
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
            fact=200;
        }else if(size > 300){
            fact=100;
        }else if(size>100){
            fact=40;
        }else if(size>50){
            fact=30;
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
        for (Point a : pointList) {
            Point wsp = nearByPoint(wsplist, a);
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
            if(count>100){
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
        String s = "select * from " + table + " where name like 'G%'and bd_lat<1"; // limit 55";
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
    public static Map<String,List<Point>> loadHighWayInfo(String table){
        Map<String,List<Point>> highWayinfo = new HashMap<>();
        String s = "select * from " + table ;
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String name = rs.getString("NAME");
                String lat_str = rs.getString("lat");
                String lng_str = rs.getString("lng");
                int pid = rs.getInt("PID");
                Point point = new Point(pid,Double.valueOf(lng_str),Double.valueOf(lat_str),0d,0);
                List<Point> points = null;
                if(highWayinfo.containsKey(name)){
                    points = highWayinfo.get(name);
                    points.add(point);
                }else {
                    points = new ArrayList<>();
                    points.add(point);
                    highWayinfo.put(name, points);
                }
            }
            mysqlConnector.disconnSQL();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(mysqlConnector !=null) {
                mysqlConnector.close_query();
                mysqlConnector.disconnSQL();
            }
        }
        return highWayinfo;
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
                String name = rs.getString("name");
                String road_id = rs.getString("road_id");
                if(name==null||name.trim().length()==0){
                    name = road_id.trim();
                }else {
                    int off = name.indexOf("-");
                    if(off>0)name = name.substring(0,off);
                }
                Point point = new Point(rs.getInt("id"),rs.getDouble("longitude"),rs.getDouble("latitude"),0d,rs.getDouble("bd_lng"),rs.getDouble("bd_lat"),0);
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
//        conn.setRequestProperty(header, value);
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

    public static String getResponse(URL url) throws IOException {
        HttpURLConnection conn = null;
        conn = openConnection(url);
        int statusCode = conn.getResponseCode();
        if(statusCode > 200)return null;
        int len = 0;
        byte[] temp = new byte[1024];
        InputStream is = conn.getInputStream();
        StringBuilder content = new StringBuilder();
        while ((len = is.read(temp)) != -1) {
            content.append(new String(temp, 0, len));
        }
        return content.toString();
    }

    public static void updatePoints(List<Point> lists, int offset, String points) throws IOException, JSONException {
        String url = "http://api.map.baidu.com/geoconv/v1/?coords=";
        String typeAndkey = "&from=1&to=6&ak=MGLOQ2LDO2W4w1ut42Y3kGPAxBk0G5N8";
        URL wurl = new URL(url + points + typeAndkey);
        String res = getResponse(wurl);
        JSONObject jsonObject = new JSONObject(res);
        JSONArray list = jsonObject.getJSONArray("result");
        for(int i=0;i<list.length();i++){
            JSONObject jso = list.getJSONObject(i);
            Point tmp = lists.get(offset+i);
            tmp.setBd_x(jso.getDouble("x"));
            tmp.setBd_y(jso.getDouble("y"));
        }
    }

    public static void updateBd_xy(String table){
        List<Point> motorwayPoints = loadPoints(table);
        System.out.println("motorway road count: " + motorwayPoints.size());

        String points = "";

        int count = 0;
        int current = 0;
        for(Point point:motorwayPoints){
            if(count>0&&count%90==0){
                if(count%9000==0){
                    System.out.println("count: " + count);
                }
                try {
                    points = points.substring(0,points.length()-1);
                    updatePoints(motorwayPoints,current,points);
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
        try {
            if(points.length()>1) {
                points = points.substring(0, points.length() - 1);
                updatePoints(motorwayPoints, current, points);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        syncPoint_bdx_ToDB(motorwayPoints);
        syncPoint_bdy_ToDB(motorwayPoints);
    }

    public static boolean lineToColumnForRain(){
        String sql = "select id,rain_probability,town_id from weather_by_town order by id";
        Map<Integer,List<Nodep>> weatherInfo = new HashMap<>();
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(sql);
        try {
            while (rs.next()) {
                int id = rs.getInt("id");
                int town_id = rs.getInt("town_id");
                String rain_probability = rs.getString("rain_probability");
                Nodep nodep = new Nodep(id,town_id,Double.valueOf(rain_probability));

                List<Nodep> points = null;
                if(weatherInfo.containsKey(town_id)){
                    points = weatherInfo.get(town_id);
                    if(points.size()<12)points.add(nodep);
                }else {
                    points = new ArrayList<>();
                    points.add(nodep);
                    weatherInfo.put(town_id, points);
                }
            }

            for(int idkey:weatherInfo.keySet()){
                List<Nodep> points = weatherInfo.get(idkey);
                Collections.sort(points,new NodepComparator());
                String insertsql = "insert into town_rain values(null,";
                insertsql = insertsql + idkey + ",";

                for(Nodep nodep:points){
                    insertsql = insertsql + nodep.getProbability() + ",";
                }
                insertsql = insertsql.substring(0,insertsql.length()-1);
                insertsql = insertsql + ")";
                mysqlConnector.insertSQL(insertsql);
            }
            mysqlConnector.disconnSQL();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(mysqlConnector !=null) {
                mysqlConnector.close_query();
                mysqlConnector.disconnSQL();
            }
        }
        return true;
    }

    public static void main(String[] args) {

//        List<Point> wspInfo = loadWSInfo();
//        System.out.println("weather station count: " + wspInfo.size());
//
//        for(String key:motorwayInfo.keySet()){
//            List<Point> points = motorwayInfo.get(key);
//            updatePointInfo(points,wspInfo);
//        }
//        syncPointInfoToDB(motorwayInfo);
//        updateBd_xy("motorway");
        lineToColumnForRain();
        System.out.println("Done");

    }
}
