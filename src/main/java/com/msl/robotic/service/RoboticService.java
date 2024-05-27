package com.msl.robotic.service;

import com.msl.robotic.param.PointParam;
import com.msl.robotic.util.GripperUtil;
import com.msl.robotic.util.LuaParamEnum;
import com.msl.robotic.util.LuaParamUtil;
import com.msl.robotic.vo.PointVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Socket;
import java.util.*;

import static com.msl.robotic.util.EdictUtil.*;

@Service
public class RoboticService {

    private static Map<Integer,PointVo> pointMap = new LinkedHashMap<>();
    private static Socket socket = null;

    private static Integer id = 0;

    @Autowired
    private RoboticService roboticService;

    @Autowired
    private GripperService gripperService;

    //建立连接
    public int connectET() {
        String robotIp = "192.168.1.200";
        int port = 8055;
        socket = connectETController(robotIp, port);
        if (socket != null){
            return 1;
        }else {
            return 0;
        }
    }


    //断开连接
    public int disconnectET() {
        disconnectETController(socket);
        return 1;
    }

    //获得当前节点位姿
    public PointVo getNowPoint(){
        Object[] zeroBase = getZeroBase(socket);
        Integer nowId = id++;
        return PointVo.of(nowId,zeroBase);
    }

    //根据坐标移动
    public int movePoint(PointParam param) throws InterruptedException {
        Object[] weizi = pointToWeizi(socket, param.getPoint());
        boolean operateFlag = lineMove(socket, Collections.singletonList(weizi));
//        pointMap.put(param.getId(),PointVo.of(param.getId(),param.getPoint()));
//        wzMap.put(param.getId(),PointVo.of(param.getId(),weizi));

        //先获取机械臂操作的返回状态，机械臂执行结束后，在操作爪子
        if (operateFlag) {
            GripperUtil.CommandResult commandResultD0 = GripperUtil.sendCMD(socket, "setSysVarD", LuaParamUtil.buildParameter(LuaParamEnum.SCRIPT_MODE, 1), 1, true);
            System.out.println("commandResultD0---" + commandResultD0.result.toString());

            GripperUtil.CommandResult commandResultD2 = GripperUtil.sendCMD(socket, "setSysVarD", LuaParamUtil.buildParameter(LuaParamEnum.TARGET_WIDTH, 100), 1, true);
            System.out.println("commandResultD2---" + commandResultD2.result.toString());

            GripperUtil.CommandResult commandResultD1 = GripperUtil.sendCMD(socket, "setSysVarD", LuaParamUtil.buildParameter(LuaParamEnum.SEND_MODE, 1), 1, true);
            System.out.println("commandResultD1---" + commandResultD1.result.toString());
        }

        return 1;
    }

    //所有坐标节点
    public List<PointVo>  listPoints(){
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
        for (Integer id : list) {
            wzList.add(pointMap.get(id).getPoint());
        }
        lineListMove(socket,wzList);
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

        pointMap.put(param.getId(),weizi);
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

    public Integer ELITEconnectGRIPPER(Integer weight) {
        if (socket != null) {
            // Example command
            GripperUtil.CommandResult commandResult = GripperUtil.sendCMD(socket, "getRobotPose", null, 1, true);
            System.out.println(commandResult.result);

            // Example of inverse kinematic command
            if (commandResult.success) {
                Map pointMap = new HashMap<>();
                pointMap.put("targetPose", commandResult.result);

                GripperUtil.CommandResult inverseResult = GripperUtil.sendCMD(socket, "inverseKinematic", pointMap, 2, true);
                System.out.println(inverseResult.result);
            }

            // Example of setting control variables
            Map mapsetD0 = new HashMap<>();
            mapsetD0.put("addr", 0);
            mapsetD0.put("value", 1);

            Map mapsetD2 = new HashMap<>();
            mapsetD2.put("addr", 2);
            mapsetD2.put("value", weight);

            Map mapsetD1 = new HashMap<>();
            mapsetD1.put("addr", 1);
            mapsetD1.put("value", 1);

            GripperUtil.CommandResult commandResultD0 = GripperUtil.sendCMD(socket, "setSysVarD", mapsetD0, 1, true);
            System.out.println("commandResultD0---" + commandResultD0.result.toString());

            GripperUtil.CommandResult commandResultD2 = GripperUtil.sendCMD(socket, "setSysVarD", mapsetD2, 1, true);
            System.out.println("commandResultD2---" + commandResultD2.result.toString());

            GripperUtil.CommandResult commandResultD1 = GripperUtil.sendCMD(socket, "setSysVarD", mapsetD1, 1, true);
            System.out.println("commandResultD1---" + commandResultD1.result.toString());
            return 1;
        } else {
            System.out.println("连接机器人失败。");
            return 0;
        }
    }
}
