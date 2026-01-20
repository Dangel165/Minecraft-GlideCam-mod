package com.cinematiccamera.network;

import com.cinematiccamera.camera.CameraController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CameraEffectPacket {
    public enum EffectType {
        FOV, SMOOTHNESS, ROLL, LOOK_AT, CLEAR_LOOK_AT
    }

    private final EffectType type;
    private final float value1;
    private final float value2;
    private final float value3;

    public CameraEffectPacket(EffectType type, float value1, float value2, float value3) {
        this.type = type;
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    public static CameraEffectPacket fov(float fov) {
        return new CameraEffectPacket(EffectType.FOV, fov, 0, 0);
    }

    public static CameraEffectPacket smoothness(float smoothness) {
        return new CameraEffectPacket(EffectType.SMOOTHNESS, smoothness, 0, 0);
    }

    public static CameraEffectPacket roll(float roll) {
        return new CameraEffectPacket(EffectType.ROLL, roll, 0, 0);
    }

    public static CameraEffectPacket lookAt(double x, double y, double z) {
        return new CameraEffectPacket(EffectType.LOOK_AT, (float)x, (float)y, (float)z);
    }

    public static CameraEffectPacket clearLookAt() {
        return new CameraEffectPacket(EffectType.CLEAR_LOOK_AT, 0, 0, 0);
    }

    public static void encode(CameraEffectPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.type);
        buf.writeFloat(msg.value1);
        buf.writeFloat(msg.value2);
        buf.writeFloat(msg.value3);
    }

    public static CameraEffectPacket decode(FriendlyByteBuf buf) {
        return new CameraEffectPacket(
            buf.readEnum(EffectType.class),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat()
        );
    }

    public static void handle(CameraEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            switch (msg.type) {
                case FOV:
                    CameraController.setFOV(msg.value1);
                    break;
                case SMOOTHNESS:
                    CameraController.setSmoothness(msg.value1);
                    break;
                case ROLL:
                    CameraController.setRoll(msg.value1);
                    break;
                case LOOK_AT:
                    CameraController.setLookAtTarget(new net.minecraft.world.phys.Vec3(
                        msg.value1, msg.value2, msg.value3));
                    break;
                case CLEAR_LOOK_AT:
                    CameraController.clearLookAtTarget();
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
