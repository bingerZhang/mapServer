package com.util;

import java.net.URL;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.*;

public class PropertiesUtils {
    public static Map<String, String> getProperties(File file) throws IOException {
        if (!file.exists()) {
            return new LinkedHashMap<String, String>();
        }
        return getProperties(new FileInputStream(file));
    }

    public static Map<String, String> getProperties(URL url) throws IOException {
        InputStream in = url.openStream();
        if (in == null) {
            return new LinkedHashMap<String, String>();
        }
        return getProperties(in);
    }

    public static Map<String, String> getProperties(BufferedReader in) throws IOException {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            int index = line.indexOf('=');
            if (index > 0) {
                String name = trim(line.substring(0, index));
                String value = trim(line.substring(index + 1));
                properties.put(name, value);
            }
        }
        return properties;
    }

    public static Map<String, String> getProperties(InputStream in) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            return getProperties(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static void setProperties(File file, Map<String, String> properties) throws IOException {
        File path = file.getParentFile();
        if (!path.exists()) {
            path.mkdirs();
        }
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            setProperties(out, properties);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void setProperties(BufferedWriter out, Map<String, String> properties) throws IOException {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null) {
                out.write(key + "=" + value);
                out.newLine();
            }
        }
        out.flush();
    }

    public static String getProperty(Map<String, String> properties, String name, String defaultValue) {
        String value = properties.get(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static boolean getProperty(Map<String, String> properties, String name, boolean defaultValue) {
        String value = properties.get(name);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public static String[] getProperty(Map<String, String> properties, String name, String[] defaultValues) {
        String value = properties.get(name);
        if (value == null) {
            return defaultValues;
        }
        return StringUtils.split(value, ";");
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

}
