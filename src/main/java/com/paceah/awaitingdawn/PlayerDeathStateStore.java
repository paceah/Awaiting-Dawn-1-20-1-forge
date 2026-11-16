package com.paceah.awaitingdawn;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;

public final class PlayerDeathStateStore extends SavedData {
    public long observedDay=-1, latestMilestone=0, sequence=0;
    public int observedInterval=10;
    public final Set<Long> immortalAnnouncements=new HashSet<>(), mortalAnnouncements=new HashSet<>();
    public final Map<UUID,Record> players=new HashMap<>();
    public static final class Record {
        public boolean pending, transition, title, eligible, spectatorTransition;
        public long nextDay, deathPos, refresh=-1, deathDay=-1;
        public String dimension="minecraft:overworld";
        public GameType preMode=GameType.SURVIVAL;
        public int lives=AwaitingDawnServerConfig.MAX_LIVES.get();
        public CompoundTag save(UUID id) {
            CompoundTag t=new CompoundTag(); t.putUUID("UUID",id);
            t.putBoolean("SpectatorTransition",spectatorTransition); t.putBoolean("Pending",pending); t.putBoolean("Transition",transition); t.putBoolean("Title",title); t.putBoolean("Eligible",eligible);
            t.putLong("NextDay",nextDay); t.putLong("DeathPos",deathPos); t.putLong("Refresh",refresh); t.putLong("DeathDay",deathDay);
            t.putString("Dimension",dimension); t.putString("Mode",preMode.getName()); t.putInt("Lives",lives); return t;
        }
        static Record load(CompoundTag t) {
            Record r=new Record(); r.pending=t.getBoolean("Pending"); r.transition=t.getBoolean("Transition"); r.title=t.getBoolean("Title"); r.eligible=t.getBoolean("Eligible");
            r.spectatorTransition=t.contains("SpectatorTransition")?t.getBoolean("SpectatorTransition"):r.transition && r.pending;
            r.nextDay=Math.max(1,t.getLong("NextDay")); r.deathPos=t.getLong("DeathPos"); r.refresh=t.contains("Refresh")?t.getLong("Refresh"):-1;
            r.deathDay=t.contains("DeathDay")?Math.max(-1,t.getLong("DeathDay")):-1;
            r.dimension=t.getString("Dimension"); r.preMode="adventure".equals(t.getString("Mode"))?GameType.ADVENTURE:GameType.SURVIVAL;
            r.lives=t.contains("Lives")?Math.max(0,t.getInt("Lives")):AwaitingDawnServerConfig.MAX_LIVES.get(); return r;
        }
    }
    public static PlayerDeathStateStore get(MinecraftServer s) { return s.overworld().getDataStorage().computeIfAbsent(PlayerDeathStateStore::load,PlayerDeathStateStore::new,"awaitingdawn"); }
    public Record record(UUID id) { return players.computeIfAbsent(id,k -> {setDirty(); return new Record();}); }
    public static PlayerDeathStateStore load(CompoundTag t) {
        PlayerDeathStateStore s=new PlayerDeathStateStore();
        s.observedDay=t.contains("ObservedDay")?t.getLong("ObservedDay"):-1; s.latestMilestone=t.getLong("LatestMilestone"); s.sequence=t.getLong("Sequence");
        s.observedInterval=t.contains("ObservedInterval")?Math.max(1,Math.min(1000000,t.getInt("ObservedInterval"))):10;
        for(long d:t.getLongArray("ImmortalAnnouncements")) s.immortalAnnouncements.add(d);
        for(long d:t.getLongArray("MortalAnnouncements")) s.mortalAnnouncements.add(d);
        ListTag list=t.getList("Players",Tag.TAG_COMPOUND);
        for(Tag item:list) {CompoundTag p=(CompoundTag)item; if(p.hasUUID("UUID")) s.players.put(p.getUUID("UUID"),Record.load(p));}
        return s;
    }
    @Override public CompoundTag save(CompoundTag t) {
        t.putInt("Version",2); t.putInt("ObservedInterval",observedInterval); t.putLong("ObservedDay",observedDay); t.putLong("LatestMilestone",latestMilestone); t.putLong("Sequence",sequence);
        t.putLongArray("ImmortalAnnouncements",immortalAnnouncements.stream().mapToLong(Long::longValue).toArray());
        t.putLongArray("MortalAnnouncements",mortalAnnouncements.stream().mapToLong(Long::longValue).toArray());
        ListTag list=new ListTag(); players.forEach((id,r)->list.add(r.save(id))); t.put("Players",list); return t;
    }
}
