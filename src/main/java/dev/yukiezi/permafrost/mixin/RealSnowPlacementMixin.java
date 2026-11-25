package dev.yukiezi.permafrost.mixin;

import com.teamtea.eclipticseasons.common.handler.CustomRandomTickHandler;
import dev.yukiezi.permafrost.SnowlineCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomRandomTickHandler.class)
public abstract class RealSnowPlacementMixin {
    @Inject(
            method = "checkExtraSnowCondition(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void seasonalPermafrost$allowRealSnow(ServerLevel level, Holder<Biome> biomeHolder,
            BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Biome biome = biomeHolder.value();
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)
                || !SnowlineCalculator.shouldOverride(biome)) return;

        boolean aboveSnowline = SnowlineCalculator.hasSnowAt(level, biome, pos);
        boolean validHeight = pos.getY() >= level.getMinY() && pos.getY() < level.getMaxY();
        boolean lowBlockLight = level.getBrightness(LightLayer.BLOCK, pos) < 10;
        BlockState state = level.getBlockState(pos);
        boolean replaceable = state.isAir() || state.is(Blocks.SNOW);
        boolean canSurvive = Blocks.SNOW.defaultBlockState().canSurvive(level, pos);

        cir.setReturnValue(aboveSnowline && validHeight && lowBlockLight && replaceable && canSurvive);
    }
}
