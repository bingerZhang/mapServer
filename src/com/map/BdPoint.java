package com.map;

/**
 * Created by 彬哥 on 2016/10/30.
 */
public class BdPoint {
    int id;
    double lat;
    double lng;
    int gps_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getGps_id() {
        return gps_id;
    }

    public void setGps_id(int gps_id) {
        this.gps_id = gps_id;
    }
}
