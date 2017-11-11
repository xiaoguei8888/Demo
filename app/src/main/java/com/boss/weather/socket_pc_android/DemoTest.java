package com.boss.weather.socket_pc_android;

import java.nio.ByteBuffer;

/**
 * Created by bojia on 2017-09-04.
 */

public class DemoTest {
    public final static void main(String[] args) {
//        PCSocketManager.startSocketClent();
//        PCSocketManager.startSocketServer();
        byte[] bytes = intToByteArray(11199);
        System.out.println(String.format("intToByteArray:%s = %s", 11199, bytes));
        System.out.println(String.format("bytes size = %s", bytes.length));

        System.out.println(String.format("byteArrayToInt:%s = %s", 11199, byteArrayToInt(bytes)));
    }

    static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).getInt();
    }
}
