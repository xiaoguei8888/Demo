package com.boss.weather;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by bojia on 2017-09-02.
 */

public class WeatherPresenter implements WearherContract.Presenter {

    WearherContract.View mView;

    public WeatherPresenter(WearherContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void getWeather(String city) {
        getWeatherDisposable = Observable.just(city)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        mView.setLoading(true);
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                    }
                })
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        mView.setLoading(false);
                    }
                })
                .delay(3, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        return s + " weather is sunny.";
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        mView.setWeather(s);
                        mView.setLoading(false);
                    }
                });
    }

    Disposable getWeatherDisposable;

    @Override
    public void cancel() {
        if (getWeatherDisposable != null) {
            getWeatherDisposable.dispose();
        }
        if (getWeatherDisposable.isDisposed()) {
            mView.showCancel("cancel");
        }
    }
}
