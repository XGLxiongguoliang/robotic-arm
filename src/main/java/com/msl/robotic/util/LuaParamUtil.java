package com.msl.robotic.util;

import java.util.HashMap;
import java.util.Map;

public class LuaParamUtil {
    private static String ADDR = "addr";
    private static String ADDR_VALUE = "value";

    public static Map<String, Integer> buildParameter(LuaParamEnum parameter, Integer value) {
        value = limitValue(value, parameter.getMinValue(), parameter.getMaxValue());

        Map<String, Integer> param = new HashMap<>();
        param.put(ADDR, parameter.getKey());
        param.put(ADDR_VALUE, value);
        return param;
    }

    private static int limitValue(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

}
