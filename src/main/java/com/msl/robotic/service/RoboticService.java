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
        pointMap.put(0, PointVo.of(0, new Object[]{93.256198347107443,-91.694214876033044,87.815903465346523,-90.094135802469125,90.293209876543216,-45.602623456790127}));
        pointMap.put(1, PointVo.of(1, new Object[]{80.065857438016508,-49.084194214876057,99.7917698019802,-128.66743827160488,178.38618827160494,-45.608024691358075}));
        pointMap.put(2, PointVo.of(2, new Object[]{85.193956611570229,-77.21539256198335,108.93935643564346,-128.77044753086446,178.38541666666666,-45.608024691357805}));
        pointMap.put(3, PointVo.of(3, new Object[]{152.98657024793388,-61.132747933884325,94.973081683168218,-113.09722222222166,182.66975308641975,-45.609182098765913}));
        pointMap.put(4, PointVo.of(4, new Object[]{95.155216942148726,-93.263171487603344,106.20018564356437,-112.99961419753053,182.68132716049379,-45.60918209876565}));
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
            params.put("speed", 10);
            params.put("circular_radius", 20);

            while (true) {
                JSONObject robotStateWai = getRobotState(socket);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (robotStateWai.get("result").equals("0")) {
                    EdictUtil.sendCMD(socket, "addPathPoint", params, i++);
                    lineListMove(socket, wzList, i);

                    //模拟初始位，抓物品位，闭合爪子;放物品位，张开爪子
                    if (id == 1) {
                        while (true) {
                            JSONObject robotState = getRobotState(socket);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            if (robotState.get("result").equals("0")) {
                                try {
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.SCRIPT_MODE, 1d));
                                    EdictUtil.setSysParamD(socket, LuaParamUtil.buildParameter(LuaParamEnum.TARGET_WIDTH, 460d));
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
