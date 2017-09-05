package com.boss.weather.socket_pc_android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import timber.log.Timber;

/**
 * Created by qiujianbo on 2017/9/5.
 */

public class AndroidSocketManager {


    Thread socketServerThread;
    void startSocketServer() {
        stopSocketServer();
        socketServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                createSocketServer();
            }
        });
        socketServerThread.start();
    }
    void stopSocketServer() {
        if (socketServerThread != null) {
            socketServerThread.interrupt();
            socketServerThread = null;
        }
    }

    private final static int PORT = 9999;

    private  void createSocketServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (!Thread.interrupted()) {
                Timber.i("start listen port:%s", PORT);
                final Socket socket = serverSocket.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Timber.i("%s start", Thread.currentThread().getName());
                            DataInputStream dis = new DataInputStream(socket.getInputStream());
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            byte[] buffer = new byte[1024];
                            int len;
                            String m = null;
                            while ((len = dis.read(buffer)) != -1) {
                                m = new String(buffer, 0, len);
                                Timber.i("--->receive:%s", m);
                                String response = String.format("I am from Android, i got \'%s\'", m);
                                dos.write(response.getBytes());
                                dos.flush();
                                Timber.i("send:%s", response);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Timber.i("%s finish", Thread.currentThread().getName());
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startSocketClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startSocketClientInner();
            }
        }).start();
    }

    private static void startSocketClientInner() {
        try {
            Timber.i("startSocketClient");
            // 启动socket，连接本地端口
            final Socket socket = new Socket();
            SocketAddress address = new InetSocketAddress("127.0.0.1", 34443);
            socket.setReuseAddress(true);
            socket.connect(address);
            if (socket.isConnected()) {
                Timber.i("socket connected");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataInputStream dis = new DataInputStream(socket.getInputStream());

                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = dis.read(buffer)) != -1) {
                                String s = new String(buffer, 0, len);
                                Timber.i("--->get from server:%s" ,s);
                            }

                        } catch (Exception e) {
                            Timber.i(e);
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
                            Timber.i("send:\'hi\'");
                            Timber.i("------------");

                            int index = 0;
                            while (index < Integer.MAX_VALUE) {
                                Thread.sleep(2000);
                                dos1.write(String.format("I am form Android client:%s", index).getBytes());
                                index ++;
                            }
                        } catch (Exception e) {
                            Timber.i(e);
                        } finally {
                            Timber.i(String.format("finish"));
                        }
                    }
                }).start();
            } else {
                Timber.i("socket is not connected");
            }
        } catch (IOException e) {
            Timber.i(e);
        }
    }
}
