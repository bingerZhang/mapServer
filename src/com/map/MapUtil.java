package com.map;

import com.util.MysqlConnector;
import com.util.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
    public static String SELECT_MOTORWAY_RAIN = "select * from motorway_rain w ";
    public static String SELECT_HIGHWAY_RAIN = "select * from highway_rain w ";
    public static String WHERE_RAIN_G = "where 1=1 ";
    public static String[] RAIN_LEVEL = {"and w.rain_probability < 30.0","and w.rain_probability >= 30.0 and w.rain_probability < 60.0","and w.rain_probability >= 60.0"};
    public static String SELECT_DATE = "";
    public static String SELECT_HOUR = "";
    public static java.text.NumberFormat nf = java.text.NumberFormat.getInstance();

    public static String BAIDU_CONV_URL = "http://api.map.baidu.com/geoconv/v1/?coords=";
//    public static String AK = "&ak=MGLOQ2LDO2W4w1ut42Y3kGPAxBk0G5N8";
    public static String AK = "&ak=zsphMD5rbLpZNNYP5C73DKhq";

//    create view motorway_rain as
//    select m.id,m.name,m.road_id,m.bd_lng,m.bd_lat,w.rain_probability
//    from motorway m inner join weather_by_town w on w.town_id = m.wsp_id where m.name like 'G%';

//    create view map_rain as
//    select h.id,h.name,h.PID,h.lng,h.lat,tr.rain_p0,tr.rain_p1,tr.rain_p2,tr.rain_p3,tr.rain_p4,tr.rain_p5,tr.rain_p6,tr.rain_p7,tr.rain_p8,tr.rain_p9,tr.rain_p10,tr.rain_p11
//    from highway h inner join town_rain tr on tr.wsp_id = h.wsp_id ;

