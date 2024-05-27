package com.msl.robotic.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GripperUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static class CommandResult {
        public boolean success;
        public Object result;
        public int id;

        public CommandResult(boolean success, Object result, int id) {
            this.success = success;
            this.result = result;
            this.id = id;
        }
    }

    public static CommandResult sendCMD(Socket socket, String cmd, Object params, int id, boolean retFlag) {
        if (params == null) {
            params = new HashMap<>();
        }
        String paramsStr;
        try {
            paramsStr = objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommandResult(false, null, id);
        }

        String sendStr = String.format("{\"method\":\"%s\",\"params\":%s,\"jsonrpc\":\"2.0\",\"id\":%d}\n", cmd, paramsStr, id);

        try {
            OutputStream out = socket.getOutputStream();
            out.write(sendStr.getBytes(StandardCharsets.UTF_8));
            out.flush();

            if (retFlag) {
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int readBytes = in.read(buffer);
                if (readBytes != -1) {
                    String response = new String(buffer, 0, readBytes, StandardCharsets.UTF_8);
                    Map<String, Object> jdata = objectMapper.readValue(response, Map.class);
                    if (jdata.containsKey("result")) {
                        return new CommandResult(true, objectMapper.readValue(jdata.get("result").toString(), Object.class), (int) jdata.get("id"));
                    } else if (jdata.containsKey("error")) {
                        return new CommandResult(false, jdata.get("error"), (int) jdata.get("id"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CommandResult(false, null, id);
    }

    public static void computerDirectGripper() {
        // 列出所有可用的串行端口
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            System.out.println("Available Port: " + port.getSystemPortName());
        }

        // 打开指定的串行端口（替换为实际的端口名称）
        SerialPort comPort = SerialPort.getCommPort("COM3");
        comPort.setBaudRate(115200); // 根据需要设置波特率
        comPort.setNumDataBits(8);
        comPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        comPort.setParity(SerialPort.NO_PARITY);

        // 打开端口并检查是否成功
        if (!comPort.openPort()) {
            System.out.println("Failed to open port.");
            return;
        }

        System.out.println("Port is open.");

        // 设置流控制
        comPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

        // 要发送的命令
        byte[] command = {0x01, 0x06, 0x01, 0x00, 0x00, (byte)0xA5, 0x48, 0x4D};
        try {
            // 确保端口已准备好发送数据
            if (comPort.isOpen() && comPort.getOutputStream() != null) {
                // 发送命令
                int bytesWritten = comPort.writeBytes(command, command.length);
                System.out.println("Sent " + bytesWritten + " bytes.");

                // 等待设备响应
                Thread.sleep(100); // 等待100毫秒

                // 接收反馈
                byte[] readBuffer = new byte[1024];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                System.out.println("Read " + numRead + " bytes.");

                // 打印反馈数据
                for (int i = 0; i < numRead; i++) {
                    System.out.printf("%02X ", readBuffer[i]);
                }
                System.out.println();
            } else {
                System.out.println("Failed to write to port. OutputStream is null or port is closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭端口
            if (comPort.isOpen()) {
                comPort.closePort();
                System.out.println("Port is closed.");
            }
        }
    }
}
