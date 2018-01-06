package com.pp.lot.service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ParameterUtil {
    /**
     * 从request中获得参数Map，并返回可读的Map,不包含值为空的属性
     *
     * @param request
     * @return
     */
    public static Map<String, Object> getParameterMap(HttpServletRequest request) {
        // 参数Map
        Map properties = request.getParameterMap();
        // 返回值Map
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }

            if (!StringUtil.isEmpty(value)) {
                returnMap.put(name, value);
            }


        }
        return returnMap;
    }
}
