package me.felek.customcapes.mixins;

import me.felek.customcapes.CustomCapeModule;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerInfo.class)
public class MixinPlayerInfo {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void updateCustomTextures(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerInfo info = (PlayerInfo) (Object) this;

        Object customCape = CustomCapeModule.capeFor(info.getProfile().id());
        if (!(customCape instanceof Identifier id)) {
            return;
        }

        // texturePath is used as-is for registered dynamic textures, so it must be the registered id too
        ClientAsset.Texture texture = new ClientAsset.ResourceTexture(id, id);
        PlayerSkin oldSkin = cir.getReturnValue();
        cir.setReturnValue(new PlayerSkin(oldSkin.body(), texture, texture, oldSkin.model(), oldSkin.secure()));
    }
}
