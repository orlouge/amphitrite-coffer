package io.github.orlouge.amphitritecoffer.mixin;

import io.github.orlouge.amphitritecoffer.AmphitriteCofferBlock;
import io.github.orlouge.amphitritecoffer.AmphitriteCofferMod;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Shadow
    private Block block;

    @Inject(method = "canBeNested()Z", at = @At("HEAD"), cancellable = true)
    public void onCanBeNested(CallbackInfoReturnable<Boolean> cir) {
        if (!AmphitriteCofferMod.ALLOW_NESTED_COFFERS && this.block instanceof AmphitriteCofferBlock) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
