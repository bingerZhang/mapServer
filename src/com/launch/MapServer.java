package com.launch;

import com.map.MapUtil;
import com.map.Parser;
import com.server.TrackerServer;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MapServer implements Main {
    private static Logger logger = Logger.getLogger("MapServer");

    public void main(String[] args) throws IOException {
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
            Application.MapInfo = (List<List<String>>) MapUtil.readObject(map);
        }else {
            logger.info("Getting info from CSV file" + csv.getAbsolutePath());
            Application.MapInfo = parser.filter(parser.readCSVFile("D:/BeiJing.csv"));
            logger.info("filter list size: " + Application.MapInfo.size());
            MapUtil.writeObject(map,Application.MapInfo);
        }
        logger.info("map info list size: " + Application.MapInfo.size());
        TrackerServer.startServer();
    }
}
