package com.boss.weather;

/**
 * Created by bojia on 2017-09-02.
 */

/** 契约类 */
public interface WearherContract {
    interface View extends IBaseView<Presenter> {
        void showWeather(String city);
        void setLoading(boolean active);
        void setWeather(String weather);
        void showCancel(String info);
    }

    interface Presenter {
        void getWeather(String city);
        void cancel();
    }
}
