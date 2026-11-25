package dev.yukiezi.permafrost.mixin;

import com.teamtea.eclipticseasons.common.core.biome.BiomeClimateManager;
import dev.yukiezi.permafrost.SnowlineCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeClimateManager.class)
public abstract class ClientSnowlineMixin {
    @Inject(method = "getSnowLine(Lnet/minecraft/world/level/biome/Biome;Z)I", at = @At("HEAD"), cancellable = true)
    private static void seasonalPermafrost$clientSnowline(Biome biome, boolean isServer,
            CallbackInfoReturnable<Integer> cir) {
        if (isServer || !SnowlineCalculator.shouldOverride(biome)) return;
        var level = Minecraft.getInstance().level;
        if (level != null) cir.setReturnValue(SnowlineCalculator.getSnowline(level));
    }
}
