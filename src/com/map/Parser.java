package com.map;

import com.util.MysqlConnector;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.random;

/**
 * Created by zlb on 2016/7/19.
 */
public class Parser {
    private static Map<String, List<List<Point>>> map_rain_info = new HashMap<>();
    private static List<Map<String, List<List<Point>>>> map_rain_level_info = new ArrayList<>();
    private static Map<String, Map<String, Point>> points = new HashMap<>();
    private static Date[] lastLevelUpdate = new Date[12];
    private static Date lastUpdate = null;
    private static Logger logger = Logger.getLogger("Parser");
    public static Parser parser = null;

    public static Parser getInstance() {
        if (parser == null) parser = new Parser();
        return parser;
    }

    public static Map<String, Map<String, Point>> getPoints() {
        return points;
    }

    /**
     * 解析csv文件 到一个list中 每个单元个为一个String类型记录，每一行为一个list。 再将所有的行放到一个总list中
     */
    public List<List<String>> readCSVFile(String file) throws IOException {
        InputStreamReader fr = new InputStreamReader(new FileInputStream(file));
        BufferedReader br = new BufferedReader(fr);
        String rec = null;// 一行
        String str;// 一个单元格
        List<List<String>> listFile = new ArrayList<List<String>>();
        try {
            // 读取一行
            while ((rec = br.readLine()) != null) {
//                    Pattern pCells = Pattern
//                            .compile("(\"[^\"]*(\"{2})*[^\"]*\")*[^,]*,");
//                    Matcher mCells = pCells.matcher(rec);
                List<String> cells = new ArrayList<String>();// 每行记录一个list
//                    // 读取每个单元格
//                    while (mCells.find()) {
//                        str = mCells.group();
//                        str = str.replaceAll(
//                                "(?sm)\"?([^\"]*(\"{2})*[^\"]*)\"?.*,", "$1");
//                        str = str.replaceAll("(?sm)(\"(\"))", "$2");
//                        cells.add(str);
//                    }

//                    listFile.add(cells);
//                    String[] list = rec.split(",");
                StringTokenizer token = new StringTokenizer(rec, " ,");
                while (token.hasMoreTokens()) {
                    cells.add(token.nextToken());
                }
                listFile.add(cells);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                fr.close();
            }
            if (br != null) {
                br.close();
            }
        }
        System.out.println("csv point size: " + listFile.size());
        return listFile;
    }

    public List<List<String>> filter(List<List<String>> allPoints) {
        List<List<String>> lists = new ArrayList<List<String>>();
        for (List<String> point : allPoints) {
            String name = point.get(1);
            String road_id = point.get(4);
            if (name.trim().length() > 0) {
                lists.add(point);
            } else if (road_id.trim().length() > 0) {
                lists.add(point);
            }
        }
        return lists;
    }

    public Map<String, List<List<Point>>> getLines(List<List<String>> allPoints, String reg) {
        Map<String, List<Point>> lines = new HashMap<String, List<Point>>();
        for (List<String> point : allPoints) {
            String name = point.get(1);
            if (name != null && name.equals("NAME")) continue;
            if (name.trim().length() == 0) {
                name = point.get(4);
            }
//                if(name.matches(reg)&&!name.contains("/")){
            List<Point> points = null;
            if (lines.containsKey(name)) {
                points = lines.get(name);
            } else {
                points = new ArrayList<Point>();
            }
            int size = points.size();
            Point point1 = new Point(0, Double.valueOf(point.get(5)), Double.valueOf(point.get(6)), 0d, size + 1, 1);
            points.add(point1);
            lines.put(name, points);

        }
        Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
        for (Map.Entry<String, List<Point>> entry : lines.entrySet()) {
            String name = entry.getKey();
            List<Point> points = entry.getValue();
            List<List<Point>> list = MapUtil.adjustment(points, 2d);
            retlines.put(name, list);
        }
        return retlines;
    }

