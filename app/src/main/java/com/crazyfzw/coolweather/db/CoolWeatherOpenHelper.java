package com.crazyfzw.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Crazyfzw on 2016/3/18.
 */
public class CoolWeatherOpenHelper extends SQLiteOpenHelper{

    /**
     * Province表建表语句
     */
    public static  final String CREATE_PROVINCE = "create table Province("
            + "id integer primary key autoincrement, "
            + "province_name text, "
            + "province_code text)";

    /**
     * City表建表语句
     */
    public static  final String CREATE_CITY = "create table City("
            + "id integer primary key autoincrement, "
            + "city_name text, "
            + "city_code text, "
            + "province_id integer)";

    /**
     * County表建表语句
     */
    public static final String CREATE_COUNT = "create table County("
            + "id integer primary key autoincrement, "
            + "county_name text, "
            + "county_code text, "
            + "city_id integer)";

    /**
     *
     * @param context  上下文对象
     * @param name     数据库名
     * @param factory  游标工厂，默认null
     * @param version  版本
     */

    public CoolWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_PROVINCE); //创建Province表
        db.execSQL(CREATE_CITY);     //创建City表
        db.execSQL(CREATE_COUNT);    //创建County表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
