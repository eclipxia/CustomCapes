package me.felek.customcapes.mixins;

import me.felek.customcapes.CustomCapeModule;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerInfo.class)
public class MixinPlayerInfo {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void updateCustomTextures(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerInfo info = (PlayerInfo) (Object) this;

        Object customCape = CustomCapeModule.capeFor(info.getProfile().getId());
        if (!(customCape instanceof ResourceLocation id)) {
            return;
        }

        PlayerSkin oldSkin = cir.getReturnValue();
        cir.setReturnValue(new PlayerSkin(
                oldSkin.texture(),
                oldSkin.textureUrl(),
                id,
                id,
                oldSkin.model(),
                oldSkin.secure()
        ));
    }
}
