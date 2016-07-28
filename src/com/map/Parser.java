package com.map;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zlb on 2016/7/19.
 */
public class Parser {

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
                    Point point1 = new Point(Double.valueOf(point.get(5)),Double.valueOf(point.get(6)),0d,size + 1);
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


        public static void main(String[] args) throws Throwable {
            Parser parser = Parser.getInstance();
//            List<List<String>> csvList = parser.readCSVFile("D:/BeiJing.csv");
            List<List<String>> csvList = (List<List<String>>) MapUtil.readObject(new File("D:/BeiJing.map"));
            Map<String, List<List<Point>>> listMap = parser.getLines(csvList,"^[SGX].+");
            System.out.println("listMap size: " + listMap.size());
//            String name = "S213";
//            if(name.matches("^[SGX].+"))
//            {
//                System.out.println("matched");
//            } else {
//                System.out.println("not matche");
//            }
        }
}
