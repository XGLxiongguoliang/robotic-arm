package com.msl.robotic.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.msl.robotic.param.PointParam;
import com.msl.robotic.util.EdictUtil;
import com.msl.robotic.util.GripperUtil;
import com.msl.robotic.util.LuaParamEnum;
import com.msl.robotic.util.LuaParamUtil;
import com.msl.robotic.vo.PointVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.msl.robotic.util.EdictUtil.*;

@Service
public class RoboticService {

    private static Map<Integer, PointVo> pointMap = new LinkedHashMap<>();
    private static Socket socket = null;

    private static Integer id = 0;

    static {
        pointMap.put(1, PointVo.of(1, new Object[]{99.62913223140491,-79.97339876033057,82.34993811881189,-91.84567901234568,88.63657407407408,-85.19945987654319}));
        pointMap.put(2, PointVo.of(2, new Object[]{115.60924586776856,-83.27195247933884,85.11850247524751,-83.55054012345678,70.19637345679011,-67.8483796296296}));
        pointMap.put(3, PointVo.of(3, new Object[]{137.22546487603304,-70.86105371900823,68.16120049504948,-72.2114197530864,74.63541666666666,-45.63618827160492}));
    }

    //建立连接
    public int connectET() {
        String robotIp = "192.168.1.200";
        int port = 8055;
        socket = connectETController(robotIp, port);
        if (socket != null) {
            return 1;
        } else {
            return 0;
        }
    }


    //断开连接
    public int disconnectET() {
        disconnectETController(socket);
        return 1;
    }

    //获得当前节点位姿
    public PointVo getNowPoint() {
        Object[] zeroBase = getZeroBase(socket);
        Integer nowId = id++;
        return PointVo.of(nowId, zeroBase);
    }

    //根据坐标移动
    public int movePoint(PointParam param) throws InterruptedException {

        Object[] weizi = pointToWeizi(socket, param.getPoint());
        lineMove(socket, Collections.singletonList(weizi));
//        pointMap.put(param.getId(),PointVo.of(param.getId(),param.getPoint()));
//        wzMap.put(param.getId(),PointVo.of(param.getId(),weizi));

        while (true) {
            JSONObject robotState = getRobotState(socket);
            if (robotState.get("result").equals("0")) {
                EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SCRIPT_MODE, 1d));
                EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_WIDTH, 1000d));
                EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1d));
                break;
            }
        }


        //先获取机械臂操作的返回状态，机械臂执行结束后，在操作爪子
//        EdictUtil.setSysD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SCRIPT_MODE, 1));
//        EdictUtil.setSysD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_WIDTH, 1000));
//        EdictUtilictUtil.setSysD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1));

        return 1;
    }

    //所有坐标节点
    public List<PointVo> listPoints() {
        Collection<PointVo> values = pointMap.values();
        ArrayList<PointVo> list = new ArrayList<>();
        list.addAll(values);
        return list;
    }

    //根据坐标旋转
    public int moveWeizi(PointParam param) throws InterruptedException {
        lineMove(socket, Collections.singletonList(param.getPoint()));
        //正解
//        Object[] point = weiziToPoint(socket, param.getPoint());
//        pointMap.put(param.getId(),PointVo.of(param.getId(),point));
//        wzMap.put(param.getId(),PointVo.of(param.getId(),param.getPoint()));
        return 1;
    }

    //轨迹运动
    public int movePath(List<Integer> list) {
        ArrayList<Object[]> wzList = new ArrayList<>();
        int i = 0;
        for (Integer id : list) {
            wzList.add(pointMap.get(id).getPoint());

            JSONObject params = new JSONObject();
            params.put("wayPoint", pointMap.get(id).getPoint());
            params.put("moveType", 0);
            params.put("speed", 5);
            params.put("circular_radius", 20);

            while (true) {
                JSONObject robotStateWai = getRobotState(socket);
                if (robotStateWai.get("result").equals("0")) {
                    EdictUtil.sendCMD(socket, "addPathPoint", params, i++);
                    lineListMove(socket, wzList, i);

                    //模拟初始位，抓物品位，放物品位，当位置1时候张开爪子；位置2的时候闭合爪子，位置三的时候张开爪子
                    if (id == 1) {
                        while (true) {
                            JSONObject robotState = getRobotState(socket);
                            if (robotState.get("result").equals("0")) {
                                try {
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SCRIPT_MODE, 1d));
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_WIDTH, 1000d));
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1d));
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            }
                        }
                    }

                    if (id == 2) {
                        while (true) {
                            JSONObject robotState = getRobotState(socket);
                            if (robotState.get("result").equals("0")) {
                                try {
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SCRIPT_MODE, 1d));
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_WIDTH, 265d));
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1d));
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            }
                        }
                    }

                    if (id == 3) {
                        while (true) {
                            JSONObject robotState = getRobotState(socket);
                            if (robotState.get("result").equals("0")) {
                                try {
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SCRIPT_MODE, 1d));
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_WIDTH, 1000d));
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1d));
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            }
                        }
                    }

                    break;
                }
            }
        }

        return 1;
    }

    public PointVo getWeizi(Integer id) {
        return pointMap.get(id);
    }

    public int savePoint(PointParam param) throws InterruptedException {
        PointVo weizi = new PointVo();
        weizi.setId(param.getId());
        weizi.setName(param.getName());
        weizi.setPoint(param.getPoint());

        pointMap.put(param.getId(), weizi);
        return 1;
    }

    public int deletePoint(Integer id) {
        pointMap.remove(id);
        return 1;
    }

    public PointVo next(PointParam param) throws InterruptedException {
        //逆解
        Object[] weizi = pointToWeizi(socket, param.getPoint());
        PointVo weiziVo = new PointVo();
        weiziVo.setId(param.getId());
        weiziVo.setName(param.getName());
        weiziVo.setPoint(weizi);

        return weiziVo;
    }

    public double getGripperCurrentWidth() {
        try {
            double width = getSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_WIDTH, 1d));
            return width;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Double getGripperCurrentForce() {
        try {
            double width = getSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_FORCE, 1d));
            return width;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Double getGripperCurrentSpeed() {
        try {
            double width = getSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_SPEED, 1d));
            return width;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String setGripperWidth(double weight) {
        try {
            EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1d));
            EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_WIDTH, weight));
            EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1d));
            return "设置爪子宽度成功";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String setGripperForce(double force) {
        try {
            EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 2d));
            EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_FORCE, force));
            EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1d));
            return "设置爪子力度成功";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String setGripperSpeed(double speed) {
        try {
            EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 3d));
            EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_SPEED, speed));
            EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1d));
            return "设置爪子速度成功";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getGripperCurrentWidthForceSpeed() {
        double gripperCurrentWidth = getGripperCurrentWidth();
        double gripperCurrentForce = getGripperCurrentForce();
        double gripperCurrentSpeed = getGripperCurrentSpeed();
        List<Double> resultList = new ArrayList<>();
        resultList.add(gripperCurrentWidth);
        resultList.add(gripperCurrentForce);
        resultList.add(gripperCurrentSpeed);
        return JSONArray.toJSONString(resultList);
    }

    public Integer setGripperCurrentWidthForceSpeed(List<Integer> list) {
        setGripperWidth(list.get(0));
        setGripperForce(list.get(1));
        setGripperSpeed(list.get(2));
        return 1;
    }
}
