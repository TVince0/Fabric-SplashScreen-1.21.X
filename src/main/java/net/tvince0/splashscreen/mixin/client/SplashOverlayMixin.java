package net.tvince0.splashscreen.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.tvince0.splashscreen.SplashScreen;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Unique
    private static boolean firstLoad = true;

    @Shadow
    @Final
    private ResourceReload reload;

    @Shadow
    private float progress;

    @Unique
    private int animProgress = 0;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private boolean reloading;

    @Unique
    private boolean animationStarting = false;

    @Unique
    private boolean animationEnded = false;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceReload;getProgress()F"))
    private float getProgress(ResourceReload instance) {
        return this.reload.getProgress();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(Lnet/minecraft/client/render/RenderLayer;IIIII)V"))
    private void fill(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIFFIIII)V", ordinal = 0))
    private void drawTexture0(DrawContext context, Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        double d = Math.min(context.getScaledWindowWidth() * 0.75D, context.getScaledWindowHeight()) * 0.25D;
        double e = d * 4.0D;
        int r = (int)e;
        if (this.progress > 0.0F && !this.animationStarting && firstLoad) {
            this.reload.whenComplete().thenAccept(object -> {
                this.client.getSoundManager().play(PositionedSoundInstance.master(SplashScreen.MOJANG_LOGO_SOUND, 1.0F));
                getAnimationThread().start();
            });
            this.animationStarting = true;
        }
        if (firstLoad) {
            if (!this.reload.isComplete()) {
                context.fill(RenderLayer.getGuiOverlay(), 0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0);
            } else {
                context.drawTexture(getMojang(this.animProgress), x, y, r, (int)d, u, v, regionWidth, regionHeight + 60, textureWidth, textureHeight);
            }
        } else {
            context.drawTexture(getMojang(100), x, y, r, (int)d, u, v, regionWidth, regionHeight + 60, textureWidth, textureHeight);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIFFIIII)V", ordinal = 1))
    private void drawTexture1(DrawContext context, Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
    }

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/SplashOverlay;reloading:Z", opcode = 180, ordinal = 2))
    private boolean isReloading(SplashOverlay instance) {
        return ((firstLoad && !this.animationEnded) || this.reloading);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V"))
    private void init(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        firstLoad = false;
    }

    @Unique
    private Identifier getMojang(int index) {
        return Identifier.of(SplashScreen.MOD_ID, "textures/gui/title/mojang/mojang" + index + ".png");
    }

    @Unique
    @NotNull
    private Thread getAnimationThread() {
        Thread animthread = new Thread(() -> {
            this.animProgress = 0;
            for (int i = 0; i < 100; i++) {
                this.animProgress++;
                try {
                    Thread.sleep(33L);
                } catch (InterruptedException interruptedException) {}
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException interruptedException) {}

            this.animationEnded = true;
        });

        animthread.setName("animthread");
        return animthread;
    }
}
