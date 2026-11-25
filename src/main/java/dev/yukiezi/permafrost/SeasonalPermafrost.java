package dev.yukiezi.permafrost;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerLevel;

public final class SeasonalPermafrost implements ModInitializer {
    public static final String MOD_ID = "seasonal_permafrost";
    private static volatile ServerLevel overworld;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> overworld = server.overworld());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> overworld = null);
    }

    public static ServerLevel getOverworld() {
        return overworld;
    }
}
