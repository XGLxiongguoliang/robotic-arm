package com.msl.robotic.util;

import com.fazecast.jSerialComm.SerialPort;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GripperUtil {
    private SerialPort serialPort;

    public GripperUtil(String portName) {
        this.serialPort = SerialPort.getCommPort(portName);
        this.serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);
        if (!serialPort.openPort()) {
            throw new RuntimeException("Failed to open port");
        }
    }

    private void sendCommand(int address, int value) {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) address);
        buffer.put((byte) 0x06); // Function code for writing a single register
        buffer.putShort((short) value);

        byte[] command = buffer.array();
        serialPort.writeBytes(command, command.length);
    }

    private int readCommand(int address) {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) address);
        buffer.put((byte) 0x03); // Function code for reading holding registers
        buffer.putShort((short) 1); // Number of registers to read

        byte[] command = buffer.array();
        serialPort.writeBytes(command, command.length);

        byte[] readBuffer = new byte[5];
        int numRead = serialPort.readBytes(readBuffer, readBuffer.length);

        if (numRead != 5) {
            throw new RuntimeException("Failed to read response");
        }

        ByteBuffer responseBuffer = ByteBuffer.wrap(readBuffer);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.getShort(); // Skip address
        responseBuffer.get(); // Skip function code
        return responseBuffer.getShort();
    }

    public void initializeGripper(int mode) {
        sendCommand(0x0100, mode);
    }

    public void setForce(int force) {
        sendCommand(0x0102, force);
    }

    public void setPosition(int position) {
        sendCommand(0x0103, position);
    }

    public void setSpeed(int speed) {
        sendCommand(0x0104, speed);
    }

    public int getInitializationStatus() {
        return readCommand(0x0200);
    }

    public int getGripStatus() {
        return readCommand(0x0201);
    }

    public int getPosition() {
        return readCommand(0x0202);
    }

    public void saveConfiguration() {
        sendCommand(0x0300, 0x01);
    }

    public void setInitializationDirection(int direction) {
        sendCommand(0x0301, direction);
    }

    public void setDeviceID(int id) {
        sendCommand(0x0302, id);
    }

    public void setBaudRate(int rate) {
        sendCommand(0x0303, rate);
    }

    public boolean connect() {
        return serialPort.openPort();
    }

    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }

    // New methods
    public void openGripper() {
        // Assuming 0 is fully open position
        setPosition(0);
    }

    public void closeGripper() {
        // Assuming 100 is fully closed position
        setPosition(100);
    }

    public void releaseObject() {
        // Assuming a middle position to release the object
        setPosition(50);
    }
}
