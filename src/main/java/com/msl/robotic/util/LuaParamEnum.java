package com.msl.robotic.util;

public enum LuaParamEnum {
    SCRIPT_MODE(0, "脚本模式", 0, 1),
    SEND_MODE(1, "发送模式", 0, 100),
    TARGET_WIDTH(2, "目标宽度", 0, 1000),
    TARGET_FORCE(3, "目标夹持力", 20, 100),
    TARGET_SPEED(4, "目标速度", 1, 100),
    ACTUAL_WIDTH(5, "位置", 0, 1000),
    ACTUAL_FORCE(6, "当前力度", 20, 100),
    ACTUAL_SPEED(7, "当前速度", 1, 100),
    INITIAL_STATE(8, "初始化状态", 0, 1),
    GRIP_STATUS(9, "夹持状态", 0, 3);

    private final Integer key;
    private final String chineseName;
    private final int minValue;
    private final int maxValue;

    LuaParamEnum(Integer key, String chineseName, int minValue, int maxValue) {
        this.key = key;
        this.chineseName = chineseName;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Integer getKey() {
        return key;
    }

    public String getChineseName() {
        return chineseName;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public static LuaParamEnum fromString(Integer key) {
        for (LuaParamEnum param : LuaParamEnum.values()) {
            if (param.key.equals(key)) {
                return param;
            }
        }
        throw new IllegalArgumentException("Invalid parameter key: " + key);
    }
}