//    create view map_rain_test as
//    select h.id,h.name,h.PID,h.lng,h.lat,tr1.rain rain_p1,tr2.rain rain_p2
//    from
//            (highway h inner join gps_point_rain1 tr1 on tr1.gps_id = h.gps_id)
//    inner join gps_point_rain2 tr2 on tr2.gps_id = h.gps_id
//    ;

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



    public static void syncPointInfoToDB(String table,Map<String,List<Point>> roadInfo){
        String prefix = "UPDATE "+ table + " SET wsp_id = CASE PID ";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        boolean ret = true;
        for(String key: roadInfo.keySet()){
            List<Point> list = roadInfo.get(key);
            String sql = new String(prefix);
            int count = 0;
            String where = "where PID in (";
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
                    where = "where PID in (";
                    if(!ret){
                        System.out.println("updateSQL failed !");
                        break;
                    }
                }

            }
            if (!ret) break;
            if(count>0) {
                sql = sql + " END\n";
                sql = sql + where.substring(0,where.length()-1)+ ")";
                ret = mysqlConnector.updateSQL(sql);
                if (!ret) {
                    System.out.println("updateSQL failed !");
                    break;
                }
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
                Point point = new Point(rs.getInt(1),rs.getDouble(5),rs.getDouble(4),0d,rs.getInt(7),1);
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
                Point point = new Point(pid,Double.valueOf(lng_str),Double.valueOf(lat_str),0d,0,1);
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
                Point point = new Point(rs.getInt(1),rs.getDouble(5),rs.getDouble(4),0d,rs.getInt(7),1);
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
                Point point = new Point(rs.getInt(1),rs.getDouble(4),rs.getDouble(3),0d,rs.getInt(1),1);
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

    public static List<Point> loadpointInfo(){
        List<Point> hwpinfo = new ArrayList<>();
        String s = "select id,lat,lng from highway";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String pid = rs.getString(1);
                Point point = new Point(rs.getInt(1),rs.getDouble(2),rs.getDouble(3));
                hwpinfo.add(point);
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
        return hwpinfo;
    }

    // map key = (int)bd_lat + (int)bd_lng
    public static Map<String,List<Point>> loadgpspointInfo(){
        Map<String,List<Point>> gps_pinfo = new HashMap<>();
        String s = "select id,bd_lat,bd_lng from gps_mapping";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String pid = rs.getString(1);
                double bd_lat = rs.getDouble(2);
                double bd_lng = rs.getDouble(3);
                Point point = new Point(rs.getInt(1),bd_lat,bd_lng);
                String lat = "" + (int)bd_lat;
                String lng = "" + (int)bd_lng;
                String key = lat + lng;
                if(gps_pinfo.containsKey(key))
                {
                    List<Point> points = gps_pinfo.get(key);
                    points.add(point);
                }else
                {
                    List<Point> points = new ArrayList<>();
                    points.add(point);
                    gps_pinfo.put(key,points);
                }
            }
            mysqlConnector.disconnSQL();
            return gps_pinfo;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if(mysqlConnector !=null)
            {
                mysqlConnector.close_query();
                mysqlConnector.disconnSQL();
            }
        }
        return null;
    }
    // map key = "" + lat
    public static Map<String,List<Point>> loadgpspointInfo2(){
        Map<String,List<Point>> gps_pinfo = new HashMap<>();
        String s = "select id,gps_lat,gps_lng from gps_point_weather1";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String pid = rs.getString(1);
                double gps_lat = rs.getDouble(2);
                double gps_lng = rs.getDouble(3);
                Point point = new Point(rs.getInt(1),gps_lat,gps_lng);
                //String lat = "" + (int)bd_lat;
                //String lng = "" + (int)bd_lng;
                String key = "" + gps_lat;
                if(gps_pinfo.containsKey(key))
                {
                    List<Point> points = gps_pinfo.get(key);
                    points.add(point);
                }else
                {
                    List<Point> points = new ArrayList<>();
                    points.add(point);
                    gps_pinfo.put(key,points);
                }
            }
            mysqlConnector.disconnSQL();
            return gps_pinfo;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if(mysqlConnector !=null)
            {
                mysqlConnector.close_query();
                mysqlConnector.disconnSQL();
            }
        }
        return null;
    }
    // map key = "" + lat
    public static void syncgpspointInfo2db(int tableNum ,Map<String,List<Point>> gps_pinfo){
//        String deleteTableCMD = "truncate table gps_point_rain" + tableNum;
//        String prefix = "insert into gps_point_rain" + tableNum + "(gps_id,gps_lat,gps_lng,rain) value ";
        String prefix = "update gps_point_weather" + tableNum + " set rain=";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        DecimalFormat decimalFormat = new DecimalFormat(".0");
        int count = 0;
        String cmds = "";
        for(String key:gps_pinfo.keySet()){
            List<Point> points = gps_pinfo.get(key);
//            List<String> cmds = new ArrayList<>();
            for(Point point:points)
            {
                String sql = prefix + point.getRainfall() + " where gps_lat=" + point.getPoint_x() + " and gps_lng=" + point.getPoint_y() + ";";
//                sql =  sql + " (" + point.getId() + "," + point.getPoint_x() + ","+ point.getPoint_y() +","+ point.getRainfall() + "),";
                cmds = cmds + sql;
                count++;
                if(count > 100){
                    mysqlConnector.insertSQL(cmds);
                    cmds = "";
                    count = 0;
                }
            }
            if(count>100){
                mysqlConnector.insertSQL(cmds);
                cmds = "";
                count = 0;
            }
//            sql = sql.substring(0,sql.length()-1)+ ";";
        }
        if(count>0){
            mysqlConnector.insertSQL(cmds);
        }
        if(mysqlConnector !=null){
            mysqlConnector.disconnSQL();
        }
//        System.out.println("Update gps_point_rain" + tableNum + " DONE!");
    }

    public static int getLevelForGps(Double pp) {
        if (pp >= 15d)
        {
            return 2;
        }else if(pp>=5d){
            return 1;
        }else {
            return 0;
        }
    }

    public static int getLevel(Double pp) {
        if (pp >= 60d)
        {
            return 2;
        }else if(pp>=30d){
            return 1;
        }else {
            return 0;
        }
    }

    public static Map<String, List<Point>> getRainInfo(String table){
        Map<String,List<Point>> highwayInfo = new HashMap<>();
        String s = "select * from " + table ;//+ " where name like 'G6'"; // + " and date and hour";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String name = rs.getString("NAME");
                int pid = rs.getInt("PID");

                Point point = new Point(pid,rs.getDouble("lng"),rs.getDouble("lat"),0d,0d,0d,0,12);
                List<Point> points;
                int[] level = new int[12];
                String pp = "rain_p";
                for(int j=0;j<12;j++){
                    String rskey = pp + j;
                    double rp = rs.getDouble(rskey);
                    int level_p = getLevelForGps(rp);
                    level[j]=level_p;
                }
                point.setLevel(level);
                if(highwayInfo.containsKey(name)){
                    points = highwayInfo.get(name);
                    points.add(point);
                }else {
                    points = new ArrayList<>();
                    points.add(point);
                    highwayInfo.put(name, points);
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
        return highwayInfo;
    }
    public static Map<String, List<Point>> getRainInfo(int index,String table){
        Map<String,List<Point>> highwayInfo = new HashMap<>();
        String s = "select * from " + table ;
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String name = rs.getString("NAME");
                int pid = rs.getInt("PID");
                Point point = new Point(pid,rs.getDouble("lng"),rs.getDouble("lat"),0d,0d,0d,0,12);
                List<Point> points = null;
                int[] level = new int[12];
                String pp = "rain";
                double rp = rs.getDouble(pp);
                int level_p = getLevelForGps(rp);
                level[index]=level_p;
                point.setLevel(level);
                if(highwayInfo.containsKey(name)){
                    points = highwayInfo.get(name);
                    points.add(point);
                }else {
                    points = new ArrayList<>();
                    points.add(point);
                    highwayInfo.put(name, points);
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
        return highwayInfo;
    }

    public static Map<String, List<Point>> getRainInfoByLevel(String table,String date,String hour,int level){
        Map<String,List<Point>> highwayInfo = new HashMap<>();
        String s = SELECT_HIGHWAY_RAIN + WHERE_RAIN_G + RAIN_LEVEL[level]; // + " and date and hour";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                String name = rs.getString("NAME");
                int pid = rs.getInt("PID");

                Point point = new Point(pid,rs.getDouble("lng"),rs.getDouble("lat"),0d,0d,0d,0,1);
                List<Point> points = null;
                if(highwayInfo.containsKey(name)){
                    points = highwayInfo.get(name);
                    points.add(point);
                }else {
                    points = new ArrayList<>();
                    points.add(point);
                    highwayInfo.put(name, points);
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
        return highwayInfo;
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
//                update town_rain set rain_p0=0.0,rain_p1=1,rain_p2=2,rain_p3=3,rain_p4=4,rain_p5=5,rain_p6=6,rain_p7=7,rain_p8=8,rain_p9=9,rain_p10=10,rain_p11=11 where wsp_id = 1;
                String updatesql = "update town_rain set rain_p";
                if(points.size()>11){
                    for(int i=0;i<12;i++){
                         updatesql = updatesql + i + "=" + points.get(i).getProbability();
                        if(i<11)updatesql = updatesql + ",rain_p";
                    }
                    updatesql = updatesql + " where wsp_id = " + idkey;
                }
//                for(Nodep nodep:points){
//                    insertsql = insertsql + nodep.getProbability() + ",";
//                }
//                insertsql = insertsql.substring(0,insertsql.length()-1);
//                insertsql = insertsql + ")";
//                mysqlConnector.insertSQL(insertsql);
                mysqlConnector.updateSQL(updatesql);
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

    public static List<GpsPoint> getGpsPoints(){
        List<GpsPoint> gpsPoints = new ArrayList<>();
        String s = "select * from gps_mapping gp where gp.bd_lat=0 or gp.bd_lng=0";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        ResultSet rs = mysqlConnector.query(s);
        try {
            while (rs.next()) {
                int id = rs.getInt("id");
                double lat = rs.getDouble("gps_lat");
                double lng = rs.getDouble("gps_lng");
                GpsPoint gpsPoint = new GpsPoint(id,lat,lng);
                gpsPoints.add(gpsPoint);
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
        return gpsPoints;
    }

    public static List<BdPoint> gps_to_bd(List<GpsPoint> gpsPoints) throws Exception {
        List<BdPoint> bdPoints = new ArrayList<>();
        String sourcePoints = "";
        for(GpsPoint point: gpsPoints){
            sourcePoints = sourcePoints + "" + point.getLng()+ "," + point.getLat() + ";";
        }
        if(sourcePoints.length()>1)
        {
            sourcePoints = sourcePoints.substring(0,sourcePoints.length()-1);
        }else {return null;}
        URL wurl = new URL(BAIDU_CONV_URL + sourcePoints + AK);
        String res = getResponse(wurl);
        JSONObject jsonObject = new JSONObject(res);
        JSONArray list = jsonObject.getJSONArray("result");
        int ret_len = list.length();
        if(ret_len != gpsPoints.size()){
            System.out.println("[ERROR] something error, src_len not equal ret_len!");
            return null;
        }
        for(int i=0;i<ret_len;i++){
            JSONObject jso = list.getJSONObject(i);
            GpsPoint gpsPoint = gpsPoints.get(i);
            BdPoint bdPoint = new BdPoint();
            bdPoint.setGps_id(gpsPoint.getId());
            bdPoint.setLat(jso.getDouble("y"));
            bdPoint.setLng(jso.getDouble("x"));
            bdPoints.add(bdPoint);
        }
        return bdPoints;
    }
    public static void syncBdToGps(List<BdPoint> bdPoints){
        String prefix = "UPDATE gps_mapping SET ";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        nf.setGroupingUsed(false);
        boolean ret = true;

        String sql = "";
        for(BdPoint point: bdPoints){
            sql = sql + prefix + " bd_lat = " + nf.format(point.getLat()) + ",bd_lng = " +  nf.format(point.getLng()) + " where id = " + point.getGps_id()+";";
        }
        mysqlConnector.updateSQL(sql);
        mysqlConnector.disconnSQL();

    }
    public static void gps_to_bd_all() throws Exception {
        List<GpsPoint> gpsPoints = getGpsPoints();
        List<BdPoint> bdPoints = new ArrayList<>();
        int len  = gpsPoints.size();
        System.out.println("There are " + len + " gps points to process");
        if(len <= 0 ) {
            return;
        }
        int count = 0;
        List<GpsPoint> gpsPoints_tmp = new ArrayList<>();
        List<BdPoint> bdPoints_tmp ;
        for(int i=0; i< len;i++){
            GpsPoint gpsPoint = gpsPoints.get(i);
            gpsPoints_tmp.add(gpsPoint);
            count++;
            if(count==1 || i==len-1){
                bdPoints_tmp = gps_to_bd(gpsPoints_tmp);
                if(bdPoints_tmp !=null && bdPoints_tmp.size()>0)
                {
//                    bdPoints.addAll(bdPoints_tmp);
                    syncBdToGps(bdPoints_tmp);
                }
                count = 0;
                gpsPoints_tmp = new ArrayList<>();
            }
        }
    }
    public static void generateGpsPoint(){
        String prefix = "insert into gps_mapping(gps_lng,gps_lat) value ";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        DecimalFormat decimalFormat=new DecimalFormat(".0");
        int count =0;
        for(float lng = 70; lng < 140.1; lng = (float) (0.1 + lng )){
            String sql = prefix;
            for(float lat = 15; lat < 55; lat = (float)(lat + 0.1)){
                sql =  sql + " (" + decimalFormat.format(lng) + "," + decimalFormat.format(lat) + "),";
            }
            sql = sql.substring(0,sql.length()-1)+ ";";
            mysqlConnector.insertSQL(sql);
        }
        if(mysqlConnector !=null){
            mysqlConnector.disconnSQL();
        }
    }

    public static int getGpsPointId(Point point, Map<String,List<Point>> gps_pinfo){
        double bd_lat = point.getPoint_x();
        double bd_lng = point.getPoint_y();
        String lat = "" + (int)bd_lat;
        String lng = "" + (int)bd_lng;
        String key = lat + lng;
        List<Point> points = gps_pinfo.get(key);
        double minDistance = 10000.0;
        int gpsId = 0;
        double distance = 0;
        for(Point point1:points){
            double bd_lat1 = point1.getPoint_x();
            double bd_lng1 = point1.getPoint_y();
            distance = (bd_lat1 - bd_lat)*(bd_lat1 - bd_lat) + (bd_lng1-bd_lng)*(bd_lng1-bd_lng);
            if(distance < minDistance){
                minDistance = distance;
                gpsId = point1.getId();
            }
        }
        return gpsId;

    }
    public static void syncGpsIdToHighwayDB(List<Point> list){
        String prefix = "UPDATE highway SET gps_id = CASE id ";
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        nf.setGroupingUsed(false);
        boolean ret = true;

        String sql = new String(prefix);
        int count = 0;
        String where = "where id in (";
        for(Point point: list){
            sql = sql + " WHEN " + point.getId() + " THEN " + point.getGps_id();
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
    public static void main(String[] args) throws Exception {
//        generateGpsPoint();
   //      gps_to_bd_all();
        Map<String,List<Point>> gps_pinfo = loadgpspointInfo();
        List<Point> hwpinfo = loadpointInfo();
        for(Point point:hwpinfo)
        {
            int gps_id = getGpsPointId(point,gps_pinfo);
            point.setGps_id(gps_id);
        }
        syncGpsIdToHighwayDB(hwpinfo);
//        List<Point> wspInfo = loadWSInfo();
//        System.out.println("weather station count: " + wspInfo.size());
//
//        Map<String,List<Point>> highwayinfo = loadHighWayInfo("highway");
//
//        for(String key:highwayinfo.keySet()){
//            List<Point> points = highwayinfo.get(key);
//            updatePointInfo(points,wspInfo);
//        }
//        syncPointInfoToDB("highway",highwayinfo);
//
//      lineToColumnForRain();

        System.out.println("Done");

    }
}
