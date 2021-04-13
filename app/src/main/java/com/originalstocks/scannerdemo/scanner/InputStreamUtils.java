package com.originalstocks.scannerdemo.scanner;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtils {
    static final int BUFFER_SIZE = 4096;
    static final String CHAR_CODE = "UTF-8";

    public InputStreamUtils() {
    }

    public static String InputStreamTOString(InputStream in) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        boolean var3 = true;

        int count;
        while ((count = in.read(data, 0, 4096)) != -1) {
            outStream.write(data, 0, count);
        }

        data = null;
        return new String(outStream.toByteArray(), "UTF-8");
    }

    public static String InputStreamTOString(InputStream in, String encoding) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        boolean var4 = true;

        int count;
        while ((count = in.read(data, 0, 4096)) != -1) {
            outStream.write(data, 0, count);
        }

        data = null;
        return new String(outStream.toByteArray(), "UTF-8");
    }

    public static InputStream StringTOInputStream(String in) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes("UTF-8"));
        return is;
    }

    public static byte[] InputStreamTOByte(InputStream in) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        boolean var3 = true;

        int count;
        while ((count = in.read(data, 0, 4096)) != -1) {
            outStream.write(data, 0, count);
        }

        data = null;
        return outStream.toByteArray();
    }

    public static InputStream byteTOInputStream(byte[] in) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(in);
        return is;
    }

    public static String byteTOString(byte[] in) throws Exception {
        InputStream is = byteTOInputStream(in);
        return InputStreamTOString(is);
    }
}
