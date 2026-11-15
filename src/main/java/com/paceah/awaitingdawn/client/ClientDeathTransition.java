package com.paceah.awaitingdawn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="awaitingdawn",value=Dist.CLIENT)
public final class ClientDeathTransition {
    private static boolean requested;
    private static int timeout;
    public static void set(boolean enabled) { requested=enabled; timeout=200; }
    public static void display(com.paceah.awaitingdawn.PacketHandler.Display message) {
        Minecraft mc=Minecraft.getInstance();
        var text=message.variants().getOrDefault(mc.options.languageCode.toLowerCase(java.util.Locale.ROOT),message.variants().getOrDefault("en_us",net.minecraft.network.chat.Component.empty()));
        if(message.title()) {
            mc.gui.setTimes(message.fadeIn(),message.stay(),message.fadeOut());
            mc.gui.setTitle(text);
        } else mc.gui.getChat().addMessage(text);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !requested) return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null || mc.getConnection()==null || --timeout<=0) {requested=false; return;}
        if(mc.player.isDeadOrDying() && mc.player.deathTime>=20) {
            // Send only vanilla PERFORM_RESPAWN. The server independently owns death and spectator state.
            mc.player.respawn();
            if(mc.screen instanceof DeathScreen) mc.setScreen(null);
            requested=false;
        }
    }
}