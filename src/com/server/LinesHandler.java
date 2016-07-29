package com.server;

import com.launch.Application;
import com.launch.MapServer;
import com.map.Parser;
import com.map.Point;
import com.sun.net.httpserver.HttpExchange;
import com.util.Utils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LinesHandler extends ProxyHandler {
    private static Logger logger = Logger.getLogger("LinesHandler");
    public LinesHandler() {
        super();
    }
    public static double AD_LNG = 0.012580687519;      //jing
    public static double AD_LAT = 0.006686316879;      //wei
    protected void logicalhandle(HttpExchange exchange) throws IOException, JSONException {
        URI url = exchange.getRequestURI();
        String query = url.getQuery();
        String jpc = Utils.findParamValue(query,"jpc");
        Parser parser = Parser.getInstance();
//        Map<String, List<List<Point>>> listMap = parser.getLines(Application.MapInfo,"^[SGX].+");

        logger.info("listMap size: " + Application.motorwaylines.size());
        JSONObject json = new JSONObject();
        JSONArray lines = new JSONArray();

        for (Map.Entry<String, List<List<Point>>> entry : Application.motorwaylines.entrySet()) {
            JSONObject line = new JSONObject();
            String name = entry.getKey();
            line.put("name",name);
            JSONArray segs = new JSONArray();
            List<List<Point>> pointslist = entry.getValue();
            int size = pointslist.size();
            for(int i=0;i<size;i++) {
                List<Point> points = pointslist.get(i);
                JSONObject pointobj = new JSONObject();
                JSONArray jpoints = new JSONArray();
                for (Point point : points) {
                    List<Double> list = new ArrayList<>();
                    list.add(point.getPoint_x()+AD_LNG);
                    list.add(point.getPoint_y()+AD_LAT);
                    jpoints.put(list);
                }
                pointobj.put("points",jpoints);
                segs.put(pointobj);
            }
            line.put("segs",segs);
            lines.put(line);
        }
        json.put("lines",lines);
        json.put("status","ok");
        String res = json.toString();
        res = "/**/ typeof "+ jpc + " === 'function' && " + jpc + "(" + res + ");";
        response(exchange,200,res);
    }

}
