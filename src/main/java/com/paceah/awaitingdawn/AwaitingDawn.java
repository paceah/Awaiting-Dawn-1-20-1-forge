package com.paceah.awaitingdawn;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(AwaitingDawn.MOD_ID)
public final class AwaitingDawn {
    public static final String MOD_ID = "awaitingdawn";
    public AwaitingDawn() { PacketHandler.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AwaitingDawnServerConfig.SPEC);
        ModSounds.REGISTER.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
    }
}
