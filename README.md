# Seasonal Permafrost

A standalone Fabric addon for Minecraft 26.2 that integrates with Ecliptic Seasons: Fabricated. It does not depend on Homeostatic and does not modify player/body temperature.

## Intended seasonal permafrost boundary

The addon overrides the Ecliptic Seasons snowline altitude for eligible Overworld biomes.

- **Summer:** boundary stays at Y=250.
- **Very late summer through autumn:** boundary descends linearly from Y=250 to a configurable autumn floor, default Y=115 (midpoint of the requested Y=110–120 range).
- **At the beginning of winter:** boundary drops sharply from that autumn floor to Y=0.
- **Winter:** boundary remains at Y=0.
- **End of winter through spring:** boundary rises gradually and linearly from Y=0 back to Y=250, reaching the top by early summer.

Warm biomes (for example deserts, jungles, savannas, and badlands) are excluded. Naturally cold/snowy biomes should retain Ecliptic Seasons' existing behavior unless explicitly configured otherwise.

## Compatibility target

- Minecraft 26.2
- Fabric
- Ecliptic Seasons: Fabricated 0.15.5.1

## Status

Initial implementation committed. GitHub Actions compile verification is running; in-game validation is still pending. The snowline hook is applied through Ecliptic Seasons' level-aware snowline check, so both client and server use the active world's solar-term progress.

## Important distinction

This addon changes the altitude threshold used by Ecliptic Seasons. It does not itself guarantee that every block above the threshold becomes a permanent frost block; actual snow placement, persistence, and melting must be verified against Ecliptic Seasons' snow mechanics in-game.
