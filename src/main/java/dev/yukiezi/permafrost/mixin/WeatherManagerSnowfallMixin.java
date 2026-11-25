package dev.yukiezi.permafrost.mixin;

import dev.yukiezi.permafrost.SnowlineCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import com.teamtea.eclipticseasons.common.core.biome.WeatherManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherManager.class)
public abstract class WeatherManagerSnowfallMixin {
    @Inject(
            method = "getPrecipitationAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void seasonalPermafrost$precipitation(Level level, Biome biome, BlockPos pos,
            CallbackInfoReturnable<Precipitation> cir) {
        if (cir.getReturnValue() == Precipitation.NONE || level == null
                || !level.dimension().equals(Level.OVERWORLD)
                || !SnowlineCalculator.shouldOverride(biome)) return;
        cir.setReturnValue(SnowlineCalculator.hasSnowAt(level, biome, pos)
                ? Precipitation.SNOW : Precipitation.RAIN);
    }

    @Inject(
            method = "getRainOrSnow(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void seasonalPermafrost$rainOrSnow(Level level, Biome biome, BlockPos pos,
            CallbackInfoReturnable<Precipitation> cir) {
        if (cir.getReturnValue() == Precipitation.NONE || !SnowlineCalculator.shouldOverride(biome)) return;
        cir.setReturnValue(pos.getY() > SnowlineCalculator.getSnowline(level)
                ? Precipitation.SNOW : Precipitation.RAIN);
    }
}
