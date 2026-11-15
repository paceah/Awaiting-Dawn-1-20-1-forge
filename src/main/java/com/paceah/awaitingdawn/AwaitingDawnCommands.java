package com.paceah.awaitingdawn;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=AwaitingDawn.MOD_ID)
public final class AwaitingDawnCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("awaitingdawn").requires(s->s.hasPermission(2))
            .then(Commands.literal("status").executes(c->status(c.getSource(),c.getSource().getPlayerOrException()))
                .then(Commands.argument("player",EntityArgument.player()).executes(c->status(c.getSource(),EntityArgument.getPlayer(c,"player")))))
            .then(Commands.literal("debug")
                .then(Commands.literal("setday").then(Commands.argument("day",LongArgumentType.longArg(0,Long.MAX_VALUE/24000-1000000)).executes(c-> {
                    if(!debug(c.getSource())) return 0;
                    c.getSource().getServer().overworld().setDayTime(LongArgumentType.getLong(c,"day")*24000);
                    WorldCycleManager.reconcile(c.getSource().getServer());
                    done(c.getSource()); return Command.SINGLE_SUCCESS;
                })))
                .then(Commands.literal("setlives").then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("count",IntegerArgumentType.integer(0,1000000)).executes(c-> {
                    if(!debug(c.getSource())) return 0;
                    var store=PlayerDeathStateStore.get(c.getSource().getServer());
                    store.record(EntityArgument.getPlayer(c,"player").getUUID()).lives=Math.min(AwaitingDawnServerConfig.MAX_LIVES.get(),IntegerArgumentType.getInteger(c,"count")); store.setDirty();
                    done(c.getSource()); return Command.SINGLE_SUCCESS;
                }))))));
    }
    private static int status(CommandSourceStack source,ServerPlayer p) {
        var r=PlayerDeathStateStore.get(p.server).record(p.getUUID());
        long day=WorldCycleManager.day(p.server); int interval=AwaitingDawnServerConfig.INTERVAL.get();
        boolean immortal=CalendarRules.immortal(day,interval);
        long next=immortal?day:CalendarRules.next(day,interval);
        ServerPlayer recipient=source.getEntity() instanceof ServerPlayer sp?sp:p;
        source.sendSuccess(()->LocalizedMessages.message(recipient,"status",p.getScoreboardName(),day,r.lives)
            .append(Component.translatable("awaitingdawn.statusFlags",immortal,next,immortal?0:next-day,r.pending)),false);
        return Command.SINGLE_SUCCESS;
    }
    private static boolean debug(CommandSourceStack source) {
        if(AwaitingDawnServerConfig.DEBUG.get()) return true;
        source.sendFailure(source.getEntity() instanceof ServerPlayer p?LocalizedMessages.message(p,"debugDisabled","",0,0):Component.literal("Debug commands disabled")); return false;
    }
    private static void done(CommandSourceStack source) {
        source.sendSuccess(()->source.getEntity() instanceof ServerPlayer p?LocalizedMessages.message(p,"debugDone","",0,0):Component.literal("Debug settings applied"),false);
    }
}