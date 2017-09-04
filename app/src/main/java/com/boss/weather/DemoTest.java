package com.boss.weather;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by bojia on 2017-09-04.
 */

public class DemoTest {
    public final static void main(String[] args) {
        startSocketClent();
    }

    private final static int LOCAL_PORT = 0; // any open port
    private final static int REMOTE_PORT = 9999;
    /**
     * 先在手机端启动应用
     */
    public static void startSocketClent() {
        try {
            // 等待设备连接
            String script_wait = "adb wait-for-device";
            Process p = Runtime.getRuntime().exec(script_wait);
            System.out.println(String.format("exec \'%s\'", script_wait));
            DataInputStream dis = new DataInputStream(p.getInputStream());
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = dis.read(buffer)) != -1) {
                String s = new String(buffer, 0, len);
                System.out.println("get " + s);
            }
            // 设置端口转发
            String script_forward = String.format("adb forward tcp:%s tcp:%s", LOCAL_PORT, REMOTE_PORT);
            Process p1 = Runtime.getRuntime().exec(script_forward);
            System.out.println(String.format("exec \'%s\'", script_forward));
            dis = new DataInputStream(p.getInputStream());
            dos = new DataOutputStream(p.getOutputStream());
            while ((len = dis.read(buffer)) != -1) {
                String s = new String(buffer, 0, len);
                System.out.println("get " + s);
            }
            // 启动socket，连接本地端口
            final Socket socket = new Socket("127.0.0.1", REMOTE_PORT);
            if (socket.isConnected()) {
                System.out.println("socket connected");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataInputStream dis = new DataInputStream(socket.getInputStream());

                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = dis.read(buffer)) != -1) {
                                String s = new String(buffer, 0, len);
                                System.out.println("--->get from Android client:" + s);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataOutputStream dos1 = new DataOutputStream(socket.getOutputStream());
                            dos1.write("hi".getBytes());
                            dos1.flush();
                            System.out.println("send:\'hi\'");
                            System.out.println("------------");

                            // ------- input
                            String input;
                            Scanner scanner = new Scanner(System.in);
                            while (true) {
                                System.out.println(String.format("input:"));
                                input = scanner.nextLine();
                                if (input.length() == 0) {
                                    System.out.println(String.format("length is 0"));
                                    continue;
                                }
                                dos1.write(input.getBytes());
                                dos1.flush();
                                System.out.println(String.format("send:\'%s'", input));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println(String.format("finish"));
                        }
                    }
                }).start();
            } else {
                System.out.println("socket is not connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
