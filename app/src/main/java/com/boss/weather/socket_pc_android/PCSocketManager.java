package com.boss.weather.socket_pc_android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by qiujianbo on 2017/9/5.
 */

public class PCSocketManager {

    private final static int FORWARD_LOCAL_PORT = 0; // any open port
    private final static int FORWARD_REMOTE_PORT = 9999;

    private final static int REVERSE_LOCAL_PORT = 34444;
    private final static int REVERSE_REMOTE_PORT = 34443; // any open port

    private static void execCommand() {
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
            // 解除绑定
//            String script_remove_forward = "adb forward --remove-all";
//            Runtime.getRuntime().exec(script_remove_forward);
//            String script_forward = String.format("adb forward tcp:%s tcp:%s", FORWARD_LOCAL_PORT, FORWARD_REMOTE_PORT);
//            Process p1 = Runtime.getRuntime().exec(script_forward);
//            System.out.println(String.format("exec \'%s\'", script_forward));
//            dis = new DataInputStream(p1.getInputStream());
//            dos = new DataOutputStream(p1.getOutputStream());
//            while ((len = dis.read(buffer)) != -1) {
//                String s = new String(buffer, 0, len);
//                System.out.println("get " + s);
//            }
            // 设置端口反转
            // 解除绑定
            String script_remove_reverse = "adb reverse --remove-all";
            Process p2 = Runtime.getRuntime().exec(script_remove_reverse);
            System.out.println(String.format("exec \'%s\'", script_remove_reverse));
            dis = new DataInputStream(p2.getInputStream());
            dos = new DataOutputStream(p2.getOutputStream());
            while ((len = dis.read(buffer)) != -1) {
                String s = new String(buffer, 0, len);
                System.out.println("get " + s);
            }
            String script_reverse = String.format("adb reverse tcp:%s tcp:%s", REVERSE_REMOTE_PORT, REVERSE_LOCAL_PORT);
            Process p3 = Runtime.getRuntime().exec(script_reverse);
            System.out.println(String.format("exec \'%s\'", script_reverse));
            dis = new DataInputStream(p3.getInputStream());
            dos = new DataOutputStream(p3.getOutputStream());
            while ((len = dis.read(buffer)) != -1) {
                String s = new String(buffer, 0, len);
                System.out.println("get " + s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 先在手机端启动应用
     */
    public static void startSocketClent() {
        execCommand();
        try {
            // 启动socket，连接本地端口
            final Socket socket = new Socket("127.0.0.1", FORWARD_REMOTE_PORT);
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

    private static Thread socketServerThread;
    public static void startSocketServer() {
        System.out.println("startSocketServer");
        stopSocketServer();
        socketServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                execCommand();
                createSocketServer();
            }
        });
        socketServerThread.start();
    }

    private static void stopSocketServer() {
        if (socketServerThread != null) {
            socketServerThread.interrupt();
            socketServerThread = null;
        }
    }

    private static void createSocketServer() {
        System.out.println("createSocketServer");
        try {
            ServerSocket serverSocket = new ServerSocket(REVERSE_LOCAL_PORT);
            serverSocket.setReuseAddress(true);
            while (!Thread.interrupted()) {
                System.out.println(String.format("wait socket connect:%s", serverSocket));
                final Socket socket = serverSocket.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println(String.format("%s start", Thread.currentThread().getName()));
                            DataInputStream dis = new DataInputStream(socket.getInputStream());
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            byte[] buffer = new byte[1024];
                            int len;
                            String m = null;
                            while ((len = dis.read(buffer)) != -1) {
                                m = new String(buffer, 0, len);
                                System.out.println(String.format("--->receive:%s", m));
                                String response = String.format("I am from PC, i got \'%s\'", m);
                                dos.write(response.getBytes());
                                dos.flush();
                                System.out.println(String.format("send:%s", response));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println(String.format("%s finish", Thread.currentThread().getName()));
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
