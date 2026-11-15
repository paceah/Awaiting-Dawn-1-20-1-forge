package com.paceah.awaitingdawn;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.*;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AwaitingDawn.MOD_ID);
    public static final RegistryObject<SoundEvent> MORTALITY = register("mortality_return");
    public static final RegistryObject<SoundEvent> RESURRECTION = register("resurrection");

    private static RegistryObject<SoundEvent> register(String name) {
        return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(
            new ResourceLocation(AwaitingDawn.MOD_ID, name)));
    }
}