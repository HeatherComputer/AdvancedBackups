package co.uk.mommyheather.advancedbackups.client;

import java.util.function.Supplier;

import co.uk.mommyheather.advancedbackups.core.CoreCommandSystem;
import co.uk.mommyheather.advancedbackups.core.config.ClientConfigManager;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ClientWrapper {

    public static void handle(Supplier<Context> ctx, PacketBackupStatus packet) {
        
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                BackupToast.starting = packet.starting;
                BackupToast.started = packet.started;
                BackupToast.failed = packet.failed;
                BackupToast.finished = packet.finished;

                BackupToast.progress = packet.progress;
                BackupToast.max = packet.max;

                if (!BackupToast.exists) {
                    BackupToast.exists = true;
                    Minecraft.getInstance().getToasts().addToast(new BackupToast());
                }
            }
        });
    }

    public static void init(FMLClientSetupEvent e) {
        MinecraftForge.EVENT_BUS.addListener(ClientWrapper::onClientChat);
        ClientConfigManager.loadOrCreateConfig();
    }

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        if (event.getMessage().equals("/backup reload-client-config")) {
            event.setCanceled(true);

            CoreCommandSystem.reloadClientConfig(Minecraft.getInstance().player::chat);
        }

    }
    
}
