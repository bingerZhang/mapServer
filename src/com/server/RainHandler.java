package com.server;

import com.launch.Application;
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

public class RainHandler extends ProxyHandler {
    private static Logger logger = Logger.getLogger("LinesHandler");
    public RainHandler() {
        super();
    }
    public static double AD_LNG = 0.012580687519;      //jing
    public static double AD_LAT = 0.006686316879;      //wei
    protected void logicalhandle(HttpExchange exchange) throws IOException, JSONException {
        URI url = exchange.getRequestURI();
        String query = url.getQuery();
        String jpc = Utils.findParamValue(query,"jpc");
        String level = Utils.findParamValue(query,"level");
        int lev = Integer.valueOf(level);
        Parser parser = Parser.getInstance();
        Map<String, List<List<Point>>> rainRoads =  parser.getRainInfo(null,null,null,lev);
        logger.info("Raining road size: " + rainRoads.size());
        JSONObject json = new JSONObject();
        JSONArray lines = new JSONArray();
        int count = 0;
        for (Map.Entry<String, List<List<Point>>> entry : Application.motorwaylines.entrySet()) {
            List<List<Point>> pointslist = entry.getValue();
            int size = pointslist.size();
            for(int i=0;i<size;i++) {
                List<Point> points = pointslist.get(i);
                count = count + points.size();
                JSONObject line_obj = new JSONObject();
                JSONArray line_points = new JSONArray();
                for (Point point : points) {
                    List<Double> list = new ArrayList<>();
                    list.add(point.getPoint_x());
                    list.add(point.getPoint_y());
                    line_points.put(list);
                }
                line_obj.put("geo",line_points);
                line_obj.put("count",lev);
                lines.put(line_obj);
            }

        }
        logger.info("points count: " + count);
        json.put("lines",lines);
        json.put("status","ok");
        String res = json.toString();
        res = "/**/ typeof "+ jpc + " === 'function' && " + jpc + "(" + res + ");";
        response(exchange,200,res);
    }

}
