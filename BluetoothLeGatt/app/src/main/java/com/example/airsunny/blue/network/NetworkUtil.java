package com.example.airsunny.blue.network;


import android.annotation.SuppressLint;

import com.example.airsunny.blue.utils.EventBusUtil;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lzq
 * @date 2020-04-26
 */
@SuppressLint("CheckResult")
public class NetworkUtil {
    private static class NetworkUtilInstance{
        private static final NetworkUtil INSTANCE = new NetworkUtil();
    }

    private NetworkUtil(){}

    public static NetworkUtil getInstance() {
        return NetworkUtilInstance.INSTANCE;
    }


    public void getFriendPosition(String name, String rename) {
        RetrofitUtil.getInstance().getFriendPosition(name, rename)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        EventBusUtil.sendEvent(EventCode.GET_FRIEND_POSITION, s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    public void uploadPosition(String name, String ib1, String ib2) {
        RetrofitUtil.getInstance().uploadPosition(name, ib1, ib2)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        EventBusUtil.sendEvent(EventCode.UPLOAD_POSITION);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        EventBusUtil.sendErrEvent(EventCode.UPLOAD_POSITION, "");
                    }
                });
    }
}