    public Map<String, List<List<Point>>> getHighWayInfo(String table) {
        Map<String, List<Point>> highwayInfo = MapUtil.loadHighWayInfo(table);
        logger.info("HighWay Road data load done!");
        Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
        for (Map.Entry<String, List<Point>> entry : highwayInfo.entrySet()) {
            String name = entry.getKey();
            List<Point> points = entry.getValue();
            List<List<Point>> list = MapUtil.adjustment(points, 0d);
            retlines.put(name, list);
        }
        return retlines;
    }


    public Map<String, List<List<Point>>> getRoadsInfo(String table) {
        Map<String, List<Point>> roadsInfo = MapUtil.loadRoadInfo(table);
        logger.info("Road data load done!");
        Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
        for (Map.Entry<String, List<Point>> entry : roadsInfo.entrySet()) {
            String name = entry.getKey();
            List<Point> points = entry.getValue();
            List<List<Point>> list = MapUtil.adjustment(points, 20d);
            retlines.put(name, list);
        }
        return retlines;
    }

    public Map<String, List<List<Point>>> getRainInfo(String table, int level, boolean refresh) {
        String tableName = table + level;
        int index = level - 1;
        if (lastLevelUpdate[index] == null || refresh || map_rain_level_info.size() == 0) {
            lastLevelUpdate[index] = new Date();
        } else {
            Date now = new Date();
            if (now.getTime() - lastLevelUpdate[index].getTime() < 3600000) {
                return map_rain_level_info.get(index);
            } else {
                lastLevelUpdate[index] = new Date();
            }
        }
        Map<String, List<Point>> roadsInfo = MapUtil.getRainInfo(index, table);
        logger.info("Raining Road data load done!");
        Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
        for (Map.Entry<String, List<Point>> entry : roadsInfo.entrySet()) {
            String name = entry.getKey();
            List<Point> points = entry.getValue();
            List<List<Point>> list = MapUtil.adjustment(points, 0d);
            retlines.put(name, list);
        }
        map_rain_level_info.add(index, retlines);
        return retlines;
    }

    public Map<String, List<List<Point>>> getRainInfo(String table, boolean refresh) {
        if (lastUpdate == null || refresh) {
            lastUpdate = new Date();
        } else {
            Date now = new Date();
            if (now.getTime() - lastUpdate.getTime() < 3600000) return map_rain_info;
        }
        Map<String, List<Point>> roadsInfo = MapUtil.getRainInfo(table);
        logger.info("Raining Road data load done!");
        Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
        for (Map.Entry<String, List<Point>> entry : roadsInfo.entrySet()) {
            String name = entry.getKey();
            List<Point> points = entry.getValue();
            List<List<Point>> list = MapUtil.adjustment(points, 0d);
            retlines.put(name, list);
        }
        map_rain_info = retlines;
        return retlines;
    }

    public Map<String, List<List<Point>>> getRainInfo(String table, String date, String hour, int level) {

        Map<String, List<Point>> roadsInfo = MapUtil.getRainInfoByLevel(table, date, hour, level);
        logger.info("Raining Road data load done!");
        Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
        for (Map.Entry<String, List<Point>> entry : roadsInfo.entrySet()) {
            String name = entry.getKey();
            List<Point> points = entry.getValue();
            List<List<Point>> list = MapUtil.adjustment(points, 0d);
            retlines.put(name, list);
        }
        return retlines;
    }

