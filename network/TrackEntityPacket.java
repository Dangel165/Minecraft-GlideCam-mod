package com.cinematiccamera.network;

import com.cinematiccamera.camera.CameraController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TrackEntityPacket {
    private final int entityId;
    private final boolean follow;
    private final float distance;
    private final boolean stop;

    public TrackEntityPacket(int entityId, boolean follow, float distance, boolean stop) {
        this.entityId = entityId;
        this.follow = follow;
        this.distance = distance;
        this.stop = stop;
    }

    public static TrackEntityPacket track(int entityId, boolean follow, float distance) {
        return new TrackEntityPacket(entityId, follow, distance, false);
    }

    public static TrackEntityPacket stop() {
        return new TrackEntityPacket(-1, false, 0, true);
    }

    public static void encode(TrackEntityPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.follow);
        buf.writeFloat(msg.distance);
        buf.writeBoolean(msg.stop);
    }

    public static TrackEntityPacket decode(FriendlyByteBuf buf) {
        return new TrackEntityPacket(
            buf.readInt(),
            buf.readBoolean(),
            buf.readFloat(),
            buf.readBoolean()
        );
    }

    public static void handle(TrackEntityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.stop) {
                CameraController.stopTracking();
            } else {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    Entity entity = mc.level.getEntity(msg.entityId);
                    if (entity != null) {
                        CameraController.trackEntity(entity, msg.follow, msg.distance);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
