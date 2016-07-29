package com.map;

import java.util.DoubleSummaryStatistics;

/**
 * Created by zlb on 2016/7/19.
 */
public class Point {
    int point_id;
    Double point_x;
    Double point_y;
    Double point_z;
    int ws_id; //weather_station id

    Point(){

    }

    public Point(int point_id,Double point_x, Double point_y, Double point_z, int ws_id) {
        this.point_id = point_id;
        this.point_x = point_x;
        this.point_y = point_y;
        this.point_z = point_z;
        this.ws_id = ws_id;
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

    public Double getPoint_x() {
        return point_x;
    }

    public void setPoint_x(Double point_x) {
        this.point_x = point_x;
    }

    public Double getPoint_z() {
        return point_z;
    }

    public void setPoint_z(Double point_z) {
        this.point_z = point_z;
    }

    public Double getPoint_y() {
        return point_y;
    }

    public void setPoint_y(Double point_y) {
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
