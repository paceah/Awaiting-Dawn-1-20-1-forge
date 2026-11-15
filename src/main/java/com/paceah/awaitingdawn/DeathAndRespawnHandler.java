package com.paceah.awaitingdawn;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=AwaitingDawn.MOD_ID)
public final class DeathAndRespawnHandler {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if(event.isCanceled() || !(event.getEntity() instanceof ServerPlayer p)) return;
        GameType mode=p.gameMode.getGameModeForPlayer();
        if(mode!=GameType.SURVIVAL && mode!=GameType.ADVENTURE) return;
        WorldCycleManager.reconcile(p.server);
        var store=PlayerDeathStateStore.get(p.server); var r=store.record(p.getUUID());
        // Persistent transition token survives entity cloning and blocks duplicate callbacks for this death.
        if(r.transition) return;
        long d=WorldCycleManager.day(p.server);
        boolean immortal=CalendarRules.immortal(d,AwaitingDawnServerConfig.INTERVAL.get());
        r.transition=true; r.title=!immortal; r.spectatorTransition=false;
        if(!r.pending) {
            r.preMode=mode; r.dimension=p.serverLevel().dimension().location().toString(); r.deathPos=p.blockPosition().asLong();
            if(!immortal) {
                if(AwaitingDawnServerConfig.LIVES.get()) r.lives=Math.max(0,r.lives-1);
                if(!AwaitingDawnServerConfig.LIVES.get() || r.lives==0) {
                    r.pending=true; r.eligible=false; r.spectatorTransition=true; r.nextDay=CalendarRules.next(d,AwaitingDawnServerConfig.INTERVAL.get());
                }
            }
        }
        store.setDirty();
        LocalizedMessages.death(p,d);
        if(r.spectatorTransition || immortal) PacketHandler.autoRespawn(p,true);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if(event.isEndConquered() || !(event.getEntity() instanceof ServerPlayer p)) return;
        complete(p);
    }
    static void complete(ServerPlayer p) {
        var store=PlayerDeathStateStore.get(p.server); var r=store.record(p.getUUID());
        if(!r.transition) return;
        if(r.spectatorTransition && !r.eligible) {
            p.setGameMode(GameType.SPECTATOR);
            if(!p.isSpectator()) return;
            if(!r.eligible && !placeAtDeath(p,r)) return;
        }
        r.transition=false; r.spectatorTransition=false;
        PacketHandler.autoRespawn(p,false);
        // Eligible pending players receive a title only after resurrection commits successfully
        if(!r.eligible && r.pending && r.title) LocalizedMessages.title(p);
        // Retain mortality at death if respawn completes after the calendar day changes
        boolean immortalDeath=!r.title;
        r.title=false;
        if(!r.eligible) {
            if(!r.pending) {
                if(immortalDeath) LocalizedMessages.direct(p,"individualResurrection",r.lives);
                else LocalizedMessages.resurrected(p,r.lives);
            }
            else if(AwaitingDawnServerConfig.LIVES.get()) LocalizedMessages.direct(p,"noLives",r.lives);
        }
        store.setDirty();
        ResurrectionService.reconcile(p);
    }
    private static boolean placeAtDeath(ServerPlayer p,PlayerDeathStateStore.Record r) {
        ResourceLocation id=ResourceLocation.tryParse(r.dimension);
        ServerLevel level=id==null?null:p.server.getLevel(ResourceKey.create(Registries.DIMENSION,id));
        if(level==null) return false;
        BlockPos pos=BlockPos.of(r.deathPos);
        p.teleportTo(level,pos.getX()+.5,pos.getY(),pos.getZ()+.5,p.getYRot(),p.getXRot());
        return true;
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer p)) return;
        WorldCycleManager.reconcile(p.server);
        var store=PlayerDeathStateStore.get(p.server); var r=store.record(p.getUUID());
        WorldCycleManager.refresh(r,store.latestMilestone);
        // A persisted eligibility token remains valid even if the clock was subsequently rewound.
        if(r.pending && WorldCycleManager.day(p.server)>=r.nextDay) r.eligible=true;
        if(r.transition) {
            if(p.isDeadOrDying()) PacketHandler.autoRespawn(p,r.spectatorTransition || !r.title);
            else complete(p);
        }
        if(!ResurrectionService.reconcile(p) && r.pending && p.isSpectator() && !r.eligible && !r.transition) placeAtDeath(p,r);
        store.setDirty();
    }
}
