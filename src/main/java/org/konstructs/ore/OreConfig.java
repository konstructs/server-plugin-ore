package org.konstructs.ore;

import com.typesafe.config.Config;
import konstructs.api.BlockTypeId;

public class OreConfig {
    private final static String SPAWNS_IN_KEY = "spawns-in";
    private final static String STONE_RADIUS_KEY = "radius";
    private final static String VEIN_GENERATIONS_KEY = "generations";
    private final static String VEIN_SPAWN_PROBABILITY_KEY = "probability";
    private final static String MAX_DISTANCE_KEY = "max-distance";

    private final BlockTypeId ore;
    private final BlockTypeId spawnsIn;
    private final int generations;
    private final int radius;
    private final int probability;
    private final int maxDistance;
    private final int minDistance;

    OreConfig(String oreType, Config config) {
        this.ore = BlockTypeId.fromString(oreType);
        this.spawnsIn = BlockTypeId.fromString(config.getString(SPAWNS_IN_KEY));
        this.radius = config.getInt(STONE_RADIUS_KEY);
        this.generations = config.getInt(VEIN_GENERATIONS_KEY);
        this.probability = config.getInt(VEIN_SPAWN_PROBABILITY_KEY);
        this.maxDistance = config.getInt(MAX_DISTANCE_KEY);
        this.minDistance = radius + 1;
    }

    public BlockTypeId getOre() {
        return ore;
    }

    public BlockTypeId getSpawnsIn() {
        return spawnsIn;
    }

    public int getGenerations() {
        return generations;
    }

    public int getRadius() {
        return radius;
    }

    public int getProbability() {
        return probability;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public int getMinDistance() {
        return minDistance;
    }

}
