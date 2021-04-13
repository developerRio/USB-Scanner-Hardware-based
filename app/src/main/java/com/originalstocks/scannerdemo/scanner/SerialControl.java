package com.originalstocks.scannerdemo.scanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;

public class SerialControl extends SerialHelper {
    private OnDataReceivedListener lisener;

    public SerialControl(String sPort, String sBaudRate, OnDataReceivedListener l) {
        super(sPort, sBaudRate);
        this.lisener = l;
    }

    private static boolean writeNode(String path, String msg) {
        boolean flag = false;
        File file = new File(path);

        try {
            FileWriter fr = new FileWriter(file);
            fr.write(msg);
            fr.close();
            flag = true;
        } catch (IOException var6) {
            flag = false;
        }

        return flag;
    }

    protected void onDataReceived(ComBean ComRecData) {
        this.lisener.onDataReceived(ComRecData);
    }

    public void getVersoin() {
        this.sendCmd("02F0033044313330323F2E");
    }

    public void stop() {
        this.sendCmd("02F503");
    }

    public void start() {
        this.sendCmd("02F403");
    }

    public void sendCmd(String cmd) {
        if (this.isOpen()) {
            this.sendHex(cmd);
        }

    }

    public void disconnect() {
        this.stopSend();
        this.close();
    }

    public boolean connect() {
        boolean flag = false;

        try {
            this.open();
            flag = true;
        } catch (SecurityException var3) {
        } catch (IOException var4) {
        } catch (InvalidParameterException var5) {
        }

        return flag;
    }

    public boolean powerOn() {
        return writeNode("/sys/devices/virtual/misc/mtgpio/pin", "-wdout65 1");
    }

    public boolean powerOff() {
        return writeNode("/sys/devices/virtual/misc/mtgpio/pin", "-wdout65 0");
    }
}
