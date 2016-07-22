package com.launch;

import com.map.MapUtil;
import com.map.Parser;
import com.server.TrackerServer;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    private static Logger logger = Logger.getLogger("Main");
    public static List<List<String>>  MapInfo = null;
    public static void main(String[] args) throws IOException {
        Parser parser = Parser.getInstance();
        File map = null;
        File csv = null;
        if(Application.isWindows()){
            map = new File("D:/BeiJing.map");
            csv = new File("D:/BeiJing.csv");
        }else if(Application.isLinux()){
            map = new File(Application.DATA_PATH + "/BeiJing.map");
            csv = new File(Application.DATA_PATH +"/BeiJing.csv");
        }else {
            logger.warn("Unknown OS , exit!");
            return;
        }
        if(map.exists()){
            logger.info("Getting info from map object..." + map.getAbsolutePath());
            MapInfo = (List<List<String>>) MapUtil.readObject(map);
        }else {
            logger.info("Getting info from CSV file" + csv.getAbsolutePath());
            MapInfo = parser.filter(parser.readCSVFile("D:/BeiJing.csv"));
            logger.info("filter list size: " + MapInfo.size());
            MapUtil.writeObject(map,MapInfo);
        }
        logger.info("map info list size: " + MapInfo.size());
        TrackerServer.startServer();
    }
}
