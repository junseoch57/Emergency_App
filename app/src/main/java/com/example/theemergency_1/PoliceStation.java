package com.example.theemergency_1;

import com.google.android.gms.maps.model.LatLng;

public class PoliceStation {
    private String name;
    private String address;
    private LatLng location;

    public PoliceStation(String name, String address, LatLng location) {
        this.name = name;
        this.address = address;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getLocation() {
        return location;
    }
}
