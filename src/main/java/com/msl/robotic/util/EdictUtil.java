package com.msl.robotic.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EdictUtil {

    //参考位置
    static double[] baseWeizhi = new double[]{0, -90, 90, -90, 90, 0};

    public static Socket connectETController(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            return socket;
        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
            throw new RuntimeException("Connection failed: " + e.getMessage());
        }
    }

    public static void disconnectETController(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error when closing socket: " + e.getMessage());
                throw new RuntimeException("Error when closing socket:: " + e.getMessage());
            }
        }
    }

    //获得当前状态
    public static JSONObject getRobotState(Socket socket) {
        return sendCMD(socket, "getRobotState", new JSONObject(), 1);

    }

    //获得当前位姿
    public static Object[] getZeroBase(Socket socket) {
        JSONObject params = new JSONObject();
        params.put("coordinate_num", -1);
        params.put("tool_num", -1);
        params.put("unit_type", 0);
        JSONObject jsonObject = sendCMD(socket, "get_tcp_pose", params, 2);
        System.out.println("获取基座位姿：===" + JSON.toJSONString(jsonObject));
        return JSON.parseArray(jsonObject.get("result").toString()).toArray();
    }

    //发送指令
    public static JSONObject sendCMD(Socket socket, String cmd, JSONObject params, int id) {
        JSONObject json = new JSONObject();
        json.put("method", cmd);
        json.put("params", params);
        json.put("jsonrpc", "2.0");
        json.put("id", id);

        try {
            System.out.println("Sending: " + JSON.toJSONString(json));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            writer.write(json.toJSONString() + "\n");
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String response = reader.readLine();
            System.out.println("Response: " + JSON.toJSONString(response));
            return JSONObject.parseObject(response);
        } catch (IOException e) {
            System.out.println("Error during communication: " + e.getMessage());
            throw new RuntimeException("Error during communication:" + e.getMessage());
        }
    }

    //获得当前位姿转化为坐标
    public static Object[] pointToWeizi(Socket socket, Object[] point) throws InterruptedException {
        JSONObject robotState = getRobotState(socket);
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 1);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
            Thread.sleep(1000);
        }


        JSONObject params = new JSONObject();
        params.put("targetPose", point);
        params.put("referencePos", baseWeizhi);
        params.put("unit_type", 0);
        JSONObject jsonObject = sendCMD(socket, "inverseKinematic", params, 2);
        System.out.println("获取某位姿的坐标：===" + JSON.toJSONString(jsonObject));
        return JSON.parseArray(jsonObject.get("result").toString()).toArray();
    }

    //输入坐标转化为位姿
    public static Object[] weiziToPoint(Socket socket, Object[] wz) throws InterruptedException {
        JSONObject robotState = getRobotState(socket);
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 1);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
            Thread.sleep(1000);
        }

        JSONObject params = new JSONObject();
        params.put("targetpos", wz);
        params.put("unit_type", 0);
        JSONObject jsonObject = sendCMD(socket, "positiveKinematic", params, 2);
        System.out.println("获取基座位姿：===" + JSON.toJSONString(jsonObject));
        return JSON.parseArray(jsonObject.get("result").toString()).toArray();
    }

    //直线运动
    public static void lineMove(Socket socket, List<Object[]> points) throws InterruptedException {
        JSONObject robotState = getRobotState(socket);
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 1);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
            Thread.sleep(1000);
        }


        for (Object[] point : points) {
            JSONObject params = new JSONObject();
            params.put("targetPos", point);
            params.put("speed_type", 0);
            params.put("speed", 200);
            JSONObject moveByLine = sendCMD(socket, "moveByLine", params, 2);
            System.out.println("moveByLine---" + moveByLine.toString());
        }
    }

    //轨迹运动
    public static void lineListMove(Socket socket, List<Object[]> points, int i) {

//        int i = 0;
//        for (Object[] o : points) {
//            JSONObject params = new JSONObject();
//            params.put("wayPoint", o);
//            params.put("moveType", 0);
//            params.put("speed", 50);
//            params.put("circular_radius", 20);
//            sendCMD(socket, "addPathPoint", params, i++);
//        }

        sendCMD(socket, "moveByPath", new JSONObject(), i++);

        sendCMD(socket, "clearPathPoint", new JSONObject(), i++);

    }

    //================================


    public static void getActualJoint(Socket socket) throws InterruptedException {
        JSONObject robotState = getRobotState(socket);
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 1);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
            Thread.sleep(1000);
        }
        JSONObject jsonObject = sendCMD(socket, "get_actual_joint", new JSONObject(), 2);
        System.out.println("获取关节：===" + JSON.toJSONString(jsonObject));
    }

    public static void getUserBase(Socket socket) throws InterruptedException, IOException {
        JSONObject robotState = getRobotState(socket);
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 1);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
            Thread.sleep(1000);
        }

        JSONObject params = new JSONObject();
        params.put("user_num", 1);
        JSONObject jsonObject = sendCMD(socket, "getUserFrame", params, 2);
        System.out.println("获取用户：===" + JSON.toJSONString(jsonObject));
    }

    public static void moveBase(Socket socket) throws InterruptedException, IOException {
        JSONObject robotState = getRobotState(socket);
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 1);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
            Thread.sleep(1000);
        }


        List<double[]> points = new ArrayList<>();
        points.add(new double[]{439.261429, 315.793059, -61.858741, -3.141551, 3.4e-05, -2.24669});

        for (double[] point : points) {
            JSONObject params = new JSONObject();
            params.put("v", new double[]{100, 100, 100, 0, 0, 0});
            params.put("acc", new double[]{100, 100, 100, 0, 0, 0});
            params.put("arot", 30);
            params.put("t", 1);
            sendCMD(socket, "moveBySpeedl", params, 1);
        }
    }


    public static void getUserPoint(Socket socket) throws InterruptedException, IOException {
        JSONObject robotState = getRobotState(socket);
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 1);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
            Thread.sleep(1000);
        }

        JSONObject params = new JSONObject();
        JSONObject jsonObject = sendCMD(socket, "get_user_flange_pose", params, 2);
        System.out.println("获取用户位姿：===" + JSON.toJSONString(jsonObject));
    }

    public static void moveXYZ(Socket socket) throws InterruptedException, IOException {
        List<double[]> points = new ArrayList<>();
        points.add(new double[]{439.261429, 315.793059, -61.858741, -3.141551, 3.4e-05, -2.24669});

        JSONObject robotState = getRobotState(socket);
        System.out.println("连接状态：===" + JSON.toJSONString(robotState));
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 2);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
            Thread.sleep(1000);
        }


        for (double[] point : points) {
            JSONObject params = new JSONObject();
            params.put("targetUserPose", point);
            params.put("user_coord", new double[]{100, 100, 100, 0, 0, 0});
            params.put("speed", 30);
            params.put("unit_type", 1);
            sendCMD(socket, "moveByLineCoord", params, 1);
        }
    }

    public static void move(Socket socket) throws InterruptedException, IOException {
        List<double[]> points = new ArrayList<>();
        points.add(new double[]{105.5805785123967, -67.417871900826441, 84.497524752475272, -89.904320987654316, 74.615740740740748, 12.812114197530864});

        JSONObject robotState = getRobotState(socket);
        System.out.println("连接状态：===" + JSON.toJSONString(robotState));
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 2);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
            Thread.sleep(1000);
        }

