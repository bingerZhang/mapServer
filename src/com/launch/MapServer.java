package com.launch;

import com.map.Parser;
import com.server.DefaultThreadFactory;
import com.server.PointsReloadTask;
import com.server.TrackerServer;
import com.server.WeatherReloadTask;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapServer implements Main {
//public class MapServer {
    private static Logger logger = Logger.getLogger("MapServer");
    private static ScheduledExecutorService scheduler;
    public void main(String[] args) throws IOException {
//    public static void main(String[] args) throws IOException {
        Parser parser = Parser.getInstance();
//        File map = null;
//        File csv = null;
//        if(Application.isWindows()){
////            map = new File("D:/BeiJing.map");
////            csv = new File("D:/BeiJing.csv");
//            csv = new File("D:/Road_Point_high_0.csv");
//            map = new File("D:/Road_Point_high_0.map");
//        }else if(Application.isLinux()){
////            map = new File(Application.DATA_PATH + "/BeiJing.map");
////            csv = new File(Application.DATA_PATH +"/BeiJing.csv");
//            map = new File(Application.DATA_PATH + "/Road_Point_high_0.map");
//            csv = new File(Application.DATA_PATH +"/Road_Point_high_0.csv");
//        }else {
//            logger.warn("Unknown OS , exit!");
//            return;
//        }
//        if(map.exists()){
//            logger.info("Getting info from map object..." + map.getAbsolutePath());
//            Application.MapInfo = (List<List<String>>) MapUtil.readObject(map);
//        }else {
//            logger.info("Getting info from CSV file" + csv.getAbsolutePath());
//            Application.MapInfo = parser.filter(parser.readCSVFile("D:/Road_Point_high_0.csv"));
//            logger.info("filter list size: " + Application.MapInfo.size());
//            MapUtil.writeObject(map,Application.MapInfo);
//        }
//        logger.info("map info list size: " + Application.MapInfo.size());

        logger.info("Started, loading....");
//        Application.motorwayInfo = MapUtil.loadRoadInfo("motorway");
//        Application.motorwaylines =parser.getRoadsInfo("motorway");
//        logger.info("motorway road count: " + Application.motorwaylines.size());
        scheduler = Executors.newScheduledThreadPool(2, new DefaultThreadFactory("tracker-reload-task"));
        scheduler.scheduleWithFixedDelay(new PointsReloadTask(),3,3600, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(new WeatherReloadTask(),1,360, TimeUnit.MINUTES);
        TrackerServer.startServer();
    }
}
