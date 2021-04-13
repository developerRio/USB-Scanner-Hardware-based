package com.originalstocks.scannerdemo.scanner;


public class MyFunc {
    public MyFunc() {
    }

    public static int isOdd(int num) {
        return num & 1;
    }

    public static int HexToInt(String inHex) {
        return Integer.parseInt(inHex, 16);
    }

    public static byte HexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    public static String Byte2Hex(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }

    public static String ByteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;

        for (int i = 0; i < j; ++i) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
            strBuilder.append(" ");
        }

        return strBuilder.toString();
    }

    public static String ByteArrToHex(byte[] inBytArr, int offset, int byteCount) {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;

        for (int i = offset; i < j; ++i) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
        }

        return strBuilder.toString();
    }

    public static byte[] HexToByteArr(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {
            ++hexlen;
            result = new byte[hexlen / 2];
            inHex = "0" + inHex;
        } else {
            result = new byte[hexlen / 2];
        }

        int j = 0;

        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            ++j;
        }

        return result;
    }
}
