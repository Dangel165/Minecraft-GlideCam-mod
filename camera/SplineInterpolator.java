package com.cinematiccamera.camera;

import net.minecraft.world.phys.Vec3;

public class SplineInterpolator {
    
    // Catmull-Rom 스플라인 보간
    public static Vec3 interpolatePosition(Waypoint p0, Waypoint p1, Waypoint p2, Waypoint p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        
        Vec3 pos0 = p0.getPosition();
        Vec3 pos1 = p1.getPosition();
        Vec3 pos2 = p2.getPosition();
        Vec3 pos3 = p3.getPosition();
        
        double x = 0.5 * ((2 * pos1.x) +
                         (-pos0.x + pos2.x) * t +
                         (2 * pos0.x - 5 * pos1.x + 4 * pos2.x - pos3.x) * t2 +
                         (-pos0.x + 3 * pos1.x - 3 * pos2.x + pos3.x) * t3);
        
        double y = 0.5 * ((2 * pos1.y) +
                         (-pos0.y + pos2.y) * t +
                         (2 * pos0.y - 5 * pos1.y + 4 * pos2.y - pos3.y) * t2 +
                         (-pos0.y + 3 * pos1.y - 3 * pos2.y + pos3.y) * t3);
        
        double z = 0.5 * ((2 * pos1.z) +
                         (-pos0.z + pos2.z) * t +
                         (2 * pos0.z - 5 * pos1.z + 4 * pos2.z - pos3.z) * t2 +
                         (-pos0.z + 3 * pos1.z - 3 * pos2.z + pos3.z) * t3);
        
        return new Vec3(x, y, z);
    }
    
    // 부드러운 회전 보간 (Slerp)
    public static float interpolateAngle(float a1, float a2, float t) {
        float diff = ((a2 - a1 + 180) % 360) - 180;
        return a1 + diff * smoothstep(t);
    }
    
    // Smoothstep 함수 (부드러운 가속/감속)
    private static float smoothstep(float t) {
        return t * t * (3 - 2 * t);
    }
    
    // Ease-in-out 함수
    public static float easeInOut(float t) {
        return t < 0.5f ? 2 * t * t : 1 - (float)Math.pow(-2 * t + 2, 2) / 2;
    }
}
