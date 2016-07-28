package com.util;

import com.map.Point;

/**
 * Created by zlb on 2016/7/22.
 */
public class Utils {
    public static String findParamValue(String query, String name) {
        if (query == null || name == null) {
            return null;
        }
        int paramBegin;
        int paramEnd = -1;
        int len = query.length();
        while (true) {
            if (paramEnd == len) {
                return null;
            }
            paramBegin = paramEnd == -1 ? 0 : paramEnd + 1;
            int idx = query.indexOf('&', paramBegin);
            paramEnd = idx == -1 ? len : idx;
            if (paramEnd > paramBegin) {
                idx = query.indexOf('=', paramBegin);
                if (idx == -1) {
                    idx = query.indexOf(':', paramBegin);
                }
                if (idx == -1 || idx > paramEnd) {
                    continue;
                }
                int paramNameEnd = idx;
                String paramName = query.substring(paramBegin, paramNameEnd);
                if (name.equals(paramName)) {
                    return query.substring(paramNameEnd + 1, paramEnd);
                }
            }
        }
    }

}
