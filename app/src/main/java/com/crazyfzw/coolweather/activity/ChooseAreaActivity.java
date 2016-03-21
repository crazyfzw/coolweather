package com.crazyfzw.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crazyfzw.coolweather.R;
import com.crazyfzw.coolweather.db.CoolWeatherDB;
import com.crazyfzw.coolweather.model.City;
import com.crazyfzw.coolweather.model.County;
import com.crazyfzw.coolweather.model.Province;
import com.crazyfzw.coolweather.util.HttpCallbackListener;
import com.crazyfzw.coolweather.util.HttpUtil;
import com.crazyfzw.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crazyfzw on 2016/3/19.
 */
public class ChooseAreaActivity extends Activity {

    /**
     *是否从WeatherActivity中跳过来
     */
    private boolean isFromWeatherActivity;

    private static final int LEVEL_PROVICE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> datalist = new ArrayList<String>();
    private CoolWeatherDB coolWeatherDB;


    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断否从WeatherActivity中跳过来，是则根据键读取到true,否则返回false
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        //从SharedPreferences文件中根据city_selected标识符判断当前是否已经选择了一个城市
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //已经选择了城市切不是从WeatherActivity跳转过来，才会直接跳转到WeatherActivity
        if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        titleText = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
        listView.setAdapter(adapter);
        //获取数据库操作类实例
        coolWeatherDB = CoolWeatherDB.getInstance(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVICE){
                    selectedProvince = provinceList.get(position);
                    //若选中了省，则把该省下各市的数据加载到datalist
                    queryCities();

                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    //若选中了市，则把该市下各县的数据加载到datalist
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    //若选中了县，则把该县的代码countyCode传到WeatherActivity.class中用于查询显示对应的天气信息
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }

            }
        });
        queryProvinces();//加载省级数据
    }

    /**
     * 查询全国所有省，优先从数据库查询，如果没有查询到再从服务器上查询。
     */
    private void queryProvinces(){

        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size()>0){
            datalist.clear();//把datalist中已有数据清空
            //遍历省列表把数据封装到datalist
            for (Province province : provinceList){
                datalist.add(province.getProvinceName());
            }
            /**
             * 动态加载ListView,
             * adater绑定的数据发生了变化，
             * 通知Activity最新加载ListView中的内容(不是刷新整个Activity)
             */
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//把listView显示定位到第一条
            titleText.setText("中国");
            currentLevel = LEVEL_PROVICE;
        }else{
            queryFromServer(null, "province");

        }

    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再到服务器上去查询。
     */
    private void queryCities(){

        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size()>0){
            datalist.clear();
            for (City city : cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }

    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再到服务器上去查询。
     */
    private void queryCounties(){
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size()>0){
            datalist.clear();
            for (County county : countyList){
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(), "county");
        }

    }


    /**
     * 根据传入的代号和类型从服务器上查询省市县数据
     */
    private void queryFromServer(final String code, final String type){
        String address;
        if(!TextUtils.isEmpty(code)){
            address = "http://weather.com.cn/data/list3/city"+ code + ".xml";
        }else{
            address = "http://weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();//显示精度条
        //调用sendHttpRequest()开启子线程去请求服务器
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinsh(String response) {
              boolean result = false;
                if ("province".equals(type)){
                    //解析和处理服务器返回的数据并存到数据库中
                    result = Utility.handleProvincesResponse(coolWeatherDB, response);
                }else if ("city".equals(type)){
                    result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
                }

                if (result){
                    //通过runOnUiThread()方法回到主线程处理逻辑
                    //重新从数据库中读取数据
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            //解决4.0系统点击ProgressDialog之外的地方Dialog消失的问题
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭对话框
     */
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出
     */
    @Override
    public void onBackPressed(){
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            if (isFromWeatherActivity){
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }

    }
}
