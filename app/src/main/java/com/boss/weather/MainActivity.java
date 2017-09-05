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

import com.boss.weather.socket_pc_android.AndroidSocketManager;

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
//        AndroidSocketManager.startSocketServer();
        AndroidSocketManager.startSocketClient();
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
