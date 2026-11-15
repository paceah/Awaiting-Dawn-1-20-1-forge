package com.paceah.awaitingdawn;

import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid=AwaitingDawn.MOD_ID)
public final class WorldCycleManager {
    public static long day(MinecraftServer server) { return CalendarRules.day(server.overworld().getDayTime()); }
    public static void reconcile(MinecraftServer server) {
        PlayerDeathStateStore store=PlayerDeathStateStore.get(server);
        long d=day(server), old=store.observedDay;
        if(d==old) return;
        int interval=AwaitingDawnServerConfig.INTERVAL.get();
        CycleStateTransitions.advance(store,d,interval,AwaitingDawnServerConfig.LIVES.get(),AwaitingDawnServerConfig.MAX_LIVES.get());
        boolean immortal=CalendarRules.immortal(d,interval);
        if(immortal && store.immortalAnnouncements.add(d)) {
            if(AwaitingDawnServerConfig.IMMORTAL_MESSAGE.get()) LocalizedMessages.broadcast(server,"immortalityAnnouncement","");
            if(AwaitingDawnServerConfig.RESURRECTION_SOUND.get()) sound(server,ModSounds.RESURRECTION.get());
        } else if(d>old && !immortal && d>1 && CalendarRules.immortal(d-1,interval) && store.mortalAnnouncements.add(d)) {
            if(AwaitingDawnServerConfig.MORTAL_MESSAGE.get()) LocalizedMessages.broadcast(server,"mortalityAnnouncement","");
            if(AwaitingDawnServerConfig.MORTAL_SOUND.get()) sound(server,ModSounds.MORTALITY.get());
        }
        reconcileOnline(server);
    }
    static void refresh(PlayerDeathStateStore.Record r,long milestone) {
        CycleStateTransitions.refresh(r,milestone,AwaitingDawnServerConfig.LIVES.get(),AwaitingDawnServerConfig.MAX_LIVES.get());
    }
    public static void reconcileOnline(MinecraftServer server) {
        List<ServerPlayer> ordered=new ArrayList<>(server.getPlayerList().getPlayers());
        ordered.sort(Comparator.comparing(ServerPlayer::getScoreboardName).thenComparing(p->p.getUUID().toString()));
        for(ServerPlayer p:ordered) ResurrectionService.reconcile(p);
    }
    private static void sound(MinecraftServer server,SoundEvent sound) {
        for(ServerPlayer p:server.getPlayerList().getPlayers()) p.playNotifySound(sound,SoundSource.PLAYERS,.6f,1f);
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END) return;
        reconcile(event.getServer());
        // Bounded retry handles temporarily unavailable destinations without per-tick teleport loops.
        if(event.getServer().getTickCount()%100==0) reconcileOnline(event.getServer());
    }
}