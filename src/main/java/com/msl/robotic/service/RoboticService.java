package com.msl.robotic.service;

import com.msl.robotic.param.PointParam;
import com.msl.robotic.util.GripperOwnUtil;
import com.msl.robotic.util.GripperUtil;
import com.msl.robotic.vo.PointVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
        lineMove(socket, Collections.singletonList(weizi));
//        pointMap.put(param.getId(),PointVo.of(param.getId(),param.getPoint()));
//        wzMap.put(param.getId(),PointVo.of(param.getId(),weizi));
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

    public void demo() {
        // 初始化RS485电爪控制器
        GripperOwnUtil gripper = new GripperOwnUtil("COM3"); // 替换为实际的COM端口

        if (gripper.connect()) {
            System.out.println("Connected to the gripper.");

            // 连接机械臂
            roboticService.connectET();

            // 移动机械臂到目标位置
            try {
                roboticService.movePoint(new PointParam());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 打开电爪
            gripper.openGripper();
            try {
                Thread.sleep(1000); // 等待电爪打开
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 关闭电爪以夹取物体
            gripper.closeGripper();
            try {
                Thread.sleep(1000); // 等待电爪关闭
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 移动机械臂到新的位置
            try {
                roboticService.movePoint(new PointParam());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 释放物体
            gripper.openGripper();
            try {
                Thread.sleep(1000); // 等待电爪打开
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 断开连接
            gripper.disconnect();
            roboticService.disconnectET();
            System.out.println("Disconnected from the gripper and arm.");
        } else {
            System.out.println("Failed to connect to the gripper.");
        }
    }

    public Integer ELITEconnectGRIPPER() {
        GripperUtil gripperUtil = new GripperUtil("192.168.1.200");
        if (gripperUtil.connect()) {
            try {
                gripperUtil.openSerialPort(1); // 0-RS232 1-RS485
                gripperUtil.configureSerialPort(115200, 8, "N", 1);
                gripperUtil.resetGripper();
                System.out.println(gripperUtil.recvSerialPort());
                //gripperUtil.resetGripperAll();

                /*// 读取反馈数据
                String feedback = gripperUtil.readData();
                if (feedback != null) {
                    System.out.println("Received feedback: " + feedback);
                } else {
                    System.out.println("No feedback received.");
                }*/

                gripperUtil.closeSerialPort();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                gripperUtil.disconnect();
            }
            return 1;
        } else {
            System.out.println("连接机器人失败。");
            return 0;
        }
    }
}
