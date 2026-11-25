package dev.yukiezi.permafrost;

import com.teamtea.eclipticseasons.api.EclipticSeasonsApi;
import com.teamtea.eclipticseasons.api.constant.solar.SolarTerm;
import com.teamtea.eclipticseasons.api.util.EclipticUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class SnowlineCalculator {
    public static final int SUMMER_HEIGHT = 250;
    public static final int AUTUMN_FLOOR = 115;
    public static final int WINTER_HEIGHT = 0;

    private static final int VARIATION_BAND = 10;

    private SnowlineCalculator() {}

    /**
     * Uses one stable pseudo-random value per X/Z column, shared by rendering,
     * precipitation and snow placement. The chance rises by 10% per block
     * through the ten-block band above the calculated snowline.
     */
    public static boolean hasSnowAt(Level level, Biome biome, BlockPos pos) {
        if (level == null || pos == null || !shouldOverride(biome)) return false;

        int snowline = getSnowline(level);
        int chance = (pos.getY() - snowline) * 10;
        if (chance <= 0) return false;
        if (chance >= 100) return true;

        long hash = (long) pos.getX() * 0x9E3779B97F4A7C15L
                ^ (long) pos.getZ() * 0xC2B2AE3D27D4EB4FL;
        hash ^= hash >>> 30;
        hash *= 0xBF58476D1CE4E5B9L;
        hash ^= hash >>> 27;
        hash *= 0x94D049BB133111EBL;
        hash ^= hash >>> 31;

        int percentile = (int) Math.floorMod(hash, 100L);
        return percentile < chance;
    }

    public static boolean shouldOverride(Biome biome) {
        float temperature = biome.getBaseTemperature();
        // Naturally cold biomes retain Ecliptic Seasons' own behavior.
        // Hot/arid/tropical biomes are outside the temperate permafrost system.
        return temperature > 0.2F && temperature < 0.9F;
    }

    public static int getSnowline(Level level) {
        EclipticSeasonsApi api = EclipticSeasonsApi.getInstance();
        if (level == null || !level.dimension().equals(Level.OVERWORLD)
                || !api.isSeasonEnabled(level)) return SUMMER_HEIGHT;

        SolarTerm term = api.getSolarTerm(level);
        if (term == null || !term.isValid()) return SUMMER_HEIGHT;

        int ordinal = term.ordinal();
        int daysInTerm = Math.max(1, api.getLastingDaysOfEachTerm(level));

        // Ecliptic Seasons advances its own solar calendar using the world's
        // default clock, not Level#getGameTime() or a hard-coded 24000 ticks.
        // This mirrors its own client progress calculation, but retains full
        // precision instead of the UI's 10-step display rounding.
        int dayLength = Math.max(1, EclipticUtil.getDayLengthInMinecraft(level));
        double dayProgress = Math.floorMod(level.getDefaultClockTime(), (long) dayLength)
                / (double) dayLength;
        double termProgress = (api.getDayInTerm(level) + dayProgress) / daysInTerm;
        termProgress = Math.max(0.0, Math.min(1.0, termProgress));

        // SolarTerm enum order is six terms per season:
        // 0-5 spring, 6-11 summer, 12-17 autumn, 18-23 winter.
        // Spring: rise continuously from Y=0 to Y=250 by the beginning of summer.
        if (ordinal <= 5) {
            double progress = (ordinal + termProgress) / 6.0;
            return interpolate(WINTER_HEIGHT, SUMMER_HEIGHT, progress);
        }

        // Hold the summer cap until the final quarter of the last summer solar term.
        if (ordinal <= 10 || (ordinal == 11 && termProgress < 0.75)) {
            return SUMMER_HEIGHT;
        }

        // Final quarter of summer plus all six autumn terms: Y=250 -> Y=115.
        if (ordinal <= 17) {
            double progress = ordinal == 11
                    ? (termProgress - 0.75) / 6.25
                    : (0.25 + ordinal - 12 + termProgress) / 6.25;
            return interpolate(SUMMER_HEIGHT, AUTUMN_FLOOR, progress);
        }

        // At the first winter solar term, drop sharply to sea level and stay there.
        return WINTER_HEIGHT;
    }

    private static int interpolate(int from, int to, double progress) {
        return (int) Math.round(from + (to - from) * Math.max(0.0, Math.min(1.0, progress)));
    }
}
