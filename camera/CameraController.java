package com.cinematiccamera.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class CameraController {
    private static final Map<String, Waypoint> waypoints = new HashMap<>();
    private static final Map<String, CameraPath> paths = new HashMap<>();
    
    private static boolean cinematicMode = false;
    private static CameraPath currentPath = null;
    private static float pathProgress = 0.0f;
    private static boolean isPlaying = false;
    private static boolean isPaused = false;
    private static boolean loopPlayback = false;
    
    // 엔티티 추적
    private static Entity trackedEntity = null;
    private static boolean followEntity = false;
    private static float followDistance = 5.0f;
    
    // 고정 좌표 바라보기
    private static Vec3 lookAtTarget = null;
    
    // 카메라 설정
    private static float fov = 70.0f;
    private static float smoothness = 0.5f;
    private static float roll = 0.0f;
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !cinematicMode) return;
        
        // 경로 재생 처리
        if (isPlaying && !isPaused && currentPath != null) {
            updatePathPlayback(mc);
        }
        
        // 엔티티 추적 처리
        if (trackedEntity != null && !trackedEntity.isRemoved()) {
            updateEntityTracking(mc);
        }
        
        // 고정 좌표 바라보기 처리
        if (lookAtTarget != null) {
            updateLookAtTarget(mc);
        }
    }
    
    private void updatePathPlayback(Minecraft mc) {
        if (!currentPath.isValid()) return;
        
        pathProgress += 0.01f * currentPath.getSpeed();
        
        if (pathProgress >= 1.0f) {
            if (loopPlayback) {
                pathProgress = 0.0f;
            } else {
                stopPlayback();
                return;
            }
        }
        
        // 현재 위치 계산
        int segmentCount = currentPath.size() - 1;
        float segmentProgress = pathProgress * segmentCount;
        int currentSegment = (int) segmentProgress;
        float t = segmentProgress - currentSegment;
        
        if (currentSegment >= segmentCount) {
            currentSegment = segmentCount - 1;
            t = 1.0f;
        }
        
        // Catmull-Rom 스플라인을 위한 4개 포인트 가져오기
        Waypoint p0 = currentPath.getWaypoints().get(Math.max(0, currentSegment - 1));
        Waypoint p1 = currentPath.getWaypoints().get(currentSegment);
        Waypoint p2 = currentPath.getWaypoints().get(Math.min(currentPath.size() - 1, currentSegment + 1));
        Waypoint p3 = currentPath.getWaypoints().get(Math.min(currentPath.size() - 1, currentSegment + 2));
        
        // 위치 보간
        Vec3 newPos = SplineInterpolator.interpolatePosition(p0, p1, p2, p3, t);
        mc.player.setPos(newPos.x, newPos.y, newPos.z);
        
        // 회전 보간 (엔티티 추적이나 고정 좌표 바라보기가 없을 때만)
        if (trackedEntity == null && lookAtTarget == null) {
            float newPitch = SplineInterpolator.interpolateAngle(p1.getPitch(), p2.getPitch(), t);
            float newYaw = SplineInterpolator.interpolateAngle(p1.getYaw(), p2.getYaw(), t);
            mc.player.setXRot(newPitch);
            mc.player.setYRot(newYaw);
        }
    }
    
    private void updateEntityTracking(Minecraft mc) {
        Vec3 entityPos = trackedEntity.position().add(0, trackedEntity.getEyeHeight(), 0);
        Vec3 cameraPos = mc.player.position().add(0, mc.player.getEyeHeight(), 0);
        
        if (followEntity) {
            // 엔티티 따라가기
            Vec3 direction = entityPos.subtract(cameraPos).normalize();
            Vec3 targetPos = entityPos.subtract(direction.scale(followDistance));
            mc.player.setPos(targetPos.x, targetPos.y - mc.player.getEyeHeight(), targetPos.z);
        }
        
        // 엔티티 바라보기
        lookAtPosition(mc, entityPos);
    }
    
    private void updateLookAtTarget(Minecraft mc) {
        lookAtPosition(mc, lookAtTarget);
    }
    
    private void lookAtPosition(Minecraft mc, Vec3 target) {
        Vec3 cameraPos = mc.player.position().add(0, mc.player.getEyeHeight(), 0);
        Vec3 direction = target.subtract(cameraPos).normalize();
        
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.asin(-direction.y));
        
        // 부드러운 회전
        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();
        
        mc.player.setYRot(SplineInterpolator.interpolateAngle(currentYaw, yaw, smoothness));
        mc.player.setXRot(SplineInterpolator.interpolateAngle(currentPitch, pitch, smoothness));
    }
    
    // Waypoint 관리
    public static void addWaypoint(String name, Vec3 position, float pitch, float yaw) {
        waypoints.put(name, new Waypoint(name, position, pitch, yaw));
    }
    
    public static void removeWaypoint(String name) {
        waypoints.remove(name);
    }
    
    public static Waypoint getWaypoint(String name) {
        return waypoints.get(name);
    }
    
    public static Map<String, Waypoint> getAllWaypoints() {
        return waypoints;
    }
    
    // 경로 관리
    public static void createPath(String name) {
        paths.put(name, new CameraPath(name));
    }
    
    public static void deletePath(String name) {
        paths.remove(name);
    }
    
    public static CameraPath getPath(String name) {
        return paths.get(name);
    }
    
    public static Map<String, CameraPath> getAllPaths() {
        return paths;
    }
    
    // 재생 제어
    public static void playPath(String pathName, boolean loop) {
        CameraPath path = paths.get(pathName);
        if (path != null && path.isValid()) {
            currentPath = path;
            pathProgress = 0.0f;
            isPlaying = true;
            isPaused = false;
            loopPlayback = loop;
            cinematicMode = true;
        }
    }
    
    public static void pausePlayback() {
        isPaused = true;
    }
    
    public static void resumePlayback() {
        isPaused = false;
    }
    
    public static void stopPlayback() {
        isPlaying = false;
        isPaused = false;
        currentPath = null;
        pathProgress = 0.0f;
        cinematicMode = false;
    }
    
    // 엔티티 추적
    public static void trackEntity(Entity entity, boolean follow, float distance) {
        trackedEntity = entity;
        followEntity = follow;
        followDistance = distance;
    }
    
    public static void stopTracking() {
        trackedEntity = null;
        followEntity = false;
    }
    
    // 고정 좌표 바라보기
    public static void setLookAtTarget(Vec3 target) {
        lookAtTarget = target;
    }
    
    public static void clearLookAtTarget() {
        lookAtTarget = null;
    }
    
    // 카메라 설정
    public static void setCinematicMode(boolean enabled) {
        cinematicMode = enabled;
    }
    
    public static boolean isCinematicMode() {
        return cinematicMode;
    }
    
    public static void setFOV(float value) {
        fov = Math.max(30, Math.min(110, value));
    }
    
    public static float getFOV() {
        return fov;
    }
    
    public static void setSmoothness(float value) {
        smoothness = Math.max(0.0f, Math.min(1.0f, value));
    }
    
    public static void setRoll(float angle) {
        roll = angle;
    }
    
    public static boolean isPlaying() {
        return isPlaying;
    }
    
    public static float getProgress() {
        return pathProgress;
    }
}
