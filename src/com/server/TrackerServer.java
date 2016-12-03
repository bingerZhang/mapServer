package com.server;

import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class TrackerServer {
    static Logger logger = Logger.getLogger("TrackerServer");
    public static void startServer() throws IOException {
        //PropertyConfigurator.configure(".\\src\\log4j.properties");

        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 8070), 5);
        server.createContext("/r_api_lines", new LinesHandler());
        server.createContext("/r_api_rain",new RainHandler());
        server.createContext("/weather_by_points",new PointsHandler());

        server.setExecutor(Executors.newFixedThreadPool(50, new DefaultThreadFactory("mapserver")));
        server.start();
        logger.info("Tracker Server start...");
    }

    public static void stopServer() {

    }
}
