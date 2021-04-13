package com.originalstocks.scannerdemo.scanner;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ComBean {
    public byte[] bRec = null;
    public String sRecTime = "";
    public String sComPort = "";
    public String sCmd = "";

    public ComBean(String sPort, byte[] buffer, int size, String cmd) {
        this.sComPort = sPort;
        this.bRec = new byte[size];

        for (int i = 0; i < size; ++i) {
            this.bRec[i] = buffer[i];
        }

        SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
        this.sRecTime = sDateFormat.format(new Date());
        this.sCmd = cmd;
    }
}

