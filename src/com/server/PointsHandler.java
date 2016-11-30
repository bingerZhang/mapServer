package com.server;

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

public class PointsHandler extends ProxyHandler {
    private static Logger logger = Logger.getLogger("LinesHandler");
    public PointsHandler() {
        super();
    }
    protected void logicalhandle(HttpExchange exchange) throws IOException, JSONException {
        URI url = exchange.getRequestURI();
        String query = url.getQuery();
        String jpc = Utils.findParamValue(query,"jpc");
        String index = Utils.findParamValue(query,"index");
        String cache = Utils.findParamValue(query,"cache");
        boolean refresh = false;
        if(cache.equals("true"))refresh = true;
        int index_level = -1;
        if(index!=null)index_level = Integer.valueOf(index);
        Parser parser = Parser.getInstance();
        Map<String, List<List<Point>>> rainRoads = null;
        if(index_level>-1 && index_level <12){
            rainRoads =  parser.getRainInfo("highway_gps_rain",index_level+1,refresh);
        }else {
            rainRoads =  parser.getRainInfo("map_rain",refresh);
        }
        logger.info("Raining road size: " + rainRoads.size());
        JSONObject json = new JSONObject();
        JSONArray lines = new JSONArray();
        int count = 0;
        for (Map.Entry<String, List<List<Point>>> entry : rainRoads.entrySet()) {
            JSONObject roadjson = new JSONObject();
            JSONArray roadsegs = new JSONArray();
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
                    int[] level = point.getLevel();
                    if(index_level<11 && index_level >-1){
                        for(int j=0;j<1;j++) {
                            list.add((double) level[index_level]);
                        }
                    }else{
                            for(int j=0;j<level.length;j++){
                                list.add((double)level[j]);
                            }
                    }
                    line_points.put(list);
                }
                line_obj.put("geo",line_points);
                roadsegs.put(line_obj);
            }
            roadjson.put("name",entry.getKey());
            roadjson.put("segs",roadsegs);
            lines.put(roadjson);

        }
        logger.info("points count: " + count);
        json.put("lines",lines);
        json.put("status","ok");
        String res = json.toString();
        res = "/**/ typeof "+ jpc + " === 'function' && " + jpc + "(" + res + ");";
        response(exchange,200,res);
    }

}
