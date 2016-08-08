package com.launch;

import com.map.MapUtil;
import com.map.Parser;
import com.map.Point;
import com.server.TrackerServer;
import com.util.PropertiesUtils;
import com.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by zlb on 2016/7/21.
 */
public class Application {
    public static final String ROOT_PATH;
    public static final String CONF_PATH;
    public static final String DATA_PATH;
    public static final String TEMP_PATH;
    public static final String OS_NAME;
    private static Logger logger = Logger.getLogger("Application");
    public static List<List<String>>  MapInfo = null;
    public static Map<String,List<Point>> motorwayInfo = null;
    public static Map<String, List<List<Point>>> motorwaylines = null;

    static {
        ROOT_PATH = System.getProperty("application.home", "/opt/tracker");
        OS_NAME = System.getProperty("os.name","linux");
        CONF_PATH = ROOT_PATH + "/conf";
        DATA_PATH = ROOT_PATH + "/data";
        TEMP_PATH = DATA_PATH + "/tmp";
        try {
            loadProperties("application.properties");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isWindows() {
        return OS_NAME.toLowerCase().contains("ind");
    }

    public static boolean isLinux() {
        return OS_NAME.toLowerCase().contains("nux");
    }

    public static String getProperty(String name) {
        return System.getProperty(name);
    }

    public static String getProperty(String name, String defaultValue) {
        return System.getProperty(name, defaultValue);
    }

    public static String[] getProperty(String name, String[] defaultValue) {
        String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return StringUtils.split(value, ",;");
    }

    public static int getProperty(String name, int defaultValue) {
        String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public static long getProperty(String name, long defaultValue) {
        String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }

    public static boolean getProperty(String name, boolean defaultValue) {
        String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public synchronized static void setProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    public synchronized static void setProperties(Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                System.clearProperty(name);
            } else {
                System.setProperty(name, value);
            }
        }
    }

    public synchronized static void setLocale(String language, String country) {
        setProperty("user.language", language);
        setProperty("user.country", country);
        Locale locale = new Locale(language, country);
        Locale.setDefault(locale);
    }

    public synchronized static void setTimeZone(String timezone) {
        setProperty("user.timezone", timezone);
        TimeZone.setDefault(TimeZone.getTimeZone(timezone));
    }

    public static File createTempFile(String type, String prefix, String suffix) throws IOException {
        File tempPath = new File(TEMP_PATH, type);
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
        return new File(tempPath, prefix + UUID.randomUUID() + suffix);
    }

    public static File createTempFile(String type, String fileName) throws IOException {
        File tempPath = new File(TEMP_PATH, type);
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
        return new File(tempPath, fileName);
    }

    private static String getName(String line) {
        if (line.startsWith("#")) {
            line = line.substring(1);
        }
        int index = line.indexOf('=');
        if (index == -1) {
            return null;
        }
        return line.substring(0, index);
    }


    private static void loadProperties(String name) throws IOException {
        URL url = Application.class.getClassLoader().getResource(name);
        if (url != null) {
            setProperties(PropertiesUtils.getProperties(url));
        }
        File applicationFile = new File(CONF_PATH, name);
        if (applicationFile.exists()) {
            setProperties(PropertiesUtils.getProperties(applicationFile));
        }
    }
    public static String getAppServer(){
        String host = Application.getProperty("application.host");
        if (host == null) {
            return "127.0.0.1";
        }
        return host;
    }
}
