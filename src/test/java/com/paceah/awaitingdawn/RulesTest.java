package com.paceah.awaitingdawn;

/** Executable assertions without third-party testing dependencies. */
public final class RulesTest {
    private static int checks;
    private static void check(boolean value) { checks++; if(!value) throw new AssertionError("Check "+checks); }
    public static void main(String[] args) {
        check(CalendarRules.day(-1)==0);
        check(CalendarRules.day(23999)==0); check(CalendarRules.day(24000)==1);
        check(!CalendarRules.immortal(0,10)); check(CalendarRules.next(0,10)==10);
        check(CalendarRules.next(5,10)-5==5); check(CalendarRules.next(9,10)-9==1);
        check(CalendarRules.immortal(10,10)); check(!CalendarRules.immortal(11,10));
        check(CalendarRules.latest(12,10)==10); check(CalendarRules.latest(123,10)==120);
        for(int interval:new int[]{1,2,3,10,1000000}) {
            for(int day=0;day<10000;day++) {
                long next=CalendarRules.next(day,interval);
                check(next>day && next%interval==0);
                check(CalendarRules.immortal(day,interval)==(day>0 && day%interval==0));
                if(interval==1 && day>0) check(CalendarRules.immortal(day,interval));
            }
        }
        for(int n:new int[]{1,21,101}) check(Pluralization.form(true,n).equals("one"));
        for(int n:new int[]{2,3,4,22,104}) check(Pluralization.form(true,n).equals("few"));
        for(int n:new int[]{0,5,11,12,14,111}) check(Pluralization.form(true,n).equals("many"));
        check(Pluralization.form(false,1).equals("one")); check(Pluralization.form(false,21).equals("many"));
        stateTests();
        check(LocalizedMessages.resurrectionCategory(true,true).equals("individualResurrection"));
        check(LocalizedMessages.resurrectionCategory(true,false).equals("livesRemaining"));
        check(LocalizedMessages.resurrectionCategory(false,true).equals("individualResurrection"));
        check(LocalizedMessages.resurrectionCategory(false,false).equals("individualResurrection"));
        check(Pluralization.form("pl_pl",1).equals("one"));
        check(Pluralization.form("pl_pl",22).equals("few"));
        check(Pluralization.form("pl_pl",12).equals("many"));
        check(Pluralization.form("pl_pl",21).equals("many"));
        for(String locale:new String[]{"ru_ru","en_us"}) {
            String main=LocalizedMessages.message(locale,"deathMain","Alex",0,0).getString();
            for(int lives:new int[]{1,2,3,1000000})
                check(LocalizedMessages.deathText(locale,"Alex",7,true,lives).getString().equals(main));
            String countdown=LocalizedMessages.message(locale,"deathCountdown","Alex",3,0).getString();
            check(LocalizedMessages.deathText(locale,"Alex",7,true,0).getString().equals(main+countdown));
            check(LocalizedMessages.deathText(locale,"Alex",7,false,3).getString().equals(main+countdown));
            String immediate=LocalizedMessages.message(locale,"deathImmortalSuffix","Alex",0,0).getString();
            check(LocalizedMessages.deathText(locale,"Alex",10,true,0).getString().equals(main+immediate));
            check(LocalizedMessages.deathText(locale,"Alex",10,true,3).getString().equals(main+immediate));
        }
        for(String category:new String[]{"deathMain","deathCountdown","deathImmortalSuffix","playerResurrected","livesRemaining","immortalityAnnouncement","mortalityAnnouncement"})
            check(LocalizedMessages.chatCategory(category));
        for(String category:new String[]{"deathTitle","individualResurrection","noLives","status","debugDisabled","debugDone"})
            check(!LocalizedMessages.chatCategory(category));
        runConfigTest();
        for(String locale:LocalizedMessages.LOCALES) {
            try(var in=RulesTest.class.getResourceAsStream("/assets/awaitingdawn/lang/"+locale+".json")) {
                var lang=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                try(var base=RulesTest.class.getResourceAsStream("/assets/awaitingdawn/lang/en_us.json")) {
                    check(lang.keySet().equals(com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(base,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().keySet()));
                }
                for(var entry:lang.entrySet()) {
                    String text=entry.getValue().getAsString();
                    check(text.codePoints().allMatch(c -> c>=32 && c!=0xfffd && c!=0xfe0f && c<0x1f000 && c!=0x2014 && c!=0x2013));
                    String rendered=LocalizedMessages.message(locale,"livesRemaining","Alex",0,2).getString();
                    check(!rendered.contains("{") && !rendered.contains("}"));
                }
            } catch(java.io.IOException e) { throw new AssertionError(e); }
        }
        System.out.println("Passed "+checks+" calendar, pluralization and persisted-state assertions");
    }
    private static void runConfigTest() {
        try {
            var root=java.nio.file.Files.createTempDirectory("awaitingdawn-config-");
            PrepareRunConfig.main(new String[]{root.toString()});
            var config=root.resolve("config/awaitingdawn-server.toml");
            String original=java.nio.file.Files.readString(config);
            check(original.contains("resurrectionIntervalDays = 10"));
            java.nio.file.Files.writeString(config,original.replace("resurrectionIntervalDays = 10","resurrectionIntervalDays = 7"));
            PrepareRunConfig.main(new String[]{root.toString()});
            check(java.nio.file.Files.readString(config).contains("resurrectionIntervalDays = 7"));
            var template=root.resolve("defaultconfigs/awaitingdawn-server.toml");
            check(java.nio.file.Files.mismatch(config,template)==-1);
            java.nio.file.Files.delete(config); java.nio.file.Files.delete(template);
            java.nio.file.Files.delete(root.resolve("config")); java.nio.file.Files.delete(root.resolve("defaultconfigs"));
            java.nio.file.Files.delete(root);
        } catch(Exception ex) { throw new AssertionError(ex); }
    }
    private static void stateTests() {
        AwaitingDawnServerConfig.SPEC.setConfig(com.electronwill.nightconfig.core.CommentedConfig.inMemory());
        var store=new PlayerDeathStateStore();
        var id=java.util.UUID.randomUUID();
        var r=store.record(id);
        r.pending=true; r.spectatorTransition=true; r.nextDay=10; r.lives=0;
        r.dimension="minecraft:the_nether"; r.deathPos=net.minecraft.core.BlockPos.asLong(12,65,-22);
        r.preMode=net.minecraft.world.level.GameType.ADVENTURE;
        CycleStateTransitions.advance(store,7,10,true,3);
        check(!r.eligible);
        CycleStateTransitions.advance(store,9,10,true,3); check(!r.eligible);
        CycleStateTransitions.advance(store,12,10,true,3); check(r.eligible); check(r.lives==3);
        r.lives=1;
        CycleStateTransitions.advance(store,12,10,true,3); check(r.lives==1);
        CycleStateTransitions.advance(store,5,10,true,3); check(r.eligible); check(r.lives==1);
        // Simulate a committed resurrection, then another death after the rewind.
        r.pending=false; r.eligible=false;
        r.pending=true; r.spectatorTransition=true; r.nextDay=10; r.lives=0;
        CycleStateTransitions.advance(store,9,10,true,3); check(!r.eligible);
        CycleStateTransitions.advance(store,10,10,true,3); check(r.eligible); check(r.lives==0);
        // Online transaction restores dead players separately; repeat milestones do not refill living players.
        r.lives=3;
        store.immortalAnnouncements.add(10L); store.mortalAnnouncements.add(11L);
        try {
            var file=java.nio.file.Files.createTempFile("awaitingdawn-state-", ".dat");
            net.minecraft.nbt.NbtIo.writeCompressed(store.save(new net.minecraft.nbt.CompoundTag()),file.toFile());
            var loaded=PlayerDeathStateStore.load(net.minecraft.nbt.NbtIo.readCompressed(file.toFile()));
            java.nio.file.Files.delete(file);
            var copy=loaded.record(id);
            check(copy.pending && copy.eligible && copy.lives==3);
            check(copy.spectatorTransition);
            check(copy.nextDay==10 && copy.refresh==10);
            check(copy.dimension.equals("minecraft:the_nether") && copy.deathPos==r.deathPos);
            check(copy.preMode==net.minecraft.world.level.GameType.ADVENTURE);
            check(loaded.observedDay==10 && loaded.latestMilestone==10);
            check(loaded.immortalAnnouncements.contains(10L) && loaded.mortalAnnouncements.contains(11L));
            check(!loaded.record(java.util.UUID.randomUUID()).pending);
            var russian=LocalizedMessages.message("ru_ru","deathCountdown","Alex",21,0);
            check(russian.getString().equals(" (возрождение через 21 день)"));
            check(!russian.getStyle().isBold());
            check(russian.getStyle().getColor().equals(net.minecraft.network.chat.TextColor.parseColor("gray")));
            var main=LocalizedMessages.message("en_us","deathMain","Alex",0,0);
            check(main.getString().equals("Alex died") && main.getStyle().isBold());
            check(main.getStyle().getColor().equals(net.minecraft.network.chat.TextColor.parseColor("red")));
            check(LocalizedMessages.message("de_de","livesRemaining","Alex",0,2).getString().contains("2 Leben"));
            check(LocalizedMessages.message("xx_xx","livesRemaining","Alex",0,2).getString().contains("2 lives"));
            AwaitingDawnServerConfig.TEXT.get("deathMain").set("💀 Тест {player}");
            check(LocalizedMessages.message("en_us","deathMain","Alex",0,0).getString().equals("💀 Тест Alex"));
            AwaitingDawnServerConfig.TEXT.get("deathMain").set("{invalid}");
            check(LocalizedMessages.message("en_us","deathMain","Alex",0,0).getString().equals("Alex died"));
            AwaitingDawnServerConfig.TEXT.get("deathMain").set("");
            CycleStateTransitions.advance(loaded,35,10,true,3);
            check(loaded.latestMilestone==30 && copy.refresh==30);
            copy.lives=1;
            CycleStateTransitions.refresh(copy,30,true,3); check(copy.lives==1);
            CycleStateTransitions.refresh(copy,40,false,3); check(copy.lives==1);
        } catch(java.io.IOException e) { throw new AssertionError(e); }
    }
}
