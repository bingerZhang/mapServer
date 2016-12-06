package com.server;

import com.map.Parser;
import org.apache.log4j.Logger;

public class WeatherReloadTask implements Runnable {
    private static Logger logger = Logger.getLogger("WeatherReloadTask");
    private static Parser parser = Parser.getInstance();
    private static boolean running = false;
    @Override
    public void run() {
        if(running)return;
        running = true;
        try {
//            System.out.println("Reloading...");
            logger.debug("Weather Reloading...");
            parser.updateHighwayWeather();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            running = false;
        }
    }
}
