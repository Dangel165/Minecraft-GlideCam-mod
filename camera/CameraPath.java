package com.cinematiccamera.camera;

import java.util.ArrayList;
import java.util.List;

public class CameraPath {
    private final String name;
    private final List<Waypoint> waypoints;
    private float speed;
    
    public CameraPath(String name) {
        this.name = name;
        this.waypoints = new ArrayList<>();
        this.speed = 1.0f;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Waypoint> getWaypoints() {
        return waypoints;
    }
    
    public void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
    }
    
    public void insertWaypoint(int index, Waypoint waypoint) {
        if (index >= 0 && index <= waypoints.size()) {
            waypoints.add(index, waypoint);
        }
    }
    
    public void removeWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
        }
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public void setSpeed(float speed) {
        this.speed = Math.max(0.1f, Math.min(10.0f, speed));
    }
    
    public boolean isValid() {
        return waypoints.size() >= 2;
    }
    
    public int size() {
        return waypoints.size();
    }
}
