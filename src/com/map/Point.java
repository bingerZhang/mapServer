package com.map;

import java.util.DoubleSummaryStatistics;

/**
 * Created by zlb on 2016/7/19.
 */
public class Point {
    int point_id;
    double point_x;
    double point_y;
    double point_z;
    double bd_x;
    double bd_y;
    int ws_id; //weather_station id
    int level[];
    int gps_id;
    double rainfall = 0.0;

    Point(){

    }

    public double getRainfall() {
        return rainfall;
    }

    public void setRainfall(double rainfall) {
        this.rainfall = rainfall;
    }

    public int getGps_id() {
        return gps_id;
    }

    public void setGps_id(int gps_id) {
        this.gps_id = gps_id;
    }

    public Point(int id, double lat, double lng){
        this.point_id = id;
        this.point_x = lat;
        this.point_y = lng;
    }

    public Point(int point_id, double point_x, double point_y, double point_z, double bd_x, double bd_y, int ws_id,int level_size) {
        this.point_id = point_id;
        this.point_x = point_x;
        this.point_y = point_y;
        this.point_z = point_z;
        this.bd_x = bd_x;
        this.bd_y = bd_y;
        this.ws_id = ws_id;
        level = new int[level_size];
    }

    public Point(int point_id, double point_x, double point_y, double point_z, int ws_id,int level_size) {
        this.point_id = point_id;
        this.point_x = point_x;
        this.point_y = point_y;
        this.ws_id = ws_id;
        level = new int[level_size];
    }

    public void setLevel(int[] level) {
        this.level = level;
    }
    public int[] getLevel() {
        return level;
    }

    public int getPoint_id() {
        return point_id;
    }

    public void setPoint_id(int point_id) {
        this.point_id = point_id;
    }

    public double getBd_x() {
        return bd_x;
    }

    public void setBd_x(double bd_x) {
        this.bd_x = bd_x;
    }

    public double getBd_y() {
        return bd_y;
    }

    public void setBd_y(double bd_y) {
        this.bd_y = bd_y;
    }

    public int getWs_id() {
        return ws_id;
    }

    public void setWs_id(int ws_id) {
        this.ws_id = ws_id;
    }

    public int getId() {
        return point_id;
    }

    public void setId(int id) {
        this.point_id = id;
    }

    public double getPoint_x() {
        return point_x;
    }

    public void setPoint_x(double point_x) {
        this.point_x = point_x;
    }

    public double getPoint_z() {
        return point_z;
    }

    public void setPoint_z(double point_z) {
        this.point_z = point_z;
    }

    public double getPoint_y() {
        return point_y;
    }

    public void setPoint_y(double point_y) {
        this.point_y = point_y;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ID: " + point_id + "  ");
        sb.append("PX: " + point_x + "  ");
        sb.append("PY: " + point_y + "  ");
        sb.append("WSPID: " + ws_id );
        return sb.toString();
    }

}
