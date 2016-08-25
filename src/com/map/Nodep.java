package com.map;

/**
 * Created by zlb on 2016/8/25.
 */
public class Nodep {
    int id;
    int town_id;
    double probability;

    public Nodep(int id, int town_id, double probability) {
        this.id = id;
        this.town_id = town_id;
        this.probability = probability;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTown_id() {
        return town_id;
    }

    public void setTown_id(int town_id) {
        this.town_id = town_id;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
