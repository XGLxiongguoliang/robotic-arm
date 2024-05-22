package com.msl.robotic.util;

public class UUIDUtil {
    public static String getUUID()
    {
        return java.util.UUID.randomUUID().toString().replace("-","");
    }
}
