package com.example.aleysha.ubicomp_group_7;

/**
 * Created by chelsey on 2017-07-16.
 */

// this class is to store the coordinates and when the coordinates are taken
public class TimeCoordPair {
    //time coordinate was taken
    public String time;
    //coordinates that was taken
    public Coordinates coordinates;
    //creates a time coordinate pair of the place and time when the coordinate was taken
    public TimeCoordPair(String time, Coordinates coordinates)
    {
        this.time = time;
        this.coordinates = coordinates;
    }
}
