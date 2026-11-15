package com.paceah.awaitingdawn;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public final class LocalizedMessages {
    public static final List<String> LOCALES=List.of("en_us","ru_ru","zh_cn","es_es","pt_br","fr_fr","de_de","ja_jp","ko_kr","pl_pl");
    private static final Logger LOG=LogUtils.getLogger();
    private static final Map<String,JsonObject> LANG=new HashMap<>();
    private static final Set<String> WARNED=new HashSet<>();
    private static final Pattern TOKEN=Pattern.compile("\\{([^{}]+)}");
    static {
        for(String locale:LOCALES) {
            try(InputStream in=LocalizedMessages.class.getResourceAsStream("/assets/awaitingdawn/lang/"+locale+".json")) {
                if(in==null) throw new IOException("Missing "+locale);
                LANG.put(locale,JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject());
            } catch(IOException ex) { throw new ExceptionInInitializerError(ex); }
        }
    }
    private static String text(String locale,String key) {
        JsonObject lang=LANG.getOrDefault(locale.toLowerCase(Locale.ROOT),LANG.get("en_us"));
        return lang.get("awaitingdawn."+key).getAsString();
    }
    public static MutableComponent message(ServerPlayer recipient,String category,String name,long days,int lives) {
        return message(recipient.getLanguage(),category,name,days,lives);
    }
    public static MutableComponent message(String locale,String category,String name,long days,int lives) {
        Map<String,String> values=Map.of("player",name,"days",Long.toString(days),"lives",Integer.toString(lives),
            "day_word",text(locale,"day."+Pluralization.form(locale,days)),
            "life_word",text(locale,"life."+Pluralization.form(locale,lives)));
        String template=AwaitingDawnServerConfig.TEXT.get(category).get();
        if(!template.isEmpty()) {
            Matcher m=TOKEN.matcher(template);
            while(m.find()) if(!values.containsKey(m.group(1))) {
                if(WARNED.add(category+template)) LOG.warn("Unknown placeholder {} in {} override; using localization",m.group(),category);
                template=""; break;
            }
        }
        if(template.isEmpty()) template=text(locale,category);
        for(var e:values.entrySet()) template=template.replace("{"+e.getKey()+"}",e.getValue());
        return Component.literal(template).setStyle(Style.EMPTY.withColor(TextColor.parseColor(AwaitingDawnServerConfig.COLORS.get(category).get())).withBold(AwaitingDawnServerConfig.BOLD.get(category).get()));
    }
    public static void direct(ServerPlayer p,String category,int lives) {
        // Keep the personal resurrection title while the life counter remains available in chat
        if(category.equals("livesRemaining")) direct(p,"individualResurrection",lives);
        display(p,locale->message(locale,category,p.getScoreboardName(),0,lives),!chatCategory(category));
    }
    public static void broadcast(MinecraftServer server,String category,String name) {
        for(ServerPlayer p:server.getPlayerList().getPlayers())
            display(p,locale->message(locale,category,name,0,0),!chatCategory(category));
    }
    private static void display(ServerPlayer p,java.util.function.Function<String,Component> text,boolean title) {
        Map<String,Component> variants=new LinkedHashMap<>();
        for(String locale:LOCALES) variants.put(locale,text.apply(locale));
        PacketHandler.display(p,variants,title);
    }
    static String resurrectionCategory(boolean limitedLives,boolean immortal) {
        return limitedLives && !immortal?"livesRemaining":"individualResurrection";
    }
    public static void resurrected(ServerPlayer p,int lives) {
        direct(p,resurrectionCategory(AwaitingDawnServerConfig.LIVES.get(),
            CalendarRules.immortal(WorldCycleManager.day(p.server),AwaitingDawnServerConfig.INTERVAL.get())),lives);
    }
    static boolean chatCategory(String category) {
        return Set.of("deathMain","deathCountdown","deathImmortalSuffix","playerResurrected",
            "livesRemaining","immortalityAnnouncement","mortalityAnnouncement").contains(category);
    }
    static MutableComponent deathText(String locale,String name,long day,boolean limitedLives,int lives) {
        int interval=AwaitingDawnServerConfig.INTERVAL.get();
        boolean immortal=CalendarRules.immortal(day,interval);
        MutableComponent result=Component.empty().append(message(locale,"deathMain",name,0,lives));
        // The counter is already decremented by the death handler
        // With lives disabled its stored value is unused, so it must not hide a real waiting period
        if(immortal || !limitedLives || lives<=0)
            result.append(message(locale,immortal?"deathImmortalSuffix":"deathCountdown",name,CalendarRules.next(day,interval)-day,lives));
        return result;
    }
    public static void death(ServerPlayer dead,long day) {
        if(!AwaitingDawnServerConfig.DEATH_MESSAGE.get()) return;
        int lives=PlayerDeathStateStore.get(dead.server).record(dead.getUUID()).lives;
        boolean limitedLives=AwaitingDawnServerConfig.LIVES.get();
        for(ServerPlayer p:dead.server.getPlayerList().getPlayers())
            display(p,locale->deathText(locale,dead.getScoreboardName(),day,limitedLives,lives),false);
    }
    public static void title(ServerPlayer p) {
        display(p,locale->message(locale,"deathTitle",p.getScoreboardName(),0,0),true);
    }
}
