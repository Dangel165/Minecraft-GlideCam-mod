package com.cinematiccamera.command;

import com.cinematiccamera.camera.CameraController;
import com.cinematiccamera.camera.CameraPath;
import com.cinematiccamera.camera.Waypoint;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class CameraCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("camera")
            .requires(source -> source.hasPermission(2))
            
            // /camera start
            .then(Commands.literal("start")
                .executes(ctx -> startCinematicMode(ctx)))
            
            // /camera stop
            .then(Commands.literal("stop")
                .executes(ctx -> stopCinematicMode(ctx)))
            
            // /camera goto <x> <y> <z>
            .then(Commands.literal("goto")
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .executes(ctx -> gotoPosition(ctx))))
            
            // /camera rotate <pitch> <yaw>
            .then(Commands.literal("rotate")
                .then(Commands.argument("pitch", FloatArgumentType.floatArg(-90, 90))
                    .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                        .executes(ctx -> rotateCamera(ctx)))))
            
            // Waypoint 명령어
            .then(Commands.literal("waypoint")
                .then(Commands.literal("add")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> addWaypointCurrent(ctx))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                            .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                                .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                                    .executes(ctx -> addWaypointManual(ctx)))))))
                .then(Commands.literal("remove")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> removeWaypoint(ctx))))
                .then(Commands.literal("list")
                    .executes(ctx -> listWaypoints(ctx)))
                .then(Commands.literal("goto")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> gotoWaypoint(ctx)))))
            
            // Path 명령어
            .then(Commands.literal("path")
                .then(Commands.literal("create")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> createPath(ctx))))
                .then(Commands.literal("add")
                    .then(Commands.argument("pathName", StringArgumentType.string())
                        .then(Commands.argument("waypointName", StringArgumentType.string())
                            .executes(ctx -> addWaypointToPath(ctx)))))
                .then(Commands.literal("insert")
                    .then(Commands.argument("pathName", StringArgumentType.string())
                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                            .then(Commands.argument("waypointName", StringArgumentType.string())
                                .executes(ctx -> insertWaypointToPath(ctx))))))
                .then(Commands.literal("remove")
                    .then(Commands.argument("pathName", StringArgumentType.string())
                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                            .executes(ctx -> removeWaypointFromPath(ctx)))))
                .then(Commands.literal("list")
                    .executes(ctx -> listPaths(ctx)))
                .then(Commands.literal("show")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> showPath(ctx))))
                .then(Commands.literal("delete")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> deletePath(ctx))))
                .then(Commands.literal("speed")
                    .then(Commands.argument("pathName", StringArgumentType.string())
                        .then(Commands.argument("speed", FloatArgumentType.floatArg(0.1f, 10.0f))
                            .executes(ctx -> setPathSpeed(ctx))))))
            
            // Play 명령어
            .then(Commands.literal("play")
                .then(Commands.argument("pathName", StringArgumentType.string())
                    .executes(ctx -> playPath(ctx, false))
                    .then(Commands.literal("loop")
                        .executes(ctx -> playPath(ctx, true)))))
            
            .then(Commands.literal("pause")
                .executes(ctx -> pausePlayback(ctx)))
            
            .then(Commands.literal("resume")
                .executes(ctx -> resumePlayback(ctx)))
            
            // Track 명령어
            .then(Commands.literal("track")
                .then(Commands.argument("entity", EntityArgument.entity())
                    .executes(ctx -> trackEntity(ctx, false, 5.0f)))
                .then(Commands.literal("follow")
                    .then(Commands.argument("entity", EntityArgument.entity())
                        .then(Commands.argument("distance", FloatArgumentType.floatArg(1.0f, 50.0f))
                            .executes(ctx -> trackEntity(ctx, true, 
                                FloatArgumentType.getFloat(ctx, "distance"))))))
                .then(Commands.literal("train")
                    .executes(ctx -> trackNearestTrain(ctx, 5.0f))
                    .then(Commands.argument("distance", FloatArgumentType.floatArg(1.0f, 50.0f))
                        .executes(ctx -> trackNearestTrain(ctx, 
                            FloatArgumentType.getFloat(ctx, "distance")))))
                .then(Commands.literal("stop")
                    .executes(ctx -> stopTracking(ctx))))
            
            // LookAt 명령어
            .then(Commands.literal("lookat")
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .executes(ctx -> lookAtPosition(ctx)))
                .then(Commands.literal("stop")
                    .executes(ctx -> stopLookAt(ctx))))
            
            // 카메라 설정
            .then(Commands.literal("fov")
                .then(Commands.argument("value", FloatArgumentType.floatArg(30, 110))
                    .executes(ctx -> setFOV(ctx))))
            
            .then(Commands.literal("smooth")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                    .executes(ctx -> setSmoothness(ctx))))
            
            .then(Commands.literal("roll")
                .then(Commands.argument("angle", FloatArgumentType.floatArg())
                    .executes(ctx -> setRoll(ctx))))
            
            .then(Commands.literal("info")
                .executes(ctx -> showInfo(ctx)))
            
            .then(Commands.literal("help")
                .executes(ctx -> showHelp(ctx)))
        );
    }
    
    private static int startCinematicMode(CommandContext<CommandSourceStack> ctx) {
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    new com.cinematiccamera.network.CameraStatePacket(true), player);
            } else {
                CameraController.setCinematicMode(true);
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§a시네마틱 모드 활성화"), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§c시네마틱 모드 활성화 실패"));
            return 0;
        }
    }
    
    private static int stopCinematicMode(CommandContext<CommandSourceStack> ctx) {
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    new com.cinematiccamera.network.CameraStatePacket(false), player);
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.PlayPathPacket.stop(), player);
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.TrackEntityPacket.stop(), player);
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.CameraEffectPacket.clearLookAt(), player);
            } else {
                CameraController.stopPlayback();
                CameraController.stopTracking();
                CameraController.clearLookAtTarget();
                CameraController.setCinematicMode(false);
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§c시네마틱 모드 비활성화"), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§c시네마틱 모드 비활성화 실패"));
            return 0;
        }
    }
    
    private static int gotoPosition(CommandContext<CommandSourceStack> ctx) {
        try {
            Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
            Entity entity = ctx.getSource().getEntity();
            if (entity != null) {
                entity.teleportTo(pos.x, pos.y, pos.z);
                ctx.getSource().sendSuccess(() -> 
                    Component.literal(String.format("§a위치로 이동: %.2f, %.2f, %.2f", pos.x, pos.y, pos.z)), false);
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§c이동 실패"));
            return 0;
        }
    }
    
    private static int rotateCamera(CommandContext<CommandSourceStack> ctx) {
        float pitch = FloatArgumentType.getFloat(ctx, "pitch");
        float yaw = FloatArgumentType.getFloat(ctx, "yaw");
        Entity entity = ctx.getSource().getEntity();
        if (entity != null) {
            entity.setXRot(pitch);
            entity.setYRot(yaw);
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§a카메라 회전: Pitch=%.1f°, Yaw=%.1f°", pitch, yaw)), false);
        }
        return 1;
    }
    
    // Waypoint 명령어 구현은 다음 파일에 계속...
    
    private static int addWaypointCurrent(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        Entity entity = ctx.getSource().getEntity();
        if (entity != null) {
            CameraController.addWaypoint(name, entity.position(), entity.getXRot(), entity.getYRot());
            ctx.getSource().sendSuccess(() -> 
                Component.literal("§aWaypoint 추가: " + name), false);
            return 1;
        }
        return 0;
    }
    
    private static int addWaypointManual(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
        float pitch = FloatArgumentType.getFloat(ctx, "pitch");
        float yaw = FloatArgumentType.getFloat(ctx, "yaw");
        
        CameraController.addWaypoint(name, pos, pitch, yaw);
        ctx.getSource().sendSuccess(() -> 
            Component.literal("§aWaypoint 추가: " + name), false);
        return 1;
    }
    
    private static int removeWaypoint(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        CameraController.removeWaypoint(name);
        ctx.getSource().sendSuccess(() -> 
            Component.literal("§cWaypoint 삭제: " + name), false);
        return 1;
    }
    
    private static int listWaypoints(CommandContext<CommandSourceStack> ctx) {
        var waypoints = CameraController.getAllWaypoints();
        if (waypoints.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§e등록된 Waypoint가 없습니다"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§6=== Waypoints ==="), false);
            waypoints.forEach((name, wp) -> {
                ctx.getSource().sendSuccess(() -> Component.literal("§f" + wp.toString()), false);
            });
        }
        return 1;
    }
    
    private static int gotoWaypoint(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        Waypoint wp = CameraController.getWaypoint(name);
        if (wp != null) {
            Entity entity = ctx.getSource().getEntity();
            if (entity != null) {
                Vec3 pos = wp.getPosition();
                entity.teleportTo(pos.x, pos.y, pos.z);
                entity.setXRot(wp.getPitch());
                entity.setYRot(wp.getYaw());
                ctx.getSource().sendSuccess(() -> 
                    Component.literal("§aWaypoint로 이동: " + name), false);
                return 1;
            }
        }
        ctx.getSource().sendFailure(Component.literal("§cWaypoint를 찾을 수 없습니다: " + name));
        return 0;
    }
    
    private static int createPath(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        CameraController.createPath(name);
        ctx.getSource().sendSuccess(() -> 
            Component.literal("§a경로 생성: " + name), false);
        return 1;
    }
    
    private static int addWaypointToPath(CommandContext<CommandSourceStack> ctx) {
        String pathName = StringArgumentType.getString(ctx, "pathName");
        String waypointName = StringArgumentType.getString(ctx, "waypointName");
        
        CameraPath path = CameraController.getPath(pathName);
        Waypoint waypoint = CameraController.getWaypoint(waypointName);
        
        if (path != null && waypoint != null) {
            path.addWaypoint(waypoint);
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§a경로 '%s'에 Waypoint '%s' 추가", pathName, waypointName)), false);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("§c경로 또는 Waypoint를 찾을 수 없습니다"));
        return 0;
    }
    
    private static int insertWaypointToPath(CommandContext<CommandSourceStack> ctx) {
        String pathName = StringArgumentType.getString(ctx, "pathName");
        int index = IntegerArgumentType.getInteger(ctx, "index");
        String waypointName = StringArgumentType.getString(ctx, "waypointName");
        
        CameraPath path = CameraController.getPath(pathName);
        Waypoint waypoint = CameraController.getWaypoint(waypointName);
        
        if (path != null && waypoint != null) {
            path.insertWaypoint(index, waypoint);
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§a경로 '%s'의 %d번 위치에 Waypoint '%s' 삽입", 
                    pathName, index, waypointName)), false);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("§c경로 또는 Waypoint를 찾을 수 없습니다"));
        return 0;
    }
    
    private static int removeWaypointFromPath(CommandContext<CommandSourceStack> ctx) {
        String pathName = StringArgumentType.getString(ctx, "pathName");
        int index = IntegerArgumentType.getInteger(ctx, "index");
        
        CameraPath path = CameraController.getPath(pathName);
        if (path != null) {
            path.removeWaypoint(index);
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§c경로 '%s'의 %d번 Waypoint 제거", pathName, index)), false);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("§c경로를 찾을 수 없습니다"));
        return 0;
    }
    
    private static int listPaths(CommandContext<CommandSourceStack> ctx) {
        var paths = CameraController.getAllPaths();
        if (paths.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§e등록된 경로가 없습니다"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§6=== Camera Paths ==="), false);
            paths.forEach((name, path) -> {
                ctx.getSource().sendSuccess(() -> 
                    Component.literal(String.format("§f%s: %d waypoints, speed=%.1f", 
                        name, path.size(), path.getSpeed())), false);
            });
        }
        return 1;
    }
    
    private static int showPath(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        CameraPath path = CameraController.getPath(name);
        
        if (path != null) {
            ctx.getSource().sendSuccess(() -> 
                Component.literal("§6=== Path: " + name + " ==="), false);
            for (int i = 0; i < path.getWaypoints().size(); i++) {
                Waypoint wp = path.getWaypoints().get(i);
                int index = i;
                ctx.getSource().sendSuccess(() -> 
                    Component.literal(String.format("§f%d. %s", index, wp.toString())), false);
            }
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("§c경로를 찾을 수 없습니다: " + name));
        return 0;
    }
    
    private static int deletePath(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        CameraController.deletePath(name);
        ctx.getSource().sendSuccess(() -> 
            Component.literal("§c경로 삭제: " + name), false);
        return 1;
    }
    
    private static int setPathSpeed(CommandContext<CommandSourceStack> ctx) {
        String pathName = StringArgumentType.getString(ctx, "pathName");
        float speed = FloatArgumentType.getFloat(ctx, "speed");
        
        CameraPath path = CameraController.getPath(pathName);
        if (path != null) {
            path.setSpeed(speed);
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§a경로 '%s'의 속도 설정: %.1f", pathName, speed)), false);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("§c경로를 찾을 수 없습니다"));
        return 0;
    }
    
    private static int playPath(CommandContext<CommandSourceStack> ctx, boolean loop) {
        String pathName = StringArgumentType.getString(ctx, "pathName");
        CameraPath path = CameraController.getPath(pathName);
        
        if (path != null && path.isValid()) {
            try {
                if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                    com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                        com.cinematiccamera.network.PlayPathPacket.play(pathName, loop), player);
                } else {
                    CameraController.playPath(pathName, loop);
                }
                ctx.getSource().sendSuccess(() -> 
                    Component.literal(String.format("§a경로 재생 시작: %s %s", 
                        pathName, loop ? "(반복)" : "")), false);
                return 1;
            } catch (Exception e) {
                ctx.getSource().sendFailure(Component.literal("§c경로 재생 실패"));
                return 0;
            }
        }
        ctx.getSource().sendFailure(Component.literal("§c경로가 유효하지 않습니다 (최소 2개의 Waypoint 필요)"));
        return 0;
    }
    
    private static int pausePlayback(CommandContext<CommandSourceStack> ctx) {
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.PlayPathPacket.pause(), player);
            } else {
                CameraController.pausePlayback();
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§e재생 일시정지"), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int resumePlayback(CommandContext<CommandSourceStack> ctx) {
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.PlayPathPacket.resume(), player);
            } else {
                CameraController.resumePlayback();
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§a재생 재개"), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int trackEntity(CommandContext<CommandSourceStack> ctx, boolean follow, float distance) {
        try {
            Entity entity = EntityArgument.getEntity(ctx, "entity");
            
            // 서버 사이드에서만 패킷 전송
            if (!ctx.getSource().getLevel().isClientSide()) {
                try {
                    if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                        // 특정 플레이어가 명령어 실행
                        com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                            com.cinematiccamera.network.TrackEntityPacket.track(entity.getId(), follow, distance), player);
                    } else {
                        // 커맨드 블록이나 콘솔에서 실행: 모든 플레이어에게 전송
                        com.cinematiccamera.network.NetworkHandler.sendToAllPlayers(
                            com.cinematiccamera.network.TrackEntityPacket.track(entity.getId(), follow, distance));
                    }
                } catch (ExceptionInInitializerError | IllegalStateException e) {
                    // NetworkHandler 초기화 실패 - 무시하고 계속
                    ctx.getSource().sendFailure(Component.literal("§c네트워크 초기화 오류 - 서버 재시작 필요"));
                    return 0;
                }
            } else {
                // 클라이언트 사이드: 직접 실행
                CameraController.trackEntity(entity, follow, distance);
            }
            
            String mode = follow ? String.format("따라가기 (거리: %.1f)", distance) : "바라보기";
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§a엔티티 추적 시작: %s (%s)", 
                    entity.getName().getString(), mode)), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§c엔티티를 찾을 수 없습니다"));
            return 0;
        }
    }
    
    private static int trackNearestTrain(CommandContext<CommandSourceStack> ctx, float distance) {
        try {
            Entity source = ctx.getSource().getEntity();
            if (source == null) {
                ctx.getSource().sendFailure(Component.literal("§c플레이어만 이 명령어를 사용할 수 있습니다"));
                return 0;
            }
            
            // 가장 가까운 기차 찾기 (25블록 반경)
            Entity nearestTrain = ctx.getSource().getLevel().getEntities(source, 
                source.getBoundingBox().inflate(25.0), 
                entity -> entity.getType().toString().contains("carriage_contraption"))
                .stream()
                .min((e1, e2) -> Double.compare(
                    e1.distanceToSqr(source), 
                    e2.distanceToSqr(source)))
                .orElse(null);
            
            if (nearestTrain == null) {
                ctx.getSource().sendFailure(Component.literal("§c근처에 기차를 찾을 수 없습니다 (25블록 반경)"));
                return 0;
            }
            
            // 서버 사이드에서 실행 중인지 확인
            if (!ctx.getSource().getLevel().isClientSide()) {
                if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                    com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                        com.cinematiccamera.network.TrackEntityPacket.track(nearestTrain.getId(), true, distance), player);
                } else {
                    com.cinematiccamera.network.NetworkHandler.sendToAllPlayers(
                        com.cinematiccamera.network.TrackEntityPacket.track(nearestTrain.getId(), true, distance));
                }
            } else {
                CameraController.trackEntity(nearestTrain, true, distance);
            }
            
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§a기차 추적 시작 (거리: %.1f블록)", distance)), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§c기차 추적 실패: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int stopTracking(CommandContext<CommandSourceStack> ctx) {
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.TrackEntityPacket.stop(), player);
            } else {
                CameraController.stopTracking();
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§c엔티티 추적 중단"), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int lookAtPosition(CommandContext<CommandSourceStack> ctx) {
        Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.CameraEffectPacket.lookAt(pos.x, pos.y, pos.z), player);
            } else {
                CameraController.setLookAtTarget(pos);
            }
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§a좌표 바라보기: %.2f, %.2f, %.2f", pos.x, pos.y, pos.z)), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int stopLookAt(CommandContext<CommandSourceStack> ctx) {
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.CameraEffectPacket.clearLookAt(), player);
            } else {
                CameraController.clearLookAtTarget();
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§c좌표 바라보기 중단"), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int setFOV(CommandContext<CommandSourceStack> ctx) {
        float fov = FloatArgumentType.getFloat(ctx, "value");
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.CameraEffectPacket.fov(fov), player);
            } else {
                CameraController.setFOV(fov);
            }
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§aFOV 설정: %.0f", fov)), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int setSmoothness(CommandContext<CommandSourceStack> ctx) {
        float smoothness = FloatArgumentType.getFloat(ctx, "value");
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.CameraEffectPacket.smoothness(smoothness), player);
            } else {
                CameraController.setSmoothness(smoothness);
            }
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§a부드러움 설정: %.2f", smoothness)), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int setRoll(CommandContext<CommandSourceStack> ctx) {
        float roll = FloatArgumentType.getFloat(ctx, "angle");
        try {
            if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cinematiccamera.network.NetworkHandler.sendToPlayer(
                    com.cinematiccamera.network.CameraEffectPacket.roll(roll), player);
            } else {
                CameraController.setRoll(roll);
            }
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§a카메라 롤: %.1f°", roll)), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int showInfo(CommandContext<CommandSourceStack> ctx) {
        Entity entity = ctx.getSource().getEntity();
        if (entity != null) {
            Vec3 pos = entity.position();
            ctx.getSource().sendSuccess(() -> Component.literal("§6=== Camera Info ==="), false);
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§f위치: %.2f, %.2f, %.2f", pos.x, pos.y, pos.z)), false);
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§f회전: Pitch=%.1f°, Yaw=%.1f°", 
                    entity.getXRot(), entity.getYRot())), false);
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§fFOV: %.0f", CameraController.getFOV())), false);
            ctx.getSource().sendSuccess(() -> 
                Component.literal(String.format("§f시네마틱 모드: %s", 
                    CameraController.isCinematicMode() ? "§a활성" : "§c비활성")), false);
            if (CameraController.isPlaying()) {
                ctx.getSource().sendSuccess(() -> 
                    Component.literal(String.format("§f재생 진행률: %.1f%%", 
                        CameraController.getProgress() * 100)), false);
            }
        }
        return 1;
    }
    
    private static int showHelp(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("§6=== Cinematic Camera Commands ==="), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera start §7- 시네마틱 모드 시작"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera stop §7- 시네마틱 모드 중단"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera goto <x> <y> <z> §7- 좌표로 이동"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera rotate <pitch> <yaw> §7- 카메라 회전"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera waypoint add <name> §7- Waypoint 추가"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera waypoint list §7- Waypoint 목록"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera waypoint goto <name> §7- Waypoint로 이동"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera path create <name> §7- 경로 생성"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera path add <path> <waypoint> §7- 경로에 Waypoint 추가"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera path list §7- 경로 목록"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera play <path> §7- 경로 재생"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera track <entity> §7- 엔티티 추적"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera track train [거리] §7- 가장 가까운 기차 추적"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera lookat <x> <y> <z> §7- 좌표 바라보기"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera fov <value> §7- FOV 설정"), true);
        ctx.getSource().sendSuccess(() -> Component.literal("§f/camera info §7- 현재 카메라 정보"), true);
        return 1;
    }
}
