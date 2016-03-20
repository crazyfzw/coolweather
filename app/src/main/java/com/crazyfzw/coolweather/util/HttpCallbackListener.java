package com.crazyfzw.coolweather.util;

/**
 * Created by Crazyfzw on 2016/3/19.
 */
public interface HttpCallbackListener {

    void onFinsh(String response);

    void onError(Exception e);
}
