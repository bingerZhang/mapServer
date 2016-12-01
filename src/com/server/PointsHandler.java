package com.server;

import com.map.Parser;
import com.map.Point;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.util.ByteStreams;
import com.util.SizeLimitedInputStream;
import com.util.Utils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class PointsHandler extends ProxyHandler {
    private static Logger logger = Logger.getLogger("LinesHandler");
    public PointsHandler() {
        super();
    }
    protected void logicalhandle(HttpExchange exchange) throws IOException, JSONException {
        URI url = exchange.getRequestURI();
        InputStream in = exchange.getRequestBody();
        byte[] data = ByteStreams.toBytes(new SizeLimitedInputStream(in, 1024));
        Map<String, Object> map = new HashMap();
        map = toMap(data.toString());
//        data = rewrite(exchange, data);
        Headers headers = exchange.getResponseHeaders();
        exchange.sendResponseHeaders(200, data.length);
        ByteStreams.copy(data, exchange.getResponseBody());

    }
    public static JSONObject toJson(Map<String, Object> map){
        JSONObject jsonObject = new JSONObject();
        Object value = null;
        for(String key: map.keySet()){
            value = map.get(key);
            if(value instanceof List)
            {
                JSONArray jsonMembers = new JSONArray();
                int size = ((List) value).size();
                for(int i=0;i<size;i++)
                {
                    Object node = ((List) value).get(i);
                    if(node instanceof Map){
                        jsonMembers.put(toJson((Map)node));
                    }
                }
                try {
                    jsonObject.put(key,jsonMembers);
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }else if(value instanceof Map){
                try {
//                    System.out.println(key + " : " + value.toString());
                    jsonObject.put(key,toJson((Map)value));
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }else {
                try {
                    jsonObject.put(key,value);
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return jsonObject;
    }
    public static Map toMap(String jsonString) throws IOException, JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        Map result = new HashMap();
        Iterator iterator = jsonObject.keys();
        String key = null;
        Object value = null;

        while (iterator.hasNext()) {
            key = (String) iterator.next();
            value = jsonObject.get(key);
            if (value instanceof JSONObject) {
//                System.out.println("key: " + key + "  value: "+ value.toString());
                value = toMap(value.toString());
            }
            result.put(key, value);
        }
        return result;
    }

}
