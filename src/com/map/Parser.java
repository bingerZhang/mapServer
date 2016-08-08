package com.map;

import com.util.MysqlConnector;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zlb on 2016/7/19.
 */
public class Parser {
    private static Logger logger = Logger.getLogger("Parser");
        public static Parser parser = null;
        public static Parser getInstance(){
                if(parser == null)parser = new Parser();
            return parser;
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
        public List<List<String>> filter(List<List<String>> allPoints){
            List<List<String>> lists = new ArrayList<List<String>>();
            for(List<String> point: allPoints){
                String name = point.get(1);
                String road_id = point.get(4);
                if(name.trim().length()>0){
                     lists.add(point);
                }else if(road_id.trim().length()>0){
                     lists.add(point);
                }
            }
            return lists;
        }
        public Map<String, List<List<Point>>> getLines(List<List<String>> allPoints,String reg){
            Map<String, List<Point>> lines = new HashMap<String, List<Point>>();
            for(List<String> point: allPoints){
                 String name = point.get(1);
                 if(name!=null&&name.equals("NAME"))continue;
                 if(name.trim().length()==0){
                    name = point.get(4);
                 }
//                if(name.matches(reg)&&!name.contains("/")){
                    List<Point> points = null;
                    if(lines.containsKey(name)){
                        points = lines.get(name);
                    }else{
                        points = new ArrayList<Point>();
                    }
                    int size = points.size();
                    Point point1 = new Point(0,Double.valueOf(point.get(5)),Double.valueOf(point.get(6)),0d,size + 1);
                    points.add(point1);
                    lines.put(name,points);

            }
            Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
            for (Map.Entry<String, List<Point>> entry : lines.entrySet()) {
                String name = entry.getKey();
                List<Point> points= entry.getValue();
                List<List<Point>> list = MapUtil.adjustment(points,2d);
                retlines.put(name,list);
            }
            return retlines;
        }

        public Map<String, List<List<Point>>> getHighWayInfo(String table) {
            Map<String,List<Point>> highwayInfo = MapUtil.loadHighWayInfo(table);
            logger.info("HighWay Road data load done!");
            Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
            for (Map.Entry<String, List<Point>> entry : highwayInfo.entrySet()) {
                String name = entry.getKey();
                List<Point> points= entry.getValue();
                List<List<Point>> list = MapUtil.adjustment(points,0d);
                retlines.put(name,list);
            }
            return retlines;
        }


        public Map<String, List<List<Point>>> getRoadsInfo(String table) {
            Map<String,List<Point>> roadsInfo = MapUtil.loadRoadInfo(table);
            logger.info("Road data load done!");
            Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
            for (Map.Entry<String, List<Point>> entry : roadsInfo.entrySet()) {
                String name = entry.getKey();
                List<Point> points= entry.getValue();
                List<List<Point>> list = MapUtil.adjustment(points,20d);
                retlines.put(name,list);
            }
            return retlines;
        }

        public Map<String, List<List<Point>>> getRainInfo(String table,String date,String hour,int level) {

            Map<String,List<Point>> roadsInfo = MapUtil.getRainInfoByLevel(table,date,hour,level);
            logger.info("Raining Road data load done!");
            Map<String, List<List<Point>>> retlines = new HashMap<String, List<List<Point>>>();
            for (Map.Entry<String, List<Point>> entry : roadsInfo.entrySet()) {
                String name = entry.getKey();
                List<Point> points= entry.getValue();
                List<List<Point>> list = MapUtil.adjustment(points,20d);
                retlines.put(name,list);
            }
            return retlines;
        }

    /**
     * 解析csv文件 到一个list中 每个单元个为一个String类型记录，每一行为一个list。 再将所有的行放到一个总list中
     */
    public static boolean csvToDB(String file) throws IOException {
        InputStreamReader fr = new InputStreamReader(new FileInputStream(file),"GBK");
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
                if(first){
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
                value_b.append("'" + cells.get(1).trim() +"','" + cells.get(4) +"',"+ cells.get(6)+","+cells.get(5));
                String value = value_b.toString();
                values.add(value.substring(0,value.length()-1));
                if(values.size()> 10000){
                    boolean ret = mysqlConnector.insert_SQLS(prefix,values);
                    if(ret){
                        values = new ArrayList<String>();
                    }else {
                        System.out.println("insert failed !");
                        Thread.sleep(2000);
                    }
                }
            }
            if(values.size()>0){
                boolean ret = mysqlConnector.insert_SQLS(prefix,values);
                if(!ret){
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
            if(mysqlConnector!=null) {
                mysqlConnector.disconnSQL();
            }
        }
        System.out.println("csvToDB Done!");
        return true;
    }

        public static void main(String[] args) throws Throwable {
            Parser parser = Parser.getInstance();
//            List<List<String>> csvList = parser.readCSVFile("D:/BeiJing.csv");
//            List<List<String>> csvList = (List<List<String>>) MapUtil.readObject(new File("D:/BeiJing.map"));
//            Map<String, List<List<Point>>> listMap = parser.getLines(csvList,"^[SGX].+");
//            System.out.println("listMap size: " + listMap.size());
//            String name = "S213";
//            if(name.matches("^[SGX].+"))
//            {
//                System.out.println("matched");
//            } else {
//                System.out.println("not matche");
//            }
            boolean ret = csvToDB("D:/Road_Point_high_0.csv");
            System.out.println(ret?"success":"failed");
        }
}
