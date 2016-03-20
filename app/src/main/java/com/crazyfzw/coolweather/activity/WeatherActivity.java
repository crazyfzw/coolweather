package com.crazyfzw.coolweather.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crazyfzw.coolweather.R;
import com.crazyfzw.coolweather.util.HttpCallbackListener;
import com.crazyfzw.coolweather.util.HttpUtil;
import com.crazyfzw.coolweather.util.Utility;

import org.w3c.dom.Text;

/**
 * Created by Crazyfzw on 2016/3/20.
 */
public class WeatherActivity extends Activity {

    private LinearLayout weatherInfoLayout;

    //用于显示城市名称
    private TextView cityNameText;

    //用于显示发布时间
    private TextView publishText;

    //用于显示天气描述信息
    private TextView weatherDespText;

    //用于显示气温1
    private TextView temp1Text;

    //用于显示气温2
    private TextView temp2Text;

    //用于显示当前日期
    private TextView currentDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化各控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText =(TextView) findViewById(R.id.current_data);

        String countyCode = getIntent().getStringExtra("county_code");
        if(!TextUtils.isEmpty(countyCode)){
            //有县级代号时就去查询天气
            publishText.setText("同步中...");
            //一开始把天气信息及城市信息设置为不可见
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            //根据县级天气代号去查询对应的天气信息
            queryWeatherCode(countyCode);
        }else {
            //没有县级代号时就直接显示本地天气信息
            showWeather();
        }
    }

    /**
     * 查询县级代号所对应的天气代号
     */
    private void queryWeatherCode(String countyCode){
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气信息
     */
    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");

    }

    private void queryFromServer(final String address, final String type){
        //经验:所有与服务器的交互请求均该在子进程中进行
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinsh(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    //通过runOnUiThread()方法回到主线程进行UI操作（把结果数据显示在UI上）
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 显示数据。从SharedPreferences文件读取数据存储的天气信息,并显示到界面上
     */
    private void showWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //prefs.getString(键,当传入的键找不到对应的值时默认返回的值)
        cityNameText.setText(prefs.getString("city_name", ""));
        temp1Text.setText(prefs.getString("temp1", ""));
        temp2Text.setText(prefs.getString("temp2", ""));
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        publishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
        currentDateText.setText(prefs.getString("current_date", ""));
        //查询的数据显示在界面上后，设置天气信息为可见
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }
}
