package com.boss.weather;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements WearherContract.View {
    EditText mCityNameEdit;
    Button mGetWeatherBtn;
    TextView mWeatherInfoText;

    WearherContract.Presenter mWeatherPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWeatherPresenter = new WeatherPresenter(this);

        // setup views
        mCityNameEdit = (EditText) findViewById(R.id.edit_city_name);
        mGetWeatherBtn = (Button) findViewById(R.id.btn_get_weather);
        mWeatherInfoText = (TextView) findViewById(R.id.text_weather_info);
        mGetWeatherBtn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                String city = mCityNameEdit.getEditableText().toString();
                // TODO check city
                showWeather(city);
//                setLoading(true);
            }
        });
//        startSocketServer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                startSocketClient();
            }
        }).start();
    }

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

    private void startSocketClient() {
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
                                Timber.i("--->get from Android client:%s" ,s);
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

//                            // ------- input
//                            String input;
//                            Scanner scanner = new Scanner(System.in);
//                            while (true) {
//                                System.out.println(String.format("input:"));
//                                input = scanner.nextLine();
//                                if (input.length() == 0) {
//                                    System.out.println(String.format("length is 0"));
//                                    continue;
//                                }
//                                dos1.write(input.getBytes());
//                                dos1.flush();
//                                System.out.println(String.format("send:\'%s'", input));
//                            }

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

    @Override
    public void showWeather(String city) {
        mWeatherPresenter.getWeather(city);
    }

    Dialog loading;

    @Override
    public void setLoading(boolean active) {
        Timber.i("loading %s %s", active, Thread.currentThread().getName());
        if (loading == null) {
            loading = new ProgressDialog.Builder(this)
                    .setTitle("Waiting")
                    .setMessage("Getting weather")
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mWeatherPresenter.cancel();
                        }
                    })
                    .create();
        }
        if (active) {
            loading.show();
        } else {
            loading.dismiss();
        }
    }

    @Override
    public void setWeather(String weather) {
        mWeatherInfoText.setText(weather);
    }

    @Override
    public void showCancel(String info) {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(WearherContract.Presenter presenter) {
        mWeatherPresenter = presenter;
    }
}
