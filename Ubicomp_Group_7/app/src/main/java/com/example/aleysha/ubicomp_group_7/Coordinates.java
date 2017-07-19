package com.example.aleysha.ubicomp_group_7;

/**
 * Created by chelsey on 2017-07-16.
 */

//coordinates for GPS, could be extended to include elevation
public class Coordinates {
    // longitude
    public String longitude;
    //latitude
    public String latitude;
    //create a coordinate object with longitude and latitude
    public Coordinates(String lon, String lat)
    {
        longitude = lon;
        latitude = lat;
    }
}
