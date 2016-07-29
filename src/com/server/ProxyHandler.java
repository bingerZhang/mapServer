package com.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public abstract class ProxyHandler implements HttpHandler {
    private static Logger logger = Logger.getLogger("ProxyHandler");
    protected ProxyHandler() {
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            logicalhandle(exchange);
        } catch (Throwable e) {
            logger.error(e.getMessage());
            byte[] error = "Invalid Request".getBytes();
            exchange.sendResponseHeaders(500, error.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(error);
        } finally {
            exchange.close();
        }
    }

    protected abstract void logicalhandle(HttpExchange exchange) throws IOException, JSONException;

    private static byte[] toBytes(String message) {
        if (message == null) {
            return new byte[0];
        }
        return message.getBytes();
    }
    public static void response(HttpExchange exchange,int code,String content) throws IOException {
        byte[] res = content.getBytes();
        exchange.sendResponseHeaders(code, res.length);
        OutputStream out = exchange.getResponseBody();
        out.write(res);
    }
}
