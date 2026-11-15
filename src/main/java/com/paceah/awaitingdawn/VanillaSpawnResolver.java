package com.paceah.awaitingdawn;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;

public final class VanillaSpawnResolver extends PlayerRespawnLogic {
    public record Destination(ServerLevel level,Vec3 position) {}
    public static Optional<Destination> resolve(ServerPlayer p) {
        if(AwaitingDawnServerConfig.SPAWN.get().equalsIgnoreCase("recent") && p.getRespawnPosition()!=null) {
            ServerLevel level=p.server.getLevel(p.getRespawnDimension());
            if(level!=null) {
                // The final flag preserves anchor charge: vanilla already consumed a charge on the death respawn.
                // This is a scheduled teleport of the existing entity, not another vanilla death respawn.
                Optional<Vec3> pos=Player.findRespawnPositionAndUseSpawnBlock(level,p.getRespawnPosition(),p.getRespawnAngle(),p.isRespawnForced(),true);
                if(pos.isPresent() && safe(p,level,pos.get())) return Optional.of(new Destination(level,pos.get()));
            }
        }
        ServerLevel level=p.server.overworld();
        BlockPos origin=level.getSharedSpawnPos();
        int radius=Math.min(16,Math.max(0,p.server.getSpawnRadius(level)));
        // Bounded search uses vanilla terrain/hazard checks, then a standing-player collision check.
        for(int ring=0;ring<=radius;ring++) for(int dx=-ring;dx<=ring;dx++) for(int dz=-ring;dz<=ring;dz++) {
            if(Math.max(Math.abs(dx),Math.abs(dz))!=ring) continue;
            BlockPos pos=getOverworldRespawnPos(level,origin.getX()+dx,origin.getZ()+dz);
            if(pos!=null && safe(p,level,Vec3.atBottomCenterOf(pos))) return Optional.of(new Destination(level,Vec3.atBottomCenterOf(pos)));
        }
        return Optional.empty();
    }
    private static boolean safe(ServerPlayer p,ServerLevel level,Vec3 v) {
        // Spectators ignore collision, so check blocks against an explicit standing box instead.
        AABB box=new AABB(v.x-.3,v.y,v.z-.3,v.x+.3,v.y+1.8,v.z+.3);
        return level.getWorldBorder().isWithinBounds(BlockPos.containing(v)) && v.y>=level.getMinBuildHeight() && v.y+1.8<level.getMaxBuildHeight()
            && level.noCollision(box) && !level.containsAnyLiquid(box);
    }
}