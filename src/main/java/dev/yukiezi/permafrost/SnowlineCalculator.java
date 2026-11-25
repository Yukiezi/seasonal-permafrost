package dev.yukiezi.permafrost;

import com.teamtea.eclipticseasons.api.EclipticSeasonsApi;
import com.teamtea.eclipticseasons.api.constant.solar.SolarTerm;
import com.teamtea.eclipticseasons.api.util.EclipticUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class SnowlineCalculator {
    public static final int SUMMER_HEIGHT = 250;
    public static final int AUTUMN_FLOOR = 115;
    public static final int WINTER_HEIGHT = 0;

    private SnowlineCalculator() {}

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
