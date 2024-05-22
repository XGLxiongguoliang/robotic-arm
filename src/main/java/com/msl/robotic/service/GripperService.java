package com.msl.robotic.service;

import com.fazecast.jSerialComm.SerialPort;
import com.msl.robotic.util.GripperUtil;
import org.springframework.stereotype.Service;

public class GripperService {
    public static void main(String[] args) {
        GripperUtil gripperUtil = new GripperUtil("COM3");
        try {
            gripperUtil.initializeGripper(0x01);
            gripperUtil.setForce(50);
            gripperUtil.setPosition(50);
            gripperUtil.setSpeed(50);
            int status = gripperUtil.getInitializationStatus();
            System.out.println("Initialization Status: " + status);
        } finally {
            gripperUtil.disconnect();
        }
    }
}
