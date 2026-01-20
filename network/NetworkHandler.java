package com.cinematiccamera.network;

import com.cinematiccamera.CinematicCameraMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(CinematicCameraMod.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(CameraStatePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(CameraStatePacket::decode)
            .encoder(CameraStatePacket::encode)
            .consumerMainThread(CameraStatePacket::handle)
            .add();

        INSTANCE.messageBuilder(CameraEffectPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(CameraEffectPacket::decode)
            .encoder(CameraEffectPacket::encode)
            .consumerMainThread(CameraEffectPacket::handle)
            .add();

        INSTANCE.messageBuilder(PlayPathPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(PlayPathPacket::decode)
            .encoder(PlayPathPacket::encode)
            .consumerMainThread(PlayPathPacket::handle)
            .add();

        INSTANCE.messageBuilder(TrackEntityPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(TrackEntityPacket::decode)
            .encoder(TrackEntityPacket::encode)
            .consumerMainThread(TrackEntityPacket::handle)
            .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
