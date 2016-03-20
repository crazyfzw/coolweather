package com.crazyfzw.coolweather.model;

/**
 * Created by Crazyfzw on 2016/3/18.
 */

/**
 * 为Province表创建一个对应的实体类（为每一张表都创建一个对应的实体类）
 */
public class Province {

    private int id;
    private String provinceName;
    private String provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }
}
