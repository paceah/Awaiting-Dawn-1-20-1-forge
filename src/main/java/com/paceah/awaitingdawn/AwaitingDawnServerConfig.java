package com.paceah.awaitingdawn;

import java.util.*;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraft.network.chat.TextColor;

public final class AwaitingDawnServerConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.IntValue INTERVAL, MAX_LIVES, FADE_IN, STAY, FADE_OUT;
    public static final ForgeConfigSpec.BooleanValue LIVES, DEBUG, IMMORTAL_MESSAGE, MORTAL_MESSAGE, DEATH_MESSAGE, REVIVE_MESSAGE, RESURRECTION_SOUND, MORTAL_SOUND;
    public static final ForgeConfigSpec.ConfigValue<String> SPAWN;
    public static final Map<String, ForgeConfigSpec.ConfigValue<String>> TEXT = new LinkedHashMap<>(), COLORS = new LinkedHashMap<>();
    public static final Map<String, ForgeConfigSpec.BooleanValue> BOLD = new LinkedHashMap<>();
    public static final ForgeConfigSpec SPEC;
    public static final String[] CATEGORIES = {"deathMain","deathCountdown","deathImmortalSuffix","deathTitle","immortalityAnnouncement","mortalityAnnouncement","playerResurrected","individualResurrection","livesRemaining","noLives","status","debugDisabled","debugDone"};
    static {
        B.push("calendar");
        INTERVAL=B.comment("Full Overworld days between resurrections, minimum 1. Day zero is always mortal. Reload applies immediately without erasing progress.").defineInRange("resurrectionIntervalDays",10,1,1000000); B.pop();
        B.push("respawn");
        SPAWN=B.comment("recent = latest valid vanilla bed/anchor, otherwise safe world spawn. global = safe Overworld spawn. Never changes saved vanilla spawn. Case insensitive.").define("spawnMode","recent",o -> o instanceof String s && (s.equalsIgnoreCase("recent")||s.equalsIgnoreCase("global"))); B.pop();
        B.push("lives");
        LIVES=B.comment("Enable limited lives. If false the first mortal death becomes Spectator.").define("enabled",false);
        MAX_LIVES=B.comment("Mortal deaths before Spectator. The last life is consumed by that death. Refills at global milestones.").defineInRange("maxLives",3,1,1000000); B.pop();
        B.push("announcements");
        IMMORTAL_MESSAGE=flag("showImmortalityDayMessage","Broadcast immortality banner once per new qualifying day.",true);
        MORTAL_MESSAGE=flag("showMortalityReturnMessage","Broadcast the first mortal day after immortality.",true);
        DEATH_MESSAGE=flag("showDeathMessages","Broadcast styled death and global resurrection countdown.",true);
        REVIVE_MESSAGE=flag("showPlayerResurrectionMessages","Broadcast each resurrected player in deterministic order.",true); B.pop();
        B.push("sounds");
        RESURRECTION_SOUND=flag("playImmortalityLevelUp","Play packaged revive.ogg once at the immortality transition. The legacy option name is kept for existing configs.",true);
        MORTAL_SOUND=flag("playMortalityReturn","Play packaged dying.ogg once when mortality returns.",true); B.pop();
        B.push("messages");
        for(String key:CATEGORIES) TEXT.put(key,B.comment("Empty uses recipient localization. Named placeholders: {player}, {days}, {day_word}, {lives}, {life_word}. Unknown placeholders fall back to localization.").define(key+"Override","")); B.pop();
        B.push("colors");
        String[] defaults={"red","gray","green","dark_red","green","dark_red","yellow","green","red","red","gray","red","green"};
        for(int i=0;i<CATEGORIES.length;i++) COLORS.put(CATEGORIES[i],B.comment("Minecraft named color or #RRGGBB for "+CATEGORIES[i]+".").define(CATEGORIES[i],defaults[i],o -> o instanceof String s && TextColor.parseColor(s)!=null)); B.pop();
        B.push("bold");
        for(String key:CATEGORIES) BOLD.put(key,flag(key,"Bold styling for "+key+".",Set.of("deathMain","deathTitle","immortalityAnnouncement","mortalityAnnouncement","individualResurrection","livesRemaining","noLives").contains(key))); B.pop();
        B.push("title");
        FADE_IN=B.comment("Death title fade-in duration in ticks.").defineInRange("fadeInTicks",10,0,1200);
        STAY=B.comment("Death title visible duration in ticks.").defineInRange("stayTicks",60,0,1200);
        FADE_OUT=B.comment("Death title fade-out duration in ticks.").defineInRange("fadeOutTicks",20,0,1200); B.pop();
        B.push("development"); DEBUG=flag("enableDebugCommands","Allow permission-level-2 debug commands. Status is also OP only.",false); B.pop();
        SPEC=B.build();
    }
    private static ForgeConfigSpec.BooleanValue flag(String key,String comment,boolean value) { return B.comment(comment).define(key,value); }
}