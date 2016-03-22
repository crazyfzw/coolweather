package com.crazyfzw.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.crazyfzw.coolweather.service.AutoUpdateService;

/**
 * Created by Crazyfzw on 2016/3/22.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //监听。当接收到定时任务发送的广播时，再次启动AutoUpdateService
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
