package com.map;

import java.util.DoubleSummaryStatistics;

/**
 * Created by zlb on 2016/7/19.
 */
public class Point {
    Double point_x;
    Double point_y;
    Double point_z;
    int id;
    int preid = 0;
    double predis = 0;
    int nextid=0;
    double nextdis = 0;

    Point(){

    }

    public Point(Double point_x, Double point_y, Double point_z, int id) {
        this.point_x = point_x;
        this.point_y = point_y;
        this.point_z = point_z;
        this.id = id;
    }

    public double getPredis() {
        return predis;
    }

    public void setPredis(double predis) {
        this.predis = predis;
    }

    public double getNextdis() {
        return nextdis;
    }

    public void setNextdis(double nextdis) {
        this.nextdis = nextdis;
    }

    public int getPreid() {
        return preid;
    }

    public void setPreid(int preid) {
        this.preid = preid;
    }

    public int getNextid() {
        return nextid;
    }

    public void setNextid(int nextid) {
        this.nextid = nextid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}