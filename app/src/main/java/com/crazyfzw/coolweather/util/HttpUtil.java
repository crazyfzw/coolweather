package com.crazyfzw.coolweather.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Crazyfzw on 2016/3/19.
 */
public class HttpUtil {

    public static void sendHttpRequest(final String address, final HttpCallbackListener listener){

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();//在循环体中用于连接字符串，比+更节约资源
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }

                    if (listener != null){
                        //回调onFinsh()方法
                        listener.onFinsh(response.toString());
                    }

                } catch (Exception e) {

                    if (listener != null){
                        //回调onError方法
                        listener.onError(e);
                    }

                } finally {

                    if (connection != null){
                        connection.disconnect();
                    }

                }
            }
        }).start();
    }
}
