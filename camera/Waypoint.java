package com.cinematiccamera.camera;

import net.minecraft.world.phys.Vec3;

public class Waypoint {
    private final String name;
    private final Vec3 position;
    private final float pitch;
    private final float yaw;
    private final long creationTime;
    
    public Waypoint(String name, Vec3 position, float pitch, float yaw) {
        this.name = name;
        this.position = position;
        this.pitch = pitch;
        this.yaw = yaw;
        this.creationTime = System.currentTimeMillis();
    }
    
    public String getName() {
        return name;
    }
    
    public Vec3 getPosition() {
        return position;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public String toString() {
        return String.format("%s [%.2f, %.2f, %.2f] (%.1f°, %.1f°)", 
            name, position.x, position.y, position.z, pitch, yaw);
    }
}
