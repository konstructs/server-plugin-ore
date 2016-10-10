package org.konstructs.ore;

import java.util.Random;
import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.Props;

import konstructs.plugin.KonstructsActor;
import konstructs.utils.*;
import konstructs.api.*;
import konstructs.api.messages.*;

public class Ore extends KonstructsActor {
    private final static LSystem SYSTEM = getLSystem();
    private final BlockFilter stoneFilter;
    private final OreConfig config;
    private final BlockMachine machine;
    private final Random random = new Random();
    private final Position radius;

    public Ore(ActorRef universe, OreConfig config) {
        super(universe);
        this.config = config;
        this.radius = new Position(config.getRadius(),config.getRadius(),config.getRadius());
        this.machine = getBlockMachine(config);
        this.stoneFilter = BlockFilterFactory.withBlockTypeId(config.getSpawnsIn());
    }

    private int randomDistance() {
        int r = random.nextInt(config.getMaxDistance() - config.getMinDistance());

        if(r>=0) {
            return r + config.getMinDistance();
        } else {
            return r - config.getMinDistance();
        }
    }

    private void tryToSpawnOre(Position pos) {
        Position growthPos = pos
            .addX(randomDistance())
            .addY(randomDistance())
            .addZ(randomDistance());
        BoxAround box = new BoxAround(growthPos, radius);
        boxShapeQuery(box);
    }

    private void growVein(Position start) {
        String vein = "a";
        for(int i = 0;i < config.getGenerations(); i++)
            vein = SYSTEM.iterate(vein);
        replaceBlocks(stoneFilter, machine.interpret(vein, start));
    }

    @Override
    public void onBoxShapeQueryResult(BoxShapeQueryResult result) {
        for(BlockTypeId block: result.getBlocks()) {
            if(!(block.equals(config.getSpawnsIn()) || block.equals(config.getOre()))) {
                /* Failed, box must be solidly of type "spawns in" or the ore itself*/
                return;
            }
        }
        growVein(((BoxAround)result.getBox()).getCenter());
    }

    @Override
    public void onReceive(Object message) {
        if(message instanceof SpawnVein) {
            tryToSpawnOre(((SpawnVein)message).getPosition());
        } else {
            super.onReceive(message); // Handle konstructs messages
        }
    }

    public static Props
        props(ActorRef universe, OreConfig config) {
        Class currentClass = new Object() { }.getClass().getEnclosingClass();
        return Props.create(currentClass, universe, config);
    }

    private static BlockMachine getBlockMachine(OreConfig config) {
        Map<Character, BlockTypeId> blockMapping = new HashMap<Character, BlockTypeId>();
        blockMapping.put('a', config.getOre());
        return new BlockMachine(blockMapping);
    }

    private static LSystem getLSystem() {
        /* Rules that randomly turns */
        ProbabilisticProduction veinDirections[] = {
            new ProbabilisticProduction(12, "a&a"),
            new ProbabilisticProduction(12, "a^a"),
            new ProbabilisticProduction(12, "a-a"),
            new ProbabilisticProduction(12, "a+a"),
            new ProbabilisticProduction(13, "a\\a"),
            new ProbabilisticProduction(13, "a/a"),
            new ProbabilisticProduction(13, "aa"),
            new ProbabilisticProduction(13, "a[&[a][-a][--a][+a]]") /* Makes the vein denser */
        };

        ProductionRule[] rules = {
            new ProbabilisticProductionRule("a", veinDirections)
        };

        return new LSystem(rules);
    }

}
