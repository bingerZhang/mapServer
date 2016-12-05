package com.server;

import com.map.Parser;
import org.apache.log4j.Logger;

public class RainsReloadTask implements Runnable {
    private static Logger logger = Logger.getLogger("PointsReloadTask");
    private static Parser parser = Parser.getInstance();
    private static boolean running = false;
    @Override
    public void run() {
        if(running)return;
        running = true;
        try {
//            System.out.println("Reloading...");
            logger.debug("Rains Reloading...");
            parser.reloadRains();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            running = false;
        }
    }
}
