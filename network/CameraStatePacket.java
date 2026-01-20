package com.cinematiccamera.network;

import com.cinematiccamera.camera.CameraController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CameraStatePacket {
    private final boolean cinematicMode;

    public CameraStatePacket(boolean cinematicMode) {
        this.cinematicMode = cinematicMode;
    }

    public static void encode(CameraStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.cinematicMode);
    }

    public static CameraStatePacket decode(FriendlyByteBuf buf) {
        return new CameraStatePacket(buf.readBoolean());
    }

    public static void handle(CameraStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            CameraController.setCinematicMode(msg.cinematicMode);
        });
        ctx.get().setPacketHandled(true);
    }
}
