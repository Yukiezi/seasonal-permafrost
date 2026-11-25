package dev.yukiezi.permafrost;

import com.teamtea.eclipticseasons.api.EclipticSeasonsApi;
import com.teamtea.eclipticseasons.api.constant.solar.SolarTerm;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class SnowlineCalculator {
    public static final int SUMMER_HEIGHT = 250;
    public static final int AUTUMN_FLOOR = 115;
    public static final int WINTER_HEIGHT = 0;

    private SnowlineCalculator() {}

    public static boolean shouldOverride(Biome biome) {
        float temperature = biome.getBaseTemperature();
        // Keep naturally cold biomes on Ecliptic Seasons' own snowline.
        // Exclude deserts, jungles, savannas, badlands and similarly hot biomes.
        return temperature > 0.2F && temperature < 0.9F;
    }

    public static int getSnowline(Level level) {
        EclipticSeasonsApi api = EclipticSeasonsApi.getInstance();
        if (level == null || !api.isSeasonEnabled(level)) return SUMMER_HEIGHT;

        SolarTerm term = api.getSolarTerm(level);
        if (term == null || !term.isValid()) return SUMMER_HEIGHT;

        int ordinal = term.ordinal();
        int daysInTerm = Math.max(1, api.getLastingDaysOfEachTerm(level));
        double dayProgress = Math.max(0L, Math.floorMod(level.getDayTime(), 24000L)) / 24000.0;
        double termProgress = (api.getDayInTerm(level) + dayProgress) / daysInTerm;
        termProgress = Math.max(0.0, Math.min(1.0, termProgress));

        // Spring: rise from sea level to the summer cap, reaching it at term 6.
        if (ordinal <= 5) {
            double progress = (ordinal + termProgress) / 6.0;
            return interpolate(WINTER_HEIGHT, SUMMER_HEIGHT, progress);
        }

        // Summer remains at the cap until the final solar term.
        if (ordinal <= 10) return SUMMER_HEIGHT;

        // Late summer through the end of autumn: descend linearly to Y=115.
        if (ordinal <= 17) {
            double progress = (ordinal - 11 + termProgress) / 7.0;
            return interpolate(SUMMER_HEIGHT, AUTUMN_FLOOR, progress);
        }

        // Winter begins with the deliberate sharp drop to sea level.
        return WINTER_HEIGHT;
    }

    private static int interpolate(int from, int to, double progress) {
        return (int) Math.round(from + (to - from) * Math.max(0.0, Math.min(1.0, progress)));
    }
}
