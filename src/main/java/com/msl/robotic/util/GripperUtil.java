package com.msl.robotic.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GripperUtil {
    private String robotIp;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    public GripperUtil(String robotIp) {
        this.robotIp = robotIp;
    }

    // 连接机器人
    public boolean connect() {
        try {
            socket = new Socket(robotIp, 8055);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 断开连接
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 发送命令
    private String sendCommand(String command) throws IOException {
        String jsonCommand = String.format("{\"jsonrpc\":\"2.0\",\"method\":\"%s,\"id\":1}", command);
        System.out.println("sendCommand: " + jsonCommand);
        outputStream.write(jsonCommand.getBytes(StandardCharsets.ISO_8859_1));
        outputStream.flush();
        // 如果需要，请添加读取响应的逻辑
        return jsonCommand;
    }

    // 打开串口
    public void openSerialPort(int deviceType) throws IOException {
        String command = String.format("open_serial_port\",\"params\":{\"device_type\":%d}", deviceType);
        sendCommand(command);
    }

    // 配置串口
    public void configureSerialPort(int baudRate, int bits, String event, int stop) throws IOException {
        String command = String.format("setopt_serial_port\",\"params\":{\"baud_rate\":%d,\"bits\":%d,\"event\":\"%s\",\"stop\":%d}", baudRate, bits, event, stop);
        sendCommand(command);
    }

    // 发送数据
    public void sendData(byte[] data) throws IOException {
        String command = String.format("send_serial_data\",\"params\":{\"data\":%s}", bytesToHex(data));
        sendCommand(command);
    }

    // 重置爪子
    public void resetGripper() throws IOException {
        byte[] command = {0x01, 0x06, 0x01, 0x00, 0x00, 0x01, 0x49, (byte) 0xF6};
        sendData(command);
    }

    // 重置爪子
    public void resetGripperAll() throws IOException {
        byte[] command = {0x01, 0x06, 0x01, 0x00, 0x00, (byte)0xA5, 0x48, 0x4D};
        sendData(command);
    }

    // 关闭串口
    public void closeSerialPort() throws IOException {
        sendCommand("close_serial_port");
    }

    // 读取反馈数据
    public String recvSerialPort() throws IOException {
        return sendCommand("recv_serial_port");
    }

    // 将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
            if (i < bytes.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static void main(String[] args) {
        GripperUtil gripperUtil = new GripperUtil("172.16.11.248");
        if (gripperUtil.connect()) {
            try {
                gripperUtil.openSerialPort(1); // 0-RS232 1-RS485通信
                gripperUtil.configureSerialPort(115200, 8, "N", 1);
                gripperUtil.resetGripper();
                gripperUtil.closeSerialPort();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                gripperUtil.disconnect();
            }
        } else {
            System.out.println("连接机器人失败。");
        }
    }
}
