package com.launch;

import com.util.ArrayUtils;

import java.util.ServiceLoader;

/**
 * Created by zlb on 2016/7/26.
 */
public class ConfigTools implements Main{
        private static ServiceLoader<Tool> tools = ServiceLoader.load(Tool.class);
        public void main(String[] args) throws Exception {
            if (args.length == 0) {
                System.out.println("[WARN] Invalid config action");
                return;
            }
            String action = args[0];
            if ("version".equals(action)) {
//                String version = getVersion();
                System.out.println("Version: " + "1.0");
            } else {
                String appServer = Application.getAppServer();
                String jdbcHost = Application.getProperty("application.jdbc.host", appServer);
                Application.setProperty("jdbc.url", "jdbc:mysql://" + jdbcHost + ":3306/application?autoReconnect=true&useUnicode=TRUE&characterEncoding=UTF8");
                Tool tool = getTool(action);
                if (tool == null) {
                    System.out.println("# Invalid config action.");
                    return;
                }
                tool.main(shift(args));
            }
        }

        private static Tool getTool(String name) {
            for (Tool tool : tools) {
                if (name.equals(tool.getName())) {
                    return tool;
                }
            }
            return null;
        }
        private static String[] shift(String[] args) {
            return ArrayUtils.remove(args, 0);
        }
}
