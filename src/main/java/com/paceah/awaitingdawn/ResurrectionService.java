package com.paceah.awaitingdawn;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import com.mojang.logging.LogUtils;

public final class ResurrectionService {
    public static boolean reconcile(ServerPlayer p) {
        var store=PlayerDeathStateStore.get(p.server);
        var r=store.record(p.getUUID());
        WorldCycleManager.refresh(r,store.latestMilestone);
        if(r.transition && !p.isDeadOrDying()) { DeathAndRespawnHandler.complete(p); return !r.pending; }
        if(!r.pending || !r.eligible || p.isDeadOrDying()) { store.setDirty(); return false; }
        try {
            Optional<VanillaSpawnResolver.Destination> target=VanillaSpawnResolver.resolve(p);
            if(target.isEmpty()) return false;
            var dest=target.get();
            p.teleportTo(dest.level(),dest.position().x,dest.position().y,dest.position().z,p.getYRot(),p.getXRot());
            if(p.serverLevel()!=dest.level() || p.position().distanceToSqr(dest.position())>4) return false;
            if(p.isSpectator()) {
                p.setGameMode(r.preMode);
                if(p.isSpectator()) return false;
            }
            // Commit only after teleport and mode restoration, so temporary failures remain retryable.
            r.pending=false; r.eligible=false; r.lives=AwaitingDawnServerConfig.MAX_LIVES.get(); r.refresh=store.latestMilestone; store.setDirty();
            LocalizedMessages.resurrected(p,r.lives);
            if(AwaitingDawnServerConfig.REVIVE_MESSAGE.get()) LocalizedMessages.broadcast(p.server,"playerResurrected",p.getScoreboardName());
            return true;
        } catch(RuntimeException ex) {
            LogUtils.getLogger().error("Resurrection failed for {}; keeping pending record",p.getUUID(),ex);
            return false;
        }
    }
}