//        JSONObject jsonObject = sendCMD(socket, "getRobotState", new JSONObject(), 3);
//        System.out.println("连接状态：==="+JSON.toJSONString(jsonObject));
//        if (Objects.equals(robotState.get("result"),"0")){
//           return;
//        }


        for (double[] point : points) {
            JSONObject params = new JSONObject();
            params.put("targetPos", point);
            params.put("speed", 30);
            params.put("acc", 10);
            params.put("dec", 10);
            params.put("cond_type", 0);
            params.put("cond_num", 7);
            params.put("cond_value", 1);
            sendCMD(socket, "moveByJoint", params, 1);
        }

//        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//        while (true) {
//            sendCMD(socket, "getRobotState",new JSONObject(), 1);
//            String response = in.readLine();
//            JSONObject jsonResponse = JSONObject.parseObject(response);
//            if (Objects.equals(jsonResponse.get("result"),"0")){
//                System.out.println("连接状态：==="+JSON.toJSONString(jsonResponse));
//                break;
//            }
//        }
    }

    public static void setSysParamD(Socket socket, JSONObject param) throws InterruptedException {
        JSONObject robotState = getRobotState(socket);
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 1);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
        }

        JSONObject jsonObject = sendCMD(socket, "setSysVarD", param, 2);
        System.out.println("设置爪子参数：===" + JSON.toJSONString(jsonObject));
    }

    public static double getSysParamD(Socket socket, JSONObject param) throws InterruptedException {
        JSONObject robotState = getRobotState(socket);
        if (Objects.equals(robotState.get("result"), "0")) {
            JSONObject params = new JSONObject();
            params.put("status", 1);
            JSONObject jsonObject = sendCMD(socket, "set_servo_status", params, 1);
            System.out.println("设置状态：===" + JSON.toJSONString(jsonObject));
        }

        JSONObject jsonObject = sendCMD(socket, "getSysVarD", param, 2);
        System.out.println("获取爪子参数：===" + JSON.toJSONString(jsonObject));

        return jsonObject.getDouble("result");
    }
}
