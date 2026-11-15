package com.paceah.awaitingdawn;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class PacketHandler {
    private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(AwaitingDawn.MOD_ID,"events"),()->"2","2"::equals,"2"::equals);
    public record AutoRespawn(boolean enabled) {}
    public record Display(java.util.Map<String,net.minecraft.network.chat.Component> variants,boolean title,int fadeIn,int stay,int fadeOut) {}
    public static void init() {
        CHANNEL.messageBuilder(AutoRespawn.class,0,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->b.writeBoolean(m.enabled())).decoder(b->new AutoRespawn(b.readBoolean()))
            .consumerMainThread((m,ctx)-> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.paceah.awaitingdawn.client.ClientDeathTransition.set(m.enabled()));
                ctx.get().setPacketHandled(true);
            }).add();
        // S2C only: packets contain server-owned formatted text, never gameplay permissions.
        CHANNEL.messageBuilder(Display.class,1,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)-> {b.writeVarInt(m.variants().size()); m.variants().forEach((locale,text)-> { b.writeUtf(locale,16); b.writeComponent(text); }); b.writeBoolean(m.title()); b.writeVarInt(m.fadeIn()); b.writeVarInt(m.stay()); b.writeVarInt(m.fadeOut());})
            .decoder(b-> {
                int count=b.readVarInt();
                if(count<1 || count>LocalizedMessages.LOCALES.size()) throw new IllegalArgumentException("Invalid locale count");
                java.util.Map<String,net.minecraft.network.chat.Component> variants=new java.util.HashMap<>();
                for(int i=0;i<count;i++) variants.put(b.readUtf(16),b.readComponent());
                return new Display(java.util.Map.copyOf(variants),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt());
            })
            .consumerMainThread((m,ctx)-> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.paceah.awaitingdawn.client.ClientDeathTransition.display(m));
                ctx.get().setPacketHandled(true);
            }).add();
    }
    public static void display(ServerPlayer p,java.util.Map<String,net.minecraft.network.chat.Component> variants,boolean title) {
        CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Display(java.util.Map.copyOf(variants),title,AwaitingDawnServerConfig.FADE_IN.get(),AwaitingDawnServerConfig.STAY.get(),AwaitingDawnServerConfig.FADE_OUT.get()));
    }
    public static void autoRespawn(ServerPlayer p,boolean enabled) { CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new AutoRespawn(enabled)); }
}