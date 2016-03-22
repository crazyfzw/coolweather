package com.crazyfzw.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.crazyfzw.coolweather.receiver.AutoUpdateReceiver;
import com.crazyfzw.coolweather.util.HttpCallbackListener;
import com.crazyfzw.coolweather.util.HttpUtil;
import com.crazyfzw.coolweather.util.Utility;

/**
 * Created by Crazyfzw on 2016/3/22.
 */
public class AutoUpdateService extends Service {

    //onBind()是Service中唯一的一个抽象方法，在服务与活动通信时会用到
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //onStartCommand,服务每次启动时都会被调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager =(AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000; //这是8小时的毫秒数
        //设置触发时间为anHour8小时
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        //PendingIntent为延迟的意图
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        /**
         *  通过 AlarmManager的set()方法设置一个定时任务
         *  参数一指定工作类型，参数二指定触发事件，参数三指定处理该定时任务的广播接收器
         */

        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = prefs.getString("weather_code", "");
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        //每次服务器请求都应该用过调用自定义的HttpUtil.sendHttpRequest()去实现（在子进程中执行）
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinsh(String response) {
                //处理服务器返回的数据
                Utility.handleWeatherResponse(AutoUpdateService.this, response);
            }

            @Override
            public void onError(Exception e) {
               e.printStackTrace();
            }
        });
    }
}
