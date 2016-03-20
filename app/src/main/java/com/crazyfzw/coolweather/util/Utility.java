package com.crazyfzw.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.crazyfzw.coolweather.db.CoolWeatherDB;
import com.crazyfzw.coolweather.model.City;
import com.crazyfzw.coolweather.model.County;
import com.crazyfzw.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Crazyfzw on 2016/3/19.
 */
public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */

    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response){
        //TextUtils.isEmpty(resopnse)判断返回结果是否为空
        if (!TextUtils.isEmpty(response)){
            String [] allprovinces = response.split(",");
            if (allprovinces !=null && allprovinces.length>0){
                //遍历数组
                for (String p :allprovinces){
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据存储到Province表
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId){

        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if (allCities !=null && allCities.length>0){
                for (String c : allCities){
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出来的数据存储到City表
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return  false;
    }


    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId){

        if (!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if (allCounties !=null && allCounties.length>0){
                for (String c : allCounties){
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //将解析出来的数据存储到County表
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }


    /**
     * 解析服务器返回地json数据，并将解析出的数据存储到本地
     */
    public static void handleWeatherResponse(Context context, String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherinfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherinfo.getString("city");
            String weatherCode = weatherinfo.getString("cityid");
            String temp1 = weatherinfo.getString("temp1");
            String temp2 = weatherinfo.getString("temp2");
            String weatherDesp = weatherinfo.getString("weather");
            String publishTime = weatherinfo.getString("ptime");
            saveWeatherinfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
        }catch (JSONException e){
            e.printStackTrace();
        }

    }


    /**
     * 将服务器返回的所有天气信息存储到SharedPreference(使用键值对的方式存储数据 1,2,3)中.
     */
    public static void saveWeatherinfo(Context context, String cityName,
                   String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime){
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);//格式化日期
        //1.取得SharedPreferences.Editor对象
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        //2.调用putString、putInt等方法存储数据
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        //3.调用commit提交数据
        editor.commit();
    }
}
