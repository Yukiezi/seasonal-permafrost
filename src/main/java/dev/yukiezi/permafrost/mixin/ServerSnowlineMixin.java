package dev.yukiezi.permafrost.mixin;

import com.teamtea.eclipticseasons.common.core.map.MapChecker;
import dev.yukiezi.permafrost.SnowlineCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapChecker.class)
public abstract class ServerSnowlineMixin {
    @Inject(method = "isAboveSnowLine(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void seasonalPermafrost$snowline(Level level, Biome biome, BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {
        if (level == null || !level.dimension().equals(Level.OVERWORLD)
                || !SnowlineCalculator.shouldOverride(biome)) return;
        cir.setReturnValue(SnowlineCalculator.hasSnowAt(level, biome, pos));
    }
}
