package com.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.util.ByteStreams;
import com.util.SizeLimitedInputStream;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import javax.persistence.criteria.CriteriaBuilder;
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
        JSONArray jsonArray = new JSONArray(data.toString());
        int len = jsonArray.length();
        boolean error = false;
        List<List<String>> weather_data = new ArrayList<>();
        for(int i =0;i<len;i++) {
            Object object = jsonArray.get(i);
            if(object instanceof JSONArray){
                if(((JSONArray) object).length()!=3) {
                    error = true;
                }
                double x = (double)((JSONArray) object).get(0);
                double y = (double)((JSONArray) object).get(1);
                String time = (String)((JSONArray) object).get(2);
                String date = time.substring(0,8);
                int hour = Integer.valueOf(time.substring(8));
                System.out.println("X:" + x + "  " + "Y:"+y  +" Date:"+date + " Hour:" + hour);
            }
        }
        if(!error) {
            exchange.sendResponseHeaders(200, data.length);
            ByteStreams.copy(data, exchange.getResponseBody());
        }else {
            byte[] resp = "data error".getBytes();
            exchange.sendResponseHeaders(403, resp.length);
            ByteStreams.copy(resp, exchange.getResponseBody());
        }
    }
//    public static JSONObject toJson(Map<String, Object> map){
//        JSONObject jsonObject = new JSONObject();
//        Object value = null;
//        for(String key: map.keySet()){
//            value = map.get(key);
//            if(value instanceof List)
//            {
//                JSONArray jsonMembers = new JSONArray();
//                int size = ((List) value).size();
//                for(int i=0;i<size;i++)
//                {
//                    Object node = ((List) value).get(i);
//                    if(node instanceof Map) jsonMembers.put(toJson((Map) node));
//                }
//                try {
//                    jsonObject.put(key,jsonMembers);
//                } catch (JSONException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//
//            }else if(value instanceof Map){
//                try {
////                    System.out.println(key + " : " + value.toString());
//                    jsonObject.put(key,toJson((Map)value));
//                } catch (JSONException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//            }else {
//                try {
//                    jsonObject.put(key,value);
//                } catch (JSONException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//            }
//        }
//        return jsonObject;
//    }
//    public static Map toMap(String jsonString) throws IOException, JSONException {
//
//        JSONObject jsonObject = new JSONObject(jsonString);
//        Map result = new HashMap();
//        Iterator iterator = jsonObject.keys();
//        String key = null;
//        Object value = null;
//
//        while (iterator.hasNext()) {
//            key = (String) iterator.next();
//            value = jsonObject.get(key);
//            if (value instanceof JSONObject) {
////                System.out.println("key: " + key + "  value: "+ value.toString());
//                value = toMap(value.toString());
//            }
//            result.put(key, value);
//        }
//        return result;
//    }

    public static void main(String[] args) throws IOException, JSONException {
        String data = "[[40.004084,116.487383,\"2016120121\"],[39.995115,116.430505,\"2016120121\"],[39.991477,116.323282,\"2016120121\"],[39.934685,116.281568,\"2016120122\"],[39.880104,116.281055,\"2016120122\"],[39.837234,116.219012,\"2016120122\"],[39.781969,116.170499,\"2016120123\"],[39.697114,116.104402,\"2016120123\"],[39.603722,116.066842,\"2016120123\"],[39.445505,116.029349,\"2016120200\"],[39.223836,115.830969,\"2016120200\"],[39.015487,115.700092,\"2016120200\"],[38.812158,115.560924,\"2016120201\"],[38.718938,115.319325,\"2016120201\"],[38.581537,115.094315,\"2016120201\"],[38.424102,114.855239,\"2016120202\"],[38.246927,114.782128,\"2016120202\"],[38.075561,114.791807,\"2016120202\"],[37.917889,114.757433,\"2016120203\"],[37.742422,114.673197,\"2016120203\"],[37.479953,114.644618,\"2016120203\"],[37.2429,114.597922,\"2016120204\"],[37.029723,114.587677,\"2016120204\"],[36.836074,114.554164,\"2016120204\"],[36.672587,114.559835,\"2016120205\"],[36.516715,114.517067,\"2016120205\"],[36.372794,114.434944,\"2016120206\"],[36.235687,114.403213,\"2016120206\"],[36.061494,114.436368,\"2016120206\"],[35.834541,114.363716,\"2016120207\"],[35.572993,114.179181,\"2016120207\"],[35.360427,114.052796,\"2016120207\"],[35.18347,113.935194,\"2016120208\"],[34.945406,113.830653,\"2016120208\"],[34.732681,113.824327,\"2016120208\"],[34.54318,113.828012,\"2016120209\"],[34.266491,113.836841,\"2016120209\"],[33.972428,113.896358,\"2016120209\"],[33.889712,113.657644,\"2016120210\"],[33.676448,113.482284,\"2016120210\"],[33.597054,113.476709,\"2016120210\"],[33.339204,113.444028,\"2016120211\"],[33.119761,113.45574,\"2016120211\"],[32.814829,113.491801,\"2016120211\"],[32.643181,113.480407,\"2016120212\"],[32.575904,113.509041,\"2016120212\"],[32.484952,113.495598,\"2016120212\"],[32.370208,113.486606,\"2016120213\"],[32.223332,113.421182,\"2016120213\"],[32.058357,113.382568,\"2016120213\"],[31.877644,113.347228,\"2016120214\"],[31.716828,113.26142,\"2016120214\"],[31.536507,113.195038,\"2016120214\"],[31.33012,113.255565,\"2016120215\"],[31.00747,113.273095,\"2016120215\"],[30.740467,113.136953,\"2016120215\"],[30.470243,113.117103,\"2016120216\"],[30.2189,113.047609,\"2016120216\"],[29.948408,113.042685,\"2016120217\"],[29.690379,113.119061,\"2016120217\"],[29.451755,113.262676,\"2016120217\"],[29.326265,113.249092,\"2016120218\"],[29.257968,113.291147,\"2016120218\"],[29.073047,113.277436,\"2016120218\"],[28.819363,113.261642,\"2016120219\"],[28.588145,113.252048,\"2016120219\"],[28.316462,113.096847,\"2016120219\"],[28.169641,113.05622,\"2016120220\"],[28.024612,113.042407,\"2016120220\"],[27.856505,113.031653,\"2016120220\"],[27.596267,113.047675,\"2016120221\"],[27.285701,112.965039,\"2016120221\"],[27.114902,112.868688,\"2016120221\"],[26.906222,112.834034,\"2016120222\"],[26.696181,112.902675,\"2016120222\"],[26.442286,112.907193,\"2016120222\"],[26.269204,112.877447,\"2016120223\"],[26.088175,112.964353,\"2016120223\"],[25.944153,113.068802,\"2016120223\"],[25.81566,113.092297,\"2016120300\"],[25.69374,113.058776,\"2016120300\"],[25.553242,113.048239,\"2016120300\"],[25.403984,112.974323,\"2016120301\"],[25.28482,113.056669,\"2016120301\"],[25.094028,113.116298,\"2016120301\"],[24.934969,113.183671,\"2016120302\"],[24.805985,113.229848,\"2016120302\"],[24.748723,113.396755,\"2016120302\"],[24.669027,113.530906,\"2016120303\"],[24.450547,113.535353,\"2016120303\"],[24.325752,113.381483,\"2016120304\"],[24.139329,113.327982,\"2016120304\"],[23.957925,113.218655,\"2016120304\"],[23.798019,113.185428,\"2016120305\"],[23.655244,113.242586,\"2016120305\"],[23.517004,113.324932,\"2016120305\"],[23.386549,113.284597,\"2016120306\"],[23.24103,113.269039,\"2016120306\"],[23.173415,113.259506,\"2016120306\"],[23.146706,113.245762,\"2016120307\"],[23.137685,113.270524,\"2016120307\"],[23.134833,113.271941,\"2016120307\"]]";
        JSONArray jsonArray = new JSONArray(data);
        int len = jsonArray.length();
        boolean error = false;
        for(int i =0;i<len;i++) {
            Object object = jsonArray.get(i);
            if(object instanceof JSONArray){
                if(((JSONArray) object).length()!=3) {
                    error = true;
                }
                double x = (double)((JSONArray) object).get(0);
                double y = (double)((JSONArray) object).get(1);
                String time = (String)((JSONArray) object).get(2);
                String date = time.substring(0,8);
                int hour = Integer.valueOf(time.substring(8)) ;
                System.out.println("X:" + x + "  " + "Y:" + y  +" Date:" + date + " Hour:" + hour);
            }
        }
        System.out.println(len);
    }

}
