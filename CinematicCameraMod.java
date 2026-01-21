package com.cinematiccamera;

import com.cinematiccamera.camera.CameraController;
import com.cinematiccamera.command.CameraCommand;
import com.cinematiccamera.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("glidecam")
public class CinematicCameraMod {
    public static final String MODID = "glidecam";
    public static final Logger LOGGER = LogManager.getLogger();
    
    public CinematicCameraMod() {
        MinecraftForge.EVENT_BUS.register(this);
        // CameraController는 클라이언트에서만 등록 (onClientSetup에서 처리)
    }
    
    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
        LOGGER.info("Cinematic Camera Mod initialized successfully!");
    }
    
    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        // 클라이언트 전용: CameraController 등록
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(new CameraController());
        });
        LOGGER.info("Cinematic Camera Mod client setup complete!");
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CameraCommand.register(event.getDispatcher());
        LOGGER.info("Camera commands registered!");
    }
}