    /**
     * 解析csv文件 到一个list中 每个单元个为一个String类型记录，每一行为一个list。 再将所有的行放到一个总list中
     */
    public static boolean csvToDB(String file) throws IOException {
        InputStreamReader fr = new InputStreamReader(new FileInputStream(file), "GBK");
        BufferedReader br = new BufferedReader(fr);
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        mysqlConnector.ready_insert();
        String prefix = "INSERT INTO motorway(name, road_id, latitude,longitude) VALUES ";
        String line = null;// 一行
        String str;// 一个单元格
        try {
            // 读取一行
            List<String> values = new ArrayList<String>();
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line + ",";
                if (first) {
                    first = false;
                    continue;
                }
//                StringTokenizer token = new StringTokenizer(line, ",");
//                int i=0;
//                StringBuilder value_b = new StringBuilder();
//                while (token.hasMoreTokens()) {
//                    if(i==1 || i==4 ){
//                        value_b.append("'" + token.nextToken() +"',");
//                    }else if(i==5 || i==6) {
//                        value_b.append( token.nextToken() +",");
//                    }else {
//                        token.nextToken();
//                    }
//                    i++;
//                }
                Pattern pCells = Pattern.compile("(\"[^\"]*(\"{2})*[^\"]*\")*[^,]*,");
                Matcher mCells = pCells.matcher(line);
                List<String> cells = new ArrayList<String>();// 每行记录一个list
                // 读取每个单元格
                while (mCells.find()) {
                    str = mCells.group();
                    str = str.replaceAll("(?sm)\"?([^\"]*(\"{2})*[^\"]*)\"?.*,", "$1");
                    str = str.replaceAll("(?sm)(\"(\"))", "$2");
                    cells.add(str);
                }
                StringBuilder value_b = new StringBuilder();
                value_b.append("'" + cells.get(1).trim() + "','" + cells.get(4) + "'," + cells.get(6) + "," + cells.get(5));
                String value = value_b.toString();
                values.add(value.substring(0, value.length() - 1));
                if (values.size() > 10000) {
                    boolean ret = mysqlConnector.insert_SQLS(prefix, values);
                    if (ret) {
                        values = new ArrayList<String>();
                    } else {
                        System.out.println("insert failed !");
                        Thread.sleep(2000);
                    }
                }
            }
            if (values.size() > 0) {
                boolean ret = mysqlConnector.insert_SQLS(prefix, values);
                if (!ret) {
                    System.out.println("last insert failed !");
                }
            }
            mysqlConnector.close_insert();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fr != null) {
                fr.close();
            }
            if (br != null) {
                br.close();
            }
            if (mysqlConnector != null) {
                mysqlConnector.disconnSQL();
            }
        }
        System.out.println("csvToDB Done!");
        return true;
    }

    public static void readtxtFile(String file, int tableNum) throws IOException {
        InputStreamReader fr = new InputStreamReader(new FileInputStream(file));
        BufferedReader br = new BufferedReader(fr);
        String rec = null;// 一行
        String str;// 一个单元格
        List<List<String>> listFile = new ArrayList<List<String>>();
        Map<String, List<Point>> listMap = MapUtil.loadgpspointInfo2();
        try {
            // 读取一行
            double lng_start = 70.0;
            double lng_end = 140.1;
            double lat_start = 60.0;
            double lat_end = 0.0;
            boolean end = false;
            rec = br.readLine();
            rec = br.readLine();
            int count = 0;
            boolean error = false;
            DecimalFormat decimalFormat = new DecimalFormat(".0");
            for (double lat = lat_start; lat > lat_end; lat = lat - 0.1) {
                count++;
                rec = br.readLine();
                if (rec == null) break;
                String[] rain = rec.trim().split("\\s+");
                String key = "" + decimalFormat.format(lat);
                int num = rain.length;

                if (listMap.containsKey(key)) {
                    List<Point> pointList = listMap.get(key);
                    for (Point point : pointList) {
                        double lng = point.getPoint_y();
                        int index = (int) ((lng * 10 - 700));
                        if (index < num) {
                            point.setRainfall(Double.valueOf(rain[index]));
                        } else {
                            error = true;
                            System.out.println("error !");
                            break;
                        }
                    }
                    if (error) break;
                }
            }
            if (!error) {
                MapUtil.syncgpspointInfo2db(tableNum, listMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                fr.close();
            }
            if (br != null) {
                br.close();
            }
        }
        return;
    }

    public static Map<String, Map<String, Point>> loadGpsRainInfo(List<String> files) throws IOException {
        Map<String, Map<String, Point>> listMap = new HashMap<>();
        int max = files.size();
        for (int i = 0; i < max; i++) {
            readtxtFile(files.get(i), listMap, i, 12);
        }
        return listMap;
    }
    public int getPointlevel(double x,double y,int index){
        if(index<0 || index>11)return -1;
        String fkey = "" + (int)x + "_" + (int)y;
        Map<String, Point> stringPointMap = points.get(fkey);
        if(stringPointMap!=null){
            Point point = getClosestPoint(stringPointMap,x,y);
            return point.getLevel()[index];
        }
        return -1;
    }

    public Point getClosestPoint(Map<String, Point> pointMap,double x,double y) {
        double min = 0d;
        boolean first = true;
        Point ret = null;
        for(Point point:pointMap.values()){
            double dis = distance2(point,x,y);
            if(first){
                first = false;
                min = dis;
                ret = point;
            }
            if(dis<min){
                min = dis;
                ret = point;
            }
        }
        return ret;
    }

    public static double distance2(Point a, double bx,double by){
        Double ax = a.getPoint_x();
        Double ay = a.getPoint_y();
        return (ax-bx)*(ax-bx)+(ay-by)*(ay-by);  // 取平方
    }

    public static Map<String, Map<String, Point>> readtxtFile(String file, Map<String, Map<String, Point>> listMap, int timeIndex, int max) throws IOException {
        InputStreamReader fr = new InputStreamReader(new FileInputStream(file));
        BufferedReader br = new BufferedReader(fr);
        String rec = null;// 一行
        String str;// 一个单元格
        if (listMap == null) listMap = new HashMap<>();
        try {
            // 读取一行
            double lng_start = 70.0;
            double lng_end = 140.1;
            double lat_start = 60.0;
            double lat_end = 0.0;
            boolean end = false;
            //跳过前两行
            rec = br.readLine();
            rec = br.readLine();
            int count = 0;
            boolean error = false;
            DecimalFormat decimalFormat = new DecimalFormat(".0");
            for (double lat = lat_start; lat > lat_end; lat = lat - 0.1) {
                count++;
                rec = br.readLine();
                if (rec == null) break;
                String[] rain = rec.trim().split("\\s+");
//                String key = "" + decimalFormat.format(lat);
                int num = rain.length;
                String fkey = "" + (int) lat + "_";
                String skey = "" + decimalFormat.format(lat) + "_";
                for (int index = 0; index < num; index++) {
                    String tmpfKey = fkey + (70 + index / 10);
                    Map<String, Point> points;
                    String tmpskey = skey + decimalFormat.format(70 + (double) index / 10);
                    int rainlevel = getLevelForGps(Double.valueOf(rain[index]));
//                    if (rainlevel > 1) {
//                        logger.debug(" rainlevel 2");
//                    } else if (rainlevel > 0) {
//                        logger.debug("rainlevel 1");
//                    }
                    if (listMap.containsKey(tmpfKey)) {
                        points = listMap.get(tmpfKey);
                        if (points.containsKey(tmpskey)) {
                            Point point = points.get(tmpskey);
                            point.setLevel(timeIndex, rainlevel);
                        } else {
                            Point point = new Point(lat, 70.0 + (double) index / 10, max);
                            point.setLevel(timeIndex, rainlevel);
                            points.put(tmpskey, point);
                        }
                    } else {
                        points = new HashMap<>();
                        Point point = new Point(lat, 70.0 + (double) index / 10, max);
                        point.setLevel(timeIndex, rainlevel);
                        points.put(tmpskey, point);
                        listMap.put(tmpfKey, points);
                    }
                }
            }
            return listMap;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                fr.close();
            }
            if (br != null) {
                br.close();
            }
        }
        return null;
    }

    public static int getLevelForGps(Double pp) {
//        Random random = new Random();
//        int n = random.nextInt();
//        if(n%3==0){
//            pp = pp + 15;
//        }else if(n%3==1){
//            pp = pp + 5;
//        }

        if (pp >= 15d) {
            return 2;
        } else if (pp >= 5d) {
            return 1;
        } else {
            return 0;
        }
    }

    public void reloadPoints() {
        try {
            logger.debug("loading points info...");
            loadGpsRainPointsInfo();
            logger.debug("load points done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean validate(List<String> files) {
        if (files.size() != 12) return false;
        for (String file : files) {
            if (!new File(file).exists()) return false;
        }
        return true;
    }

    public void loadGpsRainPointsInfo() throws IOException {
        List<String> files = getFiles();
        if(files==null)return;
//        files.add("D:/160831/grid24_2016083108.024");
        if (!validate(files)) {
            logger.debug("validate failed !");
            return;
        }

        Map<String, Map<String, Point>> maps = loadGpsRainInfo(files);
        if (maps == null || maps.size() == 0) {
            logger.debug("load points info failed !");
        } else {
            logger.debug("load points info success !");
            points = maps;
        }
        return;

    }
    public static List<String> getFiles(){
        List<String> files = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//设置日期格式
        Date date = new Date();
        String today = df.format(date);// new Date()为获取当前系统时间
        String osName = System.getProperty("os.name");
        String osPath;
        if(osName.contains("Windows"))
            osPath = "D:/sftp/";
        else
            osPath = "/home/ftp/forecastData/1-3Days_rain/";
        String prePath = osPath + today + "/";
        String first = "08.";
        String second = "20.";
        String[] exts = new String[]{"006", "012", "018", "024", "030", "036", "042", "048", "054", "060", "066", "072"};
        String preFile = "grid06_" + today;
        File directory = new File(prePath);
        int hour = date.getHours();
        String createdTime;
        if (directory.exists() && directory.isDirectory()) {
            if (hour < 8) {
                return null;
            } else if (hour < 20) {
                createdTime = first;
            } else {
                createdTime = second;
            }
            String fileh = preFile + createdTime;
            for (int i = 0; i < exts.length; i++) {
                String file = prePath + fileh + exts[i];
                files.add(file);
            }
        }
        return files;
    }

    public static void updateHighwayWeather() throws IOException {
        List<String> files = getFiles();
        if(files==null)return;
        if (!validate(files)) {
            logger.debug("validate failed !");
            return;
        }
        for(int i=0;i<12;i++){
            String file = files.get(i);
            readtxtFile(file,i+1);
        }
        updateHighwayGpsPointWeather();
//        readtxtFile("D:/160831/grid24_2016083108.024", 1);

    }
    private static void updateHighwayGpsPointWeather(){
        MysqlConnector mysqlConnector = new MysqlConnector();
        mysqlConnector.connSQL();
        String deleteOldData = "delete from highway_gps_point_weather";
        String insertCmd = "insert into highway_gps_point_weather";
        String insertColumns = "(name,pid,lat,lng,gps_id,rain,wind,snow,fog,other) " +
                "select h.NAME name,h.PID pid,h.lat,h.lng,h.gps_id,g.rain,g.wind,g.snow,g.fog,g.other from highway h,gps_point_weather";
        String insertWhere = " g where h.gps_id=g.gps_id;";
        for(int i=1;i<13;i++){
            String deleteSql = deleteOldData + i;
            String insertSql = insertCmd + i + insertColumns + i + insertWhere;
            mysqlConnector.deleteSQL(deleteSql);
            mysqlConnector.insertSQL(insertSql);
        }
        mysqlConnector.disconnSQL();
    }

    public static void main(String[] args) throws Throwable {
        updateHighwayWeather();
        //testLoadGpsRainInfo();
    }
}
