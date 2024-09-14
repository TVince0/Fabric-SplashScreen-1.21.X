package net.tvince0.splashscreen;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SplashScreen implements ModInitializer {
    public static final String MOD_ID = "splashscreen";
    public static final SoundEvent MOJANG_LOGO_SOUND = SoundEvent.of(Identifier.of(MOD_ID, "mojang_logo"));

    @Override
    public void onInitialize() {
        Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "mojang_logo"), MOJANG_LOGO_SOUND);
    }
}
