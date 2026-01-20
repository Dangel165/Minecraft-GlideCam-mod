package com.cinematiccamera.network;

import com.cinematiccamera.camera.CameraController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayPathPacket {
    public enum Action {
        PLAY, PAUSE, RESUME, STOP
    }

    private final Action action;
    private final String pathName;
    private final boolean loop;

    public PlayPathPacket(Action action, String pathName, boolean loop) {
        this.action = action;
        this.pathName = pathName;
        this.loop = loop;
    }

    public static PlayPathPacket play(String pathName, boolean loop) {
        return new PlayPathPacket(Action.PLAY, pathName, loop);
    }

    public static PlayPathPacket pause() {
        return new PlayPathPacket(Action.PAUSE, "", false);
    }

    public static PlayPathPacket resume() {
        return new PlayPathPacket(Action.RESUME, "", false);
    }

    public static PlayPathPacket stop() {
        return new PlayPathPacket(Action.STOP, "", false);
    }

    public static void encode(PlayPathPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUtf(msg.pathName);
        buf.writeBoolean(msg.loop);
    }

    public static PlayPathPacket decode(FriendlyByteBuf buf) {
        return new PlayPathPacket(
            buf.readEnum(Action.class),
            buf.readUtf(),
            buf.readBoolean()
        );
    }

    public static void handle(PlayPathPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            switch (msg.action) {
                case PLAY:
                    CameraController.playPath(msg.pathName, msg.loop);
                    break;
                case PAUSE:
                    CameraController.pausePlayback();
                    break;
                case RESUME:
                    CameraController.resumePlayback();
                    break;
                case STOP:
                    CameraController.stopPlayback();
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